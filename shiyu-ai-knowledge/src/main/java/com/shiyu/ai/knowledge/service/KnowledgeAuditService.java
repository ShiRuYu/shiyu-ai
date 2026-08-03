package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.api.response.KnowledgeAuditResponse;

public interface KnowledgeAuditService {

    void record(Long spaceId, String resourceType, Long resourceId,
                String action, Object detail);

    PageData<KnowledgeAuditResponse> page(int pageNum, int pageSize, Long spaceId);
}
