-- ============================================
-- Schema: schema_observation
-- V017: 审计日志 + 执行时间线
-- ============================================

-- 审计日志表
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `tenant_id`   BIGINT       DEFAULT NULL COMMENT '租户ID',
    `user_id`     BIGINT       DEFAULT NULL COMMENT '用户ID',
    `action`      VARCHAR(64)  NOT NULL COMMENT '操作类型: LOGIN, AGENT_EXECUTE, MODEL_CALL, KNOWLEDGE_SEARCH, CREATE, UPDATE, DELETE 等',
    `target_type` VARCHAR(64)  DEFAULT NULL COMMENT '操作对象类型: agent, model, knowledge, education, auth, system 等',
    `target_id`   VARCHAR(128) DEFAULT NULL COMMENT '操作对象ID',
    `detail`      TEXT         DEFAULT NULL COMMENT '操作详情（JSON）',
    `ip`          VARCHAR(64)  DEFAULT NULL COMMENT '请求IP',
    `result`      VARCHAR(16)  DEFAULT 'SUCCESS' COMMENT '操作结果: SUCCESS / FAILED',
    `error_msg`   VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `duration_ms` BIGINT       DEFAULT 0 COMMENT '请求耗时（毫秒）',
    `create_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX `idx_tenant_id`    (`tenant_id`),
    INDEX `idx_user_id`      (`user_id`),
    INDEX `idx_action`       (`action`),
    INDEX `idx_target_type`  (`target_type`),
    INDEX `idx_create_time`  (`create_time`)
) COMMENT='审计日志表';

-- 执行时间线表
CREATE TABLE IF NOT EXISTS `execution_timeline` (
    `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    `tenant_id`    BIGINT       DEFAULT NULL COMMENT '租户ID',
    `execution_id` VARCHAR(128) NOT NULL COMMENT '执行ID',
    `agent_id`     VARCHAR(128) DEFAULT NULL COMMENT 'Agent ID',
    `node_id`      VARCHAR(128) DEFAULT NULL COMMENT '节点ID',
    `node_type`    VARCHAR(64)  DEFAULT NULL COMMENT '节点类型',
    `event_type`   VARCHAR(32)  NOT NULL COMMENT '事件类型: NODE_START, NODE_END, EXECUTION_START, EXECUTION_END, ERROR, RETRY, CHECKPOINT',
    `payload`      TEXT         DEFAULT NULL COMMENT '事件详情（JSON）',
    `duration_ms`  BIGINT       DEFAULT 0 COMMENT '节点耗时（毫秒，仅 NODE_END 时有效）',
    `create_time`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX `idx_timeline_execution_id` (`execution_id`),
    INDEX `idx_timeline_agent_id`     (`agent_id`),
    INDEX `idx_timeline_event_type`   (`event_type`),
    INDEX `idx_timeline_create_time`  (`create_time`)
) COMMENT='执行时间线表';
