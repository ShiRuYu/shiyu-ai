package com.shiyu.ai.agent.implementation.event.model;

import java.util.Map;

/**
 * {@code AgentExecutionCompletedEvent} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public class AgentExecutionCompletedEvent extends DomainEvent {

    /**
     * 执行标识，表示当前对象中的对应属性。
     */
    private final String executionId;
    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String agentId;
    /**
     * 输出，表示当前对象中的对应属性。
     */
    private final Map<String, Object> output;
    /**
     * durationMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long durationMs;

    /**
     * {@code AgentExecutionCompletedEvent} 创建并初始化当前类型实例。
     *
     * @param executionId 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param output 参数值，用于执行当前操作。
     * @param durationMs 参数值，用于执行当前操作。
     */
    public AgentExecutionCompletedEvent(
            String executionId, String agentId, Map<String, Object> output, long durationMs) {
        super("AGENT_EXECUTION_COMPLETED");
        this.executionId = executionId;
        this.agentId = agentId;
        this.output = output;
        this.durationMs = durationMs;
    }

    /**
     * {@code getExecutionId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * {@code getAgentId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * {@code getOutput} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> getOutput() {
        return output;
    }

    /**
     * {@code getDurationMs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public long getDurationMs() {
        return durationMs;
    }
}
