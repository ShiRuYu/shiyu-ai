package com.shiyu.ai.conversation.implementation.domain.model;

/**
 * 定义 消息 可用的枚举值及其业务语义。
 */
public enum MessageStatus {
    PENDING,
    STREAMING,
    COMPLETED,
    CANCELLED,
    FAILED,
    DELETED
}
