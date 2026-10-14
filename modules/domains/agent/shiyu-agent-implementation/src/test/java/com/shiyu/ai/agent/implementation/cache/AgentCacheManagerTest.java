package com.shiyu.ai.agent.implementation.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.implementation.port.repository.AgentAdminRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 验证 智能体 Cache Manager 相关功能、边界条件、异常路径和协作行为。
 */
class AgentCacheManagerTest {

    @Test
    void resolvesExplicitSystemRegistrationsWithoutOverridingTenantScopedEntries() {
        AgentCacheManager cache =
                new AgentCacheManager(mock(AgentAdminRepository.class), mock(AgentLoader.class));
        ActorContext actor = actor(7, 42);
        AgentDefinition global =
                AgentDefinition.builder().agentId("global-agent").name("System").build();
        AgentDefinition scoped =
                AgentDefinition.builder().agentId("global-agent").name("Scoped").build();

        cache.putSystem(global);
        assertSame(global, cache.get(actor, "global-agent"));

        cache.put(actor, "global-agent", scoped);
        assertSame(scoped, cache.get(actor, "global-agent"));
    }

    @Test
    void neverSharesTenantScopedEntriesAcrossTenants() {
        AgentCacheManager cache =
                new AgentCacheManager(mock(AgentAdminRepository.class), mock(AgentLoader.class));
        AgentDefinition scoped =
                AgentDefinition.builder().agentId("private-agent").name("Private").build();

        cache.put(actor(7, 42), "private-agent", scoped);

        assertNull(cache.get(actor(8, 42), "private-agent"));
    }

    @Test
    void tenantDatabaseDefinitionTakesPriorityOverSystemFallback() {
        AgentLoader loader = mock(AgentLoader.class);
        AgentCacheManager cache = new AgentCacheManager(
                mock(AgentAdminRepository.class), loader);
        ActorContext actor = actor(7, 42);
        AgentDefinition system = AgentDefinition.builder()
                .agentId("shared").name("System").build();
        AgentDefinition tenant = AgentDefinition.builder()
                .agentId("shared").name("Tenant").build();
        cache.putSystem(system);
        when(loader.loadFromDb(actor, "shared")).thenReturn(tenant);

        assertSame(tenant, cache.getOrLoad(actor, "shared", loader));
        assertSame(tenant, cache.getOrLoad(actor, "shared", loader));
    }

    @Test
    void systemFallbackIsRecheckedAfterTenantDefinitionChanges() {
        AgentLoader loader = mock(AgentLoader.class);
        AgentCacheManager cache = new AgentCacheManager(
                mock(AgentAdminRepository.class), loader);
        ActorContext actor = actor(7, 42);
        AgentDefinition system = AgentDefinition.builder()
                .agentId("shared").name("System").build();
        AgentDefinition tenant = AgentDefinition.builder()
                .agentId("shared").name("Tenant").build();
        cache.putSystem(system);
        when(loader.loadFromDb(actor, "shared")).thenReturn(null, tenant);

        assertSame(system, cache.getOrLoad(actor, "shared", loader));
        assertSame(system, cache.getOrLoad(actor, "shared", loader));
        verify(loader, times(1)).loadFromDb(actor, "shared");

        cache.evictTenant(actor.tenantId(), "shared");
        assertSame(tenant, cache.getOrLoad(actor, "shared", loader));
        verify(loader, times(2)).loadFromDb(actor, "shared");
    }

    @Test
    void invalidTenantDefinitionDoesNotSilentlyRunTheSystemAgent() {
        AgentAdminRepository repository = mock(AgentAdminRepository.class);
        AgentLoader loader = mock(AgentLoader.class);
        AgentCacheManager cache = new AgentCacheManager(repository, loader);
        ActorContext actor = actor(7, 42);
        AgentDefinition system = AgentDefinition.builder()
                .agentId("shared").name("System").build();
        var tenantRecord = new com.shiyu.ai.agent.implementation.domain.model.AgentDefBO();
        tenantRecord.setAgentId("shared");
        tenantRecord.setStatus(0);
        cache.putSystem(system);
        when(repository.selectByAgentId(actor.tenantId(), "shared"))
                .thenReturn(tenantRecord);

        assertNull(cache.getOrLoad(actor, "shared", loader));
        assertNull(cache.getOrLoad(actor, "shared", loader));
        verify(loader, times(1)).loadFromDb(actor, "shared");
    }

    @Test
    void tenantInvalidationRefreshesAllItsUsersWithoutRemovingOtherTenantsOrSystem() {
        AgentCacheManager cache = new AgentCacheManager(
                mock(AgentAdminRepository.class), mock(AgentLoader.class));
        AgentDefinition system = AgentDefinition.builder().agentId("shared").name("System").build();
        AgentDefinition tenantSeven = AgentDefinition.builder().agentId("shared")
                .name("Tenant seven").build();
        AgentDefinition tenantEight = AgentDefinition.builder().agentId("shared")
                .name("Tenant eight").build();
        cache.putSystem(system);
        cache.put(actor(7, 42), "shared", tenantSeven);
        cache.put(actor(7, 43), "shared", tenantSeven);
        cache.put(actor(8, 42), "shared", tenantEight);

        cache.evictTenant(new TenantId(7), "shared");

        assertSame(system, cache.get(actor(7, 42), "shared"));
        assertSame(system, cache.get(actor(7, 43), "shared"));
        assertSame(tenantEight, cache.get(actor(8, 42), "shared"));
    }

    @Test
    void loadsCachesAndEvictsTenantAndSystemEntries() {
        AgentAdminRepository repository = mock(AgentAdminRepository.class);
        AgentLoader loader = mock(AgentLoader.class);
        AgentCacheManager cache = new AgentCacheManager(repository, loader);
        ActorContext actor = actor(7, 42);
        AgentDefinition loaded = AgentDefinition.builder().agentId("loaded").name("Loaded").build();
        when(loader.loadFromDb(actor, "loaded")).thenReturn(loaded);

        assertSame(loaded, cache.getOrLoad(actor, "loaded", loader));
        assertSame(loaded, cache.getOrLoad(actor, "loaded", loader));
        cache.evict(actor, "loaded");
        assertNull(cache.get(actor, "loaded"));
        cache.putSystem(loaded);
        assertSame(loaded, cache.getOrLoad(actor, "loaded", loader));
        cache.evict("loaded");
        assertNull(cache.get(actor, "loaded"));
        assertEquals(0, cache.estimatedSize());
    }

    @Test
    void listsActiveDefinitionsUsingCacheAndLoaderAndSkipsMissing() {
        AgentAdminRepository repository = mock(AgentAdminRepository.class);
        AgentLoader loader = mock(AgentLoader.class);
        AgentCacheManager cache = new AgentCacheManager(repository, loader);
        ActorContext actor = actor(7, 42);
        var first = new com.shiyu.ai.agent.implementation.domain.model.AgentDefBO();
        first.setAgentId("first");
        var second = new com.shiyu.ai.agent.implementation.domain.model.AgentDefBO();
        second.setAgentId("second");
        when(repository.selectAllActive(any())).thenReturn(List.of(first, second));
        AgentDefinition cached = AgentDefinition.builder().agentId("first").build();
        AgentDefinition loaded = AgentDefinition.builder().agentId("second").build();
        cache.put(actor, "first", cached);
        when(loader.loadFromDb(eq(actor), eq("second"))).thenReturn(loaded);

        assertEquals(List.of(cached, loaded), cache.listAll(actor));
    }

    @Test
    void invalidationCannotBeOvertakenByAnEarlierDatabaseLoad() throws Exception {
        AgentLoader loader = mock(AgentLoader.class);
        AgentCacheManager cache = new AgentCacheManager(
                mock(AgentAdminRepository.class), loader);
        ActorContext actor = actor(7, 42);
        AgentDefinition oldDefinition = AgentDefinition.builder()
                .agentId("shared").name("Before update").build();
        CountDownLatch loadStarted = new CountDownLatch(1);
        CountDownLatch finishLoad = new CountDownLatch(1);
        CountDownLatch evictionStarted = new CountDownLatch(1);
        when(loader.loadFromDb(actor, "shared")).thenAnswer(invocation -> {
            loadStarted.countDown();
            if (!finishLoad.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("test load was never released");
            }
            return oldDefinition;
        });
        try (var workers = Executors.newFixedThreadPool(2)) {
            var loading = workers.submit(() -> cache.getOrLoad(actor, "shared", loader));
            assertTrue(loadStarted.await(5, TimeUnit.SECONDS));
            var eviction = workers.submit(() -> {
                evictionStarted.countDown();
                cache.evictTenant(actor.tenantId(), "shared");
            });
            assertTrue(evictionStarted.await(5, TimeUnit.SECONDS));
            try {
                eviction.get(200, TimeUnit.MILLISECONDS);
            } catch (java.util.concurrent.TimeoutException expected) {
                // Eviction waits for the in-flight load before clearing its cached result.
            } finally {
                finishLoad.countDown();
            }
            assertSame(oldDefinition, loading.get(5, TimeUnit.SECONDS));
            eviction.get(5, TimeUnit.SECONDS);
            assertFalse(cache.containsKey(actor, "shared"));
        }
    }

    private static ActorContext actor(long tenantId, long userId) {
        return new ActorContext(new TenantId(tenantId), new UserId(userId), false);
    }
}
