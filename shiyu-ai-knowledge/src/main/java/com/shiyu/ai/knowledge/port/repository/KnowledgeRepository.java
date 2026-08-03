package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import java.util.List;

public interface KnowledgeRepository {
    KnowledgeBO findById(Long id);
    KnowledgeBO findByCode(String code);
    List<KnowledgeBO> findAll();
    List<KnowledgeBO> searchByName(String keyword, int topK);
    List<KnowledgeBO> page(int offset, int limit);
    List<KnowledgeBO> page(int offset, int limit, String category, String keyword);
    long count();
    long count(String category, String keyword);
    int insert(KnowledgeBO bo);
    int update(KnowledgeBO bo);
    int deleteById(Long id);
    boolean existsByCode(String code);
    boolean existsBySpaceAndCode(Long spaceId, String code);
    List<KnowledgeBO> findBySpace(Long spaceId);
    PageData<KnowledgeBO> pageBySpace(Long spaceId, int pageNum, int pageSize, String keyword, String category);
    int deleteByIdAndSpace(Long id, Long spaceId);
    void assignDefaultSpace(Long spaceId);
}
