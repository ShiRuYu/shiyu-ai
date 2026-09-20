package com.shiyu.ai.agent.implementation.event.model;

import java.util.Map;

/**
 * 表示 智能体 Execution Started 相关的领域事件或异常信息。
 */
public class AgentExecutionStartedEvent extends DomainEvent {

    /**
     * 执行标识，表示当前对象中的对应属性。
     */
    private final String executionId;
    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String agentId;
    /**
     * 输入，表示当前对象中的对应属性。
     */
    private final Map<String, Object> input;

    /**
     * 执行 智能体 Execution Started 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param executionId 用于定位execution的标识。
     * @param agentId 用于定位agent的标识。
     * @param input 用于完成本次业务处理的 input 参数。
     */
    public AgentExecutionStartedEvent(
            String executionId, String agentId, Map<String, Object> input) {
        super("AGENT_EXECUTION_STARTED");
        this.executionId = executionId;
        this.agentId = agentId;
        this.input = input;
    }

    /**
     * 查询 智能体 Execution Started 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 Execution Started 相关操作生成的结果数据。
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * 查询 智能体 Execution Started 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 Execution Started 相关操作生成的结果数据。
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * 查询 智能体 Execution Started 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 Execution Started 相关操作生成的结果数据。
     */
    public Map<String, Object> getInput() {
        return input;
    }
}
