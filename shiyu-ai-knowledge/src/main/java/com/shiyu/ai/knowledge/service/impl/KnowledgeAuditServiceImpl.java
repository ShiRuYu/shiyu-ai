package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeAuditLogDO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.service.KnowledgeAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgeAuditServiceImpl implements KnowledgeAuditService {

    private final KnowledgeEnterpriseRepository repository;

    @Override
    public void record(Long spaceId, String resourceType, Long resourceId,
                       String action, Object detail) {
        KnowledgeAuditLogDO audit = new KnowledgeAuditLogDO();
        audit.setSpaceId(spaceId);
        audit.setResourceType(resourceType);
        audit.setResourceId(resourceId);
        audit.setAction(action);
        audit.setDetailJson(detail == null ? null : JSONUtils.toJsonString(detail));
        audit.setStatus(1);
        audit.setDelFlag(0);
        repository.insertAudit(audit);
    }

    @Override
    public PageData<KnowledgeAuditLogDO> page(int pageNum, int pageSize, Long spaceId) {
        return repository.pageAudit(pageNum, pageSize, spaceId);
    }
}
