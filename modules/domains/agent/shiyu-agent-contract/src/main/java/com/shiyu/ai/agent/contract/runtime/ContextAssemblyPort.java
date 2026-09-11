package com.shiyu.ai.agent.contract.runtime;

/** Cross-domain context retrieval boundary owned by Agent's runtime assembly. */
public interface ContextAssemblyPort {
    /** Retrieves context items together with trace information for observability. */
    ContextResult retrieve(ContextQuery query);

    /** Immutable result returned by context assembly. */
    record ContextResult(java.util.List<ContextItem> items, ContextTrace trace) {
        public ContextResult {
            items = items == null ? java.util.List.of() : java.util.List.copyOf(items);
        }
    }
}
