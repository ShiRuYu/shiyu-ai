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
 * 内存优化：向量数据仅存一份（{@link #vectors}），元数据查询通过
 * {@link #metadataCache} 按 recordId 索引，避免 float[] 副本驻留。
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

    /** 元数据缓存（不含向量数据）：recordId → metadata */
    private final Map<String, Map<String, Object>> metadataCache = new ConcurrentHashMap<>();

    /** nodeId → 向量数据（唯一全量副本，与 graph 索引共享） */
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
     *   Map<String, Object> metadata
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
                vectors.add(TYPE_SUPPORT.createFloatVector(vectorData));
                nodeIdToRecordId.put(nodeId, recordId);
                recordIdToNodeId.put(recordId, nodeId);
                metadataCache.put(recordId, metadata);
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
                    // 按 nodeIdToRecordId 遍历而非 vectors 索引，
                    // 因为 delete() 只清除映射但不删除 vectors 元素，
                    // 直接按索引遍历会导致 nodeIdToRecordId.get(i) 返回 null
                    oos.writeInt(nodeIdToRecordId.size());
                    for (Map.Entry<Integer, String> entry : nodeIdToRecordId.entrySet()) {
                        int nodeId = entry.getKey();
                        String recordId = entry.getValue();
                        oos.writeObject(toFloatArray(vectors.get(nodeId)));
                        oos.writeObject(recordId);
                        Map<String, Object> meta = metadataCache.get(recordId);
                        oos.writeObject(meta != null ? meta : Map.of());
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
        // 仅缓存元数据，不缓存向量数据
        metadataCache.put(record.id(), new LinkedHashMap<>(record.metadata()));
        if (builder == null) return;

        try {
            int nodeId;
            // 向量数据存入唯一副本列表
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

                Map<String, Object> meta = metadataCache.get(recordId);
                if (meta == null) continue;

                // 从唯一副本取向量数据
                float[] vec = toFloatArray(vectors.get(nodeId));

                Map<String, Object> resultMeta = new LinkedHashMap<>(meta);
                resultMeta.put("_score", (double) score);
                records.add(new VectorRecord(recordId, vec, resultMeta));
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
        metadataCache.remove(id);
        Integer nodeId = recordIdToNodeId.remove(id);
        if (nodeId != null) {
            nodeIdToRecordId.remove(nodeId);
            version++;
            log.trace("HNSW delete: id={}, nodeId={}", id, nodeId);
        }
    }

    @Override
    public synchronized void rebuild() {
        metadataCache.clear();
        vectors.clear();
        nodeIdToRecordId.clear();
        recordIdToNodeId.clear();
        nextNodeId.set(0);
        graphIndex = null;
        builder = null;
        createNewBuilder();
        log.info("HNSW 索引已重置");
    }

    @Override
    public int size() {
        return vectors.size();
    }

    // ---------------------------------------------------------------
    // 其他方法
    // ---------------------------------------------------------------

    public void save() {
        if (graphIndex == null && vectors.isEmpty()) return;
        if (version == savedVersion) return;
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
        // 遍历 recordIdToNodeId，从 vectors 取向量，从 metadataCache 取元数据
        List<VectorRecord> all = new ArrayList<>();
        for (var entry : recordIdToNodeId.entrySet()) {
            String recordId = entry.getKey();
            int nodeId = entry.getValue();

            if (nodeId >= vectors.size()) continue;
            float[] vec = toFloatArray(vectors.get(nodeId));
            float score = cosineSimilarity(queryVector, vec);

            Map<String, Object> meta = new LinkedHashMap<>(metadataCache.getOrDefault(recordId, Map.of()));
            meta.put("_score", (double) score);
            all.add(new VectorRecord(recordId, vec, meta));
        }

        all.sort((a, b) -> {
            double sa = (double) a.metadata().getOrDefault("_score", 0.0);
            double sb = (double) b.metadata().getOrDefault("_score", 0.0);
            return Double.compare(sb, sa);
        });

        return all.size() <= topK ? all : all.subList(0, topK);
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
