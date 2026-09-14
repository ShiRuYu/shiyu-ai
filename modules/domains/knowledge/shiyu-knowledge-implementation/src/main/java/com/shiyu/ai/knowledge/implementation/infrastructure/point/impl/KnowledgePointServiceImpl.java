package com.shiyu.ai.knowledge.implementation.infrastructure.point.impl;

import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.SpaceRole;
import com.shiyu.ai.knowledge.implementation.infrastructure.point.KnowledgePointService.CreatePointRequest;
import com.shiyu.ai.knowledge.implementation.infrastructure.point.KnowledgePointService.PointView;
import com.shiyu.ai.knowledge.implementation.infrastructure.point.KnowledgePointService.UpdatePointRequest;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.api.KnowledgePointPort;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeRelationService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService;
import com.shiyu.ai.knowledge.implementation.application.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.implementation.domain.GraphNode;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.implementation.infrastructure.point.KnowledgePointService;
import com.shiyu.ai.knowledge.implementation.web.response.KnowledgeGraphResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code KnowledgePointServiceImpl} 实现知识模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Service
@RequiredArgsConstructor
public class KnowledgePointServiceImpl implements KnowledgePointService, KnowledgePointPort {

    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final KnowledgeRepository repository;
    /**
     * spaceService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeSpaceService spaceService;
    /**
     * knowledgeGraph 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeGraph knowledgeGraph;
    /**
     * relationService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRelationService relationService;
    /**
     * documentRelationService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeDocumentRelationService documentRelationService;

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     * @param category 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PageData<PointView> page(
            ActorContext actor,
            Long spaceId,
            int pageNum,
            int pageSize,
            String keyword,
            String category) {
        requireActor(actor);
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        PageData<KnowledgeBO> page =
                repository.pageBySpace(
                        actor.tenantId(),
                        spaceId,
                        pageNum,
                        Math.min(pageSize, 100),
                        keyword,
                        category);
        return new PageData<>(page.getItems().stream().map(this::toView).toList(), page.getTotal());
    }

    /**
     * {@code get} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param pointId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PointView get(ActorContext actor, Long pointId) {
        KnowledgeBO point = requirePoint(actor, pointId);
        spaceService.requireAccess(
                point.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        return toView(point);
    }

    /**
     * {@code getResponse} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param pointId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public KnowledgeResponse getResponse(ActorContext actor, Long pointId) {
        PointView point = get(actor, pointId);
        return toKnowledgeResponse(point);
    }

    /**
     * {@code graph} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param pointId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public KnowledgeGraphResponse graph(ActorContext actor, Long pointId) {
        PointView point = get(actor, pointId);
        return new KnowledgeGraphResponse(
                toKnowledgeResponse(point),
                relationService.getPrerequisites(actor, pointId),
                relationService.getSubsequent(actor, pointId),
                relationService.getRelated(actor, pointId));
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PointView create(ActorContext actor, Long spaceId, CreatePointRequest request) {
        requireActor(actor);
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        if (repository.existsBySpaceAndCode(actor.tenantId(), spaceId, request.code().trim())) {
            throw new ServiceException("知识点编码已存在: " + request.code());
        }
        KnowledgeBO point = new KnowledgeBO();
        point.setSpaceId(spaceId);
        point.setTenantId(actor.tenantId().value());
        point.setCode(request.code().trim());
        point.setName(request.name().trim());
        point.setDescription(request.description());
        point.setDifficulty(request.difficultyLevel());
        point.setDifficultyLevel(request.difficultyLevel());
        point.setCategory(request.category());
        point.setTags(request.tags());
        point.setStatus(1);
        point.setDelFlag(0);
        requireWrite(repository.insert(actor.tenantId(), point), "create knowledge point");
        knowledgeGraph.addNode(
                actor.tenantId(), GraphNode.of(point.getId(), point.getName(), point.getCode()));
        return toView(point);
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param pointId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PointView update(ActorContext actor, Long pointId, UpdatePointRequest request) {
        KnowledgeBO point = requirePoint(actor, pointId);
        spaceService.requireAccess(
                point.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        if (request.name() != null && !request.name().isBlank()) {
            point.setName(request.name().trim());
        }
        if (request.description() != null) {
            point.setDescription(request.description());
        }
        if (request.difficultyLevel() != null) {
            point.setDifficulty(request.difficultyLevel());
            point.setDifficultyLevel(request.difficultyLevel());
        }
        if (request.category() != null) {
            point.setCategory(request.category());
        }
        if (request.tags() != null) {
            point.setTags(request.tags());
        }
        requireWrite(repository.update(actor.tenantId(), point), "update knowledge point");
        return toView(point);
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param pointId 参数值，用于执行当前操作。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ActorContext actor, Long pointId) {
        KnowledgeBO point = requirePoint(actor, pointId);
        spaceService.requireAccess(
                point.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        knowledgeGraph.removeNode(actor.tenantId(), pointId);
        relationService.removeAllRelations(actor, pointId);
        documentRelationService.replaceDocuments(actor, pointId, java.util.List.of());
        requireWrite(
                repository.deleteByIdAndSpace(actor.tenantId(), pointId, point.getSpaceId()),
                "delete knowledge point");
    }

    private KnowledgeBO requirePoint(ActorContext actor, Long pointId) {
        requireActor(actor);
        KnowledgeBO point = repository.findById(actor.tenantId(), pointId);
        if (point == null || point.getSpaceId() == null) {
            throw new ServiceException("知识点不存在: " + pointId);
        }
        return point;
    }

    private void requireActor(ActorContext actor) {
        if (actor == null) throw new ServiceException("当前租户或用户上下文不存在");
    }

    private void requireWrite(int rows, String operation) {
        if (rows < 1) {
            throw new ServiceException(operation + " failed: no tenant-owned row was affected");
        }
    }

    private PointView toView(KnowledgeBO point) {
        return new PointView(
                point.getId(),
                point.getSpaceId(),
                point.getCode(),
                point.getName(),
                point.getDescription(),
                point.getDifficultyLevel() != null
                        ? point.getDifficultyLevel()
                        : point.getDifficulty(),
                point.getCategory(),
                point.getTags());
    }

    private com.shiyu.ai.knowledge.contract.model.KnowledgeResponse toKnowledgeResponse(
            PointView point) {
        return new com.shiyu.ai.knowledge.contract.model.KnowledgeResponse(
                point.id(),
                point.code(),
                point.name(),
                point.description(),
                point.difficultyLevel(),
                point.category(),
                point.tags(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of());
    }
}
