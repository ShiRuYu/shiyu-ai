package com.shiyu.ai.agent.implementation.web;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;
import com.shiyu.ai.agent.implementation.runtime.service.ToolApprovalService;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.port.AgentConversationCompletionPort;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.service.AuditService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 验证 工具 Approval Controller Coverage 相关功能、边界条件、异常路径和协作行为。
 */
class ToolApprovalControllerCoverageTest {
    @Test
    void approvedApprovalResumesPausedExecutionAndWritesItsResult() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        AgentConversationCompletionPort completion = mock(AgentConversationCompletionPort.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<AgentConversationCompletionPort> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(completion);
        ToolApprovalController controller = new ToolApprovalController(
                approvals, runtime, agents, provider);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval pending = approval("a1", "run-1", ToolApprovalStatus.PENDING);
        ToolApproval approved = approval("a1", "run-1", ToolApprovalStatus.APPROVED);
        AiRun run = mock(AiRun.class);
        Execution finished = mock(Execution.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(pending);
        when(approvals.decide("a1", actor.tenantId(), 9, ToolApprovalStatus.APPROVED))
                .thenReturn(approved);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(run.conversationId()).thenReturn("conversation-1");
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.PAUSED);
        when(agents.resume(actor, "exec-1")).thenReturn(finished);
        when(finished.getStatus()).thenReturn(ExecutionStatus.COMPLETED);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertTrue(controller.approveAndResume("a1").isSuccess());
        }
        verify(agents).resume(actor, "exec-1");
        verify(completion).completeApprovedRun(actor, run, finished);
    }

    @Test
    void rejectedApprovalCancelsPausedExecutionAndWritesItsResult() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        AgentConversationCompletionPort completion = mock(AgentConversationCompletionPort.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<AgentConversationCompletionPort> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(completion);
        ToolApprovalController controller = new ToolApprovalController(
                approvals, runtime, agents, provider);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval pending = approval("a1", "run-1", ToolApprovalStatus.PENDING);
        ToolApproval rejected = approval("a1", "run-1", ToolApprovalStatus.REJECTED);
        AiRun run = mock(AiRun.class);
        Execution cancelled = mock(Execution.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(pending);
        when(approvals.decide("a1", actor.tenantId(), 9, ToolApprovalStatus.REJECTED))
                .thenReturn(rejected);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(run.conversationId()).thenReturn("conversation-1");
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.PAUSED);
        when(agents.getExecution(actor, "exec-1")).thenReturn(cancelled);
        when(cancelled.getStatus()).thenReturn(ExecutionStatus.CANCELLED);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertTrue(controller.rejectAndCancel("a1").isSuccess());
        }
        verify(agents).cancel(actor, "exec-1");
        verify(completion).completeApprovedRun(actor, run, cancelled);
    }

    @Test
    void incompatibleApprovalStateCannotResumeOrCancelAnExecution() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime, agents);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval expired = approval("a1", "run-1", ToolApprovalStatus.EXPIRED);
        ToolApproval approved = approval("a2", "run-1", ToolApprovalStatus.APPROVED);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(expired);
        when(approvals.decide("a1", actor.tenantId(), 9, ToolApprovalStatus.APPROVED))
                .thenReturn(expired);
        when(approvals.require("a2", actor.tenantId(), 9)).thenReturn(approved);
        when(approvals.decide("a2", actor.tenantId(), 9, ToolApprovalStatus.REJECTED))
                .thenReturn(approved);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertThrows(IllegalStateException.class, () -> controller.approveAndResume("a1"));
            assertThrows(IllegalStateException.class, () -> controller.rejectAndCancel("a2"));
        }
        verifyNoInteractions(agents);
    }

    @Test
    void expiredApprovalCanRetryConversationCompletionAfterCancellation() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agentRuntime = mock(AgentRuntime.class);
        AgentConversationCompletionPort completion = mock(AgentConversationCompletionPort.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<AgentConversationCompletionPort> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(completion);
        ToolApprovalController controller = new ToolApprovalController(
                approvals, runtime, agentRuntime, provider);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        AiRun run = mock(AiRun.class);
        Execution cancelled = mock(Execution.class);
        ToolApproval expired = approval("a1", "run-1", ToolApprovalStatus.EXPIRED);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(expired);
        when(approvals.decide("a1", actor.tenantId(), 9, ToolApprovalStatus.REJECTED))
                .thenReturn(expired);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(run.conversationId()).thenReturn("conversation-1");
        when(agentRuntime.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.CANCELLED);
        when(agentRuntime.getExecution(actor, "exec-1")).thenReturn(cancelled);
        when(cancelled.getStatus()).thenReturn(ExecutionStatus.CANCELLED);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertTrue(controller.rejectAndCancel("a1").isSuccess());
        }
        verify(agentRuntime, never()).cancel(any(), anyString());
        verify(completion).completeApprovedRun(actor, run, cancelled);
    }

    @Test
    void claimedApprovalCanRetryConversationCompletionAfterToolFinished() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agentRuntime = mock(AgentRuntime.class);
        AgentConversationCompletionPort completion = mock(AgentConversationCompletionPort.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<AgentConversationCompletionPort> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(completion);
        ToolApprovalController controller = new ToolApprovalController(
                approvals, runtime, agentRuntime, provider);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        AiRun run = mock(AiRun.class);
        Execution finished = mock(Execution.class);
        ToolApproval claimed = approval("a1", "run-1", ToolApprovalStatus.CLAIMED);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(claimed);
        when(approvals.decide("a1", actor.tenantId(), 9, ToolApprovalStatus.APPROVED))
                .thenReturn(claimed);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(run.conversationId()).thenReturn("conversation-1");
        when(agentRuntime.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.COMPLETED);
        when(agentRuntime.getExecution(actor, "exec-1")).thenReturn(finished);
        when(finished.getStatus()).thenReturn(ExecutionStatus.COMPLETED);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertTrue(controller.approveAndResume("a1").isSuccess());
        }
        verify(agentRuntime, never()).resume(any(), anyString());
        verify(completion).completeApprovedRun(actor, run, finished);
    }

    @Test
    void approvedApprovalDoesNotStartAnExecutionThatIsAlreadyRunning() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime, agents);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval approved = approval("a1", "run-1", ToolApprovalStatus.APPROVED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(approved);
        when(approvals.decide("a1", actor.tenantId(), 9, ToolApprovalStatus.APPROVED))
                .thenReturn(approved);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.RUNNING);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertThrows(IllegalStateException.class, () -> controller.approveAndResume("a1"));
        }
        verify(agents, never()).resume(any(), anyString());
    }

    @Test
    void pendingApprovalStaysPendingWhenExecutionHasNotPaused() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime, agents);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval pending = approval("a1", "run-1", ToolApprovalStatus.PENDING);
        ToolApproval approved = approval("a1", "run-1", ToolApprovalStatus.APPROVED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(pending);
        when(approvals.decide("a1", actor.tenantId(), 9, ToolApprovalStatus.APPROVED))
                .thenReturn(approved);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.RUNNING);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertThrows(IllegalStateException.class, () -> controller.approveAndResume("a1"));
        }
        verify(approvals, never()).decide(anyString(), any(), anyLong(), any());
        verify(agents, never()).resume(any(), anyString());
    }

    @Test
    void pendingRejectionStaysPendingWhenExecutionHasNotPaused() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime, agents);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval pending = approval("a1", "run-1", ToolApprovalStatus.PENDING);
        ToolApproval rejected = approval("a1", "run-1", ToolApprovalStatus.REJECTED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(pending);
        when(approvals.decide("a1", actor.tenantId(), 9, ToolApprovalStatus.REJECTED))
                .thenReturn(rejected);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.RUNNING);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertThrows(IllegalStateException.class, () -> controller.rejectAndCancel("a1"));
        }
        verify(approvals, never()).decide(anyString(), any(), anyLong(), any());
        verify(agents, never()).cancel(any(), anyString());
    }

    @Test
    void claimedApprovalCannotRestartPausedExecution() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime, agents);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval claimed = approval("a1", "run-1", ToolApprovalStatus.CLAIMED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(claimed);
        when(approvals.decide("a1", actor.tenantId(), 9, ToolApprovalStatus.APPROVED))
                .thenReturn(claimed);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.PAUSED);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertThrows(IllegalStateException.class, () -> controller.approveAndResume("a1"));
        }
        verify(agents, never()).resume(any(), anyString());
    }

    @Test
    void claimedApprovalCanBeResolvedOnlyAfterRunAndExecutionAreTerminal() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        AuditService audit = mock(AuditService.class);
        ToolApprovalController controller =
                new ToolApprovalController(approvals, runtime, agents, audit);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval claimed = approval("a1", "run-1", ToolApprovalStatus.CLAIMED);
        ToolApproval completed = approval("a1", "run-1", ToolApprovalStatus.COMPLETED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(claimed);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(run.status()).thenReturn(AiRunStatus.FAILED);
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.FAILED);
        when(approvals.completeClaimed("a1", actor.tenantId(), 9)).thenReturn(completed);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertEquals(ToolApprovalStatus.COMPLETED,
                    controller.confirmClaimedSuccess("a1", manualConfirmation()).getData().status());
        }
        verify(approvals).completeClaimed("a1", actor.tenantId(), 9);
        verify(runtime, never()).append(any(), any(), anyString(), anyBoolean());
        verify(audit).recordSuccessRequired(
                eq(actor.tenantId()),
                eq(9L),
                isNull(),
                eq("TOOL_APPROVAL_MANUAL_RECONCILIATION"),
                eq("TOOL_APPROVAL"),
                eq("a1"),
                eq(Map.of("runId", "run-1", "source", "MANUAL_RECONCILIATION")));
    }

    @Test
    void claimedApprovalCanBeResolvedAsNotExecutedWithTerminalStateAndAudit() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        AuditService audit = mock(AuditService.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime, agents, audit);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval claimed = approval("a1", "run-1", ToolApprovalStatus.CLAIMED);
        ToolApproval resolved = approval("a1", "run-1", ToolApprovalStatus.NOT_EXECUTED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(claimed);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(run.status()).thenReturn(AiRunStatus.FAILED);
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.FAILED);
        when(approvals.markClaimedNotExecuted("a1", actor.tenantId(), 9)).thenReturn(resolved);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertEquals(ToolApprovalStatus.NOT_EXECUTED,
                    controller.confirmClaimedNotExecuted("a1", manualNonExecutionConfirmation())
                            .getData().status());
        }
        verify(approvals).markClaimedNotExecuted("a1", actor.tenantId(), 9);
        verify(audit).recordSuccessRequired(
                eq(actor.tenantId()),
                eq(9L),
                isNull(),
                eq("TOOL_APPROVAL_MANUAL_RECONCILIATION_NOT_EXECUTED"),
                eq("TOOL_APPROVAL"),
                eq("a1"),
                eq(Map.of("runId", "run-1", "source", "MANUAL_RECONCILIATION")));
    }

    @Test
    void claimedApprovalCannotBeResolvedAsNotExecutedWhileRunIsActive() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        AuditService audit = mock(AuditService.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime, agents, audit);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval claimed = approval("a1", "run-1", ToolApprovalStatus.CLAIMED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(claimed);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.status()).thenReturn(AiRunStatus.RUNNING);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertThrows(IllegalStateException.class,
                    () -> controller.confirmClaimedNotExecuted("a1", manualNonExecutionConfirmation()));
        }
        verify(approvals, never()).markClaimedNotExecuted(anyString(), any(), anyLong());
        verifyNoInteractions(audit);
    }

    @Test
    void claimedNotExecutedResolutionRequiresAuditService() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval claimed = approval("a1", "run-1", ToolApprovalStatus.CLAIMED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(claimed);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.status()).thenReturn(AiRunStatus.FAILED);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertThrows(IllegalStateException.class,
                    () -> controller.confirmClaimedNotExecuted("a1", manualNonExecutionConfirmation()));
        }
        verify(approvals, never()).markClaimedNotExecuted(anyString(), any(), anyLong());
    }

    @Test
    void claimedApprovalCannotBeResolvedWhileRunOrExecutionIsActive() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime, agents);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval claimed = approval("a1", "run-1", ToolApprovalStatus.CLAIMED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(claimed);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(run.status()).thenReturn(AiRunStatus.RUNNING);
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.RUNNING);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertThrows(IllegalStateException.class,
                    () -> controller.confirmClaimedSuccess("a1", manualConfirmation()));
        }
        verify(approvals, never()).completeClaimed(anyString(), any(), anyLong());
        verify(runtime, never()).append(any(), any(), anyString(), anyBoolean());
    }

    @Test
    void claimedApprovalCannotBeResolvedBeforeAgentExecutionTerminates() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime, agents);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval claimed = approval("a1", "run-1", ToolApprovalStatus.CLAIMED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(claimed);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.executionId()).thenReturn("exec-1");
        when(run.status()).thenReturn(AiRunStatus.COMPLETED);
        when(agents.getStatus(actor, "exec-1")).thenReturn(ExecutionStatus.PAUSED);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertThrows(IllegalStateException.class,
                    () -> controller.confirmClaimedSuccess("a1", manualConfirmation()));
        }
        verify(approvals, never()).completeClaimed(anyString(), any(), anyLong());
    }

    @Test
    void claimedApprovalWithoutAgentExecutionCanBeResolvedForTerminalRun() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AuditService audit = mock(AuditService.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime, null, audit);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval claimed = approval("a1", "run-1", ToolApprovalStatus.CLAIMED);
        ToolApproval completed = approval("a1", "run-1", ToolApprovalStatus.COMPLETED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(claimed);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.status()).thenReturn(AiRunStatus.CANCELLED);
        when(approvals.completeClaimed("a1", actor.tenantId(), 9)).thenReturn(completed);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertEquals(ToolApprovalStatus.COMPLETED,
                    controller.confirmClaimedSuccess("a1", manualConfirmation()).getData().status());
        }
        verify(approvals).completeClaimed("a1", actor.tenantId(), 9);
        verify(audit).recordSuccessRequired(
                eq(actor.tenantId()),
                eq(9L),
                isNull(),
                eq("TOOL_APPROVAL_MANUAL_RECONCILIATION"),
                eq("TOOL_APPROVAL"),
                eq("a1"),
                eq(Map.of("runId", "run-1", "source", "MANUAL_RECONCILIATION")));
    }

    @Test
    void claimedApprovalCannotBeResolvedWhenRequiredAuditServiceIsUnavailable() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval claimed = approval("a1", "run-1", ToolApprovalStatus.CLAIMED);
        AiRun run = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(claimed);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(run);
        when(run.status()).thenReturn(AiRunStatus.FAILED);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertThrows(
                    IllegalStateException.class,
                    () -> controller.confirmClaimedSuccess("a1", manualConfirmation()));
        }
        verify(approvals, never()).completeClaimed(anyString(), any(), anyLong());
    }

    @Test
    void nonClaimedApprovalCannotBeManuallyReconciled() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        when(approvals.require("a1", actor.tenantId(), 9))
                .thenReturn(approval("a1", "run-1", ToolApprovalStatus.APPROVED));
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertThrows(IllegalStateException.class,
                    () -> controller.confirmClaimedSuccess("a1", manualConfirmation()));
        }
        verifyNoInteractions(runtime);
    }

    @Test
    void manualReconciliationRequiresAnExplicitExternalSuccessConfirmation() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime);
        ToolApprovalController.ManualResolutionRequest request =
                new ToolApprovalController.ManualResolutionRequest();
        request.setExternalSuccessConfirmed(false);
        assertThrows(IllegalArgumentException.class,
                () -> controller.confirmClaimedSuccess("a1", request));
        verifyNoInteractions(approvals, runtime);
    }

    @Test
    void notExecutedReconciliationRequiresAnExplicitConfirmation() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime);
        ToolApprovalController.ManualNonExecutionResolutionRequest request =
                new ToolApprovalController.ManualNonExecutionResolutionRequest();
        request.setExternalNonExecutionConfirmed(false);
        assertThrows(IllegalArgumentException.class,
                () -> controller.confirmClaimedNotExecuted("a1", request));
        verifyNoInteractions(approvals, runtime);
    }

    @Test
    void mapsApprovalQueriesRequestsAndStateTransitions() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime);
        AiRun run = mock(AiRun.class);
        ToolApproval pending = approval("a1", "run-1", ToolApprovalStatus.PENDING);
        ToolApproval approved = approval("a1", "run-1", ToolApprovalStatus.APPROVED);
        ToolApproval rejected = approval("a2", "run-1", ToolApprovalStatus.REJECTED);
        when(runtime.requireRun(anyString(), eq(new TenantId(7L)), eq(9L))).thenReturn(run);
        TenantId tenantId = new TenantId(7);
        when(approvals.list("run-1", tenantId, 9)).thenReturn(List.of(pending));
        when(approvals.listAll(tenantId, 9)).thenReturn(List.of(pending));
        when(approvals.request(eq("run-1"), eq(tenantId), eq(9L), eq("search"), eq("{}")))
                .thenReturn(pending);
        when(approvals.require("a1", tenantId, 9)).thenReturn(pending);
        when(approvals.decide("a1", tenantId, 9, ToolApprovalStatus.APPROVED)).thenReturn(approved);
        when(approvals.require("a2", tenantId, 9)).thenReturn(approved);
        when(approvals.decide("a2", tenantId, 9, ToolApprovalStatus.REJECTED)).thenReturn(rejected);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertTrue(controller.list("run-1").isSuccess());
            assertTrue(controller.listAll().isSuccess());
            ToolApprovalController.Request request = new ToolApprovalController.Request();
            request.setToolName("search");
            assertTrue(controller.request("run-1", request).isSuccess());
            assertTrue(controller.approve("a1").isSuccess());
            assertTrue(controller.reject("a2").isSuccess());
            verify(runtime, atLeastOnce())
                    .append(eq(run), any(AiRunEventType.class), anyString(), eq(true));
        }
    }

    @Test
    void terminalRunCannotCreateToolApproval() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        AiRun terminalRun = mock(AiRun.class);
        ToolApproval pending = approval("a1", "run-1", ToolApprovalStatus.PENDING);
        when(terminalRun.status()).thenReturn(AiRunStatus.COMPLETED);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(terminalRun);
        when(approvals.request("run-1", actor.tenantId(), 9, "search", "{}"))
                .thenReturn(pending);
        ToolApprovalController.Request request = new ToolApprovalController.Request();
        request.setToolName("search");

        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertThrows(IllegalStateException.class, () -> controller.request("run-1", request));
        }

        verifyNoInteractions(approvals);
        verify(runtime, never()).append(any(), any(), anyString(), anyBoolean());
    }

    @Test
    void terminalRunCannotChangePendingApprovalState() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        ToolApprovalController controller = new ToolApprovalController(approvals, runtime, agents);
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(9), false);
        ToolApproval pending = approval("a1", "run-1", ToolApprovalStatus.PENDING);
        ToolApproval approved = approval("a1", "run-1", ToolApprovalStatus.APPROVED);
        AiRun terminalRun = mock(AiRun.class);
        when(approvals.require("a1", actor.tenantId(), 9)).thenReturn(pending);
        when(approvals.decide("a1", actor.tenantId(), 9, ToolApprovalStatus.APPROVED))
                .thenReturn(approved);
        when(runtime.requireRun("run-1", actor.tenantId(), 9)).thenReturn(terminalRun);
        when(terminalRun.status()).thenReturn(AiRunStatus.COMPLETED);

        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertThrows(IllegalStateException.class, () -> controller.approveAndResume("a1"));
        }

        verify(approvals, never()).decide(anyString(), any(), anyLong(), any());
        verify(runtime, never()).append(any(), any(), anyString(), anyBoolean());
    }

    private static ToolApproval approval(String id, String runId, ToolApprovalStatus status) {
        Instant now = Instant.now();
        return new ToolApproval(
                id, runId, 7, 9, "search", "{}", status, now, null, now.plusSeconds(300));
    }

    private static ToolApprovalController.ManualResolutionRequest manualConfirmation() {
        ToolApprovalController.ManualResolutionRequest request =
                new ToolApprovalController.ManualResolutionRequest();
        request.setExternalSuccessConfirmed(true);
        return request;
    }

    private static ToolApprovalController.ManualNonExecutionResolutionRequest
            manualNonExecutionConfirmation() {
        ToolApprovalController.ManualNonExecutionResolutionRequest request =
                new ToolApprovalController.ManualNonExecutionResolutionRequest();
        request.setExternalNonExecutionConfirmed(true);
        return request;
    }
}
