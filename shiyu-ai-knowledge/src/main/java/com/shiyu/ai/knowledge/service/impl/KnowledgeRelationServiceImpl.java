package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDO;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeRelationDO;
import com.shiyu.ai.knowledge.domain.RelationType;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.repository.KnowledgeRelationRepository;
import com.shiyu.ai.knowledge.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeRelationServiceImpl implements KnowledgeRelationService {

    private final KnowledgeRelationRepository relationRepository;
    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgeGraph knowledgeGraph;

    @Override
    public List<KnowledgeResponse> getPrerequisites(Long knowledgeId) {
        List<KnowledgeRelationDO> relations = relationRepository.findBySourceIdAndType(knowledgeId, RelationType.PRE.name());
        return relations.stream()
                .map(r -> knowledgeRepository.findById(r.getTargetId()))
                .filter(k -> k != null)
                .map(this::toSimpleResponse)
                .toList();
    }

    @Override
    public List<KnowledgeResponse> getSubsequent(Long knowledgeId) {
        List<KnowledgeRelationDO> relations = relationRepository.findByTargetIdAndType(knowledgeId, RelationType.PRE.name());
        return relations.stream()
                .map(r -> knowledgeRepository.findById(r.getSourceId()))
                .filter(k -> k != null)
                .map(this::toSimpleResponse)
                .toList();
    }

    @Override
    public List<KnowledgeResponse> getRelated(Long knowledgeId) {
        List<KnowledgeRelationDO> relations = relationRepository.findBySourceIdAndType(knowledgeId, RelationType.RELATED.name());
        return relations.stream()
                .map(r -> knowledgeRepository.findById(r.getTargetId()))
                .filter(k -> k != null)
                .map(this::toSimpleResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRelation(Long sourceId, Long targetId, RelationType type, Double weight) {
        KnowledgeDO source = knowledgeRepository.findById(sourceId);
        KnowledgeDO target = knowledgeRepository.findById(targetId);
        if (source == null || target == null) {
            throw new ServiceException("知识点不存在", 2001);
        }

        KnowledgeRelationDO relation = new KnowledgeRelationDO();
        relation.setSourceId(sourceId);
        relation.setTargetId(targetId);
        relation.setRelationType(type.name());
        relation.setWeight(weight != null ? weight : 1.0);
        relation.setCreateTime(LocalDateTime.now());
        relationRepository.insert(relation);

        knowledgeGraph.addEdge(sourceId, targetId, type.name(), relation.getWeight());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRelation(Long sourceId, Long targetId, RelationType type) {
        relationRepository.deleteBySourceAndTargetAndType(sourceId, targetId, type.name());
        knowledgeGraph.removeEdge(sourceId, targetId, type.name());
    }

    private KnowledgeResponse toSimpleResponse(KnowledgeDO k) {
        return new KnowledgeResponse(
                k.getId(), k.getCode(), k.getName(), k.getSubjectCode(),
                k.getGrade(), k.getGradeLevel(), k.getDescription(),
                k.getDifficulty(), k.getEstimatedTime(), k.getSuitableAge(),
                Collections.emptyList(), Collections.emptyList()
        );
    }
}
