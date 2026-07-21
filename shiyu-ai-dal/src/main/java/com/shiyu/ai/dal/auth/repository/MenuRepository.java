package com.shiyu.ai.dal.auth.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.auth.dataobject.MenuDO;
import com.shiyu.ai.dal.auth.dataobject.RoleWorkspaceMenuDO;
import com.shiyu.ai.dal.auth.dataobject.UserWorkspaceRoleDO;
import com.shiyu.ai.dal.auth.mapper.MenuMapper;
import com.shiyu.ai.dal.auth.bo.MenuBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mybatisflex.core.query.QueryMethods.column;

/**
 * 菜单数据仓储层
 */
@Component
public class MenuRepository {

    @Resource
    private MenuMapper menuMapper;

    /**
     * 查询所有菜单
     */
    public List<MenuBO> selectAll() {
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(new QueryWrapper());
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 根据类型查询菜单
     */
    public List<MenuBO> selectAllByType(String type) {
        QueryWrapper queryWrapper = new QueryWrapper()
                .where(MenuDO::getType).eq(type);
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 查询菜单（排除指定类型）
     */
    public List<MenuBO> selectAllExcludingType(String type) {
        QueryWrapper queryWrapper = new QueryWrapper()
                .where(MenuDO::getType).ne(type);
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(queryWrapper);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 根据ID查询菜单
     */
    public MenuBO selectById(Long id) {
        MenuDO menuDO = menuMapper.selectOneById(id);
        return MapstructUtils.convert(menuDO, MenuBO.class);
    }

    /**
     * 创建菜单
     */
    public MenuBO insert(MenuBO menuBO) {
        MenuDO menuDO = MapstructUtils.convert(menuBO, MenuDO.class);
        menuMapper.insertSelective(menuDO);
        menuBO.setId(menuDO.getId());
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
    public boolean deleteById(Long id) {
        return menuMapper.deleteById(id) > 0;
    }

    /**
     * 通过用户ID查询菜单（单SQL JOIN，消除N+1）
     * <p>
     * 一次性 JOIN user_workspace_role → role_workspace_menu → menu，
     * 替代原来的 查角色→遍历查菜单→内存过滤 流程
     *
     * @param userId      用户ID
     * @param excludeType 需要排除的菜单类型（如 BUTTON），传 null 则不过滤
     * @return 用户有权限的菜单列表（平铺，不含树结构）
     */
    public List<MenuBO> selectMenusByUserId(Long userId, String excludeType) {
        QueryWrapper qw = QueryWrapper.create()
                .from(MenuDO.class)
                .innerJoin(RoleWorkspaceMenuDO.class)
                .on(column(MenuDO::getId).eq(column(RoleWorkspaceMenuDO::getMenuId)))
                .innerJoin(UserWorkspaceRoleDO.class)
                .on(column(RoleWorkspaceMenuDO::getRoleId).eq(column(UserWorkspaceRoleDO::getRoleId)))
                .where(UserWorkspaceRoleDO::getUserId).eq(userId)
                .and(MenuDO::getStatus).eq(1)
                .and(MenuDO::getDelFlag).eq(0);
        if (excludeType != null) {
            qw.and(MenuDO::getType).ne(excludeType);
        }
        qw.groupBy(column(MenuDO::getId)).orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(qw);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 检查菜单名称是否已存在（SQL COUNT，避免全表加载）
     */
    public boolean existsByName(String name, Long excludeId) {
        QueryWrapper qw = new QueryWrapper()
                .where(MenuDO::getName).eq(name);
        if (excludeId != null) {
            qw.and(MenuDO::getId).ne(excludeId);
        }
        return menuMapper.selectCountByQuery(qw) > 0;
    }

    /**
     * 检查菜单路径是否已存在（SQL COUNT，避免全表加载）
     */
    public boolean existsByPath(String path, Long excludeId) {
        QueryWrapper qw = new QueryWrapper()
                .where(MenuDO::getPath).eq(path);
        if (excludeId != null) {
            qw.and(MenuDO::getId).ne(excludeId);
        }
        return menuMapper.selectCountByQuery(qw) > 0;
    }

    /**
     * 根据父ID查询菜单（平铺，用于懒加载）
     */
    public List<MenuBO> selectByParentId(Long parentId) {
        QueryWrapper qw = new QueryWrapper()
                .where(MenuDO::getParentId).eq(parentId)
                .and(MenuDO::getStatus).eq(1)
                .and(MenuDO::getDelFlag).eq(0);
        qw.orderBy(column(MenuDO::getOrder).asc(), column(MenuDO::getId).asc());
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(qw);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 根据父ID和类型查询菜单（避免全表查+内存过滤）
     */
    public List<MenuBO> selectByParentIdAndType(Long parentId, String type) {
        QueryWrapper qw = new QueryWrapper()
                .where(MenuDO::getParentId).eq(parentId)
                .and(MenuDO::getType).eq(type);
        List<MenuDO> menuDOs = menuMapper.selectListByQuery(qw);
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

}

