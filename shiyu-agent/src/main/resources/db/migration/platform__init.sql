-- ============================================
-- AI 平台与模型管理初始化脚本
-- 使用 common 数据源
-- ============================================

-- ============================================
-- 1. AI 平台表
-- ============================================
DROP TABLE IF EXISTS `ai_platform`;
CREATE TABLE `ai_platform` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '平台ID',
    `name`             VARCHAR(50)  NOT NULL COMMENT '平台名称（如 OpenAI、DeepSeek）',
    `code`             VARCHAR(50)  NOT NULL COMMENT '平台编码（如 OPENAI, DEEPSEEK, OLLAMA）',
    `base_url`         VARCHAR(500) DEFAULT NULL COMMENT 'Base URL',
    `api_key`          VARCHAR(500) DEFAULT NULL COMMENT 'API Key',
    `temperature`      DOUBLE       DEFAULT 0.7 COMMENT '默认温度参数',
    `max_tokens`       INT          DEFAULT 4096 COMMENT '默认最大 Token 数',
    `max_retries`      INT          DEFAULT 3 COMMENT '默认最大重试次数',
    `available_models` TEXT         DEFAULT NULL COMMENT '可用模型列表（JSON 数组）',
    `extra_config`     TEXT         DEFAULT NULL COMMENT '扩展配置（JSON 对象，Agent 数据源等）',
    `is_default`       CHAR(1)      DEFAULT 'N' COMMENT '是否默认平台（Y/N）',
    `status`           CHAR(1)      DEFAULT '1' COMMENT '状态（1正常 0停用）',
    `remark`           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_by`        VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`        VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`         CHAR(1)      DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX `uk_ai_platform_code` ON `ai_platform` (`code`);
COMMENT ON TABLE `ai_platform` IS 'AI 平台配置表';

-- ============================================
-- 2. AI 模型表
-- ============================================
DROP TABLE IF EXISTS `ai_model`;
CREATE TABLE `ai_model` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '模型ID',
    `platform_id`  BIGINT       NOT NULL COMMENT '所属平台ID',
    `model_name`   VARCHAR(100) NOT NULL COMMENT '模型名称（如 gpt-4o, deepseek-chat）',
    `display_name` VARCHAR(100) DEFAULT NULL COMMENT '模型显示名称',
    `description`  VARCHAR(500) DEFAULT NULL COMMENT '模型描述',
    `model_config` TEXT         DEFAULT NULL COMMENT '模型级参数覆盖（JSON 对象）',
    `is_default`   CHAR(1)      DEFAULT 'N' COMMENT '是否默认模型（Y/N，每平台一个）',
    `status`       CHAR(1)      DEFAULT '1' COMMENT '状态（1正常 0停用）',
    `sort`         INT          DEFAULT 0 COMMENT '排序',
    `create_by`    VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`     CHAR(1)      DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (`id`)
);
CREATE INDEX `idx_ai_model_platform_id` ON `ai_model` (`platform_id`);
COMMENT ON TABLE `ai_model` IS 'AI 模型配置表';

-- ============================================
-- 3. 初始化平台数据
-- ============================================
INSERT INTO `ai_platform` (`id`, `name`, `code`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `extra_config`, `is_default`, `status`, `remark`)
VALUES (1, 'OpenAI', 'OPENAI', 'https://api.openai.com/v1', '', 0.7, 4096, 3,
        '["gpt-4o","gpt-4o-mini","gpt-4-turbo","gpt-3.5-turbo"]',
        NULL, 'N', '1', 'OpenAI 官方 API');

INSERT INTO `ai_platform` (`id`, `name`, `code`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `extra_config`, `is_default`, `status`, `remark`)
VALUES (2, 'DeepSeek', 'DEEPSEEK', 'https://api.deepseek.com', '', 0.7, 4096, 3,
        '["deepseek-chat","deepseek-reasoner"]',
        NULL, 'Y', '1', 'DeepSeek 官方 API');

INSERT INTO `ai_platform` (`id`, `name`, `code`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `extra_config`, `is_default`, `status`, `remark`)
VALUES (3, 'OpenRouter', 'OPENROUTER', 'https://openrouter.ai/api', '', 0.7, 4096, 3,
        '["x-ai/grok-4.1-fast","anthropic/claude-3.5-sonnet","google/gemini-2.5-pro"]',
        NULL, 'N', '1', 'OpenRouter 聚合 API');

INSERT INTO `ai_platform` (`id`, `name`, `code`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `extra_config`, `is_default`, `status`, `remark`)
VALUES (4, 'SiliconFlow', 'SILICON_FLOW', 'https://api.siliconflow.cn', '', 0.7, 4096, 3,
        '["THUDM/GLM-Z1-9B-0414","deepseek-ai/DeepSeek-V3"]',
        NULL, 'N', '1', 'SiliconFlow 推理平台');

INSERT INTO `ai_platform` (`id`, `name`, `code`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `extra_config`, `is_default`, `status`, `remark`)
VALUES (5, 'Ollama', 'OLLAMA', 'http://localhost:11434', '', 0.7, 4096, 3,
        '["gemma3:4b","llama3.1:8b","qwen2.5:7b"]',
        NULL, 'N', '1', '本地 Ollama 推理');

-- ============================================
-- 4. 初始化默认模型数据
-- ============================================
INSERT INTO `ai_model` (`platform_id`, `model_name`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (1, 'gpt-4o', 'GPT-4o', 'OpenAI GPT-4o 多模态模型', 'Y', '1', 1);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (1, 'gpt-4o-mini', 'GPT-4o Mini', 'OpenAI GPT-4o Mini 轻量模型', 'N', '1', 2);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (2, 'deepseek-chat', 'DeepSeek Chat', 'DeepSeek 对话模型', 'Y', '1', 1);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (2, 'deepseek-reasoner', 'DeepSeek Reasoner', 'DeepSeek 推理模型', 'N', '1', 2);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (3, 'x-ai/grok-4.1-fast', 'Grok 4.1 Fast', 'xAI Grok 快速模型', 'Y', '1', 1);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (4, 'THUDM/GLM-Z1-9B-0414', 'GLM-Z1-9B', '智谱 GLM-Z1 9B 模型', 'Y', '1', 1);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (5, 'gemma3:4b', 'Gemma 3 4B', 'Google Gemma 3 4B 本地模型', 'Y', '1', 1);

-- ==================== 重置自增序列 ====================
ALTER TABLE `ai_platform` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `ai_model` ALTER COLUMN `id` RESTART WITH 100;
