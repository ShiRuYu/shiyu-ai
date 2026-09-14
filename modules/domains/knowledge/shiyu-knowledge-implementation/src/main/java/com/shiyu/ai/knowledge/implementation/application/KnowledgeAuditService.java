package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.web.response.KnowledgeAuditResponse;

/**
 * KnowledgeAuditService 服务接口，负责执行知识领域相关业务操作。
 */
public interface KnowledgeAuditService {

    /**
     * 执行 {@code record} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param spaceId 方法参数。
     * @param resourceType 方法参数。
     * @param resourceId 方法参数。
     * @param action 方法参数。
     * @param detail 方法参数。
     */
    void record(
            ActorContext actor,
            Long spaceId,
            String resourceType,
            Long resourceId,
            String action,
            Object detail);

    /**
     * 执行 {@code page} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param spaceId 方法参数。
     *
     * @return 操作结果。
     */
    PageData<KnowledgeAuditResponse> page(
            ActorContext actor, int pageNum, int pageSize, Long spaceId);
}
