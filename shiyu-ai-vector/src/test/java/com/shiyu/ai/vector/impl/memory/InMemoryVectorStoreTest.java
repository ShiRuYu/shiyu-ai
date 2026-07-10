package com.shiyu.ai.vector.impl.memory;
import org.junit.jupiter.api.Tag;

import com.shiyu.ai.vector.spi.VectorRecord;
import com.shiyu.ai.vector.spi.VectorSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InMemoryVectorStore 单元测试
 */
@Tag("dev")
class InMemoryVectorStoreTest {

    private InMemoryVectorStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryVectorStore();
    }

    @Test
    void testUpsertAndSize() {
        store.upsert(new VectorRecord("1", new float[]{1.0f, 0.0f}, Map.of("type", "a")));
        store.upsert(new VectorRecord("2", new float[]{0.0f, 1.0f}, Map.of("type", "b")));

        assertEquals(2, store.size());
    }

    @Test
    void testUpsertOverwrite() {
        store.upsert(new VectorRecord("1", new float[]{1.0f, 0.0f}, Map.of("type", "a")));
        store.upsert(new VectorRecord("1", new float[]{0.5f, 0.5f}, Map.of("type", "a-updated")));

        assertEquals(1, store.size());
    }

    @Test
    void testSearchReturnsTopK() {
        store.upsert(new VectorRecord("1", new float[]{1.0f, 0.0f}, Map.of("type", "a")));
        store.upsert(new VectorRecord("2", new float[]{0.0f, 1.0f}, Map.of("type", "b")));
        store.upsert(new VectorRecord("3", new float[]{0.9f, 0.1f}, Map.of("type", "c")));

        List<VectorRecord> results = store.search(new float[]{1.0f, 0.0f}, 2);

        assertEquals(2, results.size());
        assertEquals("1", results.get(0).id());
    }

    @Test
    void testSearchWithScore() {
        store.upsert(new VectorRecord("1", new float[]{1.0f, 0.0f}, Map.of("type", "a")));
        store.upsert(new VectorRecord("2", new float[]{0.0f, 1.0f}, Map.of("type", "b")));

        List<VectorRecord> results = store.search(new float[]{1.0f, 0.0f}, 10);

        assertTrue(results.size() >= 2);
        for (VectorRecord r : results) {
            assertTrue(r.metadata().containsKey("_score"));
            double score = ((Number) r.metadata().get("_score")).doubleValue();
            assertTrue(score >= -1.0 && score <= 1.0);
        }
        assertEquals("1", results.get(0).id());
    }

    @Test
    void testSearchWithMinScore() {
        store.upsert(new VectorRecord("1", new float[]{1.0f, 0.0f}, Map.of("type", "a")));
        store.upsert(new VectorRecord("2", new float[]{0.0f, 1.0f}, Map.of("type", "b")));

        VectorSearchRequest request = VectorSearchRequest.builder()
            .queryVector(new float[]{1.0f, 0.0f})
            .topK(10)
            .minScore(0.9)
            .build();

        List<VectorRecord> results = store.search(request);

        assertFalse(results.isEmpty());
        for (VectorRecord r : results) {
            double score = ((Number) r.metadata().get("_score")).doubleValue();
            assertTrue(score >= 0.9, "Score " + score + " should be >= 0.9");
        }
    }

    @Test
    void testSearchWithFilter() {
        store.upsert(new VectorRecord("1", new float[]{1.0f, 0.0f}, Map.of("type", "a")));
        store.upsert(new VectorRecord("2", new float[]{0.9f, 0.1f}, Map.of("type", "b")));
        store.upsert(new VectorRecord("3", new float[]{0.8f, 0.2f}, Map.of("type", "a")));

        VectorSearchRequest request = VectorSearchRequest.builder()
            .queryVector(new float[]{1.0f, 0.0f})
            .topK(10)
            .minScore(0.0)
            .filter(Map.of("type", "a"))
            .build();

        List<VectorRecord> results = store.search(request);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> "a".equals(r.metadata().get("type"))));
    }

    @Test
    void testDelete() {
        store.upsert(new VectorRecord("1", new float[]{1.0f, 0.0f}, Map.of()));
        store.upsert(new VectorRecord("2", new float[]{0.0f, 1.0f}, Map.of()));

        store.delete("1");

        assertEquals(1, store.size());
    }

    @Test
    void testDeleteBatch() {
        store.upsert(new VectorRecord("1", new float[]{1.0f, 0.0f}, Map.of()));
        store.upsert(new VectorRecord("2", new float[]{0.0f, 1.0f}, Map.of()));
        store.upsert(new VectorRecord("3", new float[]{0.5f, 0.5f}, Map.of()));

        store.deleteBatch(List.of("1", "3"));

        assertEquals(1, store.size());
        assertNull(store.search(new float[]{0.0f, 1.0f}, 1).stream().filter(r -> r.id().equals("1")).findFirst().orElse(null));
    }

    @Test
    void testUpsertBatch() {
        store.upsertBatch(List.of(
            new VectorRecord("1", new float[]{1.0f, 0.0f}, Map.of()),
            new VectorRecord("2", new float[]{0.0f, 1.0f}, Map.of())
        ));

        assertEquals(2, store.size());
    }

    @Test
    void testRebuild() {
        store.upsert(new VectorRecord("1", new float[]{1.0f, 0.0f}, Map.of()));
        store.upsert(new VectorRecord("2", new float[]{0.0f, 1.0f}, Map.of()));

        store.rebuild();

        assertEquals(0, store.size());
    }

    @Test
    void testEmptyStore() {
        List<VectorRecord> results = store.search(new float[]{1.0f, 0.0f}, 10);

        assertTrue(results.isEmpty());
        assertEquals(0, store.size());
    }

    @Test
    void testSimilarVectorsRankHigher() {
        store.upsert(new VectorRecord("far", new float[]{-1.0f, 0.0f}, Map.of()));
        store.upsert(new VectorRecord("close", new float[]{0.9f, 0.1f}, Map.of()));
        store.upsert(new VectorRecord("exact", new float[]{1.0f, 0.0f}, Map.of()));

        List<VectorRecord> results = store.search(new float[]{1.0f, 0.0f}, 3);

        assertEquals("exact", results.get(0).id());
        assertEquals("close", results.get(1).id());
        assertEquals("far", results.get(2).id());
    }

    @Test
    void testSearchWithBuilder() {
        store.upsert(new VectorRecord("1", new float[]{1.0f, 0.0f}, Map.of()));

        VectorSearchRequest request = VectorSearchRequest.builder()
            .queryVector(new float[]{1.0f, 0.0f})
            .topK(5)
            .minScore(0.0)
            .build();

        List<VectorRecord> results = store.search(request);
        assertEquals(1, results.size());
    }
}
