package com.shiyu.ai.knowledge.implementation.infrastructure.index.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.vector.api.VectorStoreProvider;
import com.shiyu.ai.common.vector.config.VectorStoreProperties;
import com.shiyu.ai.common.vector.factory.ConfiguredVectorStoreProvider;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.implementation.domain.port.provider.EmbeddingProvider;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeChunkBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.implementation.domain.port.provider.RerankProvider;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.service.KnowledgeIndexService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 验证 Embedded 索引 Registry Integration 相关功能、边界条件、异常路径和协作行为。
 */
class EmbeddedIndexRegistryIntegrationTest {

    @TempDir Path tempDir;

    private EmbeddedIndexRegistry registry;

    @AfterEach
    void closeRegistry() {
        if (registry != null) registry.closeAll();
    }

    @Test
    void shouldBuildAndSearchVersionedIndexThroughVectorProvider() {
        KnowledgeEnterpriseRepository enterpriseRepository =
                mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDocumentRepository documentRepository = mock(KnowledgeDocumentRepository.class);
        KnowledgeChunkRepository chunkRepository = mock(KnowledgeChunkRepository.class);

        KnowledgeSpaceBO space = new KnowledgeSpaceBO();
        space.setId(10L);
        space.setTenantId(1L);
        space.setActiveIndexVersion(0L);

        KnowledgeDocumentBO document = new KnowledgeDocumentBO();
        document.setId(20L);
        document.setSpaceId(10L);
        document.setTitle("统一向量接口");
        document.setLifecycleStatus("PUBLISHED");

        KnowledgeChunkBO chunk = new KnowledgeChunkBO();
        chunk.setId(30L);
        chunk.setTenantId(1L);
        chunk.setSpaceId(10L);
        chunk.setDocumentId(20L);
        chunk.setContent("Knowledge 通过 vector 模块的公共 Provider 构建和检索索引");
        chunk.setEmbeddingDimension(3);
        chunk.setEmbeddingBinary(toBytes(new float[] {1F, 0F, 0F}));

        when(enterpriseRepository.findSpaceByTenant(new TenantId(1L), 10L)).thenReturn(space);
        when(documentRepository.findBySpace(new TenantId(1L), 10L)).thenReturn(List.of(document));
        when(chunkRepository.findBySpace(new TenantId(1L), 10L)).thenReturn(List.of(chunk));
        when(chunkRepository.findByIds(new TenantId(1L), 10L, List.of(30L)))
                .thenReturn(List.of(chunk));

        AtomicReference<ActorContext> capturedActor = new AtomicReference<>();
        AtomicBoolean embeddingEnabled = new AtomicBoolean(true);
        EmbeddingProvider embeddingProvider =
                new EmbeddingProvider() {
                    @Override
                    public String profile() {
                        return "test";
                    }

                    @Override
                    public float[] embed(
                            com.shiyu.ai.kernel.context.TenantId tenantId, String text) {
                        if (!embeddingEnabled.get())
                            throw new IllegalStateException("embedding disabled");
                        return new float[] {1F, 0F, 0F};
                    }

                    @Override
                    public float[] embed(ActorContext actor, String text) {
                        if (!embeddingEnabled.get())
                            throw new IllegalStateException("embedding disabled");
                        capturedActor.set(actor);
                        return new float[] {1F, 0F, 0F};
                    }
                };
        RerankProvider rerankProvider = mock(RerankProvider.class);
        VectorStoreProperties vectorProperties = new VectorStoreProperties();
        vectorProperties.setType("jvector");
        vectorProperties.setDimension(3);
        vectorProperties.setDataDir(tempDir.resolve("vector-default").toString());
        VectorStoreProvider provider = new ConfiguredVectorStoreProvider(vectorProperties);

        registry =
                new EmbeddedIndexRegistry(
                        enterpriseRepository,
                        documentRepository,
                        chunkRepository,
                        embeddingProvider,
                        rerankProvider,
                        provider,
                        tempDir.toString(),
                        5,
                        60,
                        1);

        long version = registry.rebuild(new TenantId(1L), 10L);
        clearInvocations(chunkRepository);
        List<KnowledgeIndexService.HybridHit> hits =
                registry.hybridSearch(new TenantId(1L), 10L, "统一向量接口", "HYBRID", 5, 0D, false);

        assertThat(version).isEqualTo(1L);
        assertThat(space.getActiveIndexVersion()).isEqualTo(1L);
        assertThat(hits).hasSize(1);
        assertThat(hits.getFirst().chunkId()).isEqualTo(30L);
        assertThat(hits.getFirst().vectorScore()).isGreaterThan(0D);
        assertThat(hits.getFirst().bm25Score()).isGreaterThan(0D);
        verify(chunkRepository, never()).findBySpace(new TenantId(1L), 10L);

        ActorContext actor = new ActorContext(new TenantId(1), new UserId(42), false);
        assertThat(registry.hybridSearch(actor, 10L, "统一向量接口", "SEMANTIC", 5, 0D, false))
                .hasSize(1);
        assertThat(capturedActor.get()).isEqualTo(actor);
        assertThat(registry.hybridSearch(new TenantId(1L), 10L, "统一向量接口", "KEYWORD", 5, 0D, false))
                .hasSize(1);
        assertThat(registry.hybridSearch(new TenantId(1L), 10L, "统一向量接口", "VECTOR", 5, 0D, false))
                .hasSize(1);
        when(rerankProvider.rerank(any(), any(), anyInt()))
                .thenReturn(java.util.Arrays.asList(0, null, 99));
        assertThat(registry.hybridSearch(new TenantId(1L), 10L, "统一向量接口", "HYBRID", 5, 0D, true))
                .hasSize(1);
        assertThat(registry.hybridSearch(new TenantId(1L), 10L, "统一向量接口", "HYBRID", 5, 999D, false))
                .isEmpty();
        assertThatThrownBy(
                        () ->
                                registry.hybridSearch(
                                        new TenantId(1L), 10L, "q", "UNKNOWN", 5, 0D, false))
                .isInstanceOf(RuntimeException.class);
        when(enterpriseRepository.findSpaceByTenant(new TenantId(1L), 99L)).thenReturn(null);
        assertThat(registry.hybridSearch(new TenantId(1L), 99L, "q", "HYBRID", 5, 0D, false))
                .isEmpty();
        assertThatThrownBy(() -> registry.rebuild(new TenantId(2L), 10L))
                .isInstanceOf(RuntimeException.class);
        when(enterpriseRepository.findSpaceByTenant(new TenantId(2L), 10L)).thenReturn(space);
        assertThatThrownBy(() -> registry.rebuild(new TenantId(2L), 10L))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(
                        () ->
                                registry.hybridSearch(
                                        (ActorContext) null, 10L, "q", "HYBRID", 5, 0D, false))
                .isInstanceOf(IllegalArgumentException.class);
        embeddingEnabled.set(false);
        assertThat(registry.hybridSearch(new TenantId(1L), 10L, "统一向量接口", "HYBRID", 5, 0D, false))
                .hasSize(1);
        assertThatThrownBy(
                        () ->
                                registry.hybridSearch(
                                        new TenantId(1L), 10L, "q", "SEMANTIC", 5, 0D, false))
                .isInstanceOf(RuntimeException.class);
        verify(enterpriseRepository).updateSpace(eq(new TenantId(1L)), any(KnowledgeSpaceBO.class));
    }

    @Test
    void rebuildsForDifferentSpacesDoNotWaitOnOneInstanceWideLock() throws Exception {
        KnowledgeEnterpriseRepository enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeDocumentRepository documents = mock(KnowledgeDocumentRepository.class);
        KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
        VectorStoreProvider provider = mock(VectorStoreProvider.class);
        when(provider.type()).thenReturn("jvector");
        when(provider.open(any())).thenReturn(mock(com.shiyu.ai.common.vector.api.VectorStore.class));
        KnowledgeSpaceBO firstSpace = space(10L);
        KnowledgeSpaceBO secondSpace = space(20L);
        when(enterprise.findSpaceByTenant(new TenantId(1L), 10L)).thenReturn(firstSpace);
        when(enterprise.findSpaceByTenant(new TenantId(1L), 20L)).thenReturn(secondSpace);

        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch secondEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        when(documents.findBySpace(any(TenantId.class), any(Long.class)))
                .thenAnswer(
                        invocation -> {
                            Long spaceId = invocation.getArgument(1);
                            if (spaceId.equals(10L)) {
                                firstEntered.countDown();
                                releaseFirst.await(5, TimeUnit.SECONDS);
                            } else if (spaceId.equals(20L)) {
                                secondEntered.countDown();
                            }
                            return List.of();
                        });
        EmbeddedIndexRegistry registry =
                new EmbeddedIndexRegistry(
                        enterprise,
                        documents,
                        chunks,
                        mock(EmbeddingProvider.class),
                        mock(RerankProvider.class),
                        provider,
                        tempDir.toString(),
                        5,
                        60,
                        1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> registry.rebuild(new TenantId(1L), 10L));
            assertThat(firstEntered.await(2, TimeUnit.SECONDS)).isTrue();
            var second = pool.submit(() -> registry.rebuild(new TenantId(1L), 20L));
            boolean secondEnteredWhileFirstWasBuilding = secondEntered.await(1, TimeUnit.SECONDS);
            releaseFirst.countDown();

            assertThat(secondEnteredWhileFirstWasBuilding).isTrue();
            assertThat(first.get(5, TimeUnit.SECONDS)).isEqualTo(1L);
            assertThat(second.get(5, TimeUnit.SECONDS)).isEqualTo(1L);
        } finally {
            releaseFirst.countDown();
            pool.shutdownNow();
            registry.closeAll();
        }
    }

    private KnowledgeSpaceBO space(Long id) {
        KnowledgeSpaceBO value = new KnowledgeSpaceBO();
        value.setId(id);
        value.setTenantId(1L);
        value.setActiveIndexVersion(0L);
        return value;
    }

    private byte[] toBytes(float[] vector) {
        ByteBuffer buffer =
                ByteBuffer.allocate(vector.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (float value : vector) buffer.putFloat(value);
        return buffer.array();
    }
}
