package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import java.util.Objects;
import com.shiyu.ai.common.core.api.PageQuery;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeBO;
import com.shiyu.ai.knowledge.dto.CreateKnowledgeRequest;
import com.shiyu.ai.knowledge.dto.KnowledgeGraphResponse;
import com.shiyu.ai.knowledge.dto.KnowledgePageQuery;
import com.shiyu.ai.knowledge.dto.KnowledgeDocumentDTO;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.dto.UpdateKnowledgeRequest;
import com.shiyu.ai.knowledge.graph.KnowledgeGraph;
import com.shiyu.ai.knowledge.domain.GraphNode;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.search.KnowledgeSearchService;
import com.shiyu.ai.knowledge.service.DocumentKnowledgeService;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
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
    private final KnowledgeSearchService knowledgeSearchService;
    private final KnowledgeRelationService knowledgeRelationService;
    private final DocumentKnowledgeService documentKnowledgeService;

    @Override
    public KnowledgeResponse getById(Long id) {
        KnowledgeBO knowledgeDO = knowledgeRepository.findById(id);
        if (knowledgeDO == null) {
            throw new ServiceException("知识点不存在: " + id, 2001);
        }
        return toResponse(knowledgeDO);
    }

    @Override
    public PageData<KnowledgeResponse> page(KnowledgePageQuery query) {
        int pageSize = Objects.requireNonNullElse(query.getPageSize(), PageQuery.DEFAULT_PAGE_SIZE);
        int pageNum = Objects.requireNonNullElse(query.getPageNum(), PageQuery.DEFAULT_PAGE_NUM);
        int offset = (pageNum - 1) * pageSize;
        List<KnowledgeBO> list = knowledgeRepository.page(offset, pageSize,
                query.getCategory(), query.getKeyword());
        long total = knowledgeRepository.count(query.getCategory(), query.getKeyword());
        List<KnowledgeResponse> items = list.stream().map(this::toResponse).toList();
        return new PageData<>(items, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeResponse create(CreateKnowledgeRequest request) {
        if (knowledgeRepository.existsByCode(request.code())) {
            throw new ServiceException("知识点编码已存在: " + request.code(), 2001);
        }
        KnowledgeBO knowledgeDO = new KnowledgeBO();
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

        indexKnowledge(knowledgeDO);

        return toResponse(knowledgeDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UpdateKnowledgeRequest request) {
        KnowledgeBO knowledgeDO = knowledgeRepository.findById(id);
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
        // 更新搜索索引
        indexKnowledge(knowledgeDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        KnowledgeBO knowledgeDO = knowledgeRepository.findById(id);
        if (knowledgeDO == null) {
            throw new ServiceException("知识点不存在: " + id, 2001);
        }
        // 清理搜索索引
        knowledgeSearchService.removeFromIndex(id);
        // 清理图谱
        knowledgeGraph.removeNode(id);
        // 清理关联关系
        knowledgeRelationService.removeAllRelations(id);
        // 清理关联文档
        documentKnowledgeService.deleteByKnowledgeId(id);
        knowledgeRepository.deleteById(id);
    }

    @Override
    public KnowledgeGraphResponse getGraph(Long id) {
        KnowledgeResponse node = getById(id);
        List<KnowledgeResponse> parentNodes = knowledgeRelationService.getPrerequisites(id);
        List<KnowledgeResponse> childNodes = knowledgeRelationService.getSubsequent(id);
        List<KnowledgeResponse> relatedNodes = knowledgeRelationService.getRelated(id);
        return new KnowledgeGraphResponse(node, parentNodes, childNodes, relatedNodes);
    }

    private void indexKnowledge(KnowledgeBO knowledgeDO) {
        knowledgeSearchService.indexKnowledge(knowledgeDO);
    }

    private KnowledgeResponse toResponse(KnowledgeBO knowledgeDO) {
        List<Long> parentIds = knowledgeGraph.parents(knowledgeDO.getId());
        List<Long> childIds = knowledgeGraph.children(knowledgeDO.getId());

        // 查询关联文档，限制返回前 10 个
        List<KnowledgeDocumentDTO> docs =
                documentKnowledgeService.searchByKnowledgeId(knowledgeDO.getId())
                        .stream()
                        .limit(10)
                        .map(vo -> new KnowledgeDocumentDTO(
                                vo.id(), vo.title(), vo.content(),
                                vo.docType(), vo.source(), vo.knowledgeIds()))
                        .toList();

        return new KnowledgeResponse(
                knowledgeDO.getId(),
                knowledgeDO.getCode(),
                knowledgeDO.getName(),
                knowledgeDO.getDescription(),
                knowledgeDO.getDifficulty(),
                knowledgeDO.getCategory(),
                knowledgeDO.getTags(),
                parentIds,
                childIds,
                docs
        );
    }
}
