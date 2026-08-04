package com.shiyu.ai.memory.spi.impl;

import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;
import com.shiyu.ai.memory.spi.MemoryStore;
import com.shiyu.ai.memory.spi.MemoryType;
import com.shiyu.ai.model.embedding.EmbeddingService;
import com.shiyu.ai.vector.VectorRecord;
import com.shiyu.ai.vector.VectorSearchRequest;
import com.shiyu.ai.vector.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 语义记忆 — 基于向量检索
 *
 * <p>使用 {@link VectorStore} 进行向量化存储和相似度搜索，
 * {@link EmbeddingService} 负责文本到向量的转换。
 * 当 VectorStore 或 EmbeddingService 不可用时退化为空实现。</p>
 *
 * <p>语义记忆长期存储 Agent 积累的经验、知识 chunk，
 * 并通过向量相似度注入到长期记忆中（SEM → LTM）。</p>
 */
public class SemanticMemoryStore implements MemoryStore {

    private static final Logger log = LoggerFactory.getLogger(SemanticMemoryStore.class);

    private static final String COLLECTION_NAME = "semantic_memory";

    private final VectorStore vectorStore;
    private final EmbeddingService embeddingService;
    private final boolean available;

    public SemanticMemoryStore(VectorStore vectorStore, EmbeddingService embeddingService) {
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
        this.available = vectorStore != null && embeddingService != null;
        if (!this.available) {
            log.warn("SemanticMemoryStore 不可用: VectorStore={}, EmbeddingService={}",
                    vectorStore != null, embeddingService != null);
        }
    }

    @Override
    public void save(Memory memory) {
        if (!available || memory.getContent() == null || memory.getContent().isBlank()) return;

        try {
            float[] vector = embeddingService.embed(memory.getContent());
            String id = memory.getMemoryId() != null ? memory.getMemoryId() : UUID.randomUUID().toString();
            Map<String, Object> metadata = new HashMap<>(memory.getMetadata());
            metadata.put("content", memory.getContent());
            metadata.put("category", memory.getCategory() != null ? memory.getCategory() : "general");
            metadata.put("userId", memory.getUserId());
            metadata.put("agentId", memory.getAgentId());
            metadata.put("importance", memory.getImportance());
            metadata.put("source", memory.getSource() != null ? memory.getSource() : "");
            metadata.put("collection", COLLECTION_NAME);

            VectorRecord record = new VectorRecord(id, vector, metadata);
            vectorStore.upsert(record);
            memory.setMemoryId(id);
            log.debug("语义记忆已保存: id={}, contentLen={}", id, memory.getContent().length());
        } catch (Exception e) {
            log.warn("语义记忆保存失败: {}", e.getMessage());
        }
    }

    @Override
    public void saveBatch(List<Memory> memories) {
        if (!available) return;
        for (Memory memory : memories) {
            save(memory);
        }
    }

    @Override
    public List<Memory> query(MemoryQuery query) {
        if (!available) return List.of();

        String keyword = query.getKeyword();
        if (keyword == null || keyword.isBlank()) {
            // 无关键词时返回空 — 语义记忆必须通过语义搜索
            return List.of();
        }

        try {
            float[] queryVector = embeddingService.embed(keyword);

            // 构建过滤条件
            Map<String, Object> filter = new HashMap<>();
            filter.put("collection", COLLECTION_NAME);
            if (query.getUserId() != null) {
                filter.put("userId", query.getUserId());
            }
            if (query.getAgentId() != null) {
                filter.put("agentId", query.getAgentId());
            }

            VectorSearchRequest searchRequest = VectorSearchRequest.builder()
                    .queryVector(queryVector)
                    .topK(query.getTopK())
                    .filter(filter)
                    .minScore(query.getMinImportance())
                    .build();

            List<VectorRecord> results = vectorStore.search(searchRequest);
            return results.stream().map(this::toMemory).collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("语义记忆检索失败: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public Memory queryById(String memoryId) {
        return null;
    }

    @Override
    public void delete(String memoryId) {
        if (!available) return;
        try {
            vectorStore.delete(memoryId);
        } catch (Exception e) {
            log.warn("语义记忆删除失败: {}", e.getMessage());
        }
    }

    @Override
    public void deleteBySession(String sessionId) {
        // 语义记忆没有会话概念，不支持按会话删除
    }

    @Override
    public long count(MemoryQuery query) {
        return 0;
    }

    private Memory toMemory(VectorRecord record) {
        Memory mem = new Memory();
        mem.setType(MemoryType.SEMANTIC);
        mem.setMemoryId(record.id());
        mem.setContent(record.metadata() != null
                ? (String) record.metadata().getOrDefault("content", "")
                : "");
        mem.setCategory(record.metadata() != null
                ? (String) record.metadata().getOrDefault("category", "general")
                : "general");
        if (record.metadata() != null && record.metadata().get("userId") instanceof Number n) {
            mem.setUserId(n.longValue());
        }
        if (record.metadata() != null && record.metadata().get("agentId") instanceof String s) {
            mem.setAgentId(s);
        }
        if (record.metadata() != null && record.metadata().get("importance") instanceof Number n) {
            mem.setImportance(n.doubleValue());
        }
        if (record.metadata() != null && record.metadata().get("source") instanceof String s) {
            mem.setSource(s);
        }
        mem.setCreatedAt(LocalDateTime.now());
        return mem;
    }
}
