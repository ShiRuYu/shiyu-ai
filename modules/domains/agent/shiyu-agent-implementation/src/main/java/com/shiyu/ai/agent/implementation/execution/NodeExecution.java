package com.shiyu.ai.agent.implementation.execution;

import java.time.LocalDateTime;

/**
 * 表示 Node 相关流程中的状态、关系或执行数据。
 */
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
     * 执行 Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param nodeId 用于定位node的标识。
     * @param nodeType 用于完成本次业务处理的 nodeType 参数。
     */
    public NodeExecution(String nodeId, String nodeType) {
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.status = ExecutionStatus.PENDING;
        this.retryCount = 0;
    }

    /**
     * 执行 Node 相关业务操作，并维护必要的状态和协作关系。
     */
    public void start() {
        this.status = ExecutionStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    /**
     * 执行 Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param output 用于完成本次业务处理的 output 参数。
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
     * 执行 Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param errorMessage 用于完成本次业务处理的 errorMessage 参数。
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
     * 执行 Node 相关业务操作，并维护必要的状态和协作关系。
     */
    public void incrementRetry() {
        this.retryCount++;
    }

    // Getters
    /**
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public String getNodeType() {
        return nodeType;
    }

    /**
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public ExecutionStatus getStatus() {
        return status;
    }

    /**
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public Object getInput() {
        return input;
    }

    /**
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public Object getOutput() {
        return output;
    }

    /**
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public Long getDurationMs() {
        return durationMs;
    }

    /**
     * 查询 Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Node 相关操作生成的结果数据。
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 更新或设置 Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param input 用于完成本次业务处理的 input 参数。
     */
    public void setInput(Object input) {
        this.input = input;
    }
}
