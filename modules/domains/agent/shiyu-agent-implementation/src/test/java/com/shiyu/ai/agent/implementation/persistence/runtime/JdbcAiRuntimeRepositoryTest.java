package com.shiyu.ai.agent.implementation.persistence.runtime;

import com.shiyu.ai.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class JdbcAiRuntimeRepositoryTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final JdbcAiRuntimeRepository repository = new JdbcAiRuntimeRepository(jdbc);
    private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

    @Test
    void persistsAndQueriesTenantScopedRuns() {
        AiRun run = run(AiRunStatus.RUNNING);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());
        when(jdbc.query(anyString(), any(ResultSetExtractor.class), any(Object[].class)))
                .thenReturn(Optional.empty());

        repository.insert(run);
        assertTrue(repository.find("run", new TenantId(7), 9).isEmpty());
        assertTrue(repository.list(new TenantId(7), 9, 0).isEmpty());
        assertTrue(repository.findByGeneration("generation", new TenantId(7), 9).isEmpty());
        assertEquals(1, repository.linkGeneration("run", new TenantId(7), 9, "generation"));
        assertTrue(repository.findByExecution("execution", new TenantId(7), 9).isEmpty());
        assertEquals(1, repository.update(run, 0));
        verify(jdbc).update(startsWith("INSERT INTO AI_RUN"), any(Object[].class));
    }

    @Test
    void appendsEventsIdempotentlyAndProtectsTerminalRuns() {
        AiRun run = run(AiRunStatus.RUNNING);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        doAnswer((Answer<Object>) invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.startsWith("SELECT LAST_EVENT_SEQ")) return 0L;
            return List.of();
        }).when(jdbc).query(anyString(), any(ResultSetExtractor.class), any(Object[].class));
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());

        assertEquals(1, repository.appendNextEvent("run", new TenantId(7), 9, AiRunEventType.MODEL_STARTED,
                null, false, now));
        assertEquals(1, repository.updateTerminalAndAppend(run, 0, AiRunEventType.RUN_COMPLETED,
                "done", false).lastEventSeq());

        AiRunEvent existing = event(2, AiRunEventType.RUN_COMPLETED, "done", false);
        when(jdbc.query(contains("TYPE IN"), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of(existing));
        assertEquals(2, repository.appendNextEvent("run", new TenantId(7), 9, AiRunEventType.RUN_COMPLETED,
                "done", false, now));
        assertThrows(IllegalStateException.class,
                () -> repository.appendNextEvent("run", new TenantId(7), 9, AiRunEventType.MODEL_DELTA,
                        "delta", false, now));

        doAnswer((Answer<Object>) invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.startsWith("SELECT LAST_EVENT_SEQ")) return 0L;
            return List.of();
        }).when(jdbc).query(anyString(), any(ResultSetExtractor.class), any(Object[].class));
        when(jdbc.queryForObject(contains("COALESCE(MAX"), eq(Long.class), any(Object[].class))).thenReturn(0L);
        when(jdbc.queryForObject(contains("COUNT(*)"), eq(Integer.class), any(Object[].class))).thenReturn(0);
        assertEquals(1, repository.appendEvent(event(1, AiRunEventType.MODEL_STARTED, "start", false)));
        assertThrows(IllegalStateException.class,
                () -> repository.appendEvent(event(3, AiRunEventType.MODEL_DELTA, "gap", false)));
    }

    @Test
    void mapsRowsAndReportsMissingRunsOrConcurrentChanges() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("ID")).thenReturn("run");
        when(rs.getString("SOURCE_ID")).thenReturn("source");
        when(rs.getString("SOURCE_TYPE")).thenReturn("CONVERSATION");
        when(rs.getString("STATUS")).thenReturn("RUNNING");
        when(rs.getLong("TENANT_ID")).thenReturn(7L);
        when(rs.getLong("OWNER_USER_ID")).thenReturn(9L);
        when(rs.getLong("SEQ")).thenReturn(1L);
        when(rs.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(now));
        when(rs.getTimestamp("COMPLETED_AT")).thenReturn(null);
        Method mapRun = JdbcAiRuntimeRepository.class.getDeclaredMethod("mapRun", ResultSet.class);
        mapRun.setAccessible(true);
        AiRun mapped = (AiRun) mapRun.invoke(repository, rs);
        assertEquals("run", mapped.id());
        assertEquals(AiRunStatus.RUNNING, mapped.status());

        when(rs.getString("RUN_ID")).thenReturn("run");
        when(rs.getString("TYPE")).thenReturn("MODEL_DELTA");
        when(rs.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(now));
        Method mapEvent = JdbcAiRuntimeRepository.class.getDeclaredMethod("mapEvent", ResultSet.class, int.class);
        mapEvent.setAccessible(true);
        AiRunEvent mappedEvent = (AiRunEvent) mapEvent.invoke(repository, rs, 0);
        assertEquals(AiRunEventType.MODEL_DELTA, mappedEvent.type());

        doAnswer((Answer<Object>) invocation -> {
            String sql = invocation.getArgument(0);
            return sql.startsWith("SELECT LAST_EVENT_SEQ") ? null : Optional.empty();
        })
                .when(jdbc).query(anyString(), any(ResultSetExtractor.class), any(Object[].class));
        assertThrows(IllegalArgumentException.class,
                () -> repository.appendNextEvent("missing", new TenantId(7), 9, AiRunEventType.MODEL_STARTED,
                        "", false, now));
        when(jdbc.update(startsWith("UPDATE AI_RUN SET STATUS"), any(Object[].class))).thenReturn(0);
        assertThrows(IllegalStateException.class,
                () -> repository.updateTerminalAndAppend(run(AiRunStatus.COMPLETED), 4,
                        AiRunEventType.RUN_COMPLETED, "", false));
        assertThrows(IllegalArgumentException.class,
                () -> repository.events("missing", new TenantId(7), 9, 0, 10));
    }

    @Test
    void preservesDuplicateSequenceOnlyWhenPayloadMatches() {
        AiRunEvent event = event(1, AiRunEventType.MODEL_STARTED, "start", false);
        doAnswer((Answer<Object>) invocation -> 0L)
                .when(jdbc).query(anyString(), any(ResultSetExtractor.class), any(Object[].class));
        when(jdbc.queryForObject(contains("COALESCE(MAX"), eq(Long.class), any(Object[].class))).thenReturn(0L);
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of(event));
        assertEquals(1, repository.appendEvent(event));
        assertThrows(IllegalStateException.class,
                () -> repository.appendEvent(event(1, AiRunEventType.MODEL_DELTA, "other", false)));

        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());
        doAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.startsWith("UPDATE AI_RUN SET LAST_EVENT_SEQ")) return 1;
            throw new DuplicateKeyException("duplicate");
        }).when(jdbc).update(anyString(), any(Object[].class));
        when(jdbc.queryForObject(startsWith("SELECT * FROM AI_RUN_EVENT"), any(RowMapper.class), any(Object[].class)))
                .thenThrow(new DuplicateKeyException("read duplicate"));
        assertThrows(DuplicateKeyException.class,
                () -> repository.appendNextEvent("run", new TenantId(7), 9, AiRunEventType.MODEL_STARTED,
                "start", false, now));
    }

    @Test
    void validatesExplicitEventSequenceAndDuplicateAppendBranches() {
        AiRunEvent first = event(1, AiRunEventType.MODEL_STARTED, "start", false);
        doAnswer((Answer<Object>) invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.startsWith("SELECT LAST_EVENT_SEQ")) return 0L;
            if (sql.contains("SEQ=?")) return List.of();
            return List.of();
        }).when(jdbc).query(anyString(), any(ResultSetExtractor.class), any(Object[].class));
        when(jdbc.queryForObject(contains("COALESCE(MAX"), eq(Long.class), any(Object[].class))).thenReturn(0L);
        when(jdbc.queryForObject(contains("COUNT(*)"), eq(Integer.class), any(Object[].class))).thenReturn(0);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        assertEquals(1, repository.appendEvent(first));

        // A null MAX result is accepted when the locked run has no events yet.
        doAnswer((Answer<Object>) invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.startsWith("SELECT LAST_EVENT_SEQ")) return null;
            return List.of();
        }).when(jdbc).query(anyString(), any(ResultSetExtractor.class), any(Object[].class));
        when(jdbc.queryForObject(contains("COALESCE(MAX"), eq(Long.class), any(Object[].class))).thenReturn(null);
        when(jdbc.queryForObject(contains("COUNT(*)"), eq(Integer.class), any(Object[].class))).thenReturn(0);
        assertEquals(1, repository.appendEvent(first));

        when(jdbc.queryForObject(contains("COUNT(*)"), eq(Integer.class), any(Object[].class))).thenReturn(1);
        assertThrows(IllegalStateException.class, () -> repository.appendEvent(event(2,
                AiRunEventType.MODEL_DELTA, "delta", false)));

        when(jdbc.queryForObject(contains("COUNT(*)"), eq(Integer.class), any(Object[].class))).thenReturn(0);
        when(jdbc.queryForObject(contains("COALESCE(MAX"), eq(Long.class), any(Object[].class))).thenReturn(0L);
        doAnswer((Answer<Object>) invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.startsWith("SELECT LAST_EVENT_SEQ")) return 0L;
            return List.of();
        }).when(jdbc).query(anyString(), any(ResultSetExtractor.class), any(Object[].class));
        assertThrows(IllegalStateException.class, () -> repository.appendEvent(event(2,
                AiRunEventType.MODEL_DELTA, "gap", false)));

        // Concurrent sequence allocation must fail closed if the guarded update loses the race.
        when(jdbc.queryForObject(contains("COALESCE(MAX"), eq(Long.class), any(Object[].class))).thenReturn(0L);
        when(jdbc.queryForObject(contains("COUNT(*)"), eq(Integer.class), any(Object[].class))).thenReturn(0);
        when(jdbc.update(startsWith("INSERT INTO AI_RUN_EVENT"), any(Object[].class))).thenReturn(1);
        when(jdbc.update(startsWith("UPDATE AI_RUN SET LAST_EVENT_SEQ"), any(Object[].class))).thenReturn(0);
        assertThrows(IllegalStateException.class, () -> repository.appendEvent(event(1,
                AiRunEventType.MODEL_STARTED, "start", false)));
    }

    @Test
    void appendNextEventRejectsLostUpdatesAndAcceptsMatchingDuplicateInsert() {
        doAnswer((Answer<Object>) invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.startsWith("SELECT LAST_EVENT_SEQ")) return 0L;
            return List.of();
        }).when(jdbc).query(anyString(), any(ResultSetExtractor.class), any(Object[].class));
        when(jdbc.query(contains("TYPE IN"), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());
        when(jdbc.update(startsWith("UPDATE AI_RUN SET LAST_EVENT_SEQ"), any(Object[].class)))
                .thenReturn(0);
        assertThrows(IllegalStateException.class, () -> repository.appendNextEvent("run", new TenantId(7), 9,
                AiRunEventType.MODEL_STARTED, "payload", false, now));

        when(jdbc.update(startsWith("UPDATE AI_RUN SET LAST_EVENT_SEQ"), any(Object[].class)))
                .thenReturn(1);
        when(jdbc.update(startsWith("INSERT INTO AI_RUN_EVENT"), any(Object[].class)))
                .thenThrow(new DuplicateKeyException("duplicate"));
        AiRunEvent matching = event(1, AiRunEventType.MODEL_STARTED, "payload", false);
        when(jdbc.queryForObject(startsWith("SELECT * FROM AI_RUN_EVENT"), any(RowMapper.class), any(Object[].class)))
                .thenReturn(matching);
        assertEquals(1, repository.appendNextEvent("run", new TenantId(7), 9, AiRunEventType.MODEL_STARTED,
                "payload", false, now));

        // A terminal event is idempotent only when the complete envelope matches.
        when(jdbc.query(contains("TYPE IN"), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of(event(2, AiRunEventType.RUN_FAILED, "failed", true)));
        assertThrows(IllegalStateException.class, () -> repository.appendNextEvent("run", new TenantId(7), 9,
                AiRunEventType.RUN_COMPLETED, "failed", true, now));
        assertThrows(IllegalStateException.class, () -> repository.appendNextEvent("run", new TenantId(7), 9,
                AiRunEventType.RUN_FAILED, "different", true, now));
        assertThrows(IllegalStateException.class, () -> repository.appendNextEvent("run", new TenantId(7), 9,
                AiRunEventType.RUN_FAILED, "failed", false, now));
    }

    private static AiRun run(AiRunStatus status) {
        return new AiRun("run", new TenantId(7), new UserId(9), "app", "version", AiRunSource.CONVERSATION, "source",
                null, "trace", "conversation", "generation", "execution", "model", "hash", status,
                1, 2, false, "cost", Instant.parse("2025-01-01T00:00:00Z"), null, null, 0, 0);
    }

    private static AiRunEvent event(long seq, AiRunEventType type, String payload, boolean redacted) {
        return new AiRunEvent("run", new TenantId(7), seq, type, payload, redacted,
                Instant.parse("2025-01-01T00:00:00Z"));
    }
}
