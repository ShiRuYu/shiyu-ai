package com.shiyu.ai.agent.implementation.lifecycle;

/**
 * 定义 智能体 Lifecycle 相关的协作契约和调用边界。
 */
public interface AgentLifecycle {

    /**
     * 查询 智能体 Lifecycle 相关业务数据，并返回处理结果。
     *
     * @param agentId 用于定位agent的标识。
     * @return 返回 智能体 Lifecycle 相关操作生成的结果数据。
     */
    AgentState getState(String agentId);

    /**
     * 执行 智能体 Lifecycle 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentId 用于定位agent的标识。
     */
    void deploy(String agentId);

    /**
     * 更新或设置 智能体 Lifecycle 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentId 用于定位agent的标识。
     */
    void disable(String agentId);

    /**
     * 执行 智能体 Lifecycle 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentId 用于定位agent的标识。
     */
    void archive(String agentId);
}
