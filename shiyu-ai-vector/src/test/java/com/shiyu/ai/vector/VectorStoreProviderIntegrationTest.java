package com.shiyu.ai.vector;

import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.factory.ConfiguredVectorStoreProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorStoreProviderIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldPersistFilterDeleteAndReopenThroughPublicProvider() {
        VectorStoreProperties properties = properties("jvector", 3, tempDir.resolve("defaults"));
        VectorStoreProvider provider = new ConfiguredVectorStoreProvider(properties);
        Path indexDir = tempDir.resolve("knowledge-index");
        VectorStoreOptions options = VectorStoreOptions.of("knowledge/1/2/3", 3, indexDir.toString());

        try (VectorStore store = provider.open(options)) {
            store.upsertBatch(List.of(
                    new VectorRecord("a", new float[]{1F, 0F, 0F}, Map.of("tenantId", 1L)),
                    new VectorRecord("b", new float[]{0.9F, 0.1F, 0F}, Map.of("tenantId", 2L)),
                    new VectorRecord("c", new float[]{0F, 1F, 0F}, Map.of("tenantId", 1L))));

            List<VectorRecord> filtered = store.search(VectorSearchRequest.builder()
                    .queryVector(new float[]{1F, 0F, 0F})
                    .topK(2)
                    .filter(Map.of("tenantId", 1))
                    .minScore(0.75D)
                    .build());

            assertThat(filtered).extracting(VectorRecord::id).containsExactly("a");
            store.delete("b");
            store.flush();
        }

        try (VectorStore reopened = provider.open(options)) {
            assertThat(reopened.size()).isEqualTo(2);
            assertThat(reopened.search(new float[]{1F, 0F, 0F}, 3))
                    .extracting(VectorRecord::id)
                    .containsExactlyInAnyOrder("a", "c");
            reopened.deleteBatch(List.of("a", "c"));
        }

        try (VectorStore empty = provider.open(options)) {
            assertThat(empty.size()).isZero();
        }
    }

    @Test
    void shouldIsolateNamespacesAndValidateDimensions() {
        VectorStoreProvider provider = new ConfiguredVectorStoreProvider(
                properties("jvector", 2, tempDir.resolve("namespaces")));

        try (VectorStore first = provider.open(VectorStoreOptions.of("tenant/1", 2, null));
             VectorStore second = provider.open(VectorStoreOptions.of("tenant/2", 2, null))) {
            first.upsert(new VectorRecord("same-id", new float[]{1F, 0F}, Map.of()));
            second.upsert(new VectorRecord("other-id", new float[]{0F, 1F}, Map.of()));

            assertThat(first.search(new float[]{1F, 0F}, 5))
                    .extracting(VectorRecord::id).containsExactly("same-id");
            assertThat(second.search(new float[]{1F, 0F}, 5))
                    .extracting(VectorRecord::id).containsExactly("other-id");
            assertThatThrownBy(() -> first.upsert(
                    new VectorRecord("invalid", new float[]{1F}, Map.of())))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dimension mismatch");
            assertThatThrownBy(() -> second.search(new float[]{1F}, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dimension mismatch");
        }
    }

    @Test
    void shouldKeepScopedInMemoryStoreUntilProviderDropsNamespace() {
        VectorStoreProvider provider = new ConfiguredVectorStoreProvider(
                properties("inmemory", 2, tempDir.resolve("unused")));
        VectorStoreOptions options = VectorStoreOptions.of("memory/tenant-1", 2, null);

        try (VectorStore store = provider.open(options)) {
            store.upsertBatch(List.of(
                    new VectorRecord("m1", new float[]{1F, 0F}, Map.of("tenantId", 1L)),
                    new VectorRecord("m2", new float[]{0F, 1F}, Map.of("tenantId", 2L))));
            assertThat(store.search(VectorSearchRequest.builder()
                    .queryVector(new float[]{1F, 0F})
                    .topK(2)
                    .searchType(VectorSearchType.EXACT)
                    .filter(Map.of("tenantId", 1))
                    .minScore(0.75D)
                    .build())).extracting(VectorRecord::id).containsExactly("m1");
            assertThatThrownBy(() -> store.upsert(
                    new VectorRecord("invalid", new float[]{1F}, Map.of())))
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
