package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.request.MenuRequest;
import com.shiyu.ai.auth.vo.MenuVO;
import com.shiyu.ai.auth.vo.RouteMenuVO;
import com.shiyu.ai.common.core.api.PageData;

import java.util.List;

/** Menu application contract. */
public interface MenuService {
    List<RouteMenuVO> routeMenusView(Long userId);
    List<MenuVO> allTreeView();
    List<RouteMenuVO> menuRootsView();
    List<RouteMenuVO> childrenView(Long parentId);
    List<RouteMenuVO> permissionsView();
    List<RouteMenuVO> treeView();
    boolean createMenu(MenuRequest request);
    boolean updateMenu(Long id, MenuRequest request);
    PageData<MenuVO> getMenuPage(Number pageNo, Number pageSize,
                                 String name, String code, String type, Integer status);
    boolean deleteMenu(Long id);
    boolean isMenuNameExists(String name, Long id);
    boolean isMenuPathExists(String path, Long id);
    void evictRouteMenuCache(Long userId);
    void evictAllRouteMenuCache();
}
