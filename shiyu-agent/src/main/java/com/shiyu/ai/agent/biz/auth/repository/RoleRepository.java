package com.shiyu.ai.agent.biz.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.auth.MenuDO;
import com.shiyu.ai.agent.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.agent.dal.dataobject.auth.RoleWorkspaceMenuDO;
import com.shiyu.ai.agent.dal.mapper.auth.MenuMapper;
import com.shiyu.ai.agent.dal.mapper.auth.RoleMapper;
import com.shiyu.ai.agent.dal.mapper.auth.RoleWorkspaceMenuMapper;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private RoleWorkspaceMenuMapper roleWorkspaceMenuMapper;

    @Resource
    private MenuMapper menuMapper;

    /**
     * 分页查询角色列表
     */
    public Pair<Long, List<RoleBO>> selectPage(Number pageNo, Number pageSize, String name) {
        QueryWrapper countWrapper = new QueryWrapper();
        if (name != null && !name.isEmpty()) {
            countWrapper.eq(RoleDO::getName, name);
        }
        long count = roleMapper.selectCountByQuery(countWrapper);

        QueryWrapper queryWrapper = new QueryWrapper();
        if (name != null && !name.isEmpty()) {
            queryWrapper.eq(RoleDO::getName, name);
        }
        if (pageNo != null && pageSize != null) {
            queryWrapper.limit((pageNo.longValue() - 1) * pageSize.longValue(), pageSize.longValue());
        }
        
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
     * 根据角色ID查询菜单列表
     */
    public List<MenuBO> selectMenusByRoleId(Long roleId) {
        QueryWrapper qw = QueryWrapper.create()
            .from(MenuDO.class)
            .innerJoin(RoleWorkspaceMenuDO.class)
                .on(column(MenuDO::getId).eq(column(RoleWorkspaceMenuDO::getMenuId)))
            .where(RoleWorkspaceMenuDO::getRoleId).eq(roleId)
            .and(MenuDO::getStatus).eq("1")
            .and(MenuDO::getDelFlag).eq(0);
        qw.orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(qw);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 根据角色ID查询菜单ID列表
     */
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        QueryWrapper qw = QueryWrapper.create()
            .select(column(RoleWorkspaceMenuDO::getMenuId))
            .from(RoleWorkspaceMenuDO.class)
            .innerJoin(MenuDO.class)
                .on(column(RoleWorkspaceMenuDO::getMenuId).eq(column(MenuDO::getId)))
            .where(RoleWorkspaceMenuDO::getRoleId).eq(roleId)
            .and(MenuDO::getStatus).eq("1")
            .and(MenuDO::getDelFlag).eq(0);
        qw.orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        List<RoleWorkspaceMenuDO> list = roleWorkspaceMenuMapper.selectListByQuery(qw);
        return list.stream().map(RoleWorkspaceMenuDO::getMenuId).collect(Collectors.toList());
    }

    /**
     * 批量插入角色-菜单关联
     */
    public void insertRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        
        Long workspaceId = com.shiyu.ai.common.core.domain.LoginContextHolder.getCurrentWorkspaceId();
        Long tenantId = com.shiyu.ai.common.core.domain.LoginContextHolder.getTenantId();
        
        for (Long menuId : menuIds) {
            RoleWorkspaceMenuDO rwm = new RoleWorkspaceMenuDO();
            rwm.setRoleId(roleId);
            rwm.setWorkspaceId(workspaceId);
            rwm.setMenuId(menuId);
            rwm.setTenantId(tenantId);
            roleWorkspaceMenuMapper.insert(rwm);
        }
    }

    /**
     * 删除角色的所有菜单关联（按当前工作空间过滤）
     */
    public void deleteRoleMenus(Long roleId) {
        Long workspaceId = com.shiyu.ai.common.core.domain.LoginContextHolder.getCurrentWorkspaceId();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(RoleWorkspaceMenuDO::getRoleId, roleId);
        if (workspaceId != null) {
            queryWrapper.eq(RoleWorkspaceMenuDO::getWorkspaceId, workspaceId);
        }
        roleWorkspaceMenuMapper.deleteByQuery(queryWrapper);
    }

}
