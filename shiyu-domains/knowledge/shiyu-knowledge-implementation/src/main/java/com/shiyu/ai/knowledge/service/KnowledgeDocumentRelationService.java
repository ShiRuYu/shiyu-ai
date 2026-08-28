package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.kernel.context.ActorContext;
import java.util.List;

/**
 * 知识点与企业文档的统一关系服务。
 */
public interface KnowledgeDocumentRelationService {

    List<DocumentSummary> listDocuments(ActorContext actor, Long pointId);

    default void replaceDocuments(ActorContext actor, Long pointId, List<Long> documentIds) {
        replaceDocuments(actor, pointId, documentIds, "RELATED");
    }

    void replaceDocuments(ActorContext actor, Long pointId, List<Long> documentIds, String relationType);

    List<Long> listPointIds(ActorContext actor, Long documentId);

    default void replacePoints(ActorContext actor, Long documentId, List<Long> pointIds) {
        replacePoints(actor, documentId, pointIds, "RELATED");
    }

    void replacePoints(ActorContext actor, Long documentId, List<Long> pointIds, String relationType);

    void removeDocumentRelations(ActorContext actor, Long documentId);

    List<DocumentRelationView> listDocumentRelations(ActorContext actor, Long documentId);

    void replaceDocumentRelations(ActorContext actor, Long documentId, List<DocumentRelationRequest> relations);

    record DocumentRelationRequest(Long documentId, String relationType) {
    }

    record DocumentRelationView(Long id, Long sourceDocumentId, Long targetDocumentId,
                                String relationType, String targetTitle) {
    }

    record DocumentSummary(Long id, Long spaceId, String title, String docType,
                           String lifecycleStatus, String parseStatus) {
    }
}
