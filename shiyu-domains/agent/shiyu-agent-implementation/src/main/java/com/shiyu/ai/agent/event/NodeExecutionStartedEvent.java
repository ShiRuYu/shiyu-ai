package com.shiyu.ai.agent.event;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.Map;

/**
 * 节点执行开始事件
 */
public class NodeExecutionStartedEvent extends DomainEvent {

    private final String executionId;
    private final String agentId;
    private final String nodeId;
    private final String nodeType;
    private final TenantId tenantId;
    private final Map<String, Object> input;

    public NodeExecutionStartedEvent(TenantId tenantId, String executionId, String agentId,
                                     String nodeId, String nodeType, Map<String, Object> input) {
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

    public String getExecutionId() { return executionId; }
    public String getAgentId() { return agentId; }
    public String getNodeId() { return nodeId; }
    public String getNodeType() { return nodeType; }
    public TenantId getTenantId() { return tenantId; }
    public Map<String, Object> getInput() { return input; }
}
