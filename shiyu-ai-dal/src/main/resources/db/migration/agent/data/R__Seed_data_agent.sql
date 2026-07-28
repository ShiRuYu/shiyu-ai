-- ============================================
-- Data: agent — AI 平台/模型/Agent定义/意图
-- ============================================

-- AI 平台
INSERT IGNORE INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `is_default`, `status`, `remark`, `create_by`, `update_by`)
VALUES (1, 'OpenAI', 'OPENAI', 1, 'https://api.openai.com/v1', '', 0.7, 4096, 3, '["gpt-4o","gpt-4o-mini","gpt-4-turbo","gpt-3.5-turbo"]', 'N', 1, 'OpenAI 官方 API', 'system', 'system');

INSERT IGNORE INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `is_default`, `status`, `remark`, `create_by`, `update_by`)
VALUES (2, 'DeepSeek', 'DEEPSEEK', 1, 'https://api.deepseek.com', '', 0.7, 4096, 3, '["deepseek-chat","deepseek-reasoner"]', 'Y', 1, 'DeepSeek 官方 API', 'system', 'system');

INSERT IGNORE INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `is_default`, `status`, `remark`, `create_by`, `update_by`)
VALUES (3, 'OpenRouter', 'OPENROUTER', 1, 'https://openrouter.ai/api', '', 0.7, 4096, 3, '["x-ai/grok-4.1-fast","anthropic/claude-3.5-sonnet","google/gemini-2.5-pro"]', 'N', 1, 'OpenRouter 聚合 API', 'system', 'system');

-- 硅基流动（通义千问 Qwen 模型系列）
INSERT IGNORE INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `is_default`, `status`, `remark`, `create_by`, `update_by`)
VALUES (4, '硅基流动（通义千问）', 'SILICON_FLOW', 1, 'https://api.siliconflow.cn', '', 0.7, 4096, 3, '["Qwen/Qwen3-14B","Qwen/Qwen3-8B"]', 'N', 1, '硅基流动（通义千问）平台 - 通义千问 Qwen 模型服务', 'system', 'system');

-- AI 模型
INSERT IGNORE INTO `ai_model` (`id`, `platform_id`, `model_name`, `tenant_id`, `display_name`, `is_default`, `status`, `sort`, `create_by`, `update_by`)
VALUES (1, 1, 'gpt-4o', 1, 'GPT-4o', 'N', 1, 1, 'system', 'system'),
       (2, 1, 'gpt-4o-mini', 1, 'GPT-4o Mini', 'Y', 1, 2, 'system', 'system'),
       (3, 1, 'gpt-3.5-turbo', 1, 'GPT-3.5 Turbo', 'N', 1, 3, 'system', 'system'),
       (4, 2, 'deepseek-chat', 1, 'DeepSeek Chat', 'Y', 1, 1, 'system', 'system'),
       (5, 2, 'deepseek-reasoner', 1, 'DeepSeek Reasoner', 'N', 1, 2, 'system', 'system'),
       (6, 3, 'x-ai/grok-4.1-fast', 1, 'Grok 4.1 Fast', 'N', 1, 1, 'system', 'system'),
       (7, 3, 'anthropic/claude-3.5-sonnet', 1, 'Claude 3.5 Sonnet', 'Y', 1, 2, 'system', 'system'),
       -- 硅基流动（通义千问）平台模型
       (8, 4, 'Qwen/Qwen3-14B', 1, 'Qwen3 14B', 'N', 1, 1, 'system', 'system'),
       (9, 4, 'Qwen/Qwen3-8B', 1, 'Qwen3 8B', 'Y', 1, 2, 'system', 'system');

-- Agent 定义
INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (1, 'tutor-bot', '辅导助手', '通用学科辅导 Agent（使用硅基流动 Qwen3-8B 模型）', 1, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (2, 'knowledge-tutor', '知识问答助手', '知识问答 Agent（使用硅基流动 Qwen3-8B 模型）', 1, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (3, 'gcx-assistant', 'GCX 助手', 'GCX 智能助手（使用硅基流动 Qwen3-8B 模型）', 1, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (4, 'simple-assistant', '简单助手', '基础 LLM 问答助手（使用硅基流动 Qwen3-8B 模型）', 1, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (5, 'rag-knowledge-agent', '知识库问答', '基于文档知识库的 RAG 检索问答（使用硅基流动 Qwen3-8B 模型）', 1, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (6, 'smart-agent', '智能路由助手', '意图识别 + RAG + 工具调用全功能智能助手（使用硅基流动 Qwen3-8B 模型）', 1, 'v1.0.0', 1, 'system', 'system');

-- Agent 版本
INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (1, 'tutor-bot', 'v1.0.0', 1, '初始版本', '1', '{"name":"tutor-graph","description":"辅导助手工作流程","startNode":"intent","endNode":"output","nodes":{"intent":{"nodeName":"意图识别","nodeType":"INTENT","enabled":true,"config":{"category":"EDUCATION"}},"knowledge":{"nodeName":"知识检索","nodeType":"RAG_RETRIEVAL","enabled":true,"config":{"topK":5,"similarityThreshold":0.7}},"teach":{"nodeName":"教学讲解","nodeType":"EDUCATION_TEACH","enabled":true,"config":{}},"practice":{"nodeName":"出题练习","nodeType":"EDUCATION_PRACTICE","enabled":true,"config":{}},"review":{"nodeName":"复习安排","nodeType":"REVIEW_SCHEDULE","enabled":true,"config":{}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"MARKDOWN"}}},"edges":{"intent":["knowledge"],"knowledge":["teach"],"teach":["practice"],"practice":["review"],"review":["output"]},"conditionalEdges":{}}', '{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"intent":{"x":100,"y":100},"knowledge":{"x":300,"y":100},"teach":{"x":500,"y":100},"practice":{"x":700,"y":100},"review":{"x":900,"y":100},"output":{"x":1100,"y":100}}}', 'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (2, 'knowledge-tutor', 'v1.0.0', 1, '初始版本', '1', '{"name":"knowledge-qa-graph","description":"知识问答工作流程","startNode":"intent","endNode":"output","nodes":{"intent":{"nodeName":"意图识别","nodeType":"INTENT","enabled":true,"config":{"category":"KNOWLEDGE"}},"retrieval":{"nodeName":"知识检索","nodeType":"RAG_RETRIEVAL","enabled":true,"config":{"topK":5,"similarityThreshold":0.7}},"enhancement":{"nodeName":"知识增强","nodeType":"RAG_ENHANCEMENT","enabled":true,"config":{"enhancementStrategy":"SUMMARIZATION"}},"llm":{"nodeName":"LLM生成","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"DEEPSEEK","modelName":"deepseek-chat","temperature":0.7}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"MARKDOWN"}}},"edges":{"intent":["retrieval"],"retrieval":["enhancement"],"enhancement":["llm"],"llm":["output"]},"conditionalEdges":{}}', '{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"intent":{"x":100,"y":100},"retrieval":{"x":300,"y":100},"enhancement":{"x":500,"y":100},"llm":{"x":700,"y":100},"output":{"x":900,"y":100}}}', 'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (3, 'gcx-assistant', 'v1.0.0', 1, '初始版本', '1', '{"name":"gcx-graph","description":"GCX助手工作流程","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入节点","nodeType":"DEFAULT","enabled":true},"llm":{"nodeName":"GCX回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["llm"],"llm":["output"]},"conditionalEdges":{}}', '{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"input":{"x":100,"y":100},"llm":{"x":300,"y":100},"output":{"x":500,"y":100}}}', 'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (4, 'simple-assistant', 'v1.0.0', 1, '初始版本', '1', '{"name":"simple-graph","description":"简单助手工作流程","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入节点","nodeType":"DEFAULT","enabled":true},"llm":{"nodeName":"LLM回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","defaultPrompt":"你是一个智能助手，请友好地回答用户的问题。"}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["llm"],"llm":["output"]},"conditionalEdges":{}}', '{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"input":{"x":100,"y":100},"llm":{"x":300,"y":100},"output":{"x":500,"y":100}}}', 'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (5, 'rag-knowledge-agent', 'v1.0.0', 1, '初始版本', '1', '{"name":"rag-qa-graph","description":"知识库问答工作流程","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入节点","nodeType":"DEFAULT","enabled":true},"rag_retrieval":{"nodeName":"知识库检索","nodeType":"RAG_RETRIEVAL","enabled":true,"config":{"topK":5,"similarityThreshold":0.6}},"rag_enhance":{"nodeName":"检索增强","nodeType":"RAG_ENHANCEMENT","enabled":true,"config":{"enhancementStrategy":"SUMMARIZATION","contextWindowSize":3,"addContext":true}},"llm":{"nodeName":"LLM回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","promptTemplate":"基于以下检索到的文档内容回答用户问题。\n\n{context}\n\n用户问题: {query}"}},"output":{"nodeName":"格式化输出","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["rag_retrieval"],"rag_retrieval":["rag_enhance"],"rag_enhance":["llm"],"llm":["output"]},"conditionalEdges":{}}', '{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"input":{"x":100,"y":100},"rag_retrieval":{"x":300,"y":100},"rag_enhance":{"x":500,"y":100},"llm":{"x":700,"y":100},"output":{"x":900,"y":100}}}', 'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (6, 'smart-agent', 'v1.0.0', 1, '初始版本', '1', '{"name":"smart-graph","description":"智能路由助手工作流程","startNode":"intent","endNode":"output","nodes":{"intent":{"nodeName":"意图识别","nodeType":"INTENT","enabled":true,"config":{"category":"general"}},"llm_chat":{"nodeName":"闲聊回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","defaultPrompt":"你是一个友好的 AI 助手，请用轻松自然的语气和用户聊天。"}},"rag_retrieval":{"nodeName":"知识库检索","nodeType":"RAG_RETRIEVAL","enabled":true,"config":{"topK":3}},"rag_enhance":{"nodeName":"检索增强","nodeType":"RAG_ENHANCEMENT","enabled":true,"config":{"enhancementStrategy":"SUMMARIZATION","contextWindowSize":3}},"rag_llm":{"nodeName":"RAG回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","promptTemplate":"基于以下检索到的文档回答用户问题。\n\n{context}\n\n用户问题: {query}"}},"tool_call_weather":{"nodeName":"天气查询工具","nodeType":"TOOL_CALL","enabled":true,"config":{"toolName":"WEATHER","enableCache":true}},"tool_call_calculator":{"nodeName":"计算器工具","nodeType":"TOOL_CALL","enabled":true,"config":{"toolName":"CALCULATOR","enableCache":true}},"tool_llm":{"nodeName":"工具结果回答","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","promptTemplate":"以下是工具执行结果，请用自然语言回复用户。\n\n工具结果: {toolResult}\n\n用户问题: {query}"}},"output":{"nodeName":"格式化输出","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"rag_retrieval":["rag_enhance"],"rag_enhance":["rag_llm"],"rag_llm":["output"],"llm_chat":["output"],"tool_call_weather":["tool_llm"],"tool_call_calculator":["tool_llm"],"tool_llm":["output"]},"conditionalEdges":{"intent":{"conditionType":"INTENT","defaultTarget":"llm_chat","nodeMappings":{"CHITCHAT":"llm_chat","QUESTION":"rag_retrieval","CALCULATOR":"tool_call_calculator","WEATHER":"tool_call_weather"}}}}', '{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"intent":{"x":100,"y":100},"llm_chat":{"x":300,"y":50},"rag_retrieval":{"x":300,"y":150},"rag_enhance":{"x":500,"y":150},"rag_llm":{"x":700,"y":150},"tool_call_weather":{"x":300,"y":250},"tool_call_calculator":{"x":300,"y":350},"tool_llm":{"x":500,"y":300},"output":{"x":900,"y":200}}}', 'system', 'system');

-- 意图定义
INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (1, 'default', 'CHITCHAT', '闲聊', 1, '日常闲聊对话', 'CONVERSATION', 10, 0.75, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (2, 'default', 'QUESTION', '问答', 1, '知识问答', 'KNOWLEDGE', 50, 0.80, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (3, 'default', 'TRANSLATION', '翻译', 1, '语言翻译', 'TASK', 60, 0.85, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (4, 'default', 'EDUCATION', '教育', 1, '学科教育辅导', 'TASK', 70, 0.80, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (5, 'default', 'CODE_HELP', '代码帮助', 1, '编程辅助与代码生成', 'TASK', 65, 0.80, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (6, 'default', 'WRITING_ASSISTANCE', '写作辅助', 1, '文章写作与润色', 'TASK', 55, 0.75, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (7, 'default', 'DATA_ANALYSIS', '数据分析', 1, '数据处理与分析', 'TASK', 60, 0.80, '1', 1, 'system', 'system');

INSERT IGNORE INTO `intent_def` (`id`, `agent_id`, `code`, `name`, `tenant_id`, `description`, `category`, `priority`, `confidence_threshold`, `enabled`, `status`, `create_by`, `update_by`)
VALUES (8, 'default', 'UNKNOWN', '未知意图', 1, '无法识别的意图', 'CONVERSATION', 0, 0.50, '1', 1, 'system', 'system');


-- ============================================
-- 教育域 Agent: practice（AI出题）
-- ============================================
INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (7, 'practice', 'AI出题助手', '根据知识点和难度智能生成练习题（使用硅基流动 Qwen3-8B 模型）', 1, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (7, 'practice', 'v1.0.0', 1, '初始版本', '1', '{"name":"practice-graph","description":"AI出题工作流程","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入节点","nodeType":"DEFAULT","enabled":true},"llm":{"nodeName":"AI出题","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","defaultPrompt":"你是一位经验丰富的 K12 出题教师，请根据知识点生成练习题。","promptTemplate":"你是一位经验丰富的 K12 出题教师。\n\n## 出题参数\n- 知识点ID: {knowledgeId}\n- 难度级别（1-4）: {difficulty}\n- 题目数量: {count}\n- 学生ID: {studentId}\n\n请生成 {count} 道难度为 {difficulty} 级的练习题。\n- 题目类型：选择题（60%）和填空题（40%）\n- 每行输出一个 JSON：{\"type\":\"CHOICE\",\"title\":\"题干\",\"options\":[\"A.\",\"B.\",\"C.\",\"D.\"],\"answer\":\"A\",\"analysis\":\"解析\",\"ability_dimension\":\"apply\"}\n- 仅输出 JSON 数据，用中文出题。\n"}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["llm"],"llm":["output"]},"conditionalEdges":{}}', '{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"input":{"x":100,"y":100},"llm":{"x":300,"y":100},"output":{"x":500,"y":100}}}', 'system', 'system');

-- ============================================
-- 教育域 Agent: exam（AI组卷）
-- ============================================
INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (11, 'exam', 'AI组卷助手', '根据知识点和考试要求智能组卷（使用硅基流动 Qwen3-8B 模型）', 1, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (11, 'exam', 'v1.0.0', 1, '初始版本', '1', '{"name":"exam-graph","description":"AI组卷工作流程","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入节点","nodeType":"DEFAULT","enabled":true},"llm":{"nodeName":"AI组卷","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","defaultPrompt":"请根据知识点和考试要求生成一份完整的试卷。","promptTemplate":"请根据以下参数生成一份完整的试卷。\n\n## 组卷参数\n- 知识点ID: {knowledgeId}\n- 学生ID: {studentId}\n- 难度级别: {difficulty}\n- 题目数量: {count}\n- 考试时长: {duration} 分钟\n\n请生成 {count} 道题目，包含选择题、填空题和解答题。"}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["llm"],"llm":["output"]},"conditionalEdges":{}}', '{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"input":{"x":100,"y":100},"llm":{"x":300,"y":100},"output":{"x":500,"y":100}}}', 'system', 'system');

-- ============================================
-- 教育域 Agent: teacher（AI讲解/教学）
-- ============================================
INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (8, 'teacher', 'AI讲解助手', '根据知识点进行智能教学讲解（使用硅基流动 Qwen3-8B 模型）', 1, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (8, 'teacher', 'v1.0.0', 1, '初始版本', '1', '{"name":"teacher-graph","description":"AI教学讲解工作流程","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入节点","nodeType":"DEFAULT","enabled":true},"llm":{"nodeName":"教学讲解","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","defaultPrompt":"你是一位耐心的 K12 学科教师，请根据知识点进行详细讲解。","promptTemplate":"你是一位耐心的 K12 学科教师。\n\n## 教学参数\n- 知识点ID: {knowledgeId}\n- 学生ID: {studentId}\n- 讲解风格: {style}\n\n请根据上述知识点进行详细、通俗易懂的讲解，包含概念解释、典型例题和易错点提醒。"}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["llm"],"llm":["output"]},"conditionalEdges":{}}', '{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"input":{"x":100,"y":100},"llm":{"x":300,"y":100},"output":{"x":500,"y":100}}}', 'system', 'system');

-- ============================================
-- 教育域 Agent: planner（学习规划）
-- ============================================
INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (9, 'planner', '学习规划助手', '根据知识点生成个性化学习规划（使用硅基流动 Qwen3-8B 模型）', 1, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (9, 'planner', 'v1.0.0', 1, '初始版本', '1', '{"name":"planner-graph","description":"学习规划工作流程","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入节点","nodeType":"DEFAULT","enabled":true},"llm":{"nodeName":"学习规划","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","defaultPrompt":"请根据知识点和学习目标生成个性化学习规划。","promptTemplate":"请根据以下信息生成个性化学习规划。\n\n## 规划参数\n- 知识点ID: {knowledgeId}\n- 学生ID: {studentId}\n- 目标日期: {targetDate}\n\n请制定从今天到目标日期的学习计划，每天的学习内容、练习安排和复习计划。"}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["llm"],"llm":["output"]},"conditionalEdges":{}}', '{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"input":{"x":100,"y":100},"llm":{"x":300,"y":100},"output":{"x":500,"y":100}}}', 'system', 'system');

-- ============================================
-- 教育域 Agent: report（学习报告）
-- ============================================
INSERT IGNORE INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `current_version`, `status`, `create_by`, `update_by`)
VALUES (10, 'report', '学习报告助手', '基于学习数据生成学习分析报告（使用硅基流动 Qwen3-8B 模型）', 1, 'v1.0.0', 1, 'system', 'system');

INSERT IGNORE INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `description`, `status`, `graph_config`, `canvas_config`, `create_by`, `update_by`)
VALUES (10, 'report', 'v1.0.0', 1, '初始版本', '1', '{"name":"report-graph","description":"学习报告工作流程","startNode":"input","endNode":"output","nodes":{"input":{"nodeName":"输入节点","nodeType":"DEFAULT","enabled":true},"llm":{"nodeName":"报告生成","nodeType":"LLM_CALL","enabled":true,"config":{"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B","defaultPrompt":"请根据学习数据生成综合学习分析报告。","promptTemplate":"请根据以下学习数据生成学习分析报告。\n\n## 报告参数\n- 学生ID: {studentId}\n- 报告周期: {period}\n\n请生成包含以下内容的学习报告：\n1. 学习概况总结\n2. 各知识点掌握度分析\n3. 薄弱环节识别\n4. 针对性提升建议"}},"output":{"nodeName":"输出格式化","nodeType":"OUTPUT_FORMAT","enabled":true,"config":{"outputFormat":"TEXT","prettyPrint":true}}},"edges":{"input":["llm"],"llm":["output"]},"conditionalEdges":{}}', '{"zoom":1,"offsetX":0,"offsetY":0,"nodePositions":{"input":{"x":100,"y":100},"llm":{"x":300,"y":100},"output":{"x":500,"y":100}}}', 'system', 'system');

