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
    private volatile ExecutionStatus status;
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
        this(UUID.randomUUID().toString().replace("-", ""), agentId, version, ExecutionStatus.PENDING, input);
    }

    private Execution(String executionId, String agentId, String version,
                      ExecutionStatus status, Map<String, Object> input) {
        this.executionId = executionId;
        this.agentId = agentId;
        this.version = version;
        this.status = status;
        this.input = input;
        this.nodeExecutions = new ArrayList<>();
    }

    /**
     * Rebuild a persisted execution without changing its identity or lifecycle data.
     */
    public static Execution restore(String executionId, String agentId, String version,
                                    ExecutionStatus status, Map<String, Object> input,
                                    Map<String, Object> output, String errorMessage,
                                    Long userId, String sessionId, LocalDateTime startTime,
                                    LocalDateTime endTime, Long durationMs) {
        Execution execution = new Execution(executionId, agentId, version,
                status == null ? ExecutionStatus.PENDING : status, input);
        execution.output = output;
        execution.errorMessage = errorMessage;
        execution.userId = userId;
        execution.sessionId = sessionId;
        execution.startTime = startTime;
        execution.endTime = endTime;
        execution.durationMs = durationMs;
        return execution;
    }

    public synchronized void start() {
        this.status = ExecutionStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    public synchronized void complete(Map<String, Object> output) {
        this.status = ExecutionStatus.COMPLETED;
        this.output = output;
        this.endTime = LocalDateTime.now();
        this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        notifyAll();
    }

    public synchronized void fail(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
        notifyAll();
    }

    public synchronized void pause() {
        this.status = ExecutionStatus.PAUSED;
    }

    public synchronized void resume() {
        this.status = ExecutionStatus.RUNNING;
        notifyAll();
    }

    public synchronized void cancel() {
        this.status = ExecutionStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
        notifyAll();
    }

    /**
     * Blocks a cooperative executor while this execution is paused.
     *
     * @return {@code false} when the execution was cancelled or interrupted
     */
    public synchronized boolean awaitResumeOrCancellation() {
        while (status == ExecutionStatus.PAUSED) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                cancel();
                return false;
            }
        }
        return status != ExecutionStatus.CANCELLED;
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
