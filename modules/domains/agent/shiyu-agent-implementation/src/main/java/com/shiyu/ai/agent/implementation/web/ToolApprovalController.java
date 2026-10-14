package com.shiyu.ai.agent.implementation.web;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;
import com.shiyu.ai.agent.implementation.runtime.service.ToolApprovalService;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.port.AgentConversationCompletionPort;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.ActorContext;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

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
    /**
     * runtime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiRuntimeService runtime;
    private final AgentRuntime agentRuntime;
    private final AgentConversationCompletionPort conversationCompletion;

    /**
     * 构建或转换 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approvals 用于完成本次业务处理的 approvals 参数。
     * @param runtime 用于完成本次业务处理的 runtime 参数。
     */
    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime) {
        this(approvals, runtime, null, (AgentConversationCompletionPort) null);
    }

    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime,
            AgentRuntime agentRuntime) {
        this(approvals, runtime, agentRuntime, (AgentConversationCompletionPort) null);
    }

    @Autowired
    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime,
            AgentRuntime agentRuntime,
            ObjectProvider<AgentConversationCompletionPort> completionProvider) {
        this(approvals, runtime, agentRuntime, completionProvider.getIfAvailable());
    }

    private ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime,
            AgentRuntime agentRuntime, AgentConversationCompletionPort conversationCompletion) {
        this.approvals = approvals;
        this.runtime = runtime;
        this.agentRuntime = agentRuntime;
        this.conversationCompletion = conversationCompletion;
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
        runtime.requireRun(runId, tenant(), user());
        ToolApproval approval =
                approvals.request(
                        runId, tenant(), user(), request.toolName, request.argumentsRedacted);
        runtime.append(
                runtime.requireRun(runId, tenant(), user()),
                AiRunEventType.TOOL_APPROVAL_REQUIRED,
                "{\"approvalId\":\"" + approval.id() + "\"}",
                true);
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
        ToolApproval before = approvals.require(id, tenant(), user());
        ToolApproval value = approvals.decide(id, tenant(), user(), status);
        if (before == null
                || before.status() == ToolApprovalStatus.PENDING
                        && value.status() != ToolApprovalStatus.PENDING) {
            runtime.append(
                    runtime.requireRun(value.runId(), tenant(), user()),
                    AiRunEventType.TOOL_APPROVAL_DECIDED,
                    "{\"approvalId\":\""
                            + value.id()
                            + "\",\"status\":\""
                            + value.status().name()
                            + "\"}",
                    true);
        }
        return value;
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
}
