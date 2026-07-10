-- 租户配额表
CREATE TABLE IF NOT EXISTS tenant_quota (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL UNIQUE,
    max_agent_count BIGINT DEFAULT 10,
    max_token_per_day BIGINT DEFAULT 1000000,
    max_storage_mb BIGINT DEFAULT 1024,
    max_user_count BIGINT DEFAULT 100,
    status VARCHAR(32) DEFAULT 'ACTIVE',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 默认配额数据
MERGE INTO tenant_quota (tenant_id, max_agent_count, max_token_per_day, max_storage_mb, max_user_count, status)
KEY(tenant_id) VALUES(1, 50, 5000000, 5120, 500, 'ACTIVE');
