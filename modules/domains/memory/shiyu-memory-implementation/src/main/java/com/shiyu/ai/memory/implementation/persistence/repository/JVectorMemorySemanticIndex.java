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
 * {@code JVectorMemorySemanticIndex} 承载平台模块的领域状态或协作行为，负责维护本类型的职责边界。
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
     * {@code JVectorMemorySemanticIndex} 创建并初始化当前类型实例。
     *
     * @param store 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     * @param repository 参数值，用于执行当前操作。
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
     * {@code upsert} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
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
     * {@code search} 查询并返回当前操作所需的数据。
     *
     * @param query 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     */
    public void delete(String id) {
        store.delete(id);
        store.flush();
    }

    /**
     * {@code rebuild} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param namespace 参数值，用于执行当前操作。
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
