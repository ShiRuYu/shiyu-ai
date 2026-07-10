package com.shiyu.ai.agent.execution;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Execution 实体单元测试
 */
@Tag("dev")
class ExecutionTest {

    private Execution execution;
    private Map<String, Object> input;

    @BeforeEach
    void setUp() {
        input = new HashMap<>();
        input.put("message", "hello");
        execution = new Execution("agent-1", "1.0", input);
    }

    @Test
    void testCreateExecution() {
        assertNotNull(execution.getExecutionId());
        assertEquals("agent-1", execution.getAgentId());
        assertEquals("1.0", execution.getVersion());
        assertEquals(ExecutionStatus.PENDING, execution.getStatus());
        assertEquals(input, execution.getInput());
        assertNull(execution.getOutput());
        assertNull(execution.getErrorMessage());
        assertNull(execution.getUserId());
        assertNull(execution.getSessionId());
        assertNull(execution.getStartTime());
        assertNull(execution.getEndTime());
        assertNull(execution.getDurationMs());
        assertTrue(execution.getNodeExecutions().isEmpty());
    }

    @Test
    void testExecutionIdIsUUIDWithoutHyphens() {
        String id = execution.getExecutionId();
        assertNotNull(id);
        assertEquals(32, id.length());
        assertFalse(id.contains("-"));
    }

    @Test
    void testStart() {
        execution.start();
        assertEquals(ExecutionStatus.RUNNING, execution.getStatus());
        assertNotNull(execution.getStartTime());
        assertTrue(execution.getStartTime() instanceof LocalDateTime);
    }

    @Test
    void testComplete() {
        execution.start();
        Map<String, Object> output = new HashMap<>();
        output.put("result", "success");

        execution.complete(output);

        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        assertEquals(output, execution.getOutput());
        assertNotNull(execution.getEndTime());
        assertNotNull(execution.getDurationMs());
        assertTrue(execution.getDurationMs() >= 0);
    }

    @Test
    void testFail() {
        execution.start();
        execution.fail("Something went wrong");

        assertEquals(ExecutionStatus.FAILED, execution.getStatus());
        assertEquals("Something went wrong", execution.getErrorMessage());
        assertNotNull(execution.getEndTime());
    }

    @Test
    void testFailWithoutStart() {
        execution.fail("Failed before start");
        assertEquals(ExecutionStatus.FAILED, execution.getStatus());
        assertEquals("Failed before start", execution.getErrorMessage());
        assertNull(execution.getDurationMs()); // startTime is null
    }

    @Test
    void testPauseAndResume() {
        execution.start();
        assertEquals(ExecutionStatus.RUNNING, execution.getStatus());

        execution.pause();
        assertEquals(ExecutionStatus.PAUSED, execution.getStatus());

        execution.resume();
        assertEquals(ExecutionStatus.RUNNING, execution.getStatus());
    }

    @Test
    void testCancel() {
        execution.start();
        execution.cancel();

        assertEquals(ExecutionStatus.CANCELLED, execution.getStatus());
        assertNotNull(execution.getEndTime());
        assertNotNull(execution.getDurationMs());
    }

    @Test
    void testAddNodeExecution() {
        NodeExecution nodeExec = new NodeExecution("node-1", "LLM_CALL");
        nodeExec.start();
        nodeExec.complete(new HashMap<>());

        execution.addNodeExecution(nodeExec);

        assertEquals(1, execution.getNodeExecutions().size());
        assertEquals("node-1", execution.getNodeExecutions().get(0).getNodeId());
    }

    @Test
    void testSetUserId() {
        execution.setUserId(1001L);
        assertEquals(1001L, execution.getUserId());
    }

    @Test
    void testSetSessionId() {
        execution.setSessionId("session-abc");
        assertEquals("session-abc", execution.getSessionId());
    }

    @Test
    void testSetLastCheckpointId() {
        execution.setLastCheckpointId("cp-123");
        assertEquals("cp-123", execution.getLastCheckpointId());
    }

    @Test
    void testCompleteSetsDuration() throws InterruptedException {
        execution.start();
        Thread.sleep(5); // small delay to ensure duration > 0
        execution.complete(new HashMap<>());

        assertNotNull(execution.getDurationMs());
        assertTrue(execution.getDurationMs() > 0);
    }

    @Test
    void testMultipleExecutionsHaveDifferentIds() {
        Execution exec2 = new Execution("agent-1", "1.0", input);
        assertNotEquals(execution.getExecutionId(), exec2.getExecutionId());
    }
}
