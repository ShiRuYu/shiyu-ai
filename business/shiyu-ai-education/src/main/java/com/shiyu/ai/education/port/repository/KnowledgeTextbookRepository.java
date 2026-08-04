package com.shiyu.ai.education.port.repository;

import com.shiyu.ai.education.domain.model.KnowledgeTextbookBO;
import java.util.List;

public interface KnowledgeTextbookRepository {
    void insert(KnowledgeTextbookBO kt);
    void deleteById(Long id);
    void deleteByChapterId(Long cid);
    void deleteByKnowledgeIdAndChapterId(Long kid, Long cid);
    List<KnowledgeTextbookBO> selectByChapterId(Long cid);
    List<KnowledgeTextbookBO> selectByKnowledgeId(Long kid);
    List<KnowledgeTextbookBO> selectByTextbookId(Long tid);
}
