package com.shiyu.ai.conversation.implementation.persistence.repository;

import com.shiyu.ai.conversation.port.IdempotencyRepository;
import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Component
public class JdbcIdempotencyRepository implements IdempotencyRepository {
    private final JdbcTemplate jdbc;
    public JdbcIdempotencyRepository(@Qualifier("agentDataSource") DataSource dataSource) { this.jdbc = new JdbcTemplate(dataSource); }

    @Override public Optional<String> find(TenantId tenantId, long ownerUserId, String operation, String key) {
        return jdbc.query("SELECT RESOURCE_ID FROM CHAT_IDEMPOTENCY_KEY WHERE TENANT_ID=? AND OWNER_USER_ID=? AND OPERATION=? AND KEY_VALUE=?", (r, n) -> r.getString(1), tenantId.value(), ownerUserId, operation, key).stream().findFirst();
    }
    @Override public boolean claim(TenantId tenantId, long ownerUserId, String operation, String key, String resourceId) {
        try {
            jdbc.update("INSERT INTO CHAT_IDEMPOTENCY_KEY (TENANT_ID,OWNER_USER_ID,KEY_VALUE,OPERATION,RESOURCE_ID,CREATED_AT) VALUES (?,?,?,?,?,?)", tenantId.value(), ownerUserId, key, operation, resourceId, Timestamp.from(Instant.now()));
            return true;
        } catch (DuplicateKeyException duplicate) { return false; }
    }
}

