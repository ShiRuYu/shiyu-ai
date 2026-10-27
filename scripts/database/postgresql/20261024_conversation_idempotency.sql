-- 为幂等会话操作添加可为空的请求指纹。
-- 历史记录保持 NULL，重放时视为冲突，
-- 因为无法安全还原其原始请求内容。
ALTER TABLE chat_idempotency_key
    ADD COLUMN IF NOT EXISTS fingerprint CHARACTER VARYING(64);
