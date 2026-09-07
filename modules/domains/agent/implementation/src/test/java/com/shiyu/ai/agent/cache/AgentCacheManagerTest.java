package com.shiyu.ai.agent.cache;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentCacheManagerTest {

    @Test
    void resolvesExplicitSystemRegistrationsWithoutOverridingTenantScopedEntries() {
        AgentCacheManager cache = new AgentCacheManager(mock(AgentAdminRepository.class), mock(AgentLoader.class));
        ActorContext actor = actor(7, 42);
        AgentDefinition global = AgentDefinition.builder().agentId("global-agent").name("System").build();
        AgentDefinition scoped = AgentDefinition.builder().agentId("global-agent").name("Scoped").build();

        cache.putSystem(global);
        assertSame(global, cache.get(actor, "global-agent"));

        cache.put(actor, "global-agent", scoped);
        assertSame(scoped, cache.get(actor, "global-agent"));
    }

    @Test
    void neverSharesTenantScopedEntriesAcrossTenants() {
        AgentCacheManager cache = new AgentCacheManager(mock(AgentAdminRepository.class), mock(AgentLoader.class));
        AgentDefinition scoped = AgentDefinition.builder().agentId("private-agent").name("Private").build();

        cache.put(actor(7, 42), "private-agent", scoped);

        assertNull(cache.get(actor(8, 42), "private-agent"));
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
        var first = new com.shiyu.ai.agent.domain.model.AgentDefBO();
        first.setAgentId("first");
        var second = new com.shiyu.ai.agent.domain.model.AgentDefBO();
        second.setAgentId("second");
        when(repository.selectAllActive(any())).thenReturn(List.of(first, second));
        AgentDefinition cached = AgentDefinition.builder().agentId("first").build();
        AgentDefinition loaded = AgentDefinition.builder().agentId("second").build();
        cache.put(actor, "first", cached);
        when(loader.loadFromDb(eq(actor), eq("second"))).thenReturn(loaded);

        assertEquals(List.of(cached, loaded), cache.listAll(actor));
    }

    private static ActorContext actor(long tenantId, long userId) {
        return new ActorContext(new TenantId(tenantId), new UserId(userId), false);
    }
}
