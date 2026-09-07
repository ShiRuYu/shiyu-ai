package com.shiyu.ai.agent.execution;

/**
 * 执行实例状态枚举
 */
public enum ExecutionStatus {
    /** 等待执行 */
    PENDING,
    /** 执行中 */
    RUNNING,
    /** 已暂停 */
    PAUSED,
    /** 已完成 */
    COMPLETED,
    /** 执行失败 */
    FAILED,
    /** 已取消 */
    CANCELLED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    public boolean canPause() {
        return this == RUNNING;
    }

    public boolean canResume() {
        return this == PAUSED;
    }
}
