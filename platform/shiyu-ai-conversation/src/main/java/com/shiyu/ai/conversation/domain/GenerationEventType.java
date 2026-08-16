package com.shiyu.ai.conversation.domain;

public enum GenerationEventType {
    STARTED, DELTA, TOOL_CALL, USAGE, COMPLETED, FAILED, CANCELLED
}
