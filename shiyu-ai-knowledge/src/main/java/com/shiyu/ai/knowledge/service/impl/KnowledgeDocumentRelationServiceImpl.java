package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeBO;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocRelationBO;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocumentBO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeDocRelationRepository;
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
    public void replaceDocuments(Long pointId, List<Long> documentIds) {
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
            relation.setRelationType("RELATED");
            relation.setCreateTime(LocalDateTime.now());
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
    public void replacePoints(Long documentId, List<Long> pointIds) {
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
            relation.setRelationType("RELATED");
            relation.setCreateTime(LocalDateTime.now());
            return relation;
        }).toList());
    }

    @Override
    public void removeDocumentRelations(Long documentId) {
        requireDocument(documentId);
        relationRepository.deleteByDocId(documentId);
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
