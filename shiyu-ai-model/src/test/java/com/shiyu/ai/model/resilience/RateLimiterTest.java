package com.shiyu.ai.model.resilience;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RateLimiter 单元测试
 */
@Tag("dev")
class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(5, 60000); // 每分钟最多5次
    }

    @Test
    void testAllowsUpToLimit() {
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.tryAcquire(), "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void testBlocksAfterLimit() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.tryAcquire();
        }

        assertFalse(rateLimiter.tryAcquire(), "Request after limit should be blocked");
    }

    @Test
    void testDefaultConstructor() {
        RateLimiter defaultRL = new RateLimiter();
        // Default: 60 requests per 60s
        for (int i = 0; i < 60; i++) {
            assertTrue(defaultRL.tryAcquire());
        }
    }

    @Test
    void testAllowsOneRequest() {
        RateLimiter oneRequest = new RateLimiter(1, 60000);
        assertTrue(oneRequest.tryAcquire());
        assertFalse(oneRequest.tryAcquire());
    }

    @Test
    void testAllowsAfterWindowExpiry() throws InterruptedException {
        RateLimiter shortWindow = new RateLimiter(1, 100); // 1 request per 100ms

        assertTrue(shortWindow.tryAcquire());
        assertFalse(shortWindow.tryAcquire()); // blocked

        Thread.sleep(150); // wait for window to expire

        assertTrue(shortWindow.tryAcquire());
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        int threadCount = 10;
        RateLimiter concurrentRL = new RateLimiter(3, 60000);
        AtomicInteger allowed = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                if (concurrentRL.tryAcquire()) {
                    allowed.incrementAndGet();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(3, allowed.get(), "Only 3 requests should be allowed");
    }
}
