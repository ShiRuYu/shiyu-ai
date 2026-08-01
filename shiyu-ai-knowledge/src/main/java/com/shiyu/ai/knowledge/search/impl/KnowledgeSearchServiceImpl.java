package com.shiyu.ai.knowledge.search.impl;

import com.shiyu.ai.dal.knowledge.bo.KnowledgeBO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeRepository;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.knowledge.search.KnowledgeSearchService;
import com.shiyu.ai.knowledge.search.SearchResult;
import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.vector.VectorRecord;
import com.shiyu.ai.vector.VectorSearchRequest;
import com.shiyu.ai.vector.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeSearchServiceImpl implements KnowledgeSearchService {

    private static final String VS_ID_PREFIX = "kp_";

    private final KnowledgeRepository knowledgeRepository;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    public KnowledgeSearchServiceImpl(KnowledgeRepository knowledgeRepository,
                                      EmbeddingService embeddingService,
                                      VectorStore vectorStore) {
        this.knowledgeRepository = knowledgeRepository;
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        log.info("搜索服务初始化完成，VectorStore={}", vectorStore != null ? "可用" : "不可用");
    }

    @Override
    public void rebuildIndex() {
        rebuildIndexWithProgress(null);
    }

    @Override
    public int rebuildIndexWithProgress(Consumer<Integer> progressCallback) {
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

    @Override
    public List<SearchResult> search(String query, int topK) {
        return vectorSearch(query, topK);
    }

    @Override
    public List<SearchResult> keywordSearch(String query, int topK) {
        var list = knowledgeRepository.searchByName(query, topK);
        return list.stream().map(k -> new SearchResult(
                k.getId(), k.getName(), k.getCode(),
                k.getCategory() != null ? k.getCategory() : "", 0f
        )).collect(Collectors.toList());
    }

    @Override
    public List<SearchResult> vectorSearch(String query, int topK) {
        if (vectorStore == null || embeddingService == null) {
            log.warn("VectorStore 或 EmbeddingService 不可用，降级到 keyword 搜索");
            return keywordSearch(query, topK);
        }

        float[] queryVector = embeddingService.embed(query);
        Long tenantId = LoginContextHolder.getCurrentTenantId();
        if (tenantId == null) return List.of();
        List<VectorRecord> results = vectorStore.search(VectorSearchRequest.builder()
                .queryVector(queryVector)
                .topK(topK)
                .filter(Map.of("tenantId", tenantId))
                .build());

        List<SearchResult> list = new ArrayList<>();
        for (VectorRecord r : results) {
            if (!r.id().startsWith(VS_ID_PREFIX)) continue;
            try {
                String[] idParts = r.id().substring(VS_ID_PREFIX.length()).split("_", 2);
                Long id = Long.parseLong(idParts.length == 2 ? idParts[1] : idParts[0]);
                String name = (String) r.metadata().getOrDefault("name", "");
                String code = (String) r.metadata().getOrDefault("code", "");
                String category = (String) r.metadata().getOrDefault("category", "");
                double score = 0.0;
                Object scoreObj = r.metadata().get("_score");
                if (scoreObj instanceof Number n) score = n.doubleValue();
                list.add(new SearchResult(id, name, code, category, (float) score));
            } catch (Exception e) {
                log.warn("知识搜索异常: {}", e.getMessage());
            }
        }
        return list;
    }

    @Override
    public List<SearchResult> recommendRelated(Long knowledgeId, int topK) {
        var k = knowledgeRepository.findById(knowledgeId);
        if (k == null) return List.of();
        return search(k.getName(), topK);
    }

    @Override
    public void clearIndex() {
        if (vectorStore != null) {
            vectorStore.rebuild();
            log.info("VectorStore 索引已清理");
        }
    }

    @Override
    public void removeFromIndex(Long id) {
        if (vectorStore != null) {
            try {
                KnowledgeBO knowledge = knowledgeRepository.findById(id);
                Long tenantId = knowledge == null ? LoginContextHolder.getCurrentTenantId()
                        : knowledge.getTenantId();
                if (tenantId != null) vectorStore.delete(vectorId(tenantId, id));
            } catch (Exception e) {
                log.error("从 VectorStore 移除索引失败: id={}", id, e);
            }
        }
        log.info("已从搜索索引移除知识点: id={}", id);
    }

    @Override
    public void indexKnowledge(KnowledgeBO knowledgeDO) {
        Long tenantId = knowledgeDO.getTenantId() != null
                ? knowledgeDO.getTenantId() : LoginContextHolder.getCurrentTenantId();
        if (tenantId == null) return;
        String id = vectorId(tenantId, knowledgeDO.getId());
        String content = knowledgeDO.getName() + " " +
                (knowledgeDO.getDescription() != null ? knowledgeDO.getDescription() : "");

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("id", id);
        meta.put("code", knowledgeDO.getCode());
        meta.put("name", knowledgeDO.getName());
        meta.put("category", knowledgeDO.getCategory() != null ? knowledgeDO.getCategory() : "");
        meta.put("tenantId", tenantId);

        if (vectorStore != null && embeddingService != null) {
            try {
                float[] vector = embeddingService.embed(content);
                vectorStore.upsert(new VectorRecord(id, vector, meta));
            } catch (Exception e) {
                log.error("索引知识点到 VectorStore 失败: id={}", id, e);
            }
        }
    }

    private String vectorId(Long tenantId, Long knowledgeId) {
        return VS_ID_PREFIX + tenantId + "_" + knowledgeId;
    }
}
