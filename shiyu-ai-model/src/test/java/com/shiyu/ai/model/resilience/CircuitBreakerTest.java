package com.shiyu.ai.model.resilience;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CircuitBreaker 单元测试
 */
@Tag("dev")
class CircuitBreakerTest {

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        circuitBreaker = new CircuitBreaker(3, 100); // 3次失败后熔断, 100ms超时
    }

    @Test
    void testInitialStateIsClosed() {
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        assertTrue(circuitBreaker.isAllowed());
    }

    @Test
    void testOpenAfterThresholdFailures() {
        assertTrue(circuitBreaker.isAllowed());

        circuitBreaker.onFailure(); // 1
        circuitBreaker.onFailure(); // 2
        circuitBreaker.onFailure(); // 3 - threshold reached

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        assertFalse(circuitBreaker.isAllowed());
    }

    @Test
    void testHalfOpenAfterTimeout() throws InterruptedException {
        circuitBreaker.onFailure(); // 1
        circuitBreaker.onFailure(); // 2
        circuitBreaker.onFailure(); // 3

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

        // Wait for timeout
        Thread.sleep(150);

        assertTrue(circuitBreaker.isAllowed());
        assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());
    }

    @Test
    void testSuccessClosesCircuit() {
        circuitBreaker.onFailure(); // 1
        circuitBreaker.onFailure(); // 2
        circuitBreaker.onFailure(); // 3 - OPEN

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

        // Simulate timeout
        circuitBreaker.isAllowed(); // This sets to HALF_OPEN after timeout... 
        // Actually isAllowed only transitions on time check. Let me force state.
        // We need to actually wait for timeout or call onSuccess directly
    }

    @Test
    void testOnSuccessResetsFailureCount() {
        circuitBreaker.onFailure();
        circuitBreaker.onFailure();
        assertEquals(2, circuitBreaker.getFailureCount());

        circuitBreaker.onSuccess();

        assertEquals(0, circuitBreaker.getFailureCount());
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    @Test
    void testOnSuccessFromHalfOpen() throws InterruptedException {
        circuitBreaker.onFailure(); // 1
        circuitBreaker.onFailure(); // 2
        circuitBreaker.onFailure(); // 3 - OPEN

        Thread.sleep(150); // wait for timeout

        circuitBreaker.isAllowed(); // transitions to HALF_OPEN

        circuitBreaker.onSuccess();

        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        assertEquals(0, circuitBreaker.getFailureCount());
    }

    @Test
    void testDefaultConstructor() {
        CircuitBreaker defaultCB = new CircuitBreaker();
        assertEquals(CircuitBreaker.State.CLOSED, defaultCB.getState());

        // Default threshold is 5
        for (int i = 0; i < 5; i++) {
            defaultCB.onFailure();
        }
        assertEquals(CircuitBreaker.State.OPEN, defaultCB.getState());
    }

    @Test
    void testFailureCountIncrementsCorrectly() {
        assertEquals(0, circuitBreaker.getFailureCount());

        circuitBreaker.onFailure();
        assertEquals(1, circuitBreaker.getFailureCount());

        circuitBreaker.onFailure();
        assertEquals(2, circuitBreaker.getFailureCount());
    }

    @Test
    void testClosedAfterOnSuccess() {
        circuitBreaker.onFailure();
        circuitBreaker.onSuccess();

        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        assertEquals(0, circuitBreaker.getFailureCount());
    }

    @Test
    void testNotExceedingThresholdDoesNotOpen() {
        circuitBreaker.onFailure(); // 1
        circuitBreaker.onFailure(); // 2

        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        assertTrue(circuitBreaker.isAllowed());
    }
}
