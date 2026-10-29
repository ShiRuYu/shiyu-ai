-- 租户模型路由策略的持久化增补表，兼容已有数据库。
CREATE TABLE IF NOT EXISTS model_route_policy (
    id VARCHAR(100) PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    payload VARCHAR(10000) NOT NULL
);
