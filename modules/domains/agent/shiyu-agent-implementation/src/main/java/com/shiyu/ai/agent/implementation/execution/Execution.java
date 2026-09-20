package com.shiyu.ai.agent.implementation.execution;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 表示 Execution 相关流程中的状态、关系或执行数据。
 */
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
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentId 用于定位agent的标识。
     * @param version 用于完成本次业务处理的 version 参数。
     * @param input 用于完成本次业务处理的 input 参数。
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
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     */
    public synchronized void start() {
        this.status = ExecutionStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    /**
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param output 用于完成本次业务处理的 output 参数。
     */
    public synchronized void complete(Map<String, Object> output) {
        this.status = ExecutionStatus.COMPLETED;
        this.output = output;
        this.endTime = LocalDateTime.now();
        this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        notifyAll();
    }

    /**
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param errorMessage 用于完成本次业务处理的 errorMessage 参数。
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
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     */
    public synchronized void pause() {
        this.status = ExecutionStatus.PAUSED;
    }

    /**
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
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
     * 创建或保存 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param nodeExecution 用于完成本次业务处理的 nodeExecution 参数。
     */
    public void addNodeExecution(NodeExecution nodeExecution) {
        this.nodeExecutions.add(nodeExecution);
    }

    // Getters
    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public String getVersion() {
        return version;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public ExecutionStatus getStatus() {
        return status;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public Map<String, Object> getInput() {
        return input;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public Map<String, Object> getOutput() {
        return output;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public Long getDurationMs() {
        return durationMs;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<NodeExecution> getNodeExecutions() {
        return nodeExecutions;
    }

    /**
     * 查询 Execution 相关业务数据，并返回处理结果。
     *
     * @return 返回 Execution 相关操作生成的结果数据。
     */
    public String getLastCheckpointId() {
        return lastCheckpointId;
    }

    /**
     * 更新或设置 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param userId 当前操作涉及的用户标识。
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 更新或设置 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param sessionId 用于定位session的标识。
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 更新或设置 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param lastCheckpointId 用于定位last Checkpoint的标识。
     */
    public void setLastCheckpointId(String lastCheckpointId) {
        this.lastCheckpointId = lastCheckpointId;
    }
}
