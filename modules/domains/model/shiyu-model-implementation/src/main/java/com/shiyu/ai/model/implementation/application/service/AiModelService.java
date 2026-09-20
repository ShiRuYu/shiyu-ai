package com.shiyu.ai.model.implementation.application.service;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.model.implementation.web.request.AiModelRequest;
import com.shiyu.ai.model.implementation.web.response.AiModelResponse;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 提供 AI 模型 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface AiModelService {
    /**
     * 按当前租户和可选平台分页查询 AI 模型。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param platformId 可选的平台 ID；为空时查询当前租户下所有平台的模型。
     * @param pageNo 页码，从 1 开始。
     * @param pageSize 每页返回的模型数量。
     *
     * @return 左值为模型总数，右值为当前页的模型响应列表。
     */
    Pair<Long, List<AiModelResponse>> pageResponse(
            ActorContext actor, Long platformId, Number pageNo, Number pageSize);

    /**
     * 查询指定平台下当前租户可访问的全部 AI 模型。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param platformId 平台 ID。
     *
     * @return 指定平台下的模型响应列表；没有匹配模型时返回空列表。
     */
    List<AiModelResponse> byPlatformResponse(ActorContext actor, Long platformId);

    /**
     * 根据平台编码查询当前租户可访问的 AI 模型。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param platformCode 平台唯一编码。
     *
     * @return 对应平台的模型响应列表；平台不存在时返回空列表。
     */
    List<AiModelResponse> byPlatformCodeResponse(ActorContext actor, String platformCode);

    /**
     * 查询指定 AI 模型的详细信息。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 模型 ID。
     *
     * @return 指定模型的响应数据。
     */
    AiModelResponse detailResponse(ActorContext actor, Long id);

    /**
     * 查询指定平台当前配置的默认 AI 模型。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param platformId 平台 ID。
     *
     * @return 指定平台的默认模型响应数据。
     */
    AiModelResponse defaultResponse(ActorContext actor, Long platformId);

    /**
     * 根据请求参数创建 AI 模型，并维护其所属平台的默认模型状态。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param request 模型创建请求，包含所属平台、名称和默认标志等信息。
     *
     * @return 创建后的模型响应数据。
     */
    AiModelResponse createResponse(ActorContext actor, AiModelRequest request);

    /**
     * 更新指定 AI 模型，并维护其所属平台的默认模型状态。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待更新的模型 ID。
     * @param request 模型更新请求，包含需要变更的模型属性。
     *
     * @return 更新后的模型响应数据。
     */
    AiModelResponse updateResponse(ActorContext actor, Long id, AiModelRequest request);

    /**
     * 删除指定 AI 模型及其关联配置。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待删除的模型 ID。
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * 批量删除指定 AI 模型及其关联配置。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param ids 待删除的模型 ID 集合。
     */
    void deleteByIds(ActorContext actor, List<Long> ids);

    /**
     * 查询指定平台下用于选择模型的 ID 和名称选项。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param platformId 可选的平台 ID；为空时查询当前租户下所有平台的模型选项。
     *
     * @return 模型 ID 与名称选项列表。
     */
    List<IdNameOptionVO> getOptions(ActorContext actor, Long platformId);

    /**
     * 将指定 AI 模型设置为所属平台的默认模型。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待设为默认模型的模型 ID。
     *
     * @return 更新后的默认模型响应数据。
     */
    AiModelResponse setDefaultResponse(ActorContext actor, Long id);
}
