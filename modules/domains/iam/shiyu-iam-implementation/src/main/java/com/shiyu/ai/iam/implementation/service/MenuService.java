package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.iam.implementation.request.MenuRequest;
import com.shiyu.ai.iam.implementation.vo.MenuVO;
import com.shiyu.ai.iam.implementation.vo.RouteMenuVO;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * MenuService 服务接口，负责执行身份与访问领域相关业务操作。
 */
public interface MenuService {
    /**
     * 执行 {@code routeMenusView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 符合条件的结果集合。
     */
    List<RouteMenuVO> routeMenusView(ActorContext actor);

    /**
     * 执行 {@code allTreeView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 符合条件的结果集合。
     */
    List<MenuVO> allTreeView(ActorContext actor);

    /**
     * 执行 {@code menuRootsView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 符合条件的结果集合。
     */
    List<RouteMenuVO> menuRootsView(ActorContext actor);

    /**
     * 执行 {@code childrenView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param parentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<RouteMenuVO> childrenView(ActorContext actor, Long parentId);

    /**
     * 执行 {@code permissionsView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 符合条件的结果集合。
     */
    List<RouteMenuVO> permissionsView(ActorContext actor);

    /**
     * 执行 {@code treeView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 符合条件的结果集合。
     */
    List<RouteMenuVO> treeView(ActorContext actor);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 条件是否满足。
     */
    boolean createMenu(ActorContext actor, MenuRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param request 请求参数。
     *
     * @return 条件是否满足。
     */
    boolean updateMenu(ActorContext actor, Long id, MenuRequest request);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     * @param name 对象名称。
     * @param code 方法参数。
     * @param type 对象类型。
     * @param status 对象状态。
     *
     * @return 操作结果。
     */
    PageData<MenuVO> getMenuPage(
            ActorContext actor,
            Number pageNo,
            Number pageSize,
            String name,
            String code,
            String type,
            Integer status);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 条件是否满足。
     */
    boolean deleteMenu(ActorContext actor, Long id);

    /**
     * 判断当前条件是否满足。
     *
     * @param actor 当前操作主体上下文。
     * @param name 对象名称。
     * @param id 目标对象标识。
     *
     * @return 条件是否满足。
     */
    boolean isMenuNameExists(ActorContext actor, String name, Long id);

    /**
     * 判断当前条件是否满足。
     *
     * @param actor 当前操作主体上下文。
     * @param path 方法参数。
     * @param id 目标对象标识。
     *
     * @return 条件是否满足。
     */
    boolean isMenuPathExists(ActorContext actor, String path, Long id);

    /**
     * 执行 {@code evictRouteMenuCache} 定义的接口操作。
     *
     * @param userId 用户标识。
     */
    void evictRouteMenuCache(Long userId);

    /**
     * 执行 {@code evictAllRouteMenuCache} 定义的接口操作。
     */
    void evictAllRouteMenuCache();
}
