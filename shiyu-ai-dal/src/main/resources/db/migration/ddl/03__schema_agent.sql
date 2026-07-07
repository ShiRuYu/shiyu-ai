-- ============================================
-- Schema: schema_agent
-- ============================================


CREATE TABLE IF NOT EXISTS `ai_platform` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '平台ID',
    `name`             VARCHAR(50)  NOT NULL COMMENT '平台名称（如 OpenAI、DeepSeek）',
    `code`             VARCHAR(50)  NOT NULL COMMENT '平台编码（如 OPENAI, DEEPSEEK, OLLAMA）',
    `tenant_id`        BIGINT       NOT NULL COMMENT '租户ID',
    `workspace_id`     BIGINT       NOT NULL COMMENT '工作空间ID',
    `base_url`         VARCHAR(500) DEFAULT NULL COMMENT 'Base URL',
    `api_key`          VARCHAR(500) DEFAULT NULL COMMENT 'API Key',
    `temperature`      DOUBLE       DEFAULT 0.7 COMMENT '默认温度参数',
    `max_tokens`       INT          DEFAULT 4096 COMMENT '默认最大 Token 数',
    `max_retries`      INT          DEFAULT 3 COMMENT '默认最大重试次数',
    `available_models` TEXT         DEFAULT NULL COMMENT '可用模型列表（JSON 数组）',
    `extra_config`     TEXT         DEFAULT NULL COMMENT '扩展配置（JSON 对象，Agent 数据源等）',
    `is_default`       CHAR(1)      DEFAULT 'N' COMMENT '是否默认平台（Y/N）',
    `status`           TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `remark`           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `ext_info`          CLOB         DEFAULT NULL COMMENT '扩展字段：聚合的节点入参定义 (JSON)',
    `create_by`        VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`         TINYINT      DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_ai_platform_code` ON `ai_platform` (`code`);

COMMENT ON TABLE `ai_platform` IS 'AI 平台配置表';


CREATE TABLE IF NOT EXISTS `ai_model` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '模型ID',
    `platform_id`  BIGINT       NOT NULL COMMENT '所属平台ID',
    `model_name`   VARCHAR(100) NOT NULL COMMENT '模型名称（如 gpt-4o, deepseek-chat）',
    `tenant_id`    BIGINT       NOT NULL COMMENT '租户ID',
    `workspace_id` BIGINT       NOT NULL COMMENT '工作空间ID',
    `display_name` VARCHAR(100) DEFAULT NULL COMMENT '模型显示名称',
    `description`  VARCHAR(500) DEFAULT NULL COMMENT '模型描述',
    `model_config` TEXT         DEFAULT NULL COMMENT '模型级参数覆盖（JSON 对象）',
    `is_default`   CHAR(1)      DEFAULT 'N' COMMENT '是否默认模型（Y/N，每平台一个）',
    `status`       TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `sort`          INT          DEFAULT 0 COMMENT '排序',
    `create_by`    VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     TINYINT      DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_ai_model_platform_id` ON `ai_model` (`platform_id`);

COMMENT ON TABLE `ai_model` IS 'AI 模型配置表';


CREATE TABLE IF NOT EXISTS `agent_def` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `agent_id`        VARCHAR(64)  NOT NULL COMMENT 'Agent唯一标识',
    `name`            VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    `description`     VARCHAR(500) DEFAULT NULL COMMENT 'Agent描述',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `workspace_id`    BIGINT       NOT NULL COMMENT '工作空间ID',
    `owner_id`        BIGINT       DEFAULT NULL COMMENT '所属用户ID(为空则所有用户可见)',
    `current_version` VARCHAR(32)  DEFAULT NULL COMMENT '当前激活版本号',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态(1正常 0停用)',
    `ext_info`        CLOB         DEFAULT NULL COMMENT '扩展字段：聚合的节点入参定义 (JSON)',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志(0存在 1删除)',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_agent_def_agent_id` ON `agent_def` (`agent_id`);

COMMENT ON TABLE `agent_def` IS 'Agent 定义表';


CREATE TABLE IF NOT EXISTS `agent_version` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `agent_id`        VARCHAR(64)  NOT NULL COMMENT '所属Agent标识',
    `version_number`  VARCHAR(32)  NOT NULL COMMENT '版本号(如v1.0.0)',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `workspace_id`    BIGINT       NOT NULL COMMENT '工作空间ID',
    `description`     VARCHAR(500) DEFAULT NULL COMMENT '版本描述',
    `status`          VARCHAR(16)  DEFAULT 'DRAFT' COMMENT '版本状态(DRAFT/PUBLISHED/ARCHIVED)',
    `graph_config`    CLOB         DEFAULT NULL COMMENT 'Graph配置JSON',
    `canvas_config`   CLOB         DEFAULT NULL COMMENT '画布布局JSON',
    `ext_info`        CLOB         DEFAULT NULL COMMENT '扩展字段：版本所有节点的入参定义 (JSON)',
    `del_flag`        CHAR(1)      DEFAULT '0' COMMENT '删除标志(0存在 1删除)',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_agent_version` ON `agent_version` (`agent_id`, `version_number`);

COMMENT ON TABLE `agent_version` IS 'Agent 版本表';


CREATE TABLE IF NOT EXISTS `intent_def` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `agent_id`         VARCHAR(64)  NOT NULL DEFAULT 'default' COMMENT '所属Agent标识',
    `code`             VARCHAR(64)  NOT NULL COMMENT '意图代码',
    `name`             VARCHAR(100) NOT NULL COMMENT '意图名称',
    `tenant_id`        BIGINT       NOT NULL COMMENT '租户ID',
    `workspace_id`     BIGINT       NOT NULL COMMENT '工作空间ID',
    `description`      VARCHAR(500) DEFAULT NULL COMMENT '意图描述',
    `category`         VARCHAR(64)  DEFAULT 'CONVERSATION' COMMENT '意图分类',
    `priority`         INT          DEFAULT 50 COMMENT '优先级',
    `confidence_threshold` DOUBLE   DEFAULT 0.75 COMMENT '置信度阈值',
    `examples`         TEXT         DEFAULT NULL COMMENT '示例语句（JSON数组）',
    `target_node`      VARCHAR(64)  DEFAULT NULL COMMENT '路由目标节点ID',
    `require_slot_filling` CHAR(1)  DEFAULT '0' COMMENT '是否需要槽位填充(1是 0否)',
    `slots`            TEXT         DEFAULT NULL COMMENT '槽位定义（JSON对象）',
    `parameter_mapping` TEXT        DEFAULT NULL COMMENT 'Slot\u2192工具参数映射（JSON对象）',
    `slot_defaults`    TEXT         DEFAULT NULL COMMENT 'Slot默认值（JSON对象）',
    `enabled`          CHAR(1)      DEFAULT '1' COMMENT '是否启用(1是 0否)',
    `status`           TINYINT      DEFAULT 1 COMMENT '状态(1正常 0停用)',
    `del_flag`         TINYINT      DEFAULT 0 COMMENT '删除标志(0存在 1删除)',
    `create_by`        VARCHAR(64)  DEFAULT NULL,
    `create_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`        VARCHAR(64)  DEFAULT NULL,
    `update_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_intent_def_code_agent` ON `intent_def` (`agent_id`, `code`);

COMMENT ON TABLE `intent_def` IS '意图定义表';

