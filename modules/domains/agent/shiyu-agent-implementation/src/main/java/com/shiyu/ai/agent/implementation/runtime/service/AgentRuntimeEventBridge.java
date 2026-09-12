package com.shiyu.ai.agent.implementation.runtime.service;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.kernel.context.TenantId;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 将 Agent 生命周期事件转换为运行时持久化事件。
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

    AiRun start(Execution execution, Map<String, Object> input) {
        if (runtime == null) return null;
        long tenant = number(input == null ? null : input.get("tenantId"));
        long owner =
                execution.getUserId() == null
                        ? number(input == null ? null : input.get("userId"))
                        : execution.getUserId();
        if (tenant <= 0 || owner <= 0) return null;
        try {
            String appId = string(input == null ? null : input.get("__appId"));
            String appVersionId = string(input == null ? null : input.get("__appVersionId"));
            AiRun run =
                    runtime.startRun(
                            new AiRunContext(
                                    new TenantId(tenant),
                                    owner,
                                    appId,
                                    appVersionId,
                                    null,
                                    null,
                                    execution.getExecutionId(),
                                    null,
                                    Map.of("agentId", execution.getAgentId())),
                            AiRunSource.AGENT,
                            execution.getAgentId(),
                            null,
                            JSONUtils.toJsonString(input));
            runtime.append(run, AiRunEventType.MODEL_STARTED, "{}", true);
            return run;
        } catch (RuntimeException failure) {
            if (input != null && input.get("__appId") != null) {
                throw new IllegalStateException("runtime admission failed", failure);
            }
            return null;
        }
    }

    void append(AiRun run, AiRunEventType type, String payload) {
        if (run == null || runtime == null) return;
        runtime.append(run, type, payload, true);
    }

    void finish(AiRun run, Execution execution) {
        if (run == null || runtime == null) return;
        try {
            AiRunStatus status =
                    execution.getStatus() == ExecutionStatus.COMPLETED
                            ? AiRunStatus.COMPLETED
                            : execution.getStatus() == ExecutionStatus.CANCELLED
                                    ? AiRunStatus.CANCELLED
                                    : AiRunStatus.FAILED;
            runtime.finish(
                    run.id(),
                    run.tenantId(),
                    run.ownerUserId().value(),
                    status,
                    status == AiRunStatus.FAILED ? "AGENT_EXECUTION_FAILED" : null);
        } catch (RuntimeException ignored) {
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
