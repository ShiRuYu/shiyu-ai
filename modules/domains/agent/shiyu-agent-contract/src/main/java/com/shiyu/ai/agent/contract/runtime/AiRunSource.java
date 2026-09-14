package com.shiyu.ai.agent.contract.runtime;

/**
 * {@code AiRunSource} 表示智能体模块中的一组受控业务状态或分类。
 */
public enum AiRunSource {
    CONVERSATION,
    GENERATION,
    AGENT,
    KNOWLEDGE,
    MEMORY,
    TOOL,
    API
}
