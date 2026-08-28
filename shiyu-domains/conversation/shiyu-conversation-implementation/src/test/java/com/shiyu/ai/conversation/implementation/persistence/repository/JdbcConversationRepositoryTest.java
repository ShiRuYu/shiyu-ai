package com.shiyu.ai.conversation.implementation.persistence.repository;

import com.shiyu.ai.kernel.context.TenantId;

import com.shiyu.ai.conversation.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcConversationRepositoryTest {
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void persistsTenantScopedConversationsAndMessages() throws Exception {
        JdbcConversationRepository repository = new JdbcConversationRepository(mock(DataSource.class));
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Field field = JdbcConversationRepository.class.getDeclaredField("jdbc");
        field.setAccessible(true); field.set(repository, jdbc);
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        Conversation conversation = new Conversation("c1", 7, 8, "chat", "Title", ConversationStatus.ACTIVE,
                null, null, "m1", "summary", "OPENAI", "gpt", 2, now, now);
        ConversationMessage message = new ConversationMessage("m1", "c1", null, null, MessageRole.USER,
                List.of(ContentPart.text("hello")), Map.of("id", "tool"), MessageStatus.COMPLETED, 0, null, now, now);

        repository.insertConversation(conversation);
        repository.updateConversation(conversation, 1);
        repository.deleteConversation("c1", new TenantId(7), 8);
        repository.deleteMessage("m1", new TenantId(7), 8);
        verify(jdbc).update(startsWith("INSERT INTO CHAT_CONVERSATION"), any(Object[].class));
        verify(jdbc, atLeastOnce()).update(startsWith("UPDATE CHAT_CONVERSATION"), any(Object[].class));

        ResultSet row = mock(ResultSet.class);
        when(row.getString("ID")).thenReturn("c1");
        when(row.getLong("TENANT_ID")).thenReturn(7L);
        when(row.getLong("OWNER_USER_ID")).thenReturn(8L);
        when(row.getString("SCENE_TYPE")).thenReturn("chat");
        when(row.getString("TITLE")).thenReturn("Title");
        when(row.getString("STATUS")).thenReturn("ACTIVE");
        when(row.getString("PARENT_CONVERSATION_ID")).thenReturn(null);
        when(row.getString("BRANCH_FROM_MESSAGE_ID")).thenReturn(null);
        when(row.getString("ACTIVE_LEAF_MESSAGE_ID")).thenReturn("m1");
        when(row.getString("ROLLING_SUMMARY")).thenReturn("summary");
        when(row.getString("PLATFORM")).thenReturn("OPENAI");
        when(row.getString("MODEL")).thenReturn("gpt");
        when(row.getLong("VERSION")).thenReturn(2L);
        when(row.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(now));
        when(row.getTimestamp("UPDATED_AT")).thenReturn(Timestamp.from(now));
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenAnswer(invocation -> {
            RowMapper mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(row, 0));
        });
        assertEquals("c1", repository.findConversation("c1", new TenantId(7), 8).orElseThrow().id());
        assertEquals(1, repository.listConversations(new TenantId(7), 8, 1000, -1).size());
        assertEquals(1, repository.listBranches("parent", new TenantId(7), 8).size());

        when(jdbc.queryForObject(startsWith("SELECT TENANT_ID"), eq(Long.class), eq("c1"))).thenReturn(7L);
        repository.insertMessage(message);
        verify(jdbc).update(startsWith("INSERT INTO CHAT_MESSAGE"), any(Object[].class));

        when(row.getString("CONVERSATION_ID")).thenReturn("c1");
        when(row.getString("PARENT_MESSAGE_ID")).thenReturn(null);
        when(row.getString("SOURCE_MESSAGE_ID")).thenReturn(null);
        when(row.getString("ROLE")).thenReturn("USER");
        when(row.getString("CONTENT_PARTS")).thenReturn("[{\"type\":\"text\",\"text\":\"hello\"}]");
        when(row.getString("TOOL_CALL")).thenReturn("{\"id\":\"tool\"}");
        when(row.getString("STATUS")).thenReturn("COMPLETED");
        when(row.getInt("SEQUENCE")).thenReturn(0);
        when(row.getString("GENERATION_ID")).thenReturn(null);
        when(row.getString("CONTENT")).thenReturn("hello");
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenAnswer(invocation -> {
            RowMapper mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(row, 0));
        });
        assertEquals("hello", repository.findMessage("m1", new TenantId(7), 8).orElseThrow().textContent());
        assertEquals(1, repository.listMessages("c1", new TenantId(7), 8, 5000).size());
    }

    @Test
    void rejectsMessageWithoutConversationTenant() throws Exception {
        JdbcConversationRepository repository = new JdbcConversationRepository(mock(DataSource.class));
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Field field = JdbcConversationRepository.class.getDeclaredField("jdbc");
        field.setAccessible(true); field.set(repository, jdbc);
        Instant now = Instant.now();
        ConversationMessage message = new ConversationMessage("m1", "missing", null, null, MessageRole.USER,
                List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, 0, null, now, now);
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq("missing"))).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> repository.insertMessage(message));
    }
}
