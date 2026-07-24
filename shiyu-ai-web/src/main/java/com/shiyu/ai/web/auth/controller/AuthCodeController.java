package com.shiyu.ai.web.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.dal.auth.dataobject.AuthCodeDO;
import com.shiyu.ai.dal.auth.dataobject.RoleScopeAuthCodeDO;
import com.shiyu.ai.dal.auth.mapper.AuthCodeMapper;
import com.shiyu.ai.dal.auth.mapper.RoleScopeAuthCodeMapper;
import com.shiyu.ai.dal.auth.repository.RoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限码管理 Controller
 *
 * 注意：所有参数均通过 @RequestParam 或 @RequestBody 传入，不使用 @PathVariable。
 */
@Slf4j
@Tag(name = "Auth Code", description = "Auth Code")
@RestController
@RequestMapping("/auth-code")
public class AuthCodeController {

    private final AuthCodeMapper authCodeMapper;
    private final RoleScopeAuthCodeMapper roleScopeAuthCodeMapper;
    private final RoleRepository roleRepository;

    public AuthCodeController(AuthCodeMapper authCodeMapper,
                              RoleScopeAuthCodeMapper roleScopeAuthCodeMapper,
                              RoleRepository roleRepository) {
        this.authCodeMapper = authCodeMapper;
        this.roleScopeAuthCodeMapper = roleScopeAuthCodeMapper;
        this.roleRepository = roleRepository;
    }

    @Operation(summary = "List Auth Codes")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/list")
    public Result<List<AuthCodeDO>> list() {
        QueryWrapper query = QueryWrapper.create()
                .where(AuthCodeDO::getStatus).eq(1)
                .and(AuthCodeDO::getDelFlag).eq(0);
        return Result.success(authCodeMapper.selectListByQuery(query));
    }

    @Operation(summary = "Create Auth Code")
    @SaCheckPermission("system:auth-code:create")
    @PostMapping("/create")
    public Result<AuthCodeDO> create(@RequestBody AuthCodeDO authCode) {
        if (authCode == null || authCode.getCode() == null || authCode.getCode().isBlank()
                || authCode.getCode().length() > 64) {
            return Result.fail("权限编码不能为空且长度不能超过64");
        }
        authCode.setStatus(1);
        authCode.setDelFlag(0);
        authCode.setCreateTime(LocalDateTime.now());
        authCodeMapper.insertSelective(authCode);
        return Result.success(authCode);
    }

    @Operation(summary = "Update Auth Code")
    @SaCheckPermission("system:auth-code:update")
    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @RequestBody AuthCodeDO authCode) {
        AuthCodeDO existing = authCodeMapper.selectOneById(id);
        if (existing == null) {
            return Result.fail("权限码不存在");
        }
        if (authCode == null || authCode.getCode() == null || authCode.getCode().isBlank()
                || authCode.getCode().length() > 64) {
            return Result.fail("权限编码不能为空且长度不能超过64");
        }
        authCode.setId(id);
        authCode.setStatus(existing.getStatus());
        authCode.setDelFlag(existing.getDelFlag());
        authCode.setCreateTime(existing.getCreateTime());
        authCode.setUpdateTime(LocalDateTime.now());
        authCodeMapper.update(authCode);
        return Result.success();
    }

    @Operation(summary = "Delete Auth Code")
    @SaCheckPermission("system:auth-code:delete")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        AuthCodeDO existing = authCodeMapper.selectOneById(id);
        if (existing == null) {
            return Result.fail("权限码不存在");
        }
        existing.setDelFlag(1);
        existing.setUpdateTime(LocalDateTime.now());
        authCodeMapper.update(existing);
        return Result.success();
    }

    @Operation(summary = "为角色授权权限码")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/grant")
    public Result<Void> grant(@RequestParam Long roleId, @RequestBody List<Long> authCodeIds) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        Long tenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null || !roleRepository.isRoleInScope(roleId, currentTenantId)
                || authCodeIds == null || authCodeIds.isEmpty()) {
            return Result.fail("角色、作用域或权限码参数无效");
        }
        List<AuthCodeDO> validAuthCodes = authCodeMapper.selectListByQuery(QueryWrapper.create()
                .where(AuthCodeDO::getId).in(authCodeIds)
                .and(AuthCodeDO::getStatus).eq(1)
                .and(AuthCodeDO::getDelFlag).eq(0));
        if (validAuthCodes.size() != authCodeIds.stream().distinct().count()) {
            return Result.fail("包含不存在或已停用的权限码");
        }
        Set<Long> existingIds = new HashSet<>(roleScopeAuthCodeMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(RoleScopeAuthCodeDO::getRoleId).eq(roleId)
                        .and(RoleScopeAuthCodeDO::getScopedTenantId).eq(currentTenantId)
                        .and(RoleScopeAuthCodeDO::getStatus).eq(1)
                        .and(RoleScopeAuthCodeDO::getDelFlag).eq(0))
                .stream().map(RoleScopeAuthCodeDO::getAuthCodeId).toList());
        List<RoleScopeAuthCodeDO> records = authCodeIds.stream().distinct()
                .filter(authCodeId -> !existingIds.contains(authCodeId)).map(authCodeId -> {
            RoleScopeAuthCodeDO item = new RoleScopeAuthCodeDO();
            item.setRoleId(roleId);
            item.setAuthCodeId(authCodeId);
            item.setTenantId(tenantId);
            item.setScopedTenantId(currentTenantId);
            item.setStatus(1);
            item.setDelFlag(0);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            return item;
        }).toList();
        if (!records.isEmpty()) {
            roleScopeAuthCodeMapper.insertBatch(records);
        }
        return Result.success();
    }

    @Operation(summary = "取消角色权限授权")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/revoke")
    public Result<Void> revoke(@RequestParam Long roleId, @RequestParam Long authCodeId) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null || !roleRepository.isRoleInScope(roleId, currentTenantId)) {
            return Result.fail("角色不属于当前租户作用域");
        }
        QueryWrapper query = QueryWrapper.create()
                .where(RoleScopeAuthCodeDO::getRoleId).eq(roleId)
                .and(RoleScopeAuthCodeDO::getAuthCodeId).eq(authCodeId)
                .and(RoleScopeAuthCodeDO::getScopedTenantId).eq(currentTenantId);
        roleScopeAuthCodeMapper.deleteByQuery(query);
        return Result.success();
    }
}
