package com.shiyu.ai.common.event.outbox;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.event.api.InfrastructureEventPublisher;
import com.shiyu.ai.common.event.config.EventInfrastructureProperties;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * 发布 Jdbc Outbox 事件 相关的领域事件或基础设施消息。
 */
public class JdbcOutboxEventPublisher implements InfrastructureEventPublisher {

    /**
     * JDBC，表示当前对象中的对应属性。
     */
    protected final JdbcTemplate jdbc;
    /**
     * 配置属性，表示当前对象中的对应属性。
     */
    protected final EventInfrastructureProperties properties;

    /**
     * 执行 Jdbc Outbox 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param jdbc 用于完成本次业务处理的 jdbc 参数。
     * @param properties 用于完成本次业务处理的 properties 参数。
     */
    public JdbcOutboxEventPublisher(JdbcTemplate jdbc, EventInfrastructureProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
        initializeSchema();
    }

    /**
     * 发布或发送 Jdbc Outbox 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    @Override
    public void publish(DomainEventEnvelope<?> event) {
        if (event == null || event.tenantId() == null) {
            throw new IllegalArgumentException("event tenantId is required");
        }
        TenantScope.requireMatches(event.tenantId());
        String eventId = UUID.randomUUID().toString();
        jdbc.update(
                """
                INSERT INTO shiyu_event_outbox
                    (event_id, event_type, tenant_id, user_id, correlation_id,
                     occurred_at, payload, created_at, published_at, attempts)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL, 0)
                """,
                eventId,
                event.eventType(),
                event.tenantId().value(),
                event.userId().value(),
                event.correlationId().value(),
                Timestamp.from(event.occurredAt()),
                JSONUtils.toJsonString(event),
                Timestamp.from(Instant.now()));
    }

    private void initializeSchema() {
        jdbc.execute(
                """
                CREATE TABLE IF NOT EXISTS shiyu_event_outbox (
                    event_id VARCHAR(64) PRIMARY KEY,
                    event_type VARCHAR(255) NOT NULL,
                    tenant_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    correlation_id VARCHAR(255) NOT NULL,
                    occurred_at TIMESTAMP NOT NULL,
                    payload TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    published_at TIMESTAMP NULL,
                    dead_lettered_at TIMESTAMP NULL,
                    attempts INTEGER NOT NULL DEFAULT 0,
                    last_error TEXT NULL
                )
                """);
        jdbc.execute(
                "ALTER TABLE shiyu_event_outbox ADD COLUMN IF NOT EXISTS dead_lettered_at TIMESTAMP"
                        + " NULL");
    }
}
