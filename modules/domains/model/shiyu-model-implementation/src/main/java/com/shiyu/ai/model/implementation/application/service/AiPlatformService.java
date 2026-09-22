package com.shiyu.ai.model.implementation.application.service;

import com.shiyu.ai.common.foundation.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.model.implementation.web.request.AiPlatformRequest;
import com.shiyu.ai.model.implementation.web.response.AiPlatformResponse;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 定义 AI 平台应用服务契约，提供当前租户下平台的分页、详情、启用状态、创建、更新、删除、选项查询和默认平台设置。
 */
public interface AiPlatformService {
    /**
     * 按名称和编码条件分页查询当前租户的 AI 平台。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param pageNo 页码，从 1 开始。
     * @param pageSize 每页返回的平台数量。
     * @param name 可选的平台名称关键字。
     * @param code 可选的平台唯一编码。
     *
     * @return 左值为平台总数，右值为当前页的平台响应列表。
     */
    Pair<Long, List<AiPlatformResponse>> pageResponse(
            ActorContext actor, Number pageNo, Number pageSize, String name, String code);

    /**
     * 查询当前租户下已启用的全部 AI 平台。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     *
     * @return 已启用的平台响应列表。
     */
    List<AiPlatformResponse> enabledResponse(ActorContext actor);

    /**
     * 查询指定 AI 平台的详细信息。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 平台 ID。
     *
     * @return 指定平台的响应数据。
     */
    AiPlatformResponse detailResponse(ActorContext actor, Long id);

    /**
     * 根据平台编码查询 AI 平台详情。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param code 平台唯一编码。
     *
     * @return 对应平台的响应数据。
     */
    AiPlatformResponse codeResponse(ActorContext actor, String code);

    /**
     * 查询当前租户配置的默认 AI 平台。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     *
     * @return 默认平台的响应数据。
     */
    AiPlatformResponse defaultResponse(ActorContext actor);

    /**
     * 根据请求参数创建 AI 平台，并维护租户默认平台状态。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param request 平台创建请求，包含平台名称、编码、适配器和默认标志等信息。
     *
     * @return 创建后的平台响应数据。
     */
    AiPlatformResponse createResponse(ActorContext actor, AiPlatformRequest request);

    /**
     * 更新指定 AI 平台，并维护租户默认平台状态。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待更新的平台 ID。
     * @param request 平台更新请求，包含需要变更的平台属性。
     *
     * @return 更新后的平台响应数据。
     */
    AiPlatformResponse updateResponse(ActorContext actor, Long id, AiPlatformRequest request);

    /**
     * 删除指定 AI 平台及其关联配置。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待删除的平台 ID。
     */
    void deleteById(ActorContext actor, Long id);

    /**
     * 查询当前租户下用于选择平台的 ID 和名称选项。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     *
     * @return 平台 ID 与名称选项列表。
     */
    List<IdNameOptionVO> getOptions(ActorContext actor);

    /**
     * 将指定 AI 平台设置为当前租户的默认平台。
     *
     * @param actor 当前操作主体上下文，用于确定租户和访问权限。
     * @param id 待设为默认平台的平台 ID。
     *
     * @return 更新后的默认平台响应数据。
     */
    AiPlatformResponse setDefaultResponse(ActorContext actor, Long id);
}
