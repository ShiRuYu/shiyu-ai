package com.shiyu.ai.agent.implementation.execution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * 验证 Node Execution Coverage 相关功能、边界条件、异常路径和协作行为。
 */
class NodeExecutionCoverageTest {
    @Test
    void tracksSuccessfulExecutionAndRetries() {
        var execution = new NodeExecution("n1", "LLM");
        assertEquals(ExecutionStatus.PENDING, execution.getStatus());
        execution.setInput("hello");
        execution.start();
        execution.incrementRetry();
        execution.complete("world");
        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        assertEquals("hello", execution.getInput());
        assertEquals("world", execution.getOutput());
        assertNotNull(execution.getStartTime());
        assertNotNull(execution.getEndTime());
        assertNotNull(execution.getDurationMs());
        assertEquals(1, execution.getRetryCount());
    }

    @Test
    void tracksFailureBeforeAndAfterStart() {
        var pending = new NodeExecution("n0", "HTTP");
        pending.fail("bad");
        assertEquals(ExecutionStatus.FAILED, pending.getStatus());
        assertEquals("bad", pending.getErrorMessage());
        assertNull(pending.getDurationMs());
        var running = new NodeExecution("n2", "HTTP");
        running.start();
        running.fail("timeout");
        assertNotNull(running.getDurationMs());
    }
}
