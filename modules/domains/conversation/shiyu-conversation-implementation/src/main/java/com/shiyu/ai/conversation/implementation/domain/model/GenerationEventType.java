package com.shiyu.ai.conversation.implementation.domain.model;

/**
 * 定义 生成 事件 Type 可用的枚举值及其业务语义。
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
