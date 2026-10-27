-- 为终态生成的配额和用量副作用提供持久化重试队列。
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
    KEY idx_chat_generation_postprocessing_tenant (tenant_id, generation_id),
    CONSTRAINT ck_chat_generation_postprocessing_quota
        CHECK (quota_action IN ('NONE', 'SETTLE', 'RELEASE')),
    CONSTRAINT ck_chat_generation_postprocessing_usage
        CHECK (usage_action IN ('NONE', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_chat_generation_postprocessing_attempts CHECK (attempts >= 0)
) ENGINE=InnoDB;

-- 为已存在的重试队列表补齐租户前缀索引，重复执行时保留现有索引。
SET @postprocessing_tenant_index_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'CHAT_GENERATION_POSTPROCESSING'
      AND index_name = 'idx_chat_generation_postprocessing_tenant'
);
SET @postprocessing_tenant_index_migration = IF(
    @postprocessing_tenant_index_exists = 0,
    'ALTER TABLE CHAT_GENERATION_POSTPROCESSING ADD INDEX idx_chat_generation_postprocessing_tenant (tenant_id, generation_id)',
    'SELECT 1'
);
PREPARE add_postprocessing_tenant_index FROM @postprocessing_tenant_index_migration;
EXECUTE add_postprocessing_tenant_index;
DEALLOCATE PREPARE add_postprocessing_tenant_index;
