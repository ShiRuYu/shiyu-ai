package com.shiyu.ai.conversation.contract.model;

/**
 * 定义 生成 可用的枚举值及其业务语义。
 */
public enum GenerationStatus {
    CREATED,
    RUNNING,
    COMPLETED,
    CANCELLED,
    FAILED
}
