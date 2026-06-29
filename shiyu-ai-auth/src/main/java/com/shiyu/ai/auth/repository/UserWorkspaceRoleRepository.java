package com.shiyu.ai.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.dal.mapper.auth.UserWorkspaceRoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserWorkspaceRoleRepository {

    @Resource
    private UserWorkspaceRoleMapper userWorkspaceRoleMapper;

    public List<UserWorkspaceRoleDO> selectByUserId(Long userId) {
        return userWorkspaceRoleMapper.selectByUserId(userId);
    }

    public void insert(UserWorkspaceRoleDO userWorkspaceRole) {
        userWorkspaceRoleMapper.insert(userWorkspaceRole);
    }

    public void deleteByQuery(QueryWrapper queryWrapper) {
        userWorkspaceRoleMapper.deleteByQuery(queryWrapper);
    }
}
