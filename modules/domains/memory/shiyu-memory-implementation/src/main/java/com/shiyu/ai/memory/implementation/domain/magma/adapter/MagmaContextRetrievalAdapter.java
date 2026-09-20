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
 * 将 Magma Context Retrieval 在不同层之间进行适配、转换或组装。
 */
@Component
public class MagmaContextRetrievalAdapter implements ContextRetrievalPort {
    /**
     * memory 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final MemoryQueryPort memory;

    /**
     * 执行 Magma Context Retrieval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param memory 用于完成本次业务处理的 memory 参数。
     */
    public MagmaContextRetrievalAdapter(MemoryQueryPort memory) {
        this.memory = memory;
    }

    /**
     * 执行 Magma Context Retrieval 相关业务数据，并返回处理结果。
     *
     * @param query 用于筛选目标数据的查询条件。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
