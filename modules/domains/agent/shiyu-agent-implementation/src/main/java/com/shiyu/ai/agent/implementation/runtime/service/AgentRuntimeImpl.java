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
 * 实现 智能体 Runtime Impl 相关的业务处理、协作逻辑或基础设施能力。
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
     * 执行 智能体 Runtime Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param cacheManager 用于完成本次业务处理的 cacheManager 参数。
     * @param agentLoader 用于完成本次业务处理的 agentLoader 参数。
     * @param executionRepository 用于完成本次业务处理的 executionRepository 参数。
     * @param checkpointRepository 用于完成本次业务处理的 checkpointRepository 参数。
     * @param eventPublisher 用于完成本次业务处理的 eventPublisher 参数。
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
     * 执行 智能体 Runtime Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param cacheManager 用于完成本次业务处理的 cacheManager 参数。
     * @param agentLoader 用于完成本次业务处理的 agentLoader 参数。
     * @param executionRepository 用于完成本次业务处理的 executionRepository 参数。
     * @param checkpointRepository 用于完成本次业务处理的 checkpointRepository 参数。
     * @param eventPublisher 用于完成本次业务处理的 eventPublisher 参数。
     * @param runtime 用于完成本次业务处理的 runtime 参数。
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
     * 执行 智能体 Runtime Impl 相关业务操作，并维护必要的状态和协作关系。
     */
    @PreDestroy
    public void shutdown() {
        agentExecutor.close();
    }

    /**
     * 调用 智能体 Runtime Impl 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 智能体 Runtime Impl 相关操作生成的结果数据。
     */
    @Override
    public Execution execute(ActorContext actor, String agentId, Map<String, Object> input) {
        return execute(actor, agentId, null, input);
    }

    /**
     * 调用 智能体 Runtime Impl 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param version 用于完成本次业务处理的 version 参数。
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 智能体 Runtime Impl 相关操作生成的结果数据。
     */
    @Override
    public Execution execute(
            ActorContext actor, String agentId, String version, Map<String, Object> input) {
        return lifecycle.execute(actor, agentId, version, input);
    }

    /**
     * 调用 智能体 Runtime Impl 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 智能体 Runtime Impl 相关操作生成的结果数据。
     */
    @Override
    public Flux<Map<String, Object>> executeStream(
            ActorContext actor, String agentId, Map<String, Object> input) {
        return executeStream(actor, agentId, null, input);
    }

    /**
     * 调用 智能体 Runtime Impl 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param version 用于完成本次业务处理的 version 参数。
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 智能体 Runtime Impl 相关操作生成的结果数据。
     */
    @Override
    public Flux<Map<String, Object>> executeStream(
            ActorContext actor, String agentId, String version, Map<String, Object> input) {
        return lifecycle.executeStream(actor, agentId, version, input);
    }

    /**
     * 执行 智能体 Runtime Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param executionId 用于定位execution的标识。
     */
    @Override
    public void pause(ActorContext actor, String executionId) {
        lifecycle.pause(actor, executionId);
    }

    /**
     * 执行 智能体 Runtime Impl 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param executionId 用于定位execution的标识。
     * @return 返回 智能体 Runtime Impl 相关操作生成的结果数据。
     */
    @Override
    public Execution resume(ActorContext actor, String executionId) {
        return lifecycle.resume(actor, executionId);
    }

    /**
     * 校验或判断 智能体 Runtime Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param executionId 用于定位execution的标识。
     */
    @Override
    public void cancel(ActorContext actor, String executionId) {
        lifecycle.cancel(actor, executionId);
    }

    /**
     * 查询 智能体 Runtime Impl 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param executionId 用于定位execution的标识。
     * @return 返回 智能体 Runtime Impl 相关操作生成的结果数据。
     */
    @Override
    public ExecutionStatus getStatus(ActorContext actor, String executionId) {
        return lifecycle.getStatus(actor, executionId);
    }

    /**
     * 查询 智能体 Runtime Impl 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param executionId 用于定位execution的标识。
     * @return 返回 智能体 Runtime Impl 相关操作生成的结果数据。
     */
    @Override
    public Execution getExecution(ActorContext actor, String executionId) {
        return lifecycle.getExecution(actor, executionId);
    }

    /**
     * 查询 智能体 Runtime Impl 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Execution> getHistory(ActorContext actor, String agentId, int limit) {
        return lifecycle.getHistory(actor, agentId, limit);
    }

    /**
     * 查询 智能体 Runtime Impl 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param userId 当前操作涉及的用户标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
