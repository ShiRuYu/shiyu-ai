package com.shiyu.ai.agent.implementation.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunEventType;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryToolApprovalRepository;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.port.AgentConversationCompletionPort;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.port.ToolApprovalRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/** 验证审批过期后释放暂停的执行，且不会调用工具。 */
class ToolApprovalExpirySweeperTest {
    @Test
    void concurrentApprovalWinsOverExpiryWithoutCancellingExecution() {
        ToolApprovalRepository repository = mock(ToolApprovalRepository.class);
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runs = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<AgentConversationCompletionPort> provider = mock(ObjectProvider.class);
        Instant created = Instant.now().minusSeconds(600);
        ToolApproval candidate = new ToolApproval("approval-1", "run-1", 7, 9,
                "CALCULATOR", "{}", ToolApprovalStatus.PENDING,
                created, null, created.plusSeconds(300));
        ToolApproval approved = new ToolApproval("approval-1", "run-1", 7, 9,
                "CALCULATOR", "{}", ToolApprovalStatus.APPROVED,
                created, Instant.now(), created.plusSeconds(300));
        when(repository.findOverdueRunning(any(), eq(100))).thenReturn(java.util.List.of(candidate));
        when(approvals.require("approval-1", new TenantId(7), 9)).thenReturn(approved);
        when(approvals.decide("approval-1", new TenantId(7), 9, ToolApprovalStatus.REJECTED))
                .thenReturn(approved);

        new ToolApprovalExpirySweeper(repository,
                new ToolApprovalTransitionService(approvals, runs), runs, agents, provider).sweep();

        verifyNoInteractions(runs, agents);
    }

    @Test
    void alreadyFinishedExecutionIsNotCancelledAndItsConversationIsCompleted() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant created = Instant.now().minusSeconds(600);
        repository.insert(new ToolApproval("approval-1", "run-1", 7, 9, "CALCULATOR",
                "{}", ToolApprovalStatus.EXPIRED, created, created.plusSeconds(300),
                created.plusSeconds(300)));
        AgentRuntime agents = mock(AgentRuntime.class);
        AiRuntimeService runs = mock(AiRuntimeService.class);
        AgentConversationCompletionPort completion = mock(AgentConversationCompletionPort.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<AgentConversationCompletionPort> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(completion);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        AiRun run = mock(AiRun.class);
        Execution finished = mock(Execution.class);
        when(runs.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(run.conversationId()).thenReturn("conversation-1");
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.COMPLETED);
        when(agents.getExecution(actor, "exec-1")).thenReturn(finished);
        when(finished.getStatus()).thenReturn(ExecutionStatus.COMPLETED);

        new ToolApprovalExpirySweeper(repository,
                new ToolApprovalTransitionService(new ToolApprovalService(repository), runs),
                runs, agents, provider).sweep();

        verify(agents, never()).cancel(any(), anyString());
        verify(completion).completeApprovedRun(actor, run, finished);
    }

    @Test
    void oneBadRunDoesNotPreventAnotherExpiredRunFromBeingCancelled() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant created = Instant.now().minusSeconds(600);
        repository.insert(new ToolApproval("a1", "missing", 7, 9, "CALCULATOR",
                "{}", ToolApprovalStatus.PENDING, created, null, created.plusSeconds(300)));
        repository.insert(new ToolApproval("a2", "run-2", 7, 9, "CALCULATOR",
                "{}", ToolApprovalStatus.PENDING, created.plusSeconds(1), null,
                created.plusSeconds(301)));
        AgentRuntime agents = mock(AgentRuntime.class);
        AiRuntimeService runs = mock(AiRuntimeService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<AgentConversationCompletionPort> provider = mock(ObjectProvider.class);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        AiRun run = mock(AiRun.class);
        when(runs.requireRun("missing", actor.tenantId(), 9))
                .thenThrow(new IllegalArgumentException("run not found"));
        when(runs.requireRun("run-2", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-2");
        when(agents.getStatus(actor, "exec-2")).thenReturn(ExecutionStatus.PAUSED);

        new ToolApprovalExpirySweeper(repository,
                new ToolApprovalTransitionService(new ToolApprovalService(repository), runs),
                runs, agents, provider).sweep();

        verify(agents).cancel(actor, "exec-2");
        assertEquals(ToolApprovalStatus.COMPENSATED,
                repository.find("a2", actor.tenantId(), 9).orElseThrow().status());
    }

    @Test
    void overdueApprovalCancelsPausedExecutionAndPersistsConversationOutcome() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant created = Instant.now().minusSeconds(600);
        repository.insert(new ToolApproval("approval-1", "run-1", 7, 9, "CALCULATOR",
                "{}", ToolApprovalStatus.PENDING, created, null, created.plusSeconds(300)));
        AgentRuntime agents = mock(AgentRuntime.class);
        AiRuntimeService runs = mock(AiRuntimeService.class);
        AgentConversationCompletionPort completion = mock(AgentConversationCompletionPort.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<AgentConversationCompletionPort> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(completion);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        AiRun run = mock(AiRun.class);
        Execution cancelled = mock(Execution.class);
        when(runs.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(run.conversationId()).thenReturn("conversation-1");
        when(run.status()).thenReturn(com.shiyu.ai.agent.contract.runtime.AiRunStatus.RUNNING);
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.PAUSED);
        when(agents.getExecution(actor, "exec-1")).thenReturn(cancelled);
        when(cancelled.getStatus()).thenReturn(ExecutionStatus.CANCELLED);

        new ToolApprovalExpirySweeper(repository,
                new ToolApprovalTransitionService(new ToolApprovalService(repository), runs),
                runs, agents, provider).sweep();

        assertEquals(ToolApprovalStatus.COMPENSATED,
                repository.find("approval-1", actor.tenantId(), 9).orElseThrow().status());
        verify(agents).cancel(actor, "exec-1");
        verify(completion).completeApprovedRun(actor, run, cancelled);
        verify(runs).append(eq(run), eq(AiRunEventType.TOOL_APPROVAL_DECIDED),
                argThat(payload -> payload.contains("EXPIRED")), eq(true));
    }

    @Test
    void approvedApprovalIsNotCancelledByExpirySweep() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant created = Instant.now().minusSeconds(600);
        repository.insert(new ToolApproval("approval-1", "run-1", 7, 9, "CALCULATOR",
                "{}", ToolApprovalStatus.APPROVED, created, created.plusSeconds(1),
                created.plusSeconds(300)));
        AgentRuntime agents = mock(AgentRuntime.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<AgentConversationCompletionPort> provider = mock(ObjectProvider.class);

        AiRuntimeService runs = mock(AiRuntimeService.class);
        new ToolApprovalExpirySweeper(repository,
                new ToolApprovalTransitionService(new ToolApprovalService(repository), runs),
                runs, agents, provider).sweep();

        verifyNoInteractions(agents);
    }
}
