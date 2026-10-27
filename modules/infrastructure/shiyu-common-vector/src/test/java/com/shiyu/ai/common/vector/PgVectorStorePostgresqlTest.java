package com.shiyu.ai.common.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shiyu.ai.common.vector.implementation.InMemoryVectorStore;
import com.shiyu.ai.common.vector.implementation.PgVectorStore;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorSearchRequest;
import com.shiyu.ai.common.vector.model.VectorSearchType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 验证真实 PostgreSQL 中的向量分数、索引计划、维度和精确搜索契约。 */
@EnabledIfEnvironmentVariable(named = "SHIYU_PG_TEST_URL", matches = ".+")
class PgVectorStorePostgresqlTest {
    private JdbcTemplate admin;
    private JdbcTemplate jdbc;
    private String schema;

    @BeforeEach
    void createIsolatedSchema() {
        String url = System.getenv("SHIYU_PG_TEST_URL");
        String username = System.getenv().getOrDefault("SHIYU_PG_TEST_USERNAME", "postgres");
        String password = System.getenv().getOrDefault("SHIYU_PG_TEST_PASSWORD", "");
        admin = new JdbcTemplate(new DriverManagerDataSource(url, username, password));
        schema = "vector_contract_" + UUID.randomUUID().toString().replace("-", "");
        admin.execute("CREATE SCHEMA " + schema);
        String scopedUrl = url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema + ",public";
        jdbc = new JdbcTemplate(new DriverManagerDataSource(scopedUrl, username, password));
    }

    @AfterEach
    void removeIsolatedSchema() {
        if (admin != null && schema != null) admin.execute("DROP SCHEMA " + schema + " CASCADE");
    }

    @Test
    void preservesNormalizedScoresAndThresholdsAcrossProviders() {
        PgVectorStore store = new PgVectorStore(jdbc, "scores", 3);
        InMemoryVectorStore reference = new InMemoryVectorStore(3);
        List<VectorRecord> records = List.of(
                new VectorRecord("same", new float[] {1, 0, 0}, Map.of()),
                new VectorRecord("orthogonal", new float[] {0, 1, 0}, Map.of()),
                new VectorRecord("opposite", new float[] {-1, 0, 0}, Map.of()));
        store.upsertBatch(records);
        reference.upsertBatch(records);
        VectorSearchRequest request = VectorSearchRequest.builder()
                .queryVector(new float[] {1, 0, 0}).topK(5).minScore(0.4)
                .searchType(VectorSearchType.EXACT).build();
        assertThat(store.search(request)).extracting(VectorRecord::id)
                .containsExactlyElementsOf(reference.search(request).stream().map(VectorRecord::id).toList());
        List<String> exactSql = new java.util.ArrayList<>();
        JdbcTemplate exactTracing = new JdbcTemplate(jdbc.getDataSource()) {
            @Override
            public <T> List<T> query(String sql, org.springframework.jdbc.core.PreparedStatementSetter setter,
                    org.springframework.jdbc.core.RowMapper<T> mapper) {
                exactSql.add(sql);
                return super.query(sql, setter, mapper);
            }
        };
        new PgVectorStore(exactTracing, "scores", 3).search(request);
        assertThat(exactSql.getFirst()).contains("ORDER BY score DESC");
        assertThat(((Number) store.search(request).get(1).metadata().get("_score")).doubleValue())
                .isEqualTo(0.5);
    }

    @Test
    void supportsHighDimensionsWithoutAnUnsupportedHnswIndex() {
        PgVectorStore store = new PgVectorStore(jdbc, "large-model", 3072);
        float[] vector = new float[3072];
        vector[0] = 1;
        store.upsert(new VectorRecord("large", vector, Map.of()));
        assertThat(store.search(vector, 5)).extracting(VectorRecord::id).containsExactly("large");
        assertThatThrownBy(() -> new PgVectorStore(jdbc, "too-large", 16001))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("16000");
    }

    @Test
    void exactSearchEnumeratesAllMatchesEvenWhenHnswIsPreferred() {
        PgVectorStore store = new PgVectorStore(jdbc, "exact", 3);
        for (int i = 0; i < 120; i++) {
            store.upsert(new VectorRecord("v" + i, new float[] {1, i / 120f, 0}, Map.of("tenant", 1)));
        }
        // 每条连接都禁用顺序扫描，确保 EXACT 不依赖优化器偶然选择全表扫描。
        DriverManagerDataSource source = (DriverManagerDataSource) jdbc.getDataSource();
        source.setUrl(source.getUrl() + "&options=-c%20enable_seqscan%3Doff");
        assertThat(store.search(VectorSearchRequest.builder().queryVector(new float[] {1, 0, 0})
                .topK(120).filter(Map.of("tenant", 1)).searchType(VectorSearchType.EXACT).build()))
                .hasSize(120);
    }

    @Test
    void annSearchContinuesScanningPastOtherNamespaces() {
        PgVectorStore store = new PgVectorStore(jdbc, "minority", 3);
        jdbc.execute("INSERT INTO shiyu_vector_item (vector_namespace, vector_id, dimension, embedding) "
                + "SELECT 'majority', id::text, 3, '[1,0,0]'::vector FROM generate_series(1, 1000) id");
        jdbc.execute("INSERT INTO shiyu_vector_item (vector_namespace, vector_id, dimension, embedding) "
                + "SELECT 'minority', id::text, 3, '[0.9,0.1,0]'::vector FROM generate_series(1, 20) id");
        DriverManagerDataSource source = (DriverManagerDataSource) jdbc.getDataSource();
        source.setUrl(source.getUrl() + "&options=-c%20enable_seqscan%3Doff");
        assertThat(store.search(new float[] {1, 0, 0}, 10)).hasSize(10);
    }

    @Test
    void opensNewNamespacesConcurrentlyWithoutSchemaRaces() throws Exception {
        try (var workers = java.util.concurrent.Executors.newFixedThreadPool(4)) {
            var ready = new java.util.concurrent.CountDownLatch(4);
            var start = new java.util.concurrent.CountDownLatch(1);
            List<java.util.concurrent.Future<Integer>> results = new java.util.ArrayList<>();
            for (int i = 0; i < 4; i++) {
                String namespace = "parallel/" + i;
                results.add(workers.submit(() -> {
                    ready.countDown();
                    start.await();
                    return new PgVectorStore(jdbc, namespace, 3).size();
                }));
            }
            assertThat(ready.await(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            start.countDown();
            for (var result : results) assertThat(result.get(20, java.util.concurrent.TimeUnit.SECONDS)).isZero();
        }
    }

    @Test
    void annQueryCanUseThePartialIndexWithMixedDimensions() {
        PgVectorStore store = new PgVectorStore(jdbc, "indexed", 3);
        store.upsert(new VectorRecord("first", new float[] {1, 0, 0}, Map.of()));
        new PgVectorStore(jdbc, "other-dimension", 2)
                .upsert(new VectorRecord("other", new float[] {0, 1}, Map.of()));
        // 捕获实际搜索 SQL，而不是重新手写一条可能与实现不同的 EXPLAIN 查询。
        List<String> statements = new java.util.ArrayList<>();
        JdbcTemplate tracing = new JdbcTemplate(jdbc.getDataSource()) {
            @Override
            public <T> List<T> query(String sql, org.springframework.jdbc.core.PreparedStatementSetter setter,
                    org.springframework.jdbc.core.RowMapper<T> mapper) {
                statements.add(sql);
                return super.query(sql, setter, mapper);
            }
        };
        PgVectorStore traced = new PgVectorStore(tracing, "indexed", 3);
        traced.search(new float[] {1, 0, 0}, 5);
        String search = statements.getFirst();
        assertThat(search).contains("ORDER BY embedding::vector(3) <=> CAST(? AS vector(3))");
        List<String> plan = jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<List<String>>) connection -> {
            try (var statement = connection.createStatement()) {
                statement.execute("SET enable_seqscan = off");
                statement.execute("SET enable_bitmapscan = off");
            }
            try (var statement = connection.prepareStatement("EXPLAIN " + search)) {
                int parameter = 1;
                statement.setString(parameter++, "[1,0,0]");
                statement.setString(parameter++, "indexed");
                // 当前实现和修复均保留 namespace 参数；其余维度条件应为固定安全整数。
                statement.setString(parameter++, "[1,0,0]");
                statement.setDouble(parameter++, 0);
                statement.setString(parameter++, "[1,0,0]");
                statement.setInt(parameter, 5);
                try (var rows = statement.executeQuery()) {
                    List<String> lines = new java.util.ArrayList<>();
                    while (rows.next()) lines.add(rows.getString(1));
                    return lines;
                }
            }
        });
        assertThat(String.join("\n", plan)).contains("shiyu_vector_item_embedding_d3");
    }
}
