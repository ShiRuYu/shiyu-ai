package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.knowledge.domain.RelationType;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;

import java.util.List;

/**
 * Knowledge Relation 接口
 */

public interface KnowledgeRelationService {

    /**
     * Get Prerequisites
     * @return 处理结果
     */
    List<KnowledgeResponse> getPrerequisites(Long knowledgeId);

    /**
     * Get Subsequent
     * @return 处理结果
     */
    List<KnowledgeResponse> getSubsequent(Long knowledgeId);

    /**
     * Get Related
     * @return 处理结果
     */
    List<KnowledgeResponse> getRelated(Long knowledgeId);

    /**
     * Add Relation
     * @param RelationType RelationType
     * @return 处理结果
     */
    void addRelation(Long sourceId, Long targetId, RelationType type, Double weight);

    /**
     * Remove Relation
     * @param RelationType RelationType
     * @return 处理结果
     */
    void removeRelation(Long sourceId, Long targetId, RelationType type);

    /**
     * 移除指定知识点的所有关联关系
     */
    void removeAllRelations(Long knowledgeId);
}
