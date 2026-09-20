package com.shiyu.ai.agent.implementation.service;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.kernel.context.ActorContext;

import java.util.List;

/**
 * 提供 智能体 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface AgentService {

    /** 注册 Agent 定义 */
    void registerAgent(ActorContext actor, AgentDefinition agentDefinition);

    /**
     * 注册systemagent。
     *
     * @param agentDefinition agentDefinition 参数。
     */
    void registerSystemAgent(AgentDefinition agentDefinition);

    /** 获取 Agent 定义 */
    AgentDefinition getAgent(ActorContext actor, String agentId);

    /** 注销 Agent 定义 */
    boolean unregisterAgent(ActorContext actor, String agentId);

    /** 切换 Agent 当前版本 */
    boolean switchVersion(ActorContext actor, String agentId, String version);

    /** 列出所有已注册的 Agent */
    List<AgentDefinition> listAgents(ActorContext actor);

    /** 清理 Agent 的运行时缓存（本地内存 + AgentCacheManager） */
    void evictRuntimeCache(String agentId);
}
