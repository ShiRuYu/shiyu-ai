-- Add nullable request fingerprints for idempotent conversation operations.
-- Existing rows stay NULL and are treated as conflicts on replay because their request
-- payload cannot be reconstructed safely.
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
