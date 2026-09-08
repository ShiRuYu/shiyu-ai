package com.shiyu.ai.conversation.implementation.persistence.repository;

import com.shiyu.ai.kernel.context.TenantId;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcIdempotencyRepositoryTest {
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findsAndClaimsTenantScopedKeysIdempotently() throws Exception {
        JdbcIdempotencyRepository repository = new JdbcIdempotencyRepository(mock(DataSource.class));
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Field field = JdbcIdempotencyRepository.class.getDeclaredField("jdbc"); field.setAccessible(true); field.set(repository, jdbc);
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenAnswer(invocation -> {
            RowMapper mapper = invocation.getArgument(1);
            ResultSet row = mock(ResultSet.class); when(row.getString(1)).thenReturn("resource-1");
            return List.of(mapper.mapRow(row, 0));
        });
        assertEquals("resource-1", repository.find(new TenantId(7), 8, "op", "key").orElseThrow());
        assertTrue(repository.claim(new TenantId(7), 8, "op", "key", "resource-1"));
        when(jdbc.update(anyString(), any(Object[].class))).thenThrow(new DuplicateKeyException("duplicate"));
        assertFalse(repository.claim(new TenantId(7), 8, "op", "key", "resource-1"));
    }
}
