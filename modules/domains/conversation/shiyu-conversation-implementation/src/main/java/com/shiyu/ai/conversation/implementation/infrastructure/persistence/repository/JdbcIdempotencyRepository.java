package com.shiyu.ai.conversation.implementation.infrastructure.persistence.repository;

import com.shiyu.ai.conversation.implementation.domain.port.IdempotencyRepository;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * {@code JdbcIdempotencyRepository} 定义会话模块的持久化端口，隔离领域逻辑与具体存储实现。
 */
@Component
public class JdbcIdempotencyRepository implements IdempotencyRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * {@code JdbcIdempotencyRepository} 创建并初始化当前类型实例。
     *
     * @param dataSource 参数值，用于执行当前操作。
     */
    public JdbcIdempotencyRepository(@Qualifier("agentDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * {@code find} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param operation 参数值，用于执行当前操作。
     * @param key 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<String> find(
            TenantId tenantId, long ownerUserId, String operation, String key) {
        return jdbc
                .query(
                        "SELECT RESOURCE_ID FROM CHAT_IDEMPOTENCY_KEY WHERE TENANT_ID=? AND"
                                + " OWNER_USER_ID=? AND OPERATION=? AND KEY_VALUE=?",
                        (r, n) -> r.getString(1),
                        tenantId.value(),
                        ownerUserId,
                        operation,
                        key)
                .stream()
                .findFirst();
    }

    /**
     * {@code claim} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param operation 参数值，用于执行当前操作。
     * @param key 参数值，用于执行当前操作。
     * @param resourceId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean claim(
            TenantId tenantId, long ownerUserId, String operation, String key, String resourceId) {
        try {
            jdbc.update(
                    "INSERT INTO CHAT_IDEMPOTENCY_KEY"
                        + " (TENANT_ID,OWNER_USER_ID,KEY_VALUE,OPERATION,RESOURCE_ID,CREATED_AT)"
                        + " VALUES (?,?,?,?,?,?)",
                    tenantId.value(),
                    ownerUserId,
                    key,
                    operation,
                    resourceId,
                    Timestamp.from(Instant.now()));
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }
}
