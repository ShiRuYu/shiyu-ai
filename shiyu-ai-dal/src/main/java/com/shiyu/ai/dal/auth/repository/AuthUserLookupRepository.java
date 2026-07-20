package com.shiyu.ai.dal.auth.repository;

import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.UserDO;
import com.shiyu.ai.dal.auth.dataobject.UserWorkspaceRoleDO;
import com.shiyu.ai.dal.auth.mapper.RoleMapper;
import com.shiyu.ai.dal.auth.mapper.UserMapper;
import com.shiyu.ai.dal.auth.mapper.UserWorkspaceRoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

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
}
