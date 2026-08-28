package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.knowledge.domain.model.KnowledgeChunkBO;
import java.util.List;
import com.shiyu.ai.kernel.context.TenantId;

public interface KnowledgeChunkRepository {
    void insert(TenantId tenantId, KnowledgeChunkBO bo);
    KnowledgeChunkBO getById(TenantId tenantId, Long id);
    void deleteByDocumentId(TenantId tenantId, Long documentId);
    List<KnowledgeChunkBO> findBySpace(TenantId tenantId, Long spaceId);
    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
