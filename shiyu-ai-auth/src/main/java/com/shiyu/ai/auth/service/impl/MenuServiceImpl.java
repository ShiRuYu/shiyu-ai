package com.shiyu.ai.auth.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.dal.repository.MenuRepository;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.model.bo.MenuBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * 菜单服务实现类
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    /**
     * 路由菜单缓存：userId → 菜单树
     * 菜单数据由管理员维护，变更频率极低，适合 5 分钟本地缓存
     */
    private final Cache<Long, List<MenuBO>> routeMenuCache;

    public MenuServiceImpl(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
        this.routeMenuCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    @Override
    public List<MenuBO> getMenuPermissionsTree() {
        log.info("获取角色权限树 by token");
        return getAllTree();
    }

    @Override
    public List<MenuBO> getMenuTree() {
        log.info("获取权限树 - 菜单");
        List<MenuBO> menus = menuRepository.selectAllByType("MENU");
        return buildMenuTree(menus);
    }

    @Override
    public List<MenuBO> getAllTree() {
        log.info("获取权限树 all");
        List<MenuBO> allMenuBOs = menuRepository.selectAll();
        return buildMenuTree(allMenuBOs);
    }

    /**
     * 构建菜单树形结构（O(n) Map 分组，替代原 O(n²) 递归）
     */
    private List<MenuBO> buildMenuTree(List<MenuBO> allMenus) {
        if (allMenus == null || allMenus.isEmpty()) {
            return new ArrayList<>();
        }

        // 一次遍历建立 parentId → children 映射
        Map<Long, List<MenuBO>> childrenMap = new HashMap<>();
        List<MenuBO> roots = new ArrayList<>();

        for (MenuBO menu : allMenus) {
            Long pid = menu.getParentId();
            if (pid == null) {
                roots.add(menu);
            } else {
                childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(menu);
            }
        }

        // 递归挂载子节点
        for (MenuBO root : roots) {
            attachChildren(root, childrenMap);
        }
        return roots;
    }

    private void attachChildren(MenuBO parent, Map<Long, List<MenuBO>> childrenMap) {
        List<MenuBO> children = childrenMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            parent.setChildren(children);
            for (MenuBO child : children) {
                attachChildren(child, childrenMap);
            }
        }
    }

    @Override
    public boolean deleteMenu(Long id) {
        log.info("删除菜单，id: {}", id);
        boolean result = menuRepository.deleteById(id);
        if (result) {
            evictAllRouteMenuCache();
        }
        return result;
    }

    @Override
    public boolean createMenu(MenuBO menuBO) {
        log.info("新增菜单");
        menuRepository.insert(menuBO);
        evictAllRouteMenuCache();
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
        boolean result = menuRepository.update(menuBO);
        if (result) {
            evictAllRouteMenuCache();
        }
        return result;
    }

    @Override
    public List<MenuBO> getMenuRoots() {
        log.info("获取根节点菜单（parentId 为 null）");
        List<MenuBO> allMenus = menuRepository.selectAll();
        return allMenus.stream()
                .filter(m -> m.getParentId() == null)
                .sorted(Comparator.comparing(MenuBO::getOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(MenuBO::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    public List<MenuBO> getChildrenByParentId(Long parentId) {
        log.info("获取子菜单，parentId: {}", parentId);
        return menuRepository.selectByParentId(parentId);
    }

    @Override
    public List<MenuBO> getButtonsByParentId(Long parentId) {
        log.info("获取按钮权限 by parentId: {}", parentId);
        // 直接 SQL 查询，替代原 getAllTree() 全量加载后再内存过滤
        return menuRepository.selectByParentIdAndType(parentId, "BUTTON");
    }

    @Override
    public List<MenuBO> getMenuTreeByUserId(Long userId) {
        log.info("根据用户 ID 获取菜单树，userId: {}", userId);
        // 复用单 JOIN 查询 + 建树
        List<MenuBO> userMenus = menuRepository.selectMenusByUserId(userId, null);
        if (userMenus.isEmpty()) {
            return new ArrayList<>();
        }
        return buildMenuTree(userMenus);
    }

    @Override
    public List<MenuBO> getMenusByUserIdAndType(Long userId, String type) {
        log.info("根据用户 ID 和类型获取菜单树，userId: {}, type: {}", userId, type);
        List<MenuBO> userMenus = menuRepository.selectMenusByUserId(userId, null);
        if (userMenus.isEmpty()) {
            return new ArrayList<>();
        }
        List<MenuBO> filtered = userMenus.stream()
                .filter(menu -> type.equals(menu.getType()))
                .toList();
        return buildMenuTree(filtered);
    }

    @Override
    public boolean isMenuNameExists(String name, Long id) {
        log.info("检查菜单名称是否存在，name: {}, id: {}", name, id);
        // SQL COUNT，替代原全表加载 + 内存过滤
        return menuRepository.existsByName(name, id);
    }

    @Override
    public boolean isMenuPathExists(String path, Long id) {
        log.info("检查菜单路径是否存在，path: {}, id: {}", path, id);
        // SQL COUNT，替代原全表加载 + 内存过滤
        return menuRepository.existsByPath(path, id);
    }

    @Override
    public List<MenuBO> getRouteMenusByUserId(Long userId) {
        log.info("获取用户路由菜单（排除 BUTTON），userId: {}", userId);

        // 1. 查缓存
        List<MenuBO> cached = routeMenuCache.getIfPresent(userId);
        if (cached != null) {
            log.debug("路由菜单缓存命中，userId: {}", userId);
            return cached;
        }

        // 2. 单 SQL JOIN 查询，替代原来的 查角色→遍历查菜单→全表查→内存过滤 流程
        List<MenuBO> userMenus = menuRepository.selectMenusByUserId(userId, "BUTTON");
        if (userMenus.isEmpty()) {
            log.warn("用户 {} 没有分配菜单", userId);
            return new ArrayList<>();
        }

        // 3. 建树
        List<MenuBO> tree = buildMenuTree(userMenus);

        // 4. 写入缓存
        routeMenuCache.put(userId, tree);
        log.info("路由菜单已缓存，userId: {}", userId);
        return tree;
    }

    /**
     * 清除指定用户的路由菜单缓存
     */
    public void evictRouteMenuCache(Long userId) {
        if (userId != null) {
            routeMenuCache.invalidate(userId);
            log.debug("路由菜单缓存已清除，userId: {}", userId);
        }
    }

    /**
     * 清除所有用户的路由菜单缓存（菜单增删改后调用）
     */
    public void evictAllRouteMenuCache() {
        routeMenuCache.invalidateAll();
        log.info("全部路由菜单缓存已清除（菜单结构变更）");
    }
}


