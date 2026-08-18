package com.shiyu.ai.memory.magma;

import com.shiyu.ai.runtime.ContextCitation;
import com.shiyu.ai.runtime.ContextItem;
import com.shiyu.ai.runtime.ContextQuery;
import com.shiyu.ai.runtime.ContextRetrievalPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MagmaContextRetrievalAdapter implements ContextRetrievalPort {
    private final MemoryQueryPort memory;
    public MagmaContextRetrievalAdapter(MemoryQueryPort memory) { this.memory = memory; }
    @Override public List<ContextItem> retrieve(ContextQuery query) {
        if (query.namespace() != null && !query.namespace().isBlank()
                && !"memory".equalsIgnoreCase(query.namespace()) && !"magma".equalsIgnoreCase(query.namespace())) return List.of();
        // Memory is subject-scoped by design.  A missing subject filter must
        // fail closed rather than turning a tenant query into a cross-user
        // recall operation.  Domain adapters can explicitly choose a subject
        // (user, execution, document, etc.) through the common contract.
        String subjectType = query.filters().get("subjectType");
        String subjectId = query.filters().get("subjectId");
        if (subjectType == null || subjectType.isBlank() || subjectId == null || subjectId.isBlank()) return List.of();
        MemoryQuery magmaQuery = new MemoryQuery(query.tenantId(), query.namespace(), subjectType, subjectId, query.text(), null, null, null, 2, query.topK(), 2000);
        return memory.retrieve(magmaQuery).stream().map(path -> new ContextItem("MEMORY_EVENT", path.event().id(), path.event().createdAt().toString(), path.event().content(), path.score(), new ContextCitation(path.event().eventType(), path.event().sourceId(), path.event().occurredAt().toString(), null), path.edges().stream().map(e -> e.graphType().name() + ":" + e.relationType()).toList(), "tenant-subject", 0, path.event().occurredAt())).toList();
    }
}
