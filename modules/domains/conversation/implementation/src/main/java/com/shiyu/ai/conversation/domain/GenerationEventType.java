package com.shiyu.ai.conversation.domain;

public enum GenerationEventType {
    STARTED, BLOCK_STARTED, DELTA, REASONING_DELTA, TOOL_CALL, BLOCK_COMPLETED,
    USAGE, COMPLETED, FAILED, CANCELLED
}
