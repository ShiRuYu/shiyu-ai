package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.knowledge.domain.model.KnowledgeRelationBO;
import java.util.List;

public interface KnowledgeRelationRepository {
    List<KnowledgeRelationBO> findAll();
    List<KnowledgeRelationBO> findBySourceId(Long sourceId);
    List<KnowledgeRelationBO> findBySourceId(Long spaceId, Long sourceId);
    List<KnowledgeRelationBO> findByTargetId(Long targetId);
    List<KnowledgeRelationBO> findByTargetId(Long spaceId, Long targetId);
    List<KnowledgeRelationBO> findBySourceIdAndType(Long sourceId, String type);
    List<KnowledgeRelationBO> findBySourceIdAndType(Long spaceId, Long sourceId, String type);
    List<KnowledgeRelationBO> findByTargetIdAndType(Long targetId, String type);
    List<KnowledgeRelationBO> findByTargetIdAndType(Long spaceId, Long targetId, String type);
    int insert(KnowledgeRelationBO bo);
    int deleteBySourceAndTargetAndType(Long sourceId, Long targetId, String type);
    int deleteBySourceAndTargetAndType(Long spaceId, Long sourceId, Long targetId, String type);
    int deleteBySourceIdOrTargetId(Long knowledgeId);
    int deleteBySourceIdOrTargetId(Long spaceId, Long knowledgeId);
    List<KnowledgeRelationBO> findBySpace(Long spaceId);
    boolean exists(Long spaceId, Long sourceId, Long targetId, String type);
    void assignDefaultSpace(Long spaceId);
}
