package com.shiyu.ai.model.implementation.application.service;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.model.implementation.web.request.AiModelRequest;
import com.shiyu.ai.model.implementation.web.response.AiModelResponse;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * AiModelService 服务接口，负责执行模型领域相关业务操作。
 */
public interface AiModelService {
    /**
     * 执行 {@code pageResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param platformId 方法参数。
     * @param pageNo 方法参数。
     * @param pageSize 分页大小。
     *
     * @return 符合条件的结果集合。
     */
    Pair<Long, List<AiModelResponse>> pageResponse(
            ActorContext actor, Long platformId, Number pageNo, Number pageSize);

    /**
     * 执行 {@code byPlatformResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param platformId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<AiModelResponse> byPlatformResponse(ActorContext actor, Long platformId);

    /**
     * 执行 {@code byPlatformCodeResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param platformCode 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<AiModelResponse> byPlatformCodeResponse(ActorContext actor, String platformCode);

    /**
     * 执行 {@code detailResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    AiModelResponse detailResponse(ActorContext actor, Long id);

    /**
     * 执行 {@code defaultResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param platformId 方法参数。
     *
     * @return 操作结果。
     */
    AiModelResponse defaultResponse(ActorContext actor, Long platformId);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    AiModelResponse createResponse(ActorContext actor, AiModelRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    AiModelResponse updateResponse(ActorContext actor, Long id, AiModelRequest request);

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
     * @param platformId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<IdNameOptionVO> getOptions(ActorContext actor, Long platformId);

    /**
     * 执行 {@code setDefaultResponse} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    AiModelResponse setDefaultResponse(ActorContext actor, Long id);
}
