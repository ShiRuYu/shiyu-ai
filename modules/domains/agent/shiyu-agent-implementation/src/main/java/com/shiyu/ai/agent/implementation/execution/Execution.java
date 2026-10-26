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
    private boolean locallyPaused;
    private volatile boolean superseded;
    private volatile boolean yieldAfterCheckpoint;
    private volatile Runnable stateRefresh = () -> {};
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
        requireNonTerminal();
        this.status = ExecutionStatus.RUNNING;
        if (this.startTime == null) this.startTime = LocalDateTime.now();
    }

    /**
     * 将运行中的执行确定为完成态。其他终态已经先到达时保留原结果。
     *
     * @param output 成功执行生成的最终图状态
     * @return 本次调用成功确定完成终态时返回 true
     */
    public boolean complete(Map<String, Object> output) {
        if (isSuperseded()) stateRefresh.run();
        synchronized (this) {
            if (superseded || status != ExecutionStatus.RUNNING) return false;
        }
        synchronized (this) {
            if (superseded || status != ExecutionStatus.RUNNING) return false;
            this.status = ExecutionStatus.COMPLETED;
            this.output = output;
            this.endTime = LocalDateTime.now();
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            notifyAll();
            return true;
        }
    }

    /**
     * 将执行确定为失败态。其他终态已经先到达时保留原结果。
     *
     * @param errorMessage 供执行详情展示的失败原因
     * @return 本次调用成功确定失败终态时返回 true
     */
    public boolean fail(String errorMessage) {
        if (isSuperseded()) stateRefresh.run();
        synchronized (this) {
            if (superseded || status != ExecutionStatus.RUNNING) return false;
            this.status = ExecutionStatus.FAILED;
            this.errorMessage = errorMessage;
            this.endTime = LocalDateTime.now();
            if (startTime != null) {
                this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            }
            notifyAll();
            return true;
        }
    }

    /**
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     */
    public synchronized void pause() {
        if (superseded || status != ExecutionStatus.RUNNING) {
            throw new IllegalStateException("只有运行中的执行可以暂停");
        }
        this.status = ExecutionStatus.PAUSED;
        this.locallyPaused = true;
    }

    /**
     * 执行 Execution 相关业务操作，并维护必要的状态和协作关系。
     */
    public synchronized void resume() {
        if (status != ExecutionStatus.PAUSED) {
            throw new IllegalStateException("只有暂停中的执行可以恢复");
        }
        this.status = ExecutionStatus.RUNNING;
        this.locallyPaused = false;
        notifyAll();
    }

    /**
     * 确定取消终态；已经结束的执行保持原终态。
     *
     * @return 本次调用成功确定取消终态时返回 true
     */
    public synchronized boolean cancel() {
        if (superseded || isTerminal()) return false;
        this.status = ExecutionStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        if (startTime != null) {
            this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
        notifyAll();
        return true;
    }

    /** 将权威执行快照复制到本地对象，协调多实例状态竞争。 */
    public synchronized void reconcile(Execution authoritative) {
        if (authoritative == null) throw new IllegalArgumentException("execution is required");
        boolean wasLocallyPaused = this.locallyPaused;
        if (isTerminal() && !authoritative.status.isTerminal()) return;
        boolean pauseWonElsewhere = !isTerminal()
                && (status == ExecutionStatus.PAUSED
                        && authoritative.status == ExecutionStatus.RUNNING
                        || status == ExecutionStatus.RUNNING
                                && authoritative.status == ExecutionStatus.PAUSED
                                && !wasLocallyPaused);
        if (!isTerminal()
                && (pauseWonElsewhere
                        || authoritative.status == ExecutionStatus.RUNNING
                                && wasLocallyPaused)) {
            superseded = true;
            yieldAfterCheckpoint = status == ExecutionStatus.PAUSED
                    && authoritative.status == ExecutionStatus.RUNNING;
            status = authoritative.status;
            locallyPaused = false;
            output = authoritative.output;
            errorMessage = authoritative.errorMessage;
            endTime = authoritative.endTime;
            durationMs = authoritative.durationMs;
            lastCheckpointId = authoritative.lastCheckpointId;
            notifyAll();
            return;
        }
        if (status == ExecutionStatus.RUNNING
                && authoritative.status == ExecutionStatus.RUNNING
                && (endTime != null || output != null || errorMessage != null)) {
            return;
        }
        this.status = authoritative.status;
        this.locallyPaused = authoritative.status == ExecutionStatus.PAUSED
                && wasLocallyPaused;
        this.output = authoritative.output;
        this.errorMessage = authoritative.errorMessage;
        this.endTime = authoritative.endTime;
        this.durationMs = authoritative.durationMs;
        this.lastCheckpointId = authoritative.lastCheckpointId;
        notifyAll();
    }

    public synchronized boolean isSuperseded() {
        return superseded;
    }

    public boolean shouldYieldAfterCheckpoint() {
        return yieldAfterCheckpoint;
    }

    public void claimForLocalResume() {
        superseded = false;
        yieldAfterCheckpoint = false;
    }

    public void setStateRefresh(Runnable stateRefresh) {
        this.stateRefresh = stateRefresh == null ? () -> {} : stateRefresh;
    }

    public void refreshState() {
        stateRefresh.run();
    }

    public void checkCurrentOwnership() {
        if (isSuperseded()) stateRefresh.run();
        if (isSuperseded()) throw new IllegalStateException("执行已由其他 runtime 接管");
    }

    private boolean isTerminal() {
        return status == ExecutionStatus.COMPLETED
                || status == ExecutionStatus.FAILED
                || status == ExecutionStatus.CANCELLED;
    }

    private void requireNonTerminal() {
        if (isTerminal()) {
            throw new IllegalStateException("终态执行不能重新启动或暂停");
        }
    }

    /**
     * 处理awaitresumeorcancellation。
     *
     * @return 判断结果。
     */
    public synchronized boolean awaitResumeOrCancellation() {
        while (status == ExecutionStatus.PAUSED && locallyPaused) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                cancel();
                return false;
            }
        }
        return status == ExecutionStatus.RUNNING && !superseded;
    }

    /** 在暂停期间定时返回，使调用方可以轮询其他 runtime 的状态变更。 */
    public boolean awaitResumeOrCancellation(long waitMillis) {
        if (waitMillis <= 0) throw new IllegalArgumentException("wait interval must be positive");
        synchronized (this) {
            if (status != ExecutionStatus.PAUSED) {
                return status == ExecutionStatus.RUNNING && !superseded;
            }
            if (locallyPaused) {
                try {
                    wait(waitMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    cancel();
                    return false;
                }
            }
            if (superseded) return false;
        }
        stateRefresh.run();
        synchronized (this) {
            return status == ExecutionStatus.RUNNING && !superseded;
        }
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
