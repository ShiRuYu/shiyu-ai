package com.shiyu.ai.agent.implementation.execution;

import java.time.LocalDateTime;

/** 节点执行记录 */
public class NodeExecution {

    /**
     * nodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String nodeId;
    /**
     * nodeType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String nodeType;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private ExecutionStatus status;
    /**
     * 输入，表示当前对象中的对应属性。
     */
    private Object input;
    /**
     * 输出，表示当前对象中的对应属性。
     */
    private Object output;
    /**
     * 错误消息，表示当前对象中的对应属性。
     */
    private String errorMessage;
    /**
     * startTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime startTime;
    /**
     * endTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime endTime;
    /**
     * durationMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long durationMs;
    /**
     * retryCount 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int retryCount;

    /**
     * {@code NodeExecution} 创建并初始化当前类型实例。
     *
     * @param nodeId 参数值，用于执行当前操作。
     * @param nodeType 参数值，用于执行当前操作。
     */
    public NodeExecution(String nodeId, String nodeType) {
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.status = ExecutionStatus.PENDING;
        this.retryCount = 0;
    }

    /**
     * {@code start} 执行当前类型定义的业务操作。
     */
    public void start() {
        this.status = ExecutionStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    /**
     * {@code complete} 执行当前类型定义的业务操作。
     *
     * @param output 参数值，用于执行当前操作。
     */
    public void complete(Object output) {
        this.status = ExecutionStatus.COMPLETED;
        this.output = output;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * {@code fail} 执行当前类型定义的业务操作。
     *
     * @param errorMessage 参数值，用于执行当前操作。
     */
    public void fail(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * {@code incrementRetry} 执行当前类型定义的业务操作。
     */
    public void incrementRetry() {
        this.retryCount++;
    }

    // Getters
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
     * {@code getStatus} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public ExecutionStatus getStatus() {
        return status;
    }

    /**
     * {@code getInput} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Object getInput() {
        return input;
    }

    /**
     * {@code getOutput} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Object getOutput() {
        return output;
    }

    /**
     * {@code getErrorMessage} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * {@code getStartTime} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * {@code getEndTime} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * {@code getDurationMs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Long getDurationMs() {
        return durationMs;
    }

    /**
     * {@code getRetryCount} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * {@code setInput} 写入或更新当前模块中的业务数据。
     *
     * @param input 参数值，用于执行当前操作。
     */
    public void setInput(Object input) {
        this.input = input;
    }
}
