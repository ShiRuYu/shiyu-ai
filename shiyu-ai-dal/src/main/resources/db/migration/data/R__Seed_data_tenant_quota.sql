-- ============================================
-- Data: 租户默认配额
-- ============================================
INSERT IGNORE INTO `tenant_quota` (`tenant_id`, `max_agent_count`, `max_token_per_day`, `max_storage_mb`, `max_user_count`, `status`)
VALUES (1, 50, 5000000, 5120, 500, 'ACTIVE');
