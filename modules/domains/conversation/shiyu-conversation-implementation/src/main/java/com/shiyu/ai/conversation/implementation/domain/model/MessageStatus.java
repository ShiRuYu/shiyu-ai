package com.shiyu.ai.conversation.implementation.domain.model;

/**
 * {@code MessageStatus} 表示会话模块中的一组受控业务状态或分类。
 */
public enum MessageStatus {
    PENDING,
    STREAMING,
    COMPLETED,
    CANCELLED,
    FAILED,
    DELETED
}
