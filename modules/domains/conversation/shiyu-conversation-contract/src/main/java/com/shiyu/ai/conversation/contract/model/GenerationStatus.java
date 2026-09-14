package com.shiyu.ai.conversation.contract.model;

/**
 * {@code GenerationStatus} 表示会话模块中的一组受控业务状态或分类。
 */
public enum GenerationStatus {
    CREATED,
    RUNNING,
    COMPLETED,
    CANCELLED,
    FAILED
}
