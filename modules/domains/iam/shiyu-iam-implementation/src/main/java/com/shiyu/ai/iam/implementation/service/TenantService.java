package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.iam.implementation.request.TenantRequest;
import com.shiyu.ai.iam.implementation.vo.TenantVO;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * 提供 租户 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface TenantService {
    /**
     * 执行 租户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    java.util.List<TenantVO> allTenantsView(ActorContext actor);

    /**
     * 查询 租户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 租户 相关操作生成的结果数据。
     */
    TenantVO detailView(ActorContext actor, Long id);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 条件是否满足。
     */
    boolean createTenant(ActorContext actor, TenantRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param request 请求参数。
     *
     * @return 条件是否满足。
     */
    boolean updateTenant(ActorContext actor, Long id, TenantRequest request);

    /**
     * 查询 租户 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回 租户 相关操作生成的结果数据。
     */
    PageData<TenantVO> getTenantPage(
            ActorContext actor,
            Number pageNo,
            Number pageSize,
            String name,
            String code,
            Integer status);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 条件是否满足。
     */
    boolean deleteTenant(ActorContext actor, Long id);
}
