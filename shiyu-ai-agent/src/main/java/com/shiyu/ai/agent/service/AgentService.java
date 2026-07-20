package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.AgentDefinition;

import java.util.List;

/**
 * Agent Service 接口
 * 提供 Agent 定义管理、版本控制能力。
 * 注意：Agent 执行统一走 {@link com.shiyu.ai.agent.runtime.AgentRuntime}，不再通过此接口。
 */
public interface AgentService {

    /**
     * 注册 Agent 定义
     */
    void registerAgent(AgentDefinition agentDefinition);

    /**
     * 获取 Agent 定义
     */
    AgentDefinition getAgent(String agentId);

    /**
     * 注销 Agent 定义
     */
    boolean unregisterAgent(String agentId);

    /**
     * 切换 Agent 当前版本
     */
    boolean switchVersion(String agentId, String version);

    /**
     * 列出所有已注册的 Agent
     */
    List<AgentDefinition> listAgents();

    /**
     * 清理 Agent 的运行时缓存（本地内存 + AgentCacheManager）
     */
    void evictRuntimeCache(String agentId);
}
