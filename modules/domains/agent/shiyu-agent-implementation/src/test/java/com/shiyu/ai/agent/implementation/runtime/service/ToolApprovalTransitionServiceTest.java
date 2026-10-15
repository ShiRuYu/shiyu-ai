package com.shiyu.ai.agent.implementation.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunEventType;
import com.shiyu.ai.agent.contract.runtime.AiRunSource;
import com.shiyu.ai.agent.contract.runtime.AiRunStatus;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryToolApprovalRepository;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 验证审批转换与运行状态及事件记录保持一致。 */
class ToolApprovalTransitionServiceTest {

    private static final TenantId TENANT = new TenantId(7);
    private static final long OWNER_USER_ID = 9;

    @Test
    void createsApprovalAndRequiredEventForRunningRun() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AiRun run = run(AiRunStatus.RUNNING);
        when(runtime.requireRun("run-1", TENANT, OWNER_USER_ID)).thenReturn(run);
        ToolApprovalService approvals = new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolApprovalTransitionService transitions = new ToolApprovalTransitionService(approvals, runtime);

        ToolApproval approval = transitions.request("run-1", TENANT, OWNER_USER_ID, "tool", "{}");

        assertEquals(ToolApprovalStatus.PENDING, approval.status());
        assertEquals(approval, approvals.require(approval.id(), TENANT, OWNER_USER_ID));
        verify(runtime).append(
                eq(run),
                eq(AiRunEventType.TOOL_APPROVAL_REQUIRED),
                contains("\"approvalId\":\"" + approval.id() + "\""),
                eq(true));
    }

    @Test
    void rejectsTerminalRunStatusesBeforeCreatingApproval() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        ToolApprovalService approvals = new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolApprovalTransitionService transitions = new ToolApprovalTransitionService(approvals, runtime);

        for (AiRunStatus status : List.of(
                AiRunStatus.COMPLETED, AiRunStatus.FAILED, AiRunStatus.CANCELLED)) {
            when(runtime.requireRun("run-1", TENANT, OWNER_USER_ID)).thenReturn(run(status));

            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> transitions.request("run-1", TENANT, OWNER_USER_ID, "tool", "{}"));

            assertEquals("run is already terminal", error.getMessage());
            assertTrue(approvals.list("run-1", TENANT, OWNER_USER_ID).isEmpty());
        }

        verify(runtime, never()).append(
                any(AiRun.class), any(AiRunEventType.class), anyString(), anyBoolean());
    }

    @Test
    void rejectsApprovalRequestWhenRuntimeReturnsNoRun() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        when(runtime.requireRun("run-1", TENANT, OWNER_USER_ID)).thenReturn(null);
        ToolApprovalService approvals = new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolApprovalTransitionService transitions = new ToolApprovalTransitionService(approvals, runtime);

        assertThrows(
                IllegalStateException.class,
                () -> transitions.request("run-1", TENANT, OWNER_USER_ID, "tool", "{}"));
        assertTrue(approvals.list("run-1", TENANT, OWNER_USER_ID).isEmpty());
        verify(runtime, never()).append(
                any(AiRun.class), any(AiRunEventType.class), anyString(), anyBoolean());
    }

    @Test
    void rejectsDecisionWhenApprovalServiceReturnsNoApproval() {
        ToolApprovalService approvals = mock(ToolApprovalService.class);
        when(approvals.require("missing", TENANT, OWNER_USER_ID)).thenReturn(null);
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        ToolApprovalTransitionService transitions = new ToolApprovalTransitionService(approvals, runtime);

        assertThrows(
                IllegalArgumentException.class,
                () -> transitions.decide("missing", TENANT, OWNER_USER_ID, ToolApprovalStatus.APPROVED));
        verifyNoInteractions(runtime);
    }

    @Test
    void doesNotPublishDecisionEventUntilPendingApprovalUpdateSucceeds() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository() {
            @Override
            public int update(ToolApproval approval, ToolApprovalStatus expectedStatus) {
                return 0;
            }
        };
        Instant created = Instant.now();
        ToolApproval pending = new ToolApproval(
                "approval-1", "run-1", TENANT.value(), OWNER_USER_ID, "tool", "{}",
                ToolApprovalStatus.PENDING, created, null, created.plusSeconds(300));
        repository.insert(pending);

        AiRuntimeService runtime = mock(AiRuntimeService.class);
        when(runtime.requireRun("run-1", TENANT, OWNER_USER_ID)).thenReturn(run(AiRunStatus.RUNNING));
        ToolApprovalTransitionService transitions = new ToolApprovalTransitionService(
                new ToolApprovalService(repository), runtime);

        ToolApproval result = transitions.decide(
                "approval-1", TENANT, OWNER_USER_ID, ToolApprovalStatus.APPROVED);

        assertEquals(ToolApprovalStatus.PENDING, result.status());
        verify(runtime, never()).append(
                any(AiRun.class),
                eq(AiRunEventType.TOOL_APPROVAL_DECIDED),
                anyString(),
                eq(true));
    }

    private static AiRun run(AiRunStatus status) {
        return new AiRun(
                "run-1",
                TENANT,
                new UserId(OWNER_USER_ID),
                "app-1",
                "version-1",
                AiRunSource.API,
                "source-1",
                null,
                null,
                null,
                null,
                "execution-1",
                "model",
                "prompt-hash",
                status,
                0,
                0,
                false,
                null,
                Instant.now(),
                null,
                null,
                0);
    }
}
