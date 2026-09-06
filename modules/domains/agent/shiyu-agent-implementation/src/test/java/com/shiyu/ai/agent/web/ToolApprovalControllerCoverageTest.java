package com.shiyu.ai.agent.web;

import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.runtime.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ToolApprovalControllerCoverageTest {
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
        when(approvals.request(eq("run-1"), eq(tenantId), eq(9L), eq("search"), eq("{}"))).thenReturn(pending);
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
            verify(runtime, atLeastOnce()).append(eq(run), any(AiRunEventType.class), anyString(), eq(true));
        }
    }

    private static ToolApproval approval(String id, String runId, ToolApprovalStatus status) {
        Instant now = Instant.now();
        return new ToolApproval(id, runId, 7, 9, "search", "{}", status, now, null, now.plusSeconds(300));
    }
}
