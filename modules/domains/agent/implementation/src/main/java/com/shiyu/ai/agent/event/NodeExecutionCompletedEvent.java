package com.shiyu.ai.agent.event;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.Map;

/**
 * 节点执行完成事件
 */
public class NodeExecutionCompletedEvent extends DomainEvent {

    private final String executionId;
    private final String agentId;
    private final String nodeId;
    private final String nodeType;
    private final TenantId tenantId;
    private final Map<String, Object> output;
    private final String status;
    private final long durationMs;

    public NodeExecutionCompletedEvent(TenantId tenantId, String executionId, String agentId,
                                       String nodeId, String nodeType, Map<String, Object> output,
                                       String status, long durationMs) {
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

    public String getExecutionId() { return executionId; }
    public String getAgentId() { return agentId; }
    public String getNodeId() { return nodeId; }
    public String getNodeType() { return nodeType; }
    public TenantId getTenantId() { return tenantId; }
    public Map<String, Object> getOutput() { return output; }
    public String getStatus() { return status; }
    public long getDurationMs() { return durationMs; }
}
