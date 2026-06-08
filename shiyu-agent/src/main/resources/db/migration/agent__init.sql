-- ============================================
-- AI 平台与模型管理初始化脚本
-- 使用 agent 数据源
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

-- ============================================
-- 5. Agent 定义表
-- ============================================
DROP TABLE IF EXISTS `agent_def`;
CREATE TABLE `agent_def` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `agent_id`        VARCHAR(64)  NOT NULL COMMENT 'Agent唯一标识',
    `name`            VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    `description`     VARCHAR(500) DEFAULT NULL COMMENT 'Agent描述',
    `owner_id`        BIGINT       DEFAULT NULL COMMENT '所属用户ID(为空则所有用户可见)',
    `current_version` VARCHAR(32)  DEFAULT NULL COMMENT '当前激活版本号',
    `status`          CHAR(1)      DEFAULT '1' COMMENT '状态(1正常 0停用)',
    `del_flag`        CHAR(1)      DEFAULT '0' COMMENT '删除标志(0存在 1删除)',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX `uk_agent_def_agent_id` ON `agent_def` (`agent_id`);
COMMENT ON TABLE `agent_def` IS 'Agent 定义表';

-- ============================================
-- 6. Agent 版本表
-- ============================================
DROP TABLE IF EXISTS `agent_version`;
CREATE TABLE `agent_version` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `agent_id`        VARCHAR(64)  NOT NULL COMMENT '所属Agent标识',
    `version_number`  VARCHAR(32)  NOT NULL COMMENT '版本号(如v1.0.0)',
    `description`     VARCHAR(500) DEFAULT NULL COMMENT '版本描述',
    `status`          VARCHAR(16)  DEFAULT 'DRAFT' COMMENT '版本状态(DRAFT/PUBLISHED/ARCHIVED)',
    `graph_config`    CLOB         DEFAULT NULL COMMENT 'Graph配置JSON',
    `canvas_config`   CLOB         DEFAULT NULL COMMENT '画布布局JSON',
    `del_flag`        CHAR(1)      DEFAULT '0' COMMENT '删除标志(0存在 1删除)',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX `uk_agent_version` ON `agent_version` (`agent_id`, `version_number`);
COMMENT ON TABLE `agent_version` IS 'Agent 版本表';

-- ============================================
-- 7. 种子数据：示例 Agent
-- ============================================
INSERT INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `current_version`, `status`)
VALUES (1, 'simple-assistant', '简单助手', '基础 LLM 问答助手，直接调用大模型回答用户问题', 'v1.0.0', '1');

INSERT INTO `agent_version` (`id`, `agent_id`, `version_number`, `description`, `status`, `graph_config`)
VALUES (1, 'simple-assistant', 'v1.0.0', '初始版本', 'PUBLISHED',
'{"name":"simple-assistant_graph","description":"基础LLM问答","startNode":"llm","endNode":"llm","nodes":{"llm":{"nodeName":"LLM 回答","nodeType":"LLM_CALL","enabled":true,"timeout":30000,"retryCount":0,"errorStrategy":"THROW","config":{"defaultPrompt":"你是一个智能助手，请友好地回答用户的问题。","stream":false}}},"edges":{},"conditionalEdges":{}}');

INSERT INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `current_version`, `status`)
VALUES (2, 'rag-knowledge-agent', '知识库问答', '基于文档知识库的 RAG 检索问答', 'v1.0.0', '1');

INSERT INTO `agent_version` (`id`, `agent_id`, `version_number`, `description`, `status`, `graph_config`)
VALUES (2, 'rag-knowledge-agent', 'v1.0.0', '初始版本', 'PUBLISHED',
'{"name":"rag-knowledge-agent_graph","description":"RAG知识库检索问答","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入","nodeType":"DEFAULT","enabled":true,"timeout":30000,"config":{}},"rag_retrieval":{"nodeName":"知识库检索","nodeType":"RAG_RETRIEVAL","enabled":true,"timeout":30000,"config":{"topK":5,"similarityThreshold":0.6}},"rag_enhance":{"nodeName":"检索增强","nodeType":"RAG_ENHANCEMENT","enabled":true,"timeout":30000,"config":{"enhancementStrategy":"SUMMARIZATION","contextWindowSize":3,"maxLength":2000,"addContext":true}},"llm":{"nodeName":"LLM 回答","nodeType":"LLM_CALL","enabled":true,"timeout":30000,"config":{"promptTemplate":"基于以下检索到的文档内容回答用户问题。\n\n{context}\n\n用户问题: {query}","stream":false}},"output":{"nodeName":"格式化输出","nodeType":"OUTPUT_FORMAT","enabled":true,"timeout":30000,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["rag_retrieval"],"rag_retrieval":["rag_enhance"],"rag_enhance":["llm"],"llm":["output"]},"conditionalEdges":{}}');

INSERT INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `current_version`, `status`)
VALUES (3, 'smart-agent', '智能路由助手', '支持意图识别、RAG 知识检索、工具调用、闲聊的全功能智能助手', 'v1.0.0', '1');

INSERT INTO `agent_version` (`id`, `agent_id`, `version_number`, `description`, `status`, `graph_config`)
VALUES (3, 'smart-agent', 'v1.0.0', '初始版本', 'PUBLISHED',
'{"name":"smart-agent_graph","description":"全功能智能路由助手","startNode":"intent","endNode":"output","nodes":{"intent":{"nodeName":"意图识别","nodeType":"INTENT","enabled":true,"timeout":30000,"config":{"category":"general","confidenceThreshold":0.75}},"llm_chat":{"nodeName":"闲聊回答","nodeType":"LLM_CALL","enabled":true,"timeout":30000,"config":{"defaultPrompt":"你是一个友好的 AI 助手，请用轻松自然的语气和用户聊天。","stream":false}},"rag_retrieval":{"nodeName":"知识库检索","nodeType":"RAG_RETRIEVAL","enabled":true,"timeout":30000,"config":{"topK":3}},"rag_enhance":{"nodeName":"检索增强","nodeType":"RAG_ENHANCEMENT","enabled":true,"timeout":30000,"config":{"enhancementStrategy":"SUMMARIZATION","contextWindowSize":3}},"rag_llm":{"nodeName":"RAG 回答","nodeType":"LLM_CALL","enabled":true,"timeout":30000,"config":{"promptTemplate":"基于以下检索到的文档回答用户问题。\n\n{context}\n\n用户问题: {query}","stream":false}},"tool_call_weather":{"nodeName":"天气查询工具","nodeType":"TOOL_CALL","enabled":true,"timeout":30000,"config":{"toolName":"WEATHER","enableCache":true}},"tool_call_calculator":{"nodeName":"计算器工具","nodeType":"TOOL_CALL","enabled":true,"timeout":30000,"config":{"toolName":"CALCULATOR","enableCache":true}},"tool_llm":{"nodeName":"工具结果回答","nodeType":"LLM_CALL","enabled":true,"timeout":30000,"config":{"promptTemplate":"以下是工具执行结果，请用自然语言回复用户。\n\n工具结果: {toolResult}\n\n用户问题: {query}","stream":false}},"output":{"nodeName":"格式化输出","nodeType":"OUTPUT_FORMAT","enabled":true,"timeout":30000,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"rag_retrieval":["rag_enhance"],"rag_enhance":["rag_llm"],"rag_llm":["output"],"llm_chat":["output"],"tool_call_weather":["tool_llm"],"tool_call_calculator":["tool_llm"],"tool_llm":["output"]},"conditionalEdges":{"intent":{"defaultTarget":"llm_chat","nodeMappings":{"CHITCHAT":"llm_chat","QUESTION":"rag_retrieval","CALCULATOR":"tool_call_calculator","WEATHER":"tool_call_weather"},"conditionType":"INTENT_ROUTING"}}}');

-- ==================== 重置自增序列 ====================
ALTER TABLE `ai_platform` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `ai_model` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `agent_def` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `agent_version` ALTER COLUMN `id` RESTART WITH 100;
