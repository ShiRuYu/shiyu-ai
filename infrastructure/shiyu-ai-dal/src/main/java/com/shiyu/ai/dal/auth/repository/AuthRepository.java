package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.auth.dataobject.AuthCodeDO;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.RoleScopeAuthCodeDO;
import com.shiyu.ai.dal.auth.dataobject.UserDO;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.mapper.AuthCodeMapper;
import com.shiyu.ai.dal.auth.mapper.RoleMapper;
import com.shiyu.ai.dal.auth.mapper.RoleScopeAuthCodeMapper;
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
public class AuthRepository implements com.shiyu.ai.auth.port.repository.AuthRepository {

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
    public List<String> selectRoleCodesByUserId(Long userId, Long tenantId) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(RoleDO::getCode))
            .from(RoleDO.class)
            .innerJoin(UserScopeRoleDO.class)
                .on(column(RoleDO::getId).eq(column(UserScopeRoleDO::getRoleId)))
            .where(UserScopeRoleDO::getUserId).eq(userId)
            .and(column(UserScopeRoleDO::getTenantId).eq(tenantId))
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
    public List<String> selectCodesByUserIdAndRoleCode(Long userId, Long tenantId, String roleCode) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(AuthCodeDO::getCode))
            .from(AuthCodeDO.class)
            .innerJoin(RoleScopeAuthCodeDO.class)
                .on(column(AuthCodeDO::getId).eq(column(RoleScopeAuthCodeDO::getAuthCodeId)))
            .innerJoin(RoleDO.class)
                .on(column(RoleScopeAuthCodeDO::getRoleId).eq(column(RoleDO::getId)))
            .innerJoin(UserScopeRoleDO.class)
                .on(column(RoleDO::getId).eq(column(UserScopeRoleDO::getRoleId)))
            .where(UserScopeRoleDO::getUserId).eq(userId)
            .and(RoleDO::getCode).eq(roleCode)
            .and(column(RoleScopeAuthCodeDO::getTenantId).eq(column(UserScopeRoleDO::getTenantId)))
            .and(column(UserScopeRoleDO::getTenantId).eq(tenantId))
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
    public List<String> selectCodesByRoleCodeAndTenant(String roleCode, Long tenantId) {
        QueryWrapper qw = QueryWrapper.create()
                .select(column(AuthCodeDO::getCode))
                .from(AuthCodeDO.class)
                .innerJoin(RoleScopeAuthCodeDO.class)
                    .on(column(AuthCodeDO::getId).eq(column(RoleScopeAuthCodeDO::getAuthCodeId)))
                .innerJoin(RoleDO.class)
                    .on(column(RoleScopeAuthCodeDO::getRoleId).eq(column(RoleDO::getId)))
                .where(RoleDO::getCode).eq(roleCode)
                .and(RoleDO::getTenantId).eq(tenantId)
                .and(RoleScopeAuthCodeDO::getTenantId).eq(tenantId)
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
    public List<String> selectCodesByUsername(String username) {
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
     * @param currentTenantId 当前切换租户ID（null 表示查询所有空间）
     */
    public List<String> selectCodesByUserId(Long userId, Long currentTenantId) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(AuthCodeDO::getCode))
            .from(AuthCodeDO.class)
            .innerJoin(RoleScopeAuthCodeDO.class)
                .on(column(AuthCodeDO::getId).eq(column(RoleScopeAuthCodeDO::getAuthCodeId)))
            .innerJoin(UserScopeRoleDO.class)
                .on(column(RoleScopeAuthCodeDO::getRoleId).eq(column(UserScopeRoleDO::getRoleId)))
            .where(UserScopeRoleDO::getUserId).eq(userId)
            .and(column(RoleScopeAuthCodeDO::getTenantId).eq(column(UserScopeRoleDO::getTenantId)))
            .and(AuthCodeDO::getStatus).eq(1)
            .and(AuthCodeDO::getDelFlag).eq(0)
            .and(RoleScopeAuthCodeDO::getStatus).eq(1)
            .and(RoleScopeAuthCodeDO::getDelFlag).eq(0)
            .and(UserScopeRoleDO::getStatus).eq(1)
            .and(UserScopeRoleDO::getDelFlag).eq(0);
        // ✅ 按当前工作空间过滤权限码，避免跨空间越权
        if (currentTenantId != null) {
            qw.and(RoleScopeAuthCodeDO::getTenantId).eq(currentTenantId);
        }
        qw.orderBy(AuthCodeDO::getId);
        List<AuthCodeDO> list = authCodeMapper.selectListByQuery(qw);
        return list.stream().map(AuthCodeDO::getCode).distinct().collect(Collectors.toList());
    }

    /**
     * 根据角色ID查询权限码列表
     */
    public List<String> selectCodesByRoleId(Long roleId) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(AuthCodeDO::getCode))
            .from(AuthCodeDO.class)
            .innerJoin(RoleScopeAuthCodeDO.class)
                .on(column(AuthCodeDO::getId).eq(column(RoleScopeAuthCodeDO::getAuthCodeId)))
            .where(RoleScopeAuthCodeDO::getRoleId).eq(roleId)
            .and(AuthCodeDO::getStatus).eq(1)
            .and(AuthCodeDO::getDelFlag).eq(0)
            .and(RoleScopeAuthCodeDO::getStatus).eq(1)
            .and(RoleScopeAuthCodeDO::getDelFlag).eq(0);
        qw.orderBy(AuthCodeDO::getId);
        List<AuthCodeDO> list = authCodeMapper.selectListByQuery(qw);
        return list.stream().map(AuthCodeDO::getCode).collect(Collectors.toList());
    }
}
