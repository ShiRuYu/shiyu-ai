package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocRelationBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentRelationBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KnowledgeDocumentRelationServiceImpl
        implements KnowledgeDocumentRelationService {

    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeDocRelationRepository relationRepository;
    private final KnowledgeDocumentRelationRepository documentRelationRepository;
    private final KnowledgeSpaceService spaceService;

    @Override
    public List<DocumentSummary> listDocuments(ActorContext actor, Long pointId) {
        KnowledgeBO point = requirePoint(actor, pointId);
        spaceService.requireAccess(point.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        return relationRepository.selectByKnowledgeId(actor.tenantId(), point.getSpaceId(), pointId).stream()
                .map(relation -> documentRepository.selectById(
                        actor.tenantId(), relation.getDocId()))
                .filter(Objects::nonNull)
                .filter(document -> Objects.equals(point.getSpaceId(), document.getSpaceId()))
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceDocuments(ActorContext actor, Long pointId, List<Long> documentIds, String relationType) {
        KnowledgeBO point = requirePoint(actor, pointId);
        spaceService.requireAccess(point.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        List<Long> normalized = documentIds == null ? List.of()
                : documentIds.stream().filter(Objects::nonNull).distinct().toList();
        for (Long documentId : normalized) {
            KnowledgeDocumentBO document = requireDocument(actor, documentId);
            if (!Objects.equals(point.getSpaceId(), document.getSpaceId())) {
                throw new ServiceException("知识点和文档不属于同一知识空间");
            }
        }
        relationRepository.deleteByKnowledgeId(actor.tenantId(), point.getSpaceId(), pointId);
        if (normalized.isEmpty()) {
            return;
        }
        relationRepository.insertBatch(actor.tenantId(), normalized.stream().map(documentId -> {
            KnowledgeDocRelationBO relation = new KnowledgeDocRelationBO();
            relation.setSpaceId(point.getSpaceId());
            relation.setDocId(documentId);
            relation.setKnowledgeId(pointId);
            relation.setRelationType(normalizeRelationType(relationType));
            relation.setCreateTime(LocalDateTime.now());
            relation.setDelFlag(0);
            return relation;
        }).toList());
    }

    @Override
    public List<Long> listPointIds(ActorContext actor, Long documentId) {
        KnowledgeDocumentBO document = requireDocument(actor, documentId);
        if (document.getSpaceId() != null) {
            spaceService.requireAccess(document.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        }
        return relationRepository.selectByDocId(actor.tenantId(), document.getSpaceId(), documentId).stream()
                .map(KnowledgeDocRelationBO::getKnowledgeId)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replacePoints(ActorContext actor, Long documentId, List<Long> pointIds, String relationType) {
        KnowledgeDocumentBO document = requireDocument(actor, documentId);
        Long spaceId = document.getSpaceId();
        if (spaceId != null) {
            spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        }
        List<Long> normalized = pointIds == null ? List.of()
                : pointIds.stream().filter(Objects::nonNull).distinct().toList();
        for (Long pointId : normalized) {
            KnowledgeBO point = requirePoint(actor, pointId);
            if (!Objects.equals(spaceId, point.getSpaceId())) {
                throw new ServiceException("知识点和文档不属于同一知识空间");
            }
        }
        relationRepository.deleteByDocId(actor.tenantId(), spaceId, documentId);
        if (normalized.isEmpty()) {
            return;
        }
        relationRepository.insertBatch(actor.tenantId(), normalized.stream().map(pointId -> {
            KnowledgeDocRelationBO relation = new KnowledgeDocRelationBO();
            relation.setSpaceId(spaceId);
            relation.setDocId(documentId);
            relation.setKnowledgeId(pointId);
            relation.setRelationType(normalizeRelationType(relationType));
            relation.setCreateTime(LocalDateTime.now());
            relation.setDelFlag(0);
            return relation;
        }).toList());
    }

    @Override
    public void removeDocumentRelations(ActorContext actor, Long documentId) {
        KnowledgeDocumentBO document = requireDocument(actor, documentId);
        if (document.getSpaceId() != null) {
            spaceService.requireAccess(document.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        }
        relationRepository.deleteByDocId(actor.tenantId(), document.getSpaceId(), documentId);
        if (document.getTenantId() != null) {
            documentRelationRepository.deleteByDocument(actor.tenantId(), documentId);
        }
    }

    @Override
    public List<DocumentRelationView> listDocumentRelations(ActorContext actor, Long documentId) {
        KnowledgeDocumentBO source = requireDocument(actor, documentId);
        if (source.getSpaceId() == null) {
            return List.of();
        }
        spaceService.requireAccess(source.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        return documentRelationRepository.selectBySource(actor.tenantId(), source.getSpaceId(), documentId).stream()
                .map(relation -> toDocumentRelationView(actor, relation)).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceDocumentRelations(ActorContext actor, Long documentId, List<DocumentRelationRequest> relations) {
        KnowledgeDocumentBO source = requireDocument(actor, documentId);
        if (source.getSpaceId() == null || source.getTenantId() == null) {
            throw new ServiceException("文档未绑定知识空间");
        }
        spaceService.requireAccess(source.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        List<DocumentRelationRequest> normalized = relations == null ? List.of() : relations.stream()
                .filter(Objects::nonNull).filter(r -> r.documentId() != null).toList();
        List<KnowledgeDocumentRelationBO> records = normalized.stream().map(request -> {
            if (Objects.equals(documentId, request.documentId())) {
                throw new ServiceException("文档不能关联自身");
            }
            KnowledgeDocumentBO target = requireDocument(actor, request.documentId());
            if (!Objects.equals(source.getSpaceId(), target.getSpaceId())
                    || !Objects.equals(source.getTenantId(), target.getTenantId())) {
                throw new ServiceException("文档必须属于同一租户和知识空间");
            }
            KnowledgeDocumentRelationBO relation = new KnowledgeDocumentRelationBO();
            relation.setTenantId(source.getTenantId());
            relation.setSpaceId(source.getSpaceId());
            relation.setSourceDocumentId(documentId);
            relation.setTargetDocumentId(target.getId());
            relation.setRelationType(normalizeDocumentRelationType(request.relationType()));
            relation.setStatus(1);
            relation.setDelFlag(0);
            return relation;
        }).toList();
        documentRelationRepository.replace(actor.tenantId(), source.getSpaceId(), documentId, records);
    }

    private DocumentRelationView toDocumentRelationView(ActorContext actor,
                                                         KnowledgeDocumentRelationBO relation) {
        KnowledgeDocumentBO target = documentRepository.selectById(
                actor.tenantId(), relation.getTargetDocumentId());
        return new DocumentRelationView(relation.getId(), relation.getSourceDocumentId(),
                relation.getTargetDocumentId(), relation.getRelationType(),
                target == null ? null : target.getTitle());
    }

    private String normalizeDocumentRelationType(String value) {
        if (value == null || value.isBlank()) {
            return "RELATED_TO";
        }
        return switch (value.trim().toUpperCase()) {
            case "REFERENCES", "SUPERSEDES", "DERIVED_FROM", "TRANSLATION_OF",
                    "DUPLICATE_OF", "RELATED_TO" -> value.trim().toUpperCase();
            default -> throw new ServiceException("不支持的文档关系类型: " + value);
        };
    }

    private String normalizeRelationType(String relationType) {
        if (relationType == null || relationType.isBlank()) {
            return "RELATED";
        }
        return switch (relationType.trim().toUpperCase()) {
            case "SOURCE", "SUPPORTS", "EXPLAINS", "PRIMARY_SOURCE", "REFERENCE", "RELATED" ->
                    relationType.trim().toUpperCase();
            default -> throw new ServiceException("不支持的文档关联类型: " + relationType);
        };
    }

    private KnowledgeBO requirePoint(ActorContext actor, Long pointId) {
        requireActor(actor);
        KnowledgeBO point = knowledgeRepository.findById(actor.tenantId(), pointId);
        if (point == null || point.getSpaceId() == null) {
            throw new ServiceException("知识点不存在: " + pointId);
        }
        return point;
    }

    private KnowledgeDocumentBO requireDocument(ActorContext actor, Long documentId) {
        requireActor(actor);
        KnowledgeDocumentBO document = documentRepository.selectById(
                actor.tenantId(), documentId);
        if (document == null) {
            throw new ServiceException("文档不存在: " + documentId);
        }
        return document;
    }

    private void requireActor(ActorContext actor) {
        if (actor == null) throw new ServiceException("actor context is required");
    }

    private DocumentSummary toSummary(KnowledgeDocumentBO document) {
        return new DocumentSummary(document.getId(), document.getSpaceId(),
                document.getTitle(), document.getDocType(),
                document.getLifecycleStatus(), document.getParseStatus());
    }
}
