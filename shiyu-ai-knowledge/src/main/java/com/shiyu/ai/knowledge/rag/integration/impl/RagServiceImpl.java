package com.shiyu.ai.knowledge.rag.integration.impl;

import com.shiyu.ai.knowledge.rag.RagOrchestrator;
import com.shiyu.ai.knowledge.rag.RagOrchestrator.RagResult;
import com.shiyu.ai.knowledge.rag.integration.RagService;
import com.shiyu.ai.knowledge.search.KnowledgeSearchService;
import com.shiyu.ai.knowledge.search.SearchResult;
import com.shiyu.ai.knowledge.search.SearchSource;
import com.shiyu.ai.knowledge.service.DocumentKnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagServiceImpl implements RagService {

    private final KnowledgeSearchService knowledgeSearchService;
    private final DocumentKnowledgeService documentKnowledgeService;
    private final RagOrchestrator ragOrchestrator;

    public RagServiceImpl(KnowledgeSearchService knowledgeSearchService,
                          DocumentKnowledgeService documentKnowledgeService,
                          RagOrchestrator ragOrchestrator) {
        this.knowledgeSearchService = knowledgeSearchService;
        this.documentKnowledgeService = documentKnowledgeService;
        this.ragOrchestrator = ragOrchestrator;
    }

    @Override
    public RagRetrievalResult retrieve(String query, SearchSource source, int topK) {
        log.info("RAG 检索: query=[{}], source=[{}], topK={}", query, source, topK);

        if (query == null || query.trim().isEmpty()) {
            return new RagRetrievalResult(false, List.of(), "查询文本不能为空");
        }

        try {
            return switch (source) {
                case KNOWLEDGE -> retrieveFromKnowledge(query, topK);
                case DOCUMENT -> retrieveFromRagOrchestrator(query, topK);
            };
        } catch (Exception e) {
            log.error("RAG 检索失败", e);
            return new RagRetrievalResult(false, List.of(), "检索失败: " + e.getMessage());
        }
    }

    @Override
    public RagRetrievalResult retrieve(String query) {
        return retrieve(query, SearchSource.DOCUMENT, 5);
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

    private RagRetrievalResult retrieveFromRagOrchestrator(String query, int topK) {
        RagResult result = ragOrchestrator.retrieve(query, topK);

        List<Document> documents = result.chunks().stream()
                .map(chunk -> {
                    String content = chunk.content();
                    double score = chunk.score();
                    return new Document(
                            String.valueOf(chunk.metadata().getOrDefault("chunkIndex", "")),
                            content,
                            score,
                            chunk.metadata()
                    );
                })
                .collect(Collectors.toList());

        String graphContext = result.graphContext();
        if (!graphContext.isBlank()) {
            documents.add(new Document(
                    "graph_context", graphContext, 1.0,
                    Map.of("type", "graph_context")));
        }

        log.info("RAG Orchestrator 检索完成: 返回 {} 条 chunk, graphContext={}",
                result.chunks().size(), !graphContext.isBlank());
        return new RagRetrievalResult(true, documents, null);
    }
}
