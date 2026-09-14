package com.shiyu.ai.iam.implementation.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.iam.implementation.request.TenantRequest;
import com.shiyu.ai.iam.implementation.vo.TenantVO;
import com.shiyu.ai.kernel.context.ActorContext;

/**
 * TenantService 服务接口，负责执行身份与访问领域相关业务操作。
 */
public interface TenantService {
    /**
     * 执行 {@code allTenantsView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 符合条件的结果集合。
     */
    java.util.List<TenantVO> allTenantsView(ActorContext actor);

    /**
     * 执行 {@code detailView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
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
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     * @param name 对象名称。
     * @param code 方法参数。
     * @param status 对象状态。
     *
     * @return 操作结果。
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
