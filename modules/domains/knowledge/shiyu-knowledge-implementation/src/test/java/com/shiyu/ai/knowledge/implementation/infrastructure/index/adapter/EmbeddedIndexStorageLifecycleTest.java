package com.shiyu.ai.knowledge.implementation.infrastructure.index.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.api.VectorStoreProvider;
import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeChunkBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

/** 验证文件索引版本保留、清理锁、损坏清单和向量提供方切换的恢复边界。 */
class EmbeddedIndexStorageLifecycleTest {
    @TempDir Path root;

    @Test
    void keepsActiveAndRollbackVersionsAndRetainsFailedRemoteDeletion() throws Exception {
        VectorStoreProvider provider = provider();
        EmbeddedIndexStorage storage = new EmbeddedIndexStorage(provider, root.toString(), 1, 1);
        for (long version : List.of(1L, 2L, 3L, 4L)) {
            Path vectors = storage.path(1L, 5L, version).resolve("vector");
            Files.createDirectories(vectors);
            storage.writeVectorManifest(vectors, 3, "namespace-" + version);
        }
        Files.createDirectories(storage.path(1L, 5L, 3L).getParent().resolve("notes"));
        storage.cleanupOldVersions(1L, 5L, 3);
        assertFalse(Files.exists(storage.path(1L, 5L, 1L)));
        assertTrue(Files.exists(storage.path(1L, 5L, 2L)));
        assertTrue(Files.exists(storage.path(1L, 5L, 3L)));
        assertFalse(Files.exists(storage.path(1L, 5L, 4L)));
        doThrow(new IllegalStateException("remote unavailable")).when(provider).drop(any());
        storage.cleanupOldVersions(1L, 5L, 9);
        assertTrue(Files.exists(storage.path(1L, 5L, 2L)));
        assertTrue(Files.exists(storage.path(1L, 5L, 3L)));
        storage.cleanupOldVersions(99L, 5L, 1);
    }

    @Test
    void abandonedStagingCleanupHonorsLockAndCurrentFencingVersion() throws Exception {
        EmbeddedIndexStorage storage = new EmbeddedIndexStorage(provider(), root.toString(), 1, 0);
        storage.cleanupAbandonedStaging(1L, 5L, 3);
        Path old = storage.stagingPath(1L, 5L, 1L);
        Path current = storage.stagingPath(1L, 5L, 3L);
        Files.createDirectories(old.resolve("vector"));
        Files.createDirectories(current);
        Files.createDirectories(old.getParent().resolve("manual"));
        Path lockPath = storage.stagingLockPath(1L, 5L, 1L);
        Files.createDirectories(lockPath.getParent());
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                var lock = channel.lock()) {
            assertTrue(lock.isValid());
            storage.cleanupAbandonedStaging(1L, 5L, 3);
            assertTrue(Files.exists(old));
        }
        storage.cleanupAbandonedStaging(1L, 5L, 3, () -> false);
        assertTrue(Files.exists(old));
        storage.cleanupAbandonedStaging(1L, 5L, 3);
        assertFalse(Files.exists(old));
        assertTrue(Files.exists(current));
        assertTrue(Files.exists(old.getParent().resolve("manual")));
    }

    @Test
    void invalidManifestsCannotOpenOrTriggerRemoteDeletion() throws Exception {
        VectorStoreProvider provider = provider();
        EmbeddedIndexStorage storage = new EmbeddedIndexStorage(provider, root.toString(), 1, 0);
        Path vectors = storage.path(1L, 5L, 1L).resolve("vector");
        Files.createDirectories(vectors);
        assertThrows(IOException.class, () -> storage.readVectorManifest(vectors));
        for (String content : List.of("provider=test\ndimension=invalid", "dimension=3", "provider=\ndimension=3", "provider=test\ndimension=0")) {
            Files.writeString(vectors.resolve("manifest.properties"), content);
            assertThrows(IOException.class, () -> storage.readVectorManifest(vectors));
            assertFalse(storage.dropVectorStore(1L, 5L, 1, vectors));
        }
        verify(provider, never()).drop(any());
        Files.writeString(vectors.resolve("manifest.properties"), "provider=test\ndimension=3");
        assertEquals("knowledge/1/5/1", storage.readVectorManifest(vectors).namespace());
        storage.buildLucene(vectors.getParent().resolve("lucene"), List.of(), Map.of());
        Files.writeString(vectors.resolve("manifest.properties"), "provider=old\ndimension=3");
        assertThrows(ServiceException.class, () -> storage.handle(new EmbeddedIndexRegistry.IndexKey(1L, 5L, 1L)));
        storage.deleteTree(vectors.getParent());
        assertFalse(Files.exists(vectors));
        storage.deleteTree(null);
        storage.deleteTree(vectors);
    }

    @Test
    void losingLeaseDuringBuildDropsStagingVectorNamespace() throws Exception {
        VectorStoreProvider provider = provider();
        EmbeddedIndexStorage storage = new EmbeddedIndexStorage(provider, root.toString(), 1, 0);
        KnowledgeChunkBO chunk = new KnowledgeChunkBO();
        chunk.setId(1L); chunk.setDocumentId(2L); chunk.setContent("内容");
        chunk.setEmbeddingDimension(3); chunk.setEmbeddingBinary(new byte[12]);
        Path vectors = root.resolve("staging-vector");
        assertThrows(IllegalStateException.class,
                () -> storage.buildVector(vectors, 1L, 5L, 1L, List.of(chunk), 1L, () -> false));
        verify(provider).drop(any());
        assertThrows(IllegalStateException.class,
                () -> storage.buildLucene(root.resolve("staging-lucene"), List.of(chunk), Map.of(), () -> false));
        storage.closeAll();
    }

    private static VectorStoreProvider provider() {
        VectorStoreProvider provider = mock(VectorStoreProvider.class);
        when(provider.type()).thenReturn("test");
        when(provider.open(any())).thenAnswer(call -> mock(VectorStore.class));
        return provider;
    }
}
