-- ============================================
-- Schema: schema_memory
-- ============================================


CREATE TABLE IF NOT EXISTS `conversation_message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `session_id`  VARCHAR(64)  NOT NULL COMMENT '会话ID',
    `user_id`     BIGINT       DEFAULT NULL,
    `agent_id`    VARCHAR(64)  DEFAULT NULL,
    `tenant_id`   BIGINT       NOT NULL COMMENT '租户ID',
    `role`        VARCHAR(16)  NOT NULL COMMENT '角色(user/assistant/system/tool)',
    `content`     TEXT         NOT NULL COMMENT '消息内容',
    `create_by`   VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`   VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time` TIMESTAMP    DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_conv_msg_session` ON `conversation_message` (`session_id`, `create_time`);

COMMENT ON TABLE `conversation_message` IS '对话消息表（短期记忆）';


CREATE TABLE IF NOT EXISTS `long_term_memory` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       DEFAULT NULL,
    `agent_id`      VARCHAR(64)  DEFAULT NULL,
    `tenant_id`     BIGINT       NOT NULL COMMENT '租户ID',
    `category`      VARCHAR(64)  DEFAULT 'general' COMMENT '记忆分类',
    `memory_key`    VARCHAR(255) NOT NULL COMMENT '记忆键',
    `content`       TEXT         NOT NULL COMMENT '记忆内容',
    `importance`    DOUBLE       DEFAULT 0.5 COMMENT '重要度评分(0-1)',
    `source`        VARCHAR(64)  DEFAULT NULL COMMENT '来源(session_id等)',
    `create_by`     VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`     VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_ltm_user_agent` ON `long_term_memory` (`user_id`, `agent_id`);

CREATE INDEX IF NOT EXISTS `idx_ltm_category` ON `long_term_memory` (`category`);

COMMENT ON TABLE `long_term_memory` IS '长期记忆表';


CREATE TABLE IF NOT EXISTS `agent_execution` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `execution_id`  VARCHAR(64)  NOT NULL COMMENT '执行唯一ID(UUID)',
    `agent_id`      VARCHAR(64)  NOT NULL COMMENT 'Agent标识',
    `version`       VARCHAR(32)  DEFAULT NULL COMMENT '版本号',
    `user_id`       BIGINT       DEFAULT NULL,
    `session_id`    VARCHAR(64)  DEFAULT NULL COMMENT '会话ID',
    `tenant_id`     BIGINT       NOT NULL COMMENT '租户ID',
    `node_id`       VARCHAR(64)  DEFAULT NULL COMMENT '节点ID',
    `node_type`     VARCHAR(32)  DEFAULT NULL COMMENT '节点类型',
    `input_data`    TEXT         DEFAULT NULL COMMENT '输入数据(JSON)',
    `output_data`   TEXT         DEFAULT NULL COMMENT '输出数据(JSON)',
    `status`        VARCHAR(16)  NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/SUCCESS/FAILED',
    `error_message` TEXT         DEFAULT NULL COMMENT '错误信息',
    `start_time`    TIMESTAMP    NOT NULL COMMENT '开始时间',
    `end_time`      TIMESTAMP    DEFAULT NULL COMMENT '结束时间',
    `duration_ms`   BIGINT       DEFAULT NULL COMMENT '耗时(毫秒)',
    `create_by`     VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`     VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`   TIMESTAMP    DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_agent_exec_exec_id` ON `agent_execution` (`execution_id`);

CREATE INDEX IF NOT EXISTS `idx_agent_exec_agent` ON `agent_execution` (`agent_id`, `create_time`);

CREATE INDEX IF NOT EXISTS `idx_agent_exec_session` ON `agent_execution` (`session_id`);

COMMENT ON TABLE `agent_execution` IS 'Agent 执行历史记录表';



-- ============================================
-- Agent Runtime tables
-- ============================================

-- 节点执行记录表
CREATE TABLE IF NOT EXISTS `node_execution` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `execution_id`  VARCHAR(64)  NOT NULL COMMENT '执行ID',
    `node_id`       VARCHAR(64)  NOT NULL COMMENT '节点ID',
    `node_type`     VARCHAR(32)  NOT NULL COMMENT '节点类型',
    `status`        VARCHAR(16)  NOT NULL DEFAULT 'RUNNING' COMMENT 'PENDING/RUNNING/COMPLETED/FAILED',
    `input_data`    TEXT         DEFAULT NULL COMMENT '输入数据(JSON)',
    `output_data`   TEXT         DEFAULT NULL COMMENT '输出数据(JSON)',
    `error_message` TEXT         DEFAULT NULL COMMENT '错误信息',
    `start_time`    TIMESTAMP    NOT NULL COMMENT '开始时间',
    `end_time`      TIMESTAMP    DEFAULT NULL COMMENT '结束时间',
    `duration_ms`   BIGINT       DEFAULT NULL COMMENT '耗时(毫秒)',
    `retry_count`   INT          DEFAULT 0 COMMENT '重试次数',
    `create_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_node_exec_exec_id` ON `node_execution` (`execution_id`);

CREATE INDEX IF NOT EXISTS `idx_node_exec_node_id` ON `node_execution` (`node_id`);

COMMENT ON TABLE `node_execution` IS '节点执行记录表';


-- 检查点表
CREATE TABLE IF NOT EXISTS `agent_checkpoint` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `checkpoint_id`   VARCHAR(64)  NOT NULL COMMENT '检查点ID(UUID)',
    `execution_id`    VARCHAR(64)  NOT NULL COMMENT '执行ID',
    `node_id`         VARCHAR(64)  NOT NULL COMMENT '节点ID',
    `state_data`      TEXT         DEFAULT NULL COMMENT '状态数据(JSON)',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_ckpt_checkpoint_id` ON `agent_checkpoint` (`checkpoint_id`);

CREATE INDEX IF NOT EXISTS `idx_ckpt_execution_id` ON `agent_checkpoint` (`execution_id`);

COMMENT ON TABLE `agent_checkpoint` IS 'Agent 检查点表';


-- 情景记忆表（执行历史）
CREATE TABLE IF NOT EXISTS `episodic_memory` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `execution_id`     VARCHAR(64)  NOT NULL COMMENT '执行ID',
    `agent_id`         VARCHAR(64)  NOT NULL COMMENT 'Agent标识',
    `user_id`          BIGINT       DEFAULT NULL,
    `session_id`       VARCHAR(64)  DEFAULT NULL COMMENT '会话ID',
    `task_type`        VARCHAR(64)  DEFAULT NULL COMMENT '任务类型',
    `task_description` TEXT         DEFAULT NULL COMMENT '任务描述',
    `status`           VARCHAR(16)  DEFAULT NULL COMMENT '执行状态',
    `result_summary`   TEXT         DEFAULT NULL COMMENT '结果摘要',
    `error_message`    VARCHAR(1024) DEFAULT NULL COMMENT '错误信息',
    `duration_ms`      BIGINT       DEFAULT NULL COMMENT '耗时(毫秒)',
    `node_count`       INT          DEFAULT 0 COMMENT '节点数',
    `create_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_episodic_exec_id` ON `episodic_memory` (`execution_id`);
CREATE INDEX IF NOT EXISTS `idx_episodic_agent_id` ON `episodic_memory` (`agent_id`);
CREATE INDEX IF NOT EXISTS `idx_episodic_user_id` ON `episodic_memory` (`user_id`);

COMMENT ON TABLE `episodic_memory` IS '情景记忆表（Agent执行历史）';


-- ============================================
-- Schema: usage — Token 用量统计
-- ============================================

CREATE TABLE IF NOT EXISTS `token_usage` (
    `id`               VARCHAR(64)  NOT NULL COMMENT 'UUID',
    `platform`         VARCHAR(50)  NOT NULL COMMENT '平台编码',
    `model`            VARCHAR(100) NOT NULL COMMENT '模型名称',
    `prompt_tokens`    INT          NOT NULL DEFAULT 0 COMMENT '输入 Token 数',
    `completion_tokens` INT         NOT NULL DEFAULT 0 COMMENT '输出 Token 数',
    `total_tokens`     INT          NOT NULL DEFAULT 0 COMMENT '总 Token 数',
    `latency_ms`       BIGINT       NOT NULL DEFAULT 0 COMMENT '延迟(毫秒)',
    `cost`             DOUBLE       NOT NULL DEFAULT 0 COMMENT '费用',
    `user_id`          BIGINT       DEFAULT NULL COMMENT '用户ID',
    `session_id`       VARCHAR(64)  DEFAULT NULL COMMENT '会话ID',
    `create_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_token_usage_platform` ON `token_usage` (`platform`);
CREATE INDEX IF NOT EXISTS `idx_token_usage_model` ON `token_usage` (`model`);
CREATE INDEX IF NOT EXISTS `idx_token_usage_user` ON `token_usage` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_token_usage_time` ON `token_usage` (`create_time`);

COMMENT ON TABLE `token_usage` IS 'Token 用量统计表';
