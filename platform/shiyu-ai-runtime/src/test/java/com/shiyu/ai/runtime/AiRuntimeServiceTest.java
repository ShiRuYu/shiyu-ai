package com.shiyu.ai.runtime;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class AiRuntimeServiceTest {
    @Test
    void appVersionsArePublishedBeforeExecution() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(1, 2, "Tutor", "demo");
        AiAppVersion version = service.createVersion(app.id(), 1, 2, "1.0.0", "{}");
        AiRunContext context = new AiRunContext(1, 2, app.id(), version.id(), "conversation", "generation", null, "trace", Map.of());
        assertThrows(IllegalStateException.class, () -> service.startRun(context, AiRunSource.CONVERSATION, "generation", "model", "prompt"));

        service.publish(app.id(), version.id(), 1, 2);
        AiRun run = service.startRun(context, AiRunSource.CONVERSATION, "generation", "model", "prompt");
        service.append(run, AiRunEventType.MODEL_DELTA, "{\"text\":\"hi\"}", true);
        service.finish(run.id(), 1, 2, AiRunStatus.COMPLETED, null);

        assertEquals(3, service.events(run.id(), 1, 2, 0, 20).size());
        assertEquals(AiRunEventType.RUN_STARTED, service.events(run.id(), 1, 2, 0, 20).get(0).type());
        assertEquals(AiRunEventType.RUN_COMPLETED, service.events(run.id(), 1, 2, 0, 20).get(2).type());
        assertEquals(AiRunStatus.COMPLETED, service.finish(run.id(), 1, 2, AiRunStatus.CANCELLED, "late_cancel").status());
    }

    @Test
    void concurrentEventAppendUsesEachSequenceOnce() throws Exception {
        AiRuntimeService service = new AiRuntimeService();
        AiRun run = service.startRun(new AiRunContext(1, 2, null, null, "c", "g", null, null, Map.of()),
                AiRunSource.CONVERSATION, "g", "model", "prompt");
        var pool = Executors.newFixedThreadPool(8);
        CountDownLatch ready = new CountDownLatch(8);
        CountDownLatch go = new CountDownLatch(1);
        for (int i = 0; i < 8; i++) {
            pool.submit(() -> { ready.countDown(); go.await(); service.append(run, AiRunEventType.MODEL_DELTA, "{}", true); return null; });
        }
        ready.await(); go.countDown(); pool.shutdown();
        while (!pool.isTerminated()) Thread.yield();
        var events = service.events(run.id(), 1, 2, 0, 30);
        assertEquals(9, events.size());
        assertEquals(java.util.stream.LongStream.rangeClosed(1, 9).boxed().toList(), events.stream().map(AiRunEvent::seq).toList());
    }

    @Test
    void runtimeEventCarriesReplayAndProviderContext() {
        AiRuntimeService service = new AiRuntimeService();
        AiRun run = service.startRun(new AiRunContext(7, 8, null, null, "c", "g", "e", "trace-1", Map.of()),
                AiRunSource.CONVERSATION, "g", "deepseek-v4-flash", "prompt");
        service.append(run, AiRunEventType.MODEL_STARTED, "{}", true, "turn-1", "step-1", "provider-req-1");
        AiRunEvent event = service.events(run.id(), 7, 8, 1, 1).get(0);
        assertEquals("turn-1", event.turnId());
        assertEquals("step-1", event.stepId());
        assertEquals("provider-req-1", event.providerRequestId());
        assertEquals("c", event.conversationId());
        assertEquals("g", event.generationId());
        assertEquals("e", event.executionId());
        assertEquals("trace-1", event.traceId());
    }

    @Test
    void previewExposesOnlyImmutableVersionConfiguration() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(1, 2, "Tutor", "demo");
        AiAppVersion version = service.createVersion(app.id(), 1, 2, "1.0.0", "{\"model\":\"configured\",\"temperature\":0.2}");
        AiAppPreview draft = service.preview(app.id(), version.id(), 1, 2, "hello");
        assertFalse(draft.executable());
        service.publish(app.id(), version.id(), 1, 2);
        AiAppPreview published = service.preview(app.id(), version.id(), 1, 2, "hello");
        assertTrue(published.executable());
        assertEquals("configured", published.model());
        assertEquals("0.2", String.valueOf(published.configuration().get("temperature")));
    }

    @Test
    void publishRejectsMalformedConfigurationAndPublishedVersionCannotBeArchived() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(1, 2, "Tutor", "demo");
        AiAppVersion invalid = service.createVersion(app.id(), 1, 2, "bad", "[]");
        assertThrows(IllegalArgumentException.class, () -> service.publish(app.id(), invalid.id(), 1, 2));
        AiAppVersion valid = service.createVersion(app.id(), 1, 2, "good", "{}");
        service.publish(app.id(), valid.id(), 1, 2);
        assertThrows(IllegalStateException.class, () -> service.archive(app.id(), valid.id(), 1, 2));
        assertThrows(IllegalStateException.class, () -> service.publish(app.id(), valid.id(), 1, 2));
    }

    @Test
    void publishEnforcesDeclaredValidationAndEvaluationGates() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(1, 2, "Tutor", "demo");
        AiAppVersion failed = service.createVersion(app.id(), 1, 2, "failed", "{\"validation\":{\"tools\":\"FAILED\"}}");
        assertThrows(IllegalArgumentException.class, () -> service.publish(app.id(), failed.id(), 1, 2));
        AiAppVersion below = service.createVersion(app.id(), 1, 2, "below", "{\"evaluation\":{\"requiredPassRate\":0.9,\"passRate\":0.75}}");
        assertThrows(IllegalArgumentException.class, () -> service.publish(app.id(), below.id(), 1, 2));
        AiAppVersion passed = service.createVersion(app.id(), 1, 2, "passed", "{\"validation\":{\"graph\":\"PASS\",\"model\":\"PASS\",\"knowledge\":\"PASS\",\"tools\":\"PASS\",\"budget\":\"PASS\"},\"evaluation\":{\"requiredPassRate\":0.9,\"passRate\":0.95}}");
        assertEquals("PUBLISHED", service.publish(app.id(), passed.id(), 1, 2).status());
    }

    @Test
    void publishFailsClosedForDeclaredExecutableBindings() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(1, 2, "Agent", "demo");
        AiAppVersion missingAgent = service.createVersion(app.id(), 1, 2, "missing-agent", "{\"executionType\":\"AGENT\"}");
        assertThrows(IllegalArgumentException.class, () -> service.publish(app.id(), missingAgent.id(), 1, 2));
        AiAppVersion missingGate = service.createVersion(app.id(), 1, 2, "missing-gate", "{\"agentId\":\"agent-1\",\"validation\":{\"model\":\"PASS\"}}");
        assertThrows(IllegalArgumentException.class, () -> service.publish(app.id(), missingGate.id(), 1, 2));
        AiAppVersion ready = service.createVersion(app.id(), 1, 2, "ready", "{\"executionType\":\"AGENT\",\"agentId\":\"agent-1\",\"model\":\"deepseek-v4-flash\",\"validation\":{\"graph\":\"PASS\",\"model\":\"PASS\"}}");
        assertEquals("PUBLISHED", service.publish(app.id(), ready.id(), 1, 2).status());
    }

    @Test
    void linksAnOpenAiRunToTheStoredGenerationExactlyOnce() {
        AiRuntimeService service = new AiRuntimeService();
        AiRun run = service.startRun(new AiRunContext(1, 2, null, null, "conversation", null, null, null, Map.of()),
                AiRunSource.API, "openai", "deepseek-v4-flash", "prompt-hash-input");

        AiRun linked = service.linkGeneration(run, "generation-1");

        assertEquals("generation-1", linked.generationId());
        assertEquals(linked.id(), service.requireGenerationRun("generation-1", 1, 2).id());
        assertSame(linked, service.linkGeneration(linked, "generation-1"));
        assertThrows(IllegalStateException.class, () -> service.linkGeneration(linked, "generation-2"));
    }

    @Test
    void terminalEventIsIdempotentAndCannotChangeMeaning() {
        AiRuntimeService service = new AiRuntimeService();
        AiRun run = service.startRun(new AiRunContext(1, 2, null, null, "conversation", null, null, null, Map.of()),
                AiRunSource.CONVERSATION, "conversation-1", "deepseek-v4-flash", "prompt");
        service.append(run, AiRunEventType.RUN_COMPLETED, "{}", true);
        service.append(run, AiRunEventType.RUN_COMPLETED, "{}", true);
        assertEquals(2, service.events(run.id(), 1, 2, 0, 20).size());
        assertThrows(IllegalStateException.class, () -> service.append(run, AiRunEventType.RUN_FAILED, "{}", true));
    }
}
