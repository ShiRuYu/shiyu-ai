package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDO;
import com.shiyu.ai.knowledge.dto.CreateKnowledgeRequest;
import com.shiyu.ai.knowledge.dto.KnowledgePageQuery;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.dto.UpdateKnowledgeRequest;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.domain.GraphNode;
import com.shiyu.ai.knowledge.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgeGraph knowledgeGraph;

    @Override
    public KnowledgeResponse getById(Long id) {
        KnowledgeDO knowledgeDO = knowledgeRepository.findById(id);
        if (knowledgeDO == null) {
            throw new ServiceException("知识点不存在: " + id, 2001);
        }
        return toResponse(knowledgeDO);
    }

    @Override
    public PageData<KnowledgeResponse> page(KnowledgePageQuery query) {
        int offset = (query.getPageNum() - 1) * query.getPageSize();
        List<KnowledgeDO> list = knowledgeRepository.page(offset, query.getPageSize());
        long total = knowledgeRepository.count();
        List<KnowledgeResponse> items = list.stream().map(this::toResponse).toList();
        return new PageData<>(items, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeResponse create(CreateKnowledgeRequest request) {
        if (knowledgeRepository.existsByCode(request.code())) {
            throw new ServiceException("知识点编码已存在: " + request.code(), 2001);
        }
        KnowledgeDO knowledgeDO = new KnowledgeDO();
        knowledgeDO.setCode(request.code());
        knowledgeDO.setName(request.name());
        knowledgeDO.setDescription(request.description());
        knowledgeDO.setDifficulty(request.difficulty());
        knowledgeDO.setCategory(request.category());
        knowledgeDO.setTags(request.tags());
        knowledgeDO.setStatus(1);
        knowledgeRepository.insert(knowledgeDO);

        GraphNode node = GraphNode.of(knowledgeDO.getId(), knowledgeDO.getName(),
                knowledgeDO.getCode());
        knowledgeGraph.addNode(node);

        return toResponse(knowledgeDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UpdateKnowledgeRequest request) {
        KnowledgeDO knowledgeDO = knowledgeRepository.findById(id);
        if (knowledgeDO == null) {
            throw new ServiceException("知识点不存在: " + id, 2001);
        }
        if (request.name() != null) {
            knowledgeDO.setName(request.name());
        }
        if (request.description() != null) {
            knowledgeDO.setDescription(request.description());
        }
        if (request.difficulty() != null) {
            knowledgeDO.setDifficulty(request.difficulty());
        }
        if (request.category() != null) {
            knowledgeDO.setCategory(request.category());
        }
        if (request.tags() != null) {
            knowledgeDO.setTags(request.tags());
        }
        knowledgeRepository.update(knowledgeDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        KnowledgeDO knowledgeDO = knowledgeRepository.findById(id);
        if (knowledgeDO == null) {
            throw new ServiceException("知识点不存在: " + id, 2001);
        }
        knowledgeRepository.deleteById(id);
    }

    private KnowledgeResponse toResponse(KnowledgeDO knowledgeDO) {
        List<Long> parentIds = knowledgeGraph.parents(knowledgeDO.getId());
        List<Long> childIds = knowledgeGraph.children(knowledgeDO.getId());
        return new KnowledgeResponse(
                knowledgeDO.getId(),
                knowledgeDO.getCode(),
                knowledgeDO.getName(),
                knowledgeDO.getDescription(),
                knowledgeDO.getDifficulty(),
                knowledgeDO.getCategory(),
                knowledgeDO.getTags(),
                parentIds,
                childIds
        );
    }
}
