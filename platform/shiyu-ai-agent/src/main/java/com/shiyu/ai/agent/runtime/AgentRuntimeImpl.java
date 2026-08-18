package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.domain.enums.AgentExecutionStatus;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.cache.AgentLoader;
import com.shiyu.ai.agent.checkpoint.Checkpoint;
import com.shiyu.ai.agent.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.agent.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.checkpoint.DbCheckpointStore;
import com.shiyu.ai.agent.event.AgentExecutionCompletedEvent;
import com.shiyu.ai.agent.event.AgentExecutionFailedEvent;
import com.shiyu.ai.agent.event.AgentExecutionStartedEvent;
import com.shiyu.ai.agent.event.EventPublisher;
import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.agent.execution.ExecutionStatus;
import com.shiyu.ai.agent.lifecycle.AgentStateMachine;
import com.shiyu.ai.agent.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.port.repository.AgentExecutionRepository;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.runtime.*;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.state.AgentState;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Agent 运行时实现
 * 管理执行生命周期、检查点、事件发布
 */
@Slf4j
public class AgentRuntimeImpl implements AgentRuntime {

    private final AgentCacheManager cacheManager;
    private final AgentLoader agentLoader;
    private final AgentExecutionRepository executionRepository;
    private final AgentExecutor agentExecutor;
    private final CheckpointManager checkpointManager;
    private final EventPublisher eventPublisher;
    private final AiRuntimeService runtime;

    private final ConcurrentHashMap<String, Execution> activeExecutions = new ConcurrentHashMap<>();

    public AgentRuntimeImpl(AgentCacheManager cacheManager,
                            AgentLoader agentLoader,
                            AgentExecutionRepository executionRepository,
                            AgentCheckpointRepository checkpointRepository,
                            EventPublisher eventPublisher) {
        this(cacheManager, agentLoader, executionRepository, checkpointRepository, eventPublisher, null);
    }

    public AgentRuntimeImpl(AgentCacheManager cacheManager,
                            AgentLoader agentLoader,
                            AgentExecutionRepository executionRepository,
                            AgentCheckpointRepository checkpointRepository,
                            EventPublisher eventPublisher,
                            AiRuntimeService runtime) {
        this.cacheManager = cacheManager;
        this.agentLoader = agentLoader;
        this.executionRepository = executionRepository;
        this.eventPublisher = eventPublisher;
        this.runtime = runtime;

        DbCheckpointStore checkpointStore = new DbCheckpointStore(checkpointRepository);
        this.checkpointManager = new CheckpointManager(checkpointStore);
        this.agentExecutor = new AgentExecutor(checkpointManager);
    }

    @Override
    public Execution execute(String agentId, Map<String, Object> input) {
        return execute(agentId, null, input);
    }

    @Override
    public Execution execute(String agentId, String version, Map<String, Object> input) {
        Execution execution = createExecution(agentId, version, input);
        AgentDefinition definition = getAgentDefinition(agentId);
        AgentVersion agentVersion = definition.getVersion(version);
        beginExecution(execution);
        AiRun runtimeRun = startRuntime(execution, input);

        eventPublisher.publish(new AgentExecutionStartedEvent(
                execution.getExecutionId(), agentId, input));

        Map<String, Object> graphInput = withRuntime(withVersion(input, execution.getVersion()), runtimeRun);
        Execution result;
        try {
            result = agentExecutor.executeAgent(definition, agentVersion, graphInput, execution);
            saveExecution(result);
        } catch (Exception e) {
            execution.fail(e.getMessage());
            saveExecution(execution);
            finishRuntime(runtimeRun, execution);
            eventPublisher.publish(new AgentExecutionFailedEvent(
                    execution.getExecutionId(), agentId, e.getMessage()));
            cleanupExecution(execution);
            throw e;
        }
        finishRuntime(runtimeRun, result);

        if (result.getStatus() == ExecutionStatus.COMPLETED) {
            eventPublisher.publish(new AgentExecutionCompletedEvent(
                    result.getExecutionId(), agentId, result.getOutput(), result.getDurationMs()));
        } else if (result.getStatus() == ExecutionStatus.FAILED) {
            eventPublisher.publish(new AgentExecutionFailedEvent(
                    result.getExecutionId(), agentId, result.getErrorMessage()));
        }

        cleanupExecution(result);
        return result;
    }

    @Override
    public Flux<Map<String, Object>> executeStream(String agentId, Map<String, Object> input) {
        return executeStream(agentId, null, input);
    }

    @Override
    public Flux<Map<String, Object>> executeStream(String agentId, String version, Map<String, Object> input) {
        Execution execution = createExecution(agentId, version, input);
        AgentDefinition definition = getAgentDefinition(agentId);
        AgentVersion agentVersion = definition.getVersion(version);
        beginExecution(execution);
        AiRun runtimeRun = startRuntime(execution, input);

        eventPublisher.publish(new AgentExecutionStartedEvent(
                execution.getExecutionId(), agentId, input));

        return Flux.<Map<String, Object>>create(sink -> {
            try {
                // 逐节点流式执行 — 使用 graph.stream(input) 而非一次性 execute
                // stream() 返回 AsyncGenerator，forEach 遍历每个节点执行后的状态
                final Map<String, Object>[] finalState = new Map[]{null};

                Map<String, Object> graphInput = withRuntime(withVersion(input, execution.getVersion()), runtimeRun);
                agentVersion.getGraph().stream(graphInput)
                        .forEach(nodeOutput -> {
                            if (!execution.awaitResumeOrCancellation()) {
                                throw new ExecutionCancelledException();
                            }
                            Map<String, Object> stateData = nodeOutput.state().data();
                            finalState[0] = stateData;
                            appendRuntime(runtimeRun, AiRunEventType.MODEL_DELTA, JSONUtils.toJsonString(Map.of("node", nodeOutput.node())));
                            // 每个节点执行后的完整 State 作为一个 chunk 发出
                            sink.next(Map.of(
                                    "executionId", execution.getExecutionId(),
                                    "node", nodeOutput.node(),
                                    "state", stateData
                            ));
                        });

                if (!execution.awaitResumeOrCancellation()) {
                    throw new ExecutionCancelledException();
                }

                // stream() 遍历完成后，finalState[0] 即为最后一个节点的输出
                Map<String, Object> output = finalState[0] == null ? Map.of() : finalState[0];
                execution.complete(output);
                saveExecution(execution);
                finishRuntime(runtimeRun, execution);

                eventPublisher.publish(new AgentExecutionCompletedEvent(
                        execution.getExecutionId(), agentId, output,
                        execution.getDurationMs()));

                // 发出完成标记
                sink.next(Map.of(
                        "status", "COMPLETED",
                        "executionId", execution.getExecutionId()
                ));

                sink.complete();

            } catch (ExecutionCancelledException e) {
                execution.cancel();
                saveExecution(execution);
                finishRuntime(runtimeRun, execution);
                sink.next(Map.of(
                        "status", "CANCELLED",
                        "executionId", execution.getExecutionId()
                ));
                sink.complete();
            } catch (Exception e) {
                log.error("Agent 流式执行失败: agentId={}, executionId={}",
                        agentId, execution.getExecutionId(), e);
                execution.fail(e.getMessage());
                saveExecution(execution);
                finishRuntime(runtimeRun, execution);

                eventPublisher.publish(new AgentExecutionFailedEvent(
                        execution.getExecutionId(), agentId, e.getMessage()));

                sink.error(e);
            } finally {
                cleanupExecution(execution);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public void pause(String executionId) {
        Execution execution = activeExecutions.get(executionId);
        if (execution == null) {
            throw new IllegalStateException("执行实例不存在或已结束: " + executionId);
        }
        AgentStateMachine.transition(execution.getStatus(), ExecutionStatus.PAUSED);
        execution.pause();
        saveExecution(execution);
        log.info("Agent 执行已暂停: executionId={}", executionId);
    }

    @Override
    public Execution resume(String executionId) {
        Execution active = activeExecutions.get(executionId);
        if (active != null) {
            AgentStateMachine.transition(active.getStatus(), ExecutionStatus.RUNNING);
            active.resume();
            saveExecution(active);
            return active;
        }

        AgentExecutionBO execBO = executionRepository.selectByExecutionId(executionId);
        if (execBO == null) {
            throw new IllegalStateException("执行实例不存在: " + executionId);
        }
        Execution execution = rebuildExecution(execBO);
        return resumeFromCheckpoint(execution);
    }

    private Execution resumeFromCheckpoint(Execution execution) {
        AgentStateMachine.transition(execution.getStatus(), ExecutionStatus.RUNNING);
        execution.resume();
        activeExecutions.put(execution.getExecutionId(), execution);
        saveExecution(execution);

        String agentId = execution.getAgentId();
        AgentDefinition definition = getAgentDefinition(agentId);
        AgentVersion agentVersion = definition.getVersion(execution.getVersion());

        Checkpoint checkpoint = null;
        if (execution.getLastCheckpointId() != null) {
            checkpoint = checkpointManager.loadCheckpoint(execution.getLastCheckpointId());
        }

        Execution result;
        if (checkpoint != null) {
            result = agentExecutor.resumeFromCheckpoint(execution, definition, agentVersion, checkpoint);
        } else {
            result = agentExecutor.executeAgent(definition, agentVersion, execution.getInput(), execution);
        }

        saveExecution(result);
        cleanupExecution(result);
        return result;
    }

    @Override
    public void cancel(String executionId) {
        Execution execution = activeExecutions.get(executionId);
        if (execution == null) {
            AgentExecutionBO execBO = executionRepository.selectByExecutionId(executionId);
            if (execBO == null) {
                throw new IllegalStateException("执行实例不存在: " + executionId);
            }
            execution = rebuildExecution(execBO);
        }
        AgentStateMachine.transition(execution.getStatus(), ExecutionStatus.CANCELLED);
        execution.cancel();
        saveExecution(execution);
        log.info("Agent 执行已取消: executionId={}", executionId);
    }

    @Override
    public ExecutionStatus getStatus(String executionId) {
        Execution active = activeExecutions.get(executionId);
        if (active != null) return active.getStatus();

        AgentExecutionBO execBO = executionRepository.selectByExecutionId(executionId);
        if (execBO == null) return null;
        return resolveStatus(execBO);
    }

    @Override
    public Execution getExecution(String executionId) {
        Execution active = activeExecutions.get(executionId);
        if (active != null) return active;

        AgentExecutionBO execBO = executionRepository.selectByExecutionId(executionId);
        if (execBO == null) return null;
        return rebuildExecution(execBO);
    }

    @Override
    public List<Execution> getHistory(String agentId, int limit) {
        List<AgentExecutionBO> boList = executionRepository.selectByAgentId(agentId, limit);
        return boList.stream().map(this::rebuildExecution).collect(Collectors.toList());
    }

    @Override
    public List<Execution> getUserHistory(Long userId, int limit) {
        List<AgentExecutionBO> boList = executionRepository.selectBySessionId(String.valueOf(userId));
        return boList.stream().limit(limit).map(this::rebuildExecution).collect(Collectors.toList());
    }

    // ==================== 内部方法 ====================

    private Execution createExecution(String agentId, String version, Map<String, Object> input) {
        AgentDefinition definition = getAgentDefinition(agentId);
        String resolvedVersion = version != null ? version : definition.getCurrentVersion();
        Execution execution = new Execution(agentId, resolvedVersion, input);
        if (input != null) {
            Object userId = input.get("userId");
            if (userId instanceof Number number) {
                execution.setUserId(number.longValue());
            } else if (userId instanceof String value && !value.isBlank()) {
                try {
                    execution.setUserId(Long.parseLong(value));
                } catch (NumberFormatException ignored) {
                    log.warn("Ignoring non-numeric execution userId: {}", value);
                }
            }
            Object sessionId = input.get("sessionId");
            if (sessionId != null) {
                execution.setSessionId(String.valueOf(sessionId));
            }
        }
        return execution;
    }

    private static Map<String, Object> withVersion(Map<String, Object> input, String version) {
        Map<String, Object> graphInput = input == null ? new java.util.HashMap<>() : new java.util.HashMap<>(input);
        graphInput.put("version", version);
        return graphInput;
    }

    private static Map<String, Object> withRuntime(Map<String, Object> input, AiRun run) {
        if (run != null) input.put("__aiRunId", run.id());
        return input;
    }

    private void beginExecution(Execution execution) {
        execution.start();
        activeExecutions.put(execution.getExecutionId(), execution);
        saveExecution(execution);
    }

    private AgentDefinition getAgentDefinition(String agentId) {
        AgentDefinition definition = cacheManager.get(null, agentId);
        if (definition == null) {
            definition = cacheManager.getOrLoad(null, agentId, agentLoader);
        }
        if (definition == null) {
            throw new IllegalStateException("Agent 定义不存在: " + agentId);
        }
        return definition;
    }

    private void saveExecution(Execution execution) {
        try {
            AgentExecutionBO bo = new AgentExecutionBO();
            bo.setExecutionId(execution.getExecutionId());
            bo.setAgentId(execution.getAgentId());
            bo.setVersion(execution.getVersion());
            bo.setUserId(execution.getUserId());
            bo.setSessionId(execution.getSessionId());
            bo.setInputData(JSONUtils.toJsonString(execution.getInput()));
            bo.setOutputData(JSONUtils.toJsonString(execution.getOutput()));
            bo.setStatus(toStoredStatus(execution.getStatus()));
            bo.setErrorMessage(execution.getErrorMessage());
            bo.setStartTime(execution.getStartTime());
            bo.setEndTime(execution.getEndTime());
            bo.setDurationMs(execution.getDurationMs());

            AgentExecutionBO existing = executionRepository.selectByExecutionId(execution.getExecutionId());
            if (existing != null) {
                bo.setId(existing.getId());
                executionRepository.update(bo);
            } else {
                executionRepository.insert(bo);
            }
        } catch (Exception e) {
            log.error("保存执行记录失败: executionId={}", execution.getExecutionId(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Execution rebuildExecution(AgentExecutionBO bo) {
        return Execution.restore(
                bo.getExecutionId(),
                bo.getAgentId(),
                bo.getVersion(),
                resolveStatus(bo),
                parseData(bo.getInputData()),
                parseData(bo.getOutputData()),
                bo.getErrorMessage(),
                bo.getUserId(),
                bo.getSessionId(),
                bo.getStartTime(),
                bo.getEndTime(),
                bo.getDurationMs());
    }

    static Integer toStoredStatus(ExecutionStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case PENDING, RUNNING -> AgentExecutionStatus.RUNNING.getCode();
            case PAUSED -> AgentExecutionStatus.PAUSED.getCode();
            case COMPLETED -> AgentExecutionStatus.SUCCESS.getCode();
            case FAILED -> AgentExecutionStatus.FAILED.getCode();
            case CANCELLED -> AgentExecutionStatus.CANCELLED.getCode();
        };
    }

    static ExecutionStatus fromStoredStatus(Integer storedStatus) {
        AgentExecutionStatus status = AgentExecutionStatus.fromCode(storedStatus);
        if (status == null) {
            return null;
        }
        return switch (status) {
            case RUNNING -> ExecutionStatus.RUNNING;
            case SUCCESS -> ExecutionStatus.COMPLETED;
            case FAILED -> ExecutionStatus.FAILED;
            case PAUSED -> ExecutionStatus.PAUSED;
            case CANCELLED -> ExecutionStatus.CANCELLED;
        };
    }

    private static ExecutionStatus resolveStatus(AgentExecutionBO bo) {
        ExecutionStatus status = fromStoredStatus(bo.getStatus());
        if (status == ExecutionStatus.RUNNING && bo.getEndTime() != null) {
            return bo.getErrorMessage() == null || bo.getErrorMessage().isBlank()
                    ? ExecutionStatus.COMPLETED
                    : ExecutionStatus.FAILED;
        }
        return status;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseData(String data) {
        return data == null ? null : JSONUtils.parseObject(data, Map.class);
    }

    private void cleanupExecution(Execution execution) {
        if (execution.getStatus().isTerminal()) {
            activeExecutions.remove(execution.getExecutionId());
            checkpointManager.cleanCheckpoints(execution.getExecutionId());
        } else {
            activeExecutions.put(execution.getExecutionId(), execution);
        }
    }

    private AiRun startRuntime(Execution execution, Map<String, Object> input) {
        if (runtime == null) return null;
        long tenant = number(input == null ? null : input.get("tenantId"));
        long owner = execution.getUserId() == null ? number(input == null ? null : input.get("userId")) : execution.getUserId();
        if (tenant <= 0 || owner <= 0) return null;
        try {
            String appId = input == null ? null : string(input.get("__appId"));
            String appVersionId = input == null ? null : string(input.get("__appVersionId"));
            AiRun run = runtime.startRun(new AiRunContext(tenant, owner, appId, appVersionId, null, null,
                    execution.getExecutionId(), null, Map.of("agentId", execution.getAgentId())),
                    AiRunSource.AGENT, execution.getAgentId(), null, JSONUtils.toJsonString(input));
            runtime.append(run, AiRunEventType.MODEL_STARTED, "{}", true);
            return run;
        } catch (RuntimeException failure) {
            // An AI App execution is only valid when it has a durable Runtime
            // run.  Do not silently execute an untraced app when H2/event
            // admission fails; that would break the single-run audit model.
            if (input != null && input.get("__appId") != null) {
                throw new IllegalStateException("runtime admission failed", failure);
            }
            return null;
        }
    }

    private void appendRuntime(AiRun run, AiRunEventType type, String payload) {
        if (run == null || runtime == null) return;
        // Once an execution has been admitted as an AI App run, event writes
        // are part of its durable contract. Let a failed append fail the
        // execution rather than returning an untraceable successful result.
        runtime.append(run, type, payload, true);
    }

    private void finishRuntime(AiRun run, Execution execution) {
        if (run == null || runtime == null) return;
        try {
            AiRunStatus status = execution.getStatus() == ExecutionStatus.COMPLETED ? AiRunStatus.COMPLETED
                    : execution.getStatus() == ExecutionStatus.CANCELLED ? AiRunStatus.CANCELLED : AiRunStatus.FAILED;
            runtime.finish(run.id(), run.tenantId(), run.ownerUserId(), status, execution.getErrorMessage());
        } catch (RuntimeException ignored) { }
    }

    private long number(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value != null) try { return Long.parseLong(String.valueOf(value)); } catch (NumberFormatException ignored) { }
        return 0;
    }

    private String string(Object value) { return value == null ? null : String.valueOf(value); }

    private static final class ExecutionCancelledException extends RuntimeException {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
    }
}
