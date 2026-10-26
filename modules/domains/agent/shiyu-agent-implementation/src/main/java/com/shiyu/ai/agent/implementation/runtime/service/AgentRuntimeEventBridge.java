package com.shiyu.ai.agent.implementation.runtime.service;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 实现 智能体 Runtime 事件 Bridge 相关的业务处理、协作逻辑或基础设施能力。
 */
@Slf4j
final class AgentRuntimeEventBridge {
    /**
     * runtime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiRuntimeService runtime;

    AgentRuntimeEventBridge(AiRuntimeService runtime) {
        this.runtime = runtime;
    }

    AiRun executionRun(ActorContext actor, String executionId) {
        if (runtime == null) return null;
        try {
            return runtime.requireExecutionRun(executionId,
                    actor.tenantId(), actor.userId().value());
        } catch (IllegalArgumentException absent) {
            return null;
        }
    }

    AiRun start(Execution execution, Map<String, Object> input) {
        if (runtime == null) return null;
        long tenant = number(input == null ? null : input.get("tenantId"));
        long owner =
                execution.getUserId() == null
                        ? number(input == null ? null : input.get("userId"))
                        : execution.getUserId();
        if (tenant <= 0 || owner <= 0) return null;
        AiRun run = null;
        boolean startedEventAppended = false;
        try {
            String appId = string(input == null ? null : input.get("__appId"));
            String appVersionId = string(input == null ? null : input.get("__appVersionId"));
            String conversationId = string(input == null ? null : input.get("__conversationId"));
            run = runtime.startRun(
                            new AiRunContext(
                                    new TenantId(tenant),
                                    owner,
                                    appId,
                                    appVersionId,
                                    conversationId,
                                    null,
                                    execution.getExecutionId(),
                                    null,
                                    Map.of("agentId", execution.getAgentId())),
                            AiRunSource.AGENT,
                            execution.getAgentId(),
                            null,
                            JSONUtils.toJsonString(input));
            runtime.append(run, AiRunEventType.MODEL_STARTED, "{}", true);
            startedEventAppended = true;
            return runtime.requireRun(run.id(), run.tenantId(), run.ownerUserId().value());
        } catch (RuntimeException failure) {
            if (run != null) {
                try {
                    runtime.finish(run.id(), run.tenantId(), run.ownerUserId().value(),
                            AiRunStatus.FAILED, "RUN_EVENT_WRITE_FAILED");
                } catch (RuntimeException cleanupFailure) {
                    if (cleanupFailure != failure) failure.addSuppressed(cleanupFailure);
                    log.warn("运行记录事件失败后终止运行记录失败: runIdPresent={}, errorType={}",
                            run.id() != null, cleanupFailure.getClass().getSimpleName());
                }
            }
            if (startedEventAppended) {
                throw new IllegalStateException("runtime run could not be loaded after admission", failure);
            }
            if (input != null && input.get("__appId") != null) {
                throw new IllegalStateException("runtime admission failed", failure);
            }
            return null;
        }
    }

    long append(AiRun run, AiRunEventType type, String payload) {
        if (run == null || runtime == null) return 0;
        return runtime.append(run, type, payload, true);
    }

    long lastSequence(AiRun run) {
        if (run == null || runtime == null) return 0;
        return runtime.requireRun(run.id(), run.tenantId(), run.ownerUserId().value())
                .lastEventSeq();
    }

    AiRun finish(AiRun run, Execution execution) {
        if (run == null || runtime == null) return null;
        AiRunStatus status =
                execution.getStatus() == ExecutionStatus.COMPLETED
                        ? AiRunStatus.COMPLETED
                        : execution.getStatus() == ExecutionStatus.CANCELLED
                                ? AiRunStatus.CANCELLED
                                : execution.getStatus() == ExecutionStatus.FAILED
                                        ? AiRunStatus.FAILED
                                        : null;
        if (status == null) {
            throw new IllegalStateException("agent execution is not terminal");
        }
        String errorCode = status == AiRunStatus.FAILED
                ? "AGENT_EXECUTION_FAILED"
                : status == AiRunStatus.CANCELLED ? "CLIENT_CANCELLED" : null;
        try {
            return runtime.finish(
                    run.id(), run.tenantId(), run.ownerUserId().value(), status, errorCode);
        } catch (RuntimeException finishFailure) {
            AiRun current =
                    runtime.requireRun(run.id(), run.tenantId(), run.ownerUserId().value());
            if (current.status() == status) {
                if (!java.util.Objects.equals(current.errorCode(), errorCode)) throw finishFailure;
            } else if (current.status() != AiRunStatus.COMPLETED
                    && current.status() != AiRunStatus.CANCELLED
                    && current.status() != AiRunStatus.FAILED) {
                return runtime.finish(
                        run.id(), run.tenantId(), run.ownerUserId().value(), status, errorCode);
            }
            return runtime.finish(
                    run.id(),
                    run.tenantId(),
                    run.ownerUserId().value(),
                    current.status(),
                    current.errorCode());
        }
    }

    static long number(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    static String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
