package com.shiyu.ai.conversation.implementation.domain.model;

public enum GenerationEventType {
    STARTED, BLOCK_STARTED, DELTA, REASONING_DELTA, TOOL_CALL, BLOCK_COMPLETED,
    USAGE, COMPLETED, FAILED, CANCELLED
}
