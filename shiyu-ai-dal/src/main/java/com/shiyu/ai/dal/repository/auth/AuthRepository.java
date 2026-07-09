package com.shiyu.ai.dal.repository.auth;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.auth.MenuDO;
import com.shiyu.ai.dal.dataobject.auth.RoleWorkspaceMenuDO;
import com.shiyu.ai.dal.dataobject.auth.UserDO;
import com.shiyu.ai.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.dal.mapper.auth.MenuMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.mybatisflex.core.query.QueryMethods.column;

/**
 * 认证数据仓储层
 *
 * <p>权限码现已统一存储在 menu 表中（type='BUTTON'），
 * 通过 role_workspace_menu 关联角色进行分配。</p>
 */
@Component
public class AuthRepository {

    @Resource
    private MenuMapper menuMapper;

    /**
     * 根据用户名查询按钮级权限码列表
     *
     * <p>JOIN: user → user_workspace_role → role_workspace_menu → menu</p>
     */
    public List<String> selectCodesByUsername(String username) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(MenuDO::getCode))
            .from(MenuDO.class)
            .innerJoin(RoleWorkspaceMenuDO.class)
                .on(column(MenuDO::getId).eq(column(RoleWorkspaceMenuDO::getMenuId)))
            .innerJoin(UserWorkspaceRoleDO.class)
                .on(column(RoleWorkspaceMenuDO::getRoleId).eq(column(UserWorkspaceRoleDO::getRoleId)))
            .innerJoin(UserDO.class)
                .on(column(UserWorkspaceRoleDO::getUserId).eq(column(UserDO::getId)))
            .where(UserDO::getUsername).eq(username)
            .and(MenuDO::getType).eq("BUTTON")
            .and(MenuDO::getStatus).eq("1")
            .and(MenuDO::getDelFlag).eq(0)
            .and(UserDO::getStatus).eq("1")
            .and(UserDO::getDelFlag).eq(0);
        qw.orderBy(MenuDO::getId);
        List<MenuDO> list = menuMapper.selectListByQuery(qw);
        return list.stream().map(MenuDO::getCode).distinct().collect(Collectors.toList());
    }

    /**
     * 根据用户 ID 查询按钮级权限码列表
     *
     * <p>JOIN: user_workspace_role → role_workspace_menu → menu</p>
     */
    public List<String> selectCodesByUserId(Long userId) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(MenuDO::getCode))
            .from(MenuDO.class)
            .innerJoin(RoleWorkspaceMenuDO.class)
                .on(column(MenuDO::getId).eq(column(RoleWorkspaceMenuDO::getMenuId)))
            .innerJoin(UserWorkspaceRoleDO.class)
                .on(column(RoleWorkspaceMenuDO::getRoleId).eq(column(UserWorkspaceRoleDO::getRoleId)))
            .where(UserWorkspaceRoleDO::getUserId).eq(userId)
            .and(MenuDO::getType).eq("BUTTON")
            .and(MenuDO::getStatus).eq("1")
            .and(MenuDO::getDelFlag).eq(0);
        qw.orderBy(MenuDO::getId);
        List<MenuDO> list = menuMapper.selectListByQuery(qw);
        return list.stream().map(MenuDO::getCode).distinct().collect(Collectors.toList());
    }

    /**
     * 根据角色ID查询按钮级权限码列表
     */
    public List<String> selectCodesByRoleId(Long roleId) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(MenuDO::getCode))
            .from(MenuDO.class)
            .innerJoin(RoleWorkspaceMenuDO.class)
                .on(column(MenuDO::getId).eq(column(RoleWorkspaceMenuDO::getMenuId)))
            .where(RoleWorkspaceMenuDO::getRoleId).eq(roleId)
            .and(MenuDO::getType).eq("BUTTON")
            .and(MenuDO::getStatus).eq("1")
            .and(MenuDO::getDelFlag).eq(0);
        qw.orderBy(MenuDO::getId);
        List<MenuDO> list = menuMapper.selectListByQuery(qw);
        return list.stream().map(MenuDO::getCode).collect(Collectors.toList());
    }
}
