package com.shiyu.ai.agent.implementation.event.model;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.Map;

/**
 * 表示 Node Execution Started 相关的领域事件或异常信息。
 */
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
     * 执行 Node Execution Started 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @param agentId 用于定位agent的标识。
     * @param nodeId 用于定位node的标识。
     * @param nodeType 用于完成本次业务处理的 nodeType 参数。
     * @param input 用于完成本次业务处理的 input 参数。
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
     * 查询 Node Execution Started 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Started 相关操作生成的结果数据。
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * 查询 Node Execution Started 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Started 相关操作生成的结果数据。
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * 查询 Node Execution Started 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Started 相关操作生成的结果数据。
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * 查询 Node Execution Started 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Started 相关操作生成的结果数据。
     */
    public String getNodeType() {
        return nodeType;
    }

    /**
     * 查询 Node Execution Started 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Started 相关操作生成的结果数据。
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * 查询 Node Execution Started 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node Execution Started 相关操作生成的结果数据。
     */
    public Map<String, Object> getInput() {
        return input;
    }
}
