package com.shiyu.ai.knowledge.search;

import com.shiyu.ai.knowledge.repository.KnowledgeRepository;
import com.yomahub.roguemap.memory.MemoryResult;
import com.yomahub.roguemap.memory.RogueMemory;
import com.yomahub.roguemap.memory.SearchOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

/**
 * 知识点搜索服务
 * <p>
 * 支持多种检索模式：
 * <ul>
 *   <li>KEYWORD: 关键词搜索 (BM25)</li>
 *   <li>SEMANTIC: 语义搜索 (向量 ANN)，需配置 Embedding API</li>
 *   <li>HYBRID: 混合检索 (向量 + BM25 + RRF 融合)，需配置 Embedding API</li>
 * </ul>
 * <p>
 * 索引由 KnowledgeService 在 CRUD 时同步维护。
 * 可通过 rebuildIndex() 手动触发全量重建（如数据迁移后）。
 */
@Slf4j
@Service
public class KnowledgeSearchService {

    private static final String NS_KNOWLEDGE = "knowledge";

    private final Map<SearchMode, RogueMemory> memoryMap = new EnumMap<>(SearchMode.class);
    private final KnowledgeRepository knowledgeRepository;

    public KnowledgeSearchService(
            @Autowired(required = false) @Qualifier("knowledgeKeywordMemory") RogueMemory keywordMemory,
            @Autowired(required = false) @Qualifier("knowledgeSemanticMemory") RogueMemory semanticMemory,
            @Autowired(required = false) @Qualifier("knowledgeHybridMemory") RogueMemory hybridMemory,
            KnowledgeRepository knowledgeRepository) {

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

        if (memoryMap.isEmpty()) {
            log.error("没有可用的搜索实例，请检查配置");
        } else {
            log.info("搜索服务初始化完成，可用模式: {}", memoryMap.keySet());
        }
    }

    /**
     * 手动重建知识点向量索引（数据迁移或异常后使用）
     */
    public void rebuildIndex() {
        rebuildIndexWithProgress(null);
    }

    /**
     * 带进度回调的索引重建
     *
     * @param progressCallback 进度回调 (0-100)，可为 null
     * @return 重建的索引数量
     */
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

        log.info("知识点向量索引重建完成: {} 条记录", count);
        return count;
    }

    /**
     * 搜索知识点（指定模式）
     */
    public List<SearchResult> search(String query, int topK, SearchMode mode) {
        RogueMemory memory = memoryMap.get(mode);
        if (memory == null) {
            log.warn("请求的搜索模式 {} 不可用，尝试降级", mode);
            memory = getFallbackMemory(mode);
        }

        var opts = SearchOptions.builder().namespace(NS_KNOWLEDGE).build();
        List<MemoryResult> results = memory.search(query, topK, opts);
        return toResults(results);
    }

    /**
     * 搜索知识点（默认使用 HYBRID 模式）
     */
    public List<SearchResult> search(String query, int topK) {
        return search(query, topK, SearchMode.HYBRID);
    }

    /**
     * 纯关键词搜索 (BM25)
     */
    public List<SearchResult> keywordSearch(String query, int topK) {
        return search(query, topK, SearchMode.KEYWORD);
    }

    /**
     * 语义搜索 (需要 Embedding API)
     */
    public List<SearchResult> semanticSearch(String query, int topK) {
        return search(query, topK, SearchMode.SEMANTIC);
    }

    /**
     * 搜索相关知识点 (推荐用)
     */
    public List<SearchResult> recommendRelated(Long knowledgeId, int topK) {
        var k = knowledgeRepository.findById(knowledgeId);
        if (k == null) return List.of();
        return search(k.getName(), topK);
    }

    /**
     * 获取可用的搜索模式
     */
    public Set<SearchMode> getAvailableModes() {
        return memoryMap.keySet();
    }

    /**
     * 清理所有索引
     */
    public void clearIndex() {
        for (var entry : memoryMap.entrySet()) {
            log.info("清理 {} 索引", entry.getKey());
            // RogueMemory 没有直接的 clear 方法，需要重建
            // 这里只是记录日志，实际清理需要删除持久化文件
        }
    }

    /**
     * 降级策略：HYBRID -> KEYWORD -> SEMANTIC
     */
    private RogueMemory getFallbackMemory(SearchMode requestedMode) {
        // 优先级：KEYWORD > HYBRID > SEMANTIC
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

    /**
     * 索引单个知识点到所有启用的实例
     */
    public void indexKnowledge(com.shiyu.ai.dal.dataobject.knowledge.KnowledgeDO knowledgeDO) {
        String id = String.valueOf(knowledgeDO.getId());
        String content = knowledgeDO.getName() + " " +
                (knowledgeDO.getDescription() != null ? knowledgeDO.getDescription() : "");

        Map<String, String> meta = new HashMap<>();
        meta.put("id", id);
        meta.put("code", knowledgeDO.getCode());
        meta.put("name", knowledgeDO.getName());
        meta.put("category", knowledgeDO.getCategory() != null ? knowledgeDO.getCategory() : "");

        // 同步到所有启用的索引
        for (var entry : memoryMap.entrySet()) {
            try {
                entry.getValue().add(content, meta, NS_KNOWLEDGE);
            } catch (Exception e) {
                log.error("索引知识点到 {} 失败: id={}, error={}", entry.getKey(), id, e.getMessage());
            }
        }
        log.debug("知识点索引已更新: id={}, name={}", id, knowledgeDO.getName());
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
