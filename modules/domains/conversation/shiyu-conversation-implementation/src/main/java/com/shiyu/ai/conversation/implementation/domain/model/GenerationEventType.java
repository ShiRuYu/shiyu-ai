package com.shiyu.ai.conversation.implementation.domain.model;

/**
 * {@code GenerationEventType} 表示会话模块中的一组受控业务状态或分类。
 */
public enum GenerationEventType {
    STARTED,
    BLOCK_STARTED,
    DELTA,
    REASONING_DELTA,
    TOOL_CALL,
    BLOCK_COMPLETED,
    USAGE,
    COMPLETED,
    FAILED,
    CANCELLED
}
