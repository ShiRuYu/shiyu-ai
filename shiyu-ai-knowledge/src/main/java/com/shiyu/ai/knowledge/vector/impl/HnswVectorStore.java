package com.shiyu.ai.knowledge.vector.impl;

import com.shiyu.ai.knowledge.vector.VectorRecord;
import com.shiyu.ai.knowledge.vector.VectorStore;
import com.shiyu.ai.knowledge.vector.config.VectorStoreProperties;
import io.github.jbellis.jvector.graph.GraphIndexBuilder;
import io.github.jbellis.jvector.graph.GraphSearcher;
import io.github.jbellis.jvector.graph.ListRandomAccessVectorValues;
import io.github.jbellis.jvector.graph.OnHeapGraphIndex;
import io.github.jbellis.jvector.graph.SearchResult;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import io.github.jbellis.jvector.vector.VectorizationProvider;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import io.github.jbellis.jvector.vector.types.VectorTypeSupport;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于 jvector（纯 Java HNSW 实现）的向量存储。
 * 无原生库依赖，全平台兼容（Windows/Linux/macOS）。
 * <p>
 * 内部使用 HNSW 图索引进行近似最近邻搜索，同时维护一个内存缓存
 * 作为一致性保证和 fallback。缓存和向量数据通过 Java 序列化持久化到磁盘，
 * 每次 upsert 后触发异步保存，应用关闭时通过 @PreDestroy 同步保存。
 */
@Slf4j
public class HnswVectorStore implements VectorStore {

    private static final VectorTypeSupport TYPE_SUPPORT =
            VectorizationProvider.getInstance().getVectorTypeSupport();

    private static final int M = 16;
    private static final int BEAM_WIDTH = 100;
    private static final float NEIGHBOR_OVERFLOW = 1.5f;
    private static final float ALPHA = 1.0f;
    private static final boolean ADD_HIERARCHY = true;

    private final int dimension;
    private final Path indexPath;
    /** 内存缓存：recordId → VectorRecord（含向量和元数据） */
    private final Map<String, VectorRecord> cache = new ConcurrentHashMap<>();

    /** nodeId → 向量数据（有序，与 graph 中节点索引一致） */
    private final List<VectorFloat<?>> vectors = Collections.synchronizedList(new ArrayList<>());
    /** nodeId → recordId */
    private final Map<Integer, String> nodeIdToRecordId = new ConcurrentHashMap<>();
    /** recordId → nodeId */
    private final Map<String, Integer> recordIdToNodeId = new ConcurrentHashMap<>();
    /** 下一个可用的 nodeId */
    private final AtomicInteger nextNodeId = new AtomicInteger(0);

    /** jvector HNSW 图构建器（支持增量添加） */
    private volatile GraphIndexBuilder builder;
    /** 当前图索引（供搜索使用） */
    private volatile OnHeapGraphIndex graphIndex;

    /** 最后一次保存后的修改计数，用于避免空保存 */
    private volatile long version = 0;
    private volatile long savedVersion = 0;

    public HnswVectorStore(VectorStoreProperties properties) {
        this.dimension = properties.getDimension();
        this.indexPath = Path.of(properties.getDataDir(), "hnsw.index");
        initIndex();
    }

    // ---------------------------------------------------------------
    // 初始化
    // ---------------------------------------------------------------

    private void initIndex() {
        try {
            loadFromDisk();
            log.info("HNSW 索引已加载: {} ({} 条记录)", indexPath, vectors.size());
        } catch (Exception e) {
            log.info("HNSW 索引文件不存在或无法加载 (首次启动? 将新建索引), 维度={}", dimension);
            createNewBuilder();
        }
    }

    @SuppressWarnings("unchecked")
    private void createNewBuilder() {
        ListRandomAccessVectorValues rav = new ListRandomAccessVectorValues(
                (List<VectorFloat<?>>) (List<?>) vectors, dimension);
        builder = new GraphIndexBuilder(
                rav,
                VectorSimilarityFunction.DOT_PRODUCT,
                M,
                BEAM_WIDTH,
                NEIGHBOR_OVERFLOW,
                ALPHA,
                ADD_HIERARCHY
        );
    }

    // ---------------------------------------------------------------
    // 持久化
    // ---------------------------------------------------------------

    /**
     * 序列化格式：
     * <pre>
     * int size
     * for each:
     *   float[] vector
     *   String recordId
     *   Map<String, Object> metadata  ← cache 中的元数据
     * </pre>
     */
    @SuppressWarnings("unchecked")
    private void loadFromDisk() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(indexPath))) {
            int size = ois.readInt();
            for (int i = 0; i < size; i++) {
                float[] vectorData = (float[]) ois.readObject();
                String recordId = (String) ois.readObject();
                Map<String, Object> metadata = (Map<String, Object>) ois.readObject();

                int nodeId = nextNodeId.getAndIncrement();
                VectorFloat<?> vec = TYPE_SUPPORT.createFloatVector(vectorData);
                vectors.add(vec);
                nodeIdToRecordId.put(nodeId, recordId);
                recordIdToNodeId.put(recordId, nodeId);

                // 恢复 cache，确保 search 能返回向量和元数据
                cache.put(recordId, new VectorRecord(recordId, vectorData, metadata));
            }
        }

        // 重建 HNSW 图索引
        createNewBuilder();
        for (int i = 0; i < vectors.size(); i++) {
            builder.addGraphNode(i, vectors.get(i));
        }
        graphIndex = builder.getGraph();
    }

    private synchronized void saveToDisk() {
        try {
            Path parent = indexPath.getParent();
            if (parent != null) Files.createDirectories(parent);

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(indexPath))) {
                synchronized (vectors) {
                    oos.writeInt(vectors.size());
                    for (int i = 0; i < vectors.size(); i++) {
                        oos.writeObject(toFloatArray(vectors.get(i)));
                        String recordId = nodeIdToRecordId.get(i);
                        oos.writeObject(recordId);
                        VectorRecord rec = cache.get(recordId);
                        oos.writeObject(rec != null ? rec.metadata() : Map.of());
                    }
                }
            }
            savedVersion = version;
            log.info("HNSW 索引已保存: {} ({} 条记录)", indexPath, vectors.size());
        } catch (Exception e) {
            log.error("HNSW 索引保存失败", e);
        }
    }

    @PreDestroy
    public void close() {
        log.info("HNSWVectorStore 关闭中, 保存索引...");
        save();
    }

    // ---------------------------------------------------------------
    // VectorStore 接口
    // ---------------------------------------------------------------

    @Override
    public void upsert(VectorRecord record) {
        cache.put(record.id(), record);
        if (builder == null) return;

        try {
            int nodeId;
            VectorFloat<?> vec = TYPE_SUPPORT.createFloatVector(record.vector().clone());

            synchronized (vectors) {
                Integer oldNodeId = recordIdToNodeId.remove(record.id());
                if (oldNodeId != null) {
                    nodeIdToRecordId.remove(oldNodeId);
                }

                nodeId = nextNodeId.getAndIncrement();
                vectors.add(vec);
                nodeIdToRecordId.put(nodeId, record.id());
                recordIdToNodeId.put(record.id(), nodeId);

                builder.addGraphNode(nodeId, vec);
            }

            graphIndex = builder.getGraph();
            version++;

            log.trace("HNSW upsert 成功: id={}, nodeId={}", record.id(), nodeId);
        } catch (Exception e) {
            log.error("HNSW upsert 失败: id={}", record.id(), e);
        }
    }

    @Override
    public List<VectorRecord> search(float[] queryVector, int topK) {
        OnHeapGraphIndex graph = graphIndex;
        if (graph == null) {
            return fallbackSearch(queryVector, topK);
        }

        try {
            VectorFloat<?> query = TYPE_SUPPORT.createFloatVector(queryVector);
            ListRandomAccessVectorValues rav = new ListRandomAccessVectorValues(
                    (List<VectorFloat<?>>) (List<?>) vectors, dimension);

            SearchResult result = GraphSearcher.search(
                    query,
                    topK,
                    rav,
                    VectorSimilarityFunction.DOT_PRODUCT,
                    graph,
                    Bits.ALL
            );

            SearchResult.NodeScore[] nodes = result.getNodes();
            List<VectorRecord> records = new ArrayList<>(nodes.length);

            for (SearchResult.NodeScore ns : nodes) {
                int nodeId = ns.node;
                float score = ns.score;

                String recordId = nodeIdToRecordId.get(nodeId);
                if (recordId == null) continue;

                VectorRecord rec = cache.get(recordId);
                if (rec == null) continue;

                Map<String, Object> meta = new LinkedHashMap<>(rec.metadata());
                meta.put("_score", (double) score);
                records.add(new VectorRecord(recordId, rec.vector(), meta));
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
        Integer nodeId = recordIdToNodeId.remove(id);
        if (nodeId != null) {
            nodeIdToRecordId.remove(nodeId);
            version++;
            log.trace("HNSW delete: id={}, nodeId={}", id, nodeId);
        }
    }

    @Override
    public synchronized void rebuild() {
        cache.clear();
        vectors.clear();
        nodeIdToRecordId.clear();
        recordIdToNodeId.clear();
        nextNodeId.set(0);
        graphIndex = null;
        builder = null;
        createNewBuilder();
        log.info("HNSW 索引已重置");
    }

    // ---------------------------------------------------------------
    // 其他方法
    // ---------------------------------------------------------------

    public void save() {
        if (graphIndex == null && vectors.isEmpty()) return;
        if (version == savedVersion) return; // 无变更，不保存
        saveToDisk();
    }

    // ---------------------------------------------------------------
    // 辅助方法
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static float[] toFloatArray(VectorFloat<?> vec) {
        Object raw = vec.get();
        if (raw instanceof float[]) {
            return (float[]) raw;
        }
        float[] result = new float[vec.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = vec.get(i);
        }
        return result;
    }

    // ---------------------------------------------------------------
    // Fallback（纯内存余弦相似度搜索）
    // ---------------------------------------------------------------

    private List<VectorRecord> fallbackSearch(float[] queryVector, int topK) {
        return cache.values().stream()
                .map(rec -> {
                    float score = cosineSimilarity(queryVector, rec.vector());
                    Map<String, Object> meta = new LinkedHashMap<>(rec.metadata());
                    meta.put("_score", (double) score);
                    return new VectorRecord(rec.id(), rec.vector(), meta);
                })
                .sorted((a, b) -> {
                    double sa = (double) a.metadata().getOrDefault("_score", 0.0);
                    double sb = (double) b.metadata().getOrDefault("_score", 0.0);
                    return Double.compare(sb, sa);
                })
                .limit(topK)
                .toList();
    }

    private static float cosineSimilarity(float[] a, float[] b) {
        float dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10));
    }
}
