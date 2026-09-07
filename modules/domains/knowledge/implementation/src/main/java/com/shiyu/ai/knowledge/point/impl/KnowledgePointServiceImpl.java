package com.shiyu.ai.knowledge.point.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.domain.GraphNode;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.point.KnowledgePointService;
import com.shiyu.ai.knowledge.port.KnowledgePointPort;
import com.shiyu.ai.knowledge.dto.KnowledgeGraphResponse;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnowledgePointServiceImpl implements KnowledgePointService, KnowledgePointPort {

    private final KnowledgeRepository repository;
    private final KnowledgeSpaceService spaceService;
    private final KnowledgeGraph knowledgeGraph;
    private final KnowledgeRelationService relationService;
    private final KnowledgeDocumentRelationService documentRelationService;

    @Override
    public PageData<PointView> page(ActorContext actor, Long spaceId, int pageNum, int pageSize,
                                    String keyword, String category) {
        requireActor(actor);
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        PageData<KnowledgeBO> page = repository.pageBySpace(
                actor.tenantId(), spaceId, pageNum, Math.min(pageSize, 100), keyword, category);
        return new PageData<>(page.getItems().stream().map(this::toView).toList(),
                page.getTotal());
    }

    @Override
    public PointView get(ActorContext actor, Long pointId) {
        KnowledgeBO point = requirePoint(actor, pointId);
        spaceService.requireAccess(point.getSpaceId(),
                KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        return toView(point);
    }

    @Override
    public KnowledgeResponse getResponse(ActorContext actor, Long pointId) {
        PointView point = get(actor, pointId);
        return toKnowledgeResponse(point);
    }

    @Override
    public KnowledgeGraphResponse graph(ActorContext actor, Long pointId) {
        PointView point = get(actor, pointId);
        return new KnowledgeGraphResponse(
                toKnowledgeResponse(point),
                relationService.getPrerequisites(actor, pointId),
                relationService.getSubsequent(actor, pointId),
                relationService.getRelated(actor, pointId));
    }

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
        knowledgeGraph.addNode(actor.tenantId(), GraphNode.of(point.getId(), point.getName(), point.getCode()));
        return toView(point);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PointView update(ActorContext actor, Long pointId, UpdatePointRequest request) {
        KnowledgeBO point = requirePoint(actor, pointId);
        spaceService.requireAccess(point.getSpaceId(),
                KnowledgeSpaceService.SpaceRole.EDITOR, actor);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ActorContext actor, Long pointId) {
        KnowledgeBO point = requirePoint(actor, pointId);
        spaceService.requireAccess(point.getSpaceId(),
                KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        knowledgeGraph.removeNode(actor.tenantId(), pointId);
        relationService.removeAllRelations(actor, pointId);
        documentRelationService.replaceDocuments(actor, pointId, java.util.List.of());
        requireWrite(repository.deleteByIdAndSpace(actor.tenantId(), pointId, point.getSpaceId()),
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
        return new PointView(point.getId(), point.getSpaceId(), point.getCode(),
                point.getName(), point.getDescription(),
                point.getDifficultyLevel() != null ? point.getDifficultyLevel() : point.getDifficulty(),
                point.getCategory(), point.getTags());
    }

    private com.shiyu.ai.knowledge.dto.KnowledgeResponse toKnowledgeResponse(PointView point) {
        return new com.shiyu.ai.knowledge.dto.KnowledgeResponse(
                point.id(), point.code(), point.name(), point.description(),
                point.difficultyLevel(), point.category(), point.tags(),
                java.util.List.of(), java.util.List.of(), java.util.List.of());
    }
}
