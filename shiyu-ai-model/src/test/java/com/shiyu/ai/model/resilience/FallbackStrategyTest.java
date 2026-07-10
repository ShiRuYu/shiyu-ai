package com.shiyu.ai.model.resilience;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FallbackStrategy 单元测试
 */
@Tag("dev")
class FallbackStrategyTest {

    @Test
    void testPrimarySuccessReturnsPrimaryResult() {
        String result = FallbackStrategy.executeWithFallback(
            () -> "primary",
            () -> "fallback"
        );

        assertEquals("primary", result);
    }

    @Test
    void testPrimaryFailureReturnsFallback() {
        String result = FallbackStrategy.executeWithFallback(
            () -> { throw new RuntimeException("Primary failed"); },
            () -> "fallback"
        );

        assertEquals("fallback", result);
    }

    @Test
    void testPrimaryFailureWithNoFallback() {
        assertThrows(RuntimeException.class, () ->
            FallbackStrategy.executeWithFallback(
                () -> { throw new RuntimeException("Primary failed"); },
                null
            )
        );
    }

    @Test
    void testFallbackAlsoFails() {
        assertThrows(RuntimeException.class, () ->
            FallbackStrategy.executeWithFallback(
                () -> { throw new RuntimeException("Primary failed"); },
                () -> { throw new RuntimeException("Fallback also failed"); }
            )
        );
    }

    @Test
    void testPrimaryNotCalledWhenFallbackNull() {
        AtomicInteger primaryCalls = new AtomicInteger(0);

        assertThrows(RuntimeException.class, () ->
            FallbackStrategy.executeWithFallback(
                () -> {
                    primaryCalls.incrementAndGet();
                    throw new RuntimeException("Fail");
                },
                null
            )
        );

        assertEquals(1, primaryCalls.get());
    }

    @Test
    void testFallbackUsedWithIntegerReturn() {
        Integer result = FallbackStrategy.executeWithFallback(
            () -> { throw new RuntimeException("fail"); },
            () -> 42
        );

        assertEquals(42, result);
    }

    @Test
    void testPrimarySuccessWithNoFallback() {
        String result = FallbackStrategy.executeWithFallback(
            () -> "primary-success",
            null
        );

        assertEquals("primary-success", result);
    }
}
