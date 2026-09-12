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
);

CREATE INDEX IF NOT EXISTS shiyu_event_outbox_pending_idx
    ON shiyu_event_outbox (published_at, created_at);

CREATE TABLE IF NOT EXISTS shiyu_event_inbox (
    event_id VARCHAR(64) PRIMARY KEY,
    consumed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
