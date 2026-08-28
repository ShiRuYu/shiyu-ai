package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ToolExecutionPipelineTest {
    @Test
    void highRiskToolMustBeApprovedBeforeExecutionAndEmitsAuditEvents() {
        AiRuntimeService runtime = new AiRuntimeService(new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
        AiRun run = runtime.startRun(new AiRunContext(new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                AiRunSource.AGENT, "agent-1", "model", "hello");
        ToolApprovalService approvals = new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolExecutionPipeline pipeline = new ToolExecutionPipeline(runtime, approvals);

        ToolExecutionPipeline.Result pending = pipeline.execute(run,
                new ToolExecutionPipeline.Request("filesystem.read", Map.of("path", "/tmp/a"), "{\"path\":\"[redacted]\"}", true, null),
                args -> { throw new AssertionError("executor must not run before approval"); });
        assertEquals(ToolExecutionPipeline.Result.Status.APPROVAL_REQUIRED, pending.status());

        approvals.decide(pending.approval().id(), new TenantId(1), 2, ToolApprovalStatus.APPROVED);
        ToolExecutionPipeline.Result completed = pipeline.execute(run,
                new ToolExecutionPipeline.Request("filesystem.read", Map.of("path", "/tmp/a"), "{\"path\":\"[redacted]\"}", true, pending.approval().id()),
                args -> "content");
        assertEquals(ToolExecutionPipeline.Result.Status.COMPLETED, completed.status());
        assertEquals("content", completed.value());
        assertTrue(runtime.events(run.id(), new TenantId(1), 2, -1, 100).stream().anyMatch(e -> e.type() == AiRunEventType.TOOL_APPROVAL_REQUIRED));
        assertTrue(runtime.events(run.id(), new TenantId(1), 2, -1, 100).stream().anyMatch(e -> e.type() == AiRunEventType.TOOL_COMPLETED));
    }

    @Test
    void idBasedExecutionRequiresTypedTenantIdentity() {
        AiRuntimeService runtime = new AiRuntimeService(new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
        AiRun run = runtime.startRun(new AiRunContext(new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                AiRunSource.AGENT, "agent-1", "model", "hello");
        ToolExecutionPipeline pipeline = new ToolExecutionPipeline(runtime, new ToolApprovalService(new InMemoryToolApprovalRepository()));
        assertEquals("ok", pipeline.executeById(run.id(), new TenantId(1), 2,
                new ToolExecutionPipeline.Request("safe", Map.of(), null, false, null), args -> "ok").value());
        assertThrows(NullPointerException.class, () -> pipeline.executeById(run.id(), null, 2,
                new ToolExecutionPipeline.Request("safe", Map.of(), null, false, null), args -> "never"));
    }
}
