package com.shiyu.ai.agent.implementation.event.model;

import java.util.Map;

/**
 * 表示 智能体 Execution Completed 相关的领域事件或异常信息。
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
     * 执行 智能体 Execution Completed 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param executionId 用于定位execution的标识。
     * @param agentId 用于定位agent的标识。
     * @param output 用于完成本次业务处理的 output 参数。
     * @param durationMs 用于完成本次业务处理的 durationMs 参数。
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
     * 查询 智能体 Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 Execution Completed 相关操作生成的结果数据。
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * 查询 智能体 Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 Execution Completed 相关操作生成的结果数据。
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * 查询 智能体 Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 Execution Completed 相关操作生成的结果数据。
     */
    public Map<String, Object> getOutput() {
        return output;
    }

    /**
     * 查询 智能体 Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 Execution Completed 相关操作生成的结果数据。
     */
    public long getDurationMs() {
        return durationMs;
    }
}
