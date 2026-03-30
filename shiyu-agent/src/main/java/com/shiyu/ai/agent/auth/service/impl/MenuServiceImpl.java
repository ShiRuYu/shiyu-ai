package com.shiyu.ai.agent.auth.service.impl;

import com.shiyu.ai.agent.auth.service.MenuService;
import com.shiyu.ai.agent.dal.dataobject.MenuDO;
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

    // 模拟菜单数据
    private final Map<Long, MenuDO> menuDatabase = new HashMap<>();

    public MenuServiceImpl() {
        initMockData();
    }

    private void initMockData() {
        // 根菜单
        MenuDO menu1 = new MenuDO();
        menu1.setId(9L);
        menu1.setName("基础功能");
        menu1.setCode("Base");
        menu1.setType("MENU");
        menu1.setParentId(null);
        menu1.setPath("");
        menu1.setRedirect(null);
        menu1.setIcon("i-fe:grid");
        menu1.setComponent(null);
        menu1.setLayout("");
        menu1.setKeepAlive(null);
        menu1.setMethod(null);
        menu1.setDescription(null);
        menu1.setShow(true);
        menu1.setEnable(true);
        menu1.setOrder(0);
        menu1.setChildren(new ArrayList<>());

        // 子菜单
        MenuDO menu2 = new MenuDO();
        menu2.setId(14L);
        menu2.setName("图标 Icon");
        menu2.setCode("Icon");
        menu2.setType("MENU");
        menu2.setParentId(9L);
        menu2.setPath("/base/icon");
        menu2.setRedirect(null);
        menu2.setIcon("i-fe:feather");
        menu2.setComponent(null);
        menu2.setLayout(null);
        menu2.setKeepAlive(null);
        menu2.setMethod(null);
        menu2.setDescription(null);
        menu2.setShow(true);
        menu2.setEnable(true);
        menu2.setOrder(1);
        menu2.setChildren(new ArrayList<>());

        menu1.getChildren().add(menu2);

        // 按钮权限
        MenuDO button1 = new MenuDO();
        button1.setId(13L);
        button1.setName("创建新用户");
        button1.setCode("AddUser");
        button1.setType("BUTTON");
        button1.setParentId(4L);
        button1.setPath(null);
        button1.setRedirect(null);
        button1.setIcon(null);
        button1.setComponent(null);
        button1.setLayout(null);
        button1.setKeepAlive(null);
        button1.setMethod(null);
        button1.setDescription(null);
        button1.setShow(true);
        button1.setEnable(true);
        button1.setOrder(1);
        button1.setChildren(new ArrayList<>());

        menuDatabase.put(9L, menu1);
        menuDatabase.put(14L, menu2);
        menuDatabase.put(13L, button1);
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
        List<MenuDO> allMenus = new ArrayList<>(menuDatabase.values());
        List<MenuDO> menus = filterByType(allMenus, "MENU");
        return MapstructUtils.convert(menus, MenuBO.class);
    }

    @Override
    public List<MenuBO> getAllTree() {
        log.info("获取权限树-all");
        List<MenuDO> allMenus = new ArrayList<>(menuDatabase.values());
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
        MenuDO removed = menuDatabase.remove(id);
        return removed != null;
    }

    @Override
    public boolean createMenu(MenuBO menuBO) {
        log.info("新增菜单");
        Long newId = menuDatabase.keySet().stream().max(Long::compareTo).orElse(0L) + 1;
        
        MenuDO newMenu = MapstructUtils.convert(menuBO, MenuDO.class);
        newMenu.setId(newId);
        menuDatabase.put(newId, newMenu);
        
        return true;
    }

    @Override
    public boolean updateMenu(Long id, MenuBO menuBO) {
        log.info("修改菜单，id: {}", id);
        
        MenuDO existingMenu = menuDatabase.get(id);
        if (existingMenu == null) {
            return false;
        }
        
        MenuDO updatedMenu = MapstructUtils.convert(menuBO, MenuDO.class);
        updatedMenu.setId(id);
        menuDatabase.put(id, updatedMenu);
        
        return true;
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
