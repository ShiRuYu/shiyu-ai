package com.shiyu.ai.knowledge.path;

import java.util.List;
import java.util.Set;

public interface LearningPathService {

    List<Long> generatePath(Long targetKnowledgeId);

    List<Long> findMissingPrerequisites(Long targetKnowledgeId, Set<Long> masteredIds);
}
