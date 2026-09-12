package com.shiyu.ai.memory.contract.model;

/**
 * {@code MemoryEventStatus} 表示模型模块中的一组受控业务状态或分类。
 */
public enum MemoryEventStatus {
    CANDIDATE,
    ACTIVE,
    SUPERSEDED,
    REVOKED
}
