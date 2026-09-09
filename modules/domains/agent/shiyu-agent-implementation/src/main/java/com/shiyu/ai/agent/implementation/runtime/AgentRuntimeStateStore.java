package com.shiyu.ai.agent.implementation.runtime;

import com.shiyu.ai.agent.implementation.domain.enums.AgentExecutionStatus;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Owns execution persistence, restoration, tenant checks, and in-memory state. */
@Slf4j
final class AgentRuntimeStateStore {
    private final AgentExecutionRepository executionRepository;
    private final CheckpointManager checkpointManager;
    private final ConcurrentHashMap<String, Execution> activeExecutions = new ConcurrentHashMap<>();

    AgentRuntimeStateStore(AgentExecutionRepository executionRepository, CheckpointManager checkpointManager) {
        this.executionRepository = executionRepository;
        this.checkpointManager = checkpointManager;
    }

    Execution active(String executionId) {
        return activeExecutions.get(executionId);
    }

    void put(Execution execution) {
        activeExecutions.put(execution.getExecutionId(), execution);
    }

    void save(ActorContext actor, Execution execution) {
        AgentExecutionBO bo = new AgentExecutionBO();
        bo.setExecutionId(execution.getExecutionId());
        bo.setAgentId(execution.getAgentId());
        bo.setVersion(execution.getVersion());
        bo.setUserId(actor.userId().value());
        bo.setTenantId(actor.tenantId().value());
        bo.setSessionId(execution.getSessionId());
        bo.setInputData(JSONUtils.toJsonString(execution.getInput()));
        bo.setOutputData(JSONUtils.toJsonString(execution.getOutput()));
        bo.setStatus(toStoredStatus(execution.getStatus()));
        bo.setErrorMessage(execution.getErrorMessage());
        bo.setStartTime(execution.getStartTime());
        bo.setEndTime(execution.getEndTime());
        bo.setDurationMs(execution.getDurationMs());

        AgentExecutionBO existing = executionRepository.selectByExecutionId(
                actor.tenantId(), execution.getExecutionId());
        if (existing != null) {
            bo.setId(existing.getId());
            executionRepository.update(actor.tenantId(), bo);
        } else {
            executionRepository.insert(actor.tenantId(), bo);
        }
    }

    Execution rebuild(AgentExecutionBO bo) {
        Map<String, Object> input = parseData(bo.getInputData());
        if (bo.getTenantId() != null) {
            input = input == null ? new HashMap<>() : new HashMap<>(input);
            input.put("tenantId", bo.getTenantId());
        }
        return Execution.restore(
                bo.getExecutionId(), bo.getAgentId(), bo.getVersion(), resolveStatus(bo), input,
                parseData(bo.getOutputData()), bo.getErrorMessage(), bo.getUserId(), bo.getSessionId(),
                bo.getStartTime(), bo.getEndTime(), bo.getDurationMs());
    }

    AgentExecutionBO persisted(ActorContext actor, String executionId) {
        return executionRepository.selectByExecutionId(actor.tenantId(), executionId);
    }

    void ensureAccessible(ActorContext actor, Execution execution) {
        long executionTenant = number(execution.getInput() == null ? null : execution.getInput().get("tenantId"));
        if (executionTenant <= 0) {
            throw new IllegalStateException("执行实例不存在");
        }
        actor.requireTenant(new TenantId(executionTenant));
    }

    void cleanup(ActorContext actor, Execution execution) {
        if (execution.getStatus().isTerminal()) {
            activeExecutions.remove(execution.getExecutionId());
            checkpointManager.cleanCheckpoints(actor.tenantId(), execution.getExecutionId());
        } else {
            activeExecutions.put(execution.getExecutionId(), execution);
        }
    }

    static Integer toStoredStatus(ExecutionStatus status) {
        if (status == null) return null;
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
        if (status == null) return null;
        return switch (status) {
            case RUNNING -> ExecutionStatus.RUNNING;
            case SUCCESS -> ExecutionStatus.COMPLETED;
            case FAILED -> ExecutionStatus.FAILED;
            case PAUSED -> ExecutionStatus.PAUSED;
            case CANCELLED -> ExecutionStatus.CANCELLED;
        };
    }

    static ExecutionStatus resolveStatus(AgentExecutionBO bo) {
        ExecutionStatus status = fromStoredStatus(bo.getStatus());
        if (status == ExecutionStatus.RUNNING && bo.getEndTime() != null) {
            return bo.getErrorMessage() == null || bo.getErrorMessage().isBlank()
                    ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED;
        }
        return status;
    }

    static Map<String, Object> parseData(String data) {
        return data == null ? null : JSONUtils.parseMap(data);
    }

    static long number(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value != null) {
            try { return Long.parseLong(String.valueOf(value)); }
            catch (NumberFormatException ignored) { }
        }
        return 0;
    }
}
