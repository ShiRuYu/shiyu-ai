package com.shiyu.ai.memory.implementation.persistence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.common.foundation.jdbc.JdbcDialect;
import com.shiyu.ai.memory.contract.model.*;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

/**
 * 验证 Magma Consolidation Worker 相关功能、边界条件、异常路径和协作行为。
 */
class MagmaConsolidationWorkerTest {
    @Test
    void completesOnlyWithTheClaimedLeaseToken() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        List<UpdateCall> updates = new ArrayList<>();
        captureUpdates(jdbc, updates);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of(8L));
        when(jdbc.queryForMap(anyString(), any(Object[].class)))
                .thenReturn(Map.of("TENANT_ID", 7L, "EVENT_ID", "revoked"))
                .thenReturn(Map.of("STATUS", "REVOKED"));

        newWorker(jdbc).processBatch();

        UpdateCall claim = updateMatching(updates, "STATUS='RUNNING'");
        UpdateCall completion = updateMatching(updates, "STATUS='COMPLETED'");
        assertTrue(claim.sql().contains("LEASED_UNTIL=?"));
        assertTrue(completion.sql().contains("LEASED_UNTIL=?"));
        assertTrue(completion.sql().contains("STATUS='RUNNING'"));
        assertTrue(completion.sql().contains("LEASED_UNTIL>=CURRENT_TIMESTAMP"));
        assertEquals(claim.arguments()[0], completion.arguments()[1]);
    }

    @Test
    void recordsFailureOnlyWithTheClaimedLeaseToken() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        List<UpdateCall> updates = new ArrayList<>();
        captureUpdates(jdbc, updates);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of(9L));
        when(jdbc.queryForMap(anyString(), any(Object[].class)))
                .thenThrow(new IllegalStateException("db unavailable"));

        newWorker(jdbc).processBatch();

        UpdateCall claim = updateMatching(updates, "STATUS='RUNNING'");
        UpdateCall failure = updateMatching(updates, "LAST_ERROR=?");
        assertTrue(claim.sql().contains("LEASED_UNTIL=?"));
        assertTrue(failure.sql().contains("LEASED_UNTIL=?"));
        assertTrue(failure.sql().contains("STATUS='RUNNING'"));
        assertTrue(failure.sql().contains("LEASED_UNTIL>=CURRENT_TIMESTAMP"));
        assertEquals(claim.arguments()[0], failure.arguments()[2]);
    }

    @Test
    void createsEdgesWithDialectUpsertsForSupportedDatabases() throws Exception {
        List<String[]> productsAndClauses =
                List.of(
                        new String[] {
                            "PostgreSQL",
                            "ON CONFLICT",
                            "ON CONFLICT (ID) DO UPDATE SET ACTIVE=EXCLUDED.ACTIVE"
                        },
                        new String[] {
                            "MySQL", "ON DUPLICATE KEY UPDATE", "ACTIVE=VALUES(ACTIVE)"
                        },
                        new String[] {"H2", "MERGE INTO", "KEY(ID)"});

        for (String[] productAndClause : productsAndClauses) {
            JdbcTemplate jdbc = mock(JdbcTemplate.class);
            List<UpdateCall> updates = new ArrayList<>();
            captureUpdates(jdbc, updates);
            when(jdbc.queryForObject(anyString(), eq(Integer.class), any(Object[].class)))
                    .thenReturn(0);
            MagmaConsolidationWorker worker = newWorker(jdbc);
            Field dialect = MagmaConsolidationWorker.class.getDeclaredField("dialect");
            dialect.setAccessible(true);
            dialect.set(worker, JdbcDialect.fromProduct(productAndClause[0]));
            Method edgeIfAbsent =
                    MagmaConsolidationWorker.class.getDeclaredMethod(
                            "edgeIfAbsent",
                            long.class,
                            String.class,
                            String.class,
                            String.class,
                            String.class,
                            double.class,
                            double.class,
                            String.class,
                            String.class);
            edgeIfAbsent.setAccessible(true);

            for (int attempt = 0; attempt < 2; attempt++) {
                edgeIfAbsent.invoke(
                        worker,
                        7L,
                        "source",
                        "target",
                        "SEMANTIC",
                        "similar",
                        0.7d,
                        0.8d,
                        "RULE",
                        "event");
            }
            edgeIfAbsent.invoke(
                    worker,
                    8L,
                    "source",
                    "target",
                    "SEMANTIC",
                    "similar",
                    0.7d,
                    0.8d,
                    "RULE",
                    "event");

            List<UpdateCall> edgeInserts =
                    updates.stream()
                            .filter(update -> update.sql().contains("MEMORY_EDGE"))
                            .toList();
            assertEquals(3, edgeInserts.size());
            assertEquals(edgeInserts.get(0).arguments()[0], edgeInserts.get(1).arguments()[0]);
            assertNotEquals(edgeInserts.get(0).arguments()[0], edgeInserts.get(2).arguments()[0]);
            String edgeInsert = edgeInserts.get(0).sql();
            assertTrue(edgeInsert.contains(productAndClause[1]), productAndClause[0]);
            assertTrue(edgeInsert.contains(productAndClause[2]), productAndClause[0]);
            assertTrue(edgeInsert.contains("ACTIVE"), productAndClause[0]);
        }
    }

    @Test
    void leasesAndCompletesPendingJobs() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(
                        anyString(),
                        org.mockito.ArgumentMatchers.<RowMapper<Long>>any(),
                        any(Object[].class)))
                .thenReturn(List.of(1L));
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of(1L));
        when(jdbc.update(anyString(), any(), any())).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForMap(anyString(), any(Object[].class)))
                .thenReturn(Map.of("TENANT_ID", 7L, "EVENT_ID", "e1"))
                .thenReturn(
                        Map.of(
                                "STATUS",
                                "ACTIVE",
                                "SUBJECT_TYPE",
                                "PROFILE",
                                "SUBJECT_ID",
                                "u1",
                                "CONTENT",
                                "hello world",
                                "NAMESPACE",
                                "notes",
                                "ATTRIBUTES",
                                "{}"));
        when(jdbc.queryForObject(anyString(), eq(Integer.class), any(Object[].class)))
                .thenReturn(0);
        MagmaConsolidationWorker worker = newWorker(jdbc);

        worker.processBatch();

        verify(jdbc, atLeast(2)).update(anyString(), any(Object[].class));
    }

    @Test
    void recordsRetryWhenConsolidationFails() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        List<Object[]> updateArgs = new ArrayList<>();
        when(jdbc.query(
                        anyString(),
                        org.mockito.ArgumentMatchers.<RowMapper<Long>>any(),
                        any(Object[].class)))
                .thenReturn(List.of(2L));
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any()))
                .thenReturn(List.of(2L));
        org.mockito.stubbing.Answer<Integer> captureUpdate =
                invocation -> {
                    Object[] arguments = invocation.getArguments();
                    updateArgs.add(
                            arguments.length == 2 && arguments[1] instanceof Object[] nested
                                    ? nested
                                    : java.util.Arrays.copyOfRange(arguments, 1, arguments.length));
                    return 1;
                };
        when(jdbc.update(anyString(), any(), any())).thenAnswer(captureUpdate);
        when(jdbc.update(anyString(), any(Object[].class))).thenAnswer(captureUpdate);
        when(jdbc.queryForMap(anyString(), any(Object[].class)))
                .thenThrow(new IllegalStateException("db unavailable"));
        MagmaConsolidationWorker worker = newWorker(jdbc);

        worker.processBatch();

        assertTrue(updateArgs.size() >= 2);
        assertEquals("记忆整理失败，请稍后重试", updateArgs.get(updateArgs.size() - 1)[0]);
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
                .thenReturn(
                        Map.of(
                                "STATUS",
                                "ACTIVE",
                                "SUBJECT_TYPE",
                                "PROFILE",
                                "SUBJECT_ID",
                                "u1",
                                "CONTENT",
                                "hello world",
                                "NAMESPACE",
                                "notes",
                                "ATTRIBUTES",
                                "{\"causesEventId\":\"cause-1\"}"));
        when(jdbc.queryForObject(anyString(), eq(Integer.class), any(Object[].class)))
                .thenReturn(0, 1, 1, 0);
        when(jdbc.query(
                        anyString(),
                        org.mockito.ArgumentMatchers.<RowMapper<Object>>any(),
                        any(Object[].class)))
                .thenAnswer(
                        invocation -> {
                            String sql = invocation.getArgument(0, String.class);
                            if (!sql.contains("MEMORY_EVENT")) return List.of();
                            @SuppressWarnings("unchecked")
                            RowMapper<Object> mapper = invocation.getArgument(1);
                            ResultSet rs = mock(ResultSet.class);
                            when(rs.getString(1)).thenReturn("e2");
                            when(rs.getString(2)).thenReturn("hello there");
                            mapper.mapRow(rs, 0);
                            return List.of();
                        });

        newWorker(jdbc).processBatch();

        verify(jdbc, atLeast(4)).update(anyString(), any(Object[].class));
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc, atLeastOnce())
                .query(
                        sql.capture(),
                        org.mockito.ArgumentMatchers.<RowMapper<Object>>any(),
                        any(Object[].class));
        assertTrue(
                sql.getAllValues().stream()
                        .anyMatch(
                                statement ->
                                        statement.contains("ORDER BY OCCURRED_AT DESC, ID ASC")));
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
                .thenReturn(
                        Map.of(
                                "STATUS",
                                "ACTIVE",
                                "SUBJECT_TYPE",
                                "PROFILE",
                                "SUBJECT_ID",
                                "u1",
                                "CONTENT",
                                "",
                                "NAMESPACE",
                                "notes",
                                "ATTRIBUTES",
                                "{}"));
        when(jdbc.query(
                        anyString(),
                        org.mockito.ArgumentMatchers.<RowMapper<Object>>any(),
                        any(Object[].class)))
                .thenReturn(List.of());

        newWorker(jdbc).processBatch();

        verify(jdbc, atLeast(2)).update(anyString(), any(Object[].class));
    }

    private static MagmaConsolidationWorker newWorker(JdbcTemplate jdbc) throws Exception {
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenReturn(7L);
        MagmaConsolidationWorker worker = new MagmaConsolidationWorker(mock(DataSource.class));
        Field field = MagmaConsolidationWorker.class.getDeclaredField("jdbc");
        field.setAccessible(true);
        field.set(worker, jdbc);
        return worker;
    }

    private static void captureUpdates(JdbcTemplate jdbc, List<UpdateCall> updates) {
        org.mockito.stubbing.Answer<Integer> capture =
                invocation -> {
                    Object[] invocationArguments = invocation.getArguments();
                    Object[] parameters =
                            invocationArguments.length == 2
                                            && invocationArguments[1] instanceof Object[] nested
                                    ? nested
                                    : java.util.Arrays.copyOfRange(invocationArguments, 1, invocationArguments.length);
                    updates.add(new UpdateCall((String) invocationArguments[0], parameters));
                    return 1;
                };
        when(jdbc.update(anyString(), any(Object[].class))).thenAnswer(capture);
        when(jdbc.update(anyString(), any(), any())).thenAnswer(capture);
        when(jdbc.update(anyString(), any(), any(), any())).thenAnswer(capture);
    }

    private static UpdateCall updateMatching(List<UpdateCall> updates, String fragment) {
        return updates.stream()
                .filter(update -> update.sql().contains(fragment))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No update matched " + fragment));
    }

    private record UpdateCall(String sql, Object[] arguments) {}
}
