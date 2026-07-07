package com.shiyu.ai.dal.repository.auth;

import com.shiyu.ai.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.dal.dataobject.auth.UserDO;
import com.shiyu.ai.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.dal.mapper.auth.RoleMapper;
import com.shiyu.ai.dal.mapper.auth.UserMapper;
import com.shiyu.ai.dal.mapper.auth.UserWorkspaceRoleMapper;
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
