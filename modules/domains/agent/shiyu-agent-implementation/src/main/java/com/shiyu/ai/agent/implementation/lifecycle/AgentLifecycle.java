package com.shiyu.ai.agent.implementation.lifecycle;

/** Agent 生命周期管理接口 */
public interface AgentLifecycle {

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param agentId 方法参数。
     *
     * @return 操作结果。
     */
    AgentState getState(String agentId);

    /**
     * 执行 {@code deploy} 定义的接口操作。
     *
     * @param agentId 方法参数。
     */
    void deploy(String agentId);

    /**
     * 执行 {@code disable} 定义的接口操作。
     *
     * @param agentId 方法参数。
     */
    void disable(String agentId);

    /**
     * 执行 {@code archive} 定义的接口操作。
     *
     * @param agentId 方法参数。
     */
    void archive(String agentId);
}
