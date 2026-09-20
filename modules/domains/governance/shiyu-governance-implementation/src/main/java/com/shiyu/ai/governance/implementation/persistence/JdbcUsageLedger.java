package com.shiyu.ai.governance.implementation.persistence;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.shiyu.ai.kernel.context.TenantScope;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 实现 Jdbc 用量 Ledger 相关的业务处理、协作逻辑或基础设施能力。
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
     * 执行 Jdbc 用量 Ledger 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param jdbc 用于完成本次业务处理的 jdbc 参数。
     */
    public JdbcUsageLedger(JdbcTemplate jdbc) {
        this(jdbc, UUID::randomUUID);
    }

    JdbcUsageLedger(JdbcTemplate jdbc, Supplier<?> idSupplier) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc must not be null");
        this.idSupplier = Objects.requireNonNull(idSupplier, "idSupplier must not be null");
    }

    /**
     * 创建或保存 Jdbc 用量 Ledger 相关业务数据，并返回处理结果。
     *
     * @param entry 用于完成本次业务处理的 entry 参数。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean insertIfAbsent(Entry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        TenantScope.requireMatches(entry.tenantId());
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
