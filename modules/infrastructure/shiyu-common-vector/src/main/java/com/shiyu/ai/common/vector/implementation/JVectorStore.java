package com.shiyu.ai.common.vector.implementation;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.config.VectorStoreProperties;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorSearchRequest;
import com.shiyu.ai.common.vector.model.VectorSearchType;

import io.github.jbellis.jvector.graph.GraphIndexBuilder;
import io.github.jbellis.jvector.graph.GraphSearcher;
import io.github.jbellis.jvector.graph.ImmutableGraphIndex;
import io.github.jbellis.jvector.graph.ListRandomAccessVectorValues;
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
 * 管理 J 向量 相关的运行时状态、注册信息或临时数据。
 */
@Slf4j
public class JVectorStore implements VectorStore {

    private static final VectorTypeSupport TYPE_SUPPORT =
            VectorizationProvider.getInstance().getVectorTypeSupport();

    /**
     * M 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final int M = 16;
    /**
     * BEAM_WIDTH 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final int BEAM_WIDTH = 100;
    /**
     * NEIGHBOR_OVERFLOW 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final float NEIGHBOR_OVERFLOW = 1.5f;
    /**
     * ALPHA 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final float ALPHA = 1.0f;
    /**
     * 维度，表示当前对象中的对应属性。
     */
    private final int dimension;
    /**
     * 索引路径，表示当前对象中的对应属性。
     */
    private final Path indexPath;
    /**
     * 元数据路径，表示当前对象中的对应属性。
     */
    private final Path metadataPath;

    /** 向量标识到序号的映射，用于快速定位向量。 */
    private final Map<String, Integer> ordinalMap = new ConcurrentHashMap<>();

    /** ordinal → id（反向索引，用于 O(1) 查找） */
    private final Map<Integer, String> ordinalToId = new ConcurrentHashMap<>();

    private final AtomicInteger idGen = new AtomicInteger(0);
    private final List<VectorFloat<?>> vectors = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Map<String, Object>> metadataCache = new ConcurrentHashMap<>();
    /**
     * 图结构索引，表示当前对象中的对应属性。
     */
    private volatile ImmutableGraphIndex graphIndex;

    /**
     * 执行 J 向量 相关业务数据，并返回处理结果。
     *
     * @return 返回 J 向量 相关操作生成的结果数据。
     */
    @Override
    public String type() {
        return "jvector";
    }

    /**
     * 执行 J 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param properties 用于完成本次业务处理的 properties 参数。
     */
    public JVectorStore(VectorStoreProperties properties) {
        this.dimension = properties.getDimension();
        String resolvedDir = properties.getResolvedDataDir();
        try {
            Files.createDirectories(Path.of(resolvedDir));
        } catch (IOException e) {
            log.warn(
                    "无法创建向量数据目录: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }
        this.indexPath = Path.of(resolvedDir, "hnsw.index");
        this.metadataPath = Path.of(resolvedDir, "metadata.dat");
        loadFromDisk();
    }

    /**
     * 执行 J 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param record 用于完成本次业务处理的 record 参数。
     */
    @Override
    public synchronized void upsert(VectorRecord record) {
        validateVector(record.vector());
        VectorFloat<?> vec = TYPE_SUPPORT.createFloatVector(record.vector());
        Integer ordinal = ordinalMap.get(record.id());
        if (ordinal != null) {
            // 更新现有
            vectors.set(ordinal, vec);
        } else {
            ordinal = idGen.getAndIncrement();
            vectors.add(vec);
            ordinalMap.put(record.id(), ordinal);
            ordinalToId.put(ordinal, record.id());
        }
        metadataCache.put(
                record.id(),
                record.metadata() != null ? new HashMap<>(record.metadata()) : new HashMap<>());
        graphIndex = null;
    }

    /**
     * 执行 J 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param records 用于完成本次业务处理的 records 参数。
     */
    @Override
    public synchronized void upsertBatch(List<VectorRecord> records) {
        for (VectorRecord r : records) upsert(r);
    }

    /**
     * 查询 J 向量 相关业务数据，并返回处理结果。
     *
     * @param queryVector 用于完成本次业务处理的 queryVector 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<VectorRecord> search(float[] queryVector, int topK) {
        return search(queryVector, topK, null, 0.0);
    }

    /**
     * 查询 J 向量 相关业务数据，并返回处理结果。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<VectorRecord> search(VectorSearchRequest request) {
        Objects.requireNonNull(request, "Vector search request must not be null");
        if (request.getSearchType() == VectorSearchType.EXACT) {
            return exactSearch(
                    request.getQueryVector(),
                    request.getTopK(),
                    request.getFilter(),
                    request.getMinScore());
        }
        return search(
                request.getQueryVector(),
                request.getTopK(),
                request.getFilter(),
                request.getMinScore());
    }

    private synchronized List<VectorRecord> exactSearch(
            float[] queryVector, int topK, Map<String, Object> filter, double minScore) {
        if (size() == 0 || topK <= 0) return List.of();
        validateVector(queryVector);
        VectorFloat<?> query = TYPE_SUPPORT.createFloatVector(queryVector);
        return ordinalMap.entrySet().stream()
                .filter(entry -> matchesFilter(metadataCache.get(entry.getKey()), filter))
                .map(
                        entry ->
                                toScoredRecord(
                                        entry.getKey(),
                                        entry.getValue(),
                                        VectorSimilarityFunction.COSINE.compare(
                                                query, vectors.get(entry.getValue()))))
                .filter(
                        record ->
                                ((Number) record.metadata().get("_score")).doubleValue()
                                        >= minScore)
                .sorted(
                        Comparator.comparingDouble(
                                        (VectorRecord record) ->
                                                ((Number) record.metadata().get("_score"))
                                                        .doubleValue())
                                .reversed())
                .limit(topK)
                .toList();
    }

    private synchronized List<VectorRecord> search(
            float[] queryVector, int topK, Map<String, Object> filter, double minScore) {
        if (size() == 0 || topK <= 0) return List.of();
        validateVector(queryVector);

        ensureIndexBuilt();
        try {
            var rav = new ListRandomAccessVectorValues(vectors, dimension);
            VectorFloat<?> qv = TYPE_SUPPORT.createFloatVector(queryVector);
            Bits acceptBits =
                    filter == null || filter.isEmpty()
                            ? Bits.ALL
                            : node -> {
                                String id = ordinalToId.get(node);
                                return id != null && matchesFilter(metadataCache.get(id), filter);
                            };
            int searchK = Math.min(topK, size());
            SearchResult result =
                    GraphSearcher.search(
                            qv,
                            searchK,
                            rav,
                            VectorSimilarityFunction.COSINE,
                            graphIndex,
                            acceptBits);

            List<VectorRecord> results = new ArrayList<>();
            int collected = 0;
            for (SearchResult.NodeScore node : result.getNodes()) {
                if (collected >= topK) break;
                String id = ordinalToId.get(node.node);
                if (id == null) continue;

                Map<String, Object> meta = metadataCache.get(id);
                if (filter != null && !matchesFilter(meta, filter)) continue;

                double score = (double) node.score;
                if (score < minScore) continue;

                collected++;
                results.add(toScoredRecord(id, node.node, score));
            }
            return results;
        } catch (Exception e) {
            log.error(
                    "向量搜索失败: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
            return List.of();
        }
    }

    /**
     * 删除或移除 J 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
     */
    @Override
    public synchronized void delete(String id) {
        Integer ordinal = ordinalMap.remove(id);
        if (ordinal != null) {
            ordinalToId.remove(ordinal);
            metadataCache.remove(id);
            vectors.remove((int) ordinal);
            compactOrdinalsFrom(ordinal);
            graphIndex = null;
        }
    }

    /**
     * 删除或移除 J 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param ids 待处理的业务对象标识集合。
     */
    @Override
    public synchronized void deleteBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        Set<String> removed = new HashSet<>(ids);
        if (removed.stream().noneMatch(ordinalMap::containsKey)) return;

        List<Map.Entry<String, Integer>> retained =
                ordinalMap.entrySet().stream()
                        .filter(entry -> !removed.contains(entry.getKey()))
                        .sorted(Map.Entry.comparingByValue())
                        .toList();
        List<VectorFloat<?>> retainedVectors = new ArrayList<>(retained.size());
        for (Map.Entry<String, Integer> entry : retained) {
            retainedVectors.add(vectors.get(entry.getValue()));
        }
        removed.forEach(metadataCache::remove);
        ordinalMap.clear();
        ordinalToId.clear();
        vectors.clear();
        vectors.addAll(retainedVectors);
        for (int ordinal = 0; ordinal < retained.size(); ordinal++) {
            String id = retained.get(ordinal).getKey();
            ordinalMap.put(id, ordinal);
            ordinalToId.put(ordinal, id);
        }
        idGen.set(retained.size());
        graphIndex = null;
    }

    /**
     * 执行 J 向量 相关业务操作，并维护必要的状态和协作关系。
     */
    @Override
    public synchronized void rebuild() {
        ordinalMap.clear();
        ordinalToId.clear();
        metadataCache.clear();
        vectors.clear();
        idGen.set(0);
        graphIndex = null;
    }

    /**
     * 执行 J 向量 相关业务数据，并返回处理结果。
     *
     * @return 返回 J 向量 相关操作生成的结果数据。
     */
    @Override
    public int size() {
        return ordinalMap.size();
    }

    // ========================
    // 磁盘持久化
    // ========================

    /**
     * 执行 J 向量 相关业务操作，并维护必要的状态和协作关系。
     */
    @Override
    public synchronized void flush() {
        if (size() == 0) {
            try {
                Files.deleteIfExists(indexPath);
                Files.deleteIfExists(metadataPath);
            } catch (IOException exception) {
                log.warn(
                        "清理空向量索引失败: errorType={}, errorMessageLength={}",
                        exception.getClass().getSimpleName(),
                        exception.getMessage() == null ? 0 : exception.getMessage().length());
            }
            return;
        }
        // 先构建索引
        ensureIndexBuilt();
        if (graphIndex == null) {
            log.warn("HNSW 索引未就绪，跳过保存");
            return;
        }

        // 保存向量 + ordinal 映射
        try (var dos =
                new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(indexPath.toFile())))) {
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
            log.error(
                    "保存 JVector 索引失败: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }

        // 保存 metadata
        try (var dos =
                new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(metadataPath.toFile())))) {
            dos.writeInt(metadataCache.size());
            for (var entry : metadataCache.entrySet()) {
                dos.writeUTF(entry.getKey());
                dos.writeInt(entry.getValue().size());
                for (var me : entry.getValue().entrySet()) {
                    dos.writeUTF(me.getKey());
                    writeObject(dos, me.getValue());
                }
            }
            log.info("JVector metadata 已保存: {} 条", metadataCache.size());
        } catch (IOException e) {
            log.error(
                    "保存 JVector metadata 失败: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }
    }

    /**
     * {@code close} 释放或移除当前操作涉及的资源。
     */
    @Override
    @PreDestroy
    public void close() {
        flush();
    }

    private void loadFromDisk() {
        // 加载向量 + ordinal 映射
        if (!Files.exists(indexPath)) return;
        try (var dis =
                new DataInputStream(
                        new BufferedInputStream(new FileInputStream(indexPath.toFile())))) {
            int fileDim = dis.readInt();
            if (fileDim != dimension) {
                log.warn(
                        "维度不匹配: expectedDimension={}, actualDimension={}, 跳过加载",
                        dimension,
                        fileDim);
                return;
            }
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                String id = dis.readUTF();
                int ord = dis.readInt();
                ordinalMap.put(id, ord);
                ordinalToId.put(ord, id);
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
            log.warn(
                    "加载 JVector 索引失败: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }

        // 加载 metadata
        if (!Files.exists(metadataPath)) return;
        try (var dis =
                new DataInputStream(
                        new BufferedInputStream(new FileInputStream(metadataPath.toFile())))) {
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                String id = dis.readUTF();
                int mapSize = dis.readInt();
                Map<String, Object> meta = new HashMap<>();
                for (int j = 0; j < mapSize; j++) {
                    String key = dis.readUTF();
                    meta.put(key, readObject(dis));
                }
                metadataCache.put(id, meta);
            }
            log.info("JVector metadata 已加载: {} 条", count);
        } catch (IOException e) {
            log.warn(
                    "加载 JVector metadata 失败: errorType={}, errorMessageLength={}",
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }
    }

    /** 将 Object 写入 DataOutputStream（支持 String/Number/Boolean） */
    private void writeObject(DataOutputStream dos, Object obj) throws IOException {
        if (obj == null) {
            dos.writeByte(0);
        } else if (obj instanceof String s) {
            dos.writeByte(1);
            dos.writeUTF(s);
        } else if (obj instanceof Integer i) {
            dos.writeByte(2);
            dos.writeInt(i);
        } else if (obj instanceof Long l) {
            dos.writeByte(3);
            dos.writeLong(l);
        } else if (obj instanceof Float f) {
            dos.writeByte(4);
            dos.writeFloat(f);
        } else if (obj instanceof Double d) {
            dos.writeByte(5);
            dos.writeDouble(d);
        } else if (obj instanceof Boolean b) {
            dos.writeByte(6);
            dos.writeBoolean(b);
        } else {
            dos.writeByte(1); // fallback: 按 String 写
            dos.writeUTF(obj.toString());
        }
    }

    /** 从 DataInputStream 读取 Object */
    private Object readObject(DataInputStream dis) throws IOException {
        byte type = dis.readByte();
        return switch (type) {
            case 0 -> null;
            case 1 -> dis.readUTF();
            case 2 -> dis.readInt();
            case 3 -> dis.readLong();
            case 4 -> dis.readFloat();
            case 5 -> dis.readDouble();
            case 6 -> dis.readBoolean();
            default -> null;
        };
    }

    // ========================
    // HNSW 索引构建
    // ========================

    private void ensureIndexBuilt() {
        if (graphIndex != null) return;
        synchronized (this) {
            if (graphIndex != null) return;
            try {
                if (vectors.isEmpty()) return;
                var rav = new ListRandomAccessVectorValues(vectors, dimension);
                var builder =
                        GraphIndexBuilder.builder(rav, VectorSimilarityFunction.COSINE, M)
                                .withBeamWidth(BEAM_WIDTH)
                                .withNeighborOverflow(NEIGHBOR_OVERFLOW)
                                .withAlpha(ALPHA)
                                .build();
                graphIndex = builder.build(rav);
                log.debug("HNSW 图索引已重建: {} 节点", vectors.size());
            } catch (Exception e) {
                log.error("HNSW 图索引重建失败", e);
            }
        }
    }

    private void compactOrdinalsFrom(int removedOrdinal) {
        for (int ordinal = removedOrdinal; ordinal < vectors.size(); ordinal++) {
            String id = ordinalToId.remove(ordinal + 1);
            if (id != null) {
                ordinalToId.put(ordinal, id);
                ordinalMap.put(id, ordinal);
            }
        }
        idGen.set(vectors.size());
    }

    private void validateVector(float[] vector) {
        if (vector == null || vector.length != dimension) {
            throw new IllegalArgumentException(
                    "Vector dimension mismatch: expected "
                            + dimension
                            + ", actual "
                            + (vector == null ? 0 : vector.length));
        }
    }

    private boolean matchesFilter(Map<String, Object> meta, Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) return true;
        if (meta == null) return false;
        for (var entry : filter.entrySet()) {
            Object metaValue = meta.get(entry.getKey());
            if (metaValue == null && entry.getValue() == null) continue;
            if (metaValue == null || !metaValue.equals(entry.getValue())) {
                // 类型兼容：如果 metaValue 是 Long 而 filter 值是 Integer，先转换再比较
                if (metaValue instanceof Number mn && entry.getValue() instanceof Number fn) {
                    if (mn.doubleValue() != fn.doubleValue()) return false;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private VectorRecord toScoredRecord(String id, int ordinal, double score) {
        Map<String, Object> enrichedMeta = new LinkedHashMap<>();
        Map<String, Object> meta = metadataCache.get(id);
        if (meta != null) enrichedMeta.putAll(meta);
        enrichedMeta.put("_score", score);
        float[] vector = new float[dimension];
        VectorFloat<?> stored = vectors.get(ordinal);
        for (int i = 0; i < dimension; i++) vector[i] = stored.get(i);
        return new VectorRecord(id, vector, enrichedMeta);
    }
}
