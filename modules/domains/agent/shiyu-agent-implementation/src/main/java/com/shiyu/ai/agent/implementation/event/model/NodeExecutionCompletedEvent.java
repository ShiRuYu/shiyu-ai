package com.shiyu.ai.agent.implementation.event.model;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.Map;

/**
 * 表示 Node Execution Completed 相关的领域事件或异常信息。
 */
public class NodeExecutionCompletedEvent extends DomainEvent {

    /**
     * 执行标识，表示当前对象中的对应属性。
     */
    private final String executionId;
    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String agentId;
    /**
     * nodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String nodeId;
    /**
     * nodeType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String nodeType;
    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private final TenantId tenantId;
    /**
     * 输出，表示当前对象中的对应属性。
     */
    private final Map<String, Object> output;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private final String status;
    /**
     * durationMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long durationMs;

    /**
     * 执行 Node Execution Completed 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @param agentId 用于定位agent的标识。
     * @param nodeId 用于定位node的标识。
     * @param nodeType 用于完成本次业务处理的 nodeType 参数。
     * @param output 用于完成本次业务处理的 output 参数。
     * @param status 用于完成本次业务处理的 status 参数。
     * @param durationMs 用于完成本次业务处理的 durationMs 参数。
     */
    public NodeExecutionCompletedEvent(
            TenantId tenantId,
            String executionId,
            String agentId,
            String nodeId,
            String nodeType,
            Map<String, Object> output,
            String status,
            long durationMs) {
        super("NODE_EXECUTION_COMPLETED");
        if (tenantId == null || tenantId.value() <= 0) {
            throw new IllegalArgumentException("tenantId is required");
        }
        this.tenantId = tenantId;
        this.executionId = executionId;
        this.agentId = agentId;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.output = output;
        this.status = status;
        this.durationMs = durationMs;
    }

    /**
     * 查询 Node Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Completed 相关操作生成的结果数据。
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * 查询 Node Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Completed 相关操作生成的结果数据。
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * 查询 Node Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Completed 相关操作生成的结果数据。
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * 查询 Node Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Completed 相关操作生成的结果数据。
     */
    public String getNodeType() {
        return nodeType;
    }

    /**
     * 查询 Node Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Completed 相关操作生成的结果数据。
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * 查询 Node Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Completed 相关操作生成的结果数据。
     */
    public Map<String, Object> getOutput() {
        return output;
    }

    /**
     * 查询 Node Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Completed 相关操作生成的结果数据。
     */
    public String getStatus() {
        return status;
    }

    /**
     * 查询 Node Execution Completed 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Completed 相关操作生成的结果数据。
     */
    public long getDurationMs() {
        return durationMs;
    }
}
