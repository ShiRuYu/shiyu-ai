package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeBO;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocRelationBO;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocumentBO;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocumentRelationBO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeDocRelationRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeDocumentRelationRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
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
    public List<DocumentSummary> listDocuments(Long pointId) {
        KnowledgeBO point = requirePoint(pointId);
        spaceService.requireAccess(point.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER);
        return relationRepository.selectByKnowledgeId(point.getSpaceId(), pointId).stream()
                .map(relationRepository -> documentRepository.selectById(relationRepository.getDocId()))
                .filter(Objects::nonNull)
                .filter(document -> Objects.equals(point.getSpaceId(), document.getSpaceId()))
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceDocuments(Long pointId, List<Long> documentIds, String relationType) {
        KnowledgeBO point = requirePoint(pointId);
        spaceService.requireAccess(point.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR);
        List<Long> normalized = documentIds == null ? List.of()
                : documentIds.stream().filter(Objects::nonNull).distinct().toList();
        for (Long documentId : normalized) {
            KnowledgeDocumentBO document = requireDocument(documentId);
            if (!Objects.equals(point.getSpaceId(), document.getSpaceId())) {
                throw new ServiceException("知识点和文档不属于同一知识空间");
            }
        }
        relationRepository.deleteByKnowledgeId(point.getSpaceId(), pointId);
        if (normalized.isEmpty()) {
            return;
        }
        relationRepository.insertBatch(normalized.stream().map(documentId -> {
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
    public List<Long> listPointIds(Long documentId) {
        KnowledgeDocumentBO document = requireDocument(documentId);
        if (document.getSpaceId() != null) {
            spaceService.requireAccess(document.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER);
        }
        return relationRepository.selectByDocId(document.getSpaceId(), documentId).stream()
                .map(KnowledgeDocRelationBO::getKnowledgeId)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replacePoints(Long documentId, List<Long> pointIds, String relationType) {
        KnowledgeDocumentBO document = requireDocument(documentId);
        Long spaceId = document.getSpaceId();
        if (spaceId != null) {
            spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.EDITOR);
        }
        List<Long> normalized = pointIds == null ? List.of()
                : pointIds.stream().filter(Objects::nonNull).distinct().toList();
        for (Long pointId : normalized) {
            KnowledgeBO point = requirePoint(pointId);
            if (!Objects.equals(spaceId, point.getSpaceId())) {
                throw new ServiceException("知识点和文档不属于同一知识空间");
            }
        }
        relationRepository.deleteByDocId(spaceId, documentId);
        if (normalized.isEmpty()) {
            return;
        }
        relationRepository.insertBatch(normalized.stream().map(pointId -> {
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
    public void removeDocumentRelations(Long documentId) {
        KnowledgeDocumentBO document = requireDocument(documentId);
        relationRepository.deleteByDocId(documentId);
        if (document.getTenantId() != null) {
            documentRelationRepository.deleteByDocument(document.getTenantId(), documentId);
        }
    }

    @Override
    public List<DocumentRelationView> listDocumentRelations(Long documentId) {
        KnowledgeDocumentBO source = requireDocument(documentId);
        if (source.getSpaceId() == null) {
            return List.of();
        }
        spaceService.requireAccess(source.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER);
        return documentRelationRepository.selectBySource(source.getSpaceId(), documentId).stream()
                .map(this::toDocumentRelationView).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceDocumentRelations(Long documentId, List<DocumentRelationRequest> relations) {
        KnowledgeDocumentBO source = requireDocument(documentId);
        if (source.getSpaceId() == null || source.getTenantId() == null) {
            throw new ServiceException("文档未绑定知识空间");
        }
        spaceService.requireAccess(source.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR);
        List<DocumentRelationRequest> normalized = relations == null ? List.of() : relations.stream()
                .filter(Objects::nonNull).filter(r -> r.documentId() != null).toList();
        List<KnowledgeDocumentRelationBO> records = normalized.stream().map(request -> {
            if (Objects.equals(documentId, request.documentId())) {
                throw new ServiceException("文档不能关联自身");
            }
            KnowledgeDocumentBO target = requireDocument(request.documentId());
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
        documentRelationRepository.replace(source.getTenantId(), source.getSpaceId(), documentId, records);
    }

    private DocumentRelationView toDocumentRelationView(KnowledgeDocumentRelationBO relation) {
        KnowledgeDocumentBO target = documentRepository.selectById(relation.getTargetDocumentId());
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

    private KnowledgeBO requirePoint(Long pointId) {
        KnowledgeBO point = knowledgeRepository.findById(pointId);
        if (point == null || point.getSpaceId() == null) {
            throw new ServiceException("知识点不存在: " + pointId);
        }
        return point;
    }

    private KnowledgeDocumentBO requireDocument(Long documentId) {
        KnowledgeDocumentBO document = documentRepository.selectById(documentId);
        if (document == null) {
            throw new ServiceException("文档不存在: " + documentId);
        }
        return document;
    }

    private DocumentSummary toSummary(KnowledgeDocumentBO document) {
        return new DocumentSummary(document.getId(), document.getSpaceId(),
                document.getTitle(), document.getDocType(),
                document.getLifecycleStatus(), document.getParseStatus());
    }
}
