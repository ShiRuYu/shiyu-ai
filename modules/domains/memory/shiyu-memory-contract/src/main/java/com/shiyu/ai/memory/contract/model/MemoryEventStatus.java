package com.shiyu.ai.memory.contract.model;

/**
 * 定义 记忆 事件 可用的枚举值及其业务语义。
 */
public enum MemoryEventStatus {
    CANDIDATE,
    ACTIVE,
    SUPERSEDED,
    REVOKED
}
