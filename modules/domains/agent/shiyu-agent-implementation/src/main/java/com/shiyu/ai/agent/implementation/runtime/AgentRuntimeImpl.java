package com.shiyu.ai.agent.implementation.runtime;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.implementation.cache.AgentCacheManager;
import com.shiyu.ai.agent.implementation.cache.AgentLoader;
import com.shiyu.ai.agent.implementation.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.implementation.checkpoint.DbCheckpointStore;
import com.shiyu.ai.agent.implementation.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.agent.implementation.event.EventPublisher;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.kernel.context.ActorContext;
import jakarta.annotation.PreDestroy;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/** Public runtime facade; lifecycle, state, and event concerns live in collaborators. */
public class AgentRuntimeImpl implements AgentRuntime {
    private final AgentExecutor agentExecutor;
    private final AgentRuntimeStateStore stateStore;
    private final AgentRuntimeEventBridge eventBridge;
    private final AgentExecutionLifecycle lifecycle;

    public AgentRuntimeImpl(AgentCacheManager cacheManager, AgentLoader agentLoader,
                            AgentExecutionRepository executionRepository,
                            AgentCheckpointRepository checkpointRepository,
                            EventPublisher eventPublisher) {
        this(cacheManager, agentLoader, executionRepository, checkpointRepository, eventPublisher, null);
    }

    public AgentRuntimeImpl(AgentCacheManager cacheManager, AgentLoader agentLoader,
                            AgentExecutionRepository executionRepository,
                            AgentCheckpointRepository checkpointRepository,
                            EventPublisher eventPublisher, AiRuntimeService runtime) {
        CheckpointManager checkpointManager = new CheckpointManager(new DbCheckpointStore(checkpointRepository));
        this.agentExecutor = new AgentExecutor(checkpointManager);
        this.stateStore = new AgentRuntimeStateStore(executionRepository, checkpointManager);
        this.eventBridge = new AgentRuntimeEventBridge(runtime);
        this.lifecycle = new AgentExecutionLifecycle(cacheManager, agentLoader, executionRepository,
                agentExecutor, checkpointManager, eventPublisher, stateStore, eventBridge);
    }

    @PreDestroy
    public void shutdown() {
        agentExecutor.close();
    }

    @Override
    public Execution execute(ActorContext actor, String agentId, Map<String, Object> input) {
        return execute(actor, agentId, null, input);
    }

    @Override
    public Execution execute(ActorContext actor, String agentId, String version, Map<String, Object> input) {
        return lifecycle.execute(actor, agentId, version, input);
    }

    @Override
    public Flux<Map<String, Object>> executeStream(ActorContext actor, String agentId, Map<String, Object> input) {
        return executeStream(actor, agentId, null, input);
    }

    @Override
    public Flux<Map<String, Object>> executeStream(ActorContext actor, String agentId, String version,
                                                    Map<String, Object> input) {
        return lifecycle.executeStream(actor, agentId, version, input);
    }

    @Override public void pause(ActorContext actor, String executionId) { lifecycle.pause(actor, executionId); }
    @Override public Execution resume(ActorContext actor, String executionId) { return lifecycle.resume(actor, executionId); }
    @Override public void cancel(ActorContext actor, String executionId) { lifecycle.cancel(actor, executionId); }
    @Override public ExecutionStatus getStatus(ActorContext actor, String executionId) { return lifecycle.getStatus(actor, executionId); }
    @Override public Execution getExecution(ActorContext actor, String executionId) { return lifecycle.getExecution(actor, executionId); }
    @Override public List<Execution> getHistory(ActorContext actor, String agentId, int limit) { return lifecycle.getHistory(actor, agentId, limit); }
    @Override public List<Execution> getUserHistory(ActorContext actor, Long userId, int limit) { return lifecycle.getUserHistory(actor, userId, limit); }

    // Compatibility bridges retain the package's existing reflection-level helper contract.
    private static Map<String, Object> withActor(ActorContext actor, Map<String, Object> input) {
        return AgentExecutionLifecycle.withActor(actor, input);
    }

    private static Map<String, Object> withVersion(Map<String, Object> input, String version) {
        return AgentExecutionLifecycle.withVersion(input, version);
    }

    private static Map<String, Object> withRuntime(Map<String, Object> input, AiRun run) {
        return AgentExecutionLifecycle.withRuntime(input, run);
    }

    private long number(Object value) { return AgentRuntimeStateStore.number(value); }
    private String string(Object value) { return AgentRuntimeEventBridge.string(value); }
    private static ExecutionStatus resolveStatus(AgentExecutionBO bo) { return AgentRuntimeStateStore.resolveStatus(bo); }
    private static Map<String, Object> parseData(String data) { return AgentRuntimeStateStore.parseData(data); }

    private Execution createExecution(ActorContext actor, String agentId, String version, Map<String, Object> input) {
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

    static Integer toStoredStatus(ExecutionStatus status) {
        return AgentRuntimeStateStore.toStoredStatus(status);
    }

    static ExecutionStatus fromStoredStatus(Integer storedStatus) {
        return AgentRuntimeStateStore.fromStoredStatus(storedStatus);
    }
}
