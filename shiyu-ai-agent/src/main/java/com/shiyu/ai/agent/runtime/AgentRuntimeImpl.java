package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.cache.AgentLoader;
import com.shiyu.ai.agent.checkpoint.Checkpoint;
import com.shiyu.ai.agent.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.checkpoint.DbCheckpointStore;
import com.shiyu.ai.agent.event.AgentExecutionCompletedEvent;
import com.shiyu.ai.agent.event.AgentExecutionFailedEvent;
import com.shiyu.ai.agent.event.AgentExecutionStartedEvent;
import com.shiyu.ai.agent.event.EventPublisher;
import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.agent.execution.ExecutionStatus;
import com.shiyu.ai.agent.lifecycle.AgentStateMachine;
import com.shiyu.ai.dal.bo.agent.AgentExecutionBO;
import com.shiyu.ai.dal.repository.agent.AgentExecutionRepository;
import com.shiyu.ai.common.core.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import reactor.core.publisher.Flux;

import java.lang.reflect.Field;
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

    private final ConcurrentHashMap<String, Execution> activeExecutions = new ConcurrentHashMap<>();

    public AgentRuntimeImpl(AgentCacheManager cacheManager,
                            AgentLoader agentLoader,
                            AgentExecutionRepository executionRepository,
                            JdbcTemplate jdbcTemplate,
                            EventPublisher eventPublisher) {
        this.cacheManager = cacheManager;
        this.agentLoader = agentLoader;
        this.executionRepository = executionRepository;
        this.eventPublisher = eventPublisher;

        DbCheckpointStore checkpointStore = new DbCheckpointStore(jdbcTemplate);
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

        // 发布执行开始事件
        eventPublisher.publish(new AgentExecutionStartedEvent(
                execution.getExecutionId(), agentId, input));

        Execution result = agentExecutor.executeAgent(definition, agentVersion, input, execution);
        saveExecution(result);

        // 发布执行结果事件
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

        eventPublisher.publish(new AgentExecutionStartedEvent(
                execution.getExecutionId(), agentId, input));

        return Flux.defer(() -> {
            Execution result = agentExecutor.executeAgent(definition, agentVersion, input, execution);
            saveExecution(result);

            if (result.getStatus() == ExecutionStatus.COMPLETED) {
                eventPublisher.publish(new AgentExecutionCompletedEvent(
                        result.getExecutionId(), agentId, result.getOutput(), result.getDurationMs()));
            } else if (result.getStatus() == ExecutionStatus.FAILED) {
                eventPublisher.publish(new AgentExecutionFailedEvent(
                        result.getExecutionId(), agentId, result.getErrorMessage()));
            }

            cleanupExecution(result);
            if (result.getOutput() != null) {
                return Flux.just(result.getOutput());
            }
            return Flux.empty();
        });
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
        AgentExecutionBO execBO = executionRepository.selectByExecutionId(executionId);
        if (execBO == null) {
            Execution execution = activeExecutions.get(executionId);
            if (execution == null) {
                throw new IllegalStateException("执行实例不存在: " + executionId);
            }
            return resumeFromCheckpoint(execution);
        }
        Execution execution = rebuildExecution(execBO);
        return resumeFromCheckpoint(execution);
    }

    private Execution resumeFromCheckpoint(Execution execution) {
        AgentStateMachine.transition(execution.getStatus(), ExecutionStatus.RUNNING);

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
        activeExecutions.remove(executionId);
        saveExecution(execution);
        log.info("Agent 执行已取消: executionId={}", executionId);
    }

    @Override
    public ExecutionStatus getStatus(String executionId) {
        Execution active = activeExecutions.get(executionId);
        if (active != null) return active.getStatus();

        AgentExecutionBO execBO = executionRepository.selectByExecutionId(executionId);
        if (execBO == null) return null;
        return ExecutionStatus.valueOf(execBO.getStatus());
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
        return new Execution(agentId, resolvedVersion, input);
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
            bo.setStatus(execution.getStatus().name());
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
        Execution execution = new Execution(bo.getAgentId(), bo.getVersion(),
                JSONUtils.parseObject(bo.getInputData(), Map.class));
        if (bo.getOutputData() != null) {
            try {
                Field outputField = Execution.class.getDeclaredField("output");
                outputField.setAccessible(true);
                outputField.set(execution, JSONUtils.parseObject(bo.getOutputData(), Map.class));
            } catch (Exception e) {
                log.warn("重建 Execution output 失败", e);
            }
        }
        return execution;
    }

    private void cleanupExecution(Execution execution) {
        if (execution.getStatus().isTerminal()) {
            activeExecutions.remove(execution.getExecutionId());
            checkpointManager.cleanCheckpoints(execution.getExecutionId());
        } else {
            activeExecutions.put(execution.getExecutionId(), execution);
        }
    }
}
