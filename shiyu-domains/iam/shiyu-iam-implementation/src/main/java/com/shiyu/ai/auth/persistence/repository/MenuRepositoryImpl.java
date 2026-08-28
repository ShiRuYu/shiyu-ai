package com.shiyu.ai.auth.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.auth.persistence.dataobject.MenuDO;
import com.shiyu.ai.auth.persistence.dataobject.RoleDO;
import com.shiyu.ai.auth.persistence.dataobject.RoleScopeMenuDO;
import com.shiyu.ai.auth.persistence.dataobject.TenantMenuDO;
import com.shiyu.ai.auth.persistence.dataobject.UserScopeRoleDO;
import com.shiyu.ai.auth.persistence.mapper.MenuMapper;
import com.shiyu.ai.auth.persistence.mapper.RoleScopeMenuMapper;
import com.shiyu.ai.auth.persistence.mapper.TenantMenuMapper;
import com.shiyu.ai.auth.domain.model.MenuBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static com.mybatisflex.core.query.QueryMethods.column;

/**
 * 菜单数据仓储层
 */
@Component
public class MenuRepositoryImpl implements com.shiyu.ai.auth.port.repository.MenuRepository {

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private RoleScopeMenuMapper roleScopeMenuMapper;

    @Resource
    private TenantMenuMapper tenantMenuMapper;

    /**
     * 查询所有菜单
     */
    public List<MenuBO> selectAll(TenantId tenantId) {
        QueryWrapper qw = new QueryWrapper();
        addMenuTenantFilter(qw, tenantId);
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(qw);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 根据类型查询菜单
     */
    public List<MenuBO> selectAllByType(TenantId tenantId, String type) {
        QueryWrapper queryWrapper = new QueryWrapper()
                .where(MenuDO::getType).eq(type);
        addMenuTenantFilter(queryWrapper, tenantId);
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 查询菜单（排除指定类型）
     */
    public List<MenuBO> selectAllExcludingType(TenantId tenantId, String type) {
        QueryWrapper queryWrapper = new QueryWrapper()
                .where(MenuDO::getType).ne(type);
        addMenuTenantFilter(queryWrapper, tenantId);
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 根据ID查询菜单
     */
    public MenuBO selectById(TenantId tenantId, Long id) {
        QueryWrapper qw = QueryWrapper.create()
                .where(MenuDO::getId).eq(id)
                .and(MenuDO::getDelFlag).eq(0);
        addMenuTenantFilter(qw, tenantId);
        MenuDO menuDO = menuMapper.selectOneByQuery(qw);
        return MapstructUtils.convert(menuDO, MenuBO.class);
    }

    /**
     * 创建菜单
     */
    public MenuBO insert(MenuBO menuBO) {
        MenuDO menuDO = MapstructUtils.convert(menuBO, MenuDO.class);
        menuMapper.insertSelective(menuDO);
        menuBO.setId(menuDO.getId());
        if (menuBO.getTenantId() != null) {
            TenantMenuDO tenantMenu = new TenantMenuDO();
            tenantMenu.setTenantId(menuBO.getTenantId());
            tenantMenu.setMenuId(menuDO.getId());
            tenantMenu.setStatus(1);
            tenantMenuMapper.insert(tenantMenu);
        }
        return menuBO;
    }

    /**
     * 更新菜单
     */
    public boolean update(MenuBO menuBO) {
        MenuDO menuDO = MapstructUtils.convert(menuBO, MenuDO.class);
        return menuMapper.update(menuDO) > 0;
    }

    /**
     * 删除菜单
     */
    public boolean deleteById(TenantId tenantId, Long id) {
        List<Long> ids = new java.util.ArrayList<>();
        collectSubtreeIds(tenantId, id, ids);
        if (ids.isEmpty()) {
            return false;
        }
        roleScopeMenuMapper.deleteByQuery(QueryWrapper.create()
                .where(RoleScopeMenuDO::getMenuId).in(ids));
        tenantMenuMapper.deleteByQuery(QueryWrapper.create()
                .where(TenantMenuDO::getMenuId).in(ids));
        QueryWrapper deleteQuery = QueryWrapper.create()
                .where(MenuDO::getId).in(ids);
        addMenuTenantFilter(deleteQuery, tenantId);
        menuMapper.deleteByQuery(deleteQuery);
        return true;
    }

    public Pair<Long, List<MenuBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize,
                                               String name, String code,
                                               String type, Integer status) {
        QueryWrapper count = buildManageQuery(tenantId, name, code, type, status);
        long total = menuMapper.selectCountByQuery(count);
        QueryWrapper query = buildManageQuery(tenantId, name, code, type, status);
        int page = pageNo == null ? 1 : pageNo.intValue();
        int size = pageSize == null ? 10 : pageSize.intValue();
        query.limit((long) (page - 1) * size, size);
        query.orderBy(MenuDO::getOrder, true).orderBy(MenuDO::getId, true);
        return Pair.of(total, MapstructUtils.convert(
                menuMapper.selectListByQuery(query), MenuBO.class));
    }

    private QueryWrapper buildManageQuery(TenantId tenantId, String name, String code, String type, Integer status) {
        QueryWrapper query = QueryWrapper.create()
                .where(MenuDO::getDelFlag).eq(0);
        if (name != null && !name.isBlank()) query.and(MenuDO::getName).like(name);
        if (code != null && !code.isBlank()) query.and(MenuDO::getCode).like(code);
        if (type != null && !type.isBlank()) query.and(MenuDO::getType).eq(type.toUpperCase());
        if (status != null) query.and(MenuDO::getStatus).eq(status);
        addMenuTenantFilter(query, tenantId);
        return query;
    }

    private void collectSubtreeIds(TenantId tenantId, Long parentId, List<Long> ids) {
        QueryWrapper currentQuery = QueryWrapper.create()
                .where(MenuDO::getId).eq(parentId)
                .and(MenuDO::getDelFlag).eq(0);
        addMenuTenantFilter(currentQuery, tenantId);
        MenuDO current = menuMapper.selectOneByQuery(currentQuery);
        if (current == null) {
            return;
        }
        ids.add(parentId);
        QueryWrapper childrenQuery = QueryWrapper.create()
                .where(MenuDO::getParentId).eq(parentId)
                .and(MenuDO::getDelFlag).eq(0);
        addMenuTenantFilter(childrenQuery, tenantId);
        List<MenuDO> children = menuMapper.selectListByQuery(childrenQuery);
        for (MenuDO child : children) {
            collectSubtreeIds(tenantId, child.getId(), ids);
        }
    }

    /**
     * 通过用户ID查询菜单（单SQL JOIN，消除N+1）
     * <p>
     * 一次性 JOIN user_scope_role → role_scope_menu → menu，
     * 替代原来的 查角色→遍历查菜单→内存过滤 流程
     *
     * @param userId      用户ID
     * @param excludeType 需要排除的菜单类型，传 null 则不过滤
     * @return 用户有权限的菜单列表（平铺，不含树结构）
     */
    public List<MenuBO> selectMenusByUserId(TenantId tenantId, Long userId, String roleCode,
                                            boolean parentSuperAdminSwitch, String excludeType) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        long tenant = tenantId.value();
        if (parentSuperAdminSwitch) {
            QueryWrapper delegated = QueryWrapper.create()
                    .from(MenuDO.class)
                    .innerJoin(RoleScopeMenuDO.class)
                    .on(column(MenuDO::getId).eq(column(RoleScopeMenuDO::getMenuId)))
                    .innerJoin(RoleDO.class)
                    .on(column(RoleScopeMenuDO::getRoleId).eq(column(RoleDO::getId)))
                    .where(RoleDO::getCode).eq(roleCode)
                    .and(RoleDO::getTenantId).eq(tenant)
                    .and(RoleScopeMenuDO::getTenantId).eq(tenant)
                    .and(MenuDO::getTenantId).eq(tenant)
                    .and(MenuDO::getStatus).eq(1)
                    .and(MenuDO::getDelFlag).eq(0);
            if (excludeType != null) {
                delegated.and(MenuDO::getType).ne(excludeType);
            }
            delegated.orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
            return MapstructUtils.convert(menuMapper.selectListByQuery(delegated), MenuBO.class);
        }
        QueryWrapper qw = QueryWrapper.create()
                .from(MenuDO.class)
                .innerJoin(RoleScopeMenuDO.class)
                .on(column(MenuDO::getId).eq(column(RoleScopeMenuDO::getMenuId)))
                .innerJoin(RoleDO.class)
                .on(column(RoleScopeMenuDO::getRoleId).eq(column(RoleDO::getId)))
                .innerJoin(UserScopeRoleDO.class)
                .on(column(RoleDO::getId).eq(column(UserScopeRoleDO::getRoleId)))
                .where(UserScopeRoleDO::getUserId).eq(userId)
                .and(column(RoleScopeMenuDO::getTenantId).eq(column(UserScopeRoleDO::getTenantId)))
                .and(MenuDO::getStatus).eq(1)
                .and(MenuDO::getDelFlag).eq(0);
        // 按当前角色编码过滤菜单，只返回当前角色有权限的菜单
        addUserRoleFilter(qw, roleCode);
        addMenuTenantFilter(qw, tenantId);
        // 仅在当前租户下查询角色分配
        addUserScopeTenantFilter(qw, tenantId);
        if (excludeType != null) {
            qw.and(MenuDO::getType).ne(excludeType);
        }
        qw.orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(qw);
        // 一个用户可能在一个角色下拥有多个角色分配条目，用 Java 去重
        menuDOs = menuDOs.stream().distinct().toList();
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 检查菜单名称是否已存在（SQL COUNT，避免全表加载）
     */
    public boolean existsByName(TenantId tenantId, String name, Long excludeId) {
        QueryWrapper qw = new QueryWrapper()
                .where(MenuDO::getName).eq(name);
        addMenuTenantFilter(qw, tenantId);
        if (excludeId != null) {
            qw.and(MenuDO::getId).ne(excludeId);
        }
        return menuMapper.selectCountByQuery(qw) > 0;
    }

    /**
     * 检查菜单路径是否已存在（SQL COUNT，避免全表加载）
     */
    public boolean existsByPath(TenantId tenantId, String path, Long excludeId) {
        QueryWrapper qw = new QueryWrapper()
                .where(MenuDO::getPath).eq(path);
        addMenuTenantFilter(qw, tenantId);
        if (excludeId != null) {
            qw.and(MenuDO::getId).ne(excludeId);
        }
        return menuMapper.selectCountByQuery(qw) > 0;
    }

    /**
     * 根据父ID查询菜单（平铺，用于懒加载）
     */
    public List<MenuBO> selectByParentId(TenantId tenantId, Long parentId) {
        QueryWrapper qw = new QueryWrapper()
                .where(MenuDO::getParentId).eq(parentId)
                .and(MenuDO::getStatus).eq(1)
                .and(MenuDO::getDelFlag).eq(0);
        addMenuTenantFilter(qw, tenantId);
        qw.orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(qw);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 根据父ID和类型查询菜单（避免全表查+内存过滤）
     */
    public List<MenuBO> selectByParentIdAndType(TenantId tenantId, Long parentId, String type) {
        QueryWrapper qw = new QueryWrapper()
                .where(MenuDO::getParentId).eq(parentId)
                .and(MenuDO::getType).eq(type);
        addMenuTenantFilter(qw, tenantId);
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(qw);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    // ========== 租户过滤辅助方法 ==========

    /**
     * 添加当前角色过滤：role.code = currentRoleCode
     * 确保只查询当前角色有权限的菜单
     */
    private void addUserRoleFilter(QueryWrapper qw, String roleCode) {
        if (roleCode != null) {
            qw.and(RoleDO::getCode).eq(roleCode);
        } else {
            // 用户未选择角色时，不去重、不过滤角色（让 Service 层或前端处理）
        }
    }

    /**
     * 添加菜单表租户过滤：menu.tenant_id = currentTenantId
     * 确保查询的菜单属于当前用户可见的租户范围
     */
    private void addMenuTenantFilter(QueryWrapper qw, TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        qw.eq(MenuDO::getTenantId, tenantId.value());
    }

    /**
     * 添加用户租户过滤：user_scope_role.tenant_id = currentTenantId
     * 确保只查询当前租户下分配的角色菜单
     */
    private void addUserScopeTenantFilter(QueryWrapper qw, TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        qw.eq(UserScopeRoleDO::getTenantId, tenantId.value());
    }

}

