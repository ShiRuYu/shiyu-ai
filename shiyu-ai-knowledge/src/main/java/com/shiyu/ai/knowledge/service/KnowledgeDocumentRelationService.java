package com.shiyu.ai.knowledge.service;

import java.util.List;

/**
 * 知识点与企业文档的统一关系服务。
 */
public interface KnowledgeDocumentRelationService {

    List<DocumentSummary> listDocuments(Long pointId);

    default void replaceDocuments(Long pointId, List<Long> documentIds) {
        replaceDocuments(pointId, documentIds, "RELATED");
    }

    void replaceDocuments(Long pointId, List<Long> documentIds, String relationType);

    List<Long> listPointIds(Long documentId);

    default void replacePoints(Long documentId, List<Long> pointIds) {
        replacePoints(documentId, pointIds, "RELATED");
    }

    void replacePoints(Long documentId, List<Long> pointIds, String relationType);

    void removeDocumentRelations(Long documentId);

    List<DocumentRelationView> listDocumentRelations(Long documentId);

    void replaceDocumentRelations(Long documentId, List<DocumentRelationRequest> relations);

    record DocumentRelationRequest(Long documentId, String relationType) {
    }

    record DocumentRelationView(Long id, Long sourceDocumentId, Long targetDocumentId,
                                String relationType, String targetTitle) {
    }

    record DocumentSummary(Long id, Long spaceId, String title, String docType,
                           String lifecycleStatus, String parseStatus) {
    }
}
