package com.shiyu.ai.knowledge.implementation.infrastructure.path;

import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;
import java.util.Set;

/** 通用知识结构路径服务。 */
public interface KnowledgePathService {

    /**
     * 执行 {@code generatePath} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param targetKnowledgeId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> generatePath(ActorContext actor, Long targetKnowledgeId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param fromKnowledgeId 方法参数。
     * @param toKnowledgeId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> findPath(ActorContext actor, Long fromKnowledgeId, Long toKnowledgeId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param actor 当前操作主体上下文。
     * @param targetKnowledgeId 方法参数。
     * @param masteredIds 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> findMissingPrerequisites(
            ActorContext actor, Long targetKnowledgeId, Set<Long> masteredIds);
}
