package com.shiyu.ai.usage.quota;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import com.shiyu.ai.common.storage.LocalRateLimitStore;
import static org.junit.jupiter.api.Assertions.*;

class LocalQuotaGatewayTest {
    @Test void reservesSettlesAndRejectsConcurrentLimit() {
        LocalQuotaGateway gateway = new LocalQuotaGateway(100, 1, Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
        var first = gateway.reserve(1, 10, 1);
        assertTrue(first.allowed());
        assertFalse(gateway.reserve(1, 10, 1).allowed());
        gateway.settle(1, first.reservationId(), 10, 20);
        assertFalse(gateway.reserve(1, 80, 1).allowed());
        var next = gateway.reserve(1, 50, 1);
        assertTrue(next.allowed());
        gateway.release(1, next.reservationId());
    }

    @Test
    void enforcesPerMinuteGenerationLimit() {
        LocalQuotaGateway gateway = new LocalQuotaGateway(1000, 10, Clock.systemUTC(), 1, new LocalRateLimitStore());
        assertTrue(gateway.reserve(7L, 1, 0).allowed());
        assertEquals("QUOTA_RPM", gateway.reserve(7L, 1, 0).errorCode());
    }
}
