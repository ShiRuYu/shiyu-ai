package com.shiyu.ai.auth.handler;

import com.shiyu.ai.common.web.auth.HttpClientIpResolver;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {
    @Test
    void allowsFiveAttemptsThenLocksAndResetOpensWindow() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < 5; i++) assertTrue(limiter.isAllowed("10.0.0.1"));
        assertFalse(limiter.isAllowed("10.0.0.1"));
        assertTrue(limiter.isAllowed("10.0.0.2"));
        limiter.reset("10.0.0.1");
        assertTrue(limiter.isAllowed("10.0.0.1"));
        limiter.destroy();
    }

    @Test
    void resolvesClientIpThroughInjectedTransportAdapter() {
        LoginRateLimiter limiter = new LoginRateLimiter(() -> "203.0.113.42");

        assertEquals("203.0.113.42", limiter.getClientIp());

        limiter.destroy();
    }

    @Test
    void resolvesForwardedClientIpAndUnknownWithoutRequest() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        assertEquals("unknown", limiter.getClientIp());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", " 192.0.2.1, 192.0.2.2");
        LoginRateLimiter requestLimiter = new LoginRateLimiter(new HttpClientIpResolver(request));
        assertEquals("192.0.2.1", requestLimiter.getClientIp());
        limiter.destroy();
        requestLimiter.destroy();
    }

    @Test
    void fallsBackAcrossProxyHeadersAndHandlesUnknownValues() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "unknown");
        request.addHeader("Proxy-Client-IP", "unknown");
        request.addHeader("WL-Proxy-Client-IP", "198.51.100.7");
        request.setRemoteAddr("192.0.2.8");
        LoginRateLimiter requestLimiter = new LoginRateLimiter(new HttpClientIpResolver(request));
        assertEquals("198.51.100.7", requestLimiter.getClientIp());
        request.removeHeader("WL-Proxy-Client-IP");
        assertEquals("192.0.2.8", requestLimiter.getClientIp());
        requestLimiter.init();
        requestLimiter.destroy();
    }

    @Test
    void fallsBackAcrossEmptyProxyHeadersBeforeUsingRemoteAddress() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "");
        request.addHeader("Proxy-Client-IP", "");
        request.addHeader("WL-Proxy-Client-IP", "");
        request.setRemoteAddr("198.51.100.9");
        assertEquals("198.51.100.9", new LoginRateLimiter(new HttpClientIpResolver(request)).getClientIp());
        limiter.destroy();
    }

    @Test
    void cleansExpiredLocksAndWindowEntriesWithoutRemovingLiveEntries() throws Exception {
        LoginRateLimiter limiter = new LoginRateLimiter();
        Field attemptsField = LoginRateLimiter.class.getDeclaredField("attempts");
        attemptsField.setAccessible(true);
        @SuppressWarnings("unchecked") Map<String, Object> attempts = (Map<String, Object>) attemptsField.get(limiter);
        Class<?> entryType = Class.forName("com.shiyu.ai.auth.handler.LoginRateLimiter$RateLimitEntry");
        Field lockedUntil = entryType.getDeclaredField("lockedUntil");
        lockedUntil.setAccessible(true);
        Field queue = entryType.getDeclaredField("attempts");
        queue.setAccessible(true);
        var entryConstructor = entryType.getDeclaredConstructor();
        entryConstructor.setAccessible(true);

        Object expiredLock = entryConstructor.newInstance();
        lockedUntil.setLong(expiredLock, 1L);
        attempts.put("expired-lock", expiredLock);
        Object staleWindow = entryConstructor.newInstance();
        @SuppressWarnings("unchecked") Queue<Long> stale = (Queue<Long>) queue.get(staleWindow);
        stale.add(1L);
        attempts.put("stale-window", staleWindow);
        Object live = entryConstructor.newInstance();
        @SuppressWarnings("unchecked") Queue<Long> liveAttempts = (Queue<Long>) queue.get(live);
        liveAttempts.add(System.currentTimeMillis());
        lockedUntil.setLong(live, 0L);
        attempts.put("live", live);

        Method cleanup = LoginRateLimiter.class.getDeclaredMethod("cleanupExpiredEntries");
        cleanup.setAccessible(true);
        cleanup.invoke(limiter);
        assertFalse(attempts.containsKey("expired-lock"));
        assertFalse(attempts.containsKey("stale-window"));
        assertTrue(attempts.containsKey("live"));
        limiter.destroy();
    }
}
