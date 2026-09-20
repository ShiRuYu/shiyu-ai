package com.shiyu.ai.agent.implementation.service;

import com.shiyu.ai.agent.implementation.request.IntentDefRequest;
import com.shiyu.ai.agent.implementation.vo.IntentDefVO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 提供 Intent Def 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface IntentDefService {
    /**
     * 查询 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @param agentId 用于定位agent的标识。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param category 用于完成本次业务处理的 category 参数。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
     */
    Pair<Long, List<IntentDefVO>> pageView(
            ActorContext actor,
            Number pageNo,
            Number pageSize,
            String agentId,
            String name,
            String code,
            String category);

    /**
     * 查询 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 Intent Def 相关操作生成的结果数据。
     */
    IntentDefVO detailView(ActorContext actor, Long id);

    /**
     * 创建或保存 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 Intent Def 相关操作生成的结果数据。
     */
    IntentDefVO create(ActorContext actor, IntentDefRequest request);

    /**
     * 更新或设置 Intent Def 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 Intent Def 相关操作生成的结果数据。
     */
    IntentDefVO update(ActorContext actor, Long id, IntentDefRequest request);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param ids 目标对象标识集合。
     */
    void deleteByIds(ActorContext actor, List<Long> ids);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 符合条件的结果集合。
     */
    List<IdNameOptionVO> listAllOptions(ActorContext actor);
}
