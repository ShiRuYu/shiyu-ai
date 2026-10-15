-- 备份数据库后，在现有 MySQL 数据库执行一次。
-- 将旧系统 Agent 切换到 DeepSeek，同时保留图中的其他改动。

UPDATE agent_version
SET graph_config = REPLACE(graph_config, '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE id = 3 AND agent_id = 'gcx-assistant' AND version_number = 'v1.0.0'
  AND tenant_id = 1 AND create_by = 'system'
  AND graph_config LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE agent_version
SET graph_config = REPLACE(graph_config, '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE id = 4 AND agent_id = 'simple-assistant' AND version_number = 'v1.0.0'
  AND tenant_id = 1 AND create_by = 'system'
  AND graph_config LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE agent_version
SET graph_config = REPLACE(graph_config, '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE id = 5 AND agent_id = 'rag-knowledge-agent' AND version_number = 'v1.0.0'
  AND tenant_id = 1 AND create_by = 'system'
  AND graph_config LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE agent_version
SET graph_config = REPLACE(graph_config, '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE id = 6 AND agent_id = 'smart-agent' AND version_number = 'v1.0.0'
  AND tenant_id = 1 AND create_by = 'system'
  AND graph_config LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE agent_version
SET graph_config = REPLACE(graph_config, '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE id = 7 AND agent_id = 'practice' AND version_number = 'v1.0.0'
  AND tenant_id = 1 AND create_by = 'system'
  AND graph_config LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE agent_version
SET graph_config = REPLACE(graph_config, '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE id = 8 AND agent_id = 'teacher' AND version_number = 'v1.0.0'
  AND tenant_id = 1 AND create_by = 'system'
  AND graph_config LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE agent_version
SET graph_config = REPLACE(graph_config, '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE id = 9 AND agent_id = 'planner' AND version_number = 'v1.0.0'
  AND tenant_id = 1 AND create_by = 'system'
  AND graph_config LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE agent_version
SET graph_config = REPLACE(graph_config, '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE id = 10 AND agent_id = 'report' AND version_number = 'v1.0.0'
  AND tenant_id = 1 AND create_by = 'system'
  AND graph_config LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE agent_version
SET graph_config = REPLACE(graph_config, '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE id = 11 AND agent_id = 'exam' AND version_number = 'v1.0.0'
  AND tenant_id = 1 AND create_by = 'system'
  AND graph_config LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE agent_def
SET description = REPLACE(description, '使用硅基流动 Qwen3-8B 模型', '使用 DeepSeek deepseek-flash 模型')
WHERE id BETWEEN 1 AND 11 AND create_by = 'system'
  AND description LIKE '%使用硅基流动 Qwen3-8B 模型%';
