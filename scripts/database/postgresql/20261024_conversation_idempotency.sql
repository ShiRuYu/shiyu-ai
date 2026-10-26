-- Add nullable request fingerprints for idempotent conversation operations.
-- Existing rows stay NULL and are treated as conflicts on replay because their request
-- payload cannot be reconstructed safely.
ALTER TABLE chat_idempotency_key
    ADD COLUMN IF NOT EXISTS fingerprint CHARACTER VARYING(64);
