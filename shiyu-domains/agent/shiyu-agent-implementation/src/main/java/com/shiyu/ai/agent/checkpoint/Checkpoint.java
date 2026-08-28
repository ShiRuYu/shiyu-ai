package com.shiyu.ai.agent.checkpoint;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import com.shiyu.ai.kernel.context.TenantId;

/**
 * 检查点数据
 */
public class Checkpoint {

    private final String checkpointId;
    private final TenantId tenantId;
    private final String executionId;
    private final String nodeId;
    private final Map<String, Object> state;
    private byte[] serializedState;
    private final LocalDateTime createdAt;

    public Checkpoint(TenantId tenantId, String executionId, String nodeId, Map<String, Object> state) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        this.checkpointId = UUID.randomUUID().toString().replace("-", "");
        this.tenantId = tenantId;
        this.executionId = executionId;
        this.nodeId = nodeId;
        this.state = state;
        this.createdAt = LocalDateTime.now();
    }

    public String getCheckpointId() { return checkpointId; }
    public TenantId getTenantId() { return tenantId; }
    public String getExecutionId() { return executionId; }
    public String getNodeId() { return nodeId; }
    public Map<String, Object> getState() { return state; }
    public byte[] getSerializedState() { return serializedState; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setSerializedState(byte[] serializedState) { this.serializedState = serializedState; }
}
