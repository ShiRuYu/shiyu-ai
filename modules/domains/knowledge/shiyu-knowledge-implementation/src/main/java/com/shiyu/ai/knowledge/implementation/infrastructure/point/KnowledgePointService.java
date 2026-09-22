package com.shiyu.ai.knowledge.implementation.infrastructure.point;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import com.shiyu.ai.knowledge.implementation.web.response.KnowledgeGraphResponse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 提供 知识 Point 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface KnowledgePointService {

    /**
     * 查询 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param category 用于完成本次业务处理的 category 参数。
     * @return 返回 知识 Point 相关操作生成的结果数据。
     */
    PageData<PointView> page(
            ActorContext actor,
            Long spaceId,
            int pageNum,
            int pageSize,
            String keyword,
            String category);

    /**
     * 查询 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pointId 用于定位point的标识。
     * @return 返回 知识 Point 相关操作生成的结果数据。
     */
    PointView get(ActorContext actor, Long pointId);

    /**
     * 查询 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pointId 用于定位point的标识。
     * @return 返回 知识 Point 相关操作生成的结果数据。
     */
    KnowledgeResponse getResponse(ActorContext actor, Long pointId);

    /**
     * 执行 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pointId 用于定位point的标识。
     * @return 返回 知识 Point 相关操作生成的结果数据。
     */
    KnowledgeGraphResponse graph(ActorContext actor, Long pointId);

    /**
     * 创建或保存 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 知识 Point 相关操作生成的结果数据。
     */
    PointView create(ActorContext actor, Long spaceId, CreatePointRequest request);

    /**
     * 更新或设置 知识 Point 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pointId 用于定位point的标识。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 知识 Point 相关操作生成的结果数据。
     */
    PointView update(ActorContext actor, Long pointId, UpdatePointRequest request);

    /**
     * 删除或移除 知识 Point 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pointId 用于定位point的标识。
     */
    void delete(ActorContext actor, Long pointId);

    /**
     * 封装 Point View 相关的不可变数据及其字段约束。
     */
    record PointView(
            Long id,
            Long spaceId,
            String code,
            String name,
            String description,
            Integer difficultyLevel,
            String category,
            String tags) {}

    /**
     * 封装 Create Point 相关的不可变数据及其字段约束。
     */
    record CreatePointRequest(
            @NotBlank String code,
            @NotBlank String name,
            String description,
            @Min(1) @Max(100) Integer difficultyLevel,
            String category,
            String tags) {}

    /**
     * 封装 Update Point 相关的不可变数据及其字段约束。
     */
    record UpdatePointRequest(
            String name,
            String description,
            @Min(1) @Max(100) Integer difficultyLevel,
            String category,
            String tags) {}
}
