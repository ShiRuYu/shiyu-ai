package com.shiyu.ai.agent.implementation.event.model;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.Map;

/** 节点执行开始事件 */
public class NodeExecutionStartedEvent extends DomainEvent {

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
     * 输入，表示当前对象中的对应属性。
     */
    private final Map<String, Object> input;

    /**
     * {@code NodeExecutionStartedEvent} 创建并初始化当前类型实例。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param nodeId 参数值，用于执行当前操作。
     * @param nodeType 参数值，用于执行当前操作。
     * @param input 参数值，用于执行当前操作。
     */
    public NodeExecutionStartedEvent(
            TenantId tenantId,
            String executionId,
            String agentId,
            String nodeId,
            String nodeType,
            Map<String, Object> input) {
        super("NODE_EXECUTION_STARTED");
        if (tenantId == null || tenantId.value() <= 0) {
            throw new IllegalArgumentException("tenantId is required");
        }
        this.tenantId = tenantId;
        this.executionId = executionId;
        this.agentId = agentId;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.input = input;
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
     * {@code getNodeId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * {@code getNodeType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getNodeType() {
        return nodeType;
    }

    /**
     * {@code getTenantId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * {@code getInput} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> getInput() {
        return input;
    }
}
