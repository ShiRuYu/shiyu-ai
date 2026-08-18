package com.shiyu.ai.runtime;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Common tool execution boundary used by Agent and future MCP adapters.
 * It keeps capability/approval/event ordering in one place; the actual
 * executor remains supplied by the owning tool platform.
 */
public final class ToolExecutionPipeline {
    private final AiRuntimeService runtime;
    private final ToolApprovalService approvals;

    public ToolExecutionPipeline(AiRuntimeService runtime, ToolApprovalService approvals) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.approvals = Objects.requireNonNull(approvals, "approvals");
    }

    public Result execute(AiRun run, Request request, Function<Map<String, Object>, Object> executor) {
        Objects.requireNonNull(run, "run");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(executor, "executor");
        String redactedArgs = request.argumentsRedacted() == null ? "{}" : request.argumentsRedacted();
        runtime.append(run, AiRunEventType.TOOL_REQUESTED, redactedArgs, true);
        ToolApproval approval = null;
        if (request.highRisk()) {
            if (request.approvalId() == null || request.approvalId().isBlank()) {
                approval = approvals.request(run.id(), run.tenantId(), run.ownerUserId(), request.toolName(), redactedArgs);
                runtime.append(run, AiRunEventType.TOOL_APPROVAL_REQUIRED,
                        "{\"approvalId\":\"" + approval.id() + "\"}", true);
                return Result.approvalRequired(approval);
            }
            approval = approvals.require(request.approvalId(), run.tenantId(), run.ownerUserId());
            if (approval.status() != ToolApprovalStatus.APPROVED) {
                throw new IllegalStateException("tool approval is not approved");
            }
        }
        try {
            Object value = executor.apply(request.arguments());
            String payload = "{\"toolName\":\"" + escape(request.toolName()) + "\",\"success\":true}";
            runtime.append(run, AiRunEventType.TOOL_COMPLETED, payload, true);
            return Result.completed(value, approval);
        } catch (RuntimeException ex) {
            runtime.append(run, AiRunEventType.TOOL_COMPLETED,
                    "{\"toolName\":\"" + escape(request.toolName()) + "\",\"success\":false}", true);
            throw ex;
        }
    }

    /** Resolve the durable run at the execution boundary without putting a mutable
     * AiRun object into graph/checkpoint state. */
    public Result executeById(String runId, long tenantId, long ownerUserId, Request request,
                              Function<Map<String, Object>, Object> executor) {
        return execute(runtime.requireRun(runId, tenantId, ownerUserId), request, executor);
    }

    private static String escape(String value) { return Objects.toString(value, "").replace("\\", "\\\\").replace("\"", "\\\""); }

    public record Request(String toolName, Map<String, Object> arguments, String argumentsRedacted,
                          boolean highRisk, String approvalId) {
        public Request {
            if (toolName == null || toolName.isBlank()) throw new IllegalArgumentException("tool name is required");
            arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
        }
    }

    public record Result(Status status, Object value, ToolApproval approval) {
        static Result approvalRequired(ToolApproval approval) { return new Result(Status.APPROVAL_REQUIRED, null, approval); }
        static Result completed(Object value, ToolApproval approval) { return new Result(Status.COMPLETED, value, approval); }
        public enum Status { APPROVAL_REQUIRED, COMPLETED }
    }
}
