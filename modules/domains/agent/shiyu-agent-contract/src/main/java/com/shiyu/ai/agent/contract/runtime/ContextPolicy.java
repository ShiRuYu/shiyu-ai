package com.shiyu.ai.agent.contract.runtime;

public interface ContextPolicy {
    boolean canRead(ContextItem item, ContextQuery query);
}
