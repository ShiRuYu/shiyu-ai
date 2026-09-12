package com.shiyu.ai.memory.implementation.domain.magma.adapter;

import com.shiyu.ai.agent.contract.runtime.ContextCitation;
import com.shiyu.ai.agent.contract.runtime.ContextItem;
import com.shiyu.ai.agent.contract.runtime.ContextQuery;
import com.shiyu.ai.agent.contract.runtime.ContextRetrievalPort;
import com.shiyu.ai.memory.contract.api.MemoryQueryPort;
import com.shiyu.ai.memory.contract.model.*;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@code MagmaContextRetrievalAdapter} 承载平台模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Component
public class MagmaContextRetrievalAdapter implements ContextRetrievalPort {
    /**
     * memory 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final MemoryQueryPort memory;

    /**
     * {@code MagmaContextRetrievalAdapter} 创建并初始化当前类型实例。
     *
     * @param memory 参数值，用于执行当前操作。
     */
    public MagmaContextRetrievalAdapter(MemoryQueryPort memory) {
        this.memory = memory;
    }

    /**
     * {@code retrieve} 执行当前类型定义的业务操作。
     *
     * @param query 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ContextItem> retrieve(ContextQuery query) {
        if (query.namespace() != null
                && !query.namespace().isBlank()
                && !"memory".equalsIgnoreCase(query.namespace())
                && !"magma".equalsIgnoreCase(query.namespace())) return List.of();
        String subjectType = query.filters().get("subjectType");
        String subjectId = query.filters().get("subjectId");
        if (subjectType == null
                || subjectType.isBlank()
                || subjectId == null
                || subjectId.isBlank()) return List.of();
        MemoryQuery magmaQuery =
                new MemoryQuery(
                        query.tenantId(),
                        query.namespace(),
                        subjectType,
                        subjectId,
                        query.text(),
                        null,
                        null,
                        null,
                        2,
                        query.topK(),
                        2000);
        return memory.retrieve(magmaQuery).stream()
                .map(
                        path ->
                                new ContextItem(
                                        "MEMORY_EVENT",
                                        path.event().id(),
                                        path.event().createdAt().toString(),
                                        path.event().content(),
                                        path.score(),
                                        new ContextCitation(
                                                path.event().eventType(),
                                                path.event().sourceId(),
                                                path.event().occurredAt().toString(),
                                                null),
                                        path.edges().stream()
                                                .map(
                                                        e ->
                                                                e.graphType().name()
                                                                        + ":"
                                                                        + e.relationType())
                                                .toList(),
                                        "tenant-subject",
                                        0,
                                        path.event().occurredAt()))
                .toList();
    }
}
