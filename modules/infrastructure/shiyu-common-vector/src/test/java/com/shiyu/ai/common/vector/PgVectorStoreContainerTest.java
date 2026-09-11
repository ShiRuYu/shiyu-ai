package com.shiyu.ai.common.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shiyu.ai.common.vector.implementation.PgVectorStore;
import com.shiyu.ai.common.vector.model.VectorRecord;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

/** Runs only when Docker is available; CI executes this against real pgvector. */
@Testcontainers(disabledWithoutDocker = true)
class PgVectorStoreContainerTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    @Test
    void storesSearchesAndIsolatesNamespaces() {
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(
                        POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector");
        PgVectorStore tenantOne = new PgVectorStore(jdbc, "tenant/1", 3);
        PgVectorStore tenantTwo = new PgVectorStore(jdbc, "tenant/2", 3);

        tenantOne.upsert(new VectorRecord("doc-1", new float[] {1, 0, 0}, Map.of("tenant", "1")));
        tenantTwo.upsert(new VectorRecord("doc-1", new float[] {0, 1, 0}, Map.of("tenant", "2")));

        assertThat(tenantOne.size()).isEqualTo(1);
        assertThat(tenantOne.search(new float[] {1, 0, 0}, 5))
                .singleElement()
                .extracting(VectorRecord::id)
                .isEqualTo("doc-1");
        assertThat(tenantTwo.search(new float[] {1, 0, 0}, 5))
                .singleElement()
                .extracting(VectorRecord::id)
                .isEqualTo("doc-1");
        assertThatThrownBy(() -> tenantOne.search(new float[] {1, 0}, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dimension mismatch");
    }
}
