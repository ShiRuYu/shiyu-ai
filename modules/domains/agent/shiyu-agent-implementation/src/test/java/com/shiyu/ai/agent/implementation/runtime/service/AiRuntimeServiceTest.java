package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.model.AiApp;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppPreview;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppVersion;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;
import com.shiyu.ai.agent.implementation.domain.model.AgentDefBO;
import com.shiyu.ai.agent.implementation.domain.model.AgentVersionBO;
import com.shiyu.ai.agent.implementation.port.repository.AgentAdminRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiAppRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiRunRepository;
import com.shiyu.ai.agent.implementation.runtime.port.AiAppRepository;

import static org.junit.jupiter.api.Assertions.*;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 验证 AI Runtime Service 相关功能、边界条件、异常路径和协作行为。
 */
class AiRuntimeServiceTest {
    @Test
    void publishChecksTheTenantOwnedAgentVersionInsteadOfTrustingClientGraphStatus() {
        AgentAdminRepository agents = mock(AgentAdminRepository.class);
        AiRuntimeService service = new AiRuntimeService(
                new InMemoryAiRunRepository(), new InMemoryAiAppRepository(), agents);
        TenantId tenant = new TenantId(1);
        AiApp app = service.createApp(tenant, 2, "Agent", "demo");
        AiAppVersion missing = service.createVersion(app.id(), tenant, 2, "missing",
                "{\"executionType\":\"AGENT\",\"agentId\":\"agent-1\",\"agentVersion\":\"v9\",\"validation\":{\"graph\":\"PASS\"}}");
        assertThrows(IllegalArgumentException.class,
                () -> service.publish(app.id(), missing.id(), tenant, 2));

        AgentDefBO definition = new AgentDefBO();
        definition.setStatus(1);
        definition.setCurrentVersion("v1");
        when(agents.selectByAgentId(tenant, "agent-1")).thenReturn(definition);
        assertThrows(IllegalArgumentException.class,
                () -> service.publish(app.id(), missing.id(), tenant, 2));

        AgentVersionBO version = new AgentVersionBO();
        version.setStatus(1);
        version.setGraphConfig("{\"nodes\":{}}");
        when(agents.selectVersionByAgentIdAndNumber(tenant, "agent-1", "v9"))
                .thenReturn(version);
        assertThrows(IllegalArgumentException.class,
                () -> service.publish(app.id(), missing.id(), tenant, 2));
        version.setGraphConfig("{\"startNode\":\"n\",\"endNode\":\"n\",\"nodes\":{\"n\":{}}}");
        assertEquals("PUBLISHED", service.publish(app.id(), missing.id(), tenant, 2).status());
        verify(agents, times(4)).selectByAgentId(tenant, "agent-1");
    }

    @Test
    void publishRequiresAnActiveCurrentAgentVersionWithGraph() {
        AgentAdminRepository agents = mock(AgentAdminRepository.class);
        AiRuntimeService service = new AiRuntimeService(
                new InMemoryAiRunRepository(), new InMemoryAiAppRepository(), agents);
        TenantId tenant = new TenantId(1);
        AiApp app = service.createApp(tenant, 2, "Agent", "demo");
        AiAppVersion bound = service.createVersion(app.id(), tenant, 2, "bound",
                "{\"agentId\":\"agent-1\",\"validation\":{\"graph\":\"PASS\"}}");
        AgentDefBO definition = new AgentDefBO();
        definition.setStatus(0);
        when(agents.selectByAgentId(tenant, "agent-1")).thenReturn(definition);
        assertThrows(IllegalArgumentException.class,
                () -> service.publish(app.id(), bound.id(), tenant, 2));

        definition.setStatus(1);
        assertThrows(IllegalArgumentException.class,
                () -> service.publish(app.id(), bound.id(), tenant, 2));

        definition.setCurrentVersion("v1");
        AgentVersionBO version = new AgentVersionBO();
        version.setStatus(0);
        when(agents.selectVersionByAgentIdAndNumber(tenant, "agent-1", "v1"))
                .thenReturn(version);
        assertThrows(IllegalArgumentException.class,
                () -> service.publish(app.id(), bound.id(), tenant, 2));

        version.setStatus(1);
        version.setGraphConfig("{\"nodes\":{}}");
        assertThrows(IllegalArgumentException.class,
                () -> service.publish(app.id(), bound.id(), tenant, 2));
        version.setGraphConfig("{\"startNode\":\"n\",\"endNode\":\"n\",\"nodes\":{\"n\":{}}}");
        assertEquals("PUBLISHED", service.publish(app.id(), bound.id(), tenant, 2).status());
    }
    @Test
    void runtimeCommandsRequireTypedTenantIdentity() {
        AiRuntimeService service = new AiRuntimeService();
        assertEquals(java.util.List.of(), service.listRuns(new TenantId(1), 2, 10));
        assertThrows(NullPointerException.class, () -> service.listRuns(null, 2, 10));
    }

    @Test
    void runContextRequiresTypedTenantIdentity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AiRunContext(null, 2, null, null, null, null, null, null, Map.of()));
        assertEquals(
                new TenantId(7),
                new AiRunContext(new TenantId(7), 2, null, null, null, null, null, null, Map.of())
                        .tenantId());
    }

    @Test
    void appVersionsArePublishedBeforeExecution() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(new TenantId(1), 2, "Tutor", "demo");
        AiAppVersion version = service.createVersion(app.id(), new TenantId(1), 2, "1.0.0", "{}");
        AiRunContext context =
                new AiRunContext(
                        new TenantId(1),
                        2,
                        app.id(),
                        version.id(),
                        "conversation",
                        "generation",
                        null,
                        "trace",
                        Map.of());
        assertThrows(
                IllegalStateException.class,
                () ->
                        service.startRun(
                                context,
                                AiRunSource.CONVERSATION,
                                "generation",
                                "model",
                                "prompt"));

        service.publish(app.id(), version.id(), new TenantId(1), 2);
        AiRun run =
                service.startRun(
                        context, AiRunSource.CONVERSATION, "generation", "model", "prompt");
        service.append(run, AiRunEventType.MODEL_DELTA, "{\"text\":\"hi\"}", true);
        service.finish(run.id(), new TenantId(1), 2, AiRunStatus.COMPLETED, null);

        assertEquals(3, service.events(run.id(), new TenantId(1), 2, 0, 20).size());
        assertEquals(
                AiRunEventType.RUN_STARTED,
                service.events(run.id(), new TenantId(1), 2, 0, 20).get(0).type());
        assertEquals(
                AiRunEventType.RUN_COMPLETED,
                service.events(run.id(), new TenantId(1), 2, 0, 20).get(2).type());
        assertThrows(
                IllegalStateException.class,
                () -> service.finish(
                        run.id(), new TenantId(1), 2, AiRunStatus.CANCELLED, "late_cancel"));
    }

    @Test
    void versionWithoutItsAppCannotStartOrCreateAnOrphanRun() {
        AiRuntimeService service = new AiRuntimeService();
        TenantId tenant = new TenantId(1);
        AiApp app = service.createApp(tenant, 2, "Tutor", "demo");
        AiAppVersion version = service.createVersion(app.id(), tenant, 2, "v1", "{}");
        service.publish(app.id(), version.id(), tenant, 2);
        AiRunContext detachedVersion = new AiRunContext(tenant, 2, null, version.id(),
                "conversation", null, null, null, Map.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.startRun(detachedVersion, AiRunSource.CONVERSATION,
                        "conversation", "model", "prompt"));
        assertTrue(service.listRuns(tenant, 2, 10).isEmpty());
    }

    @Test
    void startedEventFailureClosesTheAdmittedRun() {
        AtomicBoolean failStartedEvent = new AtomicBoolean(true);
        InMemoryAiRunRepository runs = new InMemoryAiRunRepository() {
            @Override
            public long appendNextEvent(String runId, TenantId tenantId, long ownerUserId,
                    AiRunEventType type, String payload, boolean redacted, Instant createdAt,
                    String turnId, String stepId, String providerRequestId) {
                if (type == AiRunEventType.RUN_STARTED && failStartedEvent.getAndSet(false)) {
                    throw new IllegalStateException("event store unavailable");
                }
                return super.appendNextEvent(runId, tenantId, ownerUserId, type, payload,
                        redacted, createdAt, turnId, stepId, providerRequestId);
            }
        };
        AiRuntimeService service = new AiRuntimeService(runs, new InMemoryAiAppRepository());
        TenantId tenant = new TenantId(1);
        AiRunContext context = new AiRunContext(tenant, 2, null, null,
                null, null, null, null, Map.of());

        assertThrows(IllegalStateException.class,
                () -> service.startRun(context, AiRunSource.API, "request", "model", "prompt"));

        AiRun admitted = service.listRuns(tenant, 2, 10).getFirst();
        assertEquals(AiRunStatus.FAILED, admitted.status());
        assertEquals("RUN_EVENT_WRITE_FAILED", admitted.errorCode());
        assertEquals(AiRunEventType.RUN_FAILED,
                service.events(admitted.id(), tenant, 2, 0, 10).getLast().type());
    }

    @Test
    void supersededPublishedAppVersionCannotStartANewRun() {
        AiRuntimeService service = new AiRuntimeService();
        TenantId tenant = new TenantId(1);
        AiApp app = service.createApp(tenant, 2, "Tutor", "demo");
        AiAppVersion first = service.createVersion(app.id(), tenant, 2, "v1", "{}");
        service.publish(app.id(), first.id(), tenant, 2);
        AiAppVersion second = service.createVersion(app.id(), tenant, 2, "v2", "{}");
        service.publish(app.id(), second.id(), tenant, 2);
        AiRunContext stale = new AiRunContext(tenant, 2, app.id(), first.id(),
                "conversation", null, null, null, Map.of());

        assertThrows(IllegalStateException.class,
                () -> service.startRun(stale, AiRunSource.CONVERSATION,
                        "conversation", "model", "prompt"));
        assertTrue(service.listRuns(tenant, 2, 10).isEmpty());
    }

    @Test
    void publishConflictDoesNotClaimTheDraftWasPublished() {
        AiAppRepository apps = mock(AiAppRepository.class);
        TenantId tenant = new TenantId(1);
        Instant now = Instant.now();
        AiApp app = new AiApp("app", tenant, new UserId(2), "Tutor", "demo",
                "ACTIVE", null, now, now);
        AiAppVersion draft = new AiAppVersion("version", "app", tenant, "v1",
                "{}", "DRAFT", now, null);
        when(apps.find("app", tenant, 2)).thenReturn(Optional.of(app));
        when(apps.findVersion("app", "version", tenant)).thenReturn(Optional.of(draft));
        AiRuntimeService service = new AiRuntimeService(new InMemoryAiRunRepository(), apps);

        assertThrows(IllegalStateException.class,
                () -> service.publish("app", "version", tenant, 2));

        verify(apps).publishVersion("app", "version", tenant);
        assertEquals("DRAFT", draft.status());
    }

    @Test
    void publishedPointerMustResolveToAnActuallyPublishedVersion() {
        AiAppRepository apps = mock(AiAppRepository.class);
        TenantId tenant = new TenantId(1);
        Instant now = Instant.now();
        AiApp app = new AiApp("app", tenant, new UserId(2), "Tutor", "demo",
                "ACTIVE", "version", now, now);
        when(apps.find("app", tenant, 2)).thenReturn(Optional.of(app));
        AiRuntimeService service = new AiRuntimeService(new InMemoryAiRunRepository(), apps);

        assertThrows(IllegalStateException.class,
                () -> service.requirePublishedVersion("app", tenant, 2));
        AiAppVersion stale = new AiAppVersion("version", "app", tenant, "v1",
                "{}", "DRAFT", now, null);
        when(apps.findVersion("app", "version", tenant)).thenReturn(Optional.of(stale));
        assertThrows(IllegalStateException.class,
                () -> service.requirePublishedVersion("app", tenant, 2));
        when(apps.findVersion("app", "version", tenant))
                .thenReturn(Optional.of(new AiAppVersion("version", "app", tenant, "v1",
                        "{}", "PUBLISHED", now, now)));
        assertEquals("version", service.requirePublishedVersion("app", tenant, 2).id());
    }

    @Test
    void concurrentEventAppendUsesEachSequenceOnce() throws Exception {
        AiRuntimeService service = new AiRuntimeService();
        AiRun run =
                service.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, "c", "g", null, null, Map.of()),
                        AiRunSource.CONVERSATION,
                        "g",
                        "model",
                        "prompt");
        var pool = Executors.newFixedThreadPool(8);
        CountDownLatch ready = new CountDownLatch(8);
        CountDownLatch go = new CountDownLatch(1);
        for (int i = 0; i < 8; i++) {
            pool.submit(
                    () -> {
                        ready.countDown();
                        go.await();
                        service.append(run, AiRunEventType.MODEL_DELTA, "{}", true);
                        return null;
                    });
        }
        ready.await();
        go.countDown();
        pool.shutdown();
        while (!pool.isTerminated()) Thread.yield();
        var events = service.events(run.id(), new TenantId(1), 2, 0, 30);
        assertEquals(9, events.size());
        assertEquals(
                java.util.stream.LongStream.rangeClosed(1, 9).boxed().toList(),
                events.stream().map(AiRunEvent::seq).toList());
    }

    @Test
    void runtimeEventCarriesReplayAndProviderContext() {
        AiRuntimeService service = new AiRuntimeService();
        AiRun run =
                service.startRun(
                        new AiRunContext(
                                new TenantId(7), 8, null, null, "c", "g", "e", "trace-1", Map.of()),
                        AiRunSource.CONVERSATION,
                        "g",
                        "deepseek-v4-flash",
                        "prompt");
        service.append(
                run,
                AiRunEventType.MODEL_STARTED,
                "{}",
                true,
                "turn-1",
                "step-1",
                "provider-req-1");
        AiRunEvent event = service.events(run.id(), new TenantId(7), 8, 1, 1).get(0);
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
        AiApp app = service.createApp(new TenantId(1), 2, "Tutor", "demo");
        AiAppVersion version =
                service.createVersion(
                        app.id(),
                        new TenantId(1),
                        2,
                        "1.0.0",
                        "{\"model\":\"configured\",\"temperature\":0.2}");
        AiAppPreview draft = service.preview(app.id(), version.id(), new TenantId(1), 2, "hello");
        assertFalse(draft.executable());
        service.publish(app.id(), version.id(), new TenantId(1), 2);
        AiAppPreview published =
                service.preview(app.id(), version.id(), new TenantId(1), 2, "hello");
        assertTrue(published.executable());
        assertEquals("configured", published.model());
        assertEquals("0.2", String.valueOf(published.configuration().get("temperature")));
    }

    @Test
    void publishRejectsMalformedConfigurationAndPublishedVersionCannotBeArchived() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(new TenantId(1), 2, "Tutor", "demo");
        AiAppVersion invalid = service.createVersion(app.id(), new TenantId(1), 2, "bad", "[]");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.publish(app.id(), invalid.id(), new TenantId(1), 2));
        AiAppVersion valid = service.createVersion(app.id(), new TenantId(1), 2, "good", "{}");
        service.publish(app.id(), valid.id(), new TenantId(1), 2);
        assertThrows(
                IllegalStateException.class,
                () -> service.archive(app.id(), valid.id(), new TenantId(1), 2));
        assertThrows(
                IllegalStateException.class,
                () -> service.publish(app.id(), valid.id(), new TenantId(1), 2));
    }

    @Test
    void publishEnforcesDeclaredValidationAndEvaluationGates() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(new TenantId(1), 2, "Tutor", "demo");
        AiAppVersion failed =
                service.createVersion(
                        app.id(),
                        new TenantId(1),
                        2,
                        "failed",
                        "{\"validation\":{\"tools\":\"FAILED\"}}");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.publish(app.id(), failed.id(), new TenantId(1), 2));
        AiAppVersion below =
                service.createVersion(
                        app.id(),
                        new TenantId(1),
                        2,
                        "below",
                        "{\"evaluation\":{\"requiredPassRate\":0.9,\"passRate\":0.75}}");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.publish(app.id(), below.id(), new TenantId(1), 2));
        AiAppVersion passed =
                service.createVersion(
                        app.id(),
                        new TenantId(1),
                        2,
                        "passed",
                        "{\"validation\":{\"graph\":\"PASS\",\"model\":\"PASS\",\"knowledge\":\"PASS\",\"tools\":\"PASS\",\"budget\":\"PASS\"},\"evaluation\":{\"requiredPassRate\":0.9,\"passRate\":0.95}}");
        assertEquals(
                "PUBLISHED", service.publish(app.id(), passed.id(), new TenantId(1), 2).status());
    }

    @Test
    void publishRejectsNonNumericBudgetLimits() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(new TenantId(1), 2, "Tutor", "demo");
        for (String limit : List.of("\"maxTokens\":\"unlimited\"", "\"maxCost\":\"free\"")) {
            AiAppVersion invalid = service.createVersion(
                    app.id(), new TenantId(1), 2, "invalid-budget",
                    "{\"budget\":{" + limit + "}}");
            assertThrows(IllegalArgumentException.class,
                    () -> service.publish(app.id(), invalid.id(), new TenantId(1), 2));
        }
    }

    @Test
    void publishRejectsBudgetLimitsOutsideFiniteRange() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(new TenantId(1), 2, "Tutor", "demo");
        for (String key : List.of("maxTokens", "maxCost")) {
            AiAppVersion invalid = service.createVersion(
                    app.id(), new TenantId(1), 2, "unbounded-budget-" + key,
                    "{\"budget\":{\"" + key + "\":1e400}}");
            assertThrows(IllegalArgumentException.class,
                    () -> service.publish(app.id(), invalid.id(), new TenantId(1), 2));
        }
    }

    @Test
    void publishFailsClosedForDeclaredExecutableBindings() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(new TenantId(1), 2, "Agent", "demo");
        AiAppVersion missingAgent =
                service.createVersion(
                        app.id(),
                        new TenantId(1),
                        2,
                        "missing-agent",
                        "{\"executionType\":\"AGENT\"}");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.publish(app.id(), missingAgent.id(), new TenantId(1), 2));
        AiAppVersion missingGate =
                service.createVersion(
                        app.id(),
                        new TenantId(1),
                        2,
                        "missing-gate",
                        "{\"agentId\":\"agent-1\",\"validation\":{\"model\":\"PASS\"}}");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.publish(app.id(), missingGate.id(), new TenantId(1), 2));
        AiAppVersion ready =
                service.createVersion(
                        app.id(),
                        new TenantId(1),
                        2,
                        "ready",
                        "{\"executionType\":\"AGENT\",\"agentId\":\"agent-1\",\"model\":\"deepseek-v4-flash\",\"validation\":{\"graph\":\"PASS\",\"model\":\"PASS\"}}");
        assertEquals(
                "PUBLISHED", service.publish(app.id(), ready.id(), new TenantId(1), 2).status());
    }

    @Test
    void linksAnOpenAiRunToTheStoredGenerationExactlyOnce() {
        AiRuntimeService service = new AiRuntimeService();
        AiRun run =
                service.startRun(
                        new AiRunContext(
                                new TenantId(1),
                                2,
                                null,
                                null,
                                "conversation",
                                null,
                                null,
                                null,
                                Map.of()),
                        AiRunSource.API,
                        "openai",
                        "deepseek-v4-flash",
                        "prompt-hash-input");

        AiRun linked = service.linkGeneration(run, "generation-1");

        assertEquals("generation-1", linked.generationId());
        assertEquals(
                linked.id(), service.requireGenerationRun("generation-1", new TenantId(1), 2).id());
        assertSame(linked, service.linkGeneration(linked, "generation-1"));
        assertThrows(
                IllegalStateException.class, () -> service.linkGeneration(linked, "generation-2"));
    }

    @Test
    void refusesGenerationLinkWhenConcurrentUpdateWins() {
        AiRunRepository runs = spy(new InMemoryAiRunRepository());
        AiRuntimeService service = new AiRuntimeService(runs, new InMemoryAiAppRepository());
        AiRun run = service.startRun(
                new AiRunContext(new TenantId(1), 2, null, null,
                        "conversation", null, null, null, Map.of()),
                AiRunSource.API, "openai", "deepseek-flash", "prompt");
        doReturn(0).when(runs).linkGeneration(
                run.id(), run.tenantId(), run.ownerUserId().value(), "generation-1");

        assertThrows(IllegalStateException.class,
                () -> service.linkGeneration(run, "generation-1"));
        assertNull(service.requireRun(run.id(), run.tenantId(), run.ownerUserId().value())
                .generationId());
        assertThrows(IllegalArgumentException.class, () -> service.linkGeneration(run, null));
        assertThrows(IllegalArgumentException.class, () -> service.linkGeneration(run, " "));
    }

    @Test
    void terminalEventIsIdempotentAndCannotChangeMeaning() {
        AiRuntimeService service = new AiRuntimeService();
        AiRun run =
                service.startRun(
                        new AiRunContext(
                                new TenantId(1),
                                2,
                                null,
                                null,
                                "conversation",
                                null,
                                null,
                                null,
                                Map.of()),
                        AiRunSource.CONVERSATION,
                        "conversation-1",
                        "deepseek-v4-flash",
                        "prompt");
        service.append(run, AiRunEventType.RUN_COMPLETED, "{}", true);
        service.append(run, AiRunEventType.RUN_COMPLETED, "{}", true);
        assertEquals(2, service.events(run.id(), new TenantId(1), 2, 0, 20).size());
        assertThrows(
                IllegalStateException.class,
                () -> service.append(run, AiRunEventType.RUN_FAILED, "{}", true));
    }

    @Test
    void emitsStableFailedAndCancelledTerminalEvents() {
        AiRuntimeService service = new AiRuntimeService();
        AiRun failed =
                service.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, "c", null, null, null, Map.of()),
                        AiRunSource.API,
                        "failed",
                        "model",
                        "prompt");
        AiRun failedResult =
                service.finish(failed.id(), new TenantId(1), 2, AiRunStatus.FAILED, "MODEL_FAILED");
        assertEquals(AiRunStatus.FAILED, failedResult.status());
        assertEquals(
                AiRunEventType.RUN_FAILED,
                service.events(failed.id(), new TenantId(1), 2, 0, 10).getLast().type());

        AiRun cancelled =
                service.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, "c", null, null, null, Map.of()),
                        AiRunSource.API,
                        "cancelled",
                        "model",
                        "prompt");
        AiRun cancelledResult =
                service.finish(cancelled.id(), new TenantId(1), 2, AiRunStatus.CANCELLED, null);
        assertEquals(AiRunStatus.CANCELLED, cancelledResult.status());
        assertEquals(
                "{}",
                service.events(cancelled.id(), new TenantId(1), 2, 0, 10).getLast().payload());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.finish(
                                cancelled.id(), new TenantId(1), 2, AiRunStatus.RUNNING, null));
    }

    @Test
    void rejectsInvalidPublishShapesAndProtectsRunUsageBoundaries() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(new TenantId(1), 2, "Tutor", "demo");
        String[] invalid = {
            "{\"modelRoute\":[]}",
            "{\"knowledgeSpaces\":[\"\"]}",
            "{\"tools\":\"bad\"}",
            "{\"agentId\":\" \"}",
            "{\"model\":\" \"}",
            "{\"budget\":{\"maxTokens\":-1}}",
            "{\"validation\":[]}",
            "{\"evaluation\":{\"requiredPassRate\":2}}",
            "{\"evaluation\":{\"requiredPassRate\":0.8}}"
        };
        for (int i = 0; i < invalid.length; i++) {
            AiAppVersion version =
                    service.createVersion(app.id(), new TenantId(1), 2, "invalid-" + i, invalid[i]);
            assertThrows(
                    IllegalArgumentException.class,
                    () -> service.publish(app.id(), version.id(), new TenantId(1), 2));
        }
        AiRun run =
                service.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, "c", null, null, null, Map.of()),
                        AiRunSource.API,
                        "api",
                        "model",
                        "prompt");
        assertEquals(
                0,
                service.recordUsage(run.id(), new TenantId(1), 2, -4, -2, false, "cost")
                        .promptTokens());
        service.finish(run.id(), new TenantId(1), 2, AiRunStatus.COMPLETED, null);
        assertThrows(
                IllegalStateException.class,
                () -> service.recordUsage(run.id(), new TenantId(1), 2, 1, 1, false, null));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.startRun(
                                new AiRunContext(
                                        new TenantId(1),
                                        2,
                                        "app",
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        Map.of()),
                                AiRunSource.API,
                                "api",
                                "m",
                                "p"));
        assertThrows(IllegalStateException.class, () -> service.append(run, null, null, true));
    }

    @Test
    void failedAndCancelledRunsCannotAcquireUsageAfterTheirTerminalEvent() {
        AiRuntimeService service = new AiRuntimeService();
        for (AiRunStatus terminal : new AiRunStatus[] {
                AiRunStatus.FAILED, AiRunStatus.CANCELLED}) {
            AiRun run = service.startRun(
                    new AiRunContext(new TenantId(1), 2, null, null, null,
                            null, null, null, Map.of()),
                    AiRunSource.API, "request", "model", "prompt");
            service.append(run, AiRunEventType.MODEL_STARTED, null, true);
            assertEquals("{}", service.events(run.id(), new TenantId(1), 2, 0, 10)
                    .getLast().payload());
            service.finish(run.id(), new TenantId(1), 2, terminal, "TERMINAL");

            assertThrows(IllegalStateException.class,
                    () -> service.recordUsage(run.id(), new TenantId(1), 2,
                            10, 5, false, "cost"));
        }
    }

    @Test
    void handlesMissingAppsVersionsAndGenerationExecutionLookups() {
        AiRuntimeService service = new AiRuntimeService();
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requireApp("missing", new TenantId(1), 2));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requireRun("missing", new TenantId(1), 2));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requireGenerationRun("missing", new TenantId(1), 2));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requireExecutionRun("missing", new TenantId(1), 2));
        assertThrows(IllegalArgumentException.class, () -> service.linkGeneration(null, "g"));
        AiApp app = service.createApp(new TenantId(1), 2, "Tutor", "demo");
        AiAppVersion draft = service.createVersion(app.id(), new TenantId(1), 2, "1", "{");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.preview(app.id(), draft.id(), new TenantId(1), 2, "p"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.archive(app.id(), "missing", new TenantId(1), 2));
    }

    @Test
    void listsVersionsArchivesDraftsAndRequiresPublishedVersion() {
        AiRuntimeService service = new AiRuntimeService();
        AiApp app = service.createApp(new TenantId(1), 2, "Tutor", "demo");
        AiAppVersion draft = service.createVersion(app.id(), new TenantId(1), 2, "draft", "{}");
        assertEquals(1, service.listApps(new TenantId(1), 2, 10).size());
        assertEquals(1, service.versions(app.id(), new TenantId(1), 2).size());
        assertThrows(
                IllegalStateException.class,
                () -> service.requirePublishedVersion(app.id(), new TenantId(1), 2));
        assertEquals(
                "ARCHIVED", service.archive(app.id(), draft.id(), new TenantId(1), 2).status());
        assertThrows(
                IllegalStateException.class,
                () -> service.publish(app.id(), draft.id(), new TenantId(1), 2));
    }

    @Test
    void coversRuntimeConfigurationBoundaryHelpers() throws Exception {
        AiRuntimeService service = new AiRuntimeService();
        invoke(service, "validateConfig", (Object) null);
        invoke(service, "validateConfig", "{}");
        invoke(service, "validateConfig", "{\"evaluation\":{\"passRate\":0.8}}");
        invoke(
                service,
                "validateConfig",
                "{\"modelRoute\":[\"route\"],\"knowledgeSpaces\":[\"space\"],\"tools\":[\"tool\"],\"budget\":{\"maxTokens\":10,\"maxCost\":0},\"validation\":{\"graph\":\"PASS\",\"model\":\"PASS\",\"knowledge\":\"PASS\",\"tools\":\"PASS\",\"budget\":\"PASS\"},\"evaluation\":{\"requiredPassRate\":0.8,\"passRate\":0.9}}");
        for (String json :
                new String[] {
                    "{\"modelRoute\":[null]}",
                    "{\"knowledgeSpaces\":[]}",
                    "{\"tools\":[\" \" ]}",
                    "{\"executionType\":\"AGENT\",\"agentId\":\" \"}",
                    "{\"agentId\":\"agent-1\",\"validation\":{\"graph\":\"FAILED\"}}",
                    "{\"model\":\"\",\"validation\":{\"model\":\"PASS\"}}",
                    "{\"knowledgeSpaces\":[\"space\"],\"validation\":{\"knowledge\":\"FAILED\"}}",
                    "{\"tools\":[\"tool\"],\"validation\":{\"tools\":\"FAILED\"}}",
                    "{\"budget\":[]}",
                    "{\"budget\":{\"maxTokens\":-1}}",
                    "{\"validation\":[]}",
                    "{\"validation\":{\"graph\":\"FAILED\"}}",
                    "{\"evaluation\":[]}",
                    "{\"evaluation\":{\"requiredPassRate\":-0.1}}",
                    "{\"evaluation\":{\"requiredPassRate\":\"0.8\",\"passRate\":0.9}}",
                    "{\"evaluation\":{\"requiredPassRate\":1.1}}",
                    "{\"evaluation\":{\"requiredPassRate\":0.8}}",
                    "{\"evaluation\":{\"requiredPassRate\":0.8,\"passRate\":0.7}}"
                }) {
            assertThrows(
                    IllegalArgumentException.class, () -> invoke(service, "validateConfig", json));
        }
        assertTrue((Boolean) invoke(service, "isPass", "pass"));
        assertFalse((Boolean) invoke(service, "isPass", "failed"));
        assertFalse((Boolean) invoke(service, "isPass", (Object) null));
        assertEquals(64, ((String) invoke(service, "hash", "hello")).length());
    }

    private static Object invoke(AiRuntimeService service, String name, Object... args)
            throws Exception {
        for (var method : AiRuntimeService.class.getDeclaredMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length)
                continue;
            method.setAccessible(true);
            try {
                return method.invoke(service, args);
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof Exception checked) throw checked;
                if (cause instanceof Error error) throw error;
                throw exception;
            }
        }
        throw new NoSuchMethodException(name);
    }
}
