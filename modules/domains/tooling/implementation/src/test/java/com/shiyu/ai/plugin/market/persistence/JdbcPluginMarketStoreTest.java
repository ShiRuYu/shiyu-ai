package com.shiyu.ai.plugin.market.persistence;

import com.shiyu.ai.plugin.market.PluginMarketEntry;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcPluginMarketStoreTest {
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void persistsQueriesMapsAndDisablesEntries() throws Exception {
        JdbcPluginMarketStore store = new JdbcPluginMarketStore(mock(DataSource.class));
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Field field = JdbcPluginMarketStore.class.getDeclaredField("jdbc");
        field.setAccessible(true);
        field.set(store, jdbc);

        PluginMarketEntry entry = new PluginMarketEntry("demo", "1.0.0", "local", "{}", null, null,
                List.of("read"), "checksum", "AUTO", Instant.parse("2025-01-01T00:00:00Z"), true);
        assertSame(entry, store.save(entry));
        verify(jdbc).update(startsWith("INSERT INTO PLUGIN_MARKET_ENTRY"), any(Object[].class));

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("ID")).thenReturn("demo");
        when(resultSet.getString("VERSION")).thenReturn("1.0.0");
        when(resultSet.getString("SOURCE")).thenReturn("local");
        when(resultSet.getString("MANIFEST")).thenReturn("{}");
        when(resultSet.getString("SIGNATURE")).thenReturn(null);
        when(resultSet.getString("PUBLISHER_KEY")).thenReturn(null);
        when(resultSet.getString("PERMISSIONS_JSON")).thenReturn("[\"read\"]");
        when(resultSet.getString("CHECKSUM")).thenReturn("checksum");
        when(resultSet.getString("UPDATE_POLICY")).thenReturn("AUTO");
        when(resultSet.getTimestamp("PUBLISHED_AT")).thenReturn(Timestamp.from(entry.publishedAt()));
        when(resultSet.getBoolean("ENABLED")).thenReturn(true);

        when(jdbc.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(resultSet, 0));
        });
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenAnswer(invocation -> {
            RowMapper mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(resultSet, 0));
        });
        assertEquals(1, store.list().size());
        assertEquals("demo", store.find("demo").orElseThrow().id());
        store.disable("demo");
        verify(jdbc).update("UPDATE PLUGIN_MARKET_ENTRY SET ENABLED=FALSE WHERE ID=?", "demo");
    }
}
