package com.shiyu.ai.agent.auth.service.impl;

import com.shiyu.ai.agent.auth.service.MenuService;
import com.shiyu.ai.agent.dal.dataobject.MenuDO;
import com.shiyu.ai.agent.dal.mapper.MenuMapper;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 菜单服务实现类（模拟数据）
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    public List<MenuBO> getMenuPermissionsTree() {
        log.info("获取角色权限树-by token");
        // 返回所有权限树
        return getAllTree();
    }

    @Override
    public List<MenuBO> getMenuTree() {
        log.info("获取权限树 - 菜单");
        // 只返回菜单类型的权限
        List<MenuDO> allMenus = menuMapper.selectAll();
        List<MenuDO> menus = filterByType(allMenus, "MENU");
        return MapstructUtils.convert(menus, MenuBO.class);
    }

    @Override
    public List<MenuBO> getAllTree() {
        log.info("获取权限树-all");
        List<MenuDO> allMenus = menuMapper.selectAll();
        return MapstructUtils.convert(allMenus, MenuBO.class);
    }

    private List<MenuDO> filterByType(List<MenuDO> menus, String type) {
        List<MenuDO> result = new ArrayList<>();
        for (MenuDO menu : menus) {
            if (type.equals(menu.getType())) {
                result.add(menu);
            }
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                result.addAll(filterByType(menu.getChildren(), type));
            }
        }
        return result;
    }

    @Override
    public boolean deleteMenu(Long id) {
        log.info("删除菜单，id: {}", id);
        return menuMapper.deleteById(id) > 0;
    }

    @Override
    public boolean createMenu(MenuBO menuBO) {
        log.info("新增菜单");
        MenuDO newMenu = MapstructUtils.convert(menuBO, MenuDO.class);
        
        menuMapper.insert(newMenu);
        
        return true;
    }

    @Override
    public boolean updateMenu(Long id, MenuBO menuBO) {
        log.info("修改菜单，id: {}", id);
        
        MenuDO existingMenu = menuMapper.selectOneById(id);
        if (existingMenu == null) {
            return false;
        }
        
        MenuDO updatedMenu = MapstructUtils.convert(menuBO, MenuDO.class);
        updatedMenu.setId(id);
        
        return menuMapper.update(updatedMenu) > 0;
    }

    @Override
    public List<MenuBO> getButtonsByParentId(Long parentId) {
        log.info("获取按钮权限-by parentId: {}", parentId);
        
        List<MenuBO> allMenus = getAllTree();
        List<MenuBO> buttons = new ArrayList<>();
        
        for (MenuBO menu : allMenus) {
            if (parentId.equals(menu.getParentId()) && "BUTTON".equals(menu.getType())) {
                buttons.add(menu);
            }
        }
        
        return buttons;
    }
}
