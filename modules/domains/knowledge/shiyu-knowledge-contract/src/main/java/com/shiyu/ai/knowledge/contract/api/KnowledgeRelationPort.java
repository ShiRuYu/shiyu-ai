package com.shiyu.ai.knowledge.contract.api;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;

import java.util.List;

/**
 * 定义 知识 关系 领域与外部能力交互的端口契约。
 */
public interface KnowledgeRelationPort {

    /**
     * 查询指定知识点的前置知识。
     *
     * @param actor 当前操作主体上下文
     * @param knowledgeId 知识点 ID
     * @return 前置知识响应列表
     */
    List<KnowledgeResponse> getPrerequisites(ActorContext actor, Long knowledgeId);
}
