package com.shiyu.ai.agent.implementation.service;

import com.shiyu.ai.agent.implementation.request.IntentDefRequest;
import com.shiyu.ai.agent.implementation.vo.IntentDefVO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * IntentDefService 服务接口，负责执行智能体领域相关业务操作。
 */
public interface IntentDefService {
    /**
     * 执行 {@code pageView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     * @param agentId 方法参数。
     * @param name 对象名称。
     * @param code 方法参数。
     * @param category 方法参数。
     *
     * @return 符合条件的结果集合。
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
     * 执行 {@code detailView} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    IntentDefVO detailView(ActorContext actor, Long id);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    IntentDefVO create(ActorContext actor, IntentDefRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param request 请求参数。
     *
     * @return 操作结果。
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
