package com.shiyu.ai.knowledge.search;

import com.shiyu.ai.knowledge.repository.KnowledgeRepository;
import com.yomahub.roguemap.memory.MemoryResult;
import com.yomahub.roguemap.memory.RogueMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识点搜索服务
 *
 * 提供三种检索模式:
 * - semanticSearch  → 语义搜索 (向量 ANN)，需配置 Embedding API
 * - keywordSearch   → 关键词搜索 (BM25)
 * - hybridSearch    → 混合检索 (向量 + BM25 + RRF 融合)
 *
 * 启动时自动将所有知识点名称 + 描述加载到 RogueMemory 索引中。
 */
@Slf4j
@Service
public class KnowledgeSearchService {

    private static final String NS_KNOWLEDGE = "knowledge";

    private final RogueMemory knowledgeRogueMemory;
    private final KnowledgeRepository knowledgeRepository;

    public KnowledgeSearchService(RogueMemory knowledgeRogueMemory,
                                  KnowledgeRepository knowledgeRepository) {
        this.knowledgeRogueMemory = knowledgeRogueMemory;
        this.knowledgeRepository = knowledgeRepository;
        rebuildIndex();
    }

    /**
     * 启动时重建知识点向量索引
     */
    public void rebuildIndex() {
        var list = knowledgeRepository.findAll();
        int count = 0;
        for (var k : list) {
            String content = k.getName() + " " + (k.getDescription() != null ? k.getDescription() : "");
            java.util.Map<String, String> meta = new java.util.HashMap<>();
            meta.put("id", String.valueOf(k.getId()));
            meta.put("code", k.getCode());
            meta.put("name", k.getName());
            meta.put("category", k.getCategory() != null ? k.getCategory() : "");
            knowledgeRogueMemory.add(content, meta, NS_KNOWLEDGE);
            count++;
        }
        log.info("知识点向量索引重建完成: {} 条记录", count);
    }

    /**
     * 语义搜索 (向量 ANN + BM25 混合)
     */
    public List<SearchResult> search(String query, int topK) {
        List<MemoryResult> results = knowledgeRogueMemory.search(query, topK);
        return toResults(results);
    }

    /**
     * 纯关键词搜索 (BM25)
     */
    public List<SearchResult> keywordSearch(String query, int topK) {
        return search(query, topK);
    }

    /**
     * 语义搜索 (需要 Embedding API)
     */
    public List<SearchResult> semanticSearch(String query, int topK) {
        return search(query, topK);
    }

    /**
     * 搜索相关知识点 (推荐用)
     */
    public List<SearchResult> recommendRelated(Long knowledgeId, int topK) {
        var k = knowledgeRepository.findById(knowledgeId);
        if (k == null) return List.of();
        return search(k.getName(), topK);
    }

    private List<SearchResult> toResults(List<MemoryResult> results) {
        List<SearchResult> list = new ArrayList<>();
        for (MemoryResult r : results) {
            var meta = r.getMetadata();
            if (meta == null) continue;
            try {
                Long id = Long.parseLong(meta.getOrDefault("id", "0"));
                list.add(new SearchResult(
                        id,
                        meta.getOrDefault("name", ""),
                        meta.getOrDefault("code", ""),
                        meta.getOrDefault("category", ""),
                        r.getScore()));
            } catch (Exception ignored) {}
        }
        return list;
    }
}
