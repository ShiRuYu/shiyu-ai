package com.shiyu.ai.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.MenuDO;
import com.shiyu.ai.agent.dal.mapper.MenuMapper;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
        List<MenuDO> menuDOs = menuMapper.selectAll();
        return MapstructUtils.convert(menuDOs, MenuBO.class);
    }

    /**
     * 根据ID查询菜单
     */
    public MenuBO selectOneById(Long id) {
        MenuDO menuDO = menuMapper.selectOneById(id);
        return MapstructUtils.convert(menuDO, MenuBO.class);
    }

    /**
     * 根据类型过滤菜单
     */
    public List<MenuBO> filterByType(String type) {
        List<MenuDO> allMenus = menuMapper.selectAll();
        List<MenuDO> filteredMenus = allMenus.stream()
                .filter(menu -> type.equals(menu.getType()))
                .collect(Collectors.toList());
        return MapstructUtils.convert(filteredMenus, MenuBO.class);
    }

    /**
     * 插入菜单
     */
    public MenuBO insert(MenuBO menuBO) {
        MenuDO menuDO = MapstructUtils.convert(menuBO, MenuDO.class);
        menuMapper.insert(menuDO);
        return MapstructUtils.convert(menuDO, MenuBO.class);
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
}
