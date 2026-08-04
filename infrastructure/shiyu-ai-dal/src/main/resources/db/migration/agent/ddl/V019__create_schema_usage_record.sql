-- ============================================
-- Schema: schema_usage_record
-- 统一用量记录表（替代 token_usage + embedding_usage）
-- 通用字段由表内列承载，类型专属字段以 JSON 存于 ext_info
--
-- usage_type 现支持：
--   LLM       — ext_info: { platform, model, promptTokens, completionTokens, totalTokens, cost }
--   EMBEDDING — ext_info: { model, textLength, estimatedTokens, vectorCount }
-- ============================================

CREATE TABLE IF NOT EXISTS `agent_usage_record` (
    `id`               VARCHAR(64)  NOT NULL COMMENT 'UUID',
    `usage_type`       VARCHAR(32)  NOT NULL COMMENT '用量类型：LLM / EMBEDDING',
    `latency_ms`       BIGINT       NOT NULL DEFAULT 0 COMMENT '延迟(毫秒)',
    `user_id`          BIGINT       DEFAULT NULL COMMENT '用户ID',
    `session_id`       VARCHAR(64)  DEFAULT NULL COMMENT '会话ID',
    `ext_info`         TEXT         DEFAULT NULL COMMENT '类型专属字段（JSON）',
    `create_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_usage_record_type` ON `agent_usage_record` (`usage_type`);
CREATE INDEX IF NOT EXISTS `idx_usage_record_user` ON `agent_usage_record` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_usage_record_time` ON `agent_usage_record` (`create_time`);

COMMENT ON TABLE `agent_usage_record` IS '统一用量记录表';
