-- ============================================
-- DDL: 租户配额表
-- ============================================
CREATE TABLE IF NOT EXISTS `tenant_quota` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL UNIQUE,
    `max_agent_count` BIGINT DEFAULT 10,
    `max_token_per_day` BIGINT DEFAULT 1000000,
    `max_storage_mb` BIGINT DEFAULT 1024,
    `max_user_count` BIGINT DEFAULT 100,
    `status` TINYINT DEFAULT 0 COMMENT '0正常 1停用',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
