package com.shiyu.ai.vector.impl;

import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.VectorRecord;
import com.shiyu.ai.vector.VectorSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JVectorStore 单元测试
 *
 * <p>使用 {@code @TempDir} 隔离持久化目录，避免测试间干扰。</p>
 */
@Tag("dev")
class JVectorStoreTest {

    @TempDir
    Path tempDir;

    private JVectorStore store;

    @BeforeEach
    void setUp() {
        VectorStoreProperties props = new VectorStoreProperties();
        props.setDimension(4);
        props.setDataDir(tempDir.toString());
        store = new JVectorStore(props);
    }

    @Test
    void testUpsertAndSize() {
        store.upsert(new VectorRecord("1", new float[]{1, 0, 0, 0}, Map.of("type", "a")));
        store.upsert(new VectorRecord("2", new float[]{0, 1, 0, 0}, Map.of("type", "b")));

        assertEquals(2, store.size());
    }

    @Test
    void testUpsertOverwrite() {
        store.upsert(new VectorRecord("1", new float[]{1, 0, 0, 0}, Map.of("type", "a")));
        store.upsert(new VectorRecord("1", new float[]{0.5f, 0.5f, 0, 0}, Map.of("type", "a-updated")));

        assertEquals(1, store.size());
    }

    @Test
    void testSearchReturnsTopK() {
        store.upsert(new VectorRecord("1", new float[]{1, 0, 0, 0}, Map.of()));
        store.upsert(new VectorRecord("2", new float[]{0, 1, 0, 0}, Map.of()));
        store.upsert(new VectorRecord("3", new float[]{0.9f, 0.1f, 0, 0}, Map.of()));

        List<VectorRecord> results = store.search(new float[]{1, 0, 0, 0}, 2);

        assertEquals(2, results.size());
        assertEquals("1", results.get(0).id());
    }

    @Test
    void testSimilarVectorsRankHigher() {
        store.upsert(new VectorRecord("far", new float[]{-1, 0, 0, 0}, Map.of()));
        store.upsert(new VectorRecord("close", new float[]{0.9f, 0.1f, 0, 0}, Map.of()));
        store.upsert(new VectorRecord("exact", new float[]{1, 0, 0, 0}, Map.of()));

        List<VectorRecord> results = store.search(new float[]{1, 0, 0, 0}, 3);

        assertEquals("exact", results.get(0).id());
        assertEquals("close", results.get(1).id());
        assertEquals("far", results.get(2).id());
    }

    @Test
    void testSearchWithScore() {
        store.upsert(new VectorRecord("1", new float[]{1, 0, 0, 0}, Map.of("type", "a")));

        List<VectorRecord> results = store.search(new float[]{1, 0, 0, 0}, 10);

        assertFalse(results.isEmpty());
        assertTrue(results.get(0).metadata().containsKey("_score"));
    }

    @Test
    void testSearchWithMinScore() {
        store.upsert(new VectorRecord("1", new float[]{1, 0, 0, 0}, Map.of()));
        store.upsert(new VectorRecord("2", new float[]{0, 1, 0, 0}, Map.of()));

        VectorSearchRequest request = VectorSearchRequest.builder()
                .queryVector(new float[]{1, 0, 0, 0})
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
        store.upsert(new VectorRecord("1", new float[]{1, 0, 0, 0}, Map.of("type", "a")));
        store.upsert(new VectorRecord("2", new float[]{0.9f, 0.1f, 0, 0}, Map.of("type", "b")));
        store.upsert(new VectorRecord("3", new float[]{0.8f, 0.2f, 0, 0}, Map.of("type", "a")));

        VectorSearchRequest request = VectorSearchRequest.builder()
                .queryVector(new float[]{1, 0, 0, 0})
                .topK(10)
                .minScore(0.0)
                .filter(Map.of("type", "a"))
                .build();

        List<VectorRecord> results = store.search(request);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> "a".equals(r.metadata().get("type"))));
    }

    @Test
    void testFilterWithNumericTypeMismatch() {
        // metadata 存 Long, filter 传 Integer — 验证类型兼容
        store.upsert(new VectorRecord("1", new float[]{1, 0, 0, 0}, Map.of("userId", Long.valueOf(42L))));

        VectorSearchRequest request = VectorSearchRequest.builder()
                .queryVector(new float[]{1, 0, 0, 0})
                .topK(10)
                .filter(Map.of("userId", Integer.valueOf(42)))
                .build();

        List<VectorRecord> results = store.search(request);
        assertEquals(1, results.size());
    }

    @Test
    void testDelete() {
        store.upsert(new VectorRecord("1", new float[]{1, 0, 0, 0}, Map.of()));
        store.upsert(new VectorRecord("2", new float[]{0, 1, 0, 0}, Map.of()));

        store.delete("1");

        assertEquals(1, store.size());
        assertTrue(store.search(new float[]{1, 0, 0, 0}, 10).stream().noneMatch(r -> r.id().equals("1")));
    }

    @Test
    void testRebuild() {
        store.upsert(new VectorRecord("1", new float[]{1, 0, 0, 0}, Map.of()));
        store.upsert(new VectorRecord("2", new float[]{0, 1, 0, 0}, Map.of()));

        store.rebuild();

        assertEquals(0, store.size());
    }

    @Test
    void testEmptyStore() {
        List<VectorRecord> results = store.search(new float[]{1, 0, 0, 0}, 10);
        assertTrue(results.isEmpty());
        assertEquals(0, store.size());
    }

    @Test
    void testUpsertBatch() {
        store.upsertBatch(List.of(
                new VectorRecord("1", new float[]{1, 0, 0, 0}, Map.of()),
                new VectorRecord("2", new float[]{0, 1, 0, 0}, Map.of())
        ));

        assertEquals(2, store.size());
    }

    @Test
    void testPersistence() {
        // 写入
        store.upsert(new VectorRecord("p1", new float[]{1, 0, 0, 0}, Map.of("lang", "java")));
        store.upsert(new VectorRecord("p2", new float[]{0, 1, 0, 0}, Map.of("lang", "python")));
        store.saveToDisk();

        // 重建 Store 从磁盘加载
        VectorStoreProperties props = new VectorStoreProperties();
        props.setDimension(4);
        props.setDataDir(tempDir.toString());
        JVectorStore loaded = new JVectorStore(props);

        assertEquals(2, loaded.size());

        List<VectorRecord> results = loaded.search(new float[]{1, 0, 0, 0}, 10);
        assertFalse(results.isEmpty());
        assertEquals("p1", results.get(0).id());

        // 验证 metadata 持久化
        assertEquals("java", results.get(0).metadata().get("lang"));
        assertTrue(results.get(0).metadata().containsKey("_score"));
    }

    @Test
    void testPersistenceWithMetadataTypes() {
        // 验证多种 metadata 类型的持久化
        store.upsert(new VectorRecord("m1", new float[]{1, 0, 0, 0},
                Map.of("name", "test", "count", 42, "ratio", 0.95, "flag", true)));

        assertDoesNotThrow(() -> store.saveToDisk());

        VectorStoreProperties props = new VectorStoreProperties();
        props.setDimension(4);
        props.setDataDir(tempDir.toString());
        JVectorStore loaded = new JVectorStore(props);

        assertEquals(1, loaded.size());
        var results = loaded.search(new float[]{1, 0, 0, 0}, 1);
        assertEquals("test", results.get(0).metadata().get("name"));
        assertEquals(42, results.get(0).metadata().get("count"));
    }

    @Test
    void testMetadataPreservedAfterSearch() {
        store.upsert(new VectorRecord("meta1", new float[]{1, 0, 0, 0},
                Map.of("category", "math", "importance", 0.9)));

        List<VectorRecord> results = store.search(new float[]{1, 0, 0, 0}, 5);

        assertEquals(1, results.size());
        assertEquals("math", results.get(0).metadata().get("category"));
        assertEquals(0.9, ((Number) results.get(0).metadata().get("importance")).doubleValue(), 0.01);
    }

    @Test
    void testDeleteAndReUpsertRecyclesOrdinal() {
        store.upsert(new VectorRecord("a", new float[]{1, 0, 0, 0}, Map.of()));
        store.upsert(new VectorRecord("b", new float[]{0, 1, 0, 0}, Map.of()));
        assertEquals(2, store.size());

        store.delete("a");
        assertEquals(1, store.size());

        // 复用 ordinal
        store.upsert(new VectorRecord("c", new float[]{0, 0, 1, 0}, Map.of()));
        assertEquals(2, store.size());

        // 验证搜索正确
        List<VectorRecord> results = store.search(new float[]{0, 0, 1, 0}, 5);
        assertEquals("c", results.get(0).id());
    }
}
