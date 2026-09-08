package com.shiyu.ai.knowledge.path.impl;

import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.path.KnowledgePathService;
import com.shiyu.ai.knowledge.port.KnowledgePathPort;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service("knowledgePathServiceImpl")
@RequiredArgsConstructor
public class KnowledgePathServiceImpl implements KnowledgePathService, KnowledgePathPort {

    private final KnowledgeGraph knowledgeGraph;
    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgeSpaceService spaceService;

    @Override
    public List<Long> generatePath(ActorContext actor, Long targetKnowledgeId) {
        requireAccess(actor, targetKnowledgeId);
        return knowledgeGraph.topologicalSort(actor.tenantId(), targetKnowledgeId);
    }

    @Override
    public List<Long> findPath(ActorContext actor, Long fromKnowledgeId, Long toKnowledgeId) {
        var from = requireAccess(actor, fromKnowledgeId);
        var to = requireAccess(actor, toKnowledgeId);
        if (!java.util.Objects.equals(from.getSpaceId(), to.getSpaceId())) {
            throw new ServiceException("路径查询只能在同一知识空间内进行");
        }
        return knowledgeGraph.findPath(actor.tenantId(), fromKnowledgeId, toKnowledgeId);
    }

    @Override
    public List<Long> findMissingPrerequisites(ActorContext actor, Long targetKnowledgeId,
                                               Set<Long> masteredIds) {
        requireAccess(actor, targetKnowledgeId);
        return knowledgeGraph.findMissingPrerequisites(actor.tenantId(), targetKnowledgeId, masteredIds);
    }

    private com.shiyu.ai.knowledge.domain.model.KnowledgeBO requireAccess(
            ActorContext actor, Long pointId) {
        if (actor == null) {
            throw new ServiceException("actor context is required");
        }
        var point = knowledgeRepository.findById(actor.tenantId(), pointId);
        if (point == null || point.getSpaceId() == null) {
            throw new ServiceException("知识点不存在: " + pointId);
        }
        spaceService.requireAccess(point.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        return point;
    }
}
