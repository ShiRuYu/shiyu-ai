package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.api.response.KnowledgeAuditResponse;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.knowledge.domain.model.KnowledgeAuditLogBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.service.KnowledgeAuditService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgeAuditServiceImpl implements KnowledgeAuditService {

    private final KnowledgeEnterpriseRepository repository;

    @Override
    public void record(ActorContext actor, Long spaceId, String resourceType, Long resourceId,
                       String action, Object detail) {
        requireActor(actor);
        KnowledgeAuditLogBO audit = new KnowledgeAuditLogBO();
        audit.setTenantId(actor.tenantId().value());
        audit.setSpaceId(spaceId);
        audit.setResourceType(resourceType);
        audit.setResourceId(resourceId);
        audit.setAction(action);
        audit.setDetailJson(detail == null ? null : JSONUtils.toJsonString(detail));
        audit.setStatus(1);
        audit.setDelFlag(0);
        repository.insertAudit(actor.tenantId(), audit);
    }

    @Override
    public PageData<KnowledgeAuditResponse> page(ActorContext actor, int pageNum, int pageSize, Long spaceId) {
        requireActor(actor);
        PageData<KnowledgeAuditLogBO> data = repository.pageAudit(actor.tenantId(), pageNum, pageSize, spaceId);
        return new PageData<>(MapstructUtils.convert(data.getItems(), KnowledgeAuditResponse.class), data.getTotal());
    }

    private void requireActor(ActorContext actor) {
        if (actor == null || actor.tenantId() == null || actor.userId() == null) {
            throw new ServiceException("当前租户或用户上下文不存在");
        }
    }
}
