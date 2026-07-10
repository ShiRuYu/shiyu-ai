package com.shiyu.ai.agent.execution;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Agent 执行实例
 */
public class Execution {

    private final String executionId;
    private final String agentId;
    private final String version;
    private ExecutionStatus status;
    private final Map<String, Object> input;
    private Map<String, Object> output;
    private String errorMessage;
    private Long userId;
    private String sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private final List<NodeExecution> nodeExecutions;
    private String lastCheckpointId;

    public Execution(String agentId, String version, Map<String, Object> input) {
        this.executionId = UUID.randomUUID().toString().replace("-", "");
        this.agentId = agentId;
        this.version = version;
        this.status = ExecutionStatus.PENDING;
        this.input = input;
        this.nodeExecutions = new ArrayList<>();
    }

    public void start() {
        this.status = ExecutionStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    public void complete(Map<String, Object> output) {
        this.status = ExecutionStatus.COMPLETED;
        this.output = output;
        this.endTime = LocalDateTime.now();
        this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
    }

    public void fail(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    public void pause() {
        this.status = ExecutionStatus.PAUSED;
    }

    public void resume() {
        this.status = ExecutionStatus.RUNNING;
    }

    public void cancel() {
        this.status = ExecutionStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    public void addNodeExecution(NodeExecution nodeExecution) {
        this.nodeExecutions.add(nodeExecution);
    }

    // Getters
    public String getExecutionId() { return executionId; }
    public String getAgentId() { return agentId; }
    public String getVersion() { return version; }
    public ExecutionStatus getStatus() { return status; }
    public Map<String, Object> getInput() { return input; }
    public Map<String, Object> getOutput() { return output; }
    public String getErrorMessage() { return errorMessage; }
    public Long getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Long getDurationMs() { return durationMs; }
    public List<NodeExecution> getNodeExecutions() { return nodeExecutions; }
    public String getLastCheckpointId() { return lastCheckpointId; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public void setLastCheckpointId(String lastCheckpointId) { this.lastCheckpointId = lastCheckpointId; }
}
