package com.shiyu.ai.knowledge.index;

import com.shiyu.ai.knowledge.model.EmbeddingProvider;
import com.shiyu.ai.knowledge.model.RerankProvider;
import com.shiyu.ai.knowledge.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeChunkRepository;
import com.shiyu.ai.knowledge.domain.model.KnowledgeChunkBO;
import com.shiyu.ai.knowledge.index.FullTextIndex.FullTextHit;
import com.shiyu.ai.knowledge.index.VectorIndex.VectorHit;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.vector.VectorStoreProvider;
import com.shiyu.ai.vector.VectorStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

class EmbeddedIndexRegistryCoverageTest {

    @TempDir
    Path tempDir;

    @Test
    void validatesVectorManifestAndIndexUtilityPaths() throws Exception {
        VectorStoreProvider provider = mock(VectorStoreProvider.class);
        when(provider.type()).thenReturn("jvector");
        EmbeddedIndexRegistry registry = registry(provider, 1);
        Path vector = tempDir.resolve("vector");
        Files.createDirectories(vector);

        invoke(registry, "writeVectorManifest", new Class<?>[]{Path.class, int.class}, vector, 3);
        Object manifest = invoke(registry, "readVectorManifest", new Class<?>[]{Path.class}, vector);
        Method providerAccessor = manifest.getClass().getDeclaredMethod("provider");
        Method dimensionAccessor = manifest.getClass().getDeclaredMethod("dimension");
        assertEquals("jvector", providerAccessor.invoke(manifest));
        assertEquals(3, dimensionAccessor.invoke(manifest));

        Files.delete(vector.resolve("manifest.properties"));
        assertThrows(Exception.class, () -> invoke(registry, "readVectorManifest",
                new Class<?>[]{Path.class}, vector));
        Files.createDirectories(vector);
        Files.writeString(vector.resolve("manifest.properties"), "provider=jvector\ndimension=bad\n");
        assertThrows(Exception.class, () -> invoke(registry, "readVectorManifest",
                new Class<?>[]{Path.class}, vector));
        Files.writeString(vector.resolve("manifest.properties"), "provider=\ndimension=0\n");
        assertThrows(Exception.class, () -> invoke(registry, "readVectorManifest",
                new Class<?>[]{Path.class}, vector));

        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(1.5F).putFloat(-2F).array();
        assertArrayEquals(new float[]{1.5F, -2F},
                (float[]) invoke(registry, "fromBytes", new Class<?>[]{byte[].class}, bytes));
        assertEquals(12L, invoke(registry, "parseVersion", new Class<?>[]{String.class}, "12"));
        assertEquals(null, invoke(registry, "parseVersion", new Class<?>[]{String.class}, "bad"));
        assertEquals("/data", invoke(registry, "resolveAppHome", new Class<?>[]{String.class}, "/data"));
        registry.closeAll();
    }

    @Test
    void cleansOldVersionsAndRemovesPartiallyBuiltIndexesOnFailure() throws Exception {
        VectorStoreProvider provider = mock(VectorStoreProvider.class);
        when(provider.type()).thenReturn("jvector");
        EmbeddedIndexRegistry registry = registry(provider, 1);
        Path spaceRoot = tempDir.resolve("index").resolve("1").resolve("10");
        Files.createDirectories(spaceRoot.resolve("1"));
        Files.createDirectories(spaceRoot.resolve("2"));
        Files.createDirectories(spaceRoot.resolve("3"));
        Files.createDirectories(spaceRoot.resolve("not-a-version"));
        invoke(registry, "cleanupOldVersions", new Class<?>[]{Long.class, Long.class, long.class},
                1L, 10L, 3L);
        assertFalse(Files.exists(spaceRoot.resolve("1")));
        assertEquals(true, Files.exists(spaceRoot.resolve("not-a-version")));
        assertEquals(true, Files.exists(spaceRoot.resolve("2")));
        assertEquals(true, Files.exists(spaceRoot.resolve("3")));

        var enterprise = mock(KnowledgeEnterpriseRepository.class);
        var documents = mock(KnowledgeDocumentRepository.class);
        var chunks = mock(KnowledgeChunkRepository.class);
        var failingProvider = mock(VectorStoreProvider.class);
        when(failingProvider.type()).thenReturn("jvector");
        when(failingProvider.open(org.mockito.ArgumentMatchers.any())).thenThrow(new IllegalStateException("vector down"));
        var space = new com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO();
        space.setId(10L); space.setTenantId(1L); space.setActiveIndexVersion(0L);
        when(enterprise.findSpaceByTenant(new com.shiyu.ai.kernel.context.TenantId(1L), 10L)).thenReturn(space);
        when(documents.findBySpace(new com.shiyu.ai.kernel.context.TenantId(1L), 10L)).thenReturn(java.util.List.of());
        when(chunks.findBySpace(new com.shiyu.ai.kernel.context.TenantId(1L), 10L)).thenReturn(java.util.List.of());
        EmbeddedIndexRegistry failing = new EmbeddedIndexRegistry(enterprise, documents, chunks,
                mock(EmbeddingProvider.class), mock(RerankProvider.class), failingProvider,
                tempDir.toString(), 5, 60, 1);
        assertThrows(RuntimeException.class, () -> failing.rebuild(new TenantId(1L), 10L));
        assertFalse(Files.exists(tempDir.resolve("index").resolve("1").resolve("10").resolve("1")));
        failing.closeAll();
    }

    @Test
    void coversHitMappingAndSafeCleanupEdges() throws Exception {
        VectorStoreProvider provider = mock(VectorStoreProvider.class);
        when(provider.type()).thenReturn("jvector");
        KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
        EmbeddedIndexRegistry registry = new EmbeddedIndexRegistry(mock(KnowledgeEnterpriseRepository.class),
                mock(KnowledgeDocumentRepository.class), chunks, mock(EmbeddingProvider.class),
                mock(RerankProvider.class), provider, tempDir.toString(), 5, 60, 0);
        KnowledgeChunkBO chunk = new KnowledgeChunkBO();
        chunk.setId(10L);
        chunk.setDocumentId(20L);
        chunk.setContent("content");
        when(chunks.getById(new com.shiyu.ai.kernel.context.TenantId(1L), 10L)).thenReturn(chunk);
        var hits = java.util.List.of(new FullTextHit(10L, 20L, 0.8F, "<b>hit</b>"),
                new FullTextHit(11L, 21L, 0.1F, null));
        Object keyword = invoke(registry, "keywordHits",
                new Class<?>[]{com.shiyu.ai.kernel.context.TenantId.class, java.util.List.class, int.class, double.class},
                new com.shiyu.ai.kernel.context.TenantId(1L), hits, 10, 0D);
        assertEquals(2, ((java.util.List<?>) keyword).size());
        Object filtered = invoke(registry, "keywordHits",
                new Class<?>[]{com.shiyu.ai.kernel.context.TenantId.class, java.util.List.class, int.class, double.class},
                new com.shiyu.ai.kernel.context.TenantId(1L), hits, 1, 0.5D);
        assertEquals(1, ((java.util.List<?>) filtered).size());
        assertEquals("content", ((KnowledgeIndexService.HybridHit) invoke(registry, "vectorHit",
                new Class<?>[]{com.shiyu.ai.kernel.context.TenantId.class, VectorHit.class, Map.class},
                new com.shiyu.ai.kernel.context.TenantId(1L), new VectorHit(10L, 0.7D), null)).content());
        assertEquals("", ((KnowledgeIndexService.HybridHit) invoke(registry, "vectorHit",
                new Class<?>[]{com.shiyu.ai.kernel.context.TenantId.class, VectorHit.class, Map.class},
                new com.shiyu.ai.kernel.context.TenantId(1L), new VectorHit(99L, 0.7D), null)).content());

        assertEquals(System.getProperty("app.home", ".") + "/data", invoke(registry, "resolveAppHome",
                new Class<?>[]{String.class}, "${app.home}/data"));
        invoke(registry, "cleanupOldVersions", new Class<?>[]{Long.class, Long.class, long.class}, 99L, 99L, 1L);
        invoke(registry, "closeQuietly", new Class<?>[]{AutoCloseable.class}, new AutoCloseable() {
            @Override public void close() { throw new IllegalStateException("close"); }
        });
        invoke(registry, "closeQuietly", new Class<?>[]{AutoCloseable.class}, new Object[]{null});
        registry.closeAll();
    }

    @Test
    void coversHybridRankingModesWithTenantScopedResults() {
        var enterprise = mock(KnowledgeEnterpriseRepository.class);
        KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
        VectorStoreProvider provider = mock(VectorStoreProvider.class);
        EmbeddingProvider embedding = mock(EmbeddingProvider.class);
        RerankProvider rerank = mock(RerankProvider.class);
        when(provider.type()).thenReturn("jvector");
        var space = new com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO();
        space.setId(10L);
        space.setTenantId(1L);
        space.setActiveIndexVersion(2L);
        when(enterprise.findSpaceByTenant(new com.shiyu.ai.kernel.context.TenantId(1L), 10L)).thenReturn(space);
        when(embedding.embed(new com.shiyu.ai.kernel.context.TenantId(1L), "q"))
                .thenReturn(new float[]{1F});
        KnowledgeChunkBO first = new KnowledgeChunkBO();
        first.setId(1L); first.setDocumentId(11L); first.setContent("first");
        KnowledgeChunkBO second = new KnowledgeChunkBO();
        second.setId(2L); second.setDocumentId(12L); second.setContent("second");
        when(chunks.findBySpace(new com.shiyu.ai.kernel.context.TenantId(1L), 10L))
                .thenReturn(java.util.List.of(first, second));
        when(chunks.getById(new com.shiyu.ai.kernel.context.TenantId(1L), 1L)).thenReturn(first);
        when(chunks.getById(new com.shiyu.ai.kernel.context.TenantId(1L), 99L)).thenReturn(null);
        EmbeddedIndexRegistry base = new EmbeddedIndexRegistry(enterprise,
                mock(KnowledgeDocumentRepository.class), chunks, embedding, rerank, provider,
                tempDir.toString(), 5, 60, 1);
        EmbeddedIndexRegistry registry = org.mockito.Mockito.spy(base);
        doReturn(java.util.List.of(new FullTextHit(1L, 11L, 0.9F, "hit"),
                new FullTextHit(99L, 99L, 0.1F, null)))
                .when(registry).search(new TenantId(1L), 10L, 2L, "q", 20);
        doReturn(java.util.List.of(new VectorHit(2L, 0.8D), new VectorHit(1L, 0.2D)))
                .when(registry).search(new TenantId(1L), 10L, 2L, new float[]{1F}, 20);

        assertEquals(1, registry.hybridSearch(new TenantId(1L), 10L, "q", "KEYWORD", 1, 0.5D, false).size());
        assertEquals(1, registry.hybridSearch(new TenantId(1L), 10L, "q", "SEMANTIC", 1, 0.5D, false).size());
        when(rerank.rerank("q", java.util.List.of("first", "second"), 2))
                .thenReturn(java.util.List.of(1, -1, 99));
        assertFalse(registry.hybridSearch(new TenantId(1L), 10L, "q", "HYBRID", 2, 0D, true).isEmpty());

        ActorContext actor = new ActorContext(new com.shiyu.ai.kernel.context.TenantId(1L),
                new com.shiyu.ai.kernel.context.UserId(7L), false);
        when(embedding.embed(actor, "q")).thenReturn(new float[]{1F});
        assertFalse(registry.hybridSearch(actor, 10L, "q", "VECTOR", 2, 0D, false).isEmpty());
        registry.closeAll();
    }

    @Test
    void normalizesHybridParametersAndFailsClosedWhenVectorSearchIsUnavailable() {
        var enterprise = mock(KnowledgeEnterpriseRepository.class);
        VectorStoreProvider provider = mock(VectorStoreProvider.class);
        EmbeddingProvider embedding = mock(EmbeddingProvider.class);
        KnowledgeChunkRepository chunks = mock(KnowledgeChunkRepository.class);
        when(provider.type()).thenReturn("jvector");
        var space = new com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO();
        space.setId(10L); space.setTenantId(1L); space.setActiveIndexVersion(2L);
        when(enterprise.findSpaceByTenant(new com.shiyu.ai.kernel.context.TenantId(1L), 10L)).thenReturn(space);
        KnowledgeChunkBO chunk = new KnowledgeChunkBO();
        chunk.setId(1L); chunk.setDocumentId(11L); chunk.setContent("text");
        when(chunks.findBySpace(new com.shiyu.ai.kernel.context.TenantId(1L), 10L)).thenReturn(java.util.List.of(chunk));
        EmbeddedIndexRegistry registry = org.mockito.Mockito.spy(new EmbeddedIndexRegistry(enterprise,
                mock(KnowledgeDocumentRepository.class), chunks, embedding, mock(RerankProvider.class), provider,
                tempDir.toString(), 5, 60, 1));
        doReturn(java.util.List.of(new FullTextHit(1L, 11L, 0.9F, "hit")))
                .when(registry).search(eq(new TenantId(1L)), eq(10L), eq(2L), anyString(), anyInt());
        doReturn(java.util.List.of(new VectorHit(1L, 0.9D)))
                .when(registry).search(eq(new TenantId(1L)), eq(10L), eq(2L), any(float[].class), anyInt());
        when(embedding.embed(new com.shiyu.ai.kernel.context.TenantId(1L), "q"))
                .thenReturn(new float[]{1F});

        assertEquals(1, registry.hybridSearch(new TenantId(1L), 10L, "q", null, 0, -1D, false).size());
        assertEquals(1, registry.hybridSearch(new TenantId(1L), 10L, "q", " semantic ", 200, 0D, false).size());
        assertThrows(RuntimeException.class,
                () -> registry.hybridSearch(new TenantId(1L), 10L, "q", "not-supported", 1, 0D, false));

        when(embedding.embed(new com.shiyu.ai.kernel.context.TenantId(1L), "q"))
                .thenThrow(new IllegalStateException("disabled"));
        assertEquals(1, registry.hybridSearch(new TenantId(1L), 10L, "q", "HYBRID", 1, 0D, false).size());
        assertThrows(RuntimeException.class,
                () -> registry.hybridSearch(new TenantId(1L), 10L, "q", "VECTOR", 1, 0D, false));
        registry.closeAll();
    }

    @Test
    void rejectsMissingOrForeignSpacesAndBuildsChunksWithoutEmbeddings() {
        var enterprise = mock(KnowledgeEnterpriseRepository.class);
        var documents = mock(KnowledgeDocumentRepository.class);
        var chunks = mock(KnowledgeChunkRepository.class);
        VectorStoreProvider provider = mock(VectorStoreProvider.class);
        VectorStore store = mock(VectorStore.class);
        when(provider.type()).thenReturn("jvector");
        when(provider.open(any())).thenReturn(store);
        EmbeddedIndexRegistry registry = new EmbeddedIndexRegistry(enterprise, documents, chunks,
                mock(EmbeddingProvider.class), mock(RerankProvider.class), provider,
                tempDir.toString(), 5, 60, 1);

        assertThrows(RuntimeException.class, () -> registry.rebuild(new TenantId(1L), 10L));
        var foreign = new com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO();
        foreign.setId(10L); foreign.setTenantId(2L); foreign.setActiveIndexVersion(0L);
        when(enterprise.findSpaceByTenant(new com.shiyu.ai.kernel.context.TenantId(1L), 10L))
                .thenReturn(foreign);
        assertThrows(RuntimeException.class, () -> registry.rebuild(new TenantId(1L), 10L));

        var space = new com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO();
        space.setId(10L); space.setTenantId(1L); space.setActiveIndexVersion(null);
        when(enterprise.findSpaceByTenant(new com.shiyu.ai.kernel.context.TenantId(1L), 10L))
                .thenReturn(space);
        var document = new com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO();
        document.setId(20L); document.setLifecycleStatus("PUBLISHED"); document.setTitle("Document");
        var chunk = new KnowledgeChunkBO();
        chunk.setId(30L); chunk.setDocumentId(20L); chunk.setContent("without vector");
        when(documents.findBySpace(new com.shiyu.ai.kernel.context.TenantId(1L), 10L))
                .thenReturn(java.util.List.of(document));
        when(chunks.findBySpace(new com.shiyu.ai.kernel.context.TenantId(1L), 10L))
                .thenReturn(java.util.List.of(chunk));
        assertEquals(1L, registry.rebuild(new TenantId(1L), 10L));
        assertEquals(1L, space.getActiveIndexVersion());
        verify(store).flush();
        registry.closeAll();
    }

    @Test
    void returnsEmptyForInactiveIndexesAndMapsPreloadedVectorChunks() throws Exception {
        var enterprise = mock(KnowledgeEnterpriseRepository.class);
        VectorStoreProvider provider = mock(VectorStoreProvider.class);
        when(provider.type()).thenReturn("jvector");
        var space = new com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO();
        space.setId(10L); space.setTenantId(1L); space.setActiveIndexVersion(0L);
        when(enterprise.findSpaceByTenant(new com.shiyu.ai.kernel.context.TenantId(1L), 10L))
                .thenReturn(space);
        EmbeddedIndexRegistry registry = new EmbeddedIndexRegistry(enterprise,
                mock(KnowledgeDocumentRepository.class), mock(KnowledgeChunkRepository.class),
                mock(EmbeddingProvider.class), mock(RerankProvider.class), provider,
                tempDir.toString(), 5, 60, 1);
        assertTrue(registry.hybridSearch(new TenantId(1L), 10L, "q", "HYBRID", 5, 0D, false).isEmpty());
        KnowledgeChunkBO chunk = new KnowledgeChunkBO();
        chunk.setId(10L); chunk.setDocumentId(20L); chunk.setContent("preloaded");
        Object mapped = invoke(registry, "vectorHit",
                new Class<?>[]{com.shiyu.ai.kernel.context.TenantId.class, VectorHit.class, Map.class},
                new com.shiyu.ai.kernel.context.TenantId(1L), new VectorHit(10L, 0.8D), Map.of(10L, chunk));
        assertEquals("preloaded", ((KnowledgeIndexService.HybridHit) mapped).content());
        registry.closeAll();
    }

    @Test
    void rejectsMissingTenantAtPublicSearchBoundary() {
        EmbeddedIndexRegistry registry = registry(mock(VectorStoreProvider.class), 1);
        assertThrows(IllegalArgumentException.class,
                () -> registry.search((TenantId) null, 10L, 1L, "query", 5));
        assertThrows(IllegalArgumentException.class,
                () -> registry.search((TenantId) null, 10L, 1L, new float[]{1F}, 5));
        assertThrows(IllegalArgumentException.class,
                () -> registry.hybridSearch((TenantId) null, 10L, "query", "KEYWORD", 5, 0D, false));
        registry.closeAll();
    }

    private EmbeddedIndexRegistry registry(VectorStoreProvider provider, int rollbackVersions) {
        return new EmbeddedIndexRegistry(mock(KnowledgeEnterpriseRepository.class),
                mock(KnowledgeDocumentRepository.class), mock(KnowledgeChunkRepository.class),
                mock(EmbeddingProvider.class), mock(RerankProvider.class), provider,
                tempDir.toString(), 5, 60, rollbackVersions);
    }

    private static Object invoke(Object target, String name, Class<?>[] types, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name, types);
        method.setAccessible(true);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception checked) throw checked;
            if (cause instanceof Error error) throw error;
            throw exception;
        }
    }
}
