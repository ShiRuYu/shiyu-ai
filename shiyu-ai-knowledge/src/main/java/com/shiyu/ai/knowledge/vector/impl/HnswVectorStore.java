package com.shiyu.ai.knowledge.vector.impl;

import com.shiyu.ai.knowledge.vector.VectorRecord;
import com.shiyu.ai.knowledge.vector.VectorStore;
import com.shiyu.ai.knowledge.vector.config.VectorStoreProperties;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HnswVectorStore implements VectorStore {

    private final int dimension;
    private final Path indexPath;
    private final Map<String, VectorRecord> cache = new ConcurrentHashMap<>();

    private Object index;

    public HnswVectorStore(VectorStoreProperties properties) {
        this.dimension = properties.getDimension();
        this.indexPath = Path.of(properties.getDataDir(), "hnsw.index");
        initIndex();
    }

    private void initIndex() {
        try {
            Class<?> indexClass = Class.forName("io.milvus.usearch.Index");
            Class<?> metricClass = Class.forName("io.milvus.usearch.Metric");
            index = indexClass.getDeclaredConstructor(int.class, int.class, metricClass)
                    .newInstance(dimension, 16, Enum.valueOf((Class<Enum>) metricClass, "InnerProduct"));

            if (Files.exists(indexPath)) {
                indexClass.getMethod("load", String.class).invoke(index, indexPath.toString());
                log.info("HNSW 索引已加载: {}", indexPath);
            }
            log.info("HNSWVectorStore 初始化完成, 维度={}", dimension);
        } catch (ClassNotFoundException e) {
            log.warn("usearch 库未在 classpath 中，HNSWVectorStore 降级为 InMemory 模式");
        } catch (Exception e) {
            log.error("HNSW 索引初始化失败", e);
        }
    }

    @Override
    public void upsert(VectorRecord record) {
        cache.put(record.id(), record);
        try {
            if (index != null) {
                long key = parseIdAsLong(record.id());
                index.getClass().getMethod("add", long.class, float[].class)
                        .invoke(index, key, record.vector());
            }
        } catch (Exception e) {
            log.error("HNSW upsert 失败: id={}", record.id(), e);
        }
    }

    @Override
    public List<VectorRecord> search(float[] queryVector, int topK) {
        if (index == null) {
            return fallbackSearch(queryVector, topK);
        }
        try {
            Object result = index.getClass().getMethod("search", float[].class, int.class)
                    .invoke(index, queryVector, topK);

            @SuppressWarnings("unchecked")
            List<long[]> idsAndDistances = (List<long[]>) result.getClass()
                    .getMethod("getKeysAndDistances").invoke(result);

            List<VectorRecord> records = new ArrayList<>();
            for (long[] entry : idsAndDistances) {
                String id = String.valueOf(entry[0]);
                VectorRecord rec = cache.get(id);
                float[] vector = rec != null ? rec.vector() : null;
                float distance = Float.intBitsToFloat((int) entry[1]);
                if (vector != null) {
                    Map<String, Object> meta = new LinkedHashMap<>(rec.metadata());
                    meta.put("_score", distance);
                    records.add(new VectorRecord(id, vector, meta));
                }
            }
            records.sort((a, b) -> {
                double da = (double) a.metadata().getOrDefault("_score", 0.0);
                double db = (double) b.metadata().getOrDefault("_score", 0.0);
                return Double.compare(db, da);
            });
            return records;
        } catch (Exception e) {
            log.error("HNSW search 失败, 降级到 InMemory", e);
            return fallbackSearch(queryVector, topK);
        }
    }

    @Override
    public void delete(String id) {
        cache.remove(id);
        if (index != null) {
            try {
                long key = parseIdAsLong(id);
                index.getClass().getMethod("remove", long.class).invoke(index, key);
            } catch (Exception e) {
                log.error("HNSW delete 失败: id={}", id, e);
            }
        }
    }

    @Override
    public void rebuild() {
        cache.clear();
        if (index != null) {
            try {
                index.getClass().getMethod("reset").invoke(index);
            } catch (Exception e) {
                log.error("HNSW rebuild 失败", e);
            }
        }
    }

    private List<VectorRecord> fallbackSearch(float[] queryVector, int topK) {
        return cache.entrySet().stream()
                .map(e -> {
                    VectorRecord rec = e.getValue();
                    float score = cosineSimilarity(queryVector, rec.vector());
                    Map<String, Object> meta = new LinkedHashMap<>(rec.metadata());
                    meta.put("_score", score);
                    return new VectorRecord(e.getKey(), rec.vector(), meta);
                })
                .sorted((a, b) -> {
                    double sa = (double) a.metadata().getOrDefault("_score", 0.0);
                    double sb = (double) b.metadata().getOrDefault("_score", 0.0);
                    return Double.compare(sb, sa);
                })
                .limit(topK)
                .toList();
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

    private long parseIdAsLong(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            // Support format like "123_0" (docId_chunkIndex)
            int underscore = id.indexOf('_');
            if (underscore > 0) {
                return Long.parseLong(id.substring(0, underscore));
            }
            // Fallback: use hash
            return (long) id.hashCode();
        }
    }

    public void save() {
        if (index != null) {
            try {
                Path parent = indexPath.getParent();
                if (parent != null) Files.createDirectories(parent);
                index.getClass().getMethod("save", String.class).invoke(index, indexPath.toString());
                log.info("HNSW 索引已保存: {}", indexPath);
            } catch (Exception e) {
                log.error("HNSW 索引保存失败", e);
            }
        }
    }
}
