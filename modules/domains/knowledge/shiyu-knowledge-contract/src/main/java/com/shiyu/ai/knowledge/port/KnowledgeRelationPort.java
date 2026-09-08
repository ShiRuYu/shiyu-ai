package com.shiyu.ai.knowledge.port;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;

import java.util.List;

/** Read-only prerequisite contract for other bounded contexts. */
public interface KnowledgeRelationPort {
    List<KnowledgeResponse> getPrerequisites(ActorContext actor, Long knowledgeId);
}
