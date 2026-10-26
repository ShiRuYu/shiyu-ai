package com.shiyu.ai.agent.implementation.runtime.service;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunEventType;
import com.shiyu.ai.agent.contract.runtime.AiRunStatus;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 协调审批状态变更及其对应的运行时事件。 */
@Service
public class ToolApprovalTransitionService {

    private final ToolApprovalService approvals;
    private final AiRuntimeService runtime;

    /** 创建用于协调审批与运行状态持久化的服务。 */
    public ToolApprovalTransitionService(ToolApprovalService approvals, AiRuntimeService runtime) {
        this.approvals = approvals;
        this.runtime = runtime;
    }

    /** 仅在运行仍处于活动状态时创建审批，并记录对应事件。 */
    @Transactional
    public ToolApproval request(
            String runId,
            TenantId tenantId,
            long ownerUserId,
            String toolName,
            String argumentsRedacted) {
        AiRun run = runtime.requireRun(runId, tenantId, ownerUserId);
        requireActiveRun(run);
        ToolApproval approval = approvals.request(
                runId, tenantId, ownerUserId, toolName, argumentsRedacted);
        runtime.append(
                run,
                AiRunEventType.TOOL_APPROVAL_REQUIRED,
                "{\"approvalId\":\"" + approval.id() + "\"}",
                true);
        return approval;
    }

    /** 在审批过期后关闭待处理审批；终态运行不再接受非终态事件。 */
    @Transactional
    public ToolApproval expireOverdue(String id, TenantId tenantId, long ownerUserId) {
        ToolApproval before = approvals.require(id, tenantId, ownerUserId);
        if (before == null || before.status() != ToolApprovalStatus.PENDING) return before;
        AiRun run = runtime.requireRun(before.runId(), tenantId, ownerUserId);
        ToolApproval value = approvals.decide(
                id, tenantId, ownerUserId, ToolApprovalStatus.REJECTED);
        if (value.status() == ToolApprovalStatus.EXPIRED) {
            if (run.status() == AiRunStatus.RUNNING) {
                runtime.append(
                        run,
                        AiRunEventType.TOOL_APPROVAL_DECIDED,
                        "{\"approvalId\":\"" + value.id() + "\",\"status\":\"EXPIRED\"}",
                        true);
            }
        }
        return value;
    }

    /** 应用审批决定，并与运行事件在同一事务中保存。 */
    @Transactional
    public ToolApproval decide(
            String id, TenantId tenantId, long ownerUserId, ToolApprovalStatus status) {
        ToolApproval before = approvals.require(id, tenantId, ownerUserId);
        if (before == null) {
            throw new IllegalArgumentException("approval not found");
        }

        AiRun run = null;
        if (before.status() == ToolApprovalStatus.PENDING) {
            run = runtime.requireRun(before.runId(), tenantId, ownerUserId);
            requireActiveRun(run);
        }

        ToolApproval value = approvals.decide(id, tenantId, ownerUserId, status);
        if (before.status() == ToolApprovalStatus.PENDING
                && value.status() != ToolApprovalStatus.PENDING) {
            runtime.append(
                    run,
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

    private static void requireActiveRun(AiRun run) {
        if (run == null || terminal(run.status())) {
            throw new IllegalStateException("run is already terminal");
        }
    }

    private static boolean terminal(AiRunStatus status) {
        return status == AiRunStatus.COMPLETED
                || status == AiRunStatus.FAILED
                || status == AiRunStatus.CANCELLED;
    }
}
