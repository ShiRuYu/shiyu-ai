package com.shiyu.ai.governance.implementation.persistence;

import com.shiyu.ai.governance.contract.UsageSourceType;
import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcUsageLedgerTest {

    @Test
    void atomicallyDeduplicatesWithinTenant() {
        JdbcTemplate jdbc = initializedDatabase();
        AtomicInteger sequence = new AtomicInteger();
        JdbcUsageLedger ledger = new JdbcUsageLedger(jdbc, () -> "usage-" + sequence.incrementAndGet());
        UsageLedger.Entry entry = entry(new TenantId(5));

        assertTrue(ledger.insertIfAbsent(entry));
        assertFalse(ledger.insertIfAbsent(entry));
        assertEquals(1L, jdbc.queryForObject(
                "SELECT COUNT(*) FROM GOVERNANCE_USAGE_RECORD", Long.class));
    }

    @Test
    void sameSourceInAnotherTenantIsIndependent() {
        JdbcTemplate jdbc = initializedDatabase();
        JdbcUsageLedger ledger = new JdbcUsageLedger(jdbc, UUID::randomUUID);

        assertTrue(ledger.insertIfAbsent(entry(new TenantId(5))));
        assertTrue(ledger.insertIfAbsent(entry(new TenantId(6))));
    }

    @Test
    void infrastructureFailuresAreNotReportedAsDuplicates() {
        JdbcUsageLedger ledger = new JdbcUsageLedger(new JdbcTemplate(newDataSource()), UUID::randomUUID);

        assertThrows(RuntimeException.class, () -> ledger.insertIfAbsent(entry(new TenantId(5))));
    }

    private static JdbcTemplate initializedDatabase() {
        JdbcDataSource dataSource = newDataSource();
        new ResourceDatabasePopulator(new ClassPathResource(
                "db/baseline/h2/schema/governance/05_governance.sql")).execute(dataSource);
        return new JdbcTemplate(dataSource);
    }

    private static JdbcDataSource newDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:governance_" + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private static UsageLedger.Entry entry(TenantId tenantId) {
        return new UsageLedger.Entry(
                tenantId,
                new UserId(8),
                new CorrelationId("trace-9"),
                UsageSourceType.MODEL_INVOCATION,
                "model-call-9",
                20,
                4,
                new BigDecimal("0.0123"),
                Instant.parse("2026-08-23T12:10:00Z")
        );
    }
}
