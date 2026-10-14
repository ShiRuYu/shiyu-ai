package com.shiyu.ai.agent.implementation.runtime.service;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.port.AgentConversationCompletionPort;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.port.ToolApprovalRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Releases paused executions after their tool approval deadline passes. */
@Slf4j
@Component
public final class ToolApprovalExpirySweeper {
    private final ToolApprovalRepository repository;
    private final ToolApprovalService approvals;
    private final AiRuntimeService runs;
    private final AgentRuntime agents;
    private final ObjectProvider<AgentConversationCompletionPort> completions;

    public ToolApprovalExpirySweeper(
            ToolApprovalRepository repository,
            ToolApprovalService approvals,
            AiRuntimeService runs,
            AgentRuntime agents,
            ObjectProvider<AgentConversationCompletionPort> completions) {
        this.repository = repository;
        this.approvals = approvals;
        this.runs = runs;
        this.agents = agents;
        this.completions = completions;
    }

    @Scheduled(
            initialDelayString = "${shiyu.agent.approval-expiry-sweep-ms:30000}",
            fixedDelayString = "${shiyu.agent.approval-expiry-sweep-ms:30000}")
    public void sweep() {
        for (ToolApproval candidate : repository.findOverdueRunning(Instant.now(), 100)) {
            try {
                TenantId tenant = new TenantId(candidate.tenantId());
                TenantScope.withTenant(tenant, () -> {
                    expireAndCancel(candidate, tenant);
                    return null;
                });
            } catch (RuntimeException failure) {
                log.warn("Could not close an expired tool approval: {}", failure.getClass().getSimpleName());
            }
        }
    }

    private void expireAndCancel(ToolApproval candidate, TenantId tenant) {
        ToolApproval approval = approvals.decide(
                candidate.id(), tenant, candidate.ownerUserId(), ToolApprovalStatus.REJECTED);
        if (approval.status() != ToolApprovalStatus.EXPIRED) return;

        AiRun run = runs.requireRun(approval.runId(), tenant, approval.ownerUserId());
        if (run.executionId() == null) return;
        ActorContext actor = new ActorContext(tenant, new UserId(approval.ownerUserId()), false);
        ExecutionStatus status = agents.getStatus(actor, run.executionId());
        if (status == ExecutionStatus.PAUSED) {
            agents.cancel(actor, run.executionId());
            status = ExecutionStatus.CANCELLED;
        }
        AgentConversationCompletionPort completion = completions.getIfAvailable();
        if (status != null && status.isTerminal() && completion != null
                && run.conversationId() != null) {
            Execution result = agents.getExecution(actor, run.executionId());
            completion.completeApprovedRun(actor, run, result);
        }
    }
}
