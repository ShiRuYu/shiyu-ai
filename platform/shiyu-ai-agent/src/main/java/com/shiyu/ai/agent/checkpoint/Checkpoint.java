package com.shiyu.ai.agent.checkpoint;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 检查点数据
 */
public class Checkpoint {

    private final String checkpointId;
    private final String executionId;
    private final String nodeId;
    private final Map<String, Object> state;
    private byte[] serializedState;
    private final LocalDateTime createdAt;

    public Checkpoint(String executionId, String nodeId, Map<String, Object> state) {
        this.checkpointId = UUID.randomUUID().toString().replace("-", "");
        this.executionId = executionId;
        this.nodeId = nodeId;
        this.state = state;
        this.createdAt = LocalDateTime.now();
    }

    public String getCheckpointId() { return checkpointId; }
    public String getExecutionId() { return executionId; }
    public String getNodeId() { return nodeId; }
    public Map<String, Object> getState() { return state; }
    public byte[] getSerializedState() { return serializedState; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setSerializedState(byte[] serializedState) { this.serializedState = serializedState; }
}
