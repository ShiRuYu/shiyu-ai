package com.shiyu.ai.agent.implementation.web;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;
import com.shiyu.ai.agent.implementation.runtime.service.ToolApprovalService;
import com.shiyu.ai.agent.implementation.runtime.service.ToolApprovalTransitionService;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.port.AgentConversationCompletionPort;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.service.AuditService;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.ActorContext;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 处理 工具 Approval 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/agent")
public class ToolApprovalController {
    /**
     * approvals 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ToolApprovalService approvals;
    private final ToolApprovalTransitionService transitions;
    /**
     * runtime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiRuntimeService runtime;
    private final AgentRuntime agentRuntime;
    private final AgentConversationCompletionPort conversationCompletion;
    private final AuditService auditService;

    /**
     * 构建或转换 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approvals 用于完成本次业务处理的 approvals 参数。
     * @param runtime 用于完成本次业务处理的 runtime 参数。
     */
    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime) {
        this(approvals, runtime, null, (AgentConversationCompletionPort) null, null);
    }

    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime,
            AgentRuntime agentRuntime) {
        this(approvals, runtime, agentRuntime, (AgentConversationCompletionPort) null, null);
    }

    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime,
            AgentRuntime agentRuntime,
            ObjectProvider<AgentConversationCompletionPort> completionProvider) {
        this(approvals, runtime, agentRuntime, completionProvider.getIfAvailable(), null);
    }

    /**
     * 构建支持人工审批核对和操作审计的控制器。
     *
     * @param approvals 工具审批服务
     * @param runtime AI 运行记录服务
     * @param agentRuntime Agent 执行时的运行状态查询服务
     * @param auditService 操作审计服务
     */
    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime,
            AgentRuntime agentRuntime, AuditService auditService) {
        this(approvals, runtime, agentRuntime, (AgentConversationCompletionPort) null, auditService);
    }

    @Autowired
    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime,
            AgentRuntime agentRuntime,
            ObjectProvider<AgentConversationCompletionPort> completionProvider,
            AuditService auditService,
            ToolApprovalTransitionService transitions) {
        this(approvals, runtime, agentRuntime, completionProvider.getIfAvailable(), auditService,
                transitions);
    }

    private ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime,
            AgentRuntime agentRuntime, AgentConversationCompletionPort conversationCompletion,
            AuditService auditService) {
        this(approvals, runtime, agentRuntime, conversationCompletion, auditService,
                new ToolApprovalTransitionService(approvals, runtime));
    }

    private ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime,
            AgentRuntime agentRuntime, AgentConversationCompletionPort conversationCompletion,
            AuditService auditService, ToolApprovalTransitionService transitions) {
        this.approvals = approvals;
        this.transitions = transitions;
        this.runtime = runtime;
        this.agentRuntime = agentRuntime;
        this.conversationCompletion = conversationCompletion;
        this.auditService = auditService;
    }

    /**
     * 查询 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approvals 用于完成本次业务处理的 approvals 参数。
     */
    @GetMapping("/runs/{runId}/approvals")
    public Result<List<ToolApproval>> list(@PathVariable String runId) {
        runtime.requireRun(runId, tenant(), user());
        return Result.success(approvals.list(runId, tenant(), user()));
    }

    /**
     * 查询 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approvals 用于完成本次业务处理的 approvals 参数。
     */
    @GetMapping("/approvals")
    public Result<List<ToolApproval>> listAll() {
        return Result.success(approvals.listAll(tenant(), user()));
    }

    /**
     * 执行 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approvals 用于完成本次业务处理的 approvals 参数。
     */
    @PostMapping("/runs/{runId}/approvals")
    public Result<ToolApproval> request(
            @PathVariable String runId, @Valid @RequestBody Request request) {
        ToolApproval approval = transitions.request(
                runId, tenant(), user(), request.toolName, request.argumentsRedacted);
        return Result.success(approval);
    }

    /**
     * 执行 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approve 用于完成本次业务处理的 approve 参数。
     */
    @PostMapping("/approvals/{id}/approve")
    public Result<ToolApproval> approve(@PathVariable String id) {
        return Result.success(decide(id, ToolApprovalStatus.APPROVED));
    }

    @PostMapping("/approvals/{id}/approve-and-resume")
    public Result<ToolApproval> approveAndResume(@PathVariable String id) {
        requirePendingExecutionReady(id, true);
        ToolApproval approval = decide(id, ToolApprovalStatus.APPROVED);
        if (approval.status() != ToolApprovalStatus.APPROVED
                && approval.status() != ToolApprovalStatus.CLAIMED) {
            throw new IllegalStateException("tool approval is not approved");
        }
        AiRun run = runtime.requireRun(approval.runId(), tenant(), user());
        if (agentRuntime != null && run.executionId() != null) {
            ActorContext actor = ActorContextHttpAdapter.currentActor();
            ExecutionStatus current = agentRuntime.getStatus(actor, run.executionId());
            Execution result;
            if (current == ExecutionStatus.PAUSED) {
                if (approval.status() == ToolApprovalStatus.CLAIMED) {
                    throw new IllegalStateException("claimed approval cannot resume a paused execution");
                }
                result = agentRuntime.resume(actor, run.executionId());
            } else if (current == ExecutionStatus.COMPLETED
                    || current == ExecutionStatus.FAILED) {
                result = agentRuntime.getExecution(actor, run.executionId());
            } else {
                throw new IllegalStateException("agent execution is not waiting for approval");
            }
            if (result.getStatus().isTerminal() && conversationCompletion != null
                    && run.conversationId() != null) {
                conversationCompletion.completeApprovedRun(actor, run, result);
            }
        }
        return Result.success(approval);
    }

    /**
     * 执行 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param reject 用于完成本次业务处理的 reject 参数。
     */
    @PostMapping("/approvals/{id}/reject")
    public Result<ToolApproval> reject(@PathVariable String id) {
        return Result.success(decide(id, ToolApprovalStatus.REJECTED));
    }

    @PostMapping("/approvals/{id}/reject-and-cancel")
    public Result<ToolApproval> rejectAndCancel(@PathVariable String id) {
        requirePendingExecutionReady(id, false);
        ToolApproval approval = decide(id, ToolApprovalStatus.REJECTED);
        if (approval.status() != ToolApprovalStatus.REJECTED
                && approval.status() != ToolApprovalStatus.EXPIRED) {
            throw new IllegalStateException("tool approval can no longer be rejected");
        }
        AiRun run = runtime.requireRun(approval.runId(), tenant(), user());
        if (agentRuntime != null && run.executionId() != null) {
            ActorContext actor = ActorContextHttpAdapter.currentActor();
            ExecutionStatus status = agentRuntime.getStatus(actor, run.executionId());
            if (status == ExecutionStatus.PAUSED) {
                agentRuntime.cancel(actor, run.executionId());
                status = ExecutionStatus.CANCELLED;
            }
            if (status == ExecutionStatus.CANCELLED && conversationCompletion != null
                    && run.conversationId() != null) {
                conversationCompletion.completeApprovedRun(actor, run,
                        agentRuntime.getExecution(actor, run.executionId()));
            }
        }
        return Result.success(approval);
    }

    /**
     * 在操作者已于外部系统确认副作用成功后，关闭终态运行遗留的认领审批。
     *
     * @param id 待人工核对的审批标识
     * @return 已完成的审批记录
     */
    @Transactional
    @PostMapping("/approvals/{id}/confirm-completed")
    public Result<ToolApproval> confirmClaimedSuccess(
            @PathVariable String id, @Valid @RequestBody ManualResolutionRequest request) {
        if (!Boolean.TRUE.equals(request.getExternalSuccessConfirmed())) {
            throw new IllegalArgumentException("external success must be explicitly confirmed");
        }
        TenantId tenantId = tenant();
        long ownerUserId = user();
        ToolApproval current = approvals.require(id, tenantId, ownerUserId);
        if (current.status() != ToolApprovalStatus.CLAIMED) {
            throw new IllegalStateException("only a claimed approval can be reconciled");
        }

        AiRun run = runtime.requireRun(current.runId(), tenantId, ownerUserId);
        if (!terminal(run.status())) {
            throw new IllegalStateException("the runtime run is still active");
        }
        if (run.executionId() != null && !run.executionId().isBlank()) {
            if (agentRuntime == null) {
                throw new IllegalStateException("agent execution status is unavailable");
            }
            ExecutionStatus executionStatus =
                    agentRuntime.getStatus(ActorContextHttpAdapter.currentActor(), run.executionId());
            if (executionStatus == null || !executionStatus.isTerminal()) {
                throw new IllegalStateException("the agent execution is still active");
            }
        }

        if (auditService == null) {
            throw new IllegalStateException("required audit service is unavailable");
        }
        ToolApproval completed = approvals.completeClaimed(id, tenantId, ownerUserId);
        auditService.recordSuccessRequired(
                tenantId,
                ownerUserId,
                null,
                "TOOL_APPROVAL_MANUAL_RECONCILIATION",
                "TOOL_APPROVAL",
                completed.id(),
                Map.of("runId", completed.runId(), "source", "MANUAL_RECONCILIATION"));
        return Result.success(completed);
    }

    /** 在操作者已确认外部副作用没有发生后，关闭终态运行遗留的认领审批。 */
    @Transactional
    @PostMapping("/approvals/{id}/confirm-not-executed")
    public Result<ToolApproval> confirmClaimedNotExecuted(
            @PathVariable String id,
            @Valid @RequestBody ManualNonExecutionResolutionRequest request) {
        if (!Boolean.TRUE.equals(request.getExternalNonExecutionConfirmed())) {
            throw new IllegalArgumentException("external non-execution must be explicitly confirmed");
        }
        TenantId tenantId = tenant();
        long ownerUserId = user();
        ToolApproval current = approvals.require(id, tenantId, ownerUserId);
        if (current.status() != ToolApprovalStatus.CLAIMED) {
            throw new IllegalStateException("only a claimed approval can be reconciled");
        }

        AiRun run = runtime.requireRun(current.runId(), tenantId, ownerUserId);
        if (!terminal(run.status())) {
            throw new IllegalStateException("the runtime run is still active");
        }
        if (run.executionId() != null && !run.executionId().isBlank()) {
            if (agentRuntime == null) {
                throw new IllegalStateException("agent execution status is unavailable");
            }
            ExecutionStatus executionStatus =
                    agentRuntime.getStatus(ActorContextHttpAdapter.currentActor(), run.executionId());
            if (executionStatus == null || !executionStatus.isTerminal()) {
                throw new IllegalStateException("the agent execution is still active");
            }
        }

        if (auditService == null) {
            throw new IllegalStateException("required audit service is unavailable");
        }
        ToolApproval resolved = approvals.markClaimedNotExecuted(id, tenantId, ownerUserId);
        auditService.recordSuccessRequired(
                tenantId,
                ownerUserId,
                null,
                "TOOL_APPROVAL_MANUAL_RECONCILIATION_NOT_EXECUTED",
                "TOOL_APPROVAL",
                resolved.id(),
                Map.of("runId", resolved.runId(), "source", "MANUAL_RECONCILIATION"));
        return Result.success(resolved);
    }

    private static boolean terminal(AiRunStatus status) {
        return status == AiRunStatus.COMPLETED
                || status == AiRunStatus.FAILED
                || status == AiRunStatus.CANCELLED;
    }

    private void requirePendingExecutionReady(String id, boolean approving) {
        ToolApproval before = approvals.require(id, tenant(), user());
        if (before == null || before.status() != ToolApprovalStatus.PENDING
                || agentRuntime == null) {
            return;
        }
        AiRun pendingRun = runtime.requireRun(before.runId(), tenant(), user());
        if (pendingRun.executionId() == null) {
            return;
        }
        ExecutionStatus status = agentRuntime.getStatus(
                ActorContextHttpAdapter.currentActor(), pendingRun.executionId());
        if (status != ExecutionStatus.PAUSED
                && (approving || status != ExecutionStatus.CANCELLED)) {
            throw new IllegalStateException("agent execution is not waiting for approval");
        }
    }

    private ToolApproval decide(String id, ToolApprovalStatus status) {
        return transitions.decide(id, tenant(), user(), status);
    }

    private TenantId tenant() {
        return new TenantId(ActorContextHttpAdapter.tenantId());
    }

    private long user() {
        return ActorContextHttpAdapter.userId();
    }

    /**
     * 封装 Request 操作所需的请求条件和输入数据。
     */
    @Data
    public static class Request {
        private String toolName;
        /**
         * argumentsRedacted 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String argumentsRedacted = "{}";
    }

    /** 封装外部副作用人工核对所需的明确确认。 */
    @Data
    public static class ManualResolutionRequest {
        @NotNull private Boolean externalSuccessConfirmed;
    }

    /** 封装人工确认外部副作用未发生所需的明确确认。 */
    @Data
    public static class ManualNonExecutionResolutionRequest {
        @NotNull private Boolean externalNonExecutionConfirmed;
    }
}
