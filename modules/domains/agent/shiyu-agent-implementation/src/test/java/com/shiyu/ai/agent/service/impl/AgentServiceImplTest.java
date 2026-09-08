package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.cache.AgentLoader;
import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgentServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(4), new UserId(8), false);
    private final AgentCacheManager cache = mock(AgentCacheManager.class);
    private final AgentLoader loader = mock(AgentLoader.class);
    private final AgentAdminRepository repository = mock(AgentAdminRepository.class);
    private final AgentServiceImpl service = new AgentServiceImpl(cache, loader, repository);

    @Test
    void registersLoadsListsAndUnregistersInTenantScope() {
        AgentDefinition definition = AgentDefinition.builder().agentId("math").name("Math").build();
        service.registerAgent(ACTOR, definition);
        verify(cache).put(ACTOR, "math", definition);
        when(cache.get(ACTOR, "math")).thenReturn(definition);
        assertSame(definition, service.getAgent(ACTOR, "math"));
        when(cache.listAll(ACTOR)).thenReturn(List.of(definition));
        assertEquals(List.of(definition), service.listAgents(ACTOR));
        assertTrue(service.unregisterAgent(ACTOR, "math"));
        verify(repository).deleteByAgentId(ACTOR.tenantId(), "math");
        verify(cache).evict(ACTOR, "math");
    }

    @Test
    void loadsMissingAgentAndSwitchesOnlyExistingVersion() {
        AgentVersion version = AgentVersion.builder().versionNumber("v2").build();
        AgentDefinition definition = AgentDefinition.builder().agentId("math").currentVersion("v1").versions(Map.of("v2", version)).build();
        when(cache.get(ACTOR, "math")).thenReturn(null);
        when(cache.getOrLoad(ACTOR, "math", loader)).thenReturn(definition);
        assertSame(definition, service.getAgent(ACTOR, "math"));
        assertTrue(service.switchVersion(ACTOR, "math", "v2"));
        assertEquals("v2", definition.getCurrentVersion());
        assertFalse(service.switchVersion(ACTOR, "math", "missing"));
        when(cache.getOrLoad(ACTOR, "missing", loader)).thenReturn(null);
        assertFalse(service.switchVersion(ACTOR, "missing", "v1"));
    }

    @Test
    void rejectsInvalidDefinitionsAndAgentIds() {
        assertThrows(IllegalArgumentException.class, () -> service.registerAgent(ACTOR, null));
        assertThrows(IllegalArgumentException.class, () -> service.registerAgent(ACTOR, AgentDefinition.builder().agentId(" ").build()));
        assertThrows(IllegalArgumentException.class, () -> service.getAgent(ACTOR, ""));
        assertThrows(IllegalArgumentException.class, () -> service.unregisterAgent(ACTOR, " "));
        service.registerSystemAgent(AgentDefinition.builder().agentId("system").build());
        verify(cache).putSystem(any(AgentDefinition.class));
    }
}
