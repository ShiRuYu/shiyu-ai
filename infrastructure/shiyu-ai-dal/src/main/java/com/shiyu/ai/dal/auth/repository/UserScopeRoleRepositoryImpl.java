package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.mapper.UserScopeRoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserScopeRoleRepositoryImpl implements com.shiyu.ai.auth.port.repository.UserScopeRoleRepository {

    @Resource
    private UserScopeRoleMapper userWorkspaceRoleMapper;

    public List<UserScopeRoleBO> selectByUserId(Long userId) {
        return MapstructUtils.convert(userWorkspaceRoleMapper.selectByUserId(userId), UserScopeRoleBO.class);
    }

    /**
     * 插入用户-角色关联记录。
     * 使用 insertSelective 忽略 null 字段，让数据库 DEFAULT 生效。
     */
    public void insert(UserScopeRoleBO userWorkspaceRole) {
        userWorkspaceRoleMapper.insertSelective(MapstructUtils.convert(userWorkspaceRole, UserScopeRoleDO.class));
    }

    public void deleteByUserIdAndTenantId(Long userId, Long tenantId) {
        userWorkspaceRoleMapper.deleteByQuery(QueryWrapper.create()
                .eq(UserScopeRoleDO::getUserId, userId)
                .eq(UserScopeRoleDO::getTenantId, tenantId));
    }

    public void deleteByUserIdRoleIdAndTenantId(Long userId, Long roleId, Long tenantId) {
        userWorkspaceRoleMapper.deleteByQuery(QueryWrapper.create()
                .eq(UserScopeRoleDO::getUserId, userId)
                .eq(UserScopeRoleDO::getRoleId, roleId)
                .eq(UserScopeRoleDO::getTenantId, tenantId));
    }
}
