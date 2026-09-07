package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.domain.model.KnowledgeRelationBO;

import java.util.List;

/** Tenant-scoped persistence port for knowledge graph relations. */
public interface KnowledgeRelationRepository {
    List<KnowledgeRelationBO> findBySourceId(TenantId tenantId, Long spaceId, Long sourceId);
    List<KnowledgeRelationBO> findByTargetId(TenantId tenantId, Long spaceId, Long targetId);
    List<KnowledgeRelationBO> findBySourceIdAndType(TenantId tenantId, Long spaceId, Long sourceId, String type);
    List<KnowledgeRelationBO> findByTargetIdAndType(TenantId tenantId, Long spaceId, Long targetId, String type);
    int insert(TenantId tenantId, KnowledgeRelationBO bo);
    int deleteBySourceAndTargetAndType(TenantId tenantId, Long spaceId, Long sourceId, Long targetId, String type);
    int deleteBySourceIdOrTargetId(TenantId tenantId, Long spaceId, Long knowledgeId);
    List<KnowledgeRelationBO> findBySpace(TenantId tenantId, Long spaceId);
    boolean exists(TenantId tenantId, Long spaceId, Long sourceId, Long targetId, String type);
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
