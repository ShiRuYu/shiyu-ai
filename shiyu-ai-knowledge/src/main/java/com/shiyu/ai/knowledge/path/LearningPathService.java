package com.shiyu.ai.knowledge.path;

import java.util.List;
import java.util.Set;

/**
 * Learning Path 接口
 */

public interface LearningPathService {

    /**
     * Generate Path
     * @return 处理结果
     */
    List<Long> generatePath(Long targetKnowledgeId);

    /**
     * Find Missing Prerequisites
     * @return 处理结果
     */
    List<Long> findMissingPrerequisites(Long targetKnowledgeId, Set<Long> masteredIds);
}
