package com.shiyu.ai.knowledge.implementation.infrastructure.index.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.api.VectorStoreProvider;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.implementation.domain.port.provider.EmbeddingProvider;
import com.shiyu.ai.knowledge.implementation.domain.port.provider.RerankProvider;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** 用可控调度验证同步重建在心跳失效时停止发布，并始终释放租约。 */
@SuppressWarnings({"unchecked", "rawtypes"})
class EmbeddedIndexHeartbeatTest {
    @TempDir Path root;

    @Test
    void synchronousLeaseHeartbeatGatesPublicationAndRestoresTenantContext() {
        TenantId tenant = new TenantId(31);
        for (String outcome : List.of("success", "rejected", "unavailable")) {
            KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
            KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
            KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
            VectorStoreProvider provider = mock(VectorStoreProvider.class);
            when(provider.type()).thenReturn("test");
            when(provider.open(any())).thenAnswer(call -> mock(VectorStore.class));
            KnowledgeSpaceBO space = new KnowledgeSpaceBO();
            space.setId(5L); space.setTenantId(31L); space.setActiveIndexVersion(0L);
            when(enterprise.findSpaceByTenant(tenant,5L)).thenReturn(space);
            when(enterprise.claimIndexRebuildLease(tenant,5L)).thenReturn(1L);
            when(enterprise.activateIndexRebuild(tenant,5L,1L,0L,1L)).thenReturn(true);
            when(documents.findBySpace(tenant,5L)).thenReturn(List.of());
            when(chunks.findBySpace(tenant,5L)).thenReturn(List.of());
            if (outcome.equals("unavailable")) {
                when(enterprise.heartbeatIndexRebuildLease(eq(tenant),eq(5L),eq(1L),any()))
                        .thenThrow(new IllegalStateException("temporary database outage"));
            } else {
                when(enterprise.heartbeatIndexRebuildLease(eq(tenant),eq(5L),eq(1L),any()))
                        .thenReturn(outcome.equals("success"));
            }
            ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
            ScheduledFuture future = mock(ScheduledFuture.class);
            doAnswer(call -> { ((Runnable)call.getArgument(0)).run(); return future; })
                    .when(scheduler).scheduleWithFixedDelay(any(Runnable.class),eq(30L),eq(30L),eq(TimeUnit.SECONDS));
            EmbeddedIndexRegistry registry = new EmbeddedIndexRegistry(enterprise,documents,chunks,
                    mock(EmbeddingProvider.class),mock(RerankProvider.class),provider,
                    root.resolve(outcome).toString(),1,60,1);
            ReflectionTestUtils.setField(registry,"synchronousLeaseScheduler",scheduler);
            TenantScope.clear();
            try {
                if (outcome.equals("success")) {
                    assertEquals(1L,registry.rebuild(tenant,5L));
                    verify(enterprise).activateIndexRebuild(tenant,5L,1L,0L,1L);
                } else {
                    assertThrows(RuntimeException.class,() -> registry.rebuild(tenant,5L));
                    verify(enterprise,never()).activateIndexRebuild(any(),any(),any(),any(),any());
                }
                verify(enterprise).releaseIndexRebuildLease(tenant,5L,1L,outcome.equals("success"));
                verify(future).cancel(false);
                assertTrue(TenantScope.current().isEmpty());
            } finally {
                registry.closeAll();
                TenantScope.clear();
            }
        }
    }
}
