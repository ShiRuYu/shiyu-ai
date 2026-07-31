package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeBO;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeRelationBO;
import com.shiyu.ai.knowledge.domain.RelationType;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeRelationRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeRepository;
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
        List<KnowledgeRelationBO> relations = relationRepository.findBySourceIdAndType(knowledgeId, RelationType.PRE.name());
        return relations.stream()
                .map(r -> knowledgeRepository.findById(r.getTargetId()))
                .filter(k -> k != null)
                .map(this::toSimpleResponse)
                .toList();
    }

    @Override
    public List<KnowledgeResponse> getSubsequent(Long knowledgeId) {
        List<KnowledgeRelationBO> relations = relationRepository.findByTargetIdAndType(knowledgeId, RelationType.PRE.name());
        return relations.stream()
                .map(r -> knowledgeRepository.findById(r.getSourceId()))
                .filter(k -> k != null)
                .map(this::toSimpleResponse)
                .toList();
    }

    @Override
    public List<KnowledgeResponse> getRelated(Long knowledgeId) {
        List<KnowledgeRelationBO> relations = relationRepository.findBySourceIdAndType(knowledgeId, RelationType.RELATED.name());
        return relations.stream()
                .map(r -> knowledgeRepository.findById(r.getTargetId()))
                .filter(k -> k != null)
                .map(this::toSimpleResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRelation(Long sourceId, Long targetId, RelationType type, Double weight) {
        if (sourceId.equals(targetId)) {
            throw new ServiceException("知识关系不能指向自身");
        }
        KnowledgeBO source = knowledgeRepository.findById(sourceId);
        KnowledgeBO target = knowledgeRepository.findById(targetId);
        if (source == null || target == null) {
            throw new ServiceException("知识点不存在", 2001);
        }

        if (!java.util.Objects.equals(source.getSpaceId(), target.getSpaceId())) {
            throw new ServiceException("不能创建跨知识空间关系");
        }
        if (relationRepository.exists(source.getSpaceId(), sourceId, targetId, type.name())) {
            throw new ServiceException("知识关系已存在");
        }
        if (type == RelationType.PRE && !knowledgeGraph.findPath(sourceId, targetId).isEmpty()) {
            throw new ServiceException("前置关系会形成循环依赖");
        }

        KnowledgeRelationBO relation = new KnowledgeRelationBO();
        relation.setSpaceId(source.getSpaceId());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAllRelations(Long knowledgeId) {
        // 1. 先查 DB，保留所有关联关系（先查后删，避免删完查不到）
        var sourceRelations = relationRepository.findBySourceId(knowledgeId);
        var targetRelations = relationRepository.findByTargetId(knowledgeId);

        // 2. 再删 DB
        relationRepository.deleteBySourceIdOrTargetId(knowledgeId);

        // 3. 最后清理内存图的边
        for (var r : sourceRelations) {
            knowledgeGraph.removeEdge(knowledgeId, r.getTargetId(), r.getRelationType());
        }
        for (var r : targetRelations) {
            knowledgeGraph.removeEdge(r.getSourceId(), knowledgeId, r.getRelationType());
        }
        log.info("已移除知识点 {} 的所有关联关系 (source={}, target={})",
                knowledgeId, sourceRelations.size(), targetRelations.size());
    }

    private KnowledgeResponse toSimpleResponse(KnowledgeBO k) {
        return new KnowledgeResponse(
                k.getId(), k.getCode(), k.getName(), k.getDescription(),
                k.getDifficulty(), k.getCategory(), k.getTags(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
        );
    }
}
