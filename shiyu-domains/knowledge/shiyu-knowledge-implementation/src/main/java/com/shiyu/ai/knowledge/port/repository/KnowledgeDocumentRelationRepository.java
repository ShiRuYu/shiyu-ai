package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentRelationBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface KnowledgeDocumentRelationRepository {
    List<KnowledgeDocumentRelationBO> selectBySource(TenantId tenantId, Long spaceId, Long documentId);
    List<KnowledgeDocumentRelationBO> selectByTarget(TenantId tenantId, Long spaceId, Long documentId);
    void replace(TenantId tenantId, Long spaceId, Long sourceId, List<KnowledgeDocumentRelationBO> relations);
    void deleteByDocument(TenantId tenantId, Long documentId);
}
