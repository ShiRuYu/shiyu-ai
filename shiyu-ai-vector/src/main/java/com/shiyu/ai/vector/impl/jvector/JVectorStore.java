package com.shiyu.ai.vector.impl.jvector;

import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.spi.VectorRecord;
import com.shiyu.ai.vector.spi.VectorSearchRequest;
import com.shiyu.ai.vector.spi.VectorStore;
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
 * JVector HNSW 向量存储
 */
@Slf4j
public class JVectorStore implements VectorStore {

    private static final VectorTypeSupport TYPE_SUPPORT =
            VectorizationProvider.getInstance().getVectorTypeSupport();

    private static final int M = 16;
    private static final int BEAM_WIDTH = 100;
    private static final float NEIGHBOR_OVERFLOW = 1.5f;
    private static final float ALPHA = 1.0f;
    private static final boolean ADD_HIERARCHY = true;

    private final int dimension;
    private final Path indexPath;

    private final Map<String, Integer> ordinalMap = new ConcurrentHashMap<>();
    private final AtomicInteger idGen = new AtomicInteger(0);
    private final List<VectorFloat<?>> vectors = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Map<String, Object>> metadataCache = new ConcurrentHashMap<>();
    private volatile OnHeapGraphIndex graphIndex;

    public JVectorStore(VectorStoreProperties properties) {
        this.dimension = properties.getDimension();
        String resolvedDir = properties.getResolvedDataDir();
        try {
            Files.createDirectories(Path.of(resolvedDir));
        } catch (IOException e) {
            log.warn("无法创建向量数据目录: {}", resolvedDir, e);
        }
        this.indexPath = Path.of(resolvedDir, "hnsw.index");
        loadFromDisk();
    }

    @Override
    public synchronized void upsert(VectorRecord record) {
        VectorFloat<?> vec = TYPE_SUPPORT.createFloatVector(record.vector());
        Integer ordinal = ordinalMap.get(record.id());
        if (ordinal != null) {
            vectors.set(ordinal, vec);
        } else {
            ordinal = idGen.getAndIncrement();
            ordinalMap.put(record.id(), ordinal);
            vectors.add(vec);
        }
        metadataCache.put(record.id(), record.metadata() != null ? new HashMap<>(record.metadata()) : new HashMap<>());
        graphIndex = null;
    }

    @Override
    public synchronized void upsertBatch(List<VectorRecord> records) {
        for (VectorRecord r : records) upsert(r);
    }

    @Override
    public List<VectorRecord> search(float[] queryVector, int topK) {
        return search(queryVector, topK, null);
    }

    @Override
    public List<VectorRecord> search(VectorSearchRequest request) {
        return search(request.getQueryVector(), request.getTopK(), request.getFilter());
    }

    private List<VectorRecord> search(float[] queryVector, int topK, Map<String, Object> filter) {
        ensureIndexBuilt();
        try {
            var rav = new ListRandomAccessVectorValues(vectors, dimension);
            VectorFloat<?> qv = TYPE_SUPPORT.createFloatVector(queryVector);
            SearchResult result = GraphSearcher.search(qv, topK, rav, VectorSimilarityFunction.COSINE, graphIndex, Bits.ALL);

            List<VectorRecord> results = new ArrayList<>();
            for (SearchResult.NodeScore node : result.getNodes()) {
                String id = getRecordIdByOrdinal(node.node);
                if (id == null) continue;

                Map<String, Object> meta = metadataCache.get(id);
                if (filter != null && !matchesFilter(meta, filter)) continue;

                Map<String, Object> enrichedMeta = new LinkedHashMap<>();
                if (meta != null) enrichedMeta.putAll(meta);
                enrichedMeta.put("_score", (float) node.score);

                float[] vecArr = new float[dimension];
                VectorFloat<?> vf = vectors.get(node.node);
                for (int i = 0; i < dimension; i++) vecArr[i] = vf.get(i);

                results.add(new VectorRecord(id, vecArr, enrichedMeta));
            }
            return results;
        } catch (Exception e) {
            log.error("向量搜索失败", e);
            return List.of();
        }
    }

    @Override
    public synchronized void delete(String id) {
        Integer ordinal = ordinalMap.remove(id);
        if (ordinal != null) {
            metadataCache.remove(id);
            vectors.set(ordinal, null);
            graphIndex = null;
        }
    }

    @Override
    public synchronized void rebuild() {
        ordinalMap.clear();
        metadataCache.clear();
        vectors.clear();
        idGen.set(0);
        graphIndex = null;
    }

    @Override
    public int size() {
        return (int) vectors.stream().filter(Objects::nonNull).count();
    }

    @PreDestroy
    public void saveToDisk() {
        if (graphIndex == null) return;
        try (var dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexPath.toFile())))) {
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(ordinalMap.entrySet());
            dos.writeInt(dimension);
            dos.writeInt(entries.size());
            for (var entry : entries) {
                dos.writeUTF(entry.getKey());
                dos.writeInt(entry.getValue());
            }
            for (VectorFloat<?> v : vectors) {
                if (v == null) continue;
                for (int i = 0; i < dimension; i++) dos.writeFloat(v.get(i));
            }
            log.info("JVector 索引已保存: {} 条记录", size());
        } catch (IOException e) {
            log.error("保存 JVector 索引失败", e);
        }
    }

    private void loadFromDisk() {
        if (!Files.exists(indexPath)) return;
        try (var dis = new DataInputStream(new BufferedInputStream(new FileInputStream(indexPath.toFile())))) {
            int fileDim = dis.readInt();
            if (fileDim != dimension) { log.warn("维度不匹配"); return; }
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                ordinalMap.put(dis.readUTF(), dis.readInt());
            }
            vectors.clear();
            idGen.set(count);
            for (int i = 0; i < count; i++) {
                float[] arr = new float[dimension];
                for (int j = 0; j < dimension; j++) arr[j] = dis.readFloat();
                vectors.add(TYPE_SUPPORT.createFloatVector(arr));
            }
            log.info("JVector 索引已加载: {} 条记录", count);
        } catch (IOException e) {
            log.warn("加载 JVector 索引失败: {}", e.getMessage());
        }
    }

    private void ensureIndexBuilt() {
        if (graphIndex != null) return;
        synchronized (this) {
            if (graphIndex != null) return;
            try {
                var rav = new ListRandomAccessVectorValues(vectors, dimension);
                var builder = new GraphIndexBuilder(
                        rav, VectorSimilarityFunction.COSINE,
                        M, BEAM_WIDTH, NEIGHBOR_OVERFLOW, ALPHA, ADD_HIERARCHY);
                graphIndex = builder.build(rav);
                log.debug("HNSW 图索引已重建: {} 节点", size());
            } catch (Exception e) {
                log.error("HNSW 图索引重建失败", e);
            }
        }
    }

    private String getRecordIdByOrdinal(int ordinal) {
        for (var entry : ordinalMap.entrySet()) {
            if (entry.getValue() == ordinal) return entry.getKey();
        }
        return null;
    }

    private boolean matchesFilter(Map<String, Object> meta, Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) return true;
        if (meta == null) return false;
        for (var entry : filter.entrySet()) {
            if (!entry.getValue().equals(meta.get(entry.getKey()))) return false;
        }
        return true;
    }
}
