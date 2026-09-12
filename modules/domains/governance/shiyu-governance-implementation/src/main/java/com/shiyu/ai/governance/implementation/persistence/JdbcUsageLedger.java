package com.shiyu.ai.governance.implementation.persistence;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 通过 JDBC 持久化用量台账并支持幂等写入。
 */
public final class JdbcUsageLedger implements UsageLedger {

    /**
     * INSERT_SQL 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final String INSERT_SQL =
            """
            INSERT INTO GOVERNANCE_USAGE_RECORD
                (ID, TENANT_ID, USER_ID, CORRELATION_ID, SOURCE_TYPE, SOURCE_ID,
                 INPUT_TOKENS, OUTPUT_TOKENS, COST, OCCURRED_AT)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;
    /**
     * idSupplier 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Supplier<?> idSupplier;

    /**
     * {@code JdbcUsageLedger} 创建并初始化当前类型实例。
     *
     * @param jdbc 参数值，用于执行当前操作。
     */
    public JdbcUsageLedger(JdbcTemplate jdbc) {
        this(jdbc, UUID::randomUUID);
    }

    JdbcUsageLedger(JdbcTemplate jdbc, Supplier<?> idSupplier) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc must not be null");
        this.idSupplier = Objects.requireNonNull(idSupplier, "idSupplier must not be null");
    }

    /**
     * {@code insertIfAbsent} 执行当前类型定义的业务操作。
     *
     * @param entry 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
                    Timestamp.from(entry.occurredAt()));
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }
}
