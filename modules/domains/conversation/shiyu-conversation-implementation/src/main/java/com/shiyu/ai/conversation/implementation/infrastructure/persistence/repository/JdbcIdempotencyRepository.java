package com.shiyu.ai.conversation.implementation.infrastructure.persistence.repository;

import com.shiyu.ai.conversation.implementation.domain.port.IdempotencyRepository;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * 负责 Jdbc Idempotency 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class JdbcIdempotencyRepository implements IdempotencyRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * 执行 Jdbc Idempotency 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dataSource 用于完成本次业务处理的 dataSource 参数。
     */
    public JdbcIdempotencyRepository(@Qualifier("agentDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * 查询 Jdbc Idempotency 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param operation 用于完成本次业务处理的 operation 参数。
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<String> find(
            TenantId tenantId, long ownerUserId, String operation, String key) {
        TenantScope.requireMatches(tenantId);
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
     * 读取当前租户和用户在指定操作及幂等键下保存的请求指纹。
     *
     * @param tenantId 幂等记录所属租户，必须匹配当前租户上下文
     * @param ownerUserId 幂等记录所属用户
     * @param operation 请求操作名称
     * @param key 调用方提供的幂等键
     * @return 已保存的非空指纹；记录不存在或未保存指纹时为空
     */
    @Override
    public Optional<String> findFingerprint(
            TenantId tenantId, long ownerUserId, String operation, String key) {
        TenantScope.requireMatches(tenantId);
        return jdbc.query(
                        "SELECT FINGERPRINT FROM CHAT_IDEMPOTENCY_KEY WHERE TENANT_ID=? AND"
                                + " OWNER_USER_ID=? AND OPERATION=? AND KEY_VALUE=?",
                        (r, n) -> r.getString(1),
                        tenantId.value(),
                        ownerUserId,
                        operation,
                        key)
                .stream()
                .filter(java.util.Objects::nonNull)
                .findFirst();
    }

    @Override
    public boolean claim(
            TenantId tenantId, long ownerUserId, String operation, String key, String resourceId) {
        return claim(tenantId, ownerUserId, operation, key, null, resourceId);
    }

    @Override
    public boolean claim(
            TenantId tenantId,
            long ownerUserId,
            String operation,
            String key,
            String fingerprint,
            String resourceId) {
        TenantScope.requireMatches(tenantId);
        try {
            jdbc.update(
                    "INSERT INTO CHAT_IDEMPOTENCY_KEY"
                        + " (TENANT_ID,OWNER_USER_ID,KEY_VALUE,OPERATION,FINGERPRINT,RESOURCE_ID,CREATED_AT)"
                        + " VALUES (?,?,?,?,?,?,?)",
                    tenantId.value(),
                    ownerUserId,
                    key,
                    operation,
                    fingerprint,
                    resourceId,
                    Timestamp.from(Instant.now()));
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }

    @Override
    public int deleteClaim(
            TenantId tenantId, long ownerUserId, String operation, String key, String resourceId) {
        TenantScope.requireMatches(tenantId);
        return jdbc.update(
                "DELETE FROM CHAT_IDEMPOTENCY_KEY WHERE TENANT_ID=? AND OWNER_USER_ID=? AND"
                        + " OPERATION=? AND KEY_VALUE=? AND RESOURCE_ID=?",
                tenantId.value(),
                ownerUserId,
                operation,
                key,
                resourceId);
    }
}
