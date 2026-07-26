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

    /**
     * 插入用户-角色关联记录。
     * 使用 insertSelective 忽略 null 字段，让数据库 DEFAULT 生效。
     */
    public void insert(UserScopeRoleDO userWorkspaceRole) {
        userWorkspaceRoleMapper.insertSelective(userWorkspaceRole);
    }

    public void deleteByQuery(QueryWrapper queryWrapper) {
        userWorkspaceRoleMapper.deleteByQuery(queryWrapper);
    }
}