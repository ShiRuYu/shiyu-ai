package com.shiyu.ai.runtime;

public interface ContextPolicy {
    boolean canRead(ContextItem item, ContextQuery query);
}
