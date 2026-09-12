package com.shiyu.ai.knowledge.contract.api;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;

/**
 * 知识点端口。
 * 为外部业务提供知识点详情查询能力。
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
