package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 编排工具调用的授权校验、执行、超时和结果记录。
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
     * {@code ToolExecutionPipeline} 创建并初始化当前类型实例。
     *
     * @param runtime 参数值，用于执行当前操作。
     * @param approvals 参数值，用于执行当前操作。
     */
    public ToolExecutionPipeline(AiRuntimeService runtime, ToolApprovalService approvals) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.approvals = Objects.requireNonNull(approvals, "approvals");
    }

    /**
     * {@code execute} 执行当前模块定义的业务流程。
     *
     * @param run 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     * @param executor 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code Request} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param toolName toolName 属性，表示该记录组件承载的数据。
     * @param arguments arguments 属性，表示该记录组件承载的数据。
     * @param argumentsRedacted argumentsRedacted 属性，表示该记录组件承载的数据。
     * @param highRisk highRisk 属性，表示该记录组件承载的数据。
     * @param approvalId approvalId 属性，表示该记录组件承载的数据。
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
     * {@code Result} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param status 状态，表示该记录组件承载的数据。
     * @param value 值，表示该记录组件承载的数据。
     * @param approval approval 属性，表示该记录组件承载的数据。
     */
    public record Result(Status status, Object value, ToolApproval approval) {
        static Result approvalRequired(ToolApproval approval) {
            return new Result(Status.APPROVAL_REQUIRED, null, approval);
        }

        static Result completed(Object value, ToolApproval approval) {
            return new Result(Status.COMPLETED, value, approval);
        }

        /**
         * {@code Status} 表示智能体模块中的一组受控业务状态或分类。
         */
        public enum Status {
            APPROVAL_REQUIRED,
            COMPLETED
        }
    }
}
