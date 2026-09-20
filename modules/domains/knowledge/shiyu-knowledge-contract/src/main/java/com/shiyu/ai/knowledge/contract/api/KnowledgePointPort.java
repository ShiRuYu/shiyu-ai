package com.shiyu.ai.knowledge.contract.api;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;

/**
 * 定义 知识 Point 领域与外部能力交互的端口契约。
 */
public interface KnowledgePointPort {

    /**
     * 查询指定知识点的知识响应。
     *
     * @param actor 当前操作主体上下文
     * @param pointId 知识点 ID
     * @return 知识点响应
     */
    KnowledgeResponse getResponse(ActorContext actor, Long pointId);
}
