package com.shiyu.ai.common.vector.implementation;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorSearchRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** 内存向量存储 — 基于余弦相似度的暴力搜索 适用于测试和小规模场景 */
public class InMemoryVectorStore implements VectorStore {

    /**
     * 维度，表示当前对象中的对应属性。
     */
    private final int dimension;
    private final Map<String, InternalRecord> store = new ConcurrentHashMap<>();

    /**
     * {@code InMemoryVectorStore} 创建并初始化当前类型实例。
     */
    public InMemoryVectorStore() {
        this(-1);
    }

    /**
     * {@code InMemoryVectorStore} 创建并初始化当前类型实例。
     *
     * @param dimension 参数值，用于执行当前操作。
     */
    public InMemoryVectorStore(int dimension) {
        this.dimension = dimension;
    }

    /**
     * {@code type} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String type() {
        return "inmemory";
    }

    /**
     * {@code upsert} 执行当前类型定义的业务操作。
     *
     * @param record 参数值，用于执行当前操作。
     */
    @Override
    public void upsert(VectorRecord record) {
        Objects.requireNonNull(record, "Vector record must not be null");
        validateVector(record.vector());
        Map<String, Object> metadata =
                record.metadata() == null ? Map.of() : Map.copyOf(record.metadata());
        store.put(record.id(), new InternalRecord(record.id(), record.vector(), metadata));
    }

    /**
     * {@code search} 查询并返回当前操作所需的数据。
     *
     * @param queryVector 参数值，用于执行当前操作。
     * @param topK 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<VectorRecord> search(float[] queryVector, int topK) {
        if (topK <= 0 || store.isEmpty()) return List.of();
        validateVector(queryVector);
        return store.values().stream()
                .map(
                        r -> {
                            float score = cosineSimilarity(queryVector, r.vector);
                            Map<String, Object> meta = new LinkedHashMap<>(r.metadata);
                            meta.put("_score", score);
                            return new VectorRecord(r.id, r.vector, meta);
                        })
                .sorted(
                        (a, b) -> {
                            double sa =
                                    ((Number) a.metadata().getOrDefault("_score", 0.0))
                                            .doubleValue();
                            double sb =
                                    ((Number) b.metadata().getOrDefault("_score", 0.0))
                                            .doubleValue();
                            return Double.compare(sb, sa);
                        })
                .limit(topK)
                .toList();
    }

    /**
     * {@code search} 查询并返回当前操作所需的数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<VectorRecord> search(VectorSearchRequest request) {
        Objects.requireNonNull(request, "Vector search request must not be null");
        if (request.getTopK() <= 0 || store.isEmpty()) return List.of();
        validateVector(request.getQueryVector());
        double minScore = request.getMinScore();
        Map<String, Object> filter = request.getFilter();

        return store.values().stream()
                .filter(r -> applyFilter(r, filter))
                .map(
                        r -> {
                            float score = cosineSimilarity(request.getQueryVector(), r.vector);
                            Map<String, Object> meta = new LinkedHashMap<>(r.metadata);
                            meta.put("_score", score);
                            return new VectorRecord(r.id, r.vector, meta);
                        })
                .filter(r -> ((Number) r.metadata().get("_score")).doubleValue() >= minScore)
                .sorted(
                        (a, b) -> {
                            double sa =
                                    ((Number) a.metadata().getOrDefault("_score", 0.0))
                                            .doubleValue();
                            double sb =
                                    ((Number) b.metadata().getOrDefault("_score", 0.0))
                                            .doubleValue();
                            return Double.compare(sb, sa);
                        })
                .limit(request.getTopK())
                .collect(Collectors.toList());
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void delete(String id) {
        store.remove(id);
    }

    /**
     * {@code deleteBatch} 释放或移除当前操作涉及的资源。
     *
     * @param ids 参数值，用于执行当前操作。
     */
    @Override
    public void deleteBatch(List<String> ids) {
        ids.forEach(store::remove);
    }

    /**
     * {@code rebuild} 执行当前类型定义的业务操作。
     */
    @Override
    public void rebuild() {
        store.clear();
    }

    /**
     * {@code size} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int size() {
        return store.size();
    }

    private boolean applyFilter(InternalRecord r, Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) return true;
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            Object value = r.metadata.get(entry.getKey());
            Object expected = entry.getValue();
            if (Objects.equals(expected, value)) continue;
            if (expected instanceof Number expectedNumber && value instanceof Number actualNumber) {
                if (Double.compare(expectedNumber.doubleValue(), actualNumber.doubleValue()) == 0)
                    continue;
            }
            return false;
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
        float cosine = (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10));
        return (1F + cosine) / 2F;
    }

    private void validateVector(float[] vector) {
        if (vector == null || (dimension > 0 && vector.length != dimension)) {
            throw new IllegalArgumentException(
                    "Vector dimension mismatch: expected "
                            + (dimension > 0 ? dimension : "a non-null vector")
                            + ", actual "
                            + (vector == null ? 0 : vector.length));
        }
    }

    /**
     * {@code InternalRecord} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param vector 向量，表示该记录组件承载的数据。
     * @param metadata 元数据，表示该记录组件承载的数据。
     */
    private record InternalRecord(String id, float[] vector, Map<String, Object> metadata) {}
}
