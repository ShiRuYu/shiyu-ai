package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.knowledge.search.SearchResult;

import java.util.List;

/**
 * Document Knowledge 接口
 */

public interface DocumentKnowledgeService {

    /**
     * Get By Id
     * @return 处理结果
     */
    KnowledgeDocumentVO getById(Long id);

    /**
     * Search
     * @return 处理结果
     */
    List<KnowledgeDocumentVO> search(String keyword, int topK);

    /**
     * Search By Knowledge Id
     * @return 处理结果
     */
    List<KnowledgeDocumentVO> searchByKnowledgeId(Long knowledgeId);

    /**
     * Create
     * @param CreateDocumentRequest CreateDocumentRequest
     * @return 处理结果
     */
    KnowledgeDocumentVO create(CreateDocumentRequest request);

    /**
     * Update
     * @param UpdateDocumentRequest UpdateDocumentRequest
     * @return 处理结果
     */
    void update(Long id, UpdateDocumentRequest request);

    /**
     * Delete
     * @return 处理结果
     */
    void delete(Long id);

    /**
     * 解除知识点与所有文档的关联
     */
    void deleteByKnowledgeId(Long knowledgeId);

    record KnowledgeDocumentVO(Long id, String title, String content, String docType,
                               String source, List<Long> knowledgeIds) {}

    record CreateDocumentRequest(String title, String content, String docType,
                                 String source, List<Long> knowledgeIds) {}

    record UpdateDocumentRequest(String title, String content, String docType,
                                 String source, List<Long> knowledgeIds) {}
}
