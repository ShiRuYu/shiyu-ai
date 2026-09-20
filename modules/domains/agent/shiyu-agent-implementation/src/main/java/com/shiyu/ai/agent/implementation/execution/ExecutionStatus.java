package com.shiyu.ai.agent.implementation.execution;

/**
 * 定义 Execution 可用的枚举值及其业务语义。
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

    /**
     * 校验或判断 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    /**
     * 校验或判断 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    public boolean canPause() {
        return this == RUNNING;
    }

    /**
     * 校验或判断 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    public boolean canResume() {
        return this == PAUSED;
    }
}
