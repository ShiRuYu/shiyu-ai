package com.shiyu.ai.governance.implementation.persistence;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/** JDBC adapter relying on the database unique constraint for concurrency safety. */
public final class JdbcUsageLedger implements UsageLedger {

    private static final String INSERT_SQL = """
            INSERT INTO GOVERNANCE_USAGE_RECORD
                (ID, TENANT_ID, USER_ID, CORRELATION_ID, SOURCE_TYPE, SOURCE_ID,
                 INPUT_TOKENS, OUTPUT_TOKENS, COST, OCCURRED_AT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbc;
    private final Supplier<?> idSupplier;

    public JdbcUsageLedger(JdbcTemplate jdbc) {
        this(jdbc, UUID::randomUUID);
    }

    JdbcUsageLedger(JdbcTemplate jdbc, Supplier<?> idSupplier) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc must not be null");
        this.idSupplier = Objects.requireNonNull(idSupplier, "idSupplier must not be null");
    }

    @Override
    public boolean insertIfAbsent(Entry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        try {
            jdbc.update(
                    INSERT_SQL,
                    String.valueOf(idSupplier.get()),
                    entry.tenantId().value(),
                    entry.userId().value(),
                    entry.correlationId().value(),
                    entry.sourceType().name(),
                    entry.sourceId(),
                    entry.inputTokens(),
                    entry.outputTokens(),
                    entry.cost(),
                    Timestamp.from(entry.occurredAt())
            );
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }
}
