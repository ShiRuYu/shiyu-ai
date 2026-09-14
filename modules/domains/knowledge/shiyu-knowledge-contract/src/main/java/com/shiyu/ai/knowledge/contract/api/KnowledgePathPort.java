package com.shiyu.ai.knowledge.contract.api;

import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;
import java.util.Set;

/**
 * 知识路径端口。
 * 为外部业务提供知识学习路径生成和前置知识检查能力。
 */
public interface KnowledgePathPort {

    /**
     * 根据目标知识点生成学习路径。
     *
     * @param actor 当前操作主体上下文
     * @param targetKnowledgeId 目标知识点 ID
     * @return 从起点到目标知识点的知识点 ID 列表
     */
    List<Long> generatePath(ActorContext actor, Long targetKnowledgeId);

    /**
     * 查询目标知识点尚未掌握的前置知识。
     *
     * @param actor 当前操作主体上下文
     * @param targetKnowledgeId 目标知识点 ID
     * @param masteredIds 已掌握的知识点 ID 集合
     * @return 尚未掌握的前置知识点 ID 列表
     */
    List<Long> findMissingPrerequisites(
            ActorContext actor, Long targetKnowledgeId, Set<Long> masteredIds);
}
