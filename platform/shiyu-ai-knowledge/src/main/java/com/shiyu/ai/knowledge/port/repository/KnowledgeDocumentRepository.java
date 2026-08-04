package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import java.util.List;

public interface KnowledgeDocumentRepository {
    KnowledgeDocumentBO selectById(Long id);
    List<KnowledgeDocumentBO> selectAll();
    int insert(KnowledgeDocumentBO bo);
    int update(KnowledgeDocumentBO bo);
    int deleteById(Long id);
    List<KnowledgeDocumentBO> searchByKeyword(String keyword, int topK);
    List<KnowledgeDocumentBO> selectByKnowledgeId(Long knowledgeId);
    List<KnowledgeDocumentBO> selectByKnowledgeId(Long spaceId, Long knowledgeId);
    PageData<KnowledgeDocumentBO> pageBySpace(Long spaceId, int pageNum, int pageSize, String keyword, String lifecycleStatus, String parseStatus);
    KnowledgeDocumentBO findBySpaceAndChecksum(Long spaceId, String checksum);
    List<KnowledgeDocumentBO> findBySpace(Long spaceId);
    void assignDefaultSpace(Long spaceId);
}
