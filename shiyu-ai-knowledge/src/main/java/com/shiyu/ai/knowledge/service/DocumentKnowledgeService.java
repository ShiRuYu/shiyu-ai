package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.knowledge.search.SearchResult;

import java.util.List;

public interface DocumentKnowledgeService {

    KnowledgeDocumentVO getById(Long id);

    List<KnowledgeDocumentVO> search(String keyword, int topK);

    List<KnowledgeDocumentVO> searchByKnowledgeId(Long knowledgeId);

    KnowledgeDocumentVO create(CreateDocumentRequest request);

    void update(Long id, UpdateDocumentRequest request);

    void delete(Long id);

    record KnowledgeDocumentVO(Long id, String title, String content, String docType,
                               String source, List<Long> knowledgeIds) {}

    record CreateDocumentRequest(String title, String content, String docType,
                                 String source, List<Long> knowledgeIds) {}

    record UpdateDocumentRequest(String title, String content, String docType,
                                 String source, List<Long> knowledgeIds) {}
}
