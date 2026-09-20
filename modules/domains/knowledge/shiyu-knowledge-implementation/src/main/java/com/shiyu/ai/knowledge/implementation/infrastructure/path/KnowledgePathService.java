package com.shiyu.ai.knowledge.implementation.infrastructure.path;

import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;
import java.util.Set;

/**
 * 提供 知识 Path 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface KnowledgePathService {

    /**
     * 执行 知识 Path 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param targetKnowledgeId 用于定位target 知识的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> generatePath(ActorContext actor, Long targetKnowledgeId);

    /**
     * 查询 知识 Path 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param fromKnowledgeId 用于定位from 知识的标识。
     * @param toKnowledgeId 用于定位to 知识的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> findPath(ActorContext actor, Long fromKnowledgeId, Long toKnowledgeId);

    /**
     * 查询 知识 Path 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param targetKnowledgeId 用于定位target 知识的标识。
     * @param masteredIds 待处理的业务对象标识集合。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> findMissingPrerequisites(
            ActorContext actor, Long targetKnowledgeId, Set<Long> masteredIds);
}
