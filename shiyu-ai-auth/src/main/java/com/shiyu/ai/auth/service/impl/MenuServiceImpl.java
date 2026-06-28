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
 * 鑿滃崟鏈嶅姟瀹炵幇绫?
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    /**
     * 璺敱鑿滃崟缂撳瓨锛歶serId 鈫?鑿滃崟鏍?
     * 鑿滃崟鏁版嵁鐢辩鐞嗗憳缁存姢锛屽彉鏇撮鐜囨瀬浣庯紝閫傚悎 5 鍒嗛挓鏈湴缂撳瓨
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
        log.info("鑾峰彇瑙掕壊鏉冮檺鏍?by token");
        return getAllTree();
    }

    @Override
    public List<MenuBO> getMenuTree() {
        log.info("鑾峰彇鏉冮檺鏍?- 鑿滃崟");
        List<MenuBO> menus = menuRepository.selectAllByType("MENU");
        return buildMenuTree(menus);
    }

    @Override
    public List<MenuBO> getAllTree() {
        log.info("鑾峰彇鏉冮檺鏍?all");
        List<MenuBO> allMenuBOs = menuRepository.selectAll();
        return buildMenuTree(allMenuBOs);
    }

    /**
     * 鏋勫缓鑿滃崟鏍戝舰缁撴瀯锛圤(n) Map 鍒嗙粍锛屾浛浠ｅ師 O(n虏) 閫掑綊锛?
     */
    private List<MenuBO> buildMenuTree(List<MenuBO> allMenus) {
        if (allMenus == null || allMenus.isEmpty()) {
            return new ArrayList<>();
        }

        // 涓€娆￠亶鍘嗗缓绔?parentId 鈫?children 鏄犲皠
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

        // 閫掑綊鎸傝浇瀛愯妭鐐?
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
        log.info("鍒犻櫎鑿滃崟锛宨d: {}", id);
        boolean result = menuRepository.deleteById(id);
        if (result) {
            evictAllRouteMenuCache();
        }
        return result;
    }

    @Override
    public boolean createMenu(MenuBO menuBO) {
        log.info("鏂板鑿滃崟");
        menuRepository.insert(menuBO);
        evictAllRouteMenuCache();
        return true;
    }

    @Override
    public boolean updateMenu(Long id, MenuBO menuBO) {
        log.info("淇敼鑿滃崟锛宨d: {}", id);

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
        log.info("鑾峰彇鏍硅妭鐐硅彍鍗曪紙parentId 涓?null锛?);
        List<MenuBO> allMenus = menuRepository.selectAll();
        return allMenus.stream()
                .filter(m -> m.getParentId() == null)
                .sorted(Comparator.comparing(MenuBO::getOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(MenuBO::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    public List<MenuBO> getChildrenByParentId(Long parentId) {
        log.info("鑾峰彇瀛愯彍鍗曪紝parentId: {}", parentId);
        return menuRepository.selectByParentId(parentId);
    }

    @Override
    public List<MenuBO> getButtonsByParentId(Long parentId) {
        log.info("鑾峰彇鎸夐挳鏉冮檺 by parentId: {}", parentId);
        // 鐩存帴 SQL 鏌ヨ锛屾浛浠ｅ師 getAllTree() 鍏ㄩ噺鍔犺浇鍚庡啀鍐呭瓨杩囨护
        return menuRepository.selectByParentIdAndType(parentId, "BUTTON");
    }

    @Override
    public List<MenuBO> getMenuTreeByUserId(Long userId) {
        log.info("鏍规嵁鐢ㄦ埛 ID 鑾峰彇鑿滃崟鏍戯紝userId: {}", userId);
        // 澶嶇敤鍗?JOIN 鏌ヨ + 寤烘爲
        List<MenuBO> userMenus = menuRepository.selectMenusByUserId(userId, null);
        if (userMenus.isEmpty()) {
            return new ArrayList<>();
        }
        return buildMenuTree(userMenus);
    }

    @Override
    public List<MenuBO> getMenusByUserIdAndType(Long userId, String type) {
        log.info("鏍规嵁鐢ㄦ埛 ID 鍜岀被鍨嬭幏鍙栬彍鍗曟爲锛寀serId: {}, type: {}", userId, type);
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
        log.info("妫€鏌ヨ彍鍗曞悕绉版槸鍚﹀瓨鍦紝name: {}, id: {}", name, id);
        // SQL COUNT锛屾浛浠ｅ師鍏ㄨ〃鍔犺浇 + 鍐呭瓨杩囨护
        return menuRepository.existsByName(name, id);
    }

    @Override
    public boolean isMenuPathExists(String path, Long id) {
        log.info("妫€鏌ヨ彍鍗曡矾寰勬槸鍚﹀瓨鍦紝path: {}, id: {}", path, id);
        // SQL COUNT锛屾浛浠ｅ師鍏ㄨ〃鍔犺浇 + 鍐呭瓨杩囨护
        return menuRepository.existsByPath(path, id);
    }

    @Override
    public List<MenuBO> getRouteMenusByUserId(Long userId) {
        log.info("鑾峰彇鐢ㄦ埛璺敱鑿滃崟锛堟帓闄?BUTTON锛夛紝userId: {}", userId);

        // 1. 鏌ョ紦瀛?
        List<MenuBO> cached = routeMenuCache.getIfPresent(userId);
        if (cached != null) {
            log.debug("璺敱鑿滃崟缂撳瓨鍛戒腑锛寀serId: {}", userId);
            return cached;
        }

        // 2. 鍗?SQL JOIN 鏌ヨ锛屾浛浠ｅ師鏉ョ殑 鏌ヨ鑹测啋閬嶅巻鏌ヨ彍鍗曗啋鍏ㄨ〃鏌モ啋鍐呭瓨杩囨护 娴佺▼
        List<MenuBO> userMenus = menuRepository.selectMenusByUserId(userId, "BUTTON");
        if (userMenus.isEmpty()) {
            log.warn("鐢ㄦ埛 {} 娌℃湁鍒嗛厤鑿滃崟", userId);
            return new ArrayList<>();
        }

        // 3. 寤烘爲
        List<MenuBO> tree = buildMenuTree(userMenus);

        // 4. 鍐欏叆缂撳瓨
        routeMenuCache.put(userId, tree);
        log.info("璺敱鑿滃崟宸茬紦瀛橈紝userId: {}", userId);
        return tree;
    }

    /**
     * 娓呴櫎鎸囧畾鐢ㄦ埛鐨勮矾鐢辫彍鍗曠紦瀛?
     */
    public void evictRouteMenuCache(Long userId) {
        if (userId != null) {
            routeMenuCache.invalidate(userId);
            log.debug("璺敱鑿滃崟缂撳瓨宸叉竻闄わ紝userId: {}", userId);
        }
    }

    /**
     * 娓呴櫎鎵€鏈夌敤鎴风殑璺敱鑿滃崟缂撳瓨锛堣彍鍗曞鍒犳敼鍚庤皟鐢級
     */
    public void evictAllRouteMenuCache() {
        routeMenuCache.invalidateAll();
        log.info("鍏ㄩ儴璺敱鑿滃崟缂撳瓨宸叉竻闄わ紙鑿滃崟缁撴瀯鍙樻洿锛?);
    }
}


