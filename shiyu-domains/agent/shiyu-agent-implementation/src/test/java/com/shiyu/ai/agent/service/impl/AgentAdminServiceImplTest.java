package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.domain.model.AgentDefBO;
import com.shiyu.ai.agent.domain.model.AgentVersionBO;
import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import com.shiyu.ai.agent.request.AgentRequest;
import com.shiyu.ai.agent.service.AgentService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgentAdminServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(12), new UserId(6), false);
    private final AgentAdminRepository repository = mock(AgentAdminRepository.class);
    private final AgentService agentService = mock(AgentService.class);
    private final AgentAdminServiceImpl service = new AgentAdminServiceImpl(repository, agentService);

    @Test
    void pagesGetsCreatesUpdatesAndDeletesAgentsInTenantScope() {
        AgentDefBO def = new AgentDefBO(); def.setId(1L); def.setAgentId("math"); def.setName("Math"); def.setStatus(1);
        AgentVersionBO version = new AgentVersionBO(); version.setAgentId("math"); version.setVersionNumber("v1");
        when(repository.selectPage(ACTOR.tenantId(), 1, 10, "", 1)).thenReturn(Pair.of(1L, List.of(def)));
        when(repository.selectById(ACTOR.tenantId(), 1L)).thenReturn(def).thenReturn(def).thenReturn(null);
        when(repository.selectVersionsByAgentId(ACTOR.tenantId(), "math")).thenReturn(List.of(version));
        when(repository.selectByAgentId(ACTOR.tenantId(), "new-agent")).thenReturn(null);
        when(repository.selectAllActive(ACTOR.tenantId())).thenReturn(List.of(def));
        when(repository.create(eq(ACTOR.tenantId()), any(AgentDefBO.class))).thenAnswer(invocation -> invocation.getArgument(1));
        when(repository.update(eq(ACTOR.tenantId()), any(AgentDefBO.class))).thenAnswer(invocation -> invocation.getArgument(1));
        assertEquals(1, service.getPage(ACTOR, 1, 10, "", 1).getRight().size());
        assertNotNull(service.getById(ACTOR, 1L));
        AgentRequest request = new AgentRequest(); request.setAgentId("new-agent"); request.setName("New");
        assertEquals("new-agent", service.create(ACTOR, request).getAgentId());
        request.setName("Renamed"); assertEquals("Renamed", service.update(ACTOR, 1L, request).getName());
        service.deleteById(ACTOR, 1L);
        assertEquals(1, service.listAllOptions(ACTOR).size());
        verify(agentService, atLeastOnce()).evictRuntimeCache("math");
    }

    @Test
    void rejectsDuplicateAndMissingAgentsAndExposesNodeMetadata() {
        AgentDefBO existing = new AgentDefBO(); existing.setAgentId("math");
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(existing);
        AgentRequest request = new AgentRequest(); request.setAgentId("math"); request.setName("Math");
        assertThrows(IllegalArgumentException.class, () -> service.create(ACTOR, request));
        when(repository.selectById(ACTOR.tenantId(), 99L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.update(ACTOR, 99L, request));
        assertNull(service.getById(ACTOR, 99L));
        assertFalse(service.getNodeTypes().isEmpty());
    }
}
