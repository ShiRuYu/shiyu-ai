package com.shiyu.ai.memory.spi.impl;

import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;
import com.shiyu.ai.memory.spi.MemoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WorkingMemoryStore 单元测试
 */
@Tag("dev")
class WorkingMemoryStoreTest {

    private WorkingMemoryStore store;

    @BeforeEach
    void setUp() {
        store = new WorkingMemoryStore();
    }

    @Test
    void testSetAndGetVariable() {
        store.setVariable("session-1", "name", "Alice");
        store.setVariable("session-1", "age", 25);

        assertEquals("Alice", store.getVariable("session-1", "name"));
        assertEquals(Integer.valueOf(25), store.getVariable("session-1", "age"));
    }

    @Test
    void testGetVariableNotFound() {
        assertNull(store.getVariable("session-1", "nonexistent"));
    }

    @Test
    void testGetVariableFromNonexistentSession() {
        assertNull(store.getVariable("nonexistent-session", "key"));
    }

    @Test
    void testClearSession() {
        store.setVariable("session-1", "name", "Alice");
        store.setVariable("session-1", "age", 25);

        store.clear("session-1");

        assertNull(store.getVariable("session-1", "name"));
        assertNull(store.getVariable("session-1", "age"));
    }

    @Test
    void testGetAllVariables() {
        store.setVariable("session-1", "name", "Alice");
        store.setVariable("session-1", "age", 25);

        var all = store.getAllVariables("session-1");

        assertEquals(2, all.size());
        assertEquals("Alice", all.get("name"));
        assertEquals(Integer.valueOf(25), all.get("age"));
    }

    @Test
    void testGetAllVariablesEmptySession() {
        var all = store.getAllVariables("nonexistent");
        assertTrue(all.isEmpty());
    }

    @Test
    void testSaveMemory() {
        Memory memory = new Memory(MemoryType.WORKING, "session-1", "system", "test-content");
        memory.setMemoryKey("test-key");
        store.save(memory);

        assertEquals("test-content", store.getVariable("session-1", "test-key"));
    }

    @Test
    void testSaveBatch() {
        Memory m1 = new Memory(MemoryType.WORKING, "session-1", "system", "content-1");
        m1.setMemoryKey("key-1");
        Memory m2 = new Memory(MemoryType.WORKING, "session-1", "system", "content-2");
        m2.setMemoryKey("key-2");

        store.saveBatch(List.of(m1, m2));

        assertEquals("content-1", store.getVariable("session-1", "key-1"));
        assertEquals("content-2", store.getVariable("session-1", "key-2"));
    }

    @Test
    void testQuery() {
        store.setVariable("session-1", "lang", "Java");
        store.setVariable("session-1", "version", "21");

        MemoryQuery query = MemoryQuery.builder()
            .sessionId("session-1")
            .topK(10)
            .build();

        List<Memory> results = store.query(query);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(m -> "lang".equals(m.getMemoryKey())));
        assertTrue(results.stream().anyMatch(m -> "version".equals(m.getMemoryKey())));
    }

    @Test
    void testQueryLimitsTopK() {
        store.setVariable("session-1", "a", "1");
        store.setVariable("session-1", "b", "2");
        store.setVariable("session-1", "c", "3");

        MemoryQuery query = MemoryQuery.builder()
            .sessionId("session-1")
            .topK(2)
            .build();

        List<Memory> results = store.query(query);
        assertTrue(results.size() <= 2);
    }

    @Test
    void testQueryNonexistentSession() {
        MemoryQuery query = MemoryQuery.builder()
            .sessionId("nonexistent")
            .build();

        List<Memory> results = store.query(query);
        assertTrue(results.isEmpty());
    }

    @Test
    void testDeleteBySession() {
        store.setVariable("session-1", "name", "Alice");
        store.deleteBySession("session-1");

        assertTrue(store.getAllVariables("session-1").isEmpty());
    }

    @Test
    void testCount() {
        store.setVariable("session-1", "a", "1");
        store.setVariable("session-1", "b", "2");

        MemoryQuery query = MemoryQuery.builder()
            .sessionId("session-1")
            .build();

        assertEquals(2, store.count(query));
    }

    @Test
    void testCountNonexistentSession() {
        MemoryQuery query = MemoryQuery.builder()
            .sessionId("nonexistent")
            .build();

        assertEquals(0, store.count(query));
    }

    @Test
    void testSessionsAreIndependent() {
        store.setVariable("session-1", "name", "Alice");
        store.setVariable("session-2", "name", "Bob");

        assertEquals("Alice", store.getVariable("session-1", "name"));
        assertEquals("Bob", store.getVariable("session-2", "name"));

        store.clear("session-1");
        assertNull(store.getVariable("session-1", "name"));
        assertEquals("Bob", store.getVariable("session-2", "name"));
    }
}
