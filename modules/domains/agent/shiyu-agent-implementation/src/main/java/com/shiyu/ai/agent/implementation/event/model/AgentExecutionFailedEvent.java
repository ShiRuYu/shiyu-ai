package com.shiyu.ai.agent.implementation.event.model;

/**
 * {@code AgentExecutionFailedEvent} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public class AgentExecutionFailedEvent extends DomainEvent {

    /**
     * 执行标识，表示当前对象中的对应属性。
     */
    private final String executionId;
    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String agentId;
    /**
     * 错误消息，表示当前对象中的对应属性。
     */
    private final String errorMessage;

    /**
     * {@code AgentExecutionFailedEvent} 创建并初始化当前类型实例。
     *
     * @param executionId 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param errorMessage 参数值，用于执行当前操作。
     */
    public AgentExecutionFailedEvent(String executionId, String agentId, String errorMessage) {
        super("AGENT_EXECUTION_FAILED");
        this.executionId = executionId;
        this.agentId = agentId;
        this.errorMessage = errorMessage;
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
     * {@code getErrorMessage} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
