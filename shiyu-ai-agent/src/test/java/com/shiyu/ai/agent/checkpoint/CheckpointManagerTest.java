package com.shiyu.ai.agent.checkpoint;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CheckpointManager 单元测试
 */
@ExtendWith(MockitoExtension.class)
@Tag("dev")
class CheckpointManagerTest {

    @Mock
    private CheckpointStore checkpointStore;

    private CheckpointManager checkpointManager;

    @BeforeEach
    void setUp() {
        checkpointManager = new CheckpointManager(checkpointStore);
    }

    @Test
    void testCreateCheckpoint() {
        Map<String, Object> state = new HashMap<>();
        state.put("step", "test");

        Checkpoint result = checkpointManager.createCheckpoint("exec-1", "node-1", state);

        assertNotNull(result);
        assertEquals("exec-1", result.getExecutionId());
        assertEquals("node-1", result.getNodeId());
        assertEquals(state, result.getState());
        verify(checkpointStore, times(1)).save(any(Checkpoint.class));
    }

    @Test
    void testLoadLatestCheckpoint() {
        Map<String, Object> state = new HashMap<>();
        state.put("step", "latest");
        Checkpoint expected = new Checkpoint("exec-1", "node-2", state);
        when(checkpointStore.loadByExecutionId("exec-1")).thenReturn(expected);

        Checkpoint result = checkpointManager.loadLatestCheckpoint("exec-1");

        assertNotNull(result);
        assertEquals("node-2", result.getNodeId());
        verify(checkpointStore, times(1)).loadByExecutionId("exec-1");
    }

    @Test
    void testLoadLatestCheckpointNotFound() {
        when(checkpointStore.loadByExecutionId("exec-not-found")).thenReturn(null);

        Checkpoint result = checkpointManager.loadLatestCheckpoint("exec-not-found");

        assertNull(result);
    }

    @Test
    void testLoadCheckpoint() {
        Map<String, Object> state = new HashMap<>();
        state.put("step", "specific");
        Checkpoint expected = new Checkpoint("exec-1", "node-3", state);
        when(checkpointStore.load("cp-123")).thenReturn(expected);

        Checkpoint result = checkpointManager.loadCheckpoint("cp-123");

        assertNotNull(result);
        assertEquals("node-3", result.getNodeId());
    }

    @Test
    void testCleanCheckpoints() {
        checkpointManager.cleanCheckpoints("exec-1");

        verify(checkpointStore, times(1)).deleteByExecutionId("exec-1");
    }

    @Test
    void testCreateCheckpointCallsStore() {
        checkpointManager.createCheckpoint("exec-1", "node-1", new HashMap<>());
        checkpointManager.createCheckpoint("exec-1", "node-2", new HashMap<>());

        verify(checkpointStore, times(2)).save(any(Checkpoint.class));
    }
}
