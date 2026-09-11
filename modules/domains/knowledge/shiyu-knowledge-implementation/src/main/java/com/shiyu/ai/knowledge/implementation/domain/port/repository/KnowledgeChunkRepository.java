package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeChunkBO;

import java.util.List;

public interface KnowledgeChunkRepository {
    void insert(TenantId tenantId, KnowledgeChunkBO bo);

    KnowledgeChunkBO getById(TenantId tenantId, Long id);

    void deleteByDocumentId(TenantId tenantId, Long documentId);

    List<KnowledgeChunkBO> findBySpace(TenantId tenantId, Long spaceId);

    void assignDefaultSpace(TenantId tenantId, Long spaceId);
}
