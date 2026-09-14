package com.shiyu.ai.knowledge.implementation.infrastructure.point;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import com.shiyu.ai.knowledge.implementation.web.response.KnowledgeGraphResponse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * KnowledgePointService 服务接口，负责执行知识领域相关业务操作。
 */
public interface KnowledgePointService {

    /**
     * 执行 {@code page} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param spaceId 方法参数。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param keyword 方法参数。
     * @param category 方法参数。
     *
     * @return 操作结果。
     */
    PageData<PointView> page(
            ActorContext actor,
            Long spaceId,
            int pageNum,
            int pageSize,
            String keyword,
            String category);

    /**
     * 根据标识查询对应的数据。
     *
     * @param actor 当前操作主体上下文。
     * @param pointId 方法参数。
     *
     * @return 操作结果。
     */
    PointView get(ActorContext actor, Long pointId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param pointId 方法参数。
     *
     * @return 操作结果。
     */
    KnowledgeResponse getResponse(ActorContext actor, Long pointId);

    /**
     * 执行 {@code graph} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param pointId 方法参数。
     *
     * @return 操作结果。
     */
    KnowledgeGraphResponse graph(ActorContext actor, Long pointId);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param spaceId 方法参数。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    PointView create(ActorContext actor, Long spaceId, CreatePointRequest request);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param pointId 方法参数。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    PointView update(ActorContext actor, Long pointId, UpdatePointRequest request);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param pointId 方法参数。
     */
    void delete(ActorContext actor, Long pointId);

    /**
     * {@code PointView} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param code 编码，表示该记录组件承载的数据。
     * @param name 名称，表示该记录组件承载的数据。
     * @param description 描述，表示该记录组件承载的数据。
     * @param difficultyLevel difficultyLevel 属性，表示该记录组件承载的数据。
     * @param category category 属性，表示该记录组件承载的数据。
     * @param tags tags 属性，表示该记录组件承载的数据。
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
     * {@code CreatePointRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param code 编码，表示该记录组件承载的数据。
     * @param name 名称，表示该记录组件承载的数据。
     * @param description 描述，表示该记录组件承载的数据。
     * @param difficultyLevel difficultyLevel 属性，表示该记录组件承载的数据。
     * @param category category 属性，表示该记录组件承载的数据。
     * @param tags tags 属性，表示该记录组件承载的数据。
     */
    record CreatePointRequest(
            @NotBlank String code,
            @NotBlank String name,
            String description,
            @Min(1) @Max(100) Integer difficultyLevel,
            String category,
            String tags) {}

    /**
     * {@code UpdatePointRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param name 名称，表示该记录组件承载的数据。
     * @param description 描述，表示该记录组件承载的数据。
     * @param difficultyLevel difficultyLevel 属性，表示该记录组件承载的数据。
     * @param category category 属性，表示该记录组件承载的数据。
     * @param tags tags 属性，表示该记录组件承载的数据。
     */
    record UpdatePointRequest(
            String name,
            String description,
            @Min(1) @Max(100) Integer difficultyLevel,
            String category,
            String tags) {}
}
