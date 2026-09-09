package com.shiyu.ai.agent.implementation.runtime;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.implementation.cache.AgentCacheManager;
import com.shiyu.ai.agent.implementation.cache.AgentLoader;
import com.shiyu.ai.agent.implementation.checkpoint.Checkpoint;
import com.shiyu.ai.agent.implementation.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.implementation.event.AgentExecutionCompletedEvent;
import com.shiyu.ai.agent.implementation.event.AgentExecutionFailedEvent;
import com.shiyu.ai.agent.implementation.event.AgentExecutionStartedEvent;
import com.shiyu.ai.agent.implementation.event.EventPublisher;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.lifecycle.AgentStateMachine;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import org.bsc.langgraph4j.state.AgentState;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/** Coordinates execution, streaming, and control operations for the runtime facade. */
@Slf4j
final class AgentExecutionLifecycle {
    private final AgentCacheManager cacheManager;
    private final AgentLoader agentLoader;
    private final AgentExecutionRepository executionRepository;
    private final AgentExecutor agentExecutor;
    private final CheckpointManager checkpointManager;
    private final EventPublisher eventPublisher;
    private final AgentRuntimeStateStore stateStore;
    private final AgentRuntimeEventBridge eventBridge;

    AgentExecutionLifecycle(AgentCacheManager cacheManager, AgentLoader agentLoader,
                            AgentExecutionRepository executionRepository, AgentExecutor agentExecutor,
                            CheckpointManager checkpointManager, EventPublisher eventPublisher,
                            AgentRuntimeStateStore stateStore, AgentRuntimeEventBridge eventBridge) {
        this.cacheManager = cacheManager;
        this.agentLoader = agentLoader;
        this.executionRepository = executionRepository;
        this.agentExecutor = agentExecutor;
        this.checkpointManager = checkpointManager;
        this.eventPublisher = eventPublisher;
        this.stateStore = stateStore;
        this.eventBridge = eventBridge;
    }

    Execution execute(ActorContext actor, String agentId, String version, Map<String, Object> input) {
        Map<String, Object> actorInput = withActor(actor, input);
        Execution execution = createExecution(actor, agentId, version, actorInput);
        AgentDefinition definition = getAgentDefinition(actor, agentId);
        AgentVersion agentVersion = definition.getVersion(version);
        beginExecution(actor, execution);
        AiRun runtimeRun = eventBridge.start(execution, actorInput);
        eventPublisher.publish(new AgentExecutionStartedEvent(execution.getExecutionId(), agentId, actorInput));

        Map<String, Object> graphInput = withRuntime(withVersion(actorInput, execution.getVersion()), runtimeRun);
        Execution result;
        try {
            result = agentExecutor.executeAgent(actor.tenantId(), definition, agentVersion, graphInput, execution);
            stateStore.save(actor, result);
        } catch (Exception e) {
            execution.fail(e.getMessage());
            stateStore.save(actor, execution);
            eventBridge.finish(runtimeRun, execution);
            eventPublisher.publish(new AgentExecutionFailedEvent(
                    execution.getExecutionId(), agentId, AgentRuntimeImpl.publicFailureCode()));
            stateStore.cleanup(actor, execution);
            throw e;
        }
        eventBridge.finish(runtimeRun, result);
        if (result.getStatus() == ExecutionStatus.COMPLETED) {
            eventPublisher.publish(new AgentExecutionCompletedEvent(
                    result.getExecutionId(), agentId, result.getOutput(), result.getDurationMs()));
        } else if (result.getStatus() == ExecutionStatus.FAILED) {
            eventPublisher.publish(new AgentExecutionFailedEvent(
                    result.getExecutionId(), agentId, AgentRuntimeImpl.publicFailureCode()));
        }
        stateStore.cleanup(actor, result);
        return result;
    }

    Flux<Map<String, Object>> executeStream(ActorContext actor, String agentId, String version,
                                             Map<String, Object> input) {
        Map<String, Object> actorInput = withActor(actor, input);
        Execution execution = createExecution(actor, agentId, version, actorInput);
        AgentDefinition definition = getAgentDefinition(actor, agentId);
        AgentVersion agentVersion = definition.getVersion(version);
        beginExecution(actor, execution);
        AiRun runtimeRun = eventBridge.start(execution, actorInput);
        eventPublisher.publish(new AgentExecutionStartedEvent(execution.getExecutionId(), agentId, actorInput));

        return Flux.<Map<String, Object>>create(sink -> {
            AtomicBoolean clientCancelled = new AtomicBoolean();
            AtomicReference<Thread> workerThread = new AtomicReference<>(Thread.currentThread());
            sink.onCancel(() -> {
                clientCancelled.set(true);
                execution.cancel();
                Thread worker = workerThread.get();
                if (worker != null) worker.interrupt();
            });
            try {
                AtomicReference<Map<String, Object>> finalState = new AtomicReference<>();
                Map<String, Object> graphInput = withRuntime(withVersion(actorInput, execution.getVersion()), runtimeRun);
                agentVersion.getGraph().stream(graphInput).forEach(nodeOutput -> {
                    if (clientCancelled.get() || sink.isCancelled() || !execution.awaitResumeOrCancellation()) {
                        throw new ExecutionCancelledException();
                    }
                    Map<String, Object> stateData = nodeOutput.state().data();
                    finalState.set(stateData);
                    eventBridge.append(runtimeRun, AiRunEventType.MODEL_DELTA,
                            JSONUtils.toJsonString(Map.of("node", nodeOutput.node())));
                    if (clientCancelled.get() || sink.isCancelled()) throw new ExecutionCancelledException();
                    sink.next(Map.of("executionId", execution.getExecutionId(), "node", nodeOutput.node(), "state", stateData));
                });
                if (clientCancelled.get() || sink.isCancelled() || !execution.awaitResumeOrCancellation()) {
                    throw new ExecutionCancelledException();
                }
                Map<String, Object> output = finalState.get() == null ? Map.of() : finalState.get();
                execution.complete(output);
                stateStore.save(actor, execution);
                eventBridge.finish(runtimeRun, execution);
                eventPublisher.publish(new AgentExecutionCompletedEvent(
                        execution.getExecutionId(), agentId, output, execution.getDurationMs()));
                sink.next(Map.of("status", "COMPLETED", "executionId", execution.getExecutionId()));
                sink.complete();
            } catch (ExecutionCancelledException e) {
                execution.cancel();
                stateStore.save(actor, execution);
                eventBridge.finish(runtimeRun, execution);
                if (!clientCancelled.get() && !sink.isCancelled()) {
                    sink.next(Map.of("status", "CANCELLED", "executionId", execution.getExecutionId()));
                }
                sink.complete();
            } catch (Exception e) {
                if (clientCancelled.get() || sink.isCancelled()) {
                    execution.cancel();
                    stateStore.save(actor, execution);
                    eventBridge.finish(runtimeRun, execution);
                    return;
                }
                log.error("Agent 流式执行失败: agentIdPresent={}, executionIdPresent={}, errorType={}, errorMessageLength={}",
                        agentId != null, execution.getExecutionId() != null, e.getClass().getSimpleName(),
                        e.getMessage() == null ? 0 : e.getMessage().length());
                execution.fail(e.getMessage());
                stateStore.save(actor, execution);
                eventBridge.finish(runtimeRun, execution);
                eventPublisher.publish(new AgentExecutionFailedEvent(
                        execution.getExecutionId(), agentId, AgentRuntimeImpl.publicFailureCode()));
                sink.error(e);
            } finally {
                workerThread.set(null);
                stateStore.cleanup(actor, execution);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    void pause(ActorContext actor, String executionId) {
        Execution execution = stateStore.active(executionId);
        if (execution == null) throw new IllegalStateException("执行实例不存在或已结束: " + executionId);
        stateStore.ensureAccessible(actor, execution);
        AgentStateMachine.transition(execution.getStatus(), ExecutionStatus.PAUSED);
        execution.pause();
        stateStore.save(actor, execution);
    }

    Execution resume(ActorContext actor, String executionId) {
        Execution active = stateStore.active(executionId);
        if (active != null) {
            stateStore.ensureAccessible(actor, active);
            AgentStateMachine.transition(active.getStatus(), ExecutionStatus.RUNNING);
            active.resume();
            stateStore.save(actor, active);
            return active;
        }
        AgentExecutionBO execBO = stateStore.persisted(actor, executionId);
        if (execBO == null) throw new IllegalStateException("执行实例不存在: " + executionId);
        Execution execution = stateStore.rebuild(execBO);
        stateStore.ensureAccessible(actor, execution);
        return resumeFromCheckpoint(actor, execution);
    }

    private Execution resumeFromCheckpoint(ActorContext actor, Execution execution) {
        AgentStateMachine.transition(execution.getStatus(), ExecutionStatus.RUNNING);
        execution.resume();
        stateStore.put(execution);
        stateStore.save(actor, execution);
        AgentDefinition definition = getAgentDefinition(actor, execution.getAgentId());
        AgentVersion agentVersion = definition.getVersion(execution.getVersion());
        Checkpoint checkpoint = execution.getLastCheckpointId() == null ? null
                : checkpointManager.loadCheckpoint(actor.tenantId(), execution.getLastCheckpointId());
        Execution result = checkpoint != null
                ? agentExecutor.resumeFromCheckpoint(actor.tenantId(), execution, definition, agentVersion, checkpoint)
                : agentExecutor.executeAgent(actor.tenantId(), definition, agentVersion, execution.getInput(), execution);
        stateStore.save(actor, result);
        stateStore.cleanup(actor, result);
        return result;
    }

    void cancel(ActorContext actor, String executionId) {
        Execution execution = stateStore.active(executionId);
        if (execution == null) {
            AgentExecutionBO execBO = stateStore.persisted(actor, executionId);
            if (execBO == null) throw new IllegalStateException("执行实例不存在: " + executionId);
            execution = stateStore.rebuild(execBO);
        }
        stateStore.ensureAccessible(actor, execution);
        AgentStateMachine.transition(execution.getStatus(), ExecutionStatus.CANCELLED);
        execution.cancel();
        stateStore.save(actor, execution);
    }

    ExecutionStatus getStatus(ActorContext actor, String executionId) {
        Execution active = stateStore.active(executionId);
        if (active != null) {
            stateStore.ensureAccessible(actor, active);
            return active.getStatus();
        }
        AgentExecutionBO execBO = stateStore.persisted(actor, executionId);
        if (execBO == null) return null;
        stateStore.ensureAccessible(actor, stateStore.rebuild(execBO));
        return AgentRuntimeStateStore.resolveStatus(execBO);
    }

    Execution getExecution(ActorContext actor, String executionId) {
        Execution active = stateStore.active(executionId);
        if (active != null) {
            stateStore.ensureAccessible(actor, active);
            return active;
        }
        AgentExecutionBO execBO = stateStore.persisted(actor, executionId);
        if (execBO == null) return null;
        Execution execution = stateStore.rebuild(execBO);
        stateStore.ensureAccessible(actor, execution);
        return execution;
    }

    List<Execution> getHistory(ActorContext actor, String agentId, int limit) {
        return executionRepository.selectByAgentId(actor.tenantId(), agentId, Math.max(1, Math.min(limit, 100)))
                .stream().map(stateStore::rebuild).collect(Collectors.toList());
    }

    List<Execution> getUserHistory(ActorContext actor, Long userId, int limit) {
        if (userId == null || (!actor.platformAdmin() && actor.userId().value() != userId)) {
            throw new IllegalStateException("执行历史不存在");
        }
        return executionRepository.selectBySessionId(actor.tenantId(), String.valueOf(userId))
                .stream().limit(limit).map(stateStore::rebuild).collect(Collectors.toList());
    }

    Execution createExecution(ActorContext actor, String agentId, String version, Map<String, Object> input) {
        AgentDefinition definition = getAgentDefinition(actor, agentId);
        String resolvedVersion = version != null ? version : definition.getCurrentVersion();
        Execution execution = new Execution(agentId, resolvedVersion, input);
        if (input != null) {
            Object userId = input.get("userId");
            if (userId instanceof Number number) execution.setUserId(number.longValue());
            else if (userId instanceof String value && !value.isBlank()) {
                try { execution.setUserId(Long.parseLong(value)); }
                catch (NumberFormatException ignored) { log.warn("Ignoring non-numeric execution userId: {}", value); }
            }
            Object sessionId = input.get("sessionId");
            if (sessionId != null) execution.setSessionId(String.valueOf(sessionId));
        }
        return execution;
    }

    AgentDefinition getAgentDefinition(ActorContext actor, String agentId) {
        AgentDefinition definition = cacheManager.get(actor, agentId);
        if (definition == null) definition = cacheManager.getOrLoad(actor, agentId, agentLoader);
        if (definition == null) throw new IllegalStateException("Agent 定义不存在: " + agentId);
        return definition;
    }

    private void beginExecution(ActorContext actor, Execution execution) {
        execution.start();
        stateStore.save(actor, execution);
        stateStore.put(execution);
    }

    static Map<String, Object> withActor(ActorContext actor, Map<String, Object> input) {
        Map<String, Object> actorInput = input == null ? new HashMap<>() : new HashMap<>(input);
        actorInput.put("tenantId", actor.tenantId().value());
        actorInput.put("userId", actor.userId().value());
        return actorInput;
    }

    static Map<String, Object> withVersion(Map<String, Object> input, String version) {
        Map<String, Object> graphInput = input == null ? new HashMap<>() : new HashMap<>(input);
        graphInput.put("version", version);
        return graphInput;
    }

    static Map<String, Object> withRuntime(Map<String, Object> input, AiRun run) {
        if (run != null) input.put("__aiRunId", run.id());
        return input;
    }

    static final class ExecutionCancelledException extends RuntimeException {
        @java.io.Serial private static final long serialVersionUID = 1L;
    }
}
