package com.shiyu.ai.vector.spi.impl;

import com.shiyu.ai.vector.spi.VectorRecord;
import com.shiyu.ai.vector.spi.VectorSearchRequest;
import com.shiyu.ai.vector.spi.VectorStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存向量存储 — 基于余弦相似度的暴力搜索
 * 适用于测试和小规模场景
 */
public class InMemoryVectorStore implements VectorStore {

    private final Map<String, InternalRecord> store = new ConcurrentHashMap<>();

    @Override
    public void upsert(VectorRecord record) {
        store.put(record.id(), new InternalRecord(record.id(), record.vector(), record.metadata()));
    }

    @Override
    public List<VectorRecord> search(float[] queryVector, int topK) {
        return store.values().stream()
                .map(r -> {
                    float score = cosineSimilarity(queryVector, r.vector);
                    Map<String, Object> meta = new LinkedHashMap<>(r.metadata);
                    meta.put("_score", score);
                    return new VectorRecord(r.id, r.vector, meta);
                })
                .sorted((a, b) -> {
                    double sa = ((Number) a.metadata().getOrDefault("_score", 0.0)).doubleValue();
                    double sb = ((Number) b.metadata().getOrDefault("_score", 0.0)).doubleValue();
                    return Double.compare(sb, sa);
                })
                .limit(topK)
                .toList();
    }

    @Override
    public List<VectorRecord> search(VectorSearchRequest request) {
        double minScore = request.getMinScore();
        Map<String, Object> filter = request.getFilter();

        return store.values().stream()
                .filter(r -> applyFilter(r, filter))
                .map(r -> {
                    float score = cosineSimilarity(request.getQueryVector(), r.vector);
                    Map<String, Object> meta = new LinkedHashMap<>(r.metadata);
                    meta.put("_score", score);
                    return new VectorRecord(r.id, r.vector, meta);
                })
                .filter(r -> ((Number) r.metadata().get("_score")).doubleValue() >= minScore)
                .sorted((a, b) -> {
                    double sa = ((Number) a.metadata().getOrDefault("_score", 0.0)).doubleValue();
                    double sb = ((Number) b.metadata().getOrDefault("_score", 0.0)).doubleValue();
                    return Double.compare(sb, sa);
                })
                .limit(request.getTopK())
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    @Override
    public void deleteBatch(List<String> ids) {
        ids.forEach(store::remove);
    }

    @Override
    public void rebuild() {
        store.clear();
    }

    @Override
    public int size() {
        return store.size();
    }

    private boolean applyFilter(InternalRecord r, Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) return true;
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            Object value = r.metadata.get(entry.getKey());
            if (!entry.getValue().equals(value)) return false;
        }
        return true;
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10));
    }

    private record InternalRecord(String id, float[] vector, Map<String, Object> metadata) {}
}
