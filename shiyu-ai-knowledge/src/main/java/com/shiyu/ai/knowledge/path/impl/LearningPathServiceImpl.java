package com.shiyu.ai.knowledge.path.impl;

import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.path.LearningPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LearningPathServiceImpl implements LearningPathService {

    private final KnowledgeGraph knowledgeGraph;

    @Override
    public List<Long> generatePath(Long targetKnowledgeId) {
        return knowledgeGraph.topologicalSort(targetKnowledgeId);
    }

    @Override
    public List<Long> findMissingPrerequisites(Long targetKnowledgeId, Set<Long> masteredIds) {
        return knowledgeGraph.findMissingPrerequisites(targetKnowledgeId, masteredIds);
    }
}
