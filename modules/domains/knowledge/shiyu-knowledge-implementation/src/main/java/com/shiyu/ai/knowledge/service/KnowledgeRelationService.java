package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.knowledge.domain.RelationType;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * Knowledge Relation 接口
 */

public interface KnowledgeRelationService {

    List<RelationView> list(ActorContext actor, Long knowledgeId);

    /**
     * Get Prerequisites
     * @return 处理结果
     */
    List<KnowledgeResponse> getPrerequisites(ActorContext actor, Long knowledgeId);

    /**
     * Get Subsequent
     * @return 处理结果
     */
    List<KnowledgeResponse> getSubsequent(ActorContext actor, Long knowledgeId);

    /**
     * Get Related
     * @return 处理结果
     */
    List<KnowledgeResponse> getRelated(ActorContext actor, Long knowledgeId);

    /**
     * Add Relation
     * @param RelationType RelationType
     * @return 处理结果
     */
    void addRelation(ActorContext actor, Long sourceId, Long targetId, RelationType type, Double weight);

    /**
     * Remove Relation
     * @param RelationType RelationType
     * @return 处理结果
     */
    void removeRelation(ActorContext actor, Long sourceId, Long targetId, RelationType type);

    /**
     * 移除指定知识点的所有关联关系
     */
    void removeAllRelations(ActorContext actor, Long knowledgeId);

    record RelationView(Long sourceId, Long targetId, String relationType,
                        Double weight, KnowledgeResponse source,
                        KnowledgeResponse target) {
    }
}
