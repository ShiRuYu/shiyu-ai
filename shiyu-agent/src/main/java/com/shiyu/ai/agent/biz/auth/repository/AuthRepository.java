package com.shiyu.ai.agent.biz.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.auth.AuthCodeDO;
import com.shiyu.ai.agent.dal.dataobject.auth.UserDO;
import com.shiyu.ai.agent.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.agent.dal.mapper.auth.AuthCodeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.mybatisflex.core.query.QueryMethods.column;

/**
 * 认证数据仓储层
 */
@Component
public class AuthRepository {

    @Resource
    private AuthCodeMapper authCodeMapper;

    /**
     * 根据用户名查询权限码列表
     */
    public List<String> selectCodesByUsername(String username) {
        QueryWrapper qw = QueryWrapper.create()
            .from(AuthCodeDO.class)
            .innerJoin(UserWorkspaceRoleDO.class)
                .on(column(AuthCodeDO::getRoleId).eq(column(UserWorkspaceRoleDO::getRoleId)))
            .innerJoin(UserDO.class)
                .on(column(UserWorkspaceRoleDO::getUserId).eq(column(UserDO::getId)))
            .where(UserDO::getUsername).eq(username)
            .and(AuthCodeDO::getStatus).eq("1")
            .and(AuthCodeDO::getDelFlag).eq(0)
            .and(UserDO::getStatus).eq("1")
            .and(UserDO::getDelFlag).eq(0);
        qw.orderBy(AuthCodeDO::getId);
        List<AuthCodeDO> list = authCodeMapper.selectListByQuery(qw);
        return list.stream().map(AuthCodeDO::getCode).distinct().collect(Collectors.toList());
    }

    /**
     * 根据用户 ID 查询权限码列表
     */
    public List<String> selectCodesByUserId(Long userId) {
        QueryWrapper qw = QueryWrapper.create()
            .from(AuthCodeDO.class)
            .innerJoin(UserWorkspaceRoleDO.class)
                .on(column(AuthCodeDO::getRoleId).eq(column(UserWorkspaceRoleDO::getRoleId)))
            .where(UserWorkspaceRoleDO::getUserId).eq(userId)
            .and(AuthCodeDO::getStatus).eq("1")
            .and(AuthCodeDO::getDelFlag).eq(0);
        qw.orderBy(AuthCodeDO::getId);
        List<AuthCodeDO> list = authCodeMapper.selectListByQuery(qw);
        return list.stream().map(AuthCodeDO::getCode).distinct().collect(Collectors.toList());
    }

    /**
     * 根据角色ID查询权限码列表
     */
    public List<String> selectCodesByRoleId(Long roleId) {
        QueryWrapper qw = QueryWrapper.create()
            .from(AuthCodeDO.class)
            .where(AuthCodeDO::getRoleId).eq(roleId)
            .and(AuthCodeDO::getStatus).eq("1")
            .and(AuthCodeDO::getDelFlag).eq(0);
        qw.orderBy(AuthCodeDO::getId);
        List<AuthCodeDO> list = authCodeMapper.selectListByQuery(qw);
        return list.stream().map(AuthCodeDO::getCode).collect(Collectors.toList());
    }
}
