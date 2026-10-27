-- 为幂等会话操作添加可为空的请求指纹。
-- 历史记录保持 NULL，重放时视为冲突，
-- 因为无法安全还原其原始请求内容。
SET @fingerprint_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'CHAT_IDEMPOTENCY_KEY'
      AND column_name = 'FINGERPRINT'
);
SET @fingerprint_migration = IF(
    @fingerprint_column_exists = 0,
    'ALTER TABLE CHAT_IDEMPOTENCY_KEY ADD COLUMN FINGERPRINT VARCHAR(64) NULL',
    'SELECT 1'
);
PREPARE add_fingerprint_column FROM @fingerprint_migration;
EXECUTE add_fingerprint_column;
DEALLOCATE PREPARE add_fingerprint_column;
