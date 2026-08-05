package com.shiyu.ai.agent.cache;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class AgentCacheManagerTest {

    @Test
    void resolvesGlobalRegistrationsForAuthenticatedUsersWithoutOverridingScopedEntries() {
        AgentCacheManager cache = new AgentCacheManager(mock(AgentAdminRepository.class), mock(AgentLoader.class));
        AgentDefinition global = AgentDefinition.builder().agentId("global-agent").name("Global").build();
        AgentDefinition scoped = AgentDefinition.builder().agentId("global-agent").name("Scoped").build();

        cache.put(global);
        assertSame(global, cache.get(42L, "global-agent"));

        cache.put(42L, "global-agent", scoped);
        assertSame(scoped, cache.get(42L, "global-agent"));
    }
}
