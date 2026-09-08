package com.shiyu.ai.runtime;

/** Cross-domain context retrieval boundary owned by Agent's runtime assembly. */
public interface ContextAssemblyPort {
    ContextResult retrieve(ContextQuery query);

    record ContextResult(java.util.List<ContextItem> items, ContextTrace trace) {
        public ContextResult {
            items = items == null ? java.util.List.of() : java.util.List.copyOf(items);
        }
    }
}
