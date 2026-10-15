package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiAppRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiRunRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryToolApprovalRepository;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;
import com.shiyu.ai.agent.implementation.runtime.service.ToolApprovalService;
import com.shiyu.ai.agent.implementation.runtime.service.ToolExecutionPipeline;

import static org.junit.jupiter.api.Assertions.*;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 验证 工具 Execution Pipeline 相关功能、边界条件、异常路径和协作行为。
 */
class ToolExecutionPipelineTest {
    @Test
    void failedHighRiskToolCannotBeRetriedWithTheSameApproval() {
        AiRuntimeService runtime =
                new AiRuntimeService(new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
        AiRun run = runtime.startRun(
                new AiRunContext(new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                AiRunSource.AGENT, "agent-1", "model", "write");
        ToolApprovalService approvals =
                new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolExecutionPipeline pipeline = new ToolExecutionPipeline(runtime, approvals);
        var request = new ToolExecutionPipeline.Request(
                "filesystem.write", Map.of("path", "a"), "write", true, null);
        String approvalId = pipeline.execute(run, request, args -> {
            throw new AssertionError("must wait for approval");
        }).approval().id();
        approvals.decide(approvalId, new TenantId(1), 2, ToolApprovalStatus.APPROVED);
        var approved = new ToolExecutionPipeline.Request(
                "filesystem.write", Map.of("path", "a"), "write", true, approvalId);
        AtomicInteger calls = new AtomicInteger();

        assertThrows(IllegalStateException.class, () -> pipeline.execute(run, approved, args -> {
            calls.incrementAndGet();
            throw new IllegalStateException("remote result is uncertain");
        }));
        assertThrows(IllegalStateException.class,
                () -> pipeline.execute(run, approved, args -> calls.incrementAndGet()));
        assertEquals(1, calls.get());
        assertEquals(ToolApprovalStatus.CLAIMED,
                approvals.require(approvalId, new TenantId(1), 2).status());
        assertTrue(runtime.events(run.id(), new TenantId(1), 2, -1, 100).stream()
                .anyMatch(event -> event.type() == AiRunEventType.TOOL_COMPLETED
                        && event.payload().contains("\"success\":false")));
    }

    @Test
    void approvedHighRiskActionCannotBeReplayedOrMovedToAnotherRun() {
        AiRuntimeService runtime =
                new AiRuntimeService(new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
        AiRunContext context = new AiRunContext(new TenantId(1), 2,
                null, null, null, null, null, null, Map.of());
        AiRun first = runtime.startRun(context, AiRunSource.AGENT, "agent-1", "model", "first");
        AiRun second = runtime.startRun(context, AiRunSource.AGENT, "agent-1", "model", "second");
        ToolApprovalService approvals =
                new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolExecutionPipeline pipeline = new ToolExecutionPipeline(runtime, approvals);
        ToolExecutionPipeline.Result pending = pipeline.execute(first,
                new ToolExecutionPipeline.Request("filesystem.write", Map.of("path", "a"),
                        "fingerprint-a", true, null),
                args -> { throw new AssertionError("must wait for approval"); });
        approvals.decide(pending.approval().id(), new TenantId(1), 2, ToolApprovalStatus.APPROVED);
        AtomicInteger calls = new AtomicInteger();
        var approved = new ToolExecutionPipeline.Request("filesystem.write",
                Map.of("path", "a"), "fingerprint-a", true, pending.approval().id());

        assertThrows(IllegalStateException.class,
                () -> pipeline.execute(second, approved, args -> calls.incrementAndGet()));
        assertThrows(IllegalStateException.class,
                () -> pipeline.execute(first, new ToolExecutionPipeline.Request("filesystem.read",
                        Map.of("path", "a"), "fingerprint-a", true, pending.approval().id()),
                        args -> calls.incrementAndGet()));
        assertThrows(IllegalStateException.class,
                () -> pipeline.execute(first, new ToolExecutionPipeline.Request("filesystem.write",
                        Map.of("path", "b"), "fingerprint-a", true, pending.approval().id()),
                        args -> calls.incrementAndGet()));
        assertEquals(0, calls.get());

        assertEquals(ToolExecutionPipeline.Result.Status.COMPLETED,
                pipeline.execute(first, approved, args -> calls.incrementAndGet()).status());
        assertEquals("COMPLETED",
                approvals.require(pending.approval().id(), new TenantId(1), 2).status().name());
        assertThrows(IllegalStateException.class,
                () -> pipeline.execute(first, approved, args -> calls.incrementAndGet()));
        assertEquals(1, calls.get());
    }

    @Test
    void highRiskToolMustBeApprovedBeforeExecutionAndEmitsAuditEvents() {
        AiRuntimeService runtime =
                new AiRuntimeService(new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
        AiRun run =
                runtime.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                        AiRunSource.AGENT,
                        "agent-1",
                        "model",
                        "hello");
        ToolApprovalService approvals =
                new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolExecutionPipeline pipeline = new ToolExecutionPipeline(runtime, approvals);

        ToolExecutionPipeline.Result pending =
                pipeline.execute(
                        run,
                        new ToolExecutionPipeline.Request(
                                "filesystem.read",
                                Map.of("path", "/tmp/a"),
                                "{\"path\":\"[redacted]\"}",
                                true,
                                null),
                        args -> {
                            throw new AssertionError("executor must not run before approval");
                        });
        assertEquals(ToolExecutionPipeline.Result.Status.APPROVAL_REQUIRED, pending.status());

        approvals.decide(pending.approval().id(), new TenantId(1), 2, ToolApprovalStatus.APPROVED);
        ToolExecutionPipeline.Result completed =
                pipeline.execute(
                        run,
                        new ToolExecutionPipeline.Request(
                                "filesystem.read",
                                Map.of("path", "/tmp/a"),
                                "{\"path\":\"[redacted]\"}",
                                true,
                                pending.approval().id()),
                        args -> "content");
        assertEquals(ToolExecutionPipeline.Result.Status.COMPLETED, completed.status());
        assertEquals("content", completed.value());
        assertTrue(
                runtime.events(run.id(), new TenantId(1), 2, -1, 100).stream()
                        .anyMatch(e -> e.type() == AiRunEventType.TOOL_APPROVAL_REQUIRED));
        assertTrue(
                runtime.events(run.id(), new TenantId(1), 2, -1, 100).stream()
                        .anyMatch(e -> e.type() == AiRunEventType.TOOL_COMPLETED));
    }

    @Test
    void idBasedExecutionRequiresTypedTenantIdentity() {
        AiRuntimeService runtime =
                new AiRuntimeService(new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
        AiRun run =
                runtime.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                        AiRunSource.AGENT,
                        "agent-1",
                        "model",
                        "hello");
        ToolExecutionPipeline pipeline =
                new ToolExecutionPipeline(
                        runtime, new ToolApprovalService(new InMemoryToolApprovalRepository()));
        assertEquals(
                "ok",
                pipeline.executeById(
                                run.id(),
                                new TenantId(1),
                                2,
                                new ToolExecutionPipeline.Request(
                                        "safe", Map.of(), null, false, null),
                                args -> "ok")
                        .value());
        assertThrows(
                NullPointerException.class,
                () ->
                        pipeline.executeById(
                                run.id(),
                                null,
                                2,
                                new ToolExecutionPipeline.Request(
                                        "safe", Map.of(), null, false, null),
                                args -> "never"));
    }
}
