package com.shiyu.ai.common.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shiyu.ai.common.vector.implementation.PgVectorStore;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorSearchRequest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;
import java.util.UUID;
/** 在隔离 PostgreSQL 模式中验证 pgvector 维度、过滤和存储约束。 */

class PgVectorStoreLocalValidationTest {

    @Test
    void validatesPgVectorStoreInAnIsolatedTemporarySchema() {
        String password = System.getenv("PGPASSWORD");
        String database = System.getenv("SHIYU_PG_TEST_DATABASE");
        assumeTrue(
                password != null && !password.isBlank(),
                "PGPASSWORD must be supplied to the test process");
        assumeTrue(
                database != null && !database.isBlank(),
                "SHIYU_PG_TEST_DATABASE must name an isolated test database");

        String schema = "shiyu_pgvec_validation_" + UUID.randomUUID().toString().replace("-", "");
        JdbcTemplate adminJdbc = new JdbcTemplate(dataSource(password, null));
        Integer existingTables =
                adminJdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.tables "
                                + "WHERE table_schema NOT IN ('pg_catalog', 'information_schema') "
                                + "AND table_type = 'BASE TABLE'",
                        Integer.class);
        assertThat(existingTables)
                .as("the isolated test database must not contain application or seed tables")
                .isZero();

        boolean schemaCreated = false;
        try {
            Boolean vectorExtensionPresent =
                    adminJdbc.queryForObject(
                            "SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'vector')",
                            Boolean.class);
            assertThat(vectorExtensionPresent).isTrue();

            adminJdbc.execute("CREATE SCHEMA " + schema);
            schemaCreated = true;

            JdbcTemplate jdbc = new JdbcTemplate(dataSource(password, schema));
            PgVectorStore tenantOne = new PgVectorStore(jdbc, "tenant/1", 3);
            PgVectorStore tenantTwo = new PgVectorStore(jdbc, "tenant/2", 3);

            assertThat(tenantOne.type()).isEqualTo("pgvector");
            tenantOne.upsert(
                    new VectorRecord(
                            "doc-1", new float[] {1, 0, 0}, Map.of("tenant", "1", "revision", "initial")));
            tenantTwo.upsert(
                    new VectorRecord(
                            "doc-1", new float[] {0, 1, 0}, Map.of("tenant", "2", "revision", "initial")));

            assertThat(tenantOne.size()).isEqualTo(1);
            assertThat(tenantTwo.size()).isEqualTo(1);
            assertThat(tenantOne.count(Map.of("tenant", "1"))).isEqualTo(1);
            assertThat(tenantOne.count(Map.of("tenant", "2"))).isZero();
            assertThat(
                            tenantOne.search(
                                    VectorSearchRequest.builder()
                                            .queryVector(new float[] {1, 0, 0})
                                            .filter(Map.of("tenant", "1"))
                                            .topK(5)
                                            .build()))
                    .singleElement()
                    .satisfies(record -> assertThat(record.metadata().get("tenant")).isEqualTo("1"));
            assertThat(
                            tenantTwo.search(
                                    VectorSearchRequest.builder()
                                            .queryVector(new float[] {1, 0, 0})
                                            .filter(Map.of("tenant", "2"))
                                            .topK(5)
                                            .build()))
                    .singleElement()
                    .satisfies(record -> assertThat(record.metadata().get("tenant")).isEqualTo("2"));

            tenantOne.upsert(
                    new VectorRecord(
                            "doc-1", new float[] {0.9f, 0.1f, 0}, Map.of("tenant", "1", "revision", "updated")));
            assertThat(tenantOne.size()).isEqualTo(1);
            assertThat(
                            tenantOne.search(
                                    VectorSearchRequest.builder()
                                            .queryVector(new float[] {1, 0, 0})
                                            .filter(Map.of("revision", "updated"))
                                            .topK(5)
                                            .build()))
                    .singleElement()
                    .satisfies(record -> assertThat(record.metadata().get("revision")).isEqualTo("updated"));
            assertThatThrownBy(() -> tenantOne.search(new float[] {1, 0}, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dimension mismatch");

            tenantOne.delete("doc-1");
            assertThat(tenantOne.size()).isZero();
            assertThat(tenantTwo.size()).isEqualTo(1);
        } finally {
            if (schemaCreated) {
                adminJdbc.execute("DROP SCHEMA " + schema + " CASCADE");
            }
        }
    }

    private DriverManagerDataSource dataSource(String password, String currentSchema) {
        String database = System.getenv("SHIYU_PG_TEST_DATABASE");
        String url = "jdbc:postgresql://127.0.0.1:5433/" + database;
        if (currentSchema != null) {
            url += "?currentSchema=" + currentSchema + ",public";
        }
        return new DriverManagerDataSource(url, "postgres", password);
    }
}
