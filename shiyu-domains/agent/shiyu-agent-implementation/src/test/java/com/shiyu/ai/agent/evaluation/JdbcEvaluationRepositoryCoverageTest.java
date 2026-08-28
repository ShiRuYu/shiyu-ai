package com.shiyu.ai.agent.evaluation;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JdbcEvaluationRepositoryCoverageTest {
    @Test
    void mapsRunsWithResultsAndNullableCompletionTimes() throws Exception {
        JdbcEvaluationRepository repository = new JdbcEvaluationRepository(mock(DataSource.class));
        ResultSet rs = mock(ResultSet.class);
        Instant created = Instant.parse("2025-01-01T00:00:00Z");
        when(rs.getString("RESULTS_JSON")).thenReturn(
                "[{\"caseId\":\"c1\",\"metric\":\"EXACT_MATCH\",\"score\":\"0.75\",\"passed\":true,\"detail\":\"ok\"}]");
        when(rs.getString("ID")).thenReturn("run-1");
        when(rs.getString("DATASET_ID")).thenReturn("set-1");
        when(rs.getString("APP_VERSION_ID")).thenReturn("version-1");
        when(rs.getString("METRIC")).thenReturn("EXACT_MATCH");
        when(rs.getString("STATUS")).thenReturn("COMPLETED");
        when(rs.getLong("TENANT_ID")).thenReturn(7L);
        when(rs.getLong("OWNER_USER_ID")).thenReturn(9L);
        when(rs.getDouble("PASS_RATE")).thenReturn(0.75D);
        when(rs.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(created));
        when(rs.getTimestamp("COMPLETED_AT")).thenReturn(null);

        Method mapRun = JdbcEvaluationRepository.class.getDeclaredMethod("mapRun", ResultSet.class);
        mapRun.setAccessible(true);
        EvalRun run = (EvalRun) mapRun.invoke(repository, rs);
        assertEquals("run-1", run.id());
        assertEquals(1, run.results().size());
        assertEquals(0.75D, run.results().getFirst().score());
        assertNull(run.completedAt());
    }

    @Test
    void handlesEmptyMetadataAndNumericConversionHelpers() throws Exception {
        JdbcEvaluationRepository repository = new JdbcEvaluationRepository(mock(DataSource.class));
        Method parseMap = JdbcEvaluationRepository.class.getDeclaredMethod("parseMap", String.class);
        parseMap.setAccessible(true);
        assertEquals(Map.of(), parseMap.invoke(repository, new Object[]{null}));
        assertEquals(Map.of(), parseMap.invoke(repository, " "));
        assertEquals("value", ((Map<?, ?>) parseMap.invoke(repository, "{\"key\":\"value\"}")).get("key"));

        Method number = JdbcEvaluationRepository.class.getDeclaredMethod("number", Object.class);
        number.setAccessible(true);
        assertEquals(2.5D, number.invoke(repository, 2.5D));
        assertEquals(3.5D, number.invoke(repository, "3.5"));

        Method timestamp = JdbcEvaluationRepository.class.getDeclaredMethod("ts", Instant.class);
        timestamp.setAccessible(true);
        assertNotNull(timestamp.invoke(null, new Object[]{null}));
        Instant now = Instant.parse("2025-02-01T00:00:00Z");
        assertEquals(now, ((Timestamp) timestamp.invoke(null, now)).toInstant());
    }
}
