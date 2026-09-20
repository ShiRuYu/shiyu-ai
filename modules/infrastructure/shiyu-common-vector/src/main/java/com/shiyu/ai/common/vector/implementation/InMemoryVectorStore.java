package com.shiyu.ai.common.vector.implementation;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorSearchRequest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 管理 In 记忆 向量 相关的运行时状态、注册信息或临时数据。
 */
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
     * 执行 In 记忆 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dimension 用于完成本次业务处理的 dimension 参数。
     */
    public InMemoryVectorStore(int dimension) {
        this.dimension = dimension;
    }

    /**
     * 执行 In 记忆 向量 相关业务数据，并返回处理结果。
     *
     * @return 返回 In 记忆 向量 相关操作生成的结果数据。
     */
    @Override
    public String type() {
        return "inmemory";
    }

    /**
     * 执行 In 记忆 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param record 用于完成本次业务处理的 record 参数。
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
     * 查询 In 记忆 向量 相关业务数据，并返回处理结果。
     *
     * @param queryVector 用于完成本次业务处理的 queryVector 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 In 记忆 向量 相关业务数据，并返回处理结果。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 删除或移除 In 记忆 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
     */
    @Override
    public void delete(String id) {
        store.remove(id);
    }

    /**
     * 删除或移除 In 记忆 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param ids 待处理的业务对象标识集合。
     */
    @Override
    public void deleteBatch(List<String> ids) {
        ids.forEach(store::remove);
    }

    /**
     * 执行 In 记忆 向量 相关业务操作，并维护必要的状态和协作关系。
     */
    @Override
    public void rebuild() {
        store.clear();
    }

    /**
     * 执行 In 记忆 向量 相关业务数据，并返回处理结果。
     *
     * @return 返回 In 记忆 向量 相关操作生成的结果数据。
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
     * 封装 Internal 相关的不可变数据及其字段约束。
     */
    private record InternalRecord(String id, float[] vector, Map<String, Object> metadata) {}
}
