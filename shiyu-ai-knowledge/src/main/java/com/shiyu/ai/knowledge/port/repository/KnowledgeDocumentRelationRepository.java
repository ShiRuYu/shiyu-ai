package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentRelationBO;
import java.util.List;

public interface KnowledgeDocumentRelationRepository {
    List<KnowledgeDocumentRelationBO> selectBySource(Long spaceId, Long documentId);
    List<KnowledgeDocumentRelationBO> selectByTarget(Long spaceId, Long documentId);
    void replace(Long tenantId, Long spaceId, Long sourceId, List<KnowledgeDocumentRelationBO> relations);
    void deleteByDocument(Long tenantId, Long documentId);
}
