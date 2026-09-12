package com.shiyu.ai.agent.implementation.execution;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Agent 执行实例 */
public class Execution {

    /**
     * 执行标识，表示当前对象中的对应属性。
     */
    private final String executionId;
    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String agentId;
    /**
     * 版本，表示当前对象中的对应属性。
     */
    private final String version;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private volatile ExecutionStatus status;
    /**
     * 输入，表示当前对象中的对应属性。
     */
    private final Map<String, Object> input;
    /**
     * 输出，表示当前对象中的对应属性。
     */
    private Map<String, Object> output;
    /**
     * 错误消息，表示当前对象中的对应属性。
     */
    private String errorMessage;
    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private Long userId;
    /**
     * sessionId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String sessionId;
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
     * nodeExecutions 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<NodeExecution> nodeExecutions;
    /**
     * lastCheckpointId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String lastCheckpointId;

    /**
     * {@code Execution} 创建并初始化当前类型实例。
     *
     * @param agentId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     * @param input 参数值，用于执行当前操作。
     */
    public Execution(String agentId, String version, Map<String, Object> input) {
        this(
                UUID.randomUUID().toString().replace("-", ""),
                agentId,
                version,
                ExecutionStatus.PENDING,
                input);
    }

    private Execution(
            String executionId,
            String agentId,
            String version,
            ExecutionStatus status,
            Map<String, Object> input) {
        this.executionId = executionId;
        this.agentId = agentId;
        this.version = version;
        this.status = status;
        this.input = input;
        this.nodeExecutions = new ArrayList<>();
    }

    /**
     * 处理restore。
     *
     * @param executionId 执行记录标识。
     * @param agentId agentId 参数。
     * @param version version 参数。
     * @param status 状态。
     * @param input input 参数。
     * @param output output 参数。
     * @param errorMessage errorMessage 参数。
     * @param userId 用户标识。
     * @param sessionId sessionId 参数。
     * @param startTime startTime 参数。
     * @param endTime endTime 参数。
     * @param durationMs durationMs 参数。
     *
     * @return 处理结果。
     */
    public static Execution restore(
            String executionId,
            String agentId,
            String version,
            ExecutionStatus status,
            Map<String, Object> input,
            Map<String, Object> output,
            String errorMessage,
            Long userId,
            String sessionId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Long durationMs) {
        Execution execution =
                new Execution(
                        executionId,
                        agentId,
                        version,
                        status == null ? ExecutionStatus.PENDING : status,
                        input);
        execution.output = output;
        execution.errorMessage = errorMessage;
        execution.userId = userId;
        execution.sessionId = sessionId;
        execution.startTime = startTime;
        execution.endTime = endTime;
        execution.durationMs = durationMs;
        return execution;
    }

    /**
     * {@code start} 执行当前类型定义的业务操作。
     */
    public synchronized void start() {
        this.status = ExecutionStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    /**
     * {@code complete} 执行当前类型定义的业务操作。
     *
     * @param output 参数值，用于执行当前操作。
     */
    public synchronized void complete(Map<String, Object> output) {
        this.status = ExecutionStatus.COMPLETED;
        this.output = output;
        this.endTime = LocalDateTime.now();
        this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        notifyAll();
    }

    /**
     * {@code fail} 执行当前类型定义的业务操作。
     *
     * @param errorMessage 参数值，用于执行当前操作。
     */
    public synchronized void fail(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
        notifyAll();
    }

    /**
     * {@code pause} 执行当前类型定义的业务操作。
     */
    public synchronized void pause() {
        this.status = ExecutionStatus.PAUSED;
    }

    /**
     * {@code resume} 执行当前类型定义的业务操作。
     */
    public synchronized void resume() {
        this.status = ExecutionStatus.RUNNING;
        notifyAll();
    }

    /**
     * {@code cancel} 校验当前操作的输入或状态是否满足约束。
     */
    public synchronized void cancel() {
        this.status = ExecutionStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
        notifyAll();
    }

    /**
     * 处理awaitresumeorcancellation。
     *
     * @return 判断结果。
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

    /**
     * {@code addNodeExecution} 执行当前类型定义的业务操作。
     *
     * @param nodeExecution 参数值，用于执行当前操作。
     */
    public void addNodeExecution(NodeExecution nodeExecution) {
        this.nodeExecutions.add(nodeExecution);
    }

    // Getters
    /**
     * {@code getExecutionId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * {@code getAgentId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * {@code getVersion} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getVersion() {
        return version;
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
    public Map<String, Object> getInput() {
        return input;
    }

    /**
     * {@code getOutput} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> getOutput() {
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
     * {@code getUserId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * {@code getSessionId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getSessionId() {
        return sessionId;
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
     * {@code getNodeExecutions} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<NodeExecution> getNodeExecutions() {
        return nodeExecutions;
    }

    /**
     * {@code getLastCheckpointId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getLastCheckpointId() {
        return lastCheckpointId;
    }

    /**
     * {@code setUserId} 写入或更新当前模块中的业务数据。
     *
     * @param userId 参数值，用于执行当前操作。
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * {@code setSessionId} 写入或更新当前模块中的业务数据。
     *
     * @param sessionId 参数值，用于执行当前操作。
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * {@code setLastCheckpointId} 写入或更新当前模块中的业务数据。
     *
     * @param lastCheckpointId 参数值，用于执行当前操作。
     */
    public void setLastCheckpointId(String lastCheckpointId) {
        this.lastCheckpointId = lastCheckpointId;
    }
}
