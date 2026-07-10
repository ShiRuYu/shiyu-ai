package com.shiyu.ai.agent.retry;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RetryPolicyImpl 单元测试
 */
@Tag("dev")
class RetryPolicyImplTest {

    private RetryPolicyImpl retryPolicy;
    private RetryConfig config;

    @BeforeEach
    void setUp() {
        retryPolicy = new RetryPolicyImpl();
        config = new RetryConfig(3, 10, 2.0); // 快速重试用于测试
    }

    @Test
    void testExecuteSuccessOnFirstAttempt() {
        String result = retryPolicy.executeWithRetry(() -> "success", config);
        assertEquals("success", result);
    }

    @Test
    void testExecuteSuccessAfterRetries() {
        AtomicInteger attempts = new AtomicInteger(0);
        String result = retryPolicy.executeWithRetry(() -> {
            int count = attempts.incrementAndGet();
            if (count < 3) throw new RuntimeException("Attempt " + count + " failed");
            return "success after " + count + " attempts";
        }, config);

        assertEquals("success after 3 attempts", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void testExecuteExhaustRetries() {
        AtomicInteger attempts = new AtomicInteger(0);
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            retryPolicy.executeWithRetry(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("Always fails");
            }, config)
        );

        assertEquals("Always fails", exception.getMessage());
        assertEquals(4, attempts.get()); // initial + 3 retries
    }

    @Test
    void testZeroMaxRetries() {
        RetryConfig zeroRetry = new RetryConfig(0, 10, 2.0);
        AtomicInteger attempts = new AtomicInteger(0);

        assertThrows(RuntimeException.class, () ->
            retryPolicy.executeWithRetry(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("Fail");
            }, zeroRetry)
        );

        assertEquals(1, attempts.get()); // only initial attempt, no retries
    }

    @Test
    void testDefaultConfig() {
        RetryConfig defaultConfig = RetryConfig.defaultConfig();
        assertEquals(3, defaultConfig.getMaxRetries());
        assertEquals(1000, defaultConfig.getInitialDelayMs());
        assertEquals(2.0, defaultConfig.getBackoffMultiplier(), 0.001);
    }
}
