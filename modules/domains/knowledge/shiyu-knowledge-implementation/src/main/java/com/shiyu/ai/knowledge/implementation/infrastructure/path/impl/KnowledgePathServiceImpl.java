package com.shiyu.ai.knowledge.implementation.infrastructure.path.impl;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.SpaceRole;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeBO;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.api.KnowledgePathPort;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService;
import com.shiyu.ai.knowledge.implementation.application.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.implementation.infrastructure.path.KnowledgePathService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * {@code KnowledgePathServiceImpl} 实现知识模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Service("knowledgePathServiceImpl")
@RequiredArgsConstructor
public class KnowledgePathServiceImpl implements KnowledgePathService, KnowledgePathPort {

    /**
     * knowledgeGraph 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeGraph knowledgeGraph;
    /**
     * knowledgeRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRepository knowledgeRepository;
    /**
     * spaceService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeSpaceService spaceService;

    /**
     * {@code generatePath} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param targetKnowledgeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Long> generatePath(ActorContext actor, Long targetKnowledgeId) {
        requireAccess(actor, targetKnowledgeId);
        return knowledgeGraph.topologicalSort(actor.tenantId(), targetKnowledgeId);
    }

    /**
     * {@code findPath} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param fromKnowledgeId 参数值，用于执行当前操作。
     * @param toKnowledgeId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Long> findPath(ActorContext actor, Long fromKnowledgeId, Long toKnowledgeId) {
        var from = requireAccess(actor, fromKnowledgeId);
        var to = requireAccess(actor, toKnowledgeId);
        if (!java.util.Objects.equals(from.getSpaceId(), to.getSpaceId())) {
            throw new ServiceException("路径查询只能在同一知识空间内进行");
        }
        return knowledgeGraph.findPath(actor.tenantId(), fromKnowledgeId, toKnowledgeId);
    }

    /**
     * {@code findMissingPrerequisites} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param targetKnowledgeId 参数值，用于执行当前操作。
     * @param masteredIds 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Long> findMissingPrerequisites(
            ActorContext actor, Long targetKnowledgeId, Set<Long> masteredIds) {
        requireAccess(actor, targetKnowledgeId);
        return knowledgeGraph.findMissingPrerequisites(
                actor.tenantId(), targetKnowledgeId, masteredIds);
    }

    private com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeBO requireAccess(
            ActorContext actor, Long pointId) {
        if (actor == null) {
            throw new ServiceException("actor context is required");
        }
        var point = knowledgeRepository.findById(actor.tenantId(), pointId);
        if (point == null || point.getSpaceId() == null) {
            throw new ServiceException("知识点不存在: " + pointId);
        }
        spaceService.requireAccess(
                point.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        return point;
    }
}
