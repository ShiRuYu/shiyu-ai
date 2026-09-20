package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.iam.implementation.request.RoleRequest;
import com.shiyu.ai.iam.implementation.vo.RoleVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 提供 角色 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface RoleService {
    /**
     * 执行 角色 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param status 用于完成本次业务处理的 status 参数。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<RoleVO> allRolesView(ActorContext actor, String status, TenantId tenantId);

    /**
     * 查询 角色 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 角色 相关操作生成的结果数据。
     */
    RoleVO detailView(ActorContext actor, Long id, TenantId tenantId);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 条件是否满足。
     */
    boolean createRole(ActorContext actor, RoleRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param request 请求参数。
     *
     * @return 条件是否满足。
     */
    boolean updateRole(ActorContext actor, Long id, RoleRequest request);

    /**
     * 查询 角色 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @return 返回 角色 相关操作生成的结果数据。
     */
    PageData<RoleVO> getRoleList(ActorContext actor, Number pageNum, Number pageSize, String name);

    /**
     * 执行 角色 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param menuIds 待处理的业务对象标识集合。
     * @return 返回本次条件判断是否成立。
     */
    boolean replaceRoleMenus(ActorContext actor, Long id, TenantId tenantId, List<Long> menuIds);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 条件是否满足。
     */
    boolean deleteRole(ActorContext actor, Long id);

    /**
     * 删除或移除 角色 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param userIds 待处理的业务对象标识集合。
     * @return 返回本次条件判断是否成立。
     */
    boolean removeUserRoles(ActorContext actor, Long id, TenantId tenantId, List<Long> userIds);

    /**
     * 更新或设置 角色 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param userIds 待处理的业务对象标识集合。
     * @return 返回本次条件判断是否成立。
     */
    boolean assignUserRoles(ActorContext actor, Long id, TenantId tenantId, List<Long> userIds);
}
