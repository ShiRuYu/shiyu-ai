package com.shiyu.ai.knowledge.contract.api;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;

/** Read-only knowledge-point contract for other bounded contexts. */
public interface KnowledgePointPort {
    KnowledgeResponse getResponse(ActorContext actor, Long pointId);
}
