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

/** 工具审批超时后，释放暂停的执行。 */
@Slf4j
@Component
public final class ToolApprovalExpirySweeper {
    private final ToolApprovalRepository repository;
    private final ToolApprovalTransitionService transitions;
    private final AiRuntimeService runs;
    private final AgentRuntime agents;
    private final ObjectProvider<AgentConversationCompletionPort> completions;

    public ToolApprovalExpirySweeper(
            ToolApprovalRepository repository,
            ToolApprovalTransitionService transitions,
            AiRuntimeService runs,
            AgentRuntime agents,
            ObjectProvider<AgentConversationCompletionPort> completions) {
        this.repository = repository;
        this.transitions = transitions;
        this.runs = runs;
        this.agents = agents;
        this.completions = completions;
    }

    private static final java.time.Duration COMPENSATION_LEASE = java.time.Duration.ofMinutes(2);

    @Scheduled(
            initialDelayString = "${shiyu.agent.approval-expiry-sweep-ms:30000}",
            fixedDelayString = "${shiyu.agent.approval-expiry-sweep-ms:30000}")
    public void sweep() {
        Instant now = Instant.now();
        for (ToolApproval candidate : repository.findExpiryCompensationWork(now, 100)) {
            try {
                TenantId tenant = new TenantId(candidate.tenantId());
                TenantScope.withTenant(tenant, () -> {
                    expireAndCancel(candidate, tenant, now);
                    return null;
                });
            } catch (RuntimeException failure) {
                log.warn("Could not close an expired tool approval: {}", failure.getClass().getSimpleName());
            }
        }
    }

    private void expireAndCancel(ToolApproval candidate, TenantId tenant, Instant now) {
        ToolApproval approval = candidate;
        if (approval.status() == ToolApprovalStatus.PENDING) {
            approval = transitions.expireOverdue(
                    candidate.id(), tenant, candidate.ownerUserId());
        }
        if (approval == null || approval.status() != ToolApprovalStatus.EXPIRED
                && approval.status() != ToolApprovalStatus.COMPENSATING) return;

        Instant leaseUntil = now.plus(COMPENSATION_LEASE);
        if (repository.claimExpiryCompensation(approval, now, leaseUntil) != 1) return;
        try {
            AiRun run = runs.requireRun(approval.runId(), tenant, approval.ownerUserId());
            if (run.executionId() != null && !run.executionId().isBlank()) {
                ActorContext actor = new ActorContext(
                        tenant, new UserId(approval.ownerUserId()), false);
                ExecutionStatus status = agents.getStatus(actor, run.executionId());
                if (status == ExecutionStatus.PAUSED) {
                    agents.cancel(actor, run.executionId());
                    status = ExecutionStatus.CANCELLED;
                }
                if (status == null || !status.isTerminal()) {
                    throw new IllegalStateException("expired approval execution is not terminal yet");
                }
                if (run.conversationId() != null) {
                    AgentConversationCompletionPort completion = completions.getIfAvailable();
                    if (completion == null) {
                        throw new IllegalStateException("conversation completion service is unavailable");
                    }
                    Execution result = agents.getExecution(actor, run.executionId());
                    completion.completeApprovedRun(actor, run, result);
                }
            }
            if (repository.completeExpiryCompensation(
                    approval.id(), tenant, approval.ownerUserId(), leaseUntil,
                    Instant.now()) != 1) {
                throw new IllegalStateException("expired approval compensation lease was lost");
            }
        } catch (RuntimeException failure) {
            // Keep COMPENSATING with its lease. The row becomes claimable when the lease expires.
            throw failure;
        }
    }
}
