package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import com.shiyu.ai.knowledge.implementation.domain.RelationType;

import java.util.List;

/**
 * KnowledgeRelationService 服务接口，负责执行知识领域相关业务操作。
 */
public interface KnowledgeRelationService {

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param knowledgeId 知识点标识。
     *
     * @return 符合条件的结果集合。
     */
    List<RelationView> list(ActorContext actor, Long knowledgeId);

    /**
     * 获取prerequisites。
     *
     * @param actor 调用方上下文。
     * @param knowledgeId knowledgeId 参数。
     *
     * @return 结果列表。
     */
    List<KnowledgeResponse> getPrerequisites(ActorContext actor, Long knowledgeId);

    /**
     * 获取subsequent。
     *
     * @param actor 调用方上下文。
     * @param knowledgeId knowledgeId 参数。
     *
     * @return 结果列表。
     */
    List<KnowledgeResponse> getSubsequent(ActorContext actor, Long knowledgeId);

    /**
     * 获取related。
     *
     * @param actor 调用方上下文。
     * @param knowledgeId knowledgeId 参数。
     *
     * @return 结果列表。
     */
    List<KnowledgeResponse> getRelated(ActorContext actor, Long knowledgeId);

    /**
     * 追加关系。
     *
     * @param actor 调用方上下文。
     * @param sourceId sourceId 参数。
     * @param targetId targetId 参数。
     * @param type 数据类型。
     * @param weight weight 参数。
     */
    void addRelation(
            ActorContext actor, Long sourceId, Long targetId, RelationType type, Double weight);

    /**
     * 删除关系。
     *
     * @param actor 调用方上下文。
     * @param sourceId sourceId 参数。
     * @param targetId targetId 参数。
     * @param type 数据类型。
     */
    void removeRelation(ActorContext actor, Long sourceId, Long targetId, RelationType type);

    /** 移除指定知识点的所有关联关系 */
    void removeAllRelations(ActorContext actor, Long knowledgeId);

    /**
     * {@code RelationView} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param sourceId sourceId 属性，表示该记录组件承载的数据。
     * @param targetId targetId 属性，表示该记录组件承载的数据。
     * @param relationType relationType 属性，表示该记录组件承载的数据。
     * @param weight weight 属性，表示该记录组件承载的数据。
     * @param source 来源，表示该记录组件承载的数据。
     * @param target 目标，表示该记录组件承载的数据。
     */
    record RelationView(
            Long sourceId,
            Long targetId,
            String relationType,
            Double weight,
            KnowledgeResponse source,
            KnowledgeResponse target) {}
}
