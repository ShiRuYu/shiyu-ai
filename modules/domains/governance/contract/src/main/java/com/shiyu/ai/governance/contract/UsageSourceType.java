package com.shiyu.ai.governance.contract;

/** Stable source categories used by the usage idempotency key. */
public enum UsageSourceType {
    AGENT_EXECUTION,
    CONVERSATION_GENERATION,
    KNOWLEDGE_INDEXING,
    MODEL_INVOCATION
}
