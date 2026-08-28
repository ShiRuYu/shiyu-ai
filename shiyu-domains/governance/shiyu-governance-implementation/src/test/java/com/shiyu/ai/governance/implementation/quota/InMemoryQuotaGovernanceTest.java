package com.shiyu.ai.governance.implementation.quota;

import com.shiyu.ai.governance.contract.QuotaRequest;
import com.shiyu.ai.governance.contract.QuotaUsage;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryQuotaGovernanceTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(7), new UserId(11), false);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:30Z"), ZoneOffset.UTC);

    @Test
    void reservesSettlesAndEnforcesDailyTokensPerTenant() {
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(10, 2, 100, CLOCK);

        var first = quota.reserve(ACTOR, new QuotaRequest(6, 0));
        assertTrue(first.allowed());
        quota.settle(ACTOR, first.reservationId(), new QuotaUsage(6, 2));

        var denied = quota.reserve(ACTOR, new QuotaRequest(3, 0));
        assertFalse(denied.allowed());
        assertEquals("QUOTA_TOKENS", denied.errorCode());
    }

    @Test
    void releaseMakesConcurrentCapacityAvailableAgain() {
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(100, 1, 100, CLOCK);

        var first = quota.reserve(ACTOR, new QuotaRequest(1, 0));
        assertTrue(first.allowed());
        assertEquals("QUOTA_CONCURRENT", quota.reserve(ACTOR, new QuotaRequest(1, 0)).errorCode());

        quota.release(ACTOR, first.reservationId());
        assertTrue(quota.reserve(ACTOR, new QuotaRequest(1, 0)).allowed());
    }

    @Test
    void crossTenantSettlementIsRejected() {
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(100, 1, 100, CLOCK);
        var reservation = quota.reserve(ACTOR, new QuotaRequest(1, 0));
        ActorContext otherTenant = new ActorContext(new TenantId(8), new UserId(11), false);

        assertThrows(RuntimeException.class,
                () -> quota.settle(otherTenant, reservation.reservationId(), new QuotaUsage(1, 1)));
    }

    @Test
    void enforcesRequestsPerMinuteAndHonorsRequestedConcurrentLimit() {
        InMemoryQuotaGovernance rpm = new InMemoryQuotaGovernance(0, 4, 1, CLOCK);
        assertTrue(rpm.reserve(ACTOR, new QuotaRequest(1, 0)).allowed());
        assertEquals("QUOTA_RPM", rpm.reserve(ACTOR, new QuotaRequest(1, 0)).errorCode());

        InMemoryQuotaGovernance concurrent = new InMemoryQuotaGovernance(100, 4, 100, CLOCK);
        assertTrue(concurrent.reserve(ACTOR, new QuotaRequest(1, 1)).allowed());
        assertEquals("QUOTA_CONCURRENT", concurrent.reserve(ACTOR, new QuotaRequest(1, 1)).errorCode());
    }

    @Test
    void treatsUnknownReservationsAndUnlimitedTokenLimitsAsNoOpsOrAllowed() {
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(0, 2, 100, CLOCK);
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
        InMemoryQuotaGovernance quota = new InMemoryQuotaGovernance(100, 2, 1, clock);
        assertTrue(quota.reserve(ACTOR, new QuotaRequest(1, 0)).allowed());
        clock.advanceSeconds(2);
        assertTrue(quota.reserve(ACTOR, new QuotaRequest(1, 0)).allowed());
    }

    private static final class MutableClock extends java.time.Clock {
        private Instant instant;
        private MutableClock(Instant instant) { this.instant = instant; }
        private void advanceSeconds(long seconds) { instant = instant.plusSeconds(seconds); }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public java.time.Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
