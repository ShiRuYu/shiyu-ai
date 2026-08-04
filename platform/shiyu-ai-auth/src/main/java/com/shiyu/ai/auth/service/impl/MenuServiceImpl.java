package com.shiyu.ai.auth.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.auth.port.repository.MenuRepository;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.auth.domain.model.MenuBO;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.vo.MenuVO;
import com.shiyu.ai.auth.vo.RouteMenuVO;
import com.shiyu.ai.auth.request.MenuRequest;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 菜单服务实现类
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {

    @Override public List<RouteMenuVO> routeMenusView(Long userId) { return toRouteMenus(getRouteMenusByUserId(userId)); }
    @Override public List<MenuVO> allTreeView() { return MapstructUtils.convert(getAllTree(), MenuVO.class); }
    @Override public List<RouteMenuVO> menuRootsView() { return toRouteMenus(getMenuRoots()); }
    @Override public List<RouteMenuVO> childrenView(Long parentId) { return toRouteMenus(getChildrenByParentId(parentId)); }
    @Override public List<RouteMenuVO> permissionsView() { return toRouteMenus(getMenuPermissionsTree()); }
    @Override public List<RouteMenuVO> treeView() { return toRouteMenus(getAllTree()); }
    @Override public boolean createMenu(MenuRequest request) { return createMenu(MapstructUtils.convert(request, MenuBO.class)); }
    @Override public boolean updateMenu(Long id, MenuRequest request) { return updateMenu(id, MapstructUtils.convert(request, MenuBO.class)); }

    private List<RouteMenuVO> toRouteMenus(List<MenuBO> menus) {
        if (menus == null) return List.of();
        List<RouteMenuVO> result = new ArrayList<>();
        for (MenuBO menu : menus) {
            RouteMenuVO vo = new RouteMenuVO(); vo.setId(menu.getId()); vo.setPid(menu.getParentId()); vo.setName(menu.getCode());
            vo.setPath(menu.getPath()); vo.setComponent(menu.getComponent()); vo.setRedirect(menu.getRedirect()); vo.setStatus(menu.getStatus()); vo.setIcon(menu.getIcon());
            String type=menu.getType(); vo.setType(type == null ? "menu" : type.toLowerCase(Locale.ROOT));
            RouteMenuVO.MetaVO meta = new RouteMenuVO.MetaVO(); meta.setTitle(menu.getName()); meta.setIcon(menu.getIcon()); meta.setOrder(menu.getOrder()); meta.setKeepAlive(menu.getKeepAlive());
            if (Boolean.FALSE.equals(menu.getShow())) meta.setHideInMenu(true); if ("none".equalsIgnoreCase(menu.getLayout())) meta.setNoBasicLayout(true); vo.setMeta(meta);
            vo.setChildren(toRouteMenus(menu.getChildren())); result.add(vo);
        }
        return result;
    }

    private static final Set<String> SUPPORTED_MENU_TYPES = Set.of(
            "CATALOG", "MENU", "LINK", "EMBEDDED");

    private final MenuRepository menuRepository;
    private final TenantRepository tenantRepository;

    /**
     * 路由菜单缓存：userId:currentTenantId:currentRoleId → 菜单树
     * 菜单数据由管理员维护，变更频率极低，适合 5 分钟本地缓存
     */
    private final Cache<String, List<MenuBO>> routeMenuCache;

    public MenuServiceImpl(MenuRepository menuRepository,
                           TenantRepository tenantRepository) {
        this.menuRepository = menuRepository;
        this.tenantRepository = tenantRepository;
        this.routeMenuCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    private List<MenuBO> getMenuPermissionsTree() {
        log.info("获取角色权限树 by token");
        return getAllTree();
    }

    private List<MenuBO> getMenuTree() {
        log.info("获取权限树 - 菜单");
        List<MenuBO> menus = menuRepository.selectAllByType("MENU");
        return buildMenuTree(menus);
    }

    private List<MenuBO> getAllTree() {
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
        if (menuRepository.selectById(id) == null) {
            return false;
        }
        boolean result = menuRepository.deleteById(id);
        if (result) {
            evictAllRouteMenuCache();
        }
        return result;
    }

    private boolean createMenu(MenuBO menuBO) {
        log.info("新增菜单");
        if (!isSupportedMenuType(menuBO)) {
            return false;
        }
        normalizeMenuType(menuBO);
        Long currentTenantId = UserContextHolder.getCurrentTenantId();
        if (currentTenantId == null || !isParentMenuInTenant(menuBO.getParentId(), currentTenantId)) {
            return false;
        }
        menuBO.setTenantId(currentTenantId);
        menuRepository.insert(menuBO);
        evictAllRouteMenuCache();
        return true;
    }

    private boolean updateMenu(Long id, MenuBO menuBO) {
        log.info("修改菜单，id: {}", id);

        MenuBO existingMenu = menuRepository.selectById(id);
        if (existingMenu == null) {
            return false;
        }
        if (!isSupportedMenuType(menuBO)) {
            return false;
        }
        normalizeMenuType(menuBO);
        Long currentTenantId = UserContextHolder.getCurrentTenantId();
        if (currentTenantId == null || !isParentMenuInTenant(menuBO.getParentId(), currentTenantId)) {
            return false;
        }

        menuBO.setId(id);
        menuBO.setTenantId(currentTenantId);
        boolean result = menuRepository.update(menuBO);
        if (result) {
            evictAllRouteMenuCache();
        }
        return result;
    }

    private List<MenuBO> getMenuRoots() {
        log.info("获取根节点菜单（parentId 为 null）");
        List<MenuBO> allMenus = menuRepository.selectAll();
        return allMenus.stream()
                .filter(m -> m.getParentId() == null)
                .sorted(Comparator.comparing(MenuBO::getOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(MenuBO::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<MenuBO> getChildrenByParentId(Long parentId) {
        log.info("获取子菜单，parentId: {}", parentId);
        return menuRepository.selectByParentId(parentId);
    }

    private List<MenuBO> getMenuTreeByUserId(Long userId) {
        log.info("根据用户 ID 获取菜单树，userId: {}", userId);
        // 复用单 JOIN 查询 + 建树
        List<MenuBO> userMenus = menuRepository.selectMenusByUserId(userId, null);
        if (userMenus.isEmpty()) {
            return new ArrayList<>();
        }
        return buildMenuTree(userMenus);
    }

    private List<MenuBO> getMenusByUserIdAndType(Long userId, String type) {
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

    private List<MenuBO> getRouteMenusByUserId(Long userId) {
        log.info("获取用户路由菜单，userId: {}", userId);
        String cacheKey = buildRouteMenuCacheKey(userId);

        // 1. 查缓存
        List<MenuBO> cached = routeMenuCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("路由菜单缓存命中，cacheKey: {}", cacheKey);
            return cached;
        }

        // 2. 单 SQL JOIN 查询，替代原来的 查角色→遍历查菜单→全表查→内存过滤 流程
        List<MenuBO> userMenus = menuRepository.selectMenusByUserId(userId, null);
        if (userMenus.isEmpty()) {
            log.warn("用户 {} 没有分配菜单", userId);
            return new ArrayList<>();
        }

        // 3. 建树
        List<MenuBO> tree = buildMenuTree(userMenus);

        // 4. 写入缓存
        routeMenuCache.put(cacheKey, tree);
        log.info("路由菜单已缓存，cacheKey: {}", cacheKey);
        return tree;
    }

    private String buildRouteMenuCacheKey(Long userId) {
        return userId + ":" + UserContextHolder.getCurrentTenantId() + ":"
            + UserContextHolder.getCurrentRoleId() + ":" + UserContextHolder.getCurrentRoleCode();
    }

    /**
     * 清除指定用户的路由菜单缓存
     */
    public void evictRouteMenuCache(Long userId) {
        if (userId != null) {
            routeMenuCache.asMap().keySet().removeIf(key -> key.startsWith(userId + ":"));
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

    private boolean isSupportedMenuType(MenuBO menuBO) {
        if (menuBO == null || menuBO.getType() == null) {
            return false;
        }
        return SUPPORTED_MENU_TYPES.contains(menuBO.getType().trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public PageData<MenuVO> getMenuPage(Number pageNo, Number pageSize,
                                        String name, String code, String type, Integer status) {
        var page = menuRepository.selectPage(pageNo, pageSize, name, code, type, status);
        return new PageData<>(MapstructUtils.convert(page.getRight(), MenuVO.class), page.getLeft());
    }

    private void normalizeMenuType(MenuBO menuBO) {
        menuBO.setType(menuBO.getType().trim().toUpperCase(Locale.ROOT));
    }

    private boolean isParentMenuInTenant(Long parentId, Long tenantId) {
        if (parentId == null) {
            return true;
        }
        MenuBO parent = menuRepository.selectById(parentId);
        return parent != null && tenantId.equals(parent.getTenantId());
    }
}
