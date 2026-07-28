package com.shiyu.ai.auth.service;

import com.shiyu.ai.dal.auth.bo.MenuBO;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 获取角色权限树 by token
     *
     * @return 权限树
     */
    /**
     * Get Menu Permissions Tree
     * @return 处理结果
     */
    List<MenuBO> getMenuPermissionsTree();

    /**
     * 获取权限树 - 菜单
     *
     * @return 权限树
     */
    /**
     * Get Menu Tree
     * @return 处理结果
     */
    List<MenuBO> getMenuTree();

    /**
     * 获取权限树 all
     *
     * @return 权限树
     */
    /**
     * Get All Tree
     * @return 处理结果
     */
    List<MenuBO> getAllTree();

    /**
     * 删除菜单
     *
     * @param id 菜单 ID
     * @return 是否成功
     */
    /**
     * Delete Menu
     * @return 处理结果
     */
    boolean deleteMenu(Long id);

    /**
     * 新增菜单
     *
     * @param menuBO 菜单信息
     * @return 是否成功
     */
    /**
     * Create Menu
     * @param MenuBO MenuBO
     * @return 处理结果
     */
    boolean createMenu(MenuBO menuBO);

    /**
     * 修改菜单
     *
     * @param id      菜单 ID
     * @param menuBO  菜单信息
     * @return 是否成功
     */
    /**
     * Update Menu
     * @param MenuBO MenuBO
     * @return 处理结果
     */
    boolean updateMenu(Long id, MenuBO menuBO);

    /**
     * 根据用户 ID 获取菜单树
     *
     * @param userId 用户 ID
     * @return 菜单树
     */
    /**
     * Get Menu Tree By User Id
     * @return 处理结果
     */
    List<MenuBO> getMenuTreeByUserId(Long userId);
    
    /**
     * 根据用户 ID 和类型获取菜单列表
     *
     * @param userId 用户 ID
     * @param type 菜单类型（MENU-菜单，CATALOG-目录）
     * @return 菜单列表
     */
    /**
     * Get Menus By User Id And Type
     * @return 处理结果
     */
    List<MenuBO> getMenusByUserIdAndType(Long userId, String type);

    /**
     * 检查菜单名称是否已存在
     *
     * @param name 菜单名称
     * @param id   菜单 ID（编辑时排除自身）
     * @return true 表示已存在
     */
    /**
     * Is Menu Name Exists
     * @return 处理结果
     */
    boolean isMenuNameExists(String name, Long id);

    /**
     * 检查菜单路径是否已存在
     *
     * @param path 菜单路径
     * @param id   菜单 ID（编辑时排除自身）
     * @return true 表示已存在
     */
    /**
     * Is Menu Path Exists
     * @return 处理结果
     */
    boolean isMenuPathExists(String path, Long id);

    /**
     * 获取根节点菜单（parentId 为 null，用于懒加载初始加载）
     *
     * @return 根菜单列表（平铺）
     */
    /**
     * Get Menu Roots
     * @return 处理结果
     */
    List<MenuBO> getMenuRoots();

    /**
     * 获取指定父菜单的子菜单（用于懒加载展开）
     *
     * @param parentId 父菜单 ID
     * @return 子菜单列表（平铺）
     */
    /**
     * Get Children By Parent Id
     * @return 处理结果
     */
    List<MenuBO> getChildrenByParentId(Long parentId);

    /**
     * 获取用户路由菜单（CATALOG + MENU）
     * 用于前端动态路由生成
     *
     * @param userId 用户 ID
     * @return 路由菜单树
     */
    /**
     * Get Route Menus By User Id
     * @return 处理结果
     */
    List<MenuBO> getRouteMenusByUserId(Long userId);

    /** 清除指定用户路由菜单缓存 */
    void evictRouteMenuCache(Long userId);

    /** 清除全部路由菜单缓存 */
    void evictAllRouteMenuCache();
}
