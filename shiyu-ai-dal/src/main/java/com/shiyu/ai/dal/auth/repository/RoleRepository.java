package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.auth.dataobject.MenuDO;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.RoleScopeMenuDO;
import com.shiyu.ai.dal.auth.dataobject.RoleScopeAuthCodeDO;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.mapper.MenuMapper;
import com.shiyu.ai.dal.auth.mapper.RoleMapper;
import com.shiyu.ai.dal.auth.mapper.RoleScopeMenuMapper;
import com.shiyu.ai.dal.auth.mapper.RoleScopeAuthCodeMapper;
import com.shiyu.ai.dal.auth.mapper.UserScopeRoleMapper;
import com.shiyu.ai.dal.auth.bo.MenuBO;
import com.shiyu.ai.dal.auth.bo.RoleBO;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mybatisflex.core.query.QueryMethods.column;

/**
 * 角色数据仓储层
 */
@Component
public class RoleRepository {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleScopeMenuMapper roleWorkspaceMenuMapper;

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private UserScopeRoleMapper userScopeRoleMapper;

    @Resource
    private RoleScopeAuthCodeMapper roleScopeAuthCodeMapper;

    /**
     * 分页查询角色列表
     */
    public Pair<Long, List<RoleBO>> selectPage(Number pageNo, Number pageSize, String name) {
        QueryWrapper countWrapper = new QueryWrapper();
        if (name != null && !name.isEmpty()) {
            countWrapper.like(RoleDO::getName, name);
        }
        long count = roleMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(RoleDO::getName, name);
        }
        long page = pageNo == null ? 1 : pageNo.longValue();
        long size = pageSize == null ? 10 : pageSize.longValue();
        queryWrapper.limit((page - 1) * size, size);
        
        List<RoleDO> roleDOs = roleMapper.selectListByQuery(queryWrapper);
        
        return Pair.of(count, MapstructUtils.convert(roleDOs, RoleBO.class));
    }

    /**
     * 查询所有角色列表
     */
    public List<RoleBO> selectAll(String status) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(RoleDO::getStatus, status);
        }
        
        List<RoleDO> roleDOs = roleMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(roleDOs, RoleBO.class);
    }

    /**
     * 根据ID查询角色
     */
    public RoleBO selectById(Long id) {
        RoleDO roleDO = roleMapper.selectOneById(id);
        return MapstructUtils.convert(roleDO, RoleBO.class);
    }

    /**
     * 创建角色
     */
    public RoleBO insert(RoleBO roleBO) {
        RoleDO roleDO = MapstructUtils.convert(roleBO, RoleDO.class);
        
        // 如果 code 为空，使用 name 作为 code
        if (roleDO.getCode() == null || roleDO.getCode().isEmpty()) {
            roleDO.setCode(roleDO.getName());
        }
        
        // 使用 insertSelective 忽略 null 值，让数据库 DEFAULT 生效
        roleMapper.insertSelective(roleDO);
        roleBO.setId(roleDO.getId());
        return roleBO;
    }

    /**
     * 更新角色
     */
    public boolean update(RoleBO roleBO) {
        RoleDO roleDO = MapstructUtils.convert(roleBO, RoleDO.class);
        return roleMapper.update(roleDO) > 0;
    }

    /**
     * 删除角色
     */
    public boolean deleteById(Long id) {
        return roleMapper.deleteById(id) > 0;
    }

    /**
     * 删除角色以及该角色在所有租户作用域下的授权关系。
     */
    public boolean deleteRoleAndRelations(Long roleId) {
        userScopeRoleMapper.deleteByQuery(QueryWrapper.create()
                .where(UserScopeRoleDO::getRoleId).eq(roleId));
        roleWorkspaceMenuMapper.deleteByQuery(QueryWrapper.create()
                .where(RoleScopeMenuDO::getRoleId).eq(roleId));
        roleScopeAuthCodeMapper.deleteByQuery(QueryWrapper.create()
                .where(RoleScopeAuthCodeDO::getRoleId).eq(roleId));
        return roleMapper.deleteById(roleId) > 0;
    }

    /**
     * 校验角色是否属于当前租户作用域且处于有效状态。
     */
    public boolean isRoleInScope(Long roleId, Long currentTenantId) {
        if (roleId == null || currentTenantId == null) {
            return false;
        }
        QueryWrapper qw = QueryWrapper.create()
                .where(RoleDO::getId).eq(roleId)
                .and(RoleDO::getStatus).eq(1)
                .and(RoleDO::getDelFlag).eq(0)
                .and(RoleDO::getTenantId).eq(currentTenantId);
        return roleMapper.selectCountByQuery(qw) > 0;
    }

    /**
     * 校验菜单均存在、有效且属于允许的租户范围。
     */
    public boolean areMenusInTenantScope(List<Long> menuIds, List<Long> allowedTenantIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return true;
        }
        if (allowedTenantIds == null || allowedTenantIds.isEmpty()) {
            return false;
        }
        List<Long> distinctMenuIds = menuIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (distinctMenuIds.size() != menuIds.size()) {
            return false;
        }
        long count = menuMapper.selectCountByQuery(QueryWrapper.create()
                .where(MenuDO::getId).in(distinctMenuIds)
                .and(MenuDO::getTenantId).in(allowedTenantIds)
                .and(MenuDO::getStatus).eq(1)
                .and(MenuDO::getDelFlag).eq(0));
        return count == distinctMenuIds.size();
    }

    /**
     * 根据角色ID查询菜单列表
     */
    public List<MenuBO> selectMenusByRoleId(Long roleId) {
        QueryWrapper qw = QueryWrapper.create()
            .from(MenuDO.class)
            .innerJoin(RoleScopeMenuDO.class)
                .on(column(MenuDO::getId).eq(column(RoleScopeMenuDO::getMenuId)))
            .where(RoleScopeMenuDO::getRoleId).eq(roleId)
            .and(MenuDO::getStatus).eq(1)
            .and(MenuDO::getDelFlag).eq(0);
        addScopeFilter(qw);
        qw.orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(qw);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 根据角色ID查询菜单ID列表
     */
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(RoleScopeMenuDO::getMenuId))
            .from(RoleScopeMenuDO.class)
            .innerJoin(MenuDO.class)
                .on(column(RoleScopeMenuDO::getMenuId).eq(column(MenuDO::getId)))
            .where(RoleScopeMenuDO::getRoleId).eq(roleId)
            .and(MenuDO::getStatus).eq(1)
            .and(MenuDO::getDelFlag).eq(0);
        addScopeFilter(qw);
        qw.orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        List<RoleScopeMenuDO> list = roleWorkspaceMenuMapper.selectListByQuery(qw);
        return list.stream().map(RoleScopeMenuDO::getMenuId).collect(Collectors.toList());
    }

    /**
     * 批量查询多个角色的菜单ID列表（修复 N+1）
     */
    public Map<Long, List<Long>> selectMenuIdsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        QueryWrapper qw = QueryWrapper.create()
            .select(column(RoleScopeMenuDO::getRoleId), column(RoleScopeMenuDO::getMenuId))
            .from(RoleScopeMenuDO.class)
            .innerJoin(MenuDO.class)
                .on(column(RoleScopeMenuDO::getMenuId).eq(column(MenuDO::getId)))
            .where(RoleScopeMenuDO::getRoleId).in(roleIds)
            .and(MenuDO::getStatus).eq(1)
            .and(MenuDO::getDelFlag).eq(0);
        addScopeFilter(qw);
        qw.orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        List<RoleScopeMenuDO> list = roleWorkspaceMenuMapper.selectListByQuery(qw);
        return list.stream().collect(Collectors.groupingBy(
            RoleScopeMenuDO::getRoleId,
            Collectors.mapping(RoleScopeMenuDO::getMenuId, Collectors.toList())
        ));
    }

    /**
     * 批量插入角色-菜单关联
     */
    public void insertRoleMenus(Long roleId, List<Long> menuIds) {
        Long tenantId = LoginContextHolder.getCurrentTenantId();
        insertRoleMenus(roleId, tenantId, tenantId, menuIds);
    }

    public List<Long> selectMenuIdsByRoleId(Long roleId, Long tenantId, Long scopedTenantId) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(RoleScopeMenuDO::getMenuId))
            .from(RoleScopeMenuDO.class)
            .innerJoin(MenuDO.class)
                .on(column(RoleScopeMenuDO::getMenuId).eq(column(MenuDO::getId)))
            .where(RoleScopeMenuDO::getRoleId).eq(roleId)
            .and(RoleScopeMenuDO::getTenantId).eq(tenantId)
            .and(RoleScopeMenuDO::getScopedTenantId).eq(scopedTenantId)
            .and(MenuDO::getStatus).eq(1)
            .and(MenuDO::getDelFlag).eq(0)
            .orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        return roleWorkspaceMenuMapper.selectListByQuery(qw).stream()
                .map(RoleScopeMenuDO::getMenuId)
                .toList();
    }

    public void insertRoleMenus(Long roleId, Long tenantId, Long scopedTenantId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        List<RoleScopeMenuDO> list = menuIds.stream().map(menuId -> {
            RoleScopeMenuDO rwm = new RoleScopeMenuDO();
            rwm.setRoleId(roleId);
            rwm.setMenuId(menuId);
            rwm.setTenantId(tenantId);
            rwm.setScopedTenantId(scopedTenantId);
            return rwm;
        }).toList();
        roleWorkspaceMenuMapper.insertBatch(list);
    }

    /**
     * 删除角色的所有菜单关联（按当前工作空间过滤）
     */
    public void deleteRoleMenus(Long roleId) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        deleteRoleMenus(roleId, currentTenantId, currentTenantId);
    }

    public void deleteRoleMenus(Long roleId, Long tenantId, Long scopedTenantId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(RoleScopeMenuDO::getRoleId, roleId);
        queryWrapper.eq(RoleScopeMenuDO::getTenantId, tenantId);
        queryWrapper.eq(RoleScopeMenuDO::getScopedTenantId, scopedTenantId);
        roleWorkspaceMenuMapper.deleteByQuery(queryWrapper);
    }

    private void addScopeFilter(QueryWrapper queryWrapper) {
        Long currentTenantId = LoginContextHolder.getCurrentTenantId();
        if (currentTenantId != null) {
            queryWrapper.and(RoleScopeMenuDO::getScopedTenantId).eq(currentTenantId);
        }
    }

}
