-- 从 H2 源资源生成的 PostgreSQL 资源。
-- 不要直接编辑此生成文件；请修改源资源后重新生成。
-- 转换来源：modules/domains/conversation/shiyu-conversation-implementation/src/main/resources/db/updates/conversation/20261024_idempotency_fingerprint.sql

ALTER TABLE CHAT_IDEMPOTENCY_KEY ADD COLUMN IF NOT EXISTS FINGERPRINT CHARACTER VARYING(64);
