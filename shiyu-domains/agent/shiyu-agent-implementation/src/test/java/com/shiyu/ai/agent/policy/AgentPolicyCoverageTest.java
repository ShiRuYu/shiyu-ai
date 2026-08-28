package com.shiyu.ai.agent.policy;

import com.shiyu.ai.agent.execution.ExecutionStatus;
import com.shiyu.ai.agent.lifecycle.AgentStateMachine;
import com.shiyu.ai.agent.retry.RetryConfig;
import com.shiyu.ai.agent.retry.RetryPolicyImpl;
import com.shiyu.ai.agent.timeout.TimeoutConfig;
import com.shiyu.ai.agent.timeout.TimeoutPolicyImpl;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AgentPolicyCoverageTest {

    @Test
    void validatesAllExecutionStateTransitionsAndTerminalHelpers() {
        assertTrue(AgentStateMachine.canTransition(ExecutionStatus.PENDING, ExecutionStatus.RUNNING));
        assertTrue(AgentStateMachine.canTransition(ExecutionStatus.RUNNING, ExecutionStatus.PAUSED));
        assertTrue(AgentStateMachine.canTransition(ExecutionStatus.PAUSED, ExecutionStatus.CANCELLED));
        assertFalse(AgentStateMachine.canTransition(ExecutionStatus.COMPLETED, ExecutionStatus.RUNNING));
        assertFalse(AgentStateMachine.canTransition(null, ExecutionStatus.RUNNING));
        assertDoesNotThrow(() -> AgentStateMachine.transition(ExecutionStatus.RUNNING, ExecutionStatus.COMPLETED));
        assertThrows(IllegalStateException.class,
                () -> AgentStateMachine.transition(ExecutionStatus.FAILED, ExecutionStatus.RUNNING));

        assertTrue(ExecutionStatus.COMPLETED.isTerminal());
        assertTrue(ExecutionStatus.RUNNING.canPause());
        assertTrue(ExecutionStatus.PAUSED.canResume());
        assertFalse(ExecutionStatus.PENDING.isTerminal());
        assertFalse(ExecutionStatus.PENDING.canPause());
        assertFalse(ExecutionStatus.RUNNING.canResume());
    }

    @Test
    void retriesTransientFailuresAndStopsAtTheConfiguredLimit() {
        RetryPolicyImpl retry = new RetryPolicyImpl();
        AtomicInteger attempts = new AtomicInteger();
        String value = retry.executeWithRetry(() -> {
            if (attempts.incrementAndGet() < 3) throw new IllegalStateException("transient");
            return "ok";
        }, new RetryConfig(3, 0, 2D));
        assertEquals("ok", value);
        assertEquals(3, attempts.get());

        AtomicInteger exhausted = new AtomicInteger();
        assertThrows(IllegalArgumentException.class, () -> retry.executeWithRetry(() -> {
            exhausted.incrementAndGet();
            throw new IllegalArgumentException("permanent");
        }, new RetryConfig(1, 0, 2D)));
        assertEquals(2, exhausted.get());
    }

    @Test
    void mapsTimeoutAndExecutionFailureOutcomes() throws Exception {
        TimeoutPolicyImpl timeout = new TimeoutPolicyImpl();
        assertEquals("ok", timeout.executeWithTimeout(() -> "ok", new TimeoutConfig(1000, 1000)));
        assertThrows(TimeoutException.class,
                () -> timeout.executeWithTimeout(() -> { Thread.sleep(100); return "late"; },
                        new TimeoutConfig(1000, 1)));
        assertThrows(IllegalStateException.class,
                () -> timeout.executeWithTimeout(() -> { throw new IllegalStateException("failed"); },
                        new TimeoutConfig(1000, 1000)));
        assertThrows(RuntimeException.class,
                () -> timeout.executeWithTimeout(() -> { throw new AssertionError("fatal"); },
                        new TimeoutConfig(1000, 1000)));
    }
}
