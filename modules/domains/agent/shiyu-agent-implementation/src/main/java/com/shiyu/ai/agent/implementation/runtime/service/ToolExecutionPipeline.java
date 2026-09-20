package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 实现 工具 Execution Pipeline 相关的业务处理、协作逻辑或基础设施能力。
 */
public final class ToolExecutionPipeline {
    /**
     * runtime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiRuntimeService runtime;
    /**
     * approvals 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ToolApprovalService approvals;

    /**
     * 构建或转换 工具 Execution Pipeline 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param runtime 用于完成本次业务处理的 runtime 参数。
     * @param approvals 用于完成本次业务处理的 approvals 参数。
     */
    public ToolExecutionPipeline(AiRuntimeService runtime, ToolApprovalService approvals) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.approvals = Objects.requireNonNull(approvals, "approvals");
    }

    /**
     * 调用 工具 Execution Pipeline 相关业务数据，并返回处理结果。
     *
     * @param run 用于完成本次业务处理的 run 参数。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @param executor 用于完成本次业务处理的 executor 参数。
     * @return 返回 工具 Execution Pipeline 相关操作生成的结果数据。
     */
    public Result execute(
            AiRun run, Request request, Function<Map<String, Object>, Object> executor) {
        Objects.requireNonNull(run, "run");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(executor, "executor");
        String redactedArgs =
                request.argumentsRedacted() == null ? "{}" : request.argumentsRedacted();
        runtime.append(run, AiRunEventType.TOOL_REQUESTED, redactedArgs, true);
        ToolApproval approval = null;
        if (request.highRisk()) {
            if (request.approvalId() == null || request.approvalId().isBlank()) {
                approval =
                        approvals.request(
                                run.id(),
                                run.tenantId(),
                                run.ownerUserId().value(),
                                request.toolName(),
                                redactedArgs);
                runtime.append(
                        run,
                        AiRunEventType.TOOL_APPROVAL_REQUIRED,
                        "{\"approvalId\":\"" + approval.id() + "\"}",
                        true);
                return Result.approvalRequired(approval);
            }
            approval =
                    approvals.require(
                            request.approvalId(), run.tenantId(), run.ownerUserId().value());
            if (approval.status() != ToolApprovalStatus.APPROVED) {
                throw new IllegalStateException("tool approval is not approved");
            }
        }
        try {
            Object value = executor.apply(request.arguments());
            String payload =
                    "{\"toolName\":\"" + escape(request.toolName()) + "\",\"success\":true}";
            runtime.append(run, AiRunEventType.TOOL_COMPLETED, payload, true);
            return Result.completed(value, approval);
        } catch (RuntimeException ex) {
            runtime.append(
                    run,
                    AiRunEventType.TOOL_COMPLETED,
                    "{\"toolName\":\"" + escape(request.toolName()) + "\",\"success\":false}",
                    true);
            throw ex;
        }
    }

    /**
     * 执行按标识。
     *
     * @param runId 运行记录标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 所有者用户标识。
     * @param request 请求对象。
     * @param executor 线程池执行器。
     *
     * @return 处理结果。
     */
    public Result executeById(
            String runId,
            TenantId tenantId,
            long ownerUserId,
            Request request,
            Function<Map<String, Object>, Object> executor) {
        return execute(
                runtime.requireRun(
                        runId,
                        Objects.requireNonNull(tenantId, "tenantId must not be null"),
                        ownerUserId),
                request,
                executor);
    }

    private static String escape(String value) {
        return Objects.toString(value, "").replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * 封装 Request 相关的不可变数据及其字段约束。
     */
    public record Request(
            String toolName,
            Map<String, Object> arguments,
            String argumentsRedacted,
            boolean highRisk,
            String approvalId) {
        public Request {
            if (toolName == null || toolName.isBlank())
                throw new IllegalArgumentException("tool name is required");
            arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        }
    }

    /**
     * 封装 Result 相关的不可变数据及其字段约束。
     */
    public record Result(Status status, Object value, ToolApproval approval) {
        static Result approvalRequired(ToolApproval approval) {
            return new Result(Status.APPROVAL_REQUIRED, null, approval);
        }

        static Result completed(Object value, ToolApproval approval) {
            return new Result(Status.COMPLETED, value, approval);
        }

        /**
         * 定义 Status 可用的枚举值及其业务语义。
         */
        public enum Status {
            APPROVAL_REQUIRED,
            COMPLETED
        }
    }
}
