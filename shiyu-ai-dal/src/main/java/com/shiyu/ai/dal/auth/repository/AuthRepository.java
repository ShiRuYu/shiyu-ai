package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.auth.dataobject.MenuDO;
import com.shiyu.ai.dal.auth.dataobject.RoleScopeMenuDO;
import com.shiyu.ai.dal.auth.dataobject.UserDO;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.mapper.MenuMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.mybatisflex.core.query.QueryMethods.column;

/**
 * 认证数据仓储层
 *
 * <p>权限码现已统一存储在 menu 表中（type='BUTTON'），
 * 通过 role_scope_menu 关联角色进行分配。</p>
 */
@Component
public class AuthRepository {

    @Resource
    private MenuMapper menuMapper;

    /**
     * 根据用户名查询按钮级权限码列表
     *
     * <p>JOIN: user → user_scope_role → role_scope_menu → menu</p>
     */
    public List<String> selectCodesByUsername(String username) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(MenuDO::getCode))
            .from(MenuDO.class)
            .innerJoin(RoleScopeMenuDO.class)
                .on(column(MenuDO::getId).eq(column(RoleScopeMenuDO::getMenuId)))
            .innerJoin(UserScopeRoleDO.class)
                .on(column(RoleScopeMenuDO::getRoleId).eq(column(UserScopeRoleDO::getRoleId)))
            .innerJoin(UserDO.class)
                .on(column(UserScopeRoleDO::getUserId).eq(column(UserDO::getId)))
            .where(UserDO::getUsername).eq(username)
            .and(MenuDO::getType).eq("BUTTON")
            .and(MenuDO::getStatus).eq(1)
            .and(MenuDO::getDelFlag).eq(0)
            .and(UserDO::getStatus).eq(1)
            .and(UserDO::getDelFlag).eq(0);
        qw.orderBy(MenuDO::getId);
        List<MenuDO> list = menuMapper.selectListByQuery(qw);
        return list.stream().map(MenuDO::getCode).distinct().collect(Collectors.toList());
    }

    /**
     * 根据用户 ID 查询按钮级权限码列表
     *
     * <p>JOIN: user_scope_role → role_scope_menu → menu</p>
     *
     * @param userId      用户ID
     * @param scopedTenantId 当前作用域租户ID（null 表示查询所有空间）
     */
    public List<String> selectCodesByUserId(Long userId, Long scopeTenantId) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(MenuDO::getCode))
            .from(MenuDO.class)
            .innerJoin(RoleScopeMenuDO.class)
                .on(column(MenuDO::getId).eq(column(RoleScopeMenuDO::getMenuId)))
            .innerJoin(UserScopeRoleDO.class)
                .on(column(RoleScopeMenuDO::getRoleId).eq(column(UserScopeRoleDO::getRoleId)))
            .where(UserScopeRoleDO::getUserId).eq(userId)
            .and(MenuDO::getType).eq("BUTTON")
            .and(MenuDO::getStatus).eq(1)
            .and(MenuDO::getDelFlag).eq(0);
        // ✅ 按当前工作空间过滤权限码，避免跨空间越权
        if (scopeTenantId != null) {
            qw.and(RoleScopeMenuDO::getScopedTenantId).eq(scopeTenantId);
        }
        qw.orderBy(MenuDO::getId);
        List<MenuDO> list = menuMapper.selectListByQuery(qw);
        return list.stream().map(MenuDO::getCode).distinct().collect(Collectors.toList());
    }

    /**
     * 根据用户 ID 查询按钮级权限码列表（不按空间过滤，保留兼容）
     */
    public List<String> selectCodesByUserId(Long userId) {
        return selectCodesByUserId(userId, null);
    }

    /**
     * 根据角色ID查询按钮级权限码列表
     */
    public List<String> selectCodesByRoleId(Long roleId) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(MenuDO::getCode))
            .from(MenuDO.class)
            .innerJoin(RoleScopeMenuDO.class)
                .on(column(MenuDO::getId).eq(column(RoleScopeMenuDO::getMenuId)))
            .where(RoleScopeMenuDO::getRoleId).eq(roleId)
            .and(MenuDO::getType).eq("BUTTON")
            .and(MenuDO::getStatus).eq(1)
            .and(MenuDO::getDelFlag).eq(0);
        qw.orderBy(MenuDO::getId);
        List<MenuDO> list = menuMapper.selectListByQuery(qw);
        return list.stream().map(MenuDO::getCode).collect(Collectors.toList());
    }
}
