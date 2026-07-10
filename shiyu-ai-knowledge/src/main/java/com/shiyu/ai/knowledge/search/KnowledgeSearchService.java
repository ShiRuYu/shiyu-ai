package com.shiyu.ai.knowledge.search;

import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDO;
import com.shiyu.ai.dal.repository.knowledge.KnowledgeRepository;
import com.shiyu.ai.vector.spi.VectorRecord;
import com.shiyu.ai.vector.spi.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeSearchService {

    private static final String VS_ID_PREFIX = "kp_";

    private final KnowledgeRepository knowledgeRepository;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    public KnowledgeSearchService(KnowledgeRepository knowledgeRepository,
                                  EmbeddingService embeddingService,
                                  VectorStore vectorStore) {
        this.knowledgeRepository = knowledgeRepository;
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;

        log.info("搜索服务初始化完成，VectorStore={}", vectorStore != null ? "可用" : "不可用");
    }

    public void rebuildIndex() {
        rebuildIndexWithProgress(null);
    }

    public int rebuildIndexWithProgress(Consumer<Integer> progressCallback) {
        // 以 DB 为权威源，先清空再重建，避免旧数据残留
        if (vectorStore != null) {
            vectorStore.rebuild();
        }
        var list = knowledgeRepository.findAll();
        int count = 0;
        int total = list.size();

        for (var k : list) {
            indexKnowledge(k);
            count++;
            if (progressCallback != null && (count % 10 == 0 || count == total)) {
                progressCallback.accept((count * 100) / total);
            }
        }
        log.info("知识点索引重建完成: {} 条记录", count);
        return count;
    }

    public List<SearchResult> search(String query, int topK) {
        return vectorSearch(query, topK);
    }

    public List<SearchResult> keywordSearch(String query, int topK) {
        // 降级：从 MySQL 做 LIKE 模糊匹配
        var list = knowledgeRepository.searchByName(query, topK);
        return list.stream().map(k -> new SearchResult(
                k.getId(), k.getName(), k.getCode(),
                k.getCategory() != null ? k.getCategory() : "", 0f
        )).collect(Collectors.toList());
    }

    public List<SearchResult> vectorSearch(String query, int topK) {
        if (vectorStore == null || embeddingService == null) {
            log.warn("VectorStore 或 EmbeddingService 不可用，降级到 keyword 搜索");
            return keywordSearch(query, topK);
        }

        float[] queryVector = embeddingService.embed(query);
        List<VectorRecord> results = vectorStore.search(queryVector, topK);

        List<SearchResult> list = new ArrayList<>();
        for (VectorRecord r : results) {
            if (!r.id().startsWith(VS_ID_PREFIX)) continue;
            try {
                Long id = Long.parseLong(r.id().substring(VS_ID_PREFIX.length()));
                String name = (String) r.metadata().getOrDefault("name", "");
                String code = (String) r.metadata().getOrDefault("code", "");
                String category = (String) r.metadata().getOrDefault("category", "");
                double score = (double) r.metadata().getOrDefault("_score", 0.0);
                list.add(new SearchResult(id, name, code, category, (float) score));
            } catch (Exception e) {
            log.warn("知识搜索异常: {}", e.getMessage());
        }
        }
        return list;
    }

    public List<SearchResult> recommendRelated(Long knowledgeId, int topK) {
        var k = knowledgeRepository.findById(knowledgeId);
        if (k == null) return List.of();
        return search(k.getName(), topK);
    }

    public void clearIndex() {
        if (vectorStore != null) {
            vectorStore.rebuild();
            log.info("VectorStore 索引已清理");
        }
    }

    public void removeFromIndex(Long id) {
        if (vectorStore != null) {
            try {
                vectorStore.delete(VS_ID_PREFIX + id);
            } catch (Exception e) {
                log.error("从 VectorStore 移除索引失败: id={}", id, e);
            }
        }
        log.info("已从搜索索引移除知识点: id={}", id);
    }
    public void indexKnowledge(KnowledgeDO knowledgeDO) {
        String id = String.valueOf(knowledgeDO.getId());
        String content = knowledgeDO.getName() + " " +
                (knowledgeDO.getDescription() != null ? knowledgeDO.getDescription() : "");

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("id", id);
        meta.put("code", knowledgeDO.getCode());
        meta.put("name", knowledgeDO.getName());
        meta.put("category", knowledgeDO.getCategory() != null ? knowledgeDO.getCategory() : "");

        if (vectorStore != null && embeddingService != null) {
            try {
                float[] vector = embeddingService.embed(content);
                vectorStore.upsert(new VectorRecord(VS_ID_PREFIX + id, vector, meta));
            } catch (Exception e) {
                log.error("索引知识点到 VectorStore 失败: id={}", id, e);
            }
        }
    }
}
