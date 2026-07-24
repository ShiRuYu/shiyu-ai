package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.mapper.UserScopeRoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserScopeRoleRepository {

    @Resource
    private UserScopeRoleMapper userWorkspaceRoleMapper;

    public List<UserScopeRoleDO> selectByUserId(Long userId) {
        return userWorkspaceRoleMapper.selectByUserId(userId);
    }

    public void insert(UserScopeRoleDO userWorkspaceRole) {
        userWorkspaceRoleMapper.insert(userWorkspaceRole);
    }

    public void deleteByQuery(QueryWrapper queryWrapper) {
        userWorkspaceRoleMapper.deleteByQuery(queryWrapper);
    }
}
