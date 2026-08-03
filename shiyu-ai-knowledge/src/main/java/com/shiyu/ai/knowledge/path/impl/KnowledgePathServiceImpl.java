package com.shiyu.ai.knowledge.path.impl;

import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.path.KnowledgePathService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service("knowledgePathServiceImpl")
@RequiredArgsConstructor
public class KnowledgePathServiceImpl implements KnowledgePathService {

    private final KnowledgeGraph knowledgeGraph;
    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgeSpaceService spaceService;

    @Override
    public List<Long> generatePath(Long targetKnowledgeId) {
        requireAccess(targetKnowledgeId);
        return knowledgeGraph.topologicalSort(targetKnowledgeId);
    }

    @Override
    public List<Long> findPath(Long fromKnowledgeId, Long toKnowledgeId) {
        requireAccess(fromKnowledgeId);
        requireAccess(toKnowledgeId);
        var from = knowledgeRepository.findById(fromKnowledgeId);
        var to = knowledgeRepository.findById(toKnowledgeId);
        if (!java.util.Objects.equals(from.getSpaceId(), to.getSpaceId())) {
            throw new ServiceException("路径查询只能在同一知识空间内进行");
        }
        return knowledgeGraph.findPath(fromKnowledgeId, toKnowledgeId);
    }

    @Override
    public List<Long> findMissingPrerequisites(Long targetKnowledgeId,
                                               Set<Long> masteredIds) {
        requireAccess(targetKnowledgeId);
        return knowledgeGraph.findMissingPrerequisites(targetKnowledgeId, masteredIds);
    }

    private void requireAccess(Long pointId) {
        var point = knowledgeRepository.findById(pointId);
        if (point == null || point.getSpaceId() == null) {
            throw new ServiceException("知识点不存在: " + pointId);
        }
        spaceService.requireAccess(point.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER);
    }
}
