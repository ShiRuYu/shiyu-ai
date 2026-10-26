package com.shiyu.ai.common.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.api.VectorStoreProvider;
import com.shiyu.ai.common.vector.config.VectorStoreProperties;
import com.shiyu.ai.common.vector.factory.ConfiguredVectorStoreProvider;
import com.shiyu.ai.common.vector.implementation.InMemoryVectorStore;
import com.shiyu.ai.common.vector.implementation.JVectorStore;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorSearchRequest;
import com.shiyu.ai.common.vector.model.VectorSearchType;
import com.shiyu.ai.common.vector.model.VectorStoreOptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

/**
 * 验证 向量 Store Provider Integration 相关功能、边界条件、异常路径和协作行为。
 */
class VectorStoreProviderIntegrationTest {

    @TempDir Path tempDir;

    @Test
    void numericFiltersPreservePrecisionAcrossVectorStores() {
        long storedId = 9_007_199_254_740_993L;
        long adjacentId = 9_007_199_254_740_992L;
        BigDecimal preciseDecimal = new BigDecimal("9007199254740993.1");
        VectorRecord record =
                new VectorRecord(
                        "large-id",
                        new float[] {1F, 0F, 0F},
                        Map.of("count", storedId, "decimal", preciseDecimal));

        InMemoryVectorStore inMemoryStore = new InMemoryVectorStore(3);
        inMemoryStore.upsert(record);
        assertThat(inMemoryStore.count(Map.of("count", adjacentId))).isZero();
        assertThat(inMemoryStore.count(Map.of("count", storedId))).isEqualTo(1);
        assertThat(inMemoryStore.count(Map.of("count", 9_007_199_254_740_993D))).isZero();
        assertThat(inMemoryStore.count(Map.of("decimal", 9_007_199_254_740_992D))).isZero();
        assertThat(inMemoryStore.count(Map.of("decimal", preciseDecimal))).isEqualTo(1);
        assertThat(inMemoryStore.count(Map.of("count", 1))).isZero();
        inMemoryStore.upsert(
                new VectorRecord("small-id", new float[] {0F, 1F, 0F}, Map.of("count", 1L)));
        assertThat(inMemoryStore.count(Map.of("count", 1))).isEqualTo(1);

        VectorStoreProperties properties = properties("jvector", 3, tempDir.resolve("jvector"));
        try (JVectorStore jVectorStore = new JVectorStore(properties)) {
            jVectorStore.upsert(record);
            assertThat(jVectorStore.count(Map.of("count", adjacentId))).isZero();
            assertThat(jVectorStore.count(Map.of("count", storedId))).isEqualTo(1);
            assertThat(jVectorStore.count(Map.of("count", 9_007_199_254_740_993D))).isZero();
            assertThat(jVectorStore.count(Map.of("decimal", 9_007_199_254_740_992D))).isZero();
            assertThat(jVectorStore.count(Map.of("decimal", preciseDecimal))).isEqualTo(1);
            assertThat(jVectorStore.count(Map.of("count", 1))).isZero();
            jVectorStore.upsert(
                    new VectorRecord("small-id", new float[] {0F, 1F, 0F}, Map.of("count", 1L)));
            assertThat(jVectorStore.count(Map.of("count", 1))).isEqualTo(1);
        }
    }

    @Test
    void shouldPersistFilterDeleteAndReopenThroughPublicProvider() {
        VectorStoreProperties properties = properties("jvector", 3, tempDir.resolve("defaults"));
        VectorStoreProvider provider = new ConfiguredVectorStoreProvider(properties);
        Path indexDir = tempDir.resolve("knowledge-index");
        VectorStoreOptions options =
                VectorStoreOptions.of("knowledge/1/2/3", 3, indexDir.toString());

        try (VectorStore store = provider.open(options)) {
            store.upsertBatch(
                    List.of(
                            new VectorRecord("a", new float[] {1F, 0F, 0F}, Map.of("tenantId", 1L)),
                            new VectorRecord(
                                    "b", new float[] {0.9F, 0.1F, 0F}, Map.of("tenantId", 2L)),
                            new VectorRecord(
                                    "c", new float[] {0F, 1F, 0F}, Map.of("tenantId", 1L))));

            List<VectorRecord> filtered =
                    store.search(
                            VectorSearchRequest.builder()
                                    .queryVector(new float[] {1F, 0F, 0F})
                                    .topK(2)
                                    .filter(Map.of("tenantId", 1))
                                    .minScore(0.75D)
                                    .build());

            assertThat(filtered).extracting(VectorRecord::id).containsExactly("a");
            assertThat(store.count(Map.of("tenantId", 1L))).isEqualTo(2);
            assertThat(store.count(Map.of("tenantId", "1"))).isZero();
            assertThat(store.count(Map.of("tenantId", 99L))).isZero();
            store.delete("b");
            store.flush();
        }

        try (VectorStore reopened = provider.open(options)) {
            assertThat(reopened.size()).isEqualTo(2);
            assertThat(reopened.search(new float[] {1F, 0F, 0F}, 3))
                    .extracting(VectorRecord::id)
                    .containsExactlyInAnyOrder("a", "c");
            reopened.deleteBatch(List.of("a", "c"));
        }

        try (VectorStore empty = provider.open(options)) {
            assertThat(empty.size()).isZero();
        }
    }

    @Test
    void countIgnoresMetadataWithoutAnIndexedVector() throws Exception {
        VectorStoreProperties properties = properties("jvector", 3, tempDir.resolve("stale"));
        VectorStoreProvider provider = new ConfiguredVectorStoreProvider(properties);
        Path indexDir = tempDir.resolve("stale-metadata");
        VectorStoreOptions options = VectorStoreOptions.of("knowledge/stale", 3, indexDir.toString());
        Path savedMetadata = tempDir.resolve("metadata-stale.dat");

        try (VectorStore store = provider.open(options)) {
            store.upsert(new VectorRecord("kept", new float[] {1F, 0F, 0F}, Map.of("kind", "kept")));
            store.upsert(new VectorRecord("removed", new float[] {0F, 1F, 0F}, Map.of("kind", "removed")));
            store.flush();
            Files.copy(indexDir.resolve("metadata.dat"), savedMetadata);
            store.delete("removed");
            store.flush();
        }

        Files.copy(savedMetadata, indexDir.resolve("metadata.dat"), StandardCopyOption.REPLACE_EXISTING);
        try (VectorStore reopened = provider.open(options)) {
            assertThat(reopened.size()).isEqualTo(1);
            assertThat(reopened.count(Map.of())).isEqualTo(1);
            assertThat(reopened.count(Map.of("kind", "removed"))).isZero();
        }
    }

    @Test
    void shouldIsolateNamespacesAndValidateDimensions() {
        VectorStoreProvider provider =
                new ConfiguredVectorStoreProvider(
                        properties("jvector", 2, tempDir.resolve("namespaces")));

        try (VectorStore first = provider.open(VectorStoreOptions.of("tenant/1", 2, null));
                VectorStore second = provider.open(VectorStoreOptions.of("tenant/2", 2, null))) {
            first.upsert(new VectorRecord("same-id", new float[] {1F, 0F}, Map.of()));
            second.upsert(new VectorRecord("other-id", new float[] {0F, 1F}, Map.of()));

            assertThat(first.search(new float[] {1F, 0F}, 5))
                    .extracting(VectorRecord::id)
                    .containsExactly("same-id");
            assertThat(second.search(new float[] {1F, 0F}, 5))
                    .extracting(VectorRecord::id)
                    .containsExactly("other-id");
            assertThatThrownBy(
                            () ->
                                    first.upsert(
                                            new VectorRecord(
                                                    "invalid", new float[] {1F}, Map.of())))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dimension mismatch");
            assertThatThrownBy(() -> second.search(new float[] {1F}, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dimension mismatch");
        }
    }

    @Test
    void shouldKeepScopedInMemoryStoreUntilProviderDropsNamespace() {
        VectorStoreProvider provider =
                new ConfiguredVectorStoreProvider(
                        properties("inmemory", 2, tempDir.resolve("unused")));
        VectorStoreOptions options = VectorStoreOptions.of("memory/tenant-1", 2, null);

        try (VectorStore store = provider.open(options)) {
            store.upsertBatch(
                    List.of(
                            new VectorRecord("m1", new float[] {1F, 0F}, Map.of("tenantId", 1L)),
                            new VectorRecord("m2", new float[] {0F, 1F}, Map.of("tenantId", 2L))));
            assertThat(store.count(Map.of("tenantId", 1L))).isEqualTo(1);
            assertThat(store.count(Map.of("tenantId", "1"))).isZero();
            assertThat(store.count(Map.of("tenantId", 99L))).isZero();
            assertThat(
                            store.search(
                                    VectorSearchRequest.builder()
                                            .queryVector(new float[] {1F, 0F})
                                            .topK(2)
                                            .searchType(VectorSearchType.EXACT)
                                            .filter(Map.of("tenantId", 1))
                                            .minScore(0.75D)
                                            .build()))
                    .extracting(VectorRecord::id)
                    .containsExactly("m1");
            assertThatThrownBy(
                            () ->
                                    store.upsert(
                                            new VectorRecord(
                                                    "invalid", new float[] {1F}, Map.of())))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dimension mismatch");
        }
        assertThat(provider.open(options).size()).isEqualTo(2);

        provider.drop(options);
        assertThat(provider.open(options).size()).isZero();
        provider.close();
    }

    private VectorStoreProperties properties(String type, int dimension, Path dataDir) {
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setType(type);
        properties.setDimension(dimension);
        properties.setDataDir(dataDir.toString());
        return properties;
    }
}
