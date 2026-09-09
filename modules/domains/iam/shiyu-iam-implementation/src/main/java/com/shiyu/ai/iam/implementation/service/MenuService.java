package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.iam.implementation.request.MenuRequest;
import com.shiyu.ai.iam.implementation.vo.MenuVO;
import com.shiyu.ai.iam.implementation.vo.RouteMenuVO;
import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/** Menu application contract. */
public interface MenuService {
    List<RouteMenuVO> routeMenusView(ActorContext actor);
    List<MenuVO> allTreeView(ActorContext actor);
    List<RouteMenuVO> menuRootsView(ActorContext actor);
    List<RouteMenuVO> childrenView(ActorContext actor, Long parentId);
    List<RouteMenuVO> permissionsView(ActorContext actor);
    List<RouteMenuVO> treeView(ActorContext actor);
    boolean createMenu(ActorContext actor, MenuRequest request);
    boolean updateMenu(ActorContext actor, Long id, MenuRequest request);
    PageData<MenuVO> getMenuPage(ActorContext actor, Number pageNo, Number pageSize,
                                 String name, String code, String type, Integer status);
    boolean deleteMenu(ActorContext actor, Long id);
    boolean isMenuNameExists(ActorContext actor, String name, Long id);
    boolean isMenuPathExists(ActorContext actor, String path, Long id);
    void evictRouteMenuCache(Long userId);
    void evictAllRouteMenuCache();
}

