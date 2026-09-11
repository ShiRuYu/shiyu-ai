package com.shiyu.ai.common.core.event;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/** PostgreSQL/MySQL/H2-compatible outbox writer. */
public class JdbcOutboxEventPublisher implements InfrastructureEventPublisher {

    protected final JdbcTemplate jdbc;
    protected final EventInfrastructureProperties properties;

    public JdbcOutboxEventPublisher(JdbcTemplate jdbc, EventInfrastructureProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
        initializeSchema();
    }

    @Override
    public void publish(DomainEventEnvelope<?> event) {
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
        // Keep upgrades idempotent for outbox tables created by the previous
        // provider implementation.
        jdbc.execute(
                "ALTER TABLE shiyu_event_outbox ADD COLUMN IF NOT EXISTS dead_lettered_at TIMESTAMP"
                        + " NULL");
    }
}
