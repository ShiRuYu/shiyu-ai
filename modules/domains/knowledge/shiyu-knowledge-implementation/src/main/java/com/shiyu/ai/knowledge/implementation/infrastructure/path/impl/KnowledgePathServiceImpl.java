package com.shiyu.ai.knowledge.implementation.infrastructure.path.impl;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.SpaceRole;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeBO;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.api.KnowledgePathPort;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService;
import com.shiyu.ai.knowledge.implementation.application.service.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.implementation.infrastructure.path.KnowledgePathService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 提供 知识 Path 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 执行 知识 Path 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param targetKnowledgeId 用于定位target 知识的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Long> generatePath(ActorContext actor, Long targetKnowledgeId) {
        requireAccess(actor, targetKnowledgeId);
        return knowledgeGraph.topologicalSort(actor.tenantId(), targetKnowledgeId);
    }

    /**
     * 查询 知识 Path 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param fromKnowledgeId 用于定位from 知识的标识。
     * @param toKnowledgeId 用于定位to 知识的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 知识 Path 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param targetKnowledgeId 用于定位target 知识的标识。
     * @param masteredIds 待处理的业务对象标识集合。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
