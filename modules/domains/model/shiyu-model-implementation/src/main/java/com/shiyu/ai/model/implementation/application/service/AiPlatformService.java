package com.shiyu.ai.model.implementation.application.service;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.model.implementation.web.request.AiPlatformRequest;
import com.shiyu.ai.model.implementation.web.response.AiPlatformResponse;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * AiPlatformService 服务接口，负责执行模型领域相关业务操作。
 */
public interface AiPlatformService {
    /**
     * 执行 {@code pageResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     * @param name 对象名称。
     * @param code 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    Pair<Long, List<AiPlatformResponse>> pageResponse(
            ActorContext actor, Number pageNo, Number pageSize, String name, String code);

    /**
     * 执行 {@code enabledResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 符合条件的结果集合。
     */
    List<AiPlatformResponse> enabledResponse(ActorContext actor);

    /**
     * 执行 {@code detailResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    AiPlatformResponse detailResponse(ActorContext actor, Long id);

    /**
     * 执行 {@code codeResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param code 方法参数。
     *
     * @return 操作结果。
     */
    AiPlatformResponse codeResponse(ActorContext actor, String code);

    /**
     * 执行 {@code defaultResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 操作结果。
     */
    AiPlatformResponse defaultResponse(ActorContext actor);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    AiPlatformResponse createResponse(ActorContext actor, AiPlatformRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    AiPlatformResponse updateResponse(ActorContext actor, Long id, AiPlatformRequest request);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     *
     * @return 符合条件的结果集合。
     */
    List<IdNameOptionVO> getOptions(ActorContext actor);

    /**
     * 执行 {@code setDefaultResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    AiPlatformResponse setDefaultResponse(ActorContext actor, Long id);
}
