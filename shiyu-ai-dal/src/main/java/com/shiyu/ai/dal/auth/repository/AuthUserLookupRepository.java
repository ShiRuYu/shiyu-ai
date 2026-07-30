package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.TenantDO;
import com.shiyu.ai.dal.auth.dataobject.UserDO;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.mapper.RoleMapper;
import com.shiyu.ai.dal.auth.mapper.TenantMapper;
import com.shiyu.ai.dal.auth.mapper.UserMapper;
import com.shiyu.ai.dal.auth.mapper.UserScopeRoleMapper;
import com.mybatisflex.core.tenant.TenantManager;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class AuthUserLookupRepository {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserScopeRoleMapper userWorkspaceRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private TenantMapper tenantMapper;

    public UserDO selectUserById(Long userId) {
        return userMapper.selectOneById(userId);
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

    public List<UserScopeRoleDO> selectUserWorkspaceRoles(Long userId) {
        // 登录上下文可能已经处于子租户，校验父租户超管身份时必须读取用户全部租户角色关系。
        return TenantManager.withoutTenantCondition(
                () -> userWorkspaceRoleMapper.selectByUserId(userId));
    }

    public RoleDO selectRoleById(Long roleId) {
        return TenantManager.withoutTenantCondition(
                () -> roleMapper.selectOneById(roleId));
    }

    public RoleDO selectTenantSuperRole(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        return TenantManager.withoutTenantCondition(() -> roleMapper.selectOneByQuery(QueryWrapper.create()
                .where(RoleDO::getTenantId).eq(tenantId)
                .and(RoleDO::getCode).in("tenant_super", "super")
                .and(RoleDO::getStatus).eq(1)
                .and(RoleDO::getDelFlag).eq(0)
                .orderBy(RoleDO::getId, true)));
    }

    /**
     * 批量查询角色列表（按 ID 集合）
     */
    public List<RoleDO> selectRolesByIds(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        QueryWrapper qw = QueryWrapper.create().where(RoleDO::getId).in(roleIds);
        return roleMapper.selectListByQuery(qw);
    }

    /**
     * 根据ID查询租户（含 parentId 判断是否根租户）
     */
    public TenantDO selectTenantById(Long tenantId) {
        return tenantMapper.selectOneById(tenantId);
    }
}
