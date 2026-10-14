package com.shiyu.ai.agent.implementation.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.implementation.cache.AgentCacheManager;
import com.shiyu.ai.agent.implementation.cache.AgentLoader;
import com.shiyu.ai.agent.implementation.domain.model.AgentDefBO;
import com.shiyu.ai.agent.implementation.domain.model.AgentVersionBO;
import com.shiyu.ai.agent.implementation.port.repository.AgentAdminRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;

/**
 * 验证 智能体 Service Impl 相关功能、边界条件、异常路径和协作行为。
 */
class AgentServiceImplTest {
    private static final ActorContext ACTOR =
            new ActorContext(new TenantId(4), new UserId(8), false);
    private final AgentCacheManager cache = mock(AgentCacheManager.class);
    private final AgentLoader loader = mock(AgentLoader.class);
    private final AgentAdminRepository repository = mock(AgentAdminRepository.class);
    private final AgentServiceImpl service = new AgentServiceImpl(cache, loader, repository);

    @Test
    void registersLoadsListsAndUnregistersInTenantScope() {
        AgentDefinition definition = AgentDefinition.builder().agentId("math").name("Math").build();
        service.registerAgent(ACTOR, definition);
        verify(cache).put(ACTOR, "math", definition);
        when(cache.getOrLoad(ACTOR, "math", loader)).thenReturn(definition);
        assertSame(definition, service.getAgent(ACTOR, "math"));
        when(cache.listAll(ACTOR)).thenReturn(List.of(definition));
        assertEquals(List.of(definition), service.listAgents(ACTOR));
        when(cache.containsKey(ACTOR, "math")).thenReturn(true);
        assertTrue(service.unregisterAgent(ACTOR, "math"));
        verify(repository, never()).deleteByAgentId(ACTOR.tenantId(), "math");
        verify(cache).evictTenant(ACTOR.tenantId(), "math");
    }

    @Test
    void loadsMissingAgentButDoesNotSwitchCacheOnlyDefinition() {
        AgentVersion version = AgentVersion.builder().versionNumber("v2").build();
        AgentDefinition definition =
                AgentDefinition.builder()
                        .agentId("math")
                        .currentVersion("v1")
                        .versions(Map.of("v2", version))
                        .build();
        when(cache.get(ACTOR, "math")).thenReturn(null);
        when(cache.getOrLoad(ACTOR, "math", loader)).thenReturn(definition);
        assertSame(definition, service.getAgent(ACTOR, "math"));
        assertFalse(service.switchVersion(ACTOR, "math", "v2"));
        assertEquals("v1", definition.getCurrentVersion());
        assertFalse(service.switchVersion(ACTOR, "math", "missing"));
        when(cache.getOrLoad(ACTOR, "missing", loader)).thenReturn(null);
        assertFalse(service.switchVersion(ACTOR, "missing", "v1"));
    }

    @Test
    void getAgentLoadsTenantDefinitionBeforeUsingSystemFallback() {
        AgentCacheManager realCache = new AgentCacheManager(repository, loader);
        AgentServiceImpl realService = new AgentServiceImpl(realCache, loader, repository);
        AgentDefinition system = AgentDefinition.builder()
                .agentId("shared").name("System").build();
        AgentDefinition tenant = AgentDefinition.builder()
                .agentId("shared").name("Tenant").build();
        realCache.putSystem(system);
        when(loader.loadFromDb(ACTOR, "shared")).thenReturn(tenant);

        assertSame(tenant, realService.getAgent(ACTOR, "shared"));
    }

    @Test
    void unregisterMissingOrSystemOnlyAgentDoesNotReportTenantDeletion() {
        assertFalse(service.unregisterAgent(ACTOR, "missing"));
        verify(repository, never()).deleteByAgentId(any(TenantId.class), anyString());
        verify(cache, never()).evictTenant(any(TenantId.class), anyString());
    }

    @Test
    void unregisterPersistedAgentEvictsItsTenantCache() {
        AgentDefBO stored = new AgentDefBO();
        stored.setAgentId("stored");
        when(repository.selectByAgentId(ACTOR.tenantId(), "stored")).thenReturn(stored);

        assertTrue(service.unregisterAgent(ACTOR, "stored"));

        verify(repository).deleteByAgentId(ACTOR.tenantId(), "stored");
        verify(cache).evictTenant(ACTOR.tenantId(), "stored");
    }

    @Test
    void versionSwitchPersistsPublishedVersionBeforeRefreshingCache() {
        AgentDefBO stored = new AgentDefBO();
        stored.setId(11L);
        stored.setAgentId("math");
        stored.setCurrentVersion("v1");
        AgentVersionBO published = new AgentVersionBO();
        published.setAgentId("math");
        published.setVersionNumber("v2");
        published.setStatus(1);
        published.setGraphConfig("{\"startNode\":\"n\",\"endNode\":\"n\",\"nodes\":{\"n\":{\"nodeType\":\"DEFAULT\"}}}");
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(stored);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "v2"))
                .thenReturn(published);
        when(repository.update(eq(ACTOR.tenantId()), any(AgentDefBO.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        assertTrue(service.switchVersion(ACTOR, "math", "v2"));

        assertEquals("v2", stored.getCurrentVersion());
        verify(repository).update(ACTOR.tenantId(), stored);
        verify(cache).evictTenant(ACTOR.tenantId(), "math");
    }

    @Test
    void versionSwitchRejectsUnpublishedAndCacheOnlyVersions() {
        AgentDefBO stored = new AgentDefBO();
        stored.setId(11L);
        stored.setAgentId("math");
        stored.setCurrentVersion("v1");
        AgentVersionBO draft = new AgentVersionBO();
        draft.setStatus(0);
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(stored);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "draft"))
                .thenReturn(draft);

        assertFalse(service.switchVersion(ACTOR, "math", "draft"));
        assertFalse(service.switchVersion(ACTOR, "math", "missing"));
        assertFalse(service.switchVersion(ACTOR, "cache-only", "v2"));
        assertEquals("v1", stored.getCurrentVersion());
        verify(repository, never()).update(eq(ACTOR.tenantId()), any(AgentDefBO.class));
        verify(cache, never()).evictTenant(any(TenantId.class), anyString());
    }

    @Test
    void versionSwitchRejectsPublishedVersionWithoutExecutableGraph() {
        AgentDefBO stored = new AgentDefBO();
        stored.setAgentId("math");
        stored.setCurrentVersion("v1");
        AgentVersionBO incomplete = new AgentVersionBO();
        incomplete.setStatus(1);
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(stored);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "v2"))
                .thenReturn(incomplete);

        assertFalse(service.switchVersion(ACTOR, "math", "v2"));

        assertEquals("v1", stored.getCurrentVersion());
        verify(repository, never()).update(eq(ACTOR.tenantId()), any(AgentDefBO.class));
        verify(cache, never()).evictTenant(any(TenantId.class), anyString());
    }

    @Test
    void versionSwitchRejectsPublishedVersionWithInvalidStructure() {
        AgentDefBO stored = new AgentDefBO();
        stored.setAgentId("math");
        stored.setCurrentVersion("v1");
        AgentVersionBO invalid = new AgentVersionBO();
        invalid.setStatus(1);
        invalid.setGraphConfig("{\"startNode\":\"missing\",\"endNode\":\"n\",\"nodes\":{\"n\":{}}}");
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(stored);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "v2"))
                .thenReturn(invalid);

        assertFalse(service.switchVersion(ACTOR, "math", "v2"));
        assertEquals("v1", stored.getCurrentVersion());
        verify(repository, never()).update(eq(ACTOR.tenantId()), any(AgentDefBO.class));
    }

    @Test
    void invalidatesTenantCacheAgainAfterTransactionCompletion() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            service.evictRuntimeCache(ACTOR, "math");
            verify(cache).evictTenant(ACTOR.tenantId(), "math");

            for (TransactionSynchronization synchronization
                    : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
            }

            verify(cache, times(2)).evictTenant(ACTOR.tenantId(), "math");
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void rollbackDiscardsDefinitionLoadedInsideTheAbortedTransaction() {
        AgentCacheManager realCache = new AgentCacheManager(repository, loader);
        AgentServiceImpl realService = new AgentServiceImpl(realCache, loader, repository);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            realService.evictRuntimeCache(ACTOR, "math");
            realCache.put(ACTOR, "math", AgentDefinition.builder()
                    .agentId("math").name("Uncommitted name").build());

            for (TransactionSynchronization synchronization
                    : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            }

            assertFalse(realCache.containsKey(ACTOR, "math"));
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void rejectsInvalidDefinitionsAndAgentIds() {
        assertThrows(IllegalArgumentException.class, () -> service.registerAgent(ACTOR, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerAgent(ACTOR, AgentDefinition.builder().agentId(" ").build()));
        assertThrows(IllegalArgumentException.class, () -> service.getAgent(ACTOR, ""));
        assertThrows(IllegalArgumentException.class, () -> service.unregisterAgent(ACTOR, " "));
        service.registerSystemAgent(AgentDefinition.builder().agentId("system").build());
        verify(cache).putSystem(any(AgentDefinition.class));
    }

    @Test
    void invalidVersionSwitchIdentifiersCannotQueryOrChangeStoredAgents() {
        for (String agentId : new String[] {null, "", " "}) {
            assertFalse(service.switchVersion(ACTOR, agentId, "v2"));
        }
        for (String version : new String[] {null, "", " "}) {
            assertFalse(service.switchVersion(ACTOR, "math", version));
        }
        verifyNoInteractions(repository);
        verify(cache, never()).evictTenant(any(), anyString());
    }
}
