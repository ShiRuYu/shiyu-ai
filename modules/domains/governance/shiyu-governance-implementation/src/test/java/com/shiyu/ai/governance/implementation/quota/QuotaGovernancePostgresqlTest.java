package com.shiyu.ai.governance.implementation.quota;

import static org.junit.jupiter.api.Assertions.*;

import com.shiyu.ai.governance.contract.QuotaRequest;
import com.shiyu.ai.governance.contract.QuotaUsage;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Properties;
import java.util.UUID;

/** 验证真实 PostgreSQL 上的配额预留、释放和结算事务。 */
@EnabledIfEnvironmentVariable(named = "SHIYU_PG_TEST_URL", matches = ".+")
class QuotaGovernancePostgresqlTest {
    @Test
    void reservesGeneratedIdsAndPersistsReleaseAndSettlement() {
        DriverManagerDataSource admin = new DriverManagerDataSource(
                System.getenv("SHIYU_PG_TEST_URL"),
                System.getenv().getOrDefault("SHIYU_PG_TEST_USERNAME", "postgres"),
                System.getenv().getOrDefault("SHIYU_PG_TEST_PASSWORD", ""));
        JdbcTemplate adminJdbc = new JdbcTemplate(admin);
        String schema = "quota_test_" + UUID.randomUUID().toString().replace("-", "");
        adminJdbc.execute("CREATE SCHEMA " + schema);
        try {
            Properties properties = new Properties();
            properties.put("currentSchema", schema);
            DriverManagerDataSource scoped = new DriverManagerDataSource();
            scoped.setUrl(admin.getUrl());
            scoped.setUsername(admin.getUsername());
            scoped.setPassword(admin.getPassword());
            scoped.setConnectionProperties(properties);
            new ResourceDatabasePopulator(new FileSystemResource(Path.of(
                    "../../../applications/shiyu-platform-composition/src/main/resources/db/updates/postgresql/governance/20261024_quota_state.sql")))
                    .execute(scoped);
            JdbcTemplate jdbc = new JdbcTemplate(scoped);
            ActorContext actor = new ActorContext(new TenantId(7), new UserId(11), false);
            InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(
                    10, 1, 100, 300_000, Clock.systemUTC(), scoped);

            var first = quota.reserve(actor, new QuotaRequest(6, 0), "first");
            assertTrue(first.allowed());
            assertTrue(first.reservationId() > 0);
            assertEquals(first.reservationId(),
                    quota.reserve(actor, new QuotaRequest(6, 0), "first").reservationId());
            assertEquals("QUOTA_CONCURRENT",
                    quota.reserve(actor, new QuotaRequest(1, 0)).errorCode());
            quota.release(actor, first.reservationId());
            var second = quota.reserve(actor, new QuotaRequest(6, 0), "second");
            assertTrue(second.allowed());
            assertNotEquals(first.reservationId(), second.reservationId());
            quota.settle(actor, second.reservationId(), new QuotaUsage(5, 2));
            assertEquals(7L, jdbc.queryForObject(
                    "SELECT TOKENS_USED FROM GOVERNANCE_QUOTA_TENANT WHERE TENANT_ID=7", Long.class));
            assertEquals(0L, jdbc.queryForObject(
                    "SELECT TOKENS_RESERVED FROM GOVERNANCE_QUOTA_TENANT WHERE TENANT_ID=7", Long.class));
            assertEquals("QUOTA_TOKENS",
                    quota.reserve(actor, new QuotaRequest(4, 0)).errorCode());
        } finally {
            adminJdbc.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }
}
