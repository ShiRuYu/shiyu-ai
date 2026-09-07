package com.shiyu.ai.knowledge.port;

import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;
import java.util.Set;

/** Read-only graph path contract for other bounded contexts. */
public interface KnowledgePathPort {
    List<Long> generatePath(ActorContext actor, Long targetKnowledgeId);

    List<Long> findMissingPrerequisites(ActorContext actor, Long targetKnowledgeId,
                                        Set<Long> masteredIds);
}
