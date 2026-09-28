package com.shiyu.ai.runtimeconsole.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/** 覆盖一次性授权码和浏览器会话的生命周期行为。 */
class ConsoleSessionStoreTest {

    @Test
    void exchangesOneTimeCodeForIndependentSessionAndCsrfSecrets() {
        ConsoleSessionStore store = new ConsoleSessionStore(Clock.systemUTC());

        String code = store.issueOneTimeCode();
        ConsoleSessionStore.Session session = store.exchange(code);

        assertNotNull(session);
        assertNotEquals(code, session.sessionId());
        assertNotEquals(session.sessionId(), session.csrfToken());
        assertEquals(session, store.findSession(session.sessionId()));
        assertNull(store.exchange(code), "the browser grant must be one time");
    }

    @Test
    void rejectsExpiredOrUnknownOneTimeCode() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        ConsoleSessionStore store = new ConsoleSessionStore(clock);
        String code = store.issueOneTimeCode();

        clock.advance(Duration.ofMinutes(3));

        assertNull(store.exchange(code));
        assertNull(store.exchange("not-a-valid-code"));
        assertNull(store.findSession("anything"));
    }

    @Test
    void revokeRemovesTheInMemoryBrowserSession() {
        ConsoleSessionStore store = new ConsoleSessionStore(Clock.systemUTC());
        ConsoleSessionStore.Session session = store.exchange(store.issueOneTimeCode());

        assertTrue(store.revoke(session.sessionId()));
        assertFalse(store.revoke(session.sessionId()));
        assertNull(store.findSession(session.sessionId()));
    }

    /** 为会话生命周期测试提供可控的时间源。 */
    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> instant;

        private MutableClock(Instant instant) {
            this.instant = new AtomicReference<>(instant);
        }

        private void advance(Duration duration) {
            instant.updateAndGet(value -> value.plus(duration));
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant.get();
        }
    }
}
