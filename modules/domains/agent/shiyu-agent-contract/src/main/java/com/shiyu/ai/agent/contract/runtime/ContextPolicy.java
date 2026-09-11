package com.shiyu.ai.agent.contract.runtime;

/** Access policy consulted before a context item is exposed to an agent run. */
public interface ContextPolicy {
    /** Returns whether the item may be read for the supplied tenant-scoped query. */
    boolean canRead(ContextItem item, ContextQuery query);
}
