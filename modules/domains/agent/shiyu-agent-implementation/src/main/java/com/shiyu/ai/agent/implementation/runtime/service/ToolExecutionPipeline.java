package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.common.foundation.utils.JSONUtils;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.tooling.contract.api.ToolService.ToolExecutionContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiFunction;
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
        Objects.requireNonNull(executor, "executor");
        return executeWithContext(run, request, (arguments, context) -> executor.apply(arguments));
    }

    /**
     * 执行工具，并向执行器传递与审批关联的稳定操作标识。
     *
     * @param run 当前运行
     * @param request 工具请求
     * @param executor 接收参数和执行上下文的工具执行器
     * @return 执行结果或待审批状态
     */
    public Result executeWithContext(
            AiRun run,
            Request request,
            BiFunction<Map<String, Object>, ToolExecutionContext, Object> executor) {
        Objects.requireNonNull(run, "run");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(executor, "executor");
        Map<String, String> approvalEnvelope = new TreeMap<>();
        approvalEnvelope.put("summary", request.argumentsRedacted() == null
                ? "{}" : request.argumentsRedacted());
        approvalEnvelope.put("argumentsSha256", argumentsHash(request.arguments()));
        String redactedArgs = JSONUtils.toJsonString(approvalEnvelope);
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
            approval = approvals.claimForExecution(
                    request.approvalId(), run.tenantId(), run.ownerUserId().value(),
                    run.id(), request.toolName(), redactedArgs);
        }
        ToolExecutionContext executionContext = new ToolExecutionContext(
                approval == null ? request.approvalId() : approval.id());
        Object value;
        try {
            value = executor.apply(request.arguments(), executionContext);
        } catch (RuntimeException ex) {
            runtime.append(
                    run,
                    AiRunEventType.TOOL_COMPLETED,
                    "{\"toolName\":\"" + escape(request.toolName()) + "\",\"success\":false}",
                    true);
            throw ex;
        }
        String payload =
                "{\"toolName\":\"" + escape(request.toolName()) + "\",\"success\":true}";
        runtime.append(run, AiRunEventType.TOOL_COMPLETED, payload, true);
        if (approval != null) {
            approval = approvals.completeClaimed(
                    approval.id(), run.tenantId(), run.ownerUserId().value());
        }
        return Result.completed(value, approval);
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
        Objects.requireNonNull(executor, "executor");
        return executeByIdWithContext(
                runId,
                tenantId,
                ownerUserId,
                request,
                (arguments, context) -> executor.apply(arguments));
    }

    /**
     * 按运行标识执行工具，并向执行器传递稳定操作标识。
     *
     * @param runId 运行记录标识
     * @param tenantId 租户标识
     * @param ownerUserId 所有者用户标识
     * @param request 工具请求
     * @param executor 接收参数和执行上下文的工具执行器
     * @return 执行结果或待审批状态
     */
    public Result executeByIdWithContext(
            String runId,
            TenantId tenantId,
            long ownerUserId,
            Request request,
            BiFunction<Map<String, Object>, ToolExecutionContext, Object> executor) {
        return executeWithContext(
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

    private static String argumentsHash(Map<String, Object> arguments) {
        try {
            byte[] canonical = JSONUtils.toJsonString(canonicalize(arguments))
                    .getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private static Object canonicalize(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sorted = new TreeMap<>();
            map.forEach((key, item) -> sorted.put(String.valueOf(key), canonicalize(item)));
            return sorted;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(ToolExecutionPipeline::canonicalize).toList();
        }
        return value;
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
