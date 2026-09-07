package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.api.response.KnowledgeAuditResponse;
import com.shiyu.ai.kernel.context.ActorContext;

public interface KnowledgeAuditService {

    void record(ActorContext actor, Long spaceId, String resourceType, Long resourceId,
                String action, Object detail);

    PageData<KnowledgeAuditResponse> page(ActorContext actor, int pageNum, int pageSize, Long spaceId);
}
