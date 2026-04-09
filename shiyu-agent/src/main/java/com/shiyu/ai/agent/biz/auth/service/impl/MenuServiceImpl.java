package com.shiyu.ai.agent.biz.auth.service.impl;

import com.shiyu.ai.agent.biz.auth.repository.MenuRepository;
import com.shiyu.ai.agent.biz.auth.repository.RoleRepository;
import com.shiyu.ai.agent.biz.auth.repository.UserRepository;
import com.shiyu.ai.agent.biz.auth.service.MenuService;
import com.shiyu.ai.agent.domain.bo.MenuBO;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public MenuServiceImpl(MenuRepository menuRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
        // 查询所有菜单类型
        List<MenuBO> menus = menuRepository.selectAllByType("MENU");
        
        // 构建树形结构
        return buildMenuTree(menus, null);
    }

    @Override
    public List<MenuBO> getAllTree() {
        log.info("获取权限树-all");
        // 查询所有菜单
        List<MenuBO> allMenuBOs = menuRepository.selectAll();
        
        // 构建树形结构
        return buildMenuTree(allMenuBOs, null);
    }
    
    /**
     * 构建菜单树形结构
     * @param allMenus 所有菜单列表
     * @param parentId 父菜单 ID，null 表示根节点
     * @return 树形结构的菜单列表
     */
    private List<MenuBO> buildMenuTree(List<MenuBO> allMenus, Long parentId) {
        List<MenuBO> tree = new ArrayList<>();
        
        for (MenuBO menu : allMenus) {
            // 如果 parentId 为 null，查找所有根节点（parent_id 为 null 的菜单）
            // 如果 parentId 不为 null，查找指定父节点的子菜单
            boolean isMatch = (parentId == null && menu.getParentId() == null) ||
                             (parentId != null && parentId.equals(menu.getParentId()));
            
            if (isMatch) {
                // 递归查找子菜单
                List<MenuBO> children = buildMenuTree(allMenus, menu.getId());
                if (children != null && !children.isEmpty()) {
                    menu.setChildren(children);
                }
                tree.add(menu);
            }
        }
        
        return tree;
    }



    @Override
    public boolean deleteMenu(Long id) {
        log.info("删除菜单，id: {}", id);
        return menuRepository.deleteById(id);
    }

    @Override
    public boolean createMenu(MenuBO menuBO) {
        log.info("新增菜单");
        menuRepository.insert(menuBO);
        return true;
    }

    @Override
    public boolean updateMenu(Long id, MenuBO menuBO) {
        log.info("修改菜单，id: {}", id);
        
        MenuBO existingMenu = menuRepository.selectById(id);
        if (existingMenu == null) {
            return false;
        }
        
        menuBO.setId(id);
        return menuRepository.update(menuBO);
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
    
    @Override
    public List<MenuBO> getMenuTreeByUserId(Long userId) {
        log.info("根据用户 ID 获取菜单树，userId: {}", userId);
        
        // 1. 查询用户的角色列表
        List<RoleBO> roles = userRepository.selectRolesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            log.warn("用户 {} 没有分配角色", userId);
            return new ArrayList<>();
        }
        
        // 2. 收集所有角色的菜单 ID（去重）
        Set<Long> menuIds = new HashSet<>();
        for (RoleBO role : roles) {
            List<MenuBO> menus = roleRepository.selectMenusByRoleId(role.getId());
            if (menus != null) {
                for (MenuBO menu : menus) {
                    menuIds.add(menu.getId());
                }
            }
        }
        
        if (menuIds.isEmpty()) {
            log.warn("用户 {} 的角色没有分配菜单", userId);
            return new ArrayList<>();
        }
        
        // 3. 查询所有菜单
        List<MenuBO> allMenus = menuRepository.selectAll();
        
        // 4. 过滤出用户有权限的菜单
        List<MenuBO> userMenuBOs = allMenus.stream()
                .filter(menu -> menuIds.contains(menu.getId()))
                .collect(Collectors.toList());
        
        // 5. 构建树形结构
        return buildMenuTree(userMenuBOs, null);
    }
}
