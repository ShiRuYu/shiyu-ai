package com.shiyu.ai.agent.contract.runtime;

/**
 * 定义 AI 运行 可用的枚举值及其业务语义。
 */
public enum AiRunStatus {
    CREATED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
