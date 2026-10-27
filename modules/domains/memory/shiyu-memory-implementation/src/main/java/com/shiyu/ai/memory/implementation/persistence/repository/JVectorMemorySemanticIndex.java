package com.shiyu.ai.memory.implementation.persistence.repository;

import com.shiyu.ai.memory.implementation.domain.magma.port.MemorySemanticIndex;
import com.shiyu.ai.memory.implementation.domain.magma.port.MemorySemanticIndexUnavailableException;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.config.VectorStoreProperties;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorSearchRequest;
import com.shiyu.ai.common.vector.model.VectorSearchType;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;
import com.shiyu.ai.model.contract.api.EmbeddingService;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 将持久化记忆事件接入配置的嵌入服务和向量存储。
 */
@Component
public class JVectorMemorySemanticIndex implements MemorySemanticIndex {
    private static final String EMBEDDING_PROFILE_KEY = "embeddingProfile";
    private static final String EMBEDDING_DIMENSION_KEY = "embeddingDimension";
    private static final String EMBEDDING_PROFILE_VERSION = "memory-embedding-service-v1";
    private static final String DEFAULT_EMBEDDING_PROFILE = "BGE-small-zh-v1.5";
    private static final int MAX_SEARCH_CANDIDATES = 500;
    private static final int SEARCH_CANDIDATE_FACTOR = 4;
    private final VectorStore store;
    private final JdbcMagmaMemoryRepository repository;
    private final EmbeddingService embeddingService;
    private final int dimension;
    private final String embeddingProfile;
    private final ReentrantReadWriteLock indexLock = new ReentrantReadWriteLock();

    /**
     * 创建使用已配置嵌入服务的语义索引。
     *
     * @param store 持久化向量存储
     * @param properties 向量存储配置
     * @param repository 重建索引时读取的持久化记忆事件仓库
     * @param embeddingServices 可选的已配置语义嵌入服务
     * @param configuredEmbeddingProfile 配置的嵌入提供方及模型稳定标识
     */
    @Autowired
    public JVectorMemorySemanticIndex(
            VectorStore store,
            VectorStoreProperties properties,
            JdbcMagmaMemoryRepository repository,
            ObjectProvider<EmbeddingService> embeddingServices,
            @Value("${shiyu.memory.embedding-profile:BGE-small-zh-v1.5}")
                    String configuredEmbeddingProfile) {
        this.store = Objects.requireNonNull(store, "VectorStore is required");
        Objects.requireNonNull(properties, "VectorStoreProperties are required");
        this.repository = Objects.requireNonNull(repository, "Memory repository is required");
        this.embeddingService =
                embeddingServices == null ? null : embeddingServices.getIfAvailable();
        int configuredDimension = properties.getDimension();
        this.dimension = configuredDimension > 0 ? configuredDimension : 0;
        String profile =
                configuredEmbeddingProfile == null || configuredEmbeddingProfile.isBlank()
                        ? DEFAULT_EMBEDDING_PROFILE
                        : configuredEmbeddingProfile;
        this.embeddingProfile = profileFor(profile, dimension);
    }

    /**
     * 保留供直接调用方使用的原始构造签名；通过此构造方法执行语义操作时，
     * 报告嵌入服务不可用，不提供本地回退。
     *
     * @param store 持久化向量存储
     * @param properties 向量存储配置
     * @param repository 重建索引时读取的持久化记忆事件仓库
     */
    public JVectorMemorySemanticIndex(
            VectorStore store,
            VectorStoreProperties properties,
            JdbcMagmaMemoryRepository repository) {
        this(
                store,
                properties,
                repository,
                (ObjectProvider<EmbeddingService>) null,
                DEFAULT_EMBEDDING_PROFILE);
    }

    /**
     * 保存带有明确配置标记的事件嵌入，以便区分这些向量与
     * 历史 UTF-8 投影向量，后者的持久化元数据不包含配置标记。
     *
     * @param event 需要建立索引的记忆事件
     */
    public void upsert(MemoryEvent event) {
        indexLock.writeLock().lock();
        try {
            store.upsert(vectorRecord(event));
            store.flush();
        } finally {
            indexLock.writeLock().unlock();
        }
    }

    /**
     * 仅搜索使用当前嵌入配置生成的向量。
     *
     * @param query 语义查询及租户过滤条件
     * @param limit 最多返回的向量候选数
     * @return 匹配的有效记忆事件
     */
    public List<MemoryPath> search(MemoryQuery query, int limit) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("tenantId", query.tenantId().value());
        filter.put("namespace", query.namespace());
        filter.put(EMBEDDING_PROFILE_KEY, embeddingProfile);
        filter.put(EMBEDDING_DIMENSION_KEY, dimension);
        if (query.subjectType() != null && !query.subjectType().isBlank())
            filter.put("subjectType", query.subjectType());
        if (query.subjectId() != null && !query.subjectId().isBlank())
            filter.put("subjectId", query.subjectId());
        int resultLimit = Math.max(0, limit);
        if (resultLimit == 0) return List.of();
        float[] queryVector = embed(query.tenantId(), query.text());
        indexLock.readLock().lock();
        try {
            Map<String, Object> profileIndependentFilters = new HashMap<>(filter);
            profileIndependentFilters.remove(EMBEDDING_PROFILE_KEY);
            profileIndependentFilters.remove(EMBEDDING_DIMENSION_KEY);
            if (store.count(profileIndependentFilters) != store.count(filter)) {
                throw incompatibleProfile(query.namespace());
            }
            int candidateLimit = Math.min(
                    MAX_SEARCH_CANDIDATES,
                    (int) Math.min(
                            Integer.MAX_VALUE,
                            (long) Math.max(1, resultLimit) * SEARCH_CANDIDATE_FACTOR));
            Map<String, Object> candidateFilters = new HashMap<>(profileIndependentFilters);
            candidateFilters.put("status", MemoryEventStatus.ACTIVE.name());
            List<VectorRecord> records =
                    store.search(
                            VectorSearchRequest.builder()
                                    .queryVector(queryVector)
                                    .topK(candidateLimit)
                                    .searchType(VectorSearchType.ANN)
                                    .filter(candidateFilters)
                                    .build());
            List<MemoryPath> out = new ArrayList<>(resultLimit);
            for (int start = 0; start < records.size(); start += 500) {
                List<VectorRecord> batch =
                        records.subList(start, Math.min(start + 500, records.size()));
                Map<String, MemoryEvent> eventsById = new HashMap<>();
                List<String> eventIds = batch.stream()
                        .filter(record -> matchesFilters(record.metadata(), profileIndependentFilters))
                        .map(VectorRecord::id)
                        .toList();
                repository.findEventsByIds(query.tenantId(), eventIds)
                        .forEach(event -> eventsById.put(event.id(), event));
                for (VectorRecord record : batch) {
                    Map<String, Object> metadata = record.metadata();
                    if (!matchesFilters(metadata, profileIndependentFilters)) continue;
                    MemoryEvent event = eventsById.get(record.id());
                    if (!matchesSourceEvent(event, query)) continue;
                    validateStoredRecord(record, query.namespace());
                    if (!belongsToTenant(record, query.tenantId())
                            || !matchesFilters(metadata, filter)
                            || event.status() != MemoryEventStatus.ACTIVE
                            || out.size() >= resultLimit) {
                        continue;
                    }
                    out.add(
                            new MemoryPath(
                                    event,
                                    ((Number) metadata.getOrDefault("_score", 0d)).doubleValue(),
                                    List.of()));
                }
            }
            return out;
        } finally {
            indexLock.readLock().unlock();
        }
    }

    /**
     * 删除指定记忆事件的向量。
     *
     * @param id 记忆事件标识
     */
    public void delete(String id) {
        indexLock.writeLock().lock();
        try {
            store.delete(id);
            store.flush();
        } finally {
            indexLock.writeLock().unlock();
        }
    }

    /**
     * 重新嵌入命名空间中的持久化事件，替换历史向量和旧配置生成的向量。
     *
     * @param tenantId 需要重建事件索引的租户
     * @param namespace 需要重建的命名空间
     */
    public void rebuild(TenantId tenantId, String namespace) {
        indexLock.writeLock().lock();
        try {
            rebuildLocked(tenantId, namespace);
        } finally {
            indexLock.writeLock().unlock();
        }
    }

    private void rebuildLocked(TenantId tenantId, String namespace) {
        int pageSize = 1000;
        String afterId = "";
        while (true) {
            List<MemoryEvent> allEvents =
                    repository.findEventsForSemanticIndexRebuild(
                            tenantId, namespace, afterId, pageSize);
            if (allEvents.isEmpty()) break;
            List<MemoryEvent> indexableBatch =
                    allEvents.stream()
                            .filter(
                                    event ->
                                            event.status() == MemoryEventStatus.ACTIVE
                                                    || event.status()
                                                            == MemoryEventStatus.CANDIDATE)
                            .toList();
            store.upsertBatch(indexableBatch.stream().map(this::vectorRecord).toList());
            afterId = allEvents.getLast().id();
            if (allEvents.size() < pageSize) break;
        }

        Map<String, Object> scopeFilter =
                Map.of("tenantId", tenantId.value(), "namespace", namespace);
        List<VectorRecord> existingVectors =
                store.search(
                        VectorSearchRequest.builder()
                                .queryVector(embed(tenantId, "memory index cleanup"))
                                .topK(Math.max(1, store.size()))
                                .minScore(-1.0d)
                                .searchType(VectorSearchType.EXACT)
                                .filter(scopeFilter)
                                .build());
        Set<String> retainedIds = new HashSet<>();
        for (int start = 0; start < existingVectors.size(); start += 500) {
            List<String> vectorIds =
                    existingVectors.subList(start, Math.min(start + 500, existingVectors.size()))
                            .stream()
                            .filter(record -> matchesFilters(record.metadata(), scopeFilter))
                            .map(VectorRecord::id)
                            .toList();
            repository.findEventsByIds(tenantId, vectorIds).stream()
                    .filter(event -> namespace.equals(event.namespace()))
                    .filter(event -> event.status() == MemoryEventStatus.ACTIVE
                            || event.status() == MemoryEventStatus.CANDIDATE)
                    .map(MemoryEvent::id)
                    .forEach(retainedIds::add);
        }

        List<String> staleIds =
                existingVectors.stream()
                        .filter(record -> matchesFilters(record.metadata(), scopeFilter))
                        .map(VectorRecord::id)
                        .filter(id -> !retainedIds.contains(id))
                        .toList();
        if (!staleIds.isEmpty()) store.deleteBatch(staleIds);
        store.flush();
    }

    private VectorRecord vectorRecord(MemoryEvent event) {
        float[] vector = embed(event.tenantId(), event.content());
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("tenantId", event.tenantId().value());
        metadata.put("namespace", event.namespace());
        metadata.put("subjectType", event.subjectType());
        metadata.put("subjectId", event.subjectId());
        metadata.put("status", event.status().name());
        metadata.put(EMBEDDING_PROFILE_KEY, embeddingProfile);
        metadata.put(EMBEDDING_DIMENSION_KEY, dimension);
        return new VectorRecord(event.id(), vector, metadata);
    }

    private float[] embed(TenantId tenantId, String text) {
        if (embeddingService == null) {
            throw new MemorySemanticIndexUnavailableException(
                    "Memory semantic indexing and recall are unavailable because EmbeddingService is not configured");
        }
        int embeddingDimension = embeddingService.dimension();
        if (embeddingDimension <= 0) {
            throw new MemorySemanticIndexUnavailableException(
                    "Memory semantic indexing and recall are unavailable because EmbeddingService has no configured model");
        }
        if (dimension <= 0 || embeddingDimension != dimension) {
            throw new MemorySemanticIndexUnavailableException(
                    "Memory embedding dimension mismatch: EmbeddingService reports "
                            + embeddingDimension
                            + " but vector store is configured for "
                            + dimension);
        }
        float[] vector = embeddingService.embed(tenantId, text);
        if (vector == null || vector.length != dimension) {
            throw new MemorySemanticIndexUnavailableException(
                    "EmbeddingService returned dimension "
                            + (vector == null ? 0 : vector.length)
                            + "; memory vector store requires "
                            + dimension);
        }
        return vector;
    }

    private void validateStoredRecord(VectorRecord record, String namespace) {
        Map<String, Object> metadata = record.metadata();
        if (metadata == null
                || !embeddingProfile.equals(metadata.get(EMBEDDING_PROFILE_KEY))
                || !Integer.valueOf(dimension)
                        .equals(asInteger(metadata.get(EMBEDDING_DIMENSION_KEY)))) {
            throw incompatibleProfile(namespace);
        }
        float[] vector = record.vector();
        if (vector == null || vector.length != dimension) {
            throw new MemorySemanticIndexUnavailableException(
                    "Stored memory vector dimension mismatch for namespace '"
                            + namespace
                            + "': expected "
                            + dimension
                            + ", actual "
                            + (vector == null ? 0 : vector.length)
                            + ". Rebuild the memory semantic index.");
        }
    }

    private boolean belongsToTenant(VectorRecord record, TenantId tenantId) {
        Object value = record.metadata().get("tenantId");
        return value instanceof Number number
                && number.longValue() == tenantId.value();
    }

    private boolean matchesSourceEvent(MemoryEvent event, MemoryQuery query) {
        return event != null
                && query.namespace().equals(event.namespace())
                && (query.subjectType() == null
                        || query.subjectType().isBlank()
                        || query.subjectType().equals(event.subjectType()))
                && (query.subjectId() == null
                        || query.subjectId().isBlank()
                        || query.subjectId().equals(event.subjectId()));
    }

    private boolean matchesFilters(Map<String, Object> metadata, Map<String, Object> filters) {
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            Object actual = metadata.get(filter.getKey());
            if (actual instanceof Number actualNumber
                    && filter.getValue() instanceof Number expectedNumber) {
                if (Double.compare(actualNumber.doubleValue(), expectedNumber.doubleValue()) != 0)
                    return false;
            } else if (!Objects.equals(actual, filter.getValue())) {
                return false;
            }
        }
        return true;
    }

    private IllegalStateException incompatibleProfile(String namespace) {
        return new MemorySemanticIndexUnavailableException(
                "Incompatible semantic vector profile for memory namespace '"
                        + namespace
                        + "'. Rebuild the memory semantic index with "
                        + "POST /api/memory/admin/indexes/rebuild?namespace="
                        + namespace);
    }

    private Integer asInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private static String profileFor(String configuredProfile, int dimension) {
        return EMBEDDING_PROFILE_VERSION + ":" + configuredProfile.trim() + ":" + dimension;
    }
}
