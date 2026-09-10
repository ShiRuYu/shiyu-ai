package com.shiyu.ai.common.core.event;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Objects;

/**
 * Durable inbox guard for Kafka consumers. A consumer acknowledges a message
 * only after {@link #firstSeen(String)} returns {@code true} and its handler
 * has completed. The primary key makes concurrent deliveries idempotent.
 */
public final class EventConsumptionDeduplicator {

    private final JdbcTemplate jdbc;

    public EventConsumptionDeduplicator(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "JdbcTemplate must not be null");
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS shiyu_event_inbox (
                    event_id VARCHAR(64) PRIMARY KEY,
                    consumed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    public boolean firstSeen(String eventId) {
        if (eventId == null || eventId.isBlank()) return false;
        try {
            return jdbc.update("INSERT INTO shiyu_event_inbox (event_id, consumed_at) VALUES (?, CURRENT_TIMESTAMP)",
                    eventId) == 1;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }
}
