package com.shiyu.ai.agent.implementation.persistence.runtime;

import com.shiyu.ai.runtime.AiApp;
import com.shiyu.ai.runtime.AiAppVersion;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
class JdbcAiAppRepositoryTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final JdbcAiAppRepository repository = new JdbcAiAppRepository(jdbc);
    private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

    @Test
    void persistsAndQueriesTenantScopedAppsAndVersions() {
        AiApp app = new AiApp("app", new TenantId(1L), new UserId(2L), "Tutor", "desc", "ACTIVE", null, now, now);
        AiAppVersion draft = new AiAppVersion("version", "app", new TenantId(1L), "1", null, "DRAFT", now, null);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.query(anyString(), any(ResultSetExtractor.class), any(Object[].class)))
                .thenReturn(Optional.empty());
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of());

        repository.insert(app);
        repository.insertVersion(draft);
        assertTrue(repository.find("app", new TenantId(1), 2).isEmpty());
        assertTrue(repository.findByTenant("app", new TenantId(1)).isEmpty());
        assertTrue(repository.list(new TenantId(1), 2, 0).isEmpty());
        assertTrue(repository.findVersion("app", "version", new TenantId(1)).isEmpty());
        assertTrue(repository.versions("app", new TenantId(1)).isEmpty());
        assertEquals(1, repository.archiveVersion("app", "version", new TenantId(1)));
        verify(jdbc).update(org.mockito.ArgumentMatchers.startsWith("INSERT INTO AI_APP ("), any(Object[].class));
    }

    @Test
    void publishesOnlyDraftAndArchivesPreviousPublishedVersion() {
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(0, 1, 1, 1);
        assertEquals(0, repository.publishVersion("app", "v1", new TenantId(1)));
        assertEquals(1, repository.publishVersion("app", "v1", new TenantId(1)));
    }

    @Test
    void mapsRowsIncludingNullablePublishedTimestamp() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("ID")).thenReturn("app", "version");
        when(rs.getString("APP_ID")).thenReturn("app");
        when(rs.getString("NAME")).thenReturn("Tutor");
        when(rs.getString("DESCRIPTION")).thenReturn("desc");
        when(rs.getString("STATUS")).thenReturn("ACTIVE", "DRAFT");
        when(rs.getString("VERSION")).thenReturn("1");
        when(rs.getString("CONFIG_JSON")).thenReturn("{}");
        when(rs.getLong("TENANT_ID")).thenReturn(1L);
        when(rs.getLong("OWNER_USER_ID")).thenReturn(2L);
        when(rs.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(now));
        when(rs.getTimestamp("UPDATED_AT")).thenReturn(Timestamp.from(now));
        when(rs.getTimestamp("PUBLISHED_AT")).thenReturn(null, Timestamp.from(now));
        Method mapApp = JdbcAiAppRepository.class.getDeclaredMethod("mapApp", ResultSet.class);
        mapApp.setAccessible(true);
        assertEquals("app", ((AiApp) mapApp.invoke(repository, rs)).id());
        Method mapVersion = JdbcAiAppRepository.class.getDeclaredMethod("mapVersion", ResultSet.class);
        mapVersion.setAccessible(true);
        assertFalse(((AiAppVersion) mapVersion.invoke(repository, rs)).published());
        assertEquals(now, ((AiAppVersion) mapVersion.invoke(repository, rs)).publishedAt());
    }
}
