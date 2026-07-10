package com.shiyu.ai.agent.execution;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ExecutionStatus 枚举单元测试
 */
@Tag("dev")
class ExecutionStatusTest {

    @Test
    void testTerminalStatuses() {
        assertTrue(ExecutionStatus.COMPLETED.isTerminal());
        assertTrue(ExecutionStatus.FAILED.isTerminal());
        assertTrue(ExecutionStatus.CANCELLED.isTerminal());
        assertFalse(ExecutionStatus.PENDING.isTerminal());
        assertFalse(ExecutionStatus.RUNNING.isTerminal());
        assertFalse(ExecutionStatus.PAUSED.isTerminal());
    }

    @Test
    void testCanPause() {
        assertTrue(ExecutionStatus.RUNNING.canPause());
        assertFalse(ExecutionStatus.PENDING.canPause());
        assertFalse(ExecutionStatus.PAUSED.canPause());
        assertFalse(ExecutionStatus.COMPLETED.canPause());
        assertFalse(ExecutionStatus.FAILED.canPause());
        assertFalse(ExecutionStatus.CANCELLED.canPause());
    }

    @Test
    void testCanResume() {
        assertTrue(ExecutionStatus.PAUSED.canResume());
        assertFalse(ExecutionStatus.PENDING.canResume());
        assertFalse(ExecutionStatus.RUNNING.canResume());
        assertFalse(ExecutionStatus.COMPLETED.canResume());
        assertFalse(ExecutionStatus.FAILED.canResume());
        assertFalse(ExecutionStatus.CANCELLED.canResume());
    }

    @Test
    void testAllEnumValuesPresent() {
        ExecutionStatus[] values = ExecutionStatus.values();
        assertEquals(6, values.length);
        assertArrayEquals(new ExecutionStatus[]{
                ExecutionStatus.PENDING,
                ExecutionStatus.RUNNING,
                ExecutionStatus.PAUSED,
                ExecutionStatus.COMPLETED,
                ExecutionStatus.FAILED,
                ExecutionStatus.CANCELLED
        }, values);
    }
}
