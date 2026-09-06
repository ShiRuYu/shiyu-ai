package com.shiyu.ai.agent.web;

import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.agent.runtime.AgentRuntime;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.runtime.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiRuntimeControllerCoverageTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(7), new UserId(9), false);

    @Test
    void mapsCrudRunsSnapshotsAndNonFollowingStreams() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        AiRuntimeController controller = new AiRuntimeController(runtime, agents);
        AiApp app = new AiApp("app", new TenantId(7), new UserId(9), "Demo", "desc", "ACTIVE", "v1", Instant.now(), Instant.now());
        AiAppVersion version = new AiAppVersion("v1", "app", new TenantId(7), "1", "{\"agentId\":\"agent-1\"}", "PUBLISHED", Instant.now(), Instant.now());
        AiRun run = run("run-1", "gen-1", "exec-1");
        AiRunEvent event = new AiRunEvent("run-1", new TenantId(7), 1, AiRunEventType.RUN_COMPLETED, "{}", true, Instant.now());
        when(runtime.createApp(new TenantId(7), 9, "Demo", "desc")).thenReturn(app);
        when(runtime.listApps(new TenantId(7), 9, 50)).thenReturn(List.of(app));
        when(runtime.createVersion(eq("app"), eq(new TenantId(7L)), eq(9L), eq("1"), anyString())).thenReturn(version);
        when(runtime.versions("app", new TenantId(7), 9)).thenReturn(List.of(version));
        when(runtime.publish("app", "v1", new TenantId(7), 9)).thenReturn(version);
        when(runtime.archive("app", "v1", new TenantId(7), 9)).thenReturn(version);
        when(runtime.preview("app", "v1", new TenantId(7), 9, "hello")).thenReturn(new AiAppPreview("app", "v1", "PUBLISHED", "hash", "model", Map.of(), true));
        when(runtime.startRun(any(AiRunContext.class), eq(AiRunSource.API), eq("src"), eq("model"), eq("hello"))).thenReturn(run);
        when(runtime.requireRun("run-1", new TenantId(7), 9)).thenReturn(run);
        when(runtime.requireGenerationRun("gen-1", new TenantId(7), 9)).thenReturn(run);
        when(runtime.events("run-1", new TenantId(7), 9, 0, 500)).thenReturn(List.of(event));
        when(runtime.events("run-1", new TenantId(7), 9, 0, 5000)).thenReturn(List.of(event));
        when(runtime.events("run-1", new TenantId(7), 9, 0, 1000)).thenReturn(List.of(event));
        when(runtime.finish("run-1", new TenantId(7), 9, AiRunStatus.CANCELLED, "CLIENT_CANCELLED")).thenReturn(run);

        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(ACTOR);
            AiRuntimeController.AppRequest appRequest = new AiRuntimeController.AppRequest();
            appRequest.setName("Demo"); appRequest.setDescription("desc");
            assertTrue(controller.createApp(appRequest).isSuccess());
            assertEquals(1, controller.apps(50).getData().size());
            AiRuntimeController.VersionRequest vr = new AiRuntimeController.VersionRequest(); vr.setVersion("1"); vr.setConfigJson("{}");
            assertTrue(controller.version("app", vr).isSuccess());
            assertTrue(controller.versions("app").isSuccess());
            assertTrue(controller.publish("app", "v1").isSuccess());
            assertTrue(controller.archive("app", "v1").isSuccess());
            AiRuntimeController.PreviewRequest pr = new AiRuntimeController.PreviewRequest(); pr.setAppVersionId("v1"); pr.setPrompt("hello");
            assertTrue(controller.preview("app", pr).isSuccess());
            AiRuntimeController.RunRequest rr = new AiRuntimeController.RunRequest(); rr.setAppId("app"); rr.setAppVersionId("v1"); rr.setSourceId("src"); rr.setModel("model"); rr.setPrompt("hello");
            assertTrue(controller.startRun(rr).isSuccess());
            assertTrue(controller.run("run-1").isSuccess());
            assertTrue(controller.runs(50).isSuccess());
            assertEquals(1, controller.events("run-1", 0, 500, false, 1000, "0").collectList().block().size());
            assertEquals(1, controller.generationEvents("gen-1", 0, false, 1000, "0").collectList().block().size());
            assertEquals(1, controller.eventHistory("run-1", 0, 500).getData().size());
            assertEquals(1, controller.trajectory("run-1").getData().size());
            assertTrue(controller.promptSnapshot("run-1").getData().containsKey("promptHash"));
            assertTrue(controller.cancel("run-1").isSuccess());
        }
    }

    @Test
    void executesPublishedAppAndRejectsMismatchedOrIncompleteBindings() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntime agents = mock(AgentRuntime.class);
        AiRuntimeController controller = new AiRuntimeController(runtime, agents);
        AiAppVersion version = new AiAppVersion("v1", "app", new TenantId(7), "1", "{\"agentId\":\"agent-1\"}", "PUBLISHED", Instant.now(), Instant.now());
        Execution execution = new Execution("agent-1", "v1", Map.of()); execution.start(); execution.complete(Map.of("ok", true));
        AiRun run = run("run-1", null, execution.getExecutionId());
        when(runtime.requirePublishedVersion("app", new TenantId(7), 9)).thenReturn(version);
        when(agents.execute(eq(ACTOR), eq("agent-1"), nullable(String.class), anyMap())).thenReturn(execution);
        when(runtime.requireExecutionRun(execution.getExecutionId(), new TenantId(7), 9)).thenReturn(run);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(ACTOR);
            AiRuntimeController.AppExecutionRequest request = new AiRuntimeController.AppExecutionRequest(); request.setPrompt("go"); request.setAppVersionId("v1");
            assertTrue(controller.executeApp("app", request).getData().containsKey("runtimeRunId"));
            request.setAppVersionId(" ");
            request.setPrompt(null);
            request.setInput(Map.of("extra", true));
            assertTrue(controller.executeApp("app", request).isSuccess());
            AiAppVersion versionWithAgentVersion = new AiAppVersion("v3", "app", new TenantId(7), "3",
                    "{\"agentId\":\"agent-1\",\"agentVersion\":\"v2\"}", "PUBLISHED", Instant.now(), Instant.now());
            when(runtime.requirePublishedVersion("app", new TenantId(7), 9)).thenReturn(versionWithAgentVersion);
            request.setPrompt("with-version");
            assertTrue(controller.executeApp("app", request).isSuccess());
            request.setAppVersionId("other");
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.executeApp("app", request));
            when(runtime.requirePublishedVersion("app", new TenantId(7), 9)).thenReturn(new AiAppVersion("v2", "app", new TenantId(7), "2", "{}", "PUBLISHED", Instant.now(), Instant.now()));
            request.setAppVersionId(null);
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.executeApp("app", request));
        }
    }

    @Test
    void acceptsLastEventIdAndFallsBackForInvalidCursor() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AiRuntimeController controller = new AiRuntimeController(runtime, mock(AgentRuntime.class));
        AiRun run = run("run-1", "gen-1", null);
        when(runtime.requireRun("run-1", new TenantId(7), 9)).thenReturn(run);
        when(runtime.requireGenerationRun("gen-1", new TenantId(7), 9)).thenReturn(run);
        when(runtime.events("run-1", new TenantId(7), 9, 4, 1000)).thenReturn(List.of());
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L); ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertNotNull(controller.events("run-1", 4, 10, false, 1000, "bad").collectList().block());
            assertNotNull(controller.generationEvents("gen-1", 0, false, 1000, "4").collectList().block());
            verify(runtime).events("run-1", new TenantId(7), 9, 4, 10);
        }
    }

    @Test
    void classifiesAllTerminalEventTypes() throws Exception {
        AiRuntimeController controller = new AiRuntimeController(mock(AiRuntimeService.class), mock(AgentRuntime.class));
        var method = AiRuntimeController.class.getDeclaredMethod("isTerminal", AiRunEventType.class);
        method.setAccessible(true);
        assertTrue((Boolean) method.invoke(controller, AiRunEventType.RUN_COMPLETED));
        assertTrue((Boolean) method.invoke(controller, AiRunEventType.RUN_FAILED));
        assertTrue((Boolean) method.invoke(controller, AiRunEventType.RUN_CANCELLED));
        assertFalse((Boolean) method.invoke(controller, AiRunEventType.MODEL_STARTED));
    }

    @Test
    void streamsFollowModeUntilTerminalEvent() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AiRuntimeController controller = new AiRuntimeController(runtime, mock(AgentRuntime.class));
        AiRun run = run("run-follow", "gen-follow", null);
        AiRunEvent terminal = new AiRunEvent("run-follow", new TenantId(7), 2,
                AiRunEventType.RUN_CANCELLED, "{}", true, Instant.now());
        when(runtime.requireRun("run-follow", new TenantId(7), 9)).thenReturn(run);
        when(runtime.events("run-follow", new TenantId(7), 9, 0, 1000)).thenReturn(List.of(terminal));
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertEquals(2, controller.events("run-follow", 0, 10, true, 1000, null)
                    .collectList().block().size());
        }
    }

    private static AiRun run(String id, String generation, String execution) {
        return new AiRun(id, new TenantId(7), new UserId(9), "app", "v1", AiRunSource.API, "src", null, "trace", "conv", generation, execution,
                "model", "hash", AiRunStatus.RUNNING, 2, 3, true, null, Instant.now(), null, null, 1, 0);
    }
}
