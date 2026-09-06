package com.shiyu.ai.agent.infrastructure;

import com.shiyu.ai.agent.config.IntentDefApplicationRunner;
import com.shiyu.ai.agent.domain.model.IntentDefBO;
import com.shiyu.ai.agent.event.AuditEvent;
import com.shiyu.ai.agent.event.AuditEventListener;
import com.shiyu.ai.agent.implementation.persistence.runtime.JdbcToolApprovalRepository;
import com.shiyu.ai.agent.persistence.repository.ExecutionTimelineRepositoryImpl;
import com.shiyu.ai.agent.port.repository.AuditLogRepository;
import com.shiyu.ai.agent.port.repository.IntentDefRepository;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.runtime.ToolApproval;
import com.shiyu.ai.runtime.ToolApprovalStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class AgentInfrastructureCoverageTest {
    @Test
    void mapsJdbcApprovalRowsWithOptionalDecisionAndExpiry() throws Exception {
        JdbcToolApprovalRepository repository = new JdbcToolApprovalRepository(mock(JdbcTemplate.class));
        ResultSet rs = mock(ResultSet.class);
        Instant created = Instant.parse("2025-01-01T00:00:00Z");
        when(rs.getString("ID")).thenReturn("approval");
        when(rs.getString("RUN_ID")).thenReturn("run");
        when(rs.getLong("TENANT_ID")).thenReturn(7L);
        when(rs.getLong("OWNER_USER_ID")).thenReturn(9L);
        when(rs.getString("TOOL_NAME")).thenReturn("search");
        when(rs.getString("ARGUMENTS_REDACTED")).thenReturn("{}");
        when(rs.getString("STATUS")).thenReturn("PENDING");
        when(rs.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(created));
        when(rs.getTimestamp("DECIDED_AT")).thenReturn(null);
        when(rs.getTimestamp("EXPIRES_AT")).thenReturn(null);
        Method map = JdbcToolApprovalRepository.class.getDeclaredMethod("map", ResultSet.class);
        map.setAccessible(true);
        ToolApproval pending = (ToolApproval) map.invoke(repository, rs);
        assertNull(pending.decidedAt());
        assertNotNull(pending.expiresAt());

        when(rs.getTimestamp("DECIDED_AT")).thenReturn(Timestamp.from(created.plusSeconds(1)));
        when(rs.getTimestamp("EXPIRES_AT")).thenReturn(Timestamp.from(created.plusSeconds(300)));
        ToolApproval mapped = (ToolApproval) map.invoke(repository, rs);
        assertNotNull(mapped.decidedAt());
        assertNotNull(mapped.expiresAt());
    }

    @Test
    void rejectsMissingTimelineTenantBeforeMapperAccess() throws Exception {
        Method require = ExecutionTimelineRepositoryImpl.class.getDeclaredMethod("requireTenant", TenantId.class);
        require.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> require.invoke(null, new Object[]{null}));
        assertDoesNotThrow(() -> require.invoke(null, new TenantId(7)));
    }

    @Test
    void loadsIntentDefinitionsForAllStartupRepositoryOutcomes() throws Exception {
        IntentDefRepository repository = mock(IntentDefRepository.class);
        IntentDefApplicationRunner runner = new IntentDefApplicationRunner();
        Field field = IntentDefApplicationRunner.class.getDeclaredField("intentDefRepository");
        field.setAccessible(true);
        field.set(runner, repository);
        ApplicationArguments args = mock(ApplicationArguments.class);

        when(repository.selectByAgentId(any(TenantId.class), eq("default"))).thenReturn(null);
        runner.run(args);
        when(repository.selectByAgentId(any(TenantId.class), eq("default"))).thenReturn(List.of());
        runner.run(args);
        IntentDefBO definition = new IntentDefBO();
        when(repository.selectByAgentId(any(TenantId.class), eq("default"))).thenReturn(List.of(definition));
        runner.run(args);
        when(repository.selectByAgentId(any(TenantId.class), eq("default"))).thenThrow(new IllegalStateException("db"));
        runner.run(args);
        verify(repository, times(4)).selectByAgentId(any(TenantId.class), eq("default"));
    }

    @Test
    void recordsAuditOnlyForValidTenantAndContainsListenerFailures() {
        AuditLogRepository repository = mock(AuditLogRepository.class);
        AuditEventListener listener = new AuditEventListener(repository);
        AuditEvent valid = new AuditEvent(new TenantId(7L), 9L, "CREATE", "AGENT", "a1", "{}", "127.0.0.1", "SUCCESS", null, 3L);
        listener.onAuditEvent(valid);
        listener.onAuditEvent(new AuditEvent(null, 9L, "CREATE", "AGENT", "a1", "{}", null, "FAILED", "bad", 4L));
        assertThrows(IllegalArgumentException.class, () -> new AuditEvent(new TenantId(0L), 9L, "CREATE", "AGENT", "a1", "{}", null, "FAILED", "bad", 4L));
        assertThrows(NullPointerException.class, () -> listener.onAuditEvent(null));
        verify(repository).insert(eq(new TenantId(7L)), any());
    }

    @Test
    void exercisesDurableApprovalRepositoryOperations() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcToolApprovalRepository repository = new JdbcToolApprovalRepository(jdbc);
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        ToolApproval approval = new ToolApproval("a", "r", 7L, 9L, "tool", "{}", ToolApprovalStatus.PENDING, now, null, now.plusSeconds(300));
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), any(Object[].class))).thenReturn(List.of());
        repository.insert(approval);
        assertTrue(repository.list("r", new TenantId(7L), 9L).isEmpty());
        assertTrue(repository.listAll(new TenantId(7L), 9L).isEmpty());
        assertTrue(repository.find("a", new TenantId(7L), 9L).isEmpty());
        assertEquals(1, repository.update(approval, ToolApprovalStatus.PENDING));
        assertEquals(1, repository.expirePending(new TenantId(7L), 9L));
        verify(jdbc, times(3)).update(anyString(), any(Object[].class));
    }
}
