package com.shiyu.ai.governance.implementation.quota;

import static org.junit.jupiter.api.Assertions.*;

import com.shiyu.ai.governance.contract.QuotaRequest;
import com.shiyu.ai.governance.contract.QuotaUsage;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * 验证 In 记忆 Quota 治理 相关功能、边界条件、异常路径和协作行为。
 */
class InMemoryQuotaGovernanceTest {
    private static final ActorContext ACTOR =
            new ActorContext(new TenantId(7), new UserId(11), false);
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-01-01T00:00:30Z"), ZoneOffset.UTC);

    @Test
    void minimumReservationTtlRenewsBeforeItCanExpire() {
        org.h2.jdbcx.JdbcDataSource dataSource = newDataSource();
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:30Z"));
        InMemoryQuotaGovernance quota =
                new InMemoryQuotaGovernance(100, 1, 100, 1_000L, clock, dataSource);

        var reservation = quota.reserve(ACTOR, new QuotaRequest(1, 0), "g-short-ttl");
        assertTrue(reservation.allowed());
        assertEquals(333L, quota.reservationRenewalIntervalMillis());
        clock.advanceMillis(quota.reservationRenewalIntervalMillis());
        assertTrue(quota.renew(ACTOR, "g-short-ttl"));
        clock.advanceMillis(800);
        assertEquals("QUOTA_CONCURRENT", quota.reserve(ACTOR, new QuotaRequest(1, 0)).errorCode());
    }

    @Test
    void reservesSettlesAndEnforcesDailyTokensPerTenant() {
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(10, 2, 100, 300_000L, CLOCK, newDataSource());

        var first = quota.reserve(ACTOR, new QuotaRequest(6, 0));
        assertTrue(first.allowed());
        quota.settle(ACTOR, first.reservationId(), new QuotaUsage(6, 2));

        var denied = quota.reserve(ACTOR, new QuotaRequest(3, 0));
        assertFalse(denied.allowed());
        assertEquals("QUOTA_TOKENS", denied.errorCode());
    }

    @Test
    void activeReservationContinuesToCountAgainstTheDailyLimitAfterMidnight() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T23:58:30Z"));
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(10, 2, 100, 300_000L, clock, newDataSource());
        var reservation = quota.reserve(ACTOR, new QuotaRequest(8, 0));
        assertTrue(reservation.allowed());

        clock.advanceSeconds(120);
        assertEquals("QUOTA_TOKENS", quota.reserve(ACTOR, new QuotaRequest(3, 0)).errorCode());

        quota.settle(ACTOR, reservation.reservationId(), new QuotaUsage(8, 0));
        assertTrue(quota.reserve(ACTOR, new QuotaRequest(2, 0)).allowed());
    }

    @Test
    void releaseMakesConcurrentCapacityAvailableAgain() {
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(100, 1, 100, 300_000L, CLOCK, newDataSource());

        var first = quota.reserve(ACTOR, new QuotaRequest(1, 0));
        assertTrue(first.allowed());
        assertEquals("QUOTA_CONCURRENT", quota.reserve(ACTOR, new QuotaRequest(1, 0)).errorCode());

        quota.release(ACTOR, first.reservationId());
        assertTrue(quota.reserve(ACTOR, new QuotaRequest(1, 0)).allowed());
    }

    @Test
    void crossTenantSettlementIsRejected() {
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(100, 1, 100, 300_000L, CLOCK, newDataSource());
        var reservation = quota.reserve(ACTOR, new QuotaRequest(1, 0));
        ActorContext otherTenant = new ActorContext(new TenantId(8), new UserId(11), false);

        assertThrows(
                RuntimeException.class,
                () -> quota.settle(otherTenant, reservation.reservationId(), new QuotaUsage(1, 1)));
    }

    @Test
    void enforcesRequestsPerMinuteAndHonorsRequestedConcurrentLimit() {
        InMemoryQuotaGovernance rpm = new InMemoryQuotaGovernance(0, 4, 1, 300_000L, CLOCK, newDataSource());
        assertTrue(rpm.reserve(ACTOR, new QuotaRequest(1, 0)).allowed());
        assertEquals("QUOTA_RPM", rpm.reserve(ACTOR, new QuotaRequest(1, 0)).errorCode());

        InMemoryQuotaGovernance concurrent = new InMemoryQuotaGovernance(100, 4, 100, 300_000L, CLOCK, newDataSource());
        assertTrue(concurrent.reserve(ACTOR, new QuotaRequest(1, 1)).allowed());
        assertEquals(
                "QUOTA_CONCURRENT", concurrent.reserve(ACTOR, new QuotaRequest(1, 1)).errorCode());
    }

    @Test
    void treatsUnknownReservationsAndUnlimitedTokenLimitsAsNoOpsOrAllowed() {
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(0, 2, 100, 300_000L, CLOCK, newDataSource());
        quota.release(ACTOR, 9999L);
        quota.settle(ACTOR, 9999L, new QuotaUsage(100, 100));
        var reservation = quota.reserve(ACTOR, new QuotaRequest(1, 0));
        assertTrue(reservation.allowed());
        quota.release(ACTOR, reservation.reservationId());
        quota.release(ACTOR, reservation.reservationId());
    }

    @Test
    void resetsMinuteCountersWhenTheClockMovesToANewMinute() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:59Z"));
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(100, 2, 1, 300_000L, clock, newDataSource());
        assertTrue(quota.reserve(ACTOR, new QuotaRequest(1, 0)).allowed());
        clock.advanceSeconds(2);
        assertTrue(quota.reserve(ACTOR, new QuotaRequest(1, 0)).allowed());
    }

    @Test
    void samplesQuotaWindowAfterWaitingForTenantLock() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:59Z"));
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(100, 2, 1, 300_000L, clock, newDataSource());
        clock.afterFirstRead(() -> clock.advanceSeconds(2));

        assertTrue(quota.reserve(ACTOR, new QuotaRequest(1, 0)).allowed());
        assertEquals(
                "QUOTA_RPM", quota.reserve(ACTOR, new QuotaRequest(1, 0)).errorCode());
    }

    @Test
    void sharedReferenceSurvivesRestartAndSettlementIsIdempotent() {
        var dataSource = newDataSource();
        var jdbc = new JdbcTemplate(dataSource);
        var firstInstance = new InMemoryQuotaGovernance(20, 1, 2, 300_000L, CLOCK, dataSource);
        var secondInstance = new InMemoryQuotaGovernance(20, 1, 2, 300_000L, CLOCK, dataSource);
        var reservation = firstInstance.reserve(ACTOR, new QuotaRequest(6, 0), " shared ");

        assertEquals(reservation.reservationId(), secondInstance.reserve(
                ACTOR, new QuotaRequest(19, 0), "shared").reservationId());
        assertTrue(secondInstance.renew(ACTOR, "shared"));
        secondInstance.settle(ACTOR, "shared", new QuotaUsage(0, 2));
        firstInstance.settle(ACTOR, "shared", new QuotaUsage(9, 9));

        assertEquals(8L, jdbc.queryForObject(
                "SELECT TOKENS_USED FROM GOVERNANCE_QUOTA_TENANT WHERE TENANT_ID=7", Long.class));
        assertEquals(0L, jdbc.queryForObject(
                "SELECT TOKENS_RESERVED FROM GOVERNANCE_QUOTA_TENANT WHERE TENANT_ID=7", Long.class));
        assertEquals("QUOTA_RESERVATION_CLOSED", firstInstance.reserve(
                ACTOR, new QuotaRequest(1, 0), "shared").errorCode());
        assertFalse(firstInstance.renew(ACTOR, "shared"));
        var next = firstInstance.reserve(ACTOR, new QuotaRequest(12, 0), "next");
        assertTrue(next.allowed());
        secondInstance.release(ACTOR, "next");
        firstInstance.release(ACTOR, "next");
        assertEquals(0, jdbc.queryForObject(
                "SELECT ACTIVE_RESERVATIONS FROM GOVERNANCE_QUOTA_TENANT WHERE TENANT_ID=7", Integer.class));
    }

    @Test
    void recoversAbandonedReservationsAndChargesLateReferenceUsageExactlyOnce() {
        var dataSource = newDataSource();
        var jdbc = new JdbcTemplate(dataSource);
        var clock = new MutableClock(Instant.parse("2026-01-01T00:00:30Z"));
        var quota = new InMemoryQuotaGovernance(20, 1, 100, 1_000L, clock, dataSource);
        var expired = quota.reserve(ACTOR, new QuotaRequest(6, 0), "late");
        clock.advanceSeconds(2);
        quota.recoverExpiredReservations();

        assertFalse(quota.renew(ACTOR, "late"));
        assertEquals("QUOTA_RESERVATION_CLOSED", quota.reserve(
                ACTOR, new QuotaRequest(1, 0), "late").errorCode());
        var active = quota.reserve(ACTOR, new QuotaRequest(19, 0), "active");
        assertTrue(active.allowed());
        quota.settle(ACTOR, expired.reservationId(), new QuotaUsage(6, 2));
        assertEquals(0L, jdbc.queryForObject(
                "SELECT TOKENS_USED FROM GOVERNANCE_QUOTA_TENANT WHERE TENANT_ID=7", Long.class));
        quota.settle(ACTOR, "late", new QuotaUsage(0, 2));
        quota.settle(ACTOR, "late", new QuotaUsage(6, 2));
        quota.release(ACTOR, "active");

        assertEquals(8L, jdbc.queryForObject(
                "SELECT TOKENS_USED FROM GOVERNANCE_QUOTA_TENANT WHERE TENANT_ID=7", Long.class));
        assertTrue(quota.reserve(ACTOR, new QuotaRequest(12, 0)).allowed());
        quota.recoverExpiredReservations();
    }

    @Test
    void referenceOperationsStayTenantScopedAndUnknownReferencesAreNoOps() {
        var dataSource = newDataSource();
        var jdbc = new JdbcTemplate(dataSource);
        var quota = new InMemoryQuotaGovernance(20, 1, 100, 300_000L, CLOCK, dataSource);
        var other = new ActorContext(new TenantId(8), new UserId(11), false);
        var mine = quota.reserve(ACTOR, new QuotaRequest(5, 0), "same");
        assertTrue(quota.reserve(other, new QuotaRequest(7, 0), "same").allowed());
        assertFalse(quota.renew(ACTOR, "unknown"));
        quota.settle(ACTOR, "unknown", new QuotaUsage(1, 1));
        quota.release(ACTOR, "unknown");
        assertThrows(RuntimeException.class, () -> quota.release(other, mine.reservationId()));
        quota.settle(other, "same", new QuotaUsage(7, 1));
        quota.release(ACTOR, "same");

        assertEquals(0L, jdbc.queryForObject(
                "SELECT TOKENS_USED FROM GOVERNANCE_QUOTA_TENANT WHERE TENANT_ID=7", Long.class));
        assertEquals(8L, jdbc.queryForObject(
                "SELECT TOKENS_USED FROM GOVERNANCE_QUOTA_TENANT WHERE TENANT_ID=8", Long.class));
        for (String invalid : new String[] {"", " ", "x".repeat(129)}) {
            assertThrows(IllegalArgumentException.class,
                    () -> quota.reserve(ACTOR, new QuotaRequest(1, 0), invalid));
            assertThrows(IllegalArgumentException.class, () -> quota.renew(ACTOR, invalid));
        }
        assertThrows(IllegalArgumentException.class, () -> quota.renew(ACTOR, null));
    }

    /** 创建加载实际配额表结构的独立 H2 数据源，避免测试之间共享持久状态。 */
    private static org.h2.jdbcx.JdbcDataSource newDataSource() {
        org.h2.jdbcx.JdbcDataSource dataSource = new org.h2.jdbcx.JdbcDataSource();
        dataSource.setURL(
                "jdbc:h2:mem:quota_"
                        + java.util.UUID.randomUUID()
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        new ResourceDatabasePopulator(
                        new ClassPathResource("db/baseline/h2/schema/governance/05_governance.sql"))
                .execute(dataSource);
        return dataSource;
    }

    /**
     * 验证 Mutable Clock 相关功能、边界条件、异常路径和协作行为。
     */
    private static final class MutableClock extends java.time.Clock {
        private Instant instant;
        private Runnable firstReadCallback;
        private boolean callbackPending;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advanceSeconds(long seconds) {
            instant = instant.plusSeconds(seconds);
        }

        private void advanceMillis(long millis) {
            instant = instant.plusMillis(millis);
        }

        private void afterFirstRead(Runnable callback) {
            firstReadCallback = callback;
            callbackPending = true;
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public java.time.Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            Instant current = instant;
            if (callbackPending) {
                callbackPending = false;
                firstReadCallback.run();
            }
            return current;
        }
    }
}
