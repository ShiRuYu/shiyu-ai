package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.web.response.KnowledgeAuditResponse;

public interface KnowledgeAuditService {

    void record(
            ActorContext actor,
            Long spaceId,
            String resourceType,
            Long resourceId,
            String action,
            Object detail);

    PageData<KnowledgeAuditResponse> page(
            ActorContext actor, int pageNum, int pageSize, Long spaceId);
}
