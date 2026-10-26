package com.shiyu.ai.common.vector;

import static org.assertj.core.api.Assertions.assertThat;

import com.shiyu.ai.common.vector.implementation.PgVectorStore;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorSearchRequest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;
import java.util.UUID;

class PgVectorStoreLocalCrudSmokeTest {

    @Test
    void performsCrudAndMetadataSearchInTemporarySchema() {
        String password = System.getenv("SHIYU_PG_TEST_PASSWORD");
        assumeTrue(password != null && !password.isBlank(), "SHIYU_PG_TEST_PASSWORD is required");
        String schema = "shiyu_pgvec_smoke_" + UUID.randomUUID().toString().replace("-", "");
        JdbcTemplate adminJdbc = new JdbcTemplate(dataSource(password, null));
        adminJdbc.execute("CREATE SCHEMA " + schema);
        try {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource(password, schema));
            PgVectorStore store = new PgVectorStore(jdbc, "temporary-smoke", 3);
            store.upsert(
                    new VectorRecord("smoke-doc", new float[] {1, 0, 0}, Map.of("kind", "smoke")));
            assertThat(store.size()).isEqualTo(1);
            assertThat(store.count(Map.of("kind", "smoke"))).isEqualTo(1);
            assertThat(
                            store.search(
                                            VectorSearchRequest.builder()
                                                    .queryVector(new float[] {1, 0, 0})
                                                    .filter(Map.of("kind", "smoke"))
                                                    .topK(5)
                                                    .build()))
                    .singleElement()
                    .satisfies(record -> assertThat(record.id()).isEqualTo("smoke-doc"));

            store.upsert(
                    new VectorRecord(
                            "smoke-doc", new float[] {0.9f, 0.1f, 0}, Map.of("kind", "updated")));
            assertThat(store.count(Map.of("kind", "smoke"))).isZero();
            assertThat(store.count(Map.of("kind", "updated"))).isEqualTo(1);
            store.delete("smoke-doc");
            assertThat(store.size()).isZero();
        } finally {
            adminJdbc.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }

    private DriverManagerDataSource dataSource(String password, String currentSchema) {
        String url = "jdbc:postgresql://localhost:5433/postgres";
        if (currentSchema != null) {
            url += "?currentSchema=" + currentSchema + ",public";
        }
        return new DriverManagerDataSource(url, "postgres", password);
    }
}
