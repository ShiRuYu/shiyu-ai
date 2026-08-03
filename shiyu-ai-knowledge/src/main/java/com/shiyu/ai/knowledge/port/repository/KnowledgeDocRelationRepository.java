package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.knowledge.domain.model.KnowledgeDocRelationBO;
import java.util.List;

public interface KnowledgeDocRelationRepository {
    void insert(KnowledgeDocRelationBO bo);
    void insertBatch(List<KnowledgeDocRelationBO> boList);
    void deleteByDocId(Long id);
    void deleteByKnowledgeId(Long id);
    void deleteByKnowledgeId(Long spaceId, Long id);
    List<KnowledgeDocRelationBO> selectByDocId(Long id);
    List<KnowledgeDocRelationBO> selectByDocId(Long spaceId, Long id);
    List<KnowledgeDocRelationBO> selectByKnowledgeId(Long id);
    List<KnowledgeDocRelationBO> selectByKnowledgeId(Long spaceId, Long id);
    void deleteByDocId(Long spaceId, Long id);
    void assignDefaultSpace(Long spaceId);
}
