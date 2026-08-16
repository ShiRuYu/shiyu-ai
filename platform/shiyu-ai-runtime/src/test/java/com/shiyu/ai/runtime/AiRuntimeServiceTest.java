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
    }
}
