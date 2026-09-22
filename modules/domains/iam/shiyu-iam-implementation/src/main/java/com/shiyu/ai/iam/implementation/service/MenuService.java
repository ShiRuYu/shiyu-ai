package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.iam.implementation.request.MenuRequest;
import com.shiyu.ai.iam.implementation.vo.MenuVO;
import com.shiyu.ai.iam.implementation.vo.RouteMenuVO;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 Menu 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface MenuService {
    /**
     * 解析或路由 Menu 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<RouteMenuVO> routeMenusView(ActorContext actor);

    /**
     * 执行 Menu 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<MenuVO> allTreeView(ActorContext actor);

    /**
     * 执行 Menu 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<RouteMenuVO> menuRootsView(ActorContext actor);

    /**
     * 执行 Menu 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param parentId 用于定位parent的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<RouteMenuVO> childrenView(ActorContext actor, Long parentId);

    /**
     * 执行 Menu 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<RouteMenuVO> permissionsView(ActorContext actor);

    /**
     * 执行 Menu 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 Menu 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param type 用于完成本次业务处理的 type 参数。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回 Menu 相关操作生成的结果数据。
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
     * 校验或判断 Menu 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param path 用于完成本次业务处理的 path 参数。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean isMenuPathExists(ActorContext actor, String path, Long id);

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param userId 当前操作涉及的用户标识。
     */
    void evictRouteMenuCache(Long userId);

    /**
     * 执行 Menu 相关业务操作，并维护必要的状态和协作关系。
     */
    void evictAllRouteMenuCache();
}
