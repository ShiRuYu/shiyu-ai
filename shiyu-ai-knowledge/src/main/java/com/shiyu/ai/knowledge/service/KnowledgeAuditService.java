package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeAuditLogDO;

public interface KnowledgeAuditService {

    void record(Long spaceId, String resourceType, Long resourceId,
                String action, Object detail);

    PageData<KnowledgeAuditLogDO> page(int pageNum, int pageSize, Long spaceId);
}
