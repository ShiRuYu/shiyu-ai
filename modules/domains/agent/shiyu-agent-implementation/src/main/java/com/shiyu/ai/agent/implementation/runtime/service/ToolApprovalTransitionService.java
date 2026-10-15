package com.shiyu.ai.agent.implementation.runtime.service;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunEventType;
import com.shiyu.ai.agent.contract.runtime.AiRunStatus;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Coordinates approval state changes with their corresponding runtime events. */
@Service
public class ToolApprovalTransitionService {

    private final ToolApprovalService approvals;
    private final AiRuntimeService runtime;

    /** Creates a coordinator for approval and runtime persistence. */
    public ToolApprovalTransitionService(ToolApprovalService approvals, AiRuntimeService runtime) {
        this.approvals = approvals;
        this.runtime = runtime;
    }

    /** Creates an approval only while its run is active and records the matching event. */
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

    /** Applies an approval decision and records it atomically with the run event. */
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
