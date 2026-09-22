package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.web.response.KnowledgeAuditResponse;

/**
 * 提供 知识 Audit 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface KnowledgeAuditService {

    /**
     * 执行 知识 Audit 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @param resourceType 用于完成本次业务处理的 resourceType 参数。
     * @param resourceId 用于定位resource的标识。
     * @param action 用于完成本次业务处理的 action 参数。
     * @param detail 用于完成本次业务处理的 detail 参数。
     */
    void record(
            ActorContext actor,
            Long spaceId,
            String resourceType,
            Long resourceId,
            String action,
            Object detail);

    /**
     * 查询 知识 Audit 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param spaceId 用于定位space的标识。
     * @return 返回 知识 Audit 相关操作生成的结果数据。
     */
    PageData<KnowledgeAuditResponse> page(
            ActorContext actor, int pageNum, int pageSize, Long spaceId);
}
