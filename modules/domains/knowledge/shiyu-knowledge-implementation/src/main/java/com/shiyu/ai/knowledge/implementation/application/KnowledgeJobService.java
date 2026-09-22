package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;

import java.time.LocalDateTime;

/**
 * 提供 知识 Job 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface KnowledgeJobService {

    /**
     * 查询 知识 Job 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param spaceId 用于定位space的标识。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回 知识 Job 相关操作生成的结果数据。
     */
    PageData<JobView> page(
            ActorContext actor, int pageNum, int pageSize, Long spaceId, String status);

    /**
     * 查询 知识 Job 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 Job 相关操作生成的结果数据。
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
     * 执行 知识 Job 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param id 用于定位目标业务对象的标识。
     */
    void retry(ActorContext actor, Long id);

    /**
     * 封装 Job View 相关的不可变数据及其字段约束。
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
