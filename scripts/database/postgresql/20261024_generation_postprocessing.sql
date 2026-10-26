-- Durable retry queue for quota and usage side effects from terminal generations.
CREATE TABLE IF NOT EXISTS chat_generation_postprocessing (
    generation_id CHARACTER VARYING(64) NOT NULL,
    tenant_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    quota_action CHARACTER VARYING(16) NOT NULL,
    usage_action CHARACTER VARYING(16) NOT NULL,
    quota_done BOOLEAN NOT NULL DEFAULT FALSE,
    usage_done BOOLEAN NOT NULL DEFAULT FALSE,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NOT NULL,
    claim_token CHARACTER VARYING(64),
    claimed_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_chat_generation_postprocessing PRIMARY KEY (generation_id),
    CONSTRAINT ck_chat_generation_postprocessing_quota
        CHECK (quota_action IN ('NONE', 'SETTLE', 'RELEASE')),
    CONSTRAINT ck_chat_generation_postprocessing_usage
        CHECK (usage_action IN ('NONE', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_chat_generation_postprocessing_attempts CHECK (attempts >= 0)
);
CREATE INDEX IF NOT EXISTS idx_chat_generation_postprocessing_due
    ON chat_generation_postprocessing (next_attempt_at, claimed_until);
