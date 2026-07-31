package com.shiyu.ai.knowledge.service;

import java.util.List;

/**
 * 知识点与企业文档的统一关系服务。
 */
public interface KnowledgeDocumentRelationService {

    List<DocumentSummary> listDocuments(Long pointId);

    void replaceDocuments(Long pointId, List<Long> documentIds);

    List<Long> listPointIds(Long documentId);

    void replacePoints(Long documentId, List<Long> pointIds);

    void removeDocumentRelations(Long documentId);

    record DocumentSummary(Long id, Long spaceId, String title, String docType,
                           String lifecycleStatus, String parseStatus) {
    }
}
