package com.shiyu.ai.agent.implementation.runtime.adapter.inmemory;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiAppRepository;
import com.shiyu.ai.agent.implementation.runtime.model.AiApp;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppVersion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;

import java.time.Instant;

class InMemoryAiAppRepositoryTest {
    private static final TenantId TENANT = new TenantId(1);
    private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

    @Test
    void publishingNewVersionArchivesPreviousPublishedVersion() {
        InMemoryAiAppRepository repository = new InMemoryAiAppRepository();
        repository.insert(
                new AiApp("app", TENANT, new UserId(2), "Tutor", "desc", "ACTIVE", null, NOW, NOW));
        repository.insertVersion(
                new AiAppVersion("v1", "app", TENANT, "1", "{}", "DRAFT", NOW, null));
        repository.insertVersion(
                new AiAppVersion(
                        "v2", "app", TENANT, "2", "{}", "DRAFT", NOW.plusSeconds(1), null));

        assertEquals(1, repository.publishVersion("app", "v1", TENANT));
        assertEquals(1, repository.publishVersion("app", "v2", TENANT));

        assertEquals(
                "ARCHIVED", repository.findVersion("app", "v1", TENANT).orElseThrow().status());
        assertEquals(
                "PUBLISHED", repository.findVersion("app", "v2", TENANT).orElseThrow().status());
        assertEquals(
                "v2", repository.findByTenant("app", TENANT).orElseThrow().publishedVersionId());
    }

    @Test
    void archiveOnlyTransitionsDraftVersions() {
        InMemoryAiAppRepository repository = new InMemoryAiAppRepository();
        repository.insert(
                new AiApp("app", TENANT, new UserId(2), "Tutor", "desc", "ACTIVE", null, NOW, NOW));
        repository.insertVersion(
                new AiAppVersion("published", "app", TENANT, "1", "{}", "PUBLISHED", NOW, NOW));
        repository.insertVersion(
                new AiAppVersion(
                        "draft", "app", TENANT, "2", "{}", "DRAFT", NOW.plusSeconds(1), null));

        assertEquals(0, repository.archiveVersion("app", "published", TENANT));
        assertEquals(
                "PUBLISHED",
                repository.findVersion("app", "published", TENANT).orElseThrow().status());
        assertEquals(1, repository.archiveVersion("app", "draft", TENANT));
        assertEquals(0, repository.archiveVersion("app", "draft", TENANT));
        assertEquals(
                "ARCHIVED", repository.findVersion("app", "draft", TENANT).orElseThrow().status());
    }

    @Test
    void ordersAppsAndVersionsByIdWhenTimestampsTie() {
        InMemoryAiAppRepository repository = new InMemoryAiAppRepository();
        repository.insert(
                new AiApp("b", TENANT, new UserId(2), "B", "desc", "ACTIVE", null, NOW, NOW));
        repository.insert(
                new AiApp("a", TENANT, new UserId(2), "A", "desc", "ACTIVE", null, NOW, NOW));
        repository.insertVersion(
                new AiAppVersion("v2", "b", TENANT, "2", "{}", "DRAFT", NOW, null));
        repository.insertVersion(
                new AiAppVersion("v1", "b", TENANT, "1", "{}", "DRAFT", NOW, null));

        assertEquals("a", repository.list(TENANT, 2, 1).getFirst().id());
        assertEquals("v1", repository.versions("b", TENANT).getFirst().id());
    }
}
