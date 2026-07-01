-- ============================================
-- Data: data_agent
-- ============================================

INSERT INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `workspace_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `extra_config`, `is_default`, `status`, `remark`)
VALUES (1, 'OpenAI', 'OPENAI', 1, 0, 'https://api.openai.com/v1', '', 0.7, 4096, 3,
        '["gpt-4o","gpt-4o-mini","gpt-4-turbo","gpt-3.5-turbo"]',
        NULL, 'N', '1', 'OpenAI 官方 API');

INSERT INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `workspace_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `extra_config`, `is_default`, `status`, `remark`)
VALUES (2, 'DeepSeek', 'DEEPSEEK', 1, 0, 'https://api.deepseek.com', '', 0.7, 4096, 3,
        '["deepseek-chat","deepseek-reasoner"]',
        NULL, 'Y', '1', 'DeepSeek 官方 API');

INSERT INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `workspace_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `extra_config`, `is_default`, `status`, `remark`)
VALUES (3, 'OpenRouter', 'OPENROUTER', 1, 0, 'https://openrouter.ai/api', '', 0.7, 4096, 3,
        '["x-ai/grok-4.1-fast","anthropic/claude-3.5-sonnet","google/gemini-2.5-pro"]',
        NULL, 'N', '1', 'OpenRouter 聚合 API');

INSERT INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `workspace_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `extra_config`, `is_default`, `status`, `remark`)
VALUES (4, 'SiliconFlow', 'SILICON_FLOW', 1, 0, 'https://api.siliconflow.cn', '', 0.7, 4096, 3,
        '["THUDM/GLM-Z1-9B-0414","deepseek-ai/DeepSeek-V3"]',
        NULL, 'N', '1', 'SiliconFlow 推理平台');

INSERT INTO `ai_platform` (`id`, `name`, `code`, `tenant_id`, `workspace_id`, `base_url`, `api_key`, `temperature`, `max_tokens`, `max_retries`, `available_models`, `extra_config`, `is_default`, `status`, `remark`)
VALUES (5, 'Ollama', 'OLLAMA', 1, 0, 'http://localhost:11434', '', 0.7, 4096, 3,
        '["gemma3:4b","llama3.1:8b","qwen2.5:7b"]',
        NULL, 'N', '1', '本地 Ollama 推理');

INSERT INTO `ai_model` (`platform_id`, `model_name`, `tenant_id`, `workspace_id`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (1, 'gpt-4o', 1, 0, 'GPT-4o', 'OpenAI GPT-4o 多模态模型', 'Y', '1', 1);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `tenant_id`, `workspace_id`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (1, 'gpt-4o-mini', 1, 0, 'GPT-4o Mini', 'OpenAI GPT-4o Mini 轻量模型', 'N', '1', 2);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `tenant_id`, `workspace_id`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (2, 'deepseek-chat', 1, 0, 'DeepSeek Chat', 'DeepSeek 对话模型', 'Y', '1', 1);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `tenant_id`, `workspace_id`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (2, 'deepseek-reasoner', 1, 0, 'DeepSeek Reasoner', 'DeepSeek 推理模型', 'N', '1', 2);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `tenant_id`, `workspace_id`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (3, 'x-ai/grok-4.1-fast', 1, 0, 'Grok 4.1 Fast', 'xAI Grok 快速模型', 'Y', '1', 1);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `tenant_id`, `workspace_id`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (4, 'THUDM/GLM-Z1-9B-0414', 1, 0, 'GLM-Z1-9B', '智谱 GLM-Z1 9B 模型', 'Y', '1', 1);

INSERT INTO `ai_model` (`platform_id`, `model_name`, `tenant_id`, `workspace_id`, `display_name`, `description`, `is_default`, `status`, `sort`)
VALUES (5, 'gemma3:4b', 1, 0, 'Gemma 3 4B', 'Google Gemma 3 4B 本地模型', 'Y', '1', 1);

INSERT INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `ext_info`)
VALUES (1, 'simple-assistant', '简单助手', '基础 LLM 问答助手，直接调用大模型回答用户问题', 1, 0, 'v1.0.0', '1',
'{"requiredInputs": [{"name": "query", "type": "string", "source": "API_REQUEST", "required": false, "description": "[llm] 用户提问/输入文本", "defaultValue": ""}, {"name": "platform", "type": "string", "source": "CONFIG_VALUE", "required": false, "description": "[llm] AI 平台编码", "defaultValue": null}, {"name": "modelName", "type": "string", "source": "CONFIG_VALUE", "required": false, "description": "[llm] 模型名称", "defaultValue": null}]}');

INSERT INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `ext_info`)
VALUES (1, 'simple-assistant', 'v1.0.0', 1, 0, '初始版本', 'PUBLISHED',
'{"name": "simple-assistant_graph", "description": "基础LLM问答", "startNode": "llm", "endNode": "llm", "nodes": {"llm": {"nodeName": "LLM 回答", "nodeType": "LLM_CALL", "enabled": true, "timeout": 30000, "retryCount": 0, "errorStrategy": "THROW", "config": {"platform": "SILICON_FLOW", "modelName": "THUDM/GLM-Z1-9B-0414", "temperature": 0.7, "maxTokens": 4096, "stream": false, "defaultPrompt": "你是一个智能助手，请友好地回答用户的问题。"}}}, "edges": {}, "conditionalEdges": {}}',
NULL,
'{"requiredInputs": [{"name": "query", "type": "string", "source": "API_REQUEST", "required": false, "description": "[llm] 用户提问/输入文本", "defaultValue": ""}, {"name": "platform", "type": "string", "source": "CONFIG_VALUE", "required": false, "description": "[llm] AI 平台编码", "defaultValue": null}, {"name": "modelName", "type": "string", "source": "CONFIG_VALUE", "required": false, "description": "[llm] 模型名称", "defaultValue": null}]}');

INSERT INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `ext_info`)
VALUES (2, 'rag-knowledge-agent', '知识库问答', '基于文档知识库的 RAG 检索问答', 1, 0, 'v1.0.0', '1',
'{"requiredInputs": [{"name": "query", "type": "string", "source": "API_REQUEST", "required": true, "description": "[rag_retrieval] 检索查询文本", "defaultValue": null}, {"name": "knowledgeBaseId", "type": "string", "source": "CONFIG_VALUE", "required": false, "description": "[rag_retrieval] 知识库 ID", "defaultValue": null}, {"name": "topK", "type": "number", "source": "CONFIG_VALUE", "required": false, "description": "[rag_retrieval] 最大检索结果数", "defaultValue": null}, {"name": "similarityThreshold", "type": "number", "source": "CONFIG_VALUE", "required": false, "description": "[rag_retrieval] 相似度阈值", "defaultValue": null}]}');

INSERT INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `ext_info`)
VALUES (2, 'rag-knowledge-agent', 'v1.0.0', 1, 0, '初始版本', 'PUBLISHED',
'{"name": "rag-knowledge-agent_graph", "description": "RAG知识库检索问答", "startNode": "input", "endNode": "output", "nodes": {"input": {"nodeName": "输入", "nodeType": "DEFAULT", "enabled": true, "timeout": 30000, "config": {}}, "rag_retrieval": {"nodeName": "知识库检索", "nodeType": "RAG_RETRIEVAL", "enabled": true, "timeout": 30000, "config": {"topK": 5, "similarityThreshold": 0.6}}, "rag_enhance": {"nodeName": "检索增强", "nodeType": "RAG_ENHANCEMENT", "enabled": true, "timeout": 30000, "config": {"enhancementStrategy": "SUMMARIZATION", "contextWindowSize": 3, "maxLength": 2000, "addContext": true}}, "llm": {"nodeName": "LLM 回答", "nodeType": "LLM_CALL", "enabled": true, "timeout": 30000, "retryCount": 0, "errorStrategy": "THROW", "config": {"platform": "SILICON_FLOW", "modelName": "THUDM/GLM-Z1-9B-0414", "temperature": 0.7, "maxTokens": 4096, "stream": false, "promptTemplate": "基于以下检索到的文档内容回答用户问题。\n\n{context}\n\n用户问题: {query}"}}, "output": {"nodeName": "格式化输出", "nodeType": "OUTPUT_FORMAT", "enabled": true, "timeout": 30000, "config": {"outputFormat": "TEXT", "prettyPrint": true}}}, "edges": {"input": ["rag_retrieval"], "rag_retrieval": ["rag_enhance"], "rag_enhance": ["llm"], "llm": ["output"]}, "conditionalEdges": {}}',
NULL,
'{"requiredInputs": [{"name": "query", "type": "string", "source": "API_REQUEST", "required": true, "description": "[rag_retrieval] 检索查询文本", "defaultValue": null}, {"name": "knowledgeBaseId", "type": "string", "source": "CONFIG_VALUE", "required": false, "description": "[rag_retrieval] 知识库 ID", "defaultValue": null}, {"name": "topK", "type": "number", "source": "CONFIG_VALUE", "required": false, "description": "[rag_retrieval] 最大检索结果数", "defaultValue": null}, {"name": "similarityThreshold", "type": "number", "source": "CONFIG_VALUE", "required": false, "description": "[rag_retrieval] 相似度阈值", "defaultValue": null}]}');

INSERT INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `ext_info`)
VALUES (3, 'smart-agent', '智能路由助手', '支持意图识别、RAG 知识检索、工具调用、闲聊的全功能智能助手', 1, 0, 'v1.0.0', '1',
'{"requiredInputs": [{"name": "query", "type": "string", "source": "API_REQUEST", "required": true, "description": "[intent] 用户输入文本", "defaultValue": null}, {"name": "category", "type": "string", "source": "CONFIG_VALUE", "required": false, "description": "[intent] 意图分类", "defaultValue": null}, {"name": "confidenceThreshold", "type": "number", "source": "CONFIG_VALUE", "required": false, "description": "[intent] 置信度阈值", "defaultValue": null}]}');

INSERT INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `ext_info`)
VALUES (3, 'smart-agent', 'v1.0.0', 1, 0, '初始版本', 'PUBLISHED',
'{"name": "smart-agent_graph", "description": "全功能智能路由助手", "startNode": "intent", "endNode": "output", "nodes": {"intent": {"nodeName": "意图识别", "nodeType": "INTENT", "enabled": true, "timeout": 30000, "config": {"category": "CONVERSATION", "confidenceThreshold": 0.75}}, "llm_chat": {"nodeName": "闲聊回答", "nodeType": "LLM_CALL", "enabled": true, "timeout": 30000, "retryCount": 0, "errorStrategy": "THROW", "config": {"platform": "SILICON_FLOW", "modelName": "THUDM/GLM-Z1-9B-0414", "temperature": 0.8, "maxTokens": 4096, "stream": false, "defaultPrompt": "你是一个友好的 AI 助手，请用轻松自然的语气和用户聊天。"}}, "rag_retrieval": {"nodeName": "知识库检索", "nodeType": "RAG_RETRIEVAL", "enabled": true, "timeout": 30000, "config": {"topK": 3}}, "rag_enhance": {"nodeName": "检索增强", "nodeType": "RAG_ENHANCEMENT", "enabled": true, "timeout": 30000, "config": {"enhancementStrategy": "SUMMARIZATION", "contextWindowSize": 3}}, "rag_llm": {"nodeName": "RAG 回答", "nodeType": "LLM_CALL", "enabled": true, "timeout": 30000, "retryCount": 0, "errorStrategy": "THROW", "config": {"platform": "SILICON_FLOW", "modelName": "THUDM/GLM-Z1-9B-0414", "temperature": 0.7, "maxTokens": 4096, "stream": false, "promptTemplate": "基于以下检索到的文档回答用户问题。\n\n{context}\n\n用户问题: {query}"}}, "tool_call_weather": {"nodeName": "天气查询工具", "nodeType": "TOOL_CALL", "enabled": true, "timeout": 30000, "config": {"toolName": "WEATHER", "enableCache": true}}, "tool_call_calculator": {"nodeName": "计算器工具", "nodeType": "TOOL_CALL", "enabled": true, "timeout": 30000, "config": {"toolName": "CALCULATOR", "enableCache": true}}, "tool_llm": {"nodeName": "工具结果回答", "nodeType": "LLM_CALL", "enabled": true, "timeout": 30000, "retryCount": 0, "errorStrategy": "THROW", "config": {"platform": "SILICON_FLOW", "modelName": "THUDM/GLM-Z1-9B-0414", "temperature": 0.7, "maxTokens": 4096, "stream": false, "promptTemplate": "以下是工具执行结果，请用自然语言回复用户。\n\n工具结果: {toolResult}\n\n用户问题: {query}"}}, "output": {"nodeName": "格式化输出", "nodeType": "OUTPUT_FORMAT", "enabled": true, "timeout": 30000, "config": {"outputFormat": "TEXT", "prettyPrint": true}}}, "edges": {"rag_retrieval": ["rag_enhance"], "rag_enhance": ["rag_llm"], "rag_llm": ["output"], "llm_chat": ["output"], "tool_call_weather": ["tool_llm"], "tool_call_calculator": ["tool_llm"], "tool_llm": ["output"]}, "conditionalEdges": {"intent": {"defaultTarget": "llm_chat", "nodeMappings": {"CHITCHAT": "llm_chat", "QUESTION": "rag_retrieval", "CALCULATOR": "tool_call_calculator", "WEATHER": "tool_call_weather", "UNKNOWN": "llm_chat"}, "conditionType": "INTENT_ROUTING"}}}',
NULL,
'{"requiredInputs": [{"name": "query", "type": "string", "source": "API_REQUEST", "required": true, "description": "[intent] 用户输入文本", "defaultValue": null}, {"name": "category", "type": "string", "source": "CONFIG_VALUE", "required": false, "description": "[intent] 意图分类", "defaultValue": null}, {"name": "confidenceThreshold", "type": "number", "source": "CONFIG_VALUE", "required": false, "description": "[intent] 置信度阈值", "defaultValue": null}]}');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('default', 'CHITCHAT', '闲聊', 1, 0, '处理用户的日常闲聊对话', 'CONVERSATION', 50, 0.75, '["你好","最近怎么样","今天天气不错","你在干什么","聊聊天吧"]', '0', '_fallback');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('default', 'QUESTION', '问答', 1, 0, '处理用户的知识性问题', 'KNOWLEDGE', 60, 0.8, '["什么是人工智能","为什么天空是蓝色的","如何学习编程","地球有多大","谁发明了电灯"]', '0', '_fallback');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('default', 'CALCULATOR', '计算器', 1, 0, '执行基础的数学运算（加、减、乘、除）', 'TASK', 70, 0.85, '["帮我订一张机票","设置一个明天早上的闹钟","发送邮件给张三","创建一个待办事项","预约明天的会议"]', '1', '_fallback');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('default', 'QUERY', '查询', 1, 0, '处理数据或信息查询请求', 'SEARCH', 65, 0.8, '["查询我的订单","看看今天的新闻","搜索相关的文章","查找联系人信息","查看账户余额"]', '0', '_fallback');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `slots`, `require_slot_filling`, `target_node`)
VALUES ('default', 'CODE_HELP', '代码帮助', 1, 0, '处理编程相关的技术问题', 'TECHNICAL', 75, 0.85, '["这段代码有什么问题","如何优化这个算法","解释一下这个函数","帮我写一个排序方法","这个错误怎么解决"]', '{"language":"编程语言","codeSnippet":"代码片段"}', '0', '_fallback');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('smart-agent', 'CHITCHAT', '闲聊', 1, 0, '处理用户的日常闲聊对话', 'CONVERSATION', 50, 0.75, '["你好","最近怎么样","今天天气不错","你在干什么","聊聊天吧"]', '0', 'llm_chat');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('smart-agent', 'QUESTION', '知识查询', 1, 0, '查询知识库信息', 'CONVERSATION', 60, 0.8, '["什么是RAG","Shiyu AI 是什么"]', '0', 'rag_retrieval');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `slots`, `target_node`)
VALUES ('smart-agent', 'CALCULATOR', '计算器', 1, 0, '执行基础的数学运算', 'CONVERSATION', 70, 0.85, '["计算 1+2*3","计算 100/5"]', '1', '{"expression":"数学表达式"}', 'tool_call_calculator');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `slots`, `parameter_mapping`, `slot_defaults`, `target_node`)
VALUES ('smart-agent', 'WEATHER', '天气查询', 1, 0, '查询指定城市的当前天气信息', 'CONVERSATION', 65, 0.85, '["北京天气怎么样","上海今天冷吗"]', '1', '{"city":"城市名称","date":"日期（可选）"}', '{"city":"location"}', '{"unit":"celsius"}', 'tool_call_weather');

INSERT INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `ext_info`)
VALUES (4, 'tutor-bot', 'AI 教育辅导助手', 'K12 全科 AI 辅导 Agent，支持知识点讲解、智能出题、学习分析、复习安排，依据 Bloom 能力模型因材施教', 1, 0, 'v1.0.0', '1',
'{"requiredInputs": [{"name": "studentId", "type": "number", "source": "API_REQUEST", "required": true, "description": "[abilityQuery] 学生 ID", "defaultValue": null}, {"name": "knowledgeId", "type": "number", "source": "API_REQUEST", "required": true, "description": "[abilityQuery] 知识点 ID", "defaultValue": null}, {"name": "practiceCount", "type": "number", "source": "API_REQUEST", "required": false, "description": "[practice] 题目数量（默认5）", "defaultValue": 5}]}');

INSERT INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `ext_info`)
VALUES (4, 'tutor-bot', 'v1.0.0', 1, 0, '初始版本 - 完整辅导流程', 'PUBLISHED',
'{"name": "tutor-graph", "description": "AI Tutor完整辅导流程：查能力值→讲解→出题→评分→复习安排", "startNode": "abilityQuery", "endNode": "reviewSchedule", "nodes": {"abilityQuery": {"nodeName": "能力值查询", "nodeType": "ABILITY_QUERY", "enabled": true, "timeout": 10000, "retryCount": 0, "errorStrategy": "THROW", "config": {}}, "teach": {"nodeName": "AI知识点讲解", "nodeType": "EDUCATION_TEACH", "enabled": true, "timeout": 60000, "retryCount": 1, "retryInterval": 2000, "errorStrategy": "THROW", "config": {"platform": "SILICON_FLOW", "modelName": "THUDM/GLM-Z1-9B-0414", "temperature": 0.7, "maxTokens": 2048, "stream": false, "defaultPrompt": "你是一位经验丰富的K12教师，请根据学生的当前水平和知识背景详细讲解知识点。回答要求：1）先用通俗语言解释核心概念 2）结合前置知识建立关联 3）给出1-2个生活化的例子 4）总结重点。"}}, "practice": {"nodeName": "智能出题练习", "nodeType": "EDUCATION_PRACTICE", "enabled": true, "timeout": 60000, "retryCount": 1, "retryInterval": 2000, "errorStrategy": "THROW", "config": {"platform": "SILICON_FLOW", "modelName": "THUDM/GLM-Z1-9B-0414", "temperature": 0.8, "maxTokens": 4096, "stream": false, "defaultPrompt": "你是一位K12出题教师，请根据知识点生成练习题。要求：1）选择题和填空题各占一半 2）给出答案和详细解析 3）每题标注能力维度(remember/understand/apply/analyze) 4）用JSON格式输出，每行一个题目对象。"}}, "scoreAnalysis": {"nodeName": "评分分析", "nodeType": "SCORE_ANALYSIS", "enabled": true, "timeout": 5000, "retryCount": 0, "errorStrategy": "DEFAULT", "config": {}}, "reviewSchedule": {"nodeName": "复习安排", "nodeType": "REVIEW_SCHEDULE", "enabled": true, "timeout": 5000, "retryCount": 0, "errorStrategy": "DEFAULT", "config": {}}}, "edges": {"abilityQuery": ["teach"], "teach": ["practice"], "practice": ["scoreAnalysis"]}, "conditionalEdges": {"scoreAnalysis": {"defaultTarget": "reviewSchedule", "nodeMappings": {"pass": "reviewSchedule", "retry": "teach"}, "conditionType": "SCORE_ROUTING"}}}',
NULL,
'{"requiredInputs": [{"name": "studentId", "type": "number", "source": "API_REQUEST", "required": true, "description": "[abilityQuery] 学生 ID", "defaultValue": null}, {"name": "knowledgeId", "type": "number", "source": "API_REQUEST", "required": true, "description": "[abilityQuery] 知识点 ID", "defaultValue": null}, {"name": "practiceCount", "type": "number", "source": "API_REQUEST", "required": false, "description": "[practice] 题目数量（默认5）", "defaultValue": 5}]}');

INSERT INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `ext_info`)
VALUES (5, 'math-practice-bot', '数学练习助手', '专注于数学题目生成与练习，支持自动适配难度、错题针对性训练、知识点强化', 1, 0, 'v1.0.0', '1',
'{"requiredInputs": [{"name": "studentId", "type": "number", "source": "API_REQUEST", "required": true, "description": "[abilityQuery] 学生 ID", "defaultValue": null}, {"name": "knowledgeId", "type": "number", "source": "API_REQUEST", "required": true, "description": "[abilityQuery] 知识点 ID", "defaultValue": null}, {"name": "practiceCount", "type": "number", "source": "API_REQUEST", "required": false, "description": "[practice] 题目数量（默认5）", "defaultValue": 5}]}');

INSERT INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `ext_info`)
VALUES (5, 'math-practice-bot', 'v1.0.0', 1, 0, '初始版本 - 智能出题练习', 'PUBLISHED',
'{"name": "math-practice-graph", "description": "数学智能出题：查能力→出题→评分", "startNode": "abilityQuery", "endNode": "scoreAnalysis", "nodes": {"abilityQuery": {"nodeName": "能力值查询", "nodeType": "ABILITY_QUERY", "enabled": true, "timeout": 10000, "config": {}}, "practice": {"nodeName": "智能出题", "nodeType": "EDUCATION_PRACTICE", "enabled": true, "timeout": 60000, "retryCount": 1, "retryInterval": 2000, "errorStrategy": "THROW", "config": {"platform": "SILICON_FLOW", "modelName": "THUDM/GLM-Z1-9B-0414", "temperature": 0.8, "maxTokens": 4096, "stream": false, "defaultPrompt": "你是一位K12数学出题教师，根据学生水平生成合适的练习题。要求：1）适配学生能力水平调整难度 2）给出答案和详细解析 3）选择题和填空题混合 4）用JSON格式输出。"}}, "scoreAnalysis": {"nodeName": "评分分析", "nodeType": "SCORE_ANALYSIS", "enabled": true, "timeout": 5000, "config": {}}}, "edges": {"abilityQuery": ["practice"], "practice": ["scoreAnalysis"]}, "conditionalEdges": {}}',
NULL,
'{"requiredInputs": [{"name": "studentId", "type": "number", "source": "API_REQUEST", "required": true, "description": "[abilityQuery] 学生 ID", "defaultValue": null}, {"name": "knowledgeId", "type": "number", "source": "API_REQUEST", "required": true, "description": "[abilityQuery] 知识点 ID", "defaultValue": null}, {"name": "practiceCount", "type": "number", "source": "API_REQUEST", "required": false, "description": "[practice] 题目数量（默认5）", "defaultValue": 5}]}');

INSERT INTO `agent_def` (`id`, `agent_id`, `name`, `description`, `tenant_id`, `workspace_id`, `current_version`, `status`, `ext_info`)
VALUES (6, 'knowledge-tutor', '知识点讲解助手', '多学科知识讲解 Agent，覆盖数学/物理/英语/化学，支持前置知识检测、个性化教学', 1, 0, 'v1.0.0', '1',
'{"requiredInputs": [{"name": "knowledgeId", "type": "number", "source": "API_REQUEST", "required": true, "description": "[prereqCheck] 知识点 ID", "defaultValue": null}, {"name": "studentId", "type": "number", "source": "API_REQUEST", "required": false, "description": "[abilityQuery] 学生 ID（可选）", "defaultValue": null}]}');

INSERT INTO `agent_version` (`id`, `agent_id`, `version_number`, `tenant_id`, `workspace_id`, `description`, `status`, `graph_config`, `canvas_config`, `ext_info`)
VALUES (6, 'knowledge-tutor', 'v1.0.0', 1, 0, '初始版本 - 知识点讲解', 'PUBLISHED',
'{"name": "knowledge-teach-graph", "description": "知识点讲解：检测前置→查能力→AI讲解", "startNode": "prereqCheck", "endNode": "teach", "nodes": {"prereqCheck": {"nodeName": "前置知识检测", "nodeType": "PREREQ_CHECK", "enabled": true, "timeout": 10000, "config": {}}, "abilityQuery": {"nodeName": "能力值查询", "nodeType": "ABILITY_QUERY", "enabled": true, "timeout": 10000, "config": {}}, "teach": {"nodeName": "AI讲解", "nodeType": "EDUCATION_TEACH", "enabled": true, "timeout": 60000, "retryCount": 1, "retryInterval": 2000, "errorStrategy": "THROW", "config": {"platform": "SILICON_FLOW", "modelName": "THUDM/GLM-Z1-9B-0414", "temperature": 0.7, "maxTokens": 2048, "stream": false, "defaultPrompt": "你是一位经验丰富的K12教师，请用通俗易懂的语言讲解知识点。要求：1）先讲清楚是什么 2）再讲为什么 3）举例说明怎么用 4）总结要点。"}}}, "edges": {"prereqCheck": ["abilityQuery"], "abilityQuery": ["teach"]}, "conditionalEdges": {}}',
NULL,
'{"requiredInputs": [{"name": "knowledgeId", "type": "number", "source": "API_REQUEST", "required": true, "description": "[prereqCheck] 知识点 ID", "defaultValue": null}, {"name": "studentId", "type": "number", "source": "API_REQUEST", "required": false, "description": "[abilityQuery] 学生 ID（可选）", "defaultValue": null}]}');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('tutor-bot', 'TEACH', '知识点讲解', 1, 0,
 '学生请求讲解某个知识点', 'EDUCATION', 90, 0.75,
 '["给我讲讲绝对值","什么是函数","解释一下二次函数","讲解有理数的概念","能讲一下相反数吗"]', '0', 'teach');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('tutor-bot', 'PRACTICE', '练习题', 1, 0,
 '学生请求生成练习题', 'EDUCATION', 85, 0.7,
 '["出几道绝对值题","给我练习题做","我要练习相反数","出题","做点题目"]', '0', 'practice');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('tutor-bot', 'FULL_STUDY', '完整学习', 1, 0,
 '学生请求完整的学习流程（讲解+练习+复习）', 'EDUCATION', 95, 0.8,
 '["我要学习绝对值","帮我学会二次函数","我想学好这个知识点","带我学习有理数","帮我掌握相反数"]', '0', 'abilityQuery');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('tutor-bot', 'REVIEW', '复习回顾', 1, 0,
 '学生请求复习任务', 'EDUCATION', 80, 0.75,
 '["今天要复习什么","我的复习任务","安排复习","查看复习计划","有哪些复习要做"]', '0', 'reviewSchedule');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `slots`, `require_slot_filling`, `target_node`)
VALUES ('tutor-bot', 'REPORT', '学习报告', 1, 0,
 '学生请求查看学习报告或学习分析', 'EDUCATION', 75, 0.7,
 '["我的学习情况怎么样","学习报告","看看我的进步","分析一下我的学习","学习总结"]', '{"period":"时间范围(可选)"}', '0', 'report');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('math-practice-bot', 'GENERATE_QUESTIONS', '生成练习题', 1, 0,
 '生成数学练习题请求', 'EDUCATION', 90, 0.7,
 '["出题","给我练习题","练习","做题","来几道题"]', '0', 'practice');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `slots`, `require_slot_filling`, `target_node`)
VALUES ('math-practice-bot', 'SPECIFIC_TOPIC', '按知识点出题', 1, 0,
 '按指定知识点生成题目', 'EDUCATION', 85, 0.75,
 '["出绝对值的题","我要练习数轴题目","相反数的练习题","有理数运算练习","出些函数题"]', '{"knowledge":"知识点名称","count":"题目数量"}', '1', 'practice');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('knowledge-tutor', 'EXPLAIN', '知识点讲解', 1, 0,
 '学生请求解释某个知识概念', 'EDUCATION', 90, 0.75,
 '["什么是...","讲解一下...","能解释...","告诉我...","...是什么意思"]', '0', 'teach');

INSERT INTO `intent_def` (`agent_id`, `code`, `name`, `tenant_id`, `workspace_id`, `description`, `category`, `priority`, `confidence_threshold`, `examples`, `require_slot_filling`, `target_node`)
VALUES ('knowledge-tutor', 'PREREQ_CHECK', '前置知识查询', 1, 0,
 '查询某个知识点需要哪些前置知识', 'EDUCATION', 70, 0.7,
 '["学这个需要先会什么","前置知识有哪些","学函数要先学什么","需要什么基础"]', '0', 'prereqCheck');

