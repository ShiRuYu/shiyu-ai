package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocRelationBO;

import java.util.List;

/** Tenant-scoped persistence port for point/document relations. */
public interface KnowledgeDocRelationRepository {
    void insertBatch(TenantId tenantId, List<KnowledgeDocRelationBO> relations);
    void deleteByKnowledgeId(TenantId tenantId, Long spaceId, Long id);
    List<KnowledgeDocRelationBO> selectByDocId(TenantId tenantId, Long spaceId, Long id);
    List<KnowledgeDocRelationBO> selectByKnowledgeId(TenantId tenantId, Long spaceId, Long id);
    void deleteByDocId(TenantId tenantId, Long spaceId, Long id);
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
