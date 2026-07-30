package com.shiyu.ai.web.auth;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.auth.request.AuthCodePageRequest;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.auth.vo.AuthCodeOptionVO;
import com.shiyu.ai.dal.auth.dataobject.AuthCodeDO;
import com.shiyu.ai.dal.auth.dataobject.RoleScopeAuthCodeDO;
import com.shiyu.ai.dal.auth.dataobject.TenantAuthCodeDO;
import com.shiyu.ai.dal.auth.mapper.AuthCodeMapper;
import com.shiyu.ai.dal.auth.mapper.RoleScopeAuthCodeMapper;
import com.shiyu.ai.dal.auth.mapper.TenantAuthCodeMapper;
import com.shiyu.ai.dal.auth.repository.RoleRepository;
import com.shiyu.ai.dal.auth.repository.TenantRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mybatisflex.core.query.QueryMethods.column;

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
    private final TenantAuthCodeMapper tenantAuthCodeMapper;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;

    public AuthCodeController(AuthCodeMapper authCodeMapper,
                              RoleScopeAuthCodeMapper roleScopeAuthCodeMapper,
                              TenantAuthCodeMapper tenantAuthCodeMapper,
                              RoleRepository roleRepository,
                              TenantRepository tenantRepository) {
        this.authCodeMapper = authCodeMapper;
        this.roleScopeAuthCodeMapper = roleScopeAuthCodeMapper;
        this.tenantAuthCodeMapper = tenantAuthCodeMapper;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
    }

    @Operation(summary = "List Auth Codes")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/list")
    public Result<List<AuthCodeOptionVO>> list() {
        QueryWrapper query = buildTenantAuthCodeQuery();
        return Result.success(authCodeMapper.selectListByQuery(query).stream()
                .map(this::toOption)
                .toList());
    }

    @Operation(summary = "查询角色已分配的权限码")
    @SaCheckPermission("system:role:list")
    @GetMapping("/roles/list")
    public Result<List<String>> listRoleAuthCodes(@RequestParam Long roleId,
                                                  @RequestParam Long tenantId) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (!isValidScope(roleId, currentTenantId, tenantId)) {
            return Result.fail("角色不属于当前租户作用域");
        }
                List<String> codes = roleScopeAuthCodeMapper.selectListByQuery(QueryWrapper.create()
                        .where(RoleScopeAuthCodeDO::getRoleId).eq(roleId)
                        .and(RoleScopeAuthCodeDO::getTenantId).eq(tenantId)
                        .and(RoleScopeAuthCodeDO::getStatus).eq(1)
                        .and(RoleScopeAuthCodeDO::getDelFlag).eq(0))
                .stream()
                .map(RoleScopeAuthCodeDO::getAuthCodeId)
                .map(id -> authCodeMapper.selectOneById(id))
                .filter(java.util.Objects::nonNull)
                .map(AuthCodeDO::getCode)
                .distinct()
                .toList();
        return Result.success(codes);
    }

    @Operation(summary = "权限码下拉选项")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/options")
    public Result<List<AuthCodeOptionVO>> options() {
        QueryWrapper query = buildTenantAuthCodeQuery();
        List<AuthCodeOptionVO> options = authCodeMapper.selectListByQuery(query).stream()
                .map(this::toOption)
                .toList();
        return Result.success(options);
    }

    private AuthCodeOptionVO toOption(AuthCodeDO authCode) {
        String[] parts = authCode.getCode() == null
                ? new String[0]
                : authCode.getCode().split(":");
        AuthCodeOptionVO option = new AuthCodeOptionVO();
        option.setId(authCode.getId());
        option.setName(authCode.getName());
        option.setCode(authCode.getCode());
        option.setModule(parts.length > 0 ? parts[0] : "");
        option.setAction(parts.length > 1 ? parts[parts.length - 1] : "");
        option.setResource(parts.length > 2
                ? String.join(":", java.util.Arrays.copyOfRange(parts, 1, parts.length - 1))
                : "");
        option.setStatus(authCode.getStatus());
        option.setCreateTime(authCode.getCreateTime());
        return option;
    }

    @Operation(summary = "Create Auth Code")
    @SaCheckPermission("system:auth-code:create")
    @PostMapping("/create")
    @Transactional(rollbackFor = Exception.class)
    public Result<AuthCodeDO> create(@RequestBody AuthCodeDO authCode) {
        if (authCode == null || authCode.getCode() == null || authCode.getCode().isBlank()
                || authCode.getCode().length() > 64) {
            return Result.fail("权限编码不能为空且长度不能超过64");
        }
        if (existsByCode(authCode.getCode(), null)) {
            return Result.fail("权限编码已存在");
        }
        authCode.setStatus(1);
        authCode.setDelFlag(0);
        authCode.setCreateTime(LocalDateTime.now());
        authCodeMapper.insertSelective(authCode);
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId == null) {
            throw new IllegalStateException("Current tenant is required");
        }
        TenantAuthCodeDO tenantAuthCode = new TenantAuthCodeDO();
        tenantAuthCode.setTenantId(currentTenantId);
        tenantAuthCode.setAuthCodeId(authCode.getId());
        tenantAuthCode.setStatus(1);
        tenantAuthCodeMapper.insert(tenantAuthCode);
        return Result.success(authCode);
    }

    @Operation(summary = "Update Auth Code")
    @SaCheckPermission("system:auth-code:update")
    @PostMapping("/update")
    public Result<Void> update(@RequestParam Long id, @RequestBody AuthCodeDO authCode) {
        AuthCodeDO existing = authCodeMapper.selectOneById(id);
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (existing == null || !isAuthCodeAvailable(id, currentTenantId)) {
            return Result.fail("权限码不存在");
        }
        if (authCode == null || authCode.getCode() == null || authCode.getCode().isBlank()
                || authCode.getCode().length() > 64) {
            return Result.fail("权限编码不能为空且长度不能超过64");
        }
        if (existsByCode(authCode.getCode(), id)) {
            return Result.fail("权限编码已存在");
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
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@RequestParam Long id) {
        AuthCodeDO existing = authCodeMapper.selectOneById(id);
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (existing == null || !isAuthCodeAvailable(id, currentTenantId)) {
            return Result.fail("权限码不存在");
        }
        boolean assigned = roleScopeAuthCodeMapper.selectCountByQuery(QueryWrapper.create()
                .where(RoleScopeAuthCodeDO::getAuthCodeId).eq(id)
                .and(RoleScopeAuthCodeDO::getDelFlag).eq(0)) > 0;
        if (assigned) {
            return Result.fail("权限码已分配给角色，请先取消授权");
        }
        tenantAuthCodeMapper.deleteByQuery(QueryWrapper.create()
                .where(TenantAuthCodeDO::getTenantId).eq(currentTenantId)
                .and(TenantAuthCodeDO::getAuthCodeId).eq(id));
        long remainingTenants = tenantAuthCodeMapper.selectCountByQuery(QueryWrapper.create()
                .where(TenantAuthCodeDO::getAuthCodeId).eq(id)
                .and(TenantAuthCodeDO::getStatus).eq(1));
        if (remainingTenants == 0) {
            existing.setDelFlag(1);
            existing.setUpdateTime(LocalDateTime.now());
            authCodeMapper.update(existing);
        }
        return Result.success();
    }

    @Operation(summary = "为角色授权权限码")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/grant")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> grant(@RequestParam Long roleId,
                              @RequestParam Long tenantId,
                              @RequestBody List<Long> authCodeIds) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (!isValidScope(roleId, currentTenantId, tenantId)
                || authCodeIds == null || authCodeIds.isEmpty()) {
            return Result.fail("角色、作用域或权限码参数无效");
        }
        List<AuthCodeDO> validAuthCodes = authCodeMapper.selectListByQuery(QueryWrapper.create()
                .where(AuthCodeDO::getId).in(authCodeIds)
                .and(AuthCodeDO::getStatus).eq(1)
                .and(AuthCodeDO::getDelFlag).eq(0)).stream()
                .filter(item -> isAuthCodeAvailable(item.getId(), tenantId))
                .toList();
        if (validAuthCodes.size() != authCodeIds.stream().distinct().count()) {
            return Result.fail("包含不存在或已停用的权限码");
        }
        Set<Long> existingIds = new HashSet<>(roleScopeAuthCodeMapper.selectListByQuery(
                QueryWrapper.create()
                        .where(RoleScopeAuthCodeDO::getRoleId).eq(roleId)
                        .and(RoleScopeAuthCodeDO::getTenantId).eq(tenantId)
                        .and(RoleScopeAuthCodeDO::getStatus).eq(1)
                        .and(RoleScopeAuthCodeDO::getDelFlag).eq(0))
                .stream().map(RoleScopeAuthCodeDO::getAuthCodeId).toList());
        List<RoleScopeAuthCodeDO> records = authCodeIds.stream().distinct()
                .filter(authCodeId -> !existingIds.contains(authCodeId)).map(authCodeId -> {
            RoleScopeAuthCodeDO item = new RoleScopeAuthCodeDO();
            item.setRoleId(roleId);
            item.setAuthCodeId(authCodeId);
            item.setTenantId(tenantId);
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

    @Operation(summary = "替换角色权限码")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/replace")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> replace(@RequestParam Long roleId,
                                @RequestParam Long tenantId,
                                @RequestBody List<String> authCodes) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (!isValidScope(roleId, currentTenantId, tenantId)) {
            return Result.fail("角色不属于当前租户作用域");
        }

        List<String> targetCodes = authCodes == null
                ? List.of()
                : authCodes.stream().filter(java.util.Objects::nonNull).map(String::trim)
                .filter(code -> !code.isEmpty()).distinct().toList();
        List<AuthCodeDO> validAuthCodes = targetCodes.isEmpty() ? List.of()
                : authCodeMapper.selectListByQuery(QueryWrapper.create()
                    .where(AuthCodeDO::getCode).in(targetCodes)
                    .and(AuthCodeDO::getStatus).eq(1)
                    .and(AuthCodeDO::getDelFlag).eq(0)).stream()
                    .filter(item -> isAuthCodeAvailable(item.getId(), tenantId))
                    .toList();
        if (validAuthCodes.size() != targetCodes.size()) {
                return Result.fail("包含不存在或已停用的权限码");
        }

        roleScopeAuthCodeMapper.deleteByQuery(QueryWrapper.create()
                .where(RoleScopeAuthCodeDO::getRoleId).eq(roleId)
                .and(RoleScopeAuthCodeDO::getTenantId).eq(tenantId)
                .and(RoleScopeAuthCodeDO::getTenantId).eq(tenantId));

        if (!targetCodes.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            List<RoleScopeAuthCodeDO> records = validAuthCodes.stream().map(authCode -> {
                RoleScopeAuthCodeDO item = new RoleScopeAuthCodeDO();
                item.setRoleId(roleId);
                item.setAuthCodeId(authCode.getId());
                item.setTenantId(tenantId);
                item.setStatus(1);
                item.setDelFlag(0);
                item.setCreateTime(now);
                item.setUpdateTime(now);
                return item;
            }).toList();
            roleScopeAuthCodeMapper.insertBatch(records);
        }
        return Result.success();
    }

    @Operation(summary = "取消角色权限授权")
    @SaCheckPermission("system:role:assign")
    @PostMapping("/roles/revoke")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> revoke(@RequestParam Long roleId,
                               @RequestParam Long tenantId,
                               @RequestParam Long authCodeId) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (!isValidScope(roleId, currentTenantId, tenantId)) {
            return Result.fail("角色不属于当前租户作用域");
        }
        QueryWrapper query = QueryWrapper.create()
                .where(RoleScopeAuthCodeDO::getRoleId).eq(roleId)
                .and(RoleScopeAuthCodeDO::getAuthCodeId).eq(authCodeId)
                .and(RoleScopeAuthCodeDO::getTenantId).eq(tenantId)
                .and(RoleScopeAuthCodeDO::getTenantId).eq(tenantId);
        roleScopeAuthCodeMapper.deleteByQuery(query);
        return Result.success();
    }

    @Operation(summary = "Page Auth Codes")
    @SaCheckPermission("system:auth-code:list")
    @GetMapping("/page")
    public Result<PageData<AuthCodeOptionVO>> page(AuthCodePageRequest request) {
        QueryWrapper countQuery = buildPageQuery(request);
        long total = authCodeMapper.selectCountByQuery(countQuery);
        QueryWrapper pageQuery = buildPageQuery(request);
        int pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
        pageQuery.limit((long) (pageNum - 1) * pageSize, pageSize);
        pageQuery.orderBy(AuthCodeDO::getId, true);
        List<AuthCodeOptionVO> items = authCodeMapper.selectListByQuery(pageQuery)
                .stream().map(this::toOption).toList();
        return Result.success(new PageData<>(items, total));
    }

    private QueryWrapper buildPageQuery(AuthCodePageRequest request) {
        QueryWrapper query = buildTenantAuthCodeQuery();
        if (request.getCode() != null && !request.getCode().isBlank()) {
            query.and(AuthCodeDO::getCode).like(request.getCode());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            query.and(AuthCodeDO::getName).like(request.getName());
        }
        return query;
    }

    private QueryWrapper buildTenantAuthCodeQuery() {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        return QueryWrapper.create()
                .from(AuthCodeDO.class)
                .innerJoin(TenantAuthCodeDO.class)
                .on(column(AuthCodeDO::getId)
                        .eq(column(TenantAuthCodeDO::getAuthCodeId)))
                .where(TenantAuthCodeDO::getTenantId).eq(currentTenantId)
                .and(TenantAuthCodeDO::getStatus).eq(1)
                .and(AuthCodeDO::getStatus).eq(1)
                .and(AuthCodeDO::getDelFlag).eq(0);
    }

    private boolean isAuthCodeAvailable(Long authCodeId, Long tenantId) {
        return authCodeId != null && tenantId != null
                && tenantAuthCodeMapper.selectCountByQuery(QueryWrapper.create()
                .where(TenantAuthCodeDO::getTenantId).eq(tenantId)
                .and(TenantAuthCodeDO::getAuthCodeId).eq(authCodeId)
                .and(TenantAuthCodeDO::getStatus).eq(1)) > 0;
    }

    private boolean isValidScope(Long roleId, Long currentTenantId, Long tenantId) {
        if (currentTenantId == null || tenantId == null
                || !roleRepository.isRoleOwnedByTenant(roleId, tenantId)) {
            return false;
        }
        var tenant = tenantRepository.selectById(tenantId);
        return tenant != null
                && tenant.getStatus() != null && tenant.getStatus() == 1
                && (tenant.getDelFlag() == null || tenant.getDelFlag() == 0)
                && tenantRepository.selectDescendantIds(currentTenantId).contains(tenantId);
    }

    private boolean existsByCode(String code, Long excludeId) {
        QueryWrapper query = QueryWrapper.create()
                .where(AuthCodeDO::getCode).eq(code.trim())
                .and(AuthCodeDO::getDelFlag).eq(0);
        if (excludeId != null) {
            query.and(AuthCodeDO::getId).ne(excludeId);
        }
        return authCodeMapper.selectCountByQuery(query) > 0;
    }
}
