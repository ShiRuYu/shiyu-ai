package com.shiyu.ai.memory.magma;

import com.shiyu.ai.kernel.context.TenantId;
import java.time.Instant;
import java.util.Set;

public record MemoryQuery(
        TenantId tenantId,
        String namespace,
        String subjectType,
        String subjectId,
        String text,
        Set<GraphType> graphTypes,
        Instant from,
        Instant to,
        int maxDepth,
        int maxNodes,
        int maxTokens,
        MemoryQueryIntent intent
) {
    /** Source-compatible constructor for callers that do not provide intent. */
    public MemoryQuery(TenantId tenantId, String namespace, String subjectType, String subjectId, String text,
                       Set<GraphType> graphTypes, Instant from, Instant to, int maxDepth, int maxNodes, int maxTokens) {
        this(tenantId, namespace, subjectType, subjectId, text, graphTypes, from, to, maxDepth, maxNodes, maxTokens,
                MemoryQueryIntent.infer(text));
    }

    public MemoryQuery {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("query text is required");
        if (namespace == null || namespace.isBlank()) throw new IllegalArgumentException("namespace is required");
        graphTypes = graphTypes == null || graphTypes.isEmpty()
                ? Set.of(GraphType.SEMANTIC, GraphType.TEMPORAL, GraphType.ENTITY, GraphType.CAUSAL)
                : Set.copyOf(graphTypes);
        maxDepth = maxDepth <= 0 ? 2 : Math.min(maxDepth, 8);
        maxNodes = maxNodes <= 0 ? 20 : Math.min(maxNodes, 200);
        maxTokens = maxTokens <= 0 ? 2000 : Math.min(maxTokens, 16000);
        intent = intent == null ? MemoryQueryIntent.infer(text) : intent;
    }
}
