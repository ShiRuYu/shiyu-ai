package com.shiyu.ai.memory.implementation.persistence.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.magma.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.time.Instant;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcMagmaMemoryRepositoryTest {
    private static final TenantId TENANT = new TenantId(7L);
    @Test
    void delegatesTenantScopedEventEntityEdgeAndTraceOperations() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        doReturn(List.of()).when(jdbc).query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Object>>any(), any(Object[].class));
        JdbcMagmaMemoryRepository repository = newRepository(jdbc);
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        MemoryEvent event = new MemoryEvent("e1", TENANT, "notes", "PROFILE", "u1", "OBSERVED", "hello",
                now, "TEST", "s1", Map.of("k", "v"), .8, .4, MemoryEventStatus.ACTIVE,
                ConfirmationPolicy.AUTO, now, now);
        MemoryEntity entity = new MemoryEntity("n1", TENANT, "PROFILE", "u1", "User", "user", Map.of(), true);
        MemoryEdge edge = new MemoryEdge("edge1", TENANT, "e1", "n1", GraphType.ENTITY, "about", true,
                .9, .8, EdgeOrigin.RULE, "s1", true, now);
        MemoryRetrievalTrace trace = new MemoryRetrievalTrace("trace1", TENANT, "notes", "hello", List.of("e1"),
                Map.of(GraphType.SEMANTIC, .8), List.of(List.of("e1", "n1")), List.of(), List.of("e1"), now);

        repository.insertEvent(event);
        repository.findEvent(TENANT, "e1");
        repository.findLatestEvent(TENANT, "notes", "PROFILE", "u1");
        repository.findPreviousEvent(TENANT, "notes", "PROFILE", "u1", now);
        repository.findNextEvent(TENANT, "notes", "PROFILE", "u1", now);
        repository.findCandidates(TENANT, "notes", "PROFILE", "u1", 0);
        repository.findByNamespace(TENANT, "notes", 0);
        repository.updateEventStatus(TENANT, "e1", MemoryEventStatus.REVOKED);
        repository.deactivateEdgesForNode(TENANT, "e1");
        repository.upsertEntity(entity);
        repository.insertEdge(edge);
        repository.findEdges(TENANT, "e1", GraphType.ENTITY, 0);
        repository.enqueueConsolidation(TENANT, "e1");
        repository.recordRetrievalTrace(trace);
        repository.findRetrievalTrace(TENANT, "trace1");

        verify(jdbc, atLeast(7)).update(anyString(), any(Object[].class));
        verify(jdbc, atLeast(8)).query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Object>>any(), any(Object[].class));
    }

    @Test
    void mapsRowsAndHandlesEmptyJsonPayloads() throws Exception {
        JdbcMagmaMemoryRepository repository = newRepository(mock(JdbcTemplate.class));
        ResultSet eventRow = mock(ResultSet.class);
        Timestamp now = Timestamp.from(Instant.parse("2026-01-01T00:00:00Z"));
        when(eventRow.getString("ID")).thenReturn("e1");
        when(eventRow.getLong("TENANT_ID")).thenReturn(7L);
        when(eventRow.getString("NAMESPACE")).thenReturn("notes");
        when(eventRow.getString("SUBJECT_TYPE")).thenReturn("PROFILE");
        when(eventRow.getString("SUBJECT_ID")).thenReturn("u1");
        when(eventRow.getString("EVENT_TYPE")).thenReturn("OBSERVED");
        when(eventRow.getString("CONTENT")).thenReturn("hello");
        when(eventRow.getTimestamp(anyString())).thenReturn(now);
        when(eventRow.getString("SOURCE_TYPE")).thenReturn("TEST");
        when(eventRow.getString("SOURCE_ID")).thenReturn("s1");
        when(eventRow.getString("ATTRIBUTES")).thenReturn("");
        when(eventRow.getDouble("CONFIDENCE")).thenReturn(.8);
        when(eventRow.getDouble("IMPORTANCE")).thenReturn(.4);
        when(eventRow.getString("STATUS")).thenReturn("ACTIVE");
        when(eventRow.getString("CONFIRMATION_POLICY")).thenReturn("AUTO");
        Method mapEvent = JdbcMagmaMemoryRepository.class.getDeclaredMethod("mapEvent", ResultSet.class, int.class);
        mapEvent.setAccessible(true);
        mapEvent.invoke(repository, eventRow, 0);

        ResultSet edgeRow = mock(ResultSet.class);
        when(edgeRow.getString("ID")).thenReturn("edge1");
        when(edgeRow.getLong("TENANT_ID")).thenReturn(7L);
        when(edgeRow.getString("SOURCE_NODE_ID")).thenReturn("e1");
        when(edgeRow.getString("TARGET_NODE_ID")).thenReturn("n1");
        when(edgeRow.getString("GRAPH_TYPE")).thenReturn("ENTITY");
        when(edgeRow.getString("RELATION_TYPE")).thenReturn("about");
        when(edgeRow.getBoolean("DIRECTED")).thenReturn(true);
        when(edgeRow.getDouble("WEIGHT")).thenReturn(.9);
        when(edgeRow.getDouble("CONFIDENCE")).thenReturn(.8);
        when(edgeRow.getString("ORIGIN")).thenReturn("RULE");
        when(edgeRow.getString("EVIDENCE_SOURCE")).thenReturn("s1");
        when(edgeRow.getBoolean("ACTIVE")).thenReturn(true);
        when(edgeRow.getTimestamp("CREATED_AT")).thenReturn(now);
        Method mapEdge = JdbcMagmaMemoryRepository.class.getDeclaredMethod("mapEdge", ResultSet.class, int.class);
        mapEdge.setAccessible(true);
        mapEdge.invoke(repository, edgeRow, 0);

        Method parseList = JdbcMagmaMemoryRepository.class.getDeclaredMethod("parseList", String.class);
        Method parseMap = JdbcMagmaMemoryRepository.class.getDeclaredMethod("parseMap", String.class);
        Method parsePaths = JdbcMagmaMemoryRepository.class.getDeclaredMethod("parsePaths", String.class);
        parseList.setAccessible(true); parseMap.setAccessible(true); parsePaths.setAccessible(true);
        org.junit.jupiter.api.Assertions.assertTrue(((List<?>) parseList.invoke(repository, (Object) null)).isEmpty());
        org.junit.jupiter.api.Assertions.assertTrue(((Map<?, ?>) parseMap.invoke(repository, "")).isEmpty());
        org.junit.jupiter.api.Assertions.assertTrue(((List<?>) parsePaths.invoke(repository, " ")).isEmpty());
        org.junit.jupiter.api.Assertions.assertEquals(List.of("e1"), parseList.invoke(repository, "[\"e1\"]"));
    }

    private static JdbcMagmaMemoryRepository newRepository(JdbcTemplate jdbc) throws Exception {
        JdbcMagmaMemoryRepository repository = new JdbcMagmaMemoryRepository(mock(DataSource.class));
        Field field = JdbcMagmaMemoryRepository.class.getDeclaredField("jdbc");
        field.setAccessible(true);
        field.set(repository, jdbc);
        return repository;
    }
}
