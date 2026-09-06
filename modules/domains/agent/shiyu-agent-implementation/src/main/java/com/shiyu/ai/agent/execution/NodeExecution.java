package com.shiyu.ai.agent.execution;

import java.time.LocalDateTime;

/**
 * 节点执行记录
 */
public class NodeExecution {

    private final String nodeId;
    private final String nodeType;
    private ExecutionStatus status;
    private Object input;
    private Object output;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private int retryCount;

    public NodeExecution(String nodeId, String nodeType) {
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.status = ExecutionStatus.PENDING;
        this.retryCount = 0;
    }

    public void start() {
        this.status = ExecutionStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    public void complete(Object output) {
        this.status = ExecutionStatus.COMPLETED;
        this.output = output;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    public void fail(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    // Getters
    public String getNodeId() { return nodeId; }
    public String getNodeType() { return nodeType; }
    public ExecutionStatus getStatus() { return status; }
    public Object getInput() { return input; }
    public Object getOutput() { return output; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Long getDurationMs() { return durationMs; }
    public int getRetryCount() { return retryCount; }

    public void setInput(Object input) { this.input = input; }
}
