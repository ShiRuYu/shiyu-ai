-- Final system-ai seed baseline. Executed once on an empty H2 database.

INSERT INTO "PUBLIC"."MODEL_AI_PLATFORM" VALUES(1, 'OpenAI', 'OPENAI', 'OPENAI_COMPATIBLE', 1, 'https://api.openai.com/v1', '', 0.7, 4096, 3, '["gpt-4o","gpt-4o-mini","gpt-4-turbo","gpt-3.5-turbo"]', NULL, 'N', 1, 'OpenAI 官方 API', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_PLATFORM" VALUES(2, 'DeepSeek', 'DEEPSEEK', 'OPENAI_COMPATIBLE', 1, 'https://api.deepseek.com', '', 0.7, 4096, 3, '["deepseek-v4-flash","deepseek-reasoner"]', NULL, 'Y', 1, 'DeepSeek 官方 API', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_PLATFORM" VALUES(3, 'OpenRouter', 'OPENROUTER', 'OPENAI_COMPATIBLE', 1, 'https://openrouter.ai/api', '', 0.7, 4096, 3, '["x-ai/grok-4.1-fast","anthropic/claude-3.5-sonnet","google/gemini-2.5-pro"]', NULL, 'N', 1, 'OpenRouter 聚合 API', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_PLATFORM" VALUES(4, '硅基流动（通义千问）', 'SILICON_FLOW', 'OPENAI_COMPATIBLE', 1, 'https://api.siliconflow.cn', '', 0.7, 4096, 3, '["Qwen/Qwen3-14B","Qwen/Qwen3-8B"]', NULL, 'N', 1, '硅基流动（通义千问）平台 - 通义千问 Qwen 模型服务', NULL, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_MODEL" VALUES(1, 1, 'gpt-4o', 1, 'GPT-4o', NULL, NULL, 'N', 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_MODEL" VALUES(2, 1, 'gpt-4o-mini', 1, 'GPT-4o Mini', NULL, NULL, 'Y', 1, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_MODEL" VALUES(3, 1, 'gpt-3.5-turbo', 1, 'GPT-3.5 Turbo', NULL, NULL, 'N', 1, 3, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_MODEL" VALUES(4, 2, 'deepseek-v4-flash', 1, 'DeepSeek V4 Flash', NULL, NULL, 'Y', 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_MODEL" VALUES(5, 2, 'deepseek-reasoner', 1, 'DeepSeek Reasoner', NULL, NULL, 'N', 1, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_MODEL" VALUES(6, 3, 'x-ai/grok-4.1-fast', 1, 'Grok 4.1 Fast', NULL, NULL, 'N', 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_MODEL" VALUES(7, 3, 'anthropic/claude-3.5-sonnet', 1, 'Claude 3.5 Sonnet', NULL, NULL, 'Y', 1, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_MODEL" VALUES(8, 4, 'Qwen/Qwen3-14B', 1, 'Qwen3 14B', NULL, NULL, 'N', 1, 1, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
INSERT INTO "PUBLIC"."MODEL_AI_MODEL" VALUES(9, 4, 'Qwen/Qwen3-8B', 1, 'Qwen3 8B', NULL, NULL, 'Y', 1, 2, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 0);
