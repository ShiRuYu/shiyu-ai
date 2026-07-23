package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.UserDO;
import com.shiyu.ai.dal.auth.dataobject.UserWorkspaceRoleDO;
import com.shiyu.ai.dal.auth.mapper.RoleMapper;
import com.shiyu.ai.dal.auth.mapper.UserMapper;
import com.shiyu.ai.dal.auth.mapper.UserWorkspaceRoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class AuthUserLookupRepository {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserWorkspaceRoleMapper userWorkspaceRoleMapper;

    @Resource
    private RoleMapper roleMapper;

    public UserDO selectUserById(Long userId) {
        return userMapper.selectOneById(userId);
    }

    public List<UserWorkspaceRoleDO> selectUserWorkspaceRoles(Long userId) {
        return userWorkspaceRoleMapper.selectByUserId(userId);
    }

    public RoleDO selectRoleById(Long roleId) {
        return roleMapper.selectOneById(roleId);
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
}
