package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntimeStateStore;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.contract.node.NodeActorContext;
import com.shiyu.ai.agent.implementation.cache.AgentCacheManager;
import com.shiyu.ai.agent.implementation.cache.AgentLoader;
import com.shiyu.ai.agent.implementation.checkpoint.Checkpoint;
import com.shiyu.ai.agent.implementation.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.implementation.checkpoint.AgentGraphCheckpointSaver;
import com.shiyu.ai.agent.implementation.graph.StateGraphBuilder;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionCompletedEvent;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionFailedEvent;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionStartedEvent;
import com.shiyu.ai.agent.implementation.event.publisher.EventPublisher;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.lifecycle.AgentStateMachine;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantScope;

import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.streaming.StreamingOutput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.function.Consumer;

/**
 * 实现 智能体 Execution Lifecycle 相关的业务处理、协作逻辑或基础设施能力。
 */
@Slf4j
final class AgentExecutionLifecycle {
    /**
     * cacheManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentCacheManager cacheManager;
    /**
     * agentLoader 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentLoader agentLoader;
    /**
     * 执行仓储，表示当前对象中的对应属性。
     */
    private final AgentExecutionRepository executionRepository;
    /**
     * agentExecutor 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentExecutor agentExecutor;
    /**
     * checkpointManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final CheckpointManager checkpointManager;
    /**
     * eventPublisher 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final EventPublisher eventPublisher;
    /**
     * 状态存储，表示当前对象中的对应属性。
     */
    private final AgentRuntimeStateStore stateStore;
    /**
     * eventBridge 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentRuntimeEventBridge eventBridge;
    private final ToolApprovalService approvals;

    AgentExecutionLifecycle(
            AgentCacheManager cacheManager,
            AgentLoader agentLoader,
            AgentExecutionRepository executionRepository,
            AgentExecutor agentExecutor,
            CheckpointManager checkpointManager,
            EventPublisher eventPublisher,
            AgentRuntimeStateStore stateStore,
            AgentRuntimeEventBridge eventBridge,
            ToolApprovalService approvals) {
        this.cacheManager = cacheManager;
        this.agentLoader = agentLoader;
        this.executionRepository = executionRepository;
        this.agentExecutor = agentExecutor;
        this.checkpointManager = checkpointManager;
        this.eventPublisher = eventPublisher;
        this.stateStore = stateStore;
        this.eventBridge = eventBridge;
        this.approvals = approvals;
    }

    Execution execute(
            ActorContext actor, String agentId, String version, Map<String, Object> input) {
        Map<String, Object> actorInput = withActor(actor, input);
        Execution execution = createExecution(actor, agentId, version, actorInput);
        AgentDefinition definition = getAgentDefinition(actor, agentId);
        AgentVersion agentVersion = requireVersion(definition, execution.getVersion());
        AiRun runtimeRun = startExecution(actor, agentId, actorInput, execution);

        Map<String, Object> graphInput =
                NodeActorContext.bind(actor,
                        withRuntime(withExecution(actorInput, execution), runtimeRun));
        Execution result;
        try {
            result =
                    agentExecutor.executeAgent(
                            actor.tenantId(), definition, agentVersion, graphInput, execution,
                            checkpointRecorder(actor, execution));
            stateStore.save(actor, result);
        } catch (Exception e) {
            if (execution.getStatus() == ExecutionStatus.COMPLETED) {
                // 终态写入的暂时性故障不能将已完成的执行改为 FAILED。
                // 发布完成事件前，先重试写入相同的终态。
                try {
                    stateStore.save(actor, execution);
                    result = execution;
                } catch (RuntimeException retryFailure) {
                    if (retryFailure != e) e.addSuppressed(retryFailure);
                    stateStore.cleanup(actor, execution);
                    throw e;
                }
            } else {
                boolean failed = execution.fail(e.getMessage());
                try {
                    stateStore.save(actor, execution);
                } catch (RuntimeException cleanupFailure) {
                    suppressDistinct(e, cleanupFailure);
                }
                try {
                    eventBridge.finish(runtimeRun, execution);
                } catch (RuntimeException cleanupFailure) {
                    suppressDistinct(e, cleanupFailure);
                }
                if (failed) {
                    try {
                        eventPublisher.publish(
                                new AgentExecutionFailedEvent(
                                        execution.getExecutionId(),
                                        agentId,
                                        AgentRuntimeImpl.publicFailureCode()));
                    } catch (RuntimeException cleanupFailure) {
                        suppressDistinct(e, cleanupFailure);
                    }
                }
                try {
                    stateStore.cleanup(actor, execution);
                } catch (RuntimeException cleanupFailure) {
                    suppressDistinct(e, cleanupFailure);
                }
                throw e;
            }
        }
        try {
            if (result.getStatus() != ExecutionStatus.PAUSED) {
                eventBridge.finish(runtimeRun, result);
            }
            if (result.getStatus() == ExecutionStatus.COMPLETED) {
                eventPublisher.publish(
                        new AgentExecutionCompletedEvent(
                                result.getExecutionId(),
                                agentId,
                                result.getOutput(),
                                result.getDurationMs()));
            } else if (result.getStatus() == ExecutionStatus.FAILED) {
                eventPublisher.publish(
                        new AgentExecutionFailedEvent(
                                result.getExecutionId(),
                                agentId,
                                AgentRuntimeImpl.publicFailureCode()));
            }
        } catch (RuntimeException failure) {
            try {
                stateStore.cleanup(actor, result);
            } catch (RuntimeException cleanupFailure) {
                suppressDistinct(failure, cleanupFailure);
            }
            throw failure;
        }
        stateStore.cleanup(actor, result);
        return result;
    }

    Flux<Map<String, Object>> executeStream(
            ActorContext actor, String agentId, String version, Map<String, Object> input) {
        Map<String, Object> actorInput = withActor(actor, input);
        Execution execution = createExecution(actor, agentId, version, actorInput);
        AgentDefinition definition = getAgentDefinition(actor, agentId);
        AgentVersion agentVersion = requireVersion(definition, execution.getVersion());
        AiRun runtimeRun = startExecution(actor, agentId, actorInput, execution);

        AtomicBoolean subscribed = new AtomicBoolean();
        return Flux.defer(() -> {
            if (!subscribed.compareAndSet(false, true)) {
                return Flux.<Map<String, Object>>error(
                        new IllegalStateException("同一流式执行不能重复订阅"));
            }
            return Flux.<Map<String, Object>>create(
                        sink -> TenantScope.withTenant(actor.tenantId(), () -> {
                            AtomicBoolean clientCancelled = new AtomicBoolean();
                            AtomicBoolean approvalPaused = new AtomicBoolean();
                            AtomicReference<Thread> workerThread =
                                    new AtomicReference<>(Thread.currentThread());
                            sink.onCancel(
                                    () -> {
                                        if (approvalPaused.get()
                                                || execution.getStatus() == ExecutionStatus.PAUSED) {
                                            return;
                                        }
                                        clientCancelled.set(true);
                                        if (!execution.cancel()) return;
                                        Thread worker = workerThread.get();
                                        if (worker != null) worker.interrupt();
                                    });
                            try {
                                Map<String, Object> started = new HashMap<>();
                                started.put("status", "STARTED");
                                started.put("executionId", execution.getExecutionId());
                                if (runtimeRun != null) started.put("runtimeRunId", runtimeRun.id());
                                sink.next(started);
                                AtomicReference<Map<String, Object>> finalState =
                                        new AtomicReference<>();
                                Map<String, Object> graphInput =
                                        NodeActorContext.bind(actor,
                                                withRuntime(
                                                        withExecution(actorInput, execution),
                                                        runtimeRun));
                                var compiled = StateGraphBuilder.fromGraph(agentVersion.getGraph())
                                        .build(CompileConfig.builder().checkpointSaver(
                                                new AgentGraphCheckpointSaver(actor.tenantId(),
                                                        execution.getExecutionId(),
                                                        checkpointManager,
                                                        checkpointRecorder(actor, execution)))
                                                .build());
                                RunnableConfig config = RunnableConfig.builder()
                                        .threadId(execution.getExecutionId()).build();
                                compiled.stream(GraphInput.args(graphInput), config)
                                        .forEach(
                                                nodeOutput -> {
                                                    if (clientCancelled.get()
                                                            || sink.isCancelled()
                                                            || !execution
                                                                    .awaitResumeOrCancellation()) {
                                                        throw new ExecutionCancelledException();
                                                    }
                                                    if (nodeOutput instanceof StreamingOutput<?> streaming) {
                                                        String chunk = streaming.chunk();
                                                        if (chunk != null && !chunk.isEmpty()) {
                                                            eventBridge.append(runtimeRun,
                                                                    AiRunEventType.MODEL_DELTA,
                                                                    JSONUtils.toJsonString(
                                                                            Map.of("content", chunk)));
                                                            sink.next(Map.of(
                                                                    "status", "DELTA",
                                                                    "executionId", execution.getExecutionId(),
                                                                    "content", chunk));
                                                        }
                                                        return;
                                                    }
                                                    Map<String, Object> stateData =
                                                            NodeActorContext.publicData(
                                                                    nodeOutput.state().data());
                                                    finalState.set(stateData);
                                                    eventBridge.append(
                                                            runtimeRun,
                                                            AiRunEventType.MODEL_DELTA,
                                                            JSONUtils.toJsonString(
                                                                    Map.of(
                                                                            "node",
                                                                            nodeOutput.node())));
                                                    if (clientCancelled.get() || sink.isCancelled())
                                                        throw new ExecutionCancelledException();
                                                    sink.next(
                                                            Map.of(
                                                                    "executionId",
                                                                    execution.getExecutionId(),
                                                                    "node",
                                                                    nodeOutput.node(),
                                                                    "state",
                                                                    stateData));
                                                });
                                if (clientCancelled.get()
                                        || sink.isCancelled()
                                        || !execution.awaitResumeOrCancellation()) {
                                    throw new ExecutionCancelledException();
                                }
                                Map<String, Object> output =
                                        finalState.get() == null ? Map.of() : finalState.get();
                                if (!execution.complete(output)) {
                                    throw new ExecutionCancelledException();
                                }
                                stateStore.save(actor, execution);
                                eventBridge.finish(runtimeRun, execution);
                                eventPublisher.publish(
                                        new AgentExecutionCompletedEvent(
                                                execution.getExecutionId(),
                                                agentId,
                                                output,
                                                execution.getDurationMs()));
                                sink.next(
                                        Map.of(
                                                "status",
                                                "COMPLETED",
                                                "executionId",
                                                execution.getExecutionId(),
                                                "output",
                                                output));
                                sink.complete();
                            } catch (ExecutionCancelledException e) {
                                execution.cancel();
                                stateStore.save(actor, execution);
                                eventBridge.finish(runtimeRun, execution);
                                if (!clientCancelled.get() && !sink.isCancelled()) {
                                    sink.next(
                                            Map.of(
                                                    "status",
                                                    "CANCELLED",
                                                    "executionId",
                                                    execution.getExecutionId()));
                                }
                                sink.complete();
                            } catch (Exception e) {
                                if (clientCancelled.get() || sink.isCancelled()
                                        || execution.getStatus() == ExecutionStatus.CANCELLED) {
                                    execution.cancel();
                                    stateStore.save(actor, execution);
                                    eventBridge.finish(runtimeRun, execution);
                                    return null;
                                }
                                var approvalRequired = AgentExecutor.approvalRequired(e);
                                if (approvalRequired != null) {
                                    execution.pause();
                                    stateStore.save(actor, execution);
                                    approvalPaused.set(true);
                                    sink.next(Map.of(
                                            "status", "PAUSED",
                                            "executionId", execution.getExecutionId(),
                                            "approvalId", approvalRequired.approvalId()));
                                    sink.complete();
                                    return null;
                                }
                                log.error(
                                        "Agent 流式执行失败: agentIdPresent={}, executionIdPresent={},"
                                                + " errorType={}, errorMessageLength={}",
                                        agentId != null,
                                        execution.getExecutionId() != null,
                                        e.getClass().getSimpleName(),
                                        e.getMessage() == null ? 0 : e.getMessage().length());
                                if (!execution.fail(e.getMessage())) {
                                    stateStore.save(actor, execution);
                                    eventBridge.finish(runtimeRun, execution);
                                    sink.complete();
                                    return null;
                                }
                                stateStore.save(actor, execution);
                                eventBridge.finish(runtimeRun, execution);
                                eventPublisher.publish(
                                        new AgentExecutionFailedEvent(
                                                execution.getExecutionId(),
                                                agentId,
                                                AgentRuntimeImpl.publicFailureCode()));
                                sink.error(e);
                            } finally {
                                workerThread.set(null);
                                stateStore.cleanup(actor, execution);
                            }
                            return null;
                        }));
        })
                .subscribeOn(Schedulers.boundedElastic());
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
            Checkpoint latest = checkpointManager.loadLatestCheckpoint(actor.tenantId(), executionId);
            if (AgentGraphCheckpointSaver.isApprovalPending(latest)) {
                synchronized (active) {
                    return resumeFromCheckpoint(actor, active);
                }
            }
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
        Checkpoint checkpoint = checkpointManager.loadLatestCheckpoint(
                actor.tenantId(), execution.getExecutionId());
        if (checkpoint == null) {
            throw new IllegalStateException("无可恢复检查点，拒绝重跑已执行节点");
        }
        if (!actor.tenantId().equals(checkpoint.getTenantId())
                || !execution.getExecutionId().equals(checkpoint.getExecutionId())) {
            throw new IllegalStateException("检查点与当前执行不匹配");
        }
        if (AgentGraphCheckpointSaver.isStartedMarker(checkpoint)) {
            throw new IllegalStateException("节点可能已执行但结果检查点未保存，拒绝重复副作用");
        }
        if (AgentGraphCheckpointSaver.requiresManualRecovery(checkpoint)) {
            throw new IllegalStateException("图状态含无法无损恢复的值，拒绝自动续跑");
        }
        if (execution.getLastCheckpointId() != null
                && !execution.getLastCheckpointId().equals(checkpoint.getCheckpointId())) {
            throw new IllegalStateException("执行记录中的检查点不是最新检查点，拒绝回放");
        }
        if (!"graph_final".equals(checkpoint.getNodeId())
                && checkpoint.getNextNodeId() == null) {
            throw new IllegalStateException("旧格式中间检查点缺少下一节点，拒绝重跑");
        }
        AiRun runtimeRun = eventBridge.executionRun(actor, execution.getExecutionId());
        if (AgentGraphCheckpointSaver.isApprovalPending(checkpoint)) {
            if (approvals == null || runtimeRun == null) {
                throw new IllegalStateException("审批恢复缺少运行记录或审批服务");
            }
            String approvalId = String.valueOf(checkpoint.getState().get(
                    AgentGraphCheckpointSaver.APPROVAL_ID_KEY));
            ToolApproval approval = approvals.require(approvalId, actor.tenantId(),
                    actor.userId().value());
            if (approval.status() != ToolApprovalStatus.APPROVED
                    || !runtimeRun.id().equals(approval.runId())) {
                throw new IllegalStateException("工具审批尚未通过或不属于当前执行");
            }
        }
        AgentDefinition definition = getAgentDefinition(actor, execution.getAgentId());
        AgentVersion agentVersion = definition.getVersion(execution.getVersion());
        if (agentVersion == null || agentVersion.getGraph() == null) {
            throw new IllegalStateException("检查点对应的 Agent 版本图不存在");
        }
        execution.resume();
        stateStore.save(actor, execution);
        stateStore.put(execution);
        Execution result = agentExecutor.resumeFromCheckpoint(
                actor, execution, definition, agentVersion, checkpoint,
                checkpointRecorder(actor, execution), runtimeRun);
        stateStore.save(actor, result);
        if (result.getStatus().isTerminal()) {
            eventBridge.finish(runtimeRun, result);
            if (result.getStatus() == ExecutionStatus.COMPLETED) {
                eventPublisher.publish(new AgentExecutionCompletedEvent(
                        result.getExecutionId(), result.getAgentId(),
                        result.getOutput(), result.getDurationMs()));
            } else if (result.getStatus() == ExecutionStatus.FAILED) {
                eventPublisher.publish(new AgentExecutionFailedEvent(
                        result.getExecutionId(), result.getAgentId(),
                        AgentRuntimeImpl.publicFailureCode()));
            }
        }
        stateStore.cleanup(actor, result);
        return result;
    }

    void cancel(ActorContext actor, String executionId) {
        Execution execution = stateStore.active(executionId);
        boolean persistedOnly = execution == null;
        if (execution == null) {
            AgentExecutionBO execBO = stateStore.persisted(actor, executionId);
            if (execBO == null) throw new IllegalStateException("执行实例不存在: " + executionId);
            execution = stateStore.rebuild(execBO);
        }
        stateStore.ensureAccessible(actor, execution);
        boolean approvalPaused = execution.getStatus() == ExecutionStatus.PAUSED
                && AgentGraphCheckpointSaver.isApprovalPending(
                        checkpointManager.loadLatestCheckpoint(actor.tenantId(), executionId));
        AgentStateMachine.transition(execution.getStatus(), ExecutionStatus.CANCELLED);
        if (!execution.cancel()) {
            throw new IllegalStateException("执行已结束，不能取消");
        }
        stateStore.save(actor, execution);
        if (persistedOnly || approvalPaused) {
            eventBridge.finish(eventBridge.executionRun(actor, executionId), execution);
            stateStore.cleanup(actor, execution);
        }
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
        int boundedLimit = Math.max(1, Math.min(limit, 100));
        List<AgentExecutionBO> records = actor.platformAdmin()
                ? executionRepository.selectByAgentId(actor.tenantId(), agentId, boundedLimit)
                : executionRepository.selectByAgentIdAndUserId(
                        actor.tenantId(), agentId, actor.userId().value(), boundedLimit);
        return records
                .stream()
                .map(stateStore::rebuild)
                .collect(Collectors.toList());
    }

    List<Execution> getUserHistory(ActorContext actor, Long userId, int limit) {
        if (userId == null || (!actor.platformAdmin() && actor.userId().value() != userId)) {
            throw new IllegalStateException("执行历史不存在");
        }
        int boundedLimit = Math.max(1, Math.min(limit, 100));
        return executionRepository
                .selectByUserId(actor.tenantId(), userId, boundedLimit)
                .stream()
                .filter(bo -> Long.valueOf(userId).equals(bo.getUserId()))
                .limit(boundedLimit)
                .map(stateStore::rebuild)
                .collect(Collectors.toList());
    }

    Execution createExecution(
            ActorContext actor, String agentId, String version, Map<String, Object> input) {
        AgentDefinition definition = getAgentDefinition(actor, agentId);
        String resolvedVersion = version != null && !version.isBlank()
                ? version : definition.getCurrentVersion();
        Execution execution = new Execution(agentId, resolvedVersion, input);
        if (input != null) {
            Object userId = input.get("userId");
            if (userId instanceof Number number) execution.setUserId(number.longValue());
            else if (userId instanceof String value && !value.isBlank()) {
                try {
                    execution.setUserId(Long.parseLong(value));
                } catch (NumberFormatException ignored) {
                    log.warn("Ignoring non-numeric execution userId: {}", value);
                }
            }
            Object sessionId = input.get("sessionId");
            if (sessionId != null) execution.setSessionId(String.valueOf(sessionId));
        }
        return execution;
    }

    AgentDefinition getAgentDefinition(ActorContext actor, String agentId) {
        AgentDefinition definition = cacheManager.getOrLoad(actor, agentId, agentLoader);
        if (definition == null) throw new IllegalStateException("Agent 定义不存在: " + agentId);
        return definition;
    }

    private static AgentVersion requireVersion(AgentDefinition definition, String version) {
        AgentVersion agentVersion = definition.getVersion(version);
        if (agentVersion == null || agentVersion.getGraph() == null) {
            throw new IllegalArgumentException("Agent 版本图不存在: " + version);
        }
        return agentVersion;
    }

    private void beginExecution(ActorContext actor, Execution execution) {
        execution.start();
        stateStore.save(actor, execution);
        stateStore.put(execution);
    }

    private AiRun startExecution(
            ActorContext actor,
            String agentId,
            Map<String, Object> actorInput,
            Execution execution) {
        AiRun runtimeRun = null;
        try {
            beginExecution(actor, execution);
            runtimeRun = eventBridge.start(execution, actorInput);
            eventPublisher.publish(new AgentExecutionStartedEvent(
                    execution.getExecutionId(), agentId, actorInput));
            return runtimeRun;
        } catch (RuntimeException failure) {
            boolean failed = execution.fail(failure.getMessage());
            try {
                stateStore.save(actor, execution);
            } catch (RuntimeException cleanupFailure) {
                suppressDistinct(failure, cleanupFailure);
            }
            try {
                eventBridge.finish(runtimeRun, execution);
            } catch (RuntimeException cleanupFailure) {
                suppressDistinct(failure, cleanupFailure);
            }
            if (failed) {
                try {
                    eventPublisher.publish(new AgentExecutionFailedEvent(
                            execution.getExecutionId(), agentId,
                            AgentRuntimeImpl.publicFailureCode()));
                } catch (RuntimeException cleanupFailure) {
                    suppressDistinct(failure, cleanupFailure);
                }
            }
            try {
                stateStore.cleanup(actor, execution);
            } catch (RuntimeException cleanupFailure) {
                suppressDistinct(failure, cleanupFailure);
            }
            throw failure;
        }
    }

    /** 保留清理异常，同时避免同一个异常实例被添加为自己的附加异常。 */
    private static void suppressDistinct(Exception failure, RuntimeException cleanupFailure) {
        if (cleanupFailure != failure) {
            failure.addSuppressed(cleanupFailure);
        }
    }

    private Consumer<String> checkpointRecorder(ActorContext actor, Execution execution) {
        return checkpointId -> {
            synchronized (execution) {
                execution.setLastCheckpointId(checkpointId);
                stateStore.save(actor, execution);
            }
        };
    }

    static Map<String, Object> withActor(ActorContext actor, Map<String, Object> input) {
        Map<String, Object> actorInput = input == null ? new HashMap<>() : new HashMap<>(input);
        actorInput.remove(NodeActorContext.KEY);
        actorInput.put("tenantId", actor.tenantId().value());
        actorInput.put("userId", actor.userId().value());
        return actorInput;
    }

    static Map<String, Object> withVersion(Map<String, Object> input, String version) {
        Map<String, Object> graphInput = input == null ? new HashMap<>() : new HashMap<>(input);
        graphInput.put("version", version);
        return graphInput;
    }

    /**
     * 绑定已授权的 Agent 身份，供每条节点历史记录使用。
     *
     * @param input 调用方提供的图输入
     * @param execution 已授权的执行，其身份信息会覆盖调用方输入
     * @return 包含所选 Agent 身份和版本的图输入
     */
    static Map<String, Object> withExecution(Map<String, Object> input, Execution execution) {
        Map<String, Object> graphInput = withVersion(input, execution.getVersion());
        graphInput.put("agentId", execution.getAgentId());
        return graphInput;
    }

    static Map<String, Object> withRuntime(Map<String, Object> input, AiRun run) {
        if (run != null) input.put("__aiRunId", run.id());
        return input;
    }

    /**
     * 表示 Execution Cancelled 相关的领域事件或异常信息。
     */
    static final class ExecutionCancelledException extends RuntimeException {
        @java.io.Serial private static final long serialVersionUID = 1L;
    }
}
