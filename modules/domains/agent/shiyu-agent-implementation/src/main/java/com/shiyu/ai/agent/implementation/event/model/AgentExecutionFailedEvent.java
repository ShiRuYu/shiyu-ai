package com.shiyu.ai.agent.implementation.event.model;

/**
 * 表示 智能体 Execution Failed 相关的领域事件或异常信息。
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
     * 执行 智能体 Execution Failed 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param executionId 用于定位execution的标识。
     * @param agentId 用于定位agent的标识。
     * @param errorMessage 用于完成本次业务处理的 errorMessage 参数。
     */
    public AgentExecutionFailedEvent(String executionId, String agentId, String errorMessage) {
        super("AGENT_EXECUTION_FAILED");
        this.executionId = executionId;
        this.agentId = agentId;
        this.errorMessage = errorMessage;
    }

    /**
     * 查询 智能体 Execution Failed 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 Execution Failed 相关操作生成的结果数据。
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * 查询 智能体 Execution Failed 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 Execution Failed 相关操作生成的结果数据。
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * 查询 智能体 Execution Failed 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 Execution Failed 相关操作生成的结果数据。
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
