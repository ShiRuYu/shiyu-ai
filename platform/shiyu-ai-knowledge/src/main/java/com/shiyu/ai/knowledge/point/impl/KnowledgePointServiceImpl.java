package com.shiyu.ai.knowledge.point.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.domain.GraphNode;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.point.KnowledgePointService;
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
public class KnowledgePointServiceImpl implements KnowledgePointService {

    private final KnowledgeRepository repository;
    private final KnowledgeSpaceService spaceService;
    private final KnowledgeGraph knowledgeGraph;
    private final KnowledgeRelationService relationService;
    private final KnowledgeDocumentRelationService documentRelationService;

    @Override
    public PageData<PointView> page(Long spaceId, int pageNum, int pageSize,
                                    String keyword, String category) {
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER);
        PageData<KnowledgeBO> page = repository.pageBySpace(
                spaceId, pageNum, Math.min(pageSize, 100), keyword, category);
        return new PageData<>(page.getItems().stream().map(this::toView).toList(),
                page.getTotal());
    }

    @Override
    public PointView get(Long pointId) {
        KnowledgeBO point = requirePoint(pointId);
        spaceService.requireAccess(point.getSpaceId(),
                KnowledgeSpaceService.SpaceRole.VIEWER);
        return toView(point);
    }

    @Override
    public KnowledgeResponse getResponse(Long pointId) {
        PointView point = get(pointId);
        return toKnowledgeResponse(point);
    }

    @Override
    public KnowledgeGraphResponse graph(Long pointId) {
        PointView point = get(pointId);
        return new KnowledgeGraphResponse(
                toKnowledgeResponse(point),
                relationService.getPrerequisites(pointId),
                relationService.getSubsequent(pointId),
                relationService.getRelated(pointId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PointView create(Long spaceId, CreatePointRequest request) {
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.EDITOR);
        if (repository.existsBySpaceAndCode(spaceId, request.code().trim())) {
            throw new ServiceException("知识点编码已存在: " + request.code());
        }
        KnowledgeBO point = new KnowledgeBO();
        point.setSpaceId(spaceId);
        point.setTenantId(requireTenant());
        point.setCode(request.code().trim());
        point.setName(request.name().trim());
        point.setDescription(request.description());
        point.setDifficulty(request.difficultyLevel());
        point.setDifficultyLevel(request.difficultyLevel());
        point.setCategory(request.category());
        point.setTags(request.tags());
        point.setStatus(1);
        point.setDelFlag(0);
        repository.insert(point);
        knowledgeGraph.addNode(GraphNode.of(point.getId(), point.getName(), point.getCode()));
        return toView(point);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PointView update(Long pointId, UpdatePointRequest request) {
        KnowledgeBO point = requirePoint(pointId);
        spaceService.requireAccess(point.getSpaceId(),
                KnowledgeSpaceService.SpaceRole.EDITOR);
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
        repository.update(point);
        return toView(point);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long pointId) {
        KnowledgeBO point = requirePoint(pointId);
        spaceService.requireAccess(point.getSpaceId(),
                KnowledgeSpaceService.SpaceRole.EDITOR);
        knowledgeGraph.removeNode(pointId);
        relationService.removeAllRelations(pointId);
        documentRelationService.replaceDocuments(pointId, java.util.List.of());
        repository.deleteByIdAndSpace(pointId, point.getSpaceId());
    }

    private KnowledgeBO requirePoint(Long pointId) {
        KnowledgeBO point = repository.findById(pointId);
        if (point == null || point.getSpaceId() == null) {
            throw new ServiceException("知识点不存在: " + pointId);
        }
        return point;
    }

    private Long requireTenant() {
        Long tenantId = UserContextHolder.getCurrentTenantId();
        if (tenantId == null) {
            throw new ServiceException("当前租户上下文不存在");
        }
        return tenantId;
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
