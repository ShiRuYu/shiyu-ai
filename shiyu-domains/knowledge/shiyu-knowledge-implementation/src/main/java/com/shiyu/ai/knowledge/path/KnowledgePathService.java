package com.shiyu.ai.knowledge.path;

import com.shiyu.ai.kernel.context.ActorContext;
import java.util.List;
import java.util.Set;

/**
 * 通用知识结构路径服务。
 */
public interface KnowledgePathService {

    List<Long> generatePath(ActorContext actor, Long targetKnowledgeId);

    List<Long> findPath(ActorContext actor, Long fromKnowledgeId, Long toKnowledgeId);

    List<Long> findMissingPrerequisites(ActorContext actor, Long targetKnowledgeId,
                                        Set<Long> masteredIds);
}
