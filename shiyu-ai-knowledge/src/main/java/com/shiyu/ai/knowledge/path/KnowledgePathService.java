package com.shiyu.ai.knowledge.path;

import java.util.List;
import java.util.Set;

/**
 * 通用知识结构路径服务。
 */
public interface KnowledgePathService {

    List<Long> generatePath(Long targetKnowledgeId);

    List<Long> findMissingPrerequisites(Long targetKnowledgeId,
                                        Set<Long> masteredIds);
}
