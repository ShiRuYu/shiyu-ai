package com.shiyu.ai.agent.lifecycle;

/**
 * Agent 生命周期管理接口
 */
public interface AgentLifecycle {

    AgentState getState(String agentId);

    void deploy(String agentId);

    void disable(String agentId);

    void archive(String agentId);
}
