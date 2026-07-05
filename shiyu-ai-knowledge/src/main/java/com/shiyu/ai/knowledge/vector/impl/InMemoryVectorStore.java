package com.shiyu.ai.knowledge.vector.impl;

import com.shiyu.ai.knowledge.vector.VectorRecord;
import com.shiyu.ai.knowledge.vector.VectorStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
                    double sa = (double) a.metadata().getOrDefault("_score", 0.0);
                    double sb = (double) b.metadata().getOrDefault("_score", 0.0);
                    return Double.compare(sb, sa);
                })
                .limit(topK)
                .toList();
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    @Override
    public void rebuild() {
        store.clear();
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

    private record InternalRecord(String id, float[] vector, Map<String, Object> metadata) {
    }
}
