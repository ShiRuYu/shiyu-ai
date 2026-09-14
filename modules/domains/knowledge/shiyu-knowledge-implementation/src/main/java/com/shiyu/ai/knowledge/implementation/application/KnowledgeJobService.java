package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;

import java.time.LocalDateTime;

/**
 * KnowledgeJobService 服务接口，负责执行知识领域相关业务操作。
 */
public interface KnowledgeJobService {

    /**
     * 执行 {@code page} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param spaceId 方法参数。
     * @param status 对象状态。
     *
     * @return 操作结果。
     */
    PageData<JobView> page(
            ActorContext actor, int pageNum, int pageSize, Long spaceId, String status);

    /**
     * 根据标识查询对应的数据。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    JobView get(ActorContext actor, Long id);

    /**
     * 判断当前条件是否满足。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     */
    void cancel(ActorContext actor, Long id);

    /**
     * 执行 {@code retry} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param id 目标对象标识。
     */
    void retry(ActorContext actor, Long id);

    /**
     * {@code JobView} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param jobKey jobKey 属性，表示该记录组件承载的数据。
     * @param jobType jobType 属性，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param documentId documentId 属性，表示该记录组件承载的数据。
     * @param versionId versionId 属性，表示该记录组件承载的数据。
     * @param status 状态，表示该记录组件承载的数据。
     * @param stage stage 属性，表示该记录组件承载的数据。
     * @param progress progress 属性，表示该记录组件承载的数据。
     * @param attempts attempts 属性，表示该记录组件承载的数据。
     * @param maxAttempts maxAttempts 属性，表示该记录组件承载的数据。
     * @param errorMessage errorMessage 属性，表示该记录组件承载的数据。
     * @param heartbeatTime heartbeatTime 属性，表示该记录组件承载的数据。
     * @param startedTime startedTime 属性，表示该记录组件承载的数据。
     * @param finishedTime finishedTime 属性，表示该记录组件承载的数据。
     * @param createTime createTime 属性，表示该记录组件承载的数据。
     */
    record JobView(
            Long id,
            String jobKey,
            String jobType,
            Long spaceId,
            Long documentId,
            Long versionId,
            String status,
            String stage,
            Integer progress,
            Integer attempts,
            Integer maxAttempts,
            String errorMessage,
            LocalDateTime heartbeatTime,
            LocalDateTime startedTime,
            LocalDateTime finishedTime,
            LocalDateTime createTime) {}
}
