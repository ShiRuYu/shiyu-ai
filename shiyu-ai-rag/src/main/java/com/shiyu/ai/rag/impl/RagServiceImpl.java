package com.shiyu.ai.rag.impl;

import com.shiyu.ai.knowledge.search.KnowledgeSearchService;
import com.shiyu.ai.knowledge.search.SearchResult;
import com.shiyu.ai.knowledge.service.DocumentKnowledgeService;
import com.shiyu.ai.knowledge.service.DocumentKnowledgeService.KnowledgeDocumentVO;
import com.shiyu.ai.rag.RagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 检索服务实现
 * <p>
 * 委托 Knowledge 模块的搜索服务实现检索：
 * - knowledgeBaseId = "knowledge" → KnowledgeSearchService（知识点检索）
 * - 其他值（默认 "document"） → DocumentKnowledgeService（文档检索）
 */
@Slf4j
@Service
public class RagServiceImpl implements RagService {

    private final KnowledgeSearchService knowledgeSearchService;
    private final DocumentKnowledgeService documentKnowledgeService;

    public RagServiceImpl(KnowledgeSearchService knowledgeSearchService,
                          DocumentKnowledgeService documentKnowledgeService) {
        this.knowledgeSearchService = knowledgeSearchService;
        this.documentKnowledgeService = documentKnowledgeService;
    }

    @Override
    public RagRetrievalResult retrieve(String query, String knowledgeBaseId, int topK) {
        log.info("RAG 检索: query=[{}], kb=[{}], topK={}", query, knowledgeBaseId, topK);

        if (query == null || query.trim().isEmpty()) {
            return new RagRetrievalResult(false, List.of(), "查询文本不能为空");
        }

        try {
            if ("knowledge".equals(knowledgeBaseId)) {
                return retrieveFromKnowledge(query, topK);
            } else {
                return retrieveFromDocument(query, topK);
            }
        } catch (Exception e) {
            log.error("RAG 检索失败", e);
            return new RagRetrievalResult(false, List.of(), "检索失败: " + e.getMessage());
        }
    }

    @Override
    public RagRetrievalResult retrieve(String query) {
        return retrieve(query, "document", 5);
    }

    private RagRetrievalResult retrieveFromKnowledge(String query, int topK) {
        List<SearchResult> results = knowledgeSearchService.search(query, topK);
        List<Document> documents = results.stream()
                .map(r -> new Document(
                        String.valueOf(r.getId()),
                        r.getName() + ": " + r.getCode(),
                        (double) r.getScore(),
                        Map.of("type", "knowledge", "category", r.getCategory())
                ))
                .collect(Collectors.toList());

        log.info("知识点检索完成: 返回 {} 条", documents.size());
        return new RagRetrievalResult(true, documents, null);
    }

    private RagRetrievalResult retrieveFromDocument(String query, int topK) {
        List<KnowledgeDocumentVO> results = documentKnowledgeService.search(query, topK);
        List<Document> documents = results.stream()
                .map(vo -> new Document(
                        String.valueOf(vo.id()),
                        vo.content(),
                        1.0,
                        Map.of("type", "document", "title", vo.title(), "docType", vo.docType())
                ))
                .collect(Collectors.toList());

        log.info("文档检索完成: 返回 {} 条", documents.size());
        return new RagRetrievalResult(true, documents, null);
    }
}
