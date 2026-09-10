package com.shiyu.ai.common.core.jdbc;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcDialectTest {

    @Test
    void rendersVendorSpecificUpsertStatements() {
        List<String> columns = List.of("ID", "VALUE");
        List<String> keys = List.of("ID");
        List<String> updates = List.of("VALUE");

        assertThat(JdbcDialect.fromProduct("PostgreSQL").upsert("example", columns, "?, ?", keys, updates))
                .contains("ON CONFLICT (ID) DO UPDATE SET VALUE=EXCLUDED.VALUE");
        assertThat(JdbcDialect.fromProduct("MySQL").upsert("example", columns, "?, ?", keys, updates))
                .contains("ON DUPLICATE KEY UPDATE VALUE=VALUES(VALUE)");
        assertThat(JdbcDialect.fromProduct("H2").upsert("example", columns, "?, ?", keys, updates))
                .startsWith("MERGE INTO example");
    }

    @Test
    void rejectsUntrustedIdentifiers() {
        assertThatThrownBy(() -> JdbcDialect.fromProduct("PostgreSQL")
                .upsert("example;drop", List.of("ID"), "?", List.of("ID"), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rendersRetryTimestampForEachDatabase() {
        assertThat(JdbcDialect.fromProduct("PostgreSQL").retryTimestampExpression("ATTEMPTS"))
                .contains("INTERVAL '1 second'");
        assertThat(JdbcDialect.fromProduct("MySQL").retryTimestampExpression("ATTEMPTS"))
                .contains("DATE_ADD");
        assertThat(JdbcDialect.fromProduct("H2").retryTimestampExpression("ATTEMPTS"))
                .contains("DATEADD");
    }
}
