-- Durable retry queue for quota and usage side effects from terminal generations.
CREATE TABLE IF NOT EXISTS CHAT_GENERATION_POSTPROCESSING (
    generation_id VARCHAR(64) NOT NULL,
    tenant_id BIGINT NOT NULL,
    owner_user_id BIGINT NOT NULL,
    quota_action VARCHAR(16) NOT NULL,
    usage_action VARCHAR(16) NOT NULL,
    quota_done BOOLEAN NOT NULL DEFAULT FALSE,
    usage_done BOOLEAN NOT NULL DEFAULT FALSE,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NOT NULL,
    claim_token VARCHAR(64),
    claimed_until TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (generation_id),
    KEY idx_chat_generation_postprocessing_due (next_attempt_at, claimed_until),
    CONSTRAINT ck_chat_generation_postprocessing_quota
        CHECK (quota_action IN ('NONE', 'SETTLE', 'RELEASE')),
    CONSTRAINT ck_chat_generation_postprocessing_usage
        CHECK (usage_action IN ('NONE', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_chat_generation_postprocessing_attempts CHECK (attempts >= 0)
) ENGINE=InnoDB;
