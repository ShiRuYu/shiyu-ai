package com.shiyu.ai.agent.checkpoint;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checkpoint 实体单元测试
 */
@Tag("dev")
class CheckpointTest {

    @Test
    void testCreateCheckpoint() {
        Map<String, Object> state = new HashMap<>();
        state.put("step", "intent_recognition");
        state.put("result", "greeting");

        Checkpoint cp = new Checkpoint("exec-1", "node-1", state);

        assertNotNull(cp.getCheckpointId());
        assertEquals("exec-1", cp.getExecutionId());
        assertEquals("node-1", cp.getNodeId());
        assertEquals(state, cp.getState());
        assertNotNull(cp.getCreatedAt());
        assertTrue(cp.getCreatedAt() instanceof LocalDateTime);
        assertNull(cp.getSerializedState());
    }

    @Test
    void testCheckpointIdIsUUIDWithoutHyphens() {
        Map<String, Object> state = new HashMap<>();
        state.put("key", "value");

        Checkpoint cp = new Checkpoint("exec-1", "node-1", state);
        String id = cp.getCheckpointId();

        assertNotNull(id);
        assertEquals(32, id.length());
        assertFalse(id.contains("-"));
    }

    @Test
    void testSetSerializedState() {
        Map<String, Object> state = new HashMap<>();
        state.put("key", "value");

        Checkpoint cp = new Checkpoint("exec-1", "node-1", state);

        byte[] serialized = new byte[]{1, 2, 3, 4};
        cp.setSerializedState(serialized);

        assertArrayEquals(serialized, cp.getSerializedState());
    }

    @Test
    void testMultipleCheckpointsHaveDifferentIds() {
        Map<String, Object> state = new HashMap<>();
        state.put("key", "value");

        Checkpoint cp1 = new Checkpoint("exec-1", "node-1", state);
        Checkpoint cp2 = new Checkpoint("exec-1", "node-1", state);

        assertNotEquals(cp1.getCheckpointId(), cp2.getCheckpointId());
    }

    @Test
    void testEmptyState() {
        Checkpoint cp = new Checkpoint("exec-1", "node-1", new HashMap<>());
        assertTrue(cp.getState().isEmpty());
    }
}
