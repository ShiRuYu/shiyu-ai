-- PostgreSQL resources generated from the H2 resources.
-- Do not edit this generated file directly; update the source and regenerate.
-- Converted from modules/domains/conversation/shiyu-conversation-implementation/src/main/resources/db/updates/conversation/20261024_idempotency_fingerprint.sql

ALTER TABLE CHAT_IDEMPOTENCY_KEY ADD COLUMN IF NOT EXISTS FINGERPRINT CHARACTER VARYING(64);
