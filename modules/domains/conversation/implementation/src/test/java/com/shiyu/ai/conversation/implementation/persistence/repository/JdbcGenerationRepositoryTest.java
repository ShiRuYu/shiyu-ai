package com.shiyu.ai.conversation.implementation.persistence.repository;

import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.runtime.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
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

@SuppressWarnings({"unchecked", "rawtypes"})
class JdbcGenerationRepositoryTest {
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void persistsQueriesUpdatesAndMapsGenerationRuns() throws Exception {
        AiRunRepository runtime = mock(AiRunRepository.class);
        JdbcGenerationRepository repository = new JdbcGenerationRepository(mock(DataSource.class), runtime, 5);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Field field = JdbcGenerationRepository.class.getDeclaredField("jdbc");
        field.setAccessible(true); field.set(repository, jdbc);
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        GenerationRun run = new GenerationRun("g1", "c1", "m1", null, "speaker", "OPENAI", "gpt",
                GenerationStatus.RUNNING, 2, 3, 10, null, 1, false, 1, now, now, "r1");
        when(jdbc.queryForObject(startsWith("SELECT TENANT_ID"), eq(Long.class), eq("c1"))).thenReturn(7L);
        repository.insert(run);
        verify(jdbc, times(2)).update(anyString(), any(Object[].class));
        when(jdbc.queryForObject(contains("INPUT_MESSAGE_ID"), eq(Integer.class), any(), any(), any())).thenReturn(1);
        when(jdbc.queryForObject(contains("CONVERSATION_ID=? AND TENANT_ID=?"), eq(Integer.class), any(), any())).thenReturn(1);
        assertTrue(repository.hasRunning("c1", "m1", new TenantId(7)));
        assertTrue(repository.hasRunningConversation("c1", new TenantId(7)));
        when(jdbc.queryForObject(contains("INPUT_MESSAGE_ID"), eq(Integer.class), any(), any(), any())).thenReturn(null);
        assertFalse(repository.hasRunning("c1", "m1", new TenantId(7)));

        ResultSet row = mock(ResultSet.class);
        when(row.getString("ID")).thenReturn("g1");
        when(row.getString("CONVERSATION_ID")).thenReturn("c1");
        when(row.getString("INPUT_MESSAGE_ID")).thenReturn("m1");
        when(row.getString("ASSISTANT_MESSAGE_ID")).thenReturn(null);
        when(row.getString("SPEAKER_ID")).thenReturn("speaker");
        when(row.getString("PLATFORM")).thenReturn("OPENAI");
        when(row.getString("MODEL")).thenReturn("gpt");
        when(row.getString("STATUS")).thenReturn("RUNNING");
        when(row.getLong("PROMPT_TOKENS")).thenReturn(2L);
        when(row.getLong("COMPLETION_TOKENS")).thenReturn(3L);
        when(row.getLong("LATENCY_MS")).thenReturn(10L);
        when(row.getString("ERROR_CODE")).thenReturn(null);
        when(row.getInt("LAST_EVENT_SEQUENCE")).thenReturn(1);
        when(row.getBoolean("CANCEL_REQUESTED")).thenReturn(false);
        when(row.getLong("VERSION")).thenReturn(1L);
        when(row.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(now));
        when(row.getTimestamp("UPDATED_AT")).thenReturn(Timestamp.from(now));
        when(row.getString("RUNTIME_RUN_ID")).thenReturn("r1");
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenAnswer(inv -> {
            RowMapper mapper = inv.getArgument(1); return List.of(mapper.mapRow(row, 0));
        });
        assertEquals("g1", repository.find("g1", new TenantId(7), 8).orElseThrow().id());
        assertEquals(1, repository.listConversation("c1", new TenantId(7), 10000).size());

        GenerationRun completed = run.transition(GenerationStatus.COMPLETED);
        when(jdbc.update(startsWith("UPDATE CHAT_GENERATION_RUN"), any(Object[].class))).thenReturn(1);
        assertEquals(1, repository.update(completed, 1));
        verify(jdbc).update(startsWith("DELETE FROM CHAT_GENERATION_ACTIVE"), eq("g1"));

        when(jdbc.queryForObject(contains("COALESCE(MAX"), eq(Integer.class), eq("g1"), eq(7L), eq(7L))).thenReturn(null);
        assertEquals(0, repository.nextEventSequence("g1", new TenantId(7)));
        when(jdbc.queryForObject(contains("COALESCE(MAX"), eq(Integer.class), eq("g1"), eq(7L), eq(7L))).thenReturn(4);
        assertEquals(4, repository.nextEventSequence("g1", new TenantId(7)));

        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenAnswer(inv -> {
            RowMapper mapper = inv.getArgument(1);
            ResultSet event = mock(ResultSet.class);
            when(event.getInt("SEQ")).thenReturn(2); when(event.getString("TYPE")).thenReturn("MODEL_DELTA");
            when(event.getString("PAYLOAD")).thenReturn("hello"); when(event.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(now));
            return List.of(mapper.mapRow(event, 0));
        });
        assertEquals(GenerationEventType.DELTA, repository.listEvents("g1", new TenantId(7), 0, 2000).get(0).type());
        assertThrows(IllegalArgumentException.class, () -> repository.appendEvent(new GenerationEvent("g1", 1, GenerationEventType.DELTA, "x", now), new TenantId(7)));
    }

    @Test
    void handlesMissingConversationDuplicateReservationAndStaleRecovery() throws Exception {
        AiRunRepository runtime = mock(AiRunRepository.class);
        JdbcGenerationRepository repository = new JdbcGenerationRepository(mock(DataSource.class), runtime, 1000);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Field field = JdbcGenerationRepository.class.getDeclaredField("jdbc"); field.setAccessible(true); field.set(repository, jdbc);
        Instant now = Instant.now();
        GenerationRun run = new GenerationRun("g1", "c1", "m1", null, null, "OPENAI", "gpt", GenerationStatus.CREATED,
                0, 0, 0, null, -1, false, 0, now, now);
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq("c1"))).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> repository.insert(run));
        when(jdbc.queryForObject(anyString(), eq(Long.class), eq("c1"))).thenReturn(7L);
        when(jdbc.update(startsWith("INSERT INTO CHAT_GENERATION_ACTIVE"), any(Object[].class))).thenThrow(new DuplicateKeyException("duplicate"));
        assertThrows(IllegalStateException.class, () -> repository.insert(run));
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());
        assertEquals(0, repository.recoverStaleGenerations(1));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void recoversStaleGenerationAndRepairsRuntimeProjection() throws Exception {
        AiRunRepository runtime = mock(AiRunRepository.class);
        JdbcGenerationRepository repository = new JdbcGenerationRepository(mock(DataSource.class), runtime, 1000);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Field field = JdbcGenerationRepository.class.getDeclaredField("jdbc"); field.setAccessible(true); field.set(repository, jdbc);
        Instant created = Instant.now().minusSeconds(20);
        GenerationRun running = new GenerationRun("g-stale", "c1", "m1", null, "speaker", "OPENAI", "gpt",
                GenerationStatus.RUNNING, 2, 3, 0, null, 4, false, 2, created, created, "r-stale");
        ResultSet row = generationRow(running);
        when(row.getLong("OWNER_USER_ID")).thenReturn(8L);
        when(jdbc.query(startsWith("SELECT g.*,c.OWNER_USER_ID"), any(RowMapper.class), any(Object[].class))).thenAnswer(inv -> {
            RowMapper mapper = inv.getArgument(1);
            return List.of(mapper.mapRow(row, 0));
        });
        when(jdbc.queryForObject(startsWith("SELECT TENANT_ID"), eq(Long.class), eq("g-stale"))).thenReturn(7L);
        when(jdbc.update(startsWith("UPDATE CHAT_GENERATION_RUN"), any(Object[].class))).thenReturn(1);
        AiRun run = new AiRun("r-stale", new TenantId(7), new UserId(8), null, null, AiRunSource.GENERATION, "g-stale", null, null,
                "c1", "g-stale", null, "gpt", "hash", AiRunStatus.RUNNING, 2, 3, false, null, created, null, null, 2, 3);
        when(runtime.find("r-stale", new TenantId(7), 8)).thenReturn(Optional.of(run));
        assertEquals(1, repository.recoverStaleGenerations(1000));
        verify(runtime).updateTerminalAndAppend(any(AiRun.class), eq(2L), eq(AiRunEventType.RUN_FAILED),
                contains("SERVICE_RESTART"), eq(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    void appendsEventsToLinkedRuntimeAndRejectsBlankLink() throws Exception {
        AiRunRepository runtime = mock(AiRunRepository.class);
        JdbcGenerationRepository repository = new JdbcGenerationRepository(mock(DataSource.class), runtime, 1000);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Field field = JdbcGenerationRepository.class.getDeclaredField("jdbc"); field.setAccessible(true); field.set(repository, jdbc);
        when(jdbc.query(startsWith("SELECT g.RUNTIME_RUN_ID"), any(org.springframework.jdbc.core.ResultSetExtractor.class), eq("g1"), eq(7L)))
                .thenAnswer(inv -> {
                    org.springframework.jdbc.core.ResultSetExtractor<?> extractor = inv.getArgument(1);
                    ResultSet row = mock(ResultSet.class); when(row.next()).thenReturn(true);
                    when(row.getString("RUNTIME_RUN_ID")).thenReturn("r1"); when(row.getLong("OWNER_USER_ID")).thenReturn(8L);
                    return extractor.extractData(row);
                });
        Instant now = Instant.now();
        repository.appendEvent(new GenerationEvent("g1", 1, GenerationEventType.USAGE, "{}", now), new TenantId(7));
        verify(runtime).appendNextEvent(eq("r1"), eq(new TenantId(7)), eq(8L), eq(AiRunEventType.MODEL_USAGE), eq("{}"), eq(true), eq(now));

        when(jdbc.query(startsWith("SELECT g.RUNTIME_RUN_ID"), any(org.springframework.jdbc.core.ResultSetExtractor.class), eq("g2"), eq(7L)))
                .thenAnswer(inv -> {
                    org.springframework.jdbc.core.ResultSetExtractor<?> extractor = inv.getArgument(1);
                    ResultSet row = mock(ResultSet.class); when(row.next()).thenReturn(true);
                    when(row.getString("RUNTIME_RUN_ID")).thenReturn(" "); when(row.getLong("OWNER_USER_ID")).thenReturn(8L);
                    return extractor.extractData(row);
                });
        assertThrows(IllegalStateException.class, () -> repository.appendEvent(new GenerationEvent("g2", 1, GenerationEventType.DELTA, "x", now), new TenantId(7)));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void mapsEveryGenerationEventTypeAndHandlesEmptyQueries() throws Exception {
        AiRunRepository runtime = mock(AiRunRepository.class);
        JdbcGenerationRepository repository = new JdbcGenerationRepository(mock(DataSource.class), runtime, 1000);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Field field = JdbcGenerationRepository.class.getDeclaredField("jdbc"); field.setAccessible(true); field.set(repository, jdbc);
        Instant now = Instant.now();
        when(jdbc.query(startsWith("SELECT g.RUNTIME_RUN_ID"), any(org.springframework.jdbc.core.ResultSetExtractor.class), any(), any()))
                .thenAnswer(inv -> {
                    var extractor = (org.springframework.jdbc.core.ResultSetExtractor<?>) inv.getArgument(1);
                    ResultSet row = mock(ResultSet.class); when(row.next()).thenReturn(true); when(row.getString("RUNTIME_RUN_ID")).thenReturn("r1"); when(row.getLong("OWNER_USER_ID")).thenReturn(8L);
                    return extractor.extractData(row);
                });
        for (GenerationEventType type : GenerationEventType.values()) {
            repository.appendEvent(new GenerationEvent("g1", 1, type, "{}", now), new TenantId(7));
        }
        verify(runtime, times(GenerationEventType.values().length)).appendNextEvent(eq("r1"), eq(new TenantId(7)), eq(8L), any(AiRunEventType.class), eq("{}"), eq(true), eq(now));

        when(jdbc.query(startsWith("SELECT e.SEQ,e.TYPE"), any(RowMapper.class), any(Object[].class))).thenAnswer(inv -> {
            RowMapper mapper = inv.getArgument(1); ResultSet row = mock(ResultSet.class);
            when(row.getInt("SEQ")).thenReturn(1); when(row.getString("PAYLOAD")).thenReturn("{}"); when(row.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(now));
            when(row.getString("TYPE")).thenReturn(invocationType(inv));
            return List.of(mapper.mapRow(row, 0));
        });
        for (AiRunEventType type : AiRunEventType.values()) {
            when(jdbc.query(startsWith("SELECT e.SEQ,e.TYPE"), any(RowMapper.class), any(Object[].class))).thenAnswer(inv -> {
                RowMapper mapper = inv.getArgument(1); ResultSet row = mock(ResultSet.class);
                when(row.getInt("SEQ")).thenReturn(1); when(row.getString("TYPE")).thenReturn(type.name()); when(row.getString("PAYLOAD")).thenReturn("{}"); when(row.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(now));
                return List.of(mapper.mapRow(row, 0));
            });
            assertEquals(1, repository.listEvents("g1", new TenantId(7), 0, 1).size());
        }
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());
        assertTrue(repository.find("missing", new TenantId(7), 8).isEmpty());
    }

    private static String invocationType(org.mockito.invocation.InvocationOnMock ignored) {
        return AiRunEventType.MODEL_DELTA.name();
    }

    private static ResultSet generationRow(GenerationRun run) throws Exception {
        ResultSet row = mock(ResultSet.class);
        when(row.getString("ID")).thenReturn(run.id()); when(row.getString("CONVERSATION_ID")).thenReturn(run.conversationId());
        when(row.getString("INPUT_MESSAGE_ID")).thenReturn(run.inputMessageId()); when(row.getString("ASSISTANT_MESSAGE_ID")).thenReturn(run.assistantMessageId());
        when(row.getString("SPEAKER_ID")).thenReturn(run.speakerId()); when(row.getString("PLATFORM")).thenReturn(run.platform());
        when(row.getString("MODEL")).thenReturn(run.model()); when(row.getString("STATUS")).thenReturn(run.status().name());
        when(row.getLong("PROMPT_TOKENS")).thenReturn(run.promptTokens()); when(row.getLong("COMPLETION_TOKENS")).thenReturn(run.completionTokens());
        when(row.getLong("LATENCY_MS")).thenReturn(run.latencyMs()); when(row.getString("ERROR_CODE")).thenReturn(run.errorCode());
        when(row.getInt("LAST_EVENT_SEQUENCE")).thenReturn(run.lastEventSequence()); when(row.getBoolean("CANCEL_REQUESTED")).thenReturn(run.cancelRequested());
        when(row.getLong("VERSION")).thenReturn(run.version()); when(row.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(run.createdAt()));
        when(row.getTimestamp("UPDATED_AT")).thenReturn(Timestamp.from(run.updatedAt())); when(row.getString("RUNTIME_RUN_ID")).thenReturn(run.runtimeRunId());
        return row;
    }
}
