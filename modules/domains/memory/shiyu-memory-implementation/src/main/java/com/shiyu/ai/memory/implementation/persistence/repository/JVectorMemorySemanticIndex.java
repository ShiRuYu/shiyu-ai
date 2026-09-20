package com.shiyu.ai.memory.implementation.persistence.repository;
import com.shiyu.ai.memory.implementation.domain.magma.port.MemorySemanticIndex;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.config.VectorStoreProperties;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorSearchRequest;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 实现 J 向量 记忆 Semantic 索引 相关的业务处理、协作逻辑或基础设施能力。
 */
@Component
public class JVectorMemorySemanticIndex implements MemorySemanticIndex {
    /**
     * 存储，表示当前对象中的对应属性。
     */
    private final VectorStore store;
    /**
     * 配置属性，表示当前对象中的对应属性。
     */
    private final VectorStoreProperties properties;
    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final JdbcMagmaMemoryRepository repository;

    /**
     * 执行 J 向量 记忆 Semantic 索引 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param store 用于完成本次业务处理的 store 参数。
     * @param properties 用于完成本次业务处理的 properties 参数。
     * @param repository 用于完成本次业务处理的 repository 参数。
     */
    public JVectorMemorySemanticIndex(
            VectorStore store,
            VectorStoreProperties properties,
            JdbcMagmaMemoryRepository repository) {
        this.store = store;
        this.properties = properties;
        this.repository = repository;
    }

    /**
     * 执行 J 向量 记忆 Semantic 索引 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    public void upsert(MemoryEvent event) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("tenantId", event.tenantId());
        metadata.put("namespace", event.namespace());
        metadata.put("subjectType", event.subjectType());
        metadata.put("subjectId", event.subjectId());
        metadata.put("status", event.status().name());
        store.upsert(new VectorRecord(event.id(), embed(event.content()), metadata));
        store.flush();
    }

    /**
     * 查询 J 向量 记忆 Semantic 索引 相关业务数据，并返回处理结果。
     *
     * @param query 用于筛选目标数据的查询条件。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<MemoryPath> search(MemoryQuery query, int limit) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("tenantId", query.tenantId().value());
        filter.put("namespace", query.namespace());
        if (query.subjectType() != null && !query.subjectType().isBlank())
            filter.put("subjectType", query.subjectType());
        if (query.subjectId() != null && !query.subjectId().isBlank())
            filter.put("subjectId", query.subjectId());
        List<VectorRecord> records =
                store.search(
                        VectorSearchRequest.builder()
                                .queryVector(embed(query.text()))
                                .topK(limit)
                                .filter(filter)
                                .build());
        List<MemoryPath> out = new ArrayList<>();
        for (VectorRecord r : records) {
            repository
                    .findEvent(query.tenantId(), r.id())
                    .filter(e -> e.status() == MemoryEventStatus.ACTIVE)
                    .ifPresent(
                            e ->
                                    out.add(
                                            new MemoryPath(
                                                    e,
                                                    ((Number)
                                                                    r.metadata()
                                                                            .getOrDefault(
                                                                                    "_score", 0d))
                                                            .doubleValue(),
                                                    List.of())));
        }
        return out;
    }

    /**
     * 删除或移除 J 向量 记忆 Semantic 索引 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
     */
    public void delete(String id) {
        store.delete(id);
        store.flush();
    }

    /**
     * 执行 J 向量 记忆 Semantic 索引 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     */
    public void rebuild(TenantId tenantId, String namespace) {
        repository.findByNamespace(tenantId, namespace, 100000).forEach(this::upsert);
    }

    private float[] embed(String text) {
        int d = Math.max(8, properties.getDimension());
        float[] v = new float[d];
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) v[i % d] += ((bytes[i] & 0xff) - 128) / 128f;
        float norm = 0;
        for (float x : v) norm += x * x;
        norm = (float) Math.sqrt(norm);
        if (norm > 0) for (int i = 0; i < v.length; i++) v[i] /= norm;
        return v;
    }
}
