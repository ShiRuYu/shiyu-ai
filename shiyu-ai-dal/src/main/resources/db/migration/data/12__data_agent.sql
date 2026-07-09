-- ============================================
-- Data: agent — AI 平台/模型/Agent定义/意图
-- ============================================

-- AI 平台
INSERT IGNORE INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `workspace_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `is_default`, `status`, `remark`, `create_by`, `update_by`)
VALUES (1, 'OpenAI', 'OPENAI', 1, 0, 'https://api.openai.com/v1', '', 0.7, 4096, 3,
        '["gpt-4o","gpt-4o-mini","gpt-4-turbo","gpt-3.5-turbo"]', 'N', 1, 'OpenAI 官方 API', 'system', 'system');

INSERT IGNORE INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `workspace_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `is_default`, `status`, `remark`, `create_by`, `update_by`)
VALUES (2, 'DeepSeek', 'DEEPSEEK', 1, 0, 'https://api.deepseek.com', '', 0.7, 4096, 3,
        '["deepseek-chat","deepseek-reasoner"]', 'Y', 1, 'DeepSeek 官方 API', 'system', 'system');

INSERT IGNORE INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `workspace_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `is_default`, `status`, `remark`, `create_by`, `update_by`)
VALUES (3, 'OpenRouter', 'OPENROUTER', 1, 0, 'https://openrouter.ai/api', '', 0.7, 4096, 3,
        '["x-ai/grok-4.1-fast","anthropic/claude-3.5-sonnet","google/gemini-2.5-pro"]', 'N', 1, 'OpenRouter 聚合 API', 'system', 'system');

-- 硅基流动（通义千问 Qwen 模型系列）
INSERT IGNORE INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `workspace_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `is_default`, `status`, `remark`, `create_by`, `update_by`)
VALUES (4, '硅基流动（通义千问）', 'SILICON_FLOW', 1, 0, 'https://api.siliconflow.cn', '', 0.7, 4096, 3,
        '["Qwen/Qwen3-14B","Qwen/Qwen3-8B"]', 'N', 1, '硅基流动（通义千问）平台 - 通义千问 Qwen 模型服务', 'system', 'system');

-- AI 模型
INSERT IGNORE INTO `ai_model` (`id`, `platform_id`, `model_name`, `tenant_id`, `workspace_id`, `display_name`, `is_default`, `status`, `sort`, `create_by`, `update_by`)
VALUES (1, 1, 'gpt-4o', 1, 0, 'GPT-4o', 'N', 1, 1, 'system', 'system'),
       (2, 1, 'gpt-4o-mini', 1, 0, 'GPT-4o Mini', 'Y', 1, 2, 'system', 'system'),
       (3, 1, 'gpt-3.5-turbo', 1, 0, 'GPT-3.5 Turbo', 'N', 1, 3, 'system', 'system'),
       (4, 2, 'deepseek-chat', 1, 0, 'DeepSeek Chat', 'Y', 1, 1, 'system', 'system'),
       (5, 2, 'deepseek-reasoner', 1, 0, 'DeepSeek Reasoner', 'N', 1, 2, 'system', 'system'),
       (6, 3, 'x-ai/grok-4.1-fast', 1, 0, 'Grok 4.1 Fast', 'N', 1, 1, 'system', 'system'),
       (7, 3, 'anthropic/claude-3.5-sonnet', 1, 0, 'Claude 3.5 Sonnet', 'Y', 1, 2, 'system', 'system'),
       -- 硅基流动（通义千问）平台模型
       (8, 4, 'Qwen/Qwen3-14B', 1, 0, 'Qwen3 14B', 'N', 1, 1, 'system', 'system'),
       (9, 4, 'Qwen/Qwen3-8B', 1, 0, 'Qwen3 8B', 'Y', 1, 2, 'system', 'system');

-- Agent 定义
INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (1, 'tutor-bot', '辅导助手', '通用学科辅导 Agent（使用硅基流动 Qwen3-8B 模型）', 1, 0, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (2, 'knowledge-tutor', '知识问答助手', '知识问答 Agent（使用硅基流动 Qwen3-8B 模型）', 1, 0, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (3, 'gcx-assistant', 'GCX 助手', 'GCX 智能助手（使用硅基流动 Qwen3-8B 模型）', 1, 0, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (4, 'simple-assistant', '简单助手', '基础 LLM 问答助手（使用硅基流动 Qwen3-8B 模型）', 1, 0, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (5, 'rag-knowledge-agent', '知识库问答', '基于文档知识库的 RAG 检索问答（使用硅基流动 Qwen3-8B 模型）', 1, 0, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (6, 'smart-agent', '智能路由助手', '意图识别 + RAG + 工具调用全功能智能助手（使用硅基流动 Qwen3-8B 模型）', 1, 0, 'v1.0.0', 1, 'system', 'system');

-- Agent 版本
INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (1, 'tutor-bot', 'v1.0.0', 1, 0, '初始版本', 'PUBLISHED', 
'{"name":"tutor-graph","description":"辅导助手工作流程","startNode":"intent","endNode":"output","nodes":{"intent":{"nodeName":"意图识别","nodeType":"INTENT","enabled":true,"config":{"category":"EDUCATION"}},"knowledge":{"nodeName":"知识检索","nodeType":"RAG_RETRIEVAL","enabled":true,"config":{"topK":5,"similarityThreshold":0.7}},"teach":{"nodeName":"教学讲解","nodeType":"EDUCATION_TEACH","enabled":true,"config":{}},"practice":{"nodeName":"出题练习","nodeType":"EDUCATION_PRACTICE","enabled":true,"config":{}},"review":{"nodeName":"复习安排","nodeType":"REVIEW_SCHEDULE","enabled":true,"config":{}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"MARKDOWN"}}},"edges":{"intent":["knowledge"],"knowledge":["teach"],"teach":["practice"],"practice":["review"],"review":["output"]},"conditionalEdges":{}}',
'{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"intent":{"x":100,"y":100},"knowledge":{"x":300,"y":100},"teach":{"x":500,"y":100},"practice":{"x":700,"y":100},"review":{"x":900,"y":100},"output":{"x":1100,"y":100}}}',
'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (2, 'knowledge-tutor', 'v1.0.0', 1, 0, '初始版本', 'PUBLISHED', 
'{"name":"knowledge-qa-graph","description":"知识问答工作流程","startNode":"intent","endNode":"output","nodes":{"intent":{"nodeName":"意图识别","nodeType":"INTENT","enabled":true,"config":{"category":"KNOWLEDGE"}},"retrieval":{"nodeName":"知识检索","nodeType":"RAG_RETRIEVAL","enabled":true,"config":{"topK":5,"similarityThreshold":0.7}},"enhancement":{"nodeName":"知识增强","nodeType":"RAG_ENHANCEMENT","enabled":true,"config":{"enhancementStrategy":"SUMMARIZATION"}},"llm":{"nodeName":"LLM生成","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"DEEPSEEK","modelName":"deepseek-chat","temperature":0.7}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"MARKDOWN"}}},"edges":{"intent":["retrieval"],"retrieval":["enhancement"],"enhancement":["llm"],"llm":["output"]},"conditionalEdges":{}}',
'{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"intent":{"x":100,"y":100},"retrieval":{"x":300,"y":100},"enhancement":{"x":500,"y":100},"llm":{"x":700,"y":100},"output":{"x":900,"y":100}}}',
'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (3, 'gcx-assistant', 'v1.0.0', 1, 0, '初始版本', 'PUBLISHED', 
'{"name":"gcx-graph","description":"GCX助手工作流程","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入节点","nodeType":"DEFAULT","enabled":true},"llm":{"nodeName":"GCX回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["llm"],"llm":["output"]},"conditionalEdges":{}}',
'{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"input":{"x":100,"y":100},"llm":{"x":300,"y":100},"output":{"x":500,"y":100}}}',
'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (4, 'simple-assistant', 'v1.0.0', 1, 0, '初始版本', 'PUBLISHED', 
'{"name":"simple-graph","description":"简单助手工作流程","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入节点","nodeType":"DEFAULT","enabled":true},"llm":{"nodeName":"LLM回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","defaultPrompt":"你是一个智能助手，请友好地回答用户的问题。"}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["llm"],"llm":["output"]},"conditionalEdges":{}}',
'{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"input":{"x":100,"y":100},"llm":{"x":300,"y":100},"output":{"x":500,"y":100}}}',
'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (5, 'rag-knowledge-agent', 'v1.0.0', 1, 0, '初始版本', 'PUBLISHED', 
'{"name":"rag-qa-graph","description":"知识库问答工作流程","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入节点","nodeType":"DEFAULT","enabled":true},"rag_retrieval":{"nodeName":"知识库检索","nodeType":"RAG_RETRIEVAL","enabled":true,"config":{"topK":5,"similarityThreshold":0.6}},"rag_enhance":{"nodeName":"检索增强","nodeType":"RAG_ENHANCEMENT","enabled":true,"config":{"enhancementStrategy":"SUMMARIZATION","contextWindowSize":3,"addContext":true}},"llm":{"nodeName":"LLM回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","promptTemplate":"基于以下检索到的文档内容回答用户问题。\n\n{context}\n\n用户问题: {query}"}},"output":{"nodeName":"格式化输出","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["rag_retrieval"],"rag_retrieval":["rag_enhance"],"rag_enhance":["llm"],"llm":["output"]},"conditionalEdges":{}}',
'{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"input":{"x":100,"y":100},"rag_retrieval":{"x":300,"y":100},"rag_enhance":{"x":500,"y":100},"llm":{"x":700,"y":100},"output":{"x":900,"y":100}}}',
'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (6, 'smart-agent', 'v1.0.0', 1, 0, '初始版本', 'PUBLISHED', 
'{"name":"smart-graph","description":"智能路由助手工作流程","startNode":"intent","endNode":"output","nodes":{"intent":{"nodeName":"意图识别","nodeType":"INTENT","enabled":true,"config":{"category":"general"}},"llm_chat":{"nodeName":"闲聊回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","defaultPrompt":"你是一个友好的 AI 助手，请用轻松自然的语气和用户聊天。"}},"rag_retrieval":{"nodeName":"知识库检索","nodeType":"RAG_RETRIEVAL","enabled":true,"config":{"topK":3}},"rag_enhance":{"nodeName":"检索增强","nodeType":"RAG_ENHANCEMENT","enabled":true,"config":{"enhancementStrategy":"SUMMARIZATION","contextWindowSize":3}},"rag_llm":{"nodeName":"RAG回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","promptTemplate":"基于以下检索到的文档回答用户问题。\n\n{context}\n\n用户问题: {query}"}},"tool_call_weather":{"nodeName":"天气查询工具","nodeType":"TOOL_CALL","enabled":true,"config":{"toolName":"WEATHER","enableCache":true}},"tool_call_calculator":{"nodeName":"计算器工具","nodeType":"TOOL_CALL","enabled":true,"config":{"toolName":"CALCULATOR","enableCache":true}},"tool_llm":{"nodeName":"工具结果回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","promptTemplate":"以下是工具执行结果，请用自然语言回复用户。\n\n工具结果: {toolResult}\n\n用户问题: {query}"}},"output":{"nodeName":"格式化输出","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"rag_retrieval":["rag_enhance"],"rag_enhance":["rag_llm"],"rag_llm":["output"],"llm_chat":["output"],"tool_call_weather":["tool_llm"],"tool_call_calculator":["tool_llm"],"tool_llm":["output"]},"conditionalEdges":{"intent":{"conditionType":"INTENT","defaultTarget":"llm_chat","nodeMappings":{"CHITCHAT":"llm_chat","QUESTION":"rag_retrieval","CALCULATOR":"tool_call_calculator","WEATHER":"tool_call_weather"}}}}',
'{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"intent":{"x":100,"y":100},"llm_chat":{"x":300,"y":50},"rag_retrieval":{"x":300,"y":150},"rag_enhance":{"x":500,"y":150},"rag_llm":{"x":700,"y":150},"tool_call_weather":{"x":300,"y":250},"tool_call_calculator":{"x":300,"y":350},"tool_llm":{"x":500,"y":300},"output":{"x":900,"y":200}}}',
'system', 'system');

-- 意图定义
INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (1, 'default', 'CHITCHAT', '闲聊', 1, 0, '日常闲聊对话', 'CONVERSATION', 10, 0.75, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (2, 'default', 'QUESTION', '问答', 1, 0, '知识问答', 'KNOWLEDGE', 50, 0.80, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (3, 'default', 'TRANSLATION', '翻译', 1, 0, '语言翻译', 'TASK', 60, 0.85, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (4, 'default', 'EDUCATION', '教育', 1, 0, '学科教育辅导', 'TASK', 70, 0.80, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (5, 'default', 'CODE_HELP', '代码帮助', 1, 0, '编程辅助与代码生成', 'TASK', 65, 0.80, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (6, 'default', 'WRITING_ASSISTANCE', '写作辅助', 1, 0, '文章写作与润色', 'TASK', 55, 0.75, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (7, 'default', 'DATA_ANALYSIS', '数据分析', 1, 0, '数据处理与分析', 'TASK', 60, 0.80, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (8, 'default', 'UNKNOWN', '未知意图', 1, 0, '无法识别的意图', 'CONVERSATION', 0, 0.50, '1', 1, 'system', 'system');
