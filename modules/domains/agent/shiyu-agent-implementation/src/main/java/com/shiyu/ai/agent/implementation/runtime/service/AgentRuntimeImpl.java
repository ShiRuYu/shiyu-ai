package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntimeStateStore;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.implementation.cache.AgentCacheManager;
import com.shiyu.ai.agent.implementation.cache.AgentLoader;
import com.shiyu.ai.agent.implementation.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.implementation.checkpoint.DbCheckpointStore;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.implementation.event.publisher.EventPublisher;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.kernel.context.ActorContext;

import jakarta.annotation.PreDestroy;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 协调 Agent 执行、流式输出和运行控制操作。
 */
public class AgentRuntimeImpl implements AgentRuntime {
    /**
     * agentExecutor 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentExecutor agentExecutor;
    /**
     * 状态存储，表示当前对象中的对应属性。
     */
    private final AgentRuntimeStateStore stateStore;
    /**
     * eventBridge 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentRuntimeEventBridge eventBridge;
    /**
     * lifecycle 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentExecutionLifecycle lifecycle;

    /**
     * {@code AgentRuntimeImpl} 创建并初始化当前类型实例。
     *
     * @param cacheManager 参数值，用于执行当前操作。
     * @param agentLoader 参数值，用于执行当前操作。
     * @param executionRepository 参数值，用于执行当前操作。
     * @param checkpointRepository 参数值，用于执行当前操作。
     * @param eventPublisher 参数值，用于执行当前操作。
     */
    public AgentRuntimeImpl(
            AgentCacheManager cacheManager,
            AgentLoader agentLoader,
            AgentExecutionRepository executionRepository,
            AgentCheckpointRepository checkpointRepository,
            EventPublisher eventPublisher) {
        this(
                cacheManager,
                agentLoader,
                executionRepository,
                checkpointRepository,
                eventPublisher,
                null);
    }

    /**
     * {@code AgentRuntimeImpl} 创建并初始化当前类型实例。
     *
     * @param cacheManager 参数值，用于执行当前操作。
     * @param agentLoader 参数值，用于执行当前操作。
     * @param executionRepository 参数值，用于执行当前操作。
     * @param checkpointRepository 参数值，用于执行当前操作。
     * @param eventPublisher 参数值，用于执行当前操作。
     * @param runtime 参数值，用于执行当前操作。
     */
    public AgentRuntimeImpl(
            AgentCacheManager cacheManager,
            AgentLoader agentLoader,
            AgentExecutionRepository executionRepository,
            AgentCheckpointRepository checkpointRepository,
            EventPublisher eventPublisher,
            AiRuntimeService runtime) {
        CheckpointManager checkpointManager =
                new CheckpointManager(new DbCheckpointStore(checkpointRepository));
        this.agentExecutor = new AgentExecutor(checkpointManager);
        this.stateStore = new AgentRuntimeStateStore(executionRepository, checkpointManager);
        this.eventBridge = new AgentRuntimeEventBridge(runtime);
        this.lifecycle =
                new AgentExecutionLifecycle(
                        cacheManager,
                        agentLoader,
                        executionRepository,
                        agentExecutor,
                        checkpointManager,
                        eventPublisher,
                        stateStore,
                        eventBridge);
    }

    /**
     * {@code shutdown} 执行当前类型定义的业务操作。
     */
    @PreDestroy
    public void shutdown() {
        agentExecutor.close();
    }

    /**
     * {@code execute} 执行当前模块定义的业务流程。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param input 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Execution execute(ActorContext actor, String agentId, Map<String, Object> input) {
        return execute(actor, agentId, null, input);
    }

    /**
     * {@code execute} 执行当前模块定义的业务流程。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     * @param input 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Execution execute(
            ActorContext actor, String agentId, String version, Map<String, Object> input) {
        return lifecycle.execute(actor, agentId, version, input);
    }

    /**
     * {@code executeStream} 执行当前模块定义的业务流程。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param input 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Flux<Map<String, Object>> executeStream(
            ActorContext actor, String agentId, Map<String, Object> input) {
        return executeStream(actor, agentId, null, input);
    }

    /**
     * {@code executeStream} 执行当前模块定义的业务流程。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     * @param input 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Flux<Map<String, Object>> executeStream(
            ActorContext actor, String agentId, String version, Map<String, Object> input) {
        return lifecycle.executeStream(actor, agentId, version, input);
    }

    /**
     * {@code pause} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     */
    @Override
    public void pause(ActorContext actor, String executionId) {
        lifecycle.pause(actor, executionId);
    }

    /**
     * {@code resume} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Execution resume(ActorContext actor, String executionId) {
        return lifecycle.resume(actor, executionId);
    }

    /**
     * {@code cancel} 校验当前操作的输入或状态是否满足约束。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     */
    @Override
    public void cancel(ActorContext actor, String executionId) {
        lifecycle.cancel(actor, executionId);
    }

    /**
     * {@code getStatus} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public ExecutionStatus getStatus(ActorContext actor, String executionId) {
        return lifecycle.getStatus(actor, executionId);
    }

    /**
     * {@code getExecution} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Execution getExecution(ActorContext actor, String executionId) {
        return lifecycle.getExecution(actor, executionId);
    }

    /**
     * {@code getHistory} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Execution> getHistory(ActorContext actor, String agentId, int limit) {
        return lifecycle.getHistory(actor, agentId, limit);
    }

    /**
     * {@code getUserHistory} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Execution> getUserHistory(ActorContext actor, Long userId, int limit) {
        return lifecycle.getUserHistory(actor, userId, limit);
    }

    private static Map<String, Object> withActor(ActorContext actor, Map<String, Object> input) {
        return AgentExecutionLifecycle.withActor(actor, input);
    }

    private static Map<String, Object> withVersion(Map<String, Object> input, String version) {
        return AgentExecutionLifecycle.withVersion(input, version);
    }

    private static Map<String, Object> withRuntime(Map<String, Object> input, AiRun run) {
        return AgentExecutionLifecycle.withRuntime(input, run);
    }

    private long number(Object value) {
        return AgentRuntimeStateStore.number(value);
    }

    private String string(Object value) {
        return AgentRuntimeEventBridge.string(value);
    }

    private static ExecutionStatus resolveStatus(AgentExecutionBO bo) {
        return AgentRuntimeStateStore.resolveStatus(bo);
    }

    private static Map<String, Object> parseData(String data) {
        return AgentRuntimeStateStore.parseData(data);
    }

    private Execution createExecution(
            ActorContext actor, String agentId, String version, Map<String, Object> input) {
        return lifecycle.createExecution(actor, agentId, version, input);
    }

    private AgentDefinition getAgentDefinition(ActorContext actor, String agentId) {
        return lifecycle.getAgentDefinition(actor, agentId);
    }

    private AiRun startRuntime(Execution execution, Map<String, Object> input) {
        return eventBridge.start(execution, input);
    }

    private void appendRuntime(AiRun run, AiRunEventType type, String payload) {
        eventBridge.append(run, type, payload);
    }

    private void finishRuntime(AiRun run, Execution execution) {
        eventBridge.finish(run, execution);
    }

    static String publicFailureCode() {
        return "AGENT_EXECUTION_FAILED";
    }

    public static Integer toStoredStatus(ExecutionStatus status) {
        return AgentRuntimeStateStore.toStoredStatus(status);
    }

    public static ExecutionStatus fromStoredStatus(Integer storedStatus) {
        return AgentRuntimeStateStore.fromStoredStatus(storedStatus);
    }
}
