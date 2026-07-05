package com.shiyu.ai.knowledge.search;

import com.shiyu.ai.core.embedding.EmbeddingService;
import com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDO;
import com.shiyu.ai.knowledge.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.vector.VectorRecord;
import com.shiyu.ai.knowledge.vector.VectorStore;
import com.yomahub.roguemap.memory.MemoryResult;
import com.yomahub.roguemap.memory.RogueMemory;
import com.yomahub.roguemap.memory.SearchOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Service
public class KnowledgeSearchService {

    private static final String NS_KNOWLEDGE = "knowledge";
    private static final String VS_ID_PREFIX = "kp_";

    private final Map<SearchMode, RogueMemory> memoryMap = new EnumMap<>(SearchMode.class);
    private final KnowledgeRepository knowledgeRepository;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;

    public KnowledgeSearchService(
            @Autowired(required = false) @Qualifier("knowledgeKeywordMemory") RogueMemory keywordMemory,
            @Autowired(required = false) @Qualifier("knowledgeSemanticMemory") RogueMemory semanticMemory,
            @Autowired(required = false) @Qualifier("knowledgeHybridMemory") RogueMemory hybridMemory,
            KnowledgeRepository knowledgeRepository,
            @Autowired(required = false) EmbeddingService embeddingService,
            @Autowired(required = false) VectorStore vectorStore) {

        if (keywordMemory != null) {
            memoryMap.put(SearchMode.KEYWORD, keywordMemory);
            log.info("关键词搜索实例已加载");
        }
        if (semanticMemory != null) {
            memoryMap.put(SearchMode.SEMANTIC, semanticMemory);
            log.info("语义搜索实例已加载");
        }
        if (hybridMemory != null) {
            memoryMap.put(SearchMode.HYBRID, hybridMemory);
            log.info("混合检索实例已加载");
        }

        this.knowledgeRepository = knowledgeRepository;
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;

        if (memoryMap.isEmpty() && vectorStore == null) {
            log.error("没有可用的搜索实例，请检查配置");
        } else {
            log.info("搜索服务初始化完成，可用模式: {}, VectorStore={}",
                    memoryMap.keySet(), vectorStore != null ? "可用" : "不可用");
        }
    }

    public void rebuildIndex() {
        rebuildIndexWithProgress(null);
    }

    public int rebuildIndexWithProgress(Consumer<Integer> progressCallback) {
        var list = knowledgeRepository.findAll();
        int count = 0;
        int total = list.size();

        for (var k : list) {
            indexKnowledge(k);
            count++;

            if (progressCallback != null && (count % 10 == 0 || count == total)) {
                int progress = (count * 100) / total;
                progressCallback.accept(progress);
            }
        }

        log.info("知识点索引重建完成: {} 条记录", count);
        return count;
    }

    public List<SearchResult> search(String query, int topK, SearchMode mode) {
        if (mode == SearchMode.VECTOR) {
            return vectorSearch(query, topK);
        }

        RogueMemory memory = memoryMap.get(mode);
        if (memory == null) {
            log.warn("搜索模式 {} 不可用，尝试降级", mode);
            memory = getFallbackMemory(mode);
        }

        var opts = SearchOptions.builder().namespace(NS_KNOWLEDGE).build();
        List<MemoryResult> results = memory.search(query, topK, opts);
        return toResults(results);
    }

    public List<SearchResult> search(String query, int topK) {
        return search(query, topK, SearchMode.HYBRID);
    }

    public List<SearchResult> keywordSearch(String query, int topK) {
        return search(query, topK, SearchMode.KEYWORD);
    }

    public List<SearchResult> semanticSearch(String query, int topK) {
        return search(query, topK, SearchMode.SEMANTIC);
    }

    public List<SearchResult> vectorSearch(String query, int topK) {
        if (vectorStore == null || embeddingService == null) {
            log.warn("VectorStore 或 EmbeddingService 不可用，VECTOR 模式不可用");
            return List.of();
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
            } catch (Exception ignored) {}
        }
        return list;
    }

    public List<SearchResult> recommendRelated(Long knowledgeId, int topK) {
        var k = knowledgeRepository.findById(knowledgeId);
        if (k == null) return List.of();
        return search(k.getName(), topK);
    }

    public Set<SearchMode> getAvailableModes() {
        Set<SearchMode> modes = new HashSet<>(memoryMap.keySet());
        if (vectorStore != null) {
            modes.add(SearchMode.VECTOR);
        }
        return modes;
    }

    public void clearIndex() {
        for (var entry : memoryMap.entrySet()) {
            try {
                entry.getValue().deleteByNamespace(NS_KNOWLEDGE);
                log.info("已清理 {} 索引", entry.getKey());
            } catch (Exception e) {
                log.error("清理 {} 索引失败", entry.getKey(), e);
            }
        }
        if (vectorStore != null) {
            vectorStore.rebuild();
            log.info("VectorStore 索引已清理");
        }
    }

    public void removeFromIndex(Long id) {
        String idStr = String.valueOf(id);
        for (var entry : memoryMap.entrySet()) {
            try {
                entry.getValue().delete(idStr);
            } catch (Exception e) {
                log.error("从 {} 移除索引失败: id={}", entry.getKey(), id, e);
            }
        }
        if (vectorStore != null) {
            try {
                vectorStore.delete(VS_ID_PREFIX + idStr);
            } catch (Exception e) {
                log.error("从 VectorStore 移除索引失败: id={}", id, e);
            }
        }
        log.info("已从搜索索引移除知识点: id={}", id);
    }

    private RogueMemory getFallbackMemory(SearchMode requestedMode) {
        if (memoryMap.containsKey(SearchMode.KEYWORD)) {
            log.info("降级到 KEYWORD 模式");
            return memoryMap.get(SearchMode.KEYWORD);
        } else if (memoryMap.containsKey(SearchMode.HYBRID)) {
            log.info("降级到 HYBRID 模式");
            return memoryMap.get(SearchMode.HYBRID);
        } else if (memoryMap.containsKey(SearchMode.SEMANTIC)) {
            log.info("降级到 SEMANTIC 模式");
            return memoryMap.get(SearchMode.SEMANTIC);
        }
        throw new IllegalStateException("没有可用的搜索模式");
    }

    public void indexKnowledge(KnowledgeDO knowledgeDO) {
        String id = String.valueOf(knowledgeDO.getId());
        String content = knowledgeDO.getName() + " " +
                (knowledgeDO.getDescription() != null ? knowledgeDO.getDescription() : "");

        Map<String, String> meta = new HashMap<>();
        meta.put("id", id);
        meta.put("code", knowledgeDO.getCode());
        meta.put("name", knowledgeDO.getName());
        meta.put("category", knowledgeDO.getCategory() != null ? knowledgeDO.getCategory() : "");

        // Deduplicate memory instances to avoid indexing twice if KEYWORD and HYBRID share the same bean
        Set<RogueMemory> deduped = new HashSet<>();
        for (var entry : memoryMap.entrySet()) {
            if (!deduped.add(entry.getValue())) {
                log.debug("Skip duplicate memory instance: {} (same as previous)", entry.getKey());
                continue;
            }
            try {
                entry.getValue().add(content, meta, NS_KNOWLEDGE);
            } catch (Exception e) {
                log.error("索引知识点到 {} 失败: id={}, error={}", entry.getKey(), id, e.getMessage());
            }
        }

        if (vectorStore != null && embeddingService != null) {
            try {
                float[] vector = embeddingService.embed(content);
                Map<String, Object> vsMeta = new LinkedHashMap<>(meta);
                vectorStore.upsert(new VectorRecord(VS_ID_PREFIX + id, vector, vsMeta));
            } catch (Exception e) {
                log.error("索引知识点到 VectorStore 失败: id={}", id, e);
            }
        }
    }

    private List<SearchResult> toResults(List<MemoryResult> results) {
        Set<Long> seen = new HashSet<>();
        List<SearchResult> list = new ArrayList<>();
        for (MemoryResult r : results) {
            var meta = r.getMetadata();
            if (meta == null) continue;
            try {
                Long id = Long.parseLong(meta.getOrDefault("id", "0"));
                if (!seen.add(id)) continue;
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
