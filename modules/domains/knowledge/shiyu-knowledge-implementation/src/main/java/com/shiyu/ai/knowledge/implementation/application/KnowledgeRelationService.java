package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import com.shiyu.ai.knowledge.implementation.domain.RelationType;

import java.util.List;

/**
 * 提供 知识 关系 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeResponse> getPrerequisites(ActorContext actor, Long knowledgeId);

    /**
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeResponse> getSubsequent(ActorContext actor, Long knowledgeId);

    /**
     * 查询 知识 关系 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param knowledgeId 用于定位knowledge的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 封装 关系 View 相关的不可变数据及其字段约束。
     */
    record RelationView(
            Long sourceId,
            Long targetId,
            String relationType,
            Double weight,
            KnowledgeResponse source,
            KnowledgeResponse target) {}
}
