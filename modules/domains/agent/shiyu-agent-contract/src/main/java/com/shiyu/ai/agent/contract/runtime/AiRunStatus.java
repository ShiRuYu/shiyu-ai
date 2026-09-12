package com.shiyu.ai.agent.contract.runtime;

/**
 * {@code AiRunStatus} 表示智能体模块中的一组受控业务状态或分类。
 */
public enum AiRunStatus {
    CREATED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
