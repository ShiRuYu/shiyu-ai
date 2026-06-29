package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.knowledge.domain.RelationType;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;

import java.util.List;

public interface KnowledgeRelationService {

    List<KnowledgeResponse> getPrerequisites(Long knowledgeId);

    List<KnowledgeResponse> getSubsequent(Long knowledgeId);

    List<KnowledgeResponse> getRelated(Long knowledgeId);

    void addRelation(Long sourceId, Long targetId, RelationType type, Double weight);

    void removeRelation(Long sourceId, Long targetId, RelationType type);
}
