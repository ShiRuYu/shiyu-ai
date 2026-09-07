package com.shiyu.ai.auth.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.auth.persistence.dataobject.AuthCodeDO;
import com.shiyu.ai.auth.persistence.dataobject.RoleDO;
import com.shiyu.ai.auth.persistence.dataobject.RoleScopeAuthCodeDO;
import com.shiyu.ai.auth.persistence.dataobject.UserDO;
import com.shiyu.ai.auth.persistence.dataobject.UserScopeRoleDO;
import com.shiyu.ai.auth.persistence.mapper.AuthCodeMapper;
import com.shiyu.ai.auth.persistence.mapper.RoleMapper;
import com.shiyu.ai.auth.persistence.mapper.RoleScopeAuthCodeMapper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.mybatisflex.core.query.QueryMethods.column;

/**
 * 认证数据仓储层
 *
 * <p>权限码统一存储在 auth_auth_code 表中，通过角色和租户作用域进行分配。
 * auth_menu 表只负责菜单、路由和展示。</p>
 */
@Component
public class AuthRepositoryImpl implements com.shiyu.ai.auth.port.repository.AuthRepository {

    @Resource
    private AuthCodeMapper authCodeMapper;

    @Resource
    private RoleMapper roleMapper;

    /**
     * 根据用户 ID + 当前租户查询角色编码列表
     *
     * <p>JOIN: auth_user_scope_role → auth_role</p>
     *
     * @param userId      用户ID
     * @param tenantId    当前租户ID
     * @return 角色编码列表
     */
    public List<String> selectRoleCodesByUserId(UserId userId, TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        long userValue = requireUser(userId);
        QueryWrapper qw = QueryWrapper.create()
            .select(column(RoleDO::getCode))
            .from(RoleDO.class)
            .innerJoin(UserScopeRoleDO.class)
                .on(column(RoleDO::getId).eq(column(UserScopeRoleDO::getRoleId)))
            .where(UserScopeRoleDO::getUserId).eq(userValue)
            .and(column(UserScopeRoleDO::getTenantId).eq(tenantValue))
            .and(RoleDO::getStatus).eq(1)
            .and(RoleDO::getDelFlag).eq(0)
            .and(UserScopeRoleDO::getStatus).eq(1)
            .and(UserScopeRoleDO::getDelFlag).eq(0);
        qw.orderBy(RoleDO::getId);
        List<RoleDO> list = roleMapper.selectListByQuery(qw);
        return list.stream().map(RoleDO::getCode).distinct().collect(Collectors.toList());
    }

    /**
     * 根据用户 ID + 当前租户 + 角色编码查询权限码列表
     *
     * <p>JOIN: auth_user_scope_role → auth_role → auth_role_scope_auth_code → auth_auth_code</p>
     * <p>仅返回用户在指定租户下、指定角色的权限码，避免跨角色越权。</p>
     *
     * @param userId      用户ID
     * @param tenantId    当前租户ID
     * @param roleCode    当前角色编码
     * @return 权限码列表
     */
    public List<String> selectCodesByUserIdAndRoleCode(UserId userId, TenantId tenantId, String roleCode) {
        long tenantValue = requireTenant(tenantId);
        long userValue = requireUser(userId);
        requireRoleCode(roleCode);
        QueryWrapper qw = QueryWrapper.create()
            .select(column(AuthCodeDO::getCode))
            .from(AuthCodeDO.class)
            .innerJoin(RoleScopeAuthCodeDO.class)
                .on(column(AuthCodeDO::getId).eq(column(RoleScopeAuthCodeDO::getAuthCodeId)))
            .innerJoin(RoleDO.class)
                .on(column(RoleScopeAuthCodeDO::getRoleId).eq(column(RoleDO::getId)))
            .innerJoin(UserScopeRoleDO.class)
                .on(column(RoleDO::getId).eq(column(UserScopeRoleDO::getRoleId)))
            .where(UserScopeRoleDO::getUserId).eq(userValue)
            .and(RoleDO::getCode).eq(roleCode)
            .and(RoleDO::getTenantId).eq(tenantValue)
            .and(column(RoleScopeAuthCodeDO::getTenantId).eq(column(UserScopeRoleDO::getTenantId)))
            .and(column(UserScopeRoleDO::getTenantId).eq(tenantValue))
            .and(AuthCodeDO::getStatus).eq(1)
            .and(AuthCodeDO::getDelFlag).eq(0)
            .and(RoleScopeAuthCodeDO::getStatus).eq(1)
            .and(RoleScopeAuthCodeDO::getDelFlag).eq(0)
            .and(RoleDO::getStatus).eq(1)
            .and(RoleDO::getDelFlag).eq(0)
            .and(UserScopeRoleDO::getStatus).eq(1)
            .and(UserScopeRoleDO::getDelFlag).eq(0);
        qw.orderBy(AuthCodeDO::getId);
        List<AuthCodeDO> list = authCodeMapper.selectListByQuery(qw);
        return list.stream().map(AuthCodeDO::getCode).distinct().collect(Collectors.toList());
    }

    /** 父租户超级管理员切换到子租户时，按目标租户超级角色计算权限。 */
    public List<String> selectCodesByRoleCodeAndTenant(String roleCode, TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        requireRoleCode(roleCode);
        QueryWrapper qw = QueryWrapper.create()
                .select(column(AuthCodeDO::getCode))
                .from(AuthCodeDO.class)
                .innerJoin(RoleScopeAuthCodeDO.class)
                    .on(column(AuthCodeDO::getId).eq(column(RoleScopeAuthCodeDO::getAuthCodeId)))
                .innerJoin(RoleDO.class)
                    .on(column(RoleScopeAuthCodeDO::getRoleId).eq(column(RoleDO::getId)))
                .where(RoleDO::getCode).eq(roleCode)
                .and(RoleDO::getTenantId).eq(tenantValue)
                .and(RoleScopeAuthCodeDO::getTenantId).eq(tenantValue)
                .and(AuthCodeDO::getStatus).eq(1)
                .and(AuthCodeDO::getDelFlag).eq(0)
                .and(RoleScopeAuthCodeDO::getStatus).eq(1)
                .and(RoleScopeAuthCodeDO::getDelFlag).eq(0)
                .and(RoleDO::getStatus).eq(1)
                .and(RoleDO::getDelFlag).eq(0);
        return authCodeMapper.selectListByQuery(qw).stream()
                .map(AuthCodeDO::getCode)
                .distinct()
                .toList();
    }

    /**
     * 根据用户名查询按钮级权限码列表
     *
     * <p>JOIN: auth_user → auth_user_scope_role → auth_role_scope_auth_code → auth_auth_code</p>
     */
    public List<String> selectCodesByUsername(String username, TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        QueryWrapper qw = QueryWrapper.create()
            .select(column(AuthCodeDO::getCode))
            .from(AuthCodeDO.class)
            .innerJoin(RoleScopeAuthCodeDO.class)
                .on(column(AuthCodeDO::getId).eq(column(RoleScopeAuthCodeDO::getAuthCodeId)))
            .innerJoin(UserScopeRoleDO.class)
                .on(column(RoleScopeAuthCodeDO::getRoleId).eq(column(UserScopeRoleDO::getRoleId)))
            .innerJoin(UserDO.class)
                .on(column(UserScopeRoleDO::getUserId).eq(column(UserDO::getId)))
            .where(UserDO::getUsername).eq(username)
            .and(column(RoleScopeAuthCodeDO::getTenantId).eq(column(UserScopeRoleDO::getTenantId)))
            .and(column(UserScopeRoleDO::getTenantId).eq(tenantValue))
            .and(AuthCodeDO::getStatus).eq(1)
            .and(AuthCodeDO::getDelFlag).eq(0)
            .and(RoleScopeAuthCodeDO::getStatus).eq(1)
            .and(RoleScopeAuthCodeDO::getDelFlag).eq(0)
            .and(UserScopeRoleDO::getStatus).eq(1)
            .and(UserScopeRoleDO::getDelFlag).eq(0)
            .and(UserDO::getStatus).eq(1)
            .and(UserDO::getDelFlag).eq(0);
        qw.orderBy(AuthCodeDO::getId);
        List<AuthCodeDO> list = authCodeMapper.selectListByQuery(qw);
        return list.stream().map(AuthCodeDO::getCode).distinct().collect(Collectors.toList());
    }

    /**
     * 根据用户 ID 查询按钮级权限码列表
     *
     * <p>JOIN: auth_user_scope_role → auth_role_scope_auth_code → auth_auth_code</p>
     *
     * @param userId      用户ID
     * @param currentTenantId 当前切换租户 ID（必填）
     */
    public List<String> selectCodesByUserId(UserId userId, TenantId currentTenantId) {
        long tenantValue = requireTenant(currentTenantId);
        long userValue = requireUser(userId);
        QueryWrapper qw = QueryWrapper.create()
            .select(column(AuthCodeDO::getCode))
            .from(AuthCodeDO.class)
            .innerJoin(RoleScopeAuthCodeDO.class)
                .on(column(AuthCodeDO::getId).eq(column(RoleScopeAuthCodeDO::getAuthCodeId)))
            .innerJoin(UserScopeRoleDO.class)
                .on(column(RoleScopeAuthCodeDO::getRoleId).eq(column(UserScopeRoleDO::getRoleId)))
            .innerJoin(RoleDO.class)
                .on(column(RoleScopeAuthCodeDO::getRoleId).eq(column(RoleDO::getId)))
            .where(UserScopeRoleDO::getUserId).eq(userValue)
            .and(column(RoleScopeAuthCodeDO::getTenantId).eq(column(UserScopeRoleDO::getTenantId)))
            .and(RoleDO::getTenantId).eq(tenantValue)
            .and(RoleDO::getStatus).eq(1)
            .and(RoleDO::getDelFlag).eq(0)
            .and(AuthCodeDO::getStatus).eq(1)
            .and(AuthCodeDO::getDelFlag).eq(0)
            .and(RoleScopeAuthCodeDO::getStatus).eq(1)
            .and(RoleScopeAuthCodeDO::getDelFlag).eq(0)
            .and(UserScopeRoleDO::getStatus).eq(1)
            .and(UserScopeRoleDO::getDelFlag).eq(0);
        // 按当前租户过滤权限码，避免跨租户越权
        qw.and(RoleScopeAuthCodeDO::getTenantId).eq(tenantValue);
        qw.orderBy(AuthCodeDO::getId);
        List<AuthCodeDO> list = authCodeMapper.selectListByQuery(qw);
        return list.stream().map(AuthCodeDO::getCode).distinct().collect(Collectors.toList());
    }

    /**
     * 根据角色ID查询权限码列表
     */
    public List<String> selectCodesByRoleId(Long roleId, TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        QueryWrapper qw = QueryWrapper.create()
            .select(column(AuthCodeDO::getCode))
            .from(AuthCodeDO.class)
            .innerJoin(RoleScopeAuthCodeDO.class)
                .on(column(AuthCodeDO::getId).eq(column(RoleScopeAuthCodeDO::getAuthCodeId)))
            .innerJoin(RoleDO.class)
                .on(column(RoleScopeAuthCodeDO::getRoleId).eq(column(RoleDO::getId)))
            .where(RoleScopeAuthCodeDO::getRoleId).eq(roleId)
            .and(RoleScopeAuthCodeDO::getTenantId).eq(tenantValue)
            .and(RoleDO::getTenantId).eq(tenantValue)
            .and(AuthCodeDO::getStatus).eq(1)
            .and(AuthCodeDO::getDelFlag).eq(0)
            .and(RoleScopeAuthCodeDO::getStatus).eq(1)
            .and(RoleScopeAuthCodeDO::getDelFlag).eq(0)
            .and(RoleDO::getStatus).eq(1)
            .and(RoleDO::getDelFlag).eq(0);
        qw.orderBy(AuthCodeDO::getId);
        List<AuthCodeDO> list = authCodeMapper.selectListByQuery(qw);
        return list.stream().map(AuthCodeDO::getCode).collect(Collectors.toList());
    }

    private static long requireTenant(TenantId tenantId) {
        TenantId value = java.util.Objects.requireNonNull(tenantId, "tenantId must not be null");
        if (value.value() <= 0) {
            throw new IllegalArgumentException("tenantId must be positive");
        }
        return value.value();
    }

    private static long requireUser(UserId userId) {
        return java.util.Objects.requireNonNull(userId, "userId must not be null").value();
    }

    private static String requireRoleCode(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalArgumentException("roleCode must not be blank");
        }
        return roleCode;
    }
}

