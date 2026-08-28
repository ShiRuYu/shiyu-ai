package com.shiyu.ai.auth.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.persistence.dataobject.RoleDO;
import com.shiyu.ai.auth.persistence.dataobject.TenantDO;
import com.shiyu.ai.auth.persistence.dataobject.UserDO;
import com.shiyu.ai.auth.persistence.dataobject.UserScopeRoleDO;
import com.shiyu.ai.auth.persistence.mapper.RoleMapper;
import com.shiyu.ai.auth.persistence.mapper.TenantMapper;
import com.shiyu.ai.auth.persistence.mapper.UserMapper;
import com.shiyu.ai.auth.persistence.mapper.UserScopeRoleMapper;
import com.mybatisflex.core.tenant.TenantManager;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Set;

@Component
public class AuthUserLookupRepositoryImpl implements com.shiyu.ai.auth.port.repository.AuthUserLookupRepository {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserScopeRoleMapper userScopeRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private TenantMapper tenantMapper;

    public UserBO selectUserById(Long userId) {
        return MapstructUtils.convert(userMapper.selectOneById(userId), UserBO.class);
    }

    public boolean updateUserExtInfo(Long userId, String extInfo) {
        if (userId == null) {
            return false;
        }
        UserDO user = new UserDO();
        user.setId(userId);
        user.setExtInfo(extInfo);
        return userMapper.update(user) > 0;
    }

    public List<UserScopeRoleBO> selectUserScopeRoles(Long userId) {
        // 登录上下文可能已经处于子租户，校验父租户超管身份时必须读取用户全部租户角色关系。
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(
                () -> userScopeRoleMapper.selectByUserId(userId)), UserScopeRoleBO.class);
    }

    public RoleBO selectRoleById(Long roleId) {
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(
                () -> roleMapper.selectOneById(roleId)), RoleBO.class);
    }

    public RoleBO selectTenantSuperRole(TenantId tenantId) {
        long tenantValue = requireTenant(tenantId);
        return MapstructUtils.convert(TenantManager.withoutTenantCondition(() -> roleMapper.selectOneByQuery(QueryWrapper.create()
                .where(RoleDO::getTenantId).eq(tenantValue)
                .and(RoleDO::getCode).in("tenant_super", "super")
                .and(RoleDO::getStatus).eq(1)
                .and(RoleDO::getDelFlag).eq(0)
                .orderBy(RoleDO::getId, true))), RoleBO.class);
    }

    /**
     * 批量查询角色列表（按 ID 集合）
     */
    public List<RoleBO> selectRolesByIds(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        QueryWrapper qw = QueryWrapper.create().where(RoleDO::getId).in(roleIds);
        return MapstructUtils.convert(roleMapper.selectListByQuery(qw), RoleBO.class);
    }

    /**
     * 根据ID查询租户（含 parentId 判断是否根租户）
     */
    public TenantBO selectTenantById(TenantId tenantId) {
        return MapstructUtils.convert(tenantMapper.selectOneById(requireTenant(tenantId)), TenantBO.class);
    }

    private static long requireTenant(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required for auth lookup");
        }
        return tenantId.value();
    }
}

