package com.shiyu.ai.memory.shortterm;
import org.junit.jupiter.api.Tag;

import com.shiyu.ai.dal.bo.memory.ConversationMessageBO;
import com.shiyu.ai.dal.repository.ConversationMessageRepository;
import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;
import com.shiyu.ai.memory.spi.MemoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ShortTermMemoryStore 单元测试
 */
@ExtendWith(MockitoExtension.class)
@Tag("dev")
class ShortTermMemoryStoreTest {

    @Mock
    private ConversationMessageRepository repository;

    private ShortTermMemoryStore store;

    @BeforeEach
    void setUp() {
        store = new ShortTermMemoryStore(repository, 10);
    }

    @Test
    void testSaveMemory() {
        Memory memory = new Memory(MemoryType.SHORT_TERM, "session-1", "user", "Hello");
        memory.setUserId(1L);
        memory.setAgentId("agent-1");

        store.save(memory);

        verify(repository, times(1)).insert(any(ConversationMessageBO.class));
    }

    @Test
    void testSaveBatch() {
        Memory m1 = new Memory(MemoryType.SHORT_TERM, "session-1", "user", "Hello");
        Memory m2 = new Memory(MemoryType.SHORT_TERM, "session-1", "assistant", "Hi");

        store.saveBatch(List.of(m1, m2));

        verify(repository, times(2)).insert(any(ConversationMessageBO.class));
    }

    @Test
    void testQueryWithCaching() {
        ConversationMessageBO msg = new ConversationMessageBO();
        msg.setSessionId("session-1");
        msg.setRole("user");
        msg.setContent("Hello");
        msg.setUserId(1L);
        msg.setCreateTime(LocalDateTime.now());

        when(repository.selectRecentBySession("session-1", 10)).thenReturn(List.of(msg));

        MemoryQuery query = MemoryQuery.builder()
            .sessionId("session-1")
            .topK(10)
            .build();

        List<Memory> results = store.query(query);

        assertEquals(1, results.size());
        assertEquals("user", results.get(0).getRole());
        assertEquals("Hello", results.get(0).getContent());

        // Second query should use cache
        List<Memory> cachedResults = store.query(query);
        assertEquals(1, cachedResults.size());
        // Repository should only be called once
        verify(repository, times(1)).selectRecentBySession(anyString(), anyInt());
    }

    @Test
    void testQueryNullSessionId() {
        MemoryQuery query = MemoryQuery.builder().build();

        List<Memory> results = store.query(query);
        assertTrue(results.isEmpty());
    }

    @Test
    void testDeleteBySession() {
        store.deleteBySession("session-1");
        verify(repository, times(1)).deleteBySession("session-1");
    }

    @Test
    void testCount() {
        when(repository.countBySession("session-1")).thenReturn(5L);

        MemoryQuery query = MemoryQuery.builder()
            .sessionId("session-1")
            .build();

        assertEquals(5, store.count(query));
    }

    @Test
    void testCountNullSessionId() {
        MemoryQuery query = MemoryQuery.builder().build();

        assertEquals(0, store.count(query));
    }

    @Test
    void testDefaultWindowSize() {
        ShortTermMemoryStore defaultStore = new ShortTermMemoryStore(repository);
        assertEquals(10, defaultStore.getWindowSize());
    }

    @Test
    void testCustomWindowSize() {
        assertEquals(10, store.getWindowSize());
    }
}
