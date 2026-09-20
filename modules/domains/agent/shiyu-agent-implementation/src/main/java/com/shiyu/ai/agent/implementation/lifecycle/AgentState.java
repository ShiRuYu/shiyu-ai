package com.shiyu.ai.agent.implementation.lifecycle;

/**
 * 定义 智能体 可用的枚举值及其业务语义。
 */
public enum AgentState {
    /** 已创建 */
    CREATED,
    /** 已部署 */
    DEPLOYED,
    /** 已停用 */
    DISABLED,
    /** 已归档 */
    ARCHIVED
}
