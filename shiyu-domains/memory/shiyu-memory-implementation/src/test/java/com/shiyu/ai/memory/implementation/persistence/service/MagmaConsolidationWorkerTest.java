package com.shiyu.ai.memory.implementation.persistence.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.sql.ResultSet;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MagmaConsolidationWorkerTest {
    @Test
    void leasesAndCompletesPendingJobs() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any(), any(Object[].class)))
                .thenReturn(List.of(1L));
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any())).thenReturn(List.of(1L));
        when(jdbc.update(anyString(), any(), any())).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForMap(anyString(), any(Object[].class)))
                .thenReturn(Map.of("TENANT_ID", 7L, "EVENT_ID", "e1"))
                .thenReturn(Map.of("STATUS", "ACTIVE", "SUBJECT_TYPE", "PROFILE", "SUBJECT_ID", "u1",
                        "CONTENT", "hello world", "NAMESPACE", "notes", "ATTRIBUTES", "{}"));
        when(jdbc.queryForObject(anyString(), eq(Integer.class), any(Object[].class))).thenReturn(0);
        MagmaConsolidationWorker worker = newWorker(jdbc);

        worker.processBatch();

        verify(jdbc, atLeast(2)).update(anyString(), any(Object[].class));
    }

    @Test
    void recordsRetryWhenConsolidationFails() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any(), any(Object[].class)))
                .thenReturn(List.of(2L));
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any())).thenReturn(List.of(2L));
        when(jdbc.update(anyString(), any(), any())).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForMap(anyString(), any(Object[].class))).thenThrow(new IllegalStateException("db unavailable"));
        MagmaConsolidationWorker worker = newWorker(jdbc);

        worker.processBatch();

        verify(jdbc, atLeast(2)).update(anyString(), any(Object[].class));
    }

    @Test
    void skipsJobsClaimedByAnotherWorker() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of(3L));
        when(jdbc.update(anyString(), any(), any())).thenReturn(0);

        newWorker(jdbc).processBatch();

        verify(jdbc, never()).queryForMap(anyString(), any(Object[].class));
    }

    @Test
    void ignoresRevokedEventsAndCompletesTheLease() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of(4L));
        when(jdbc.update(anyString(), any(), any())).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForMap(anyString(), any(Object[].class)))
                .thenReturn(Map.of("TENANT_ID", 7L, "EVENT_ID", "revoked"))
                .thenReturn(Map.of("STATUS", "REVOKED"));

        newWorker(jdbc).processBatch();

        verify(jdbc, atLeast(2)).update(anyString(), any(Object[].class));
    }

    @Test
    void buildsSemanticAndCausalEdgesFromRelatedEvents() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of(5L));
        when(jdbc.update(anyString(), any(), any())).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForMap(anyString(), any(Object[].class)))
                .thenReturn(Map.of("TENANT_ID", 7L, "EVENT_ID", "e1"))
                .thenReturn(Map.of("STATUS", "ACTIVE", "SUBJECT_TYPE", "PROFILE", "SUBJECT_ID", "u1",
                        "CONTENT", "hello world", "NAMESPACE", "notes",
                        "ATTRIBUTES", "{\"causesEventId\":\"cause-1\"}"));
        when(jdbc.queryForObject(anyString(), eq(Integer.class), any(Object[].class)))
                .thenReturn(0, 1, 1, 0);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Object>>any(), any(Object[].class)))
                .thenAnswer(invocation -> {
                    String sql = invocation.getArgument(0, String.class);
                    if (!sql.contains("MEMORY_EVENT")) return List.of();
                    @SuppressWarnings("unchecked") RowMapper<Object> mapper = invocation.getArgument(1);
                    ResultSet rs = mock(ResultSet.class);
                    when(rs.getString(1)).thenReturn("e2");
                    when(rs.getString(2)).thenReturn("hello there");
                    mapper.mapRow(rs, 0);
                    return List.of();
                });

        newWorker(jdbc).processBatch();

        verify(jdbc, atLeast(4)).update(anyString(), any(Object[].class));
    }

    @Test
    void handlesEmptyAndShortTokensWithoutCreatingSemanticEdges() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of(6L));
        when(jdbc.update(anyString(), any(), any())).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForMap(anyString(), any(Object[].class)))
                .thenReturn(Map.of("TENANT_ID", 7L, "EVENT_ID", "e-empty"))
                .thenReturn(Map.of("STATUS", "ACTIVE", "SUBJECT_TYPE", "PROFILE", "SUBJECT_ID", "u1",
                        "CONTENT", "", "NAMESPACE", "notes", "ATTRIBUTES", "{}"));
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Object>>any(), any(Object[].class)))
                .thenReturn(List.of());

        newWorker(jdbc).processBatch();

        verify(jdbc, atLeast(2)).update(anyString(), any(Object[].class));
    }

    private static MagmaConsolidationWorker newWorker(JdbcTemplate jdbc) throws Exception {
        MagmaConsolidationWorker worker = new MagmaConsolidationWorker(mock(DataSource.class));
        Field field = MagmaConsolidationWorker.class.getDeclaredField("jdbc");
        field.setAccessible(true);
        field.set(worker, jdbc);
        return worker;
    }
}
