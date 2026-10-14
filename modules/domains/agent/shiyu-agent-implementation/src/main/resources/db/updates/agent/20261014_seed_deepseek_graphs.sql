-- Route legacy system agents to DeepSeek while preserving other graph changes.

UPDATE "PUBLIC"."AGENT_VERSION"
SET "GRAPH_CONFIG" = REPLACE("GRAPH_CONFIG", '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE "ID" = 3 AND "AGENT_ID" = 'gcx-assistant' AND "VERSION_NUMBER" = 'v1.0.0'
  AND "TENANT_ID" = 1 AND "CREATE_BY" = 'system'
  AND "GRAPH_CONFIG" LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE "PUBLIC"."AGENT_VERSION"
SET "GRAPH_CONFIG" = REPLACE("GRAPH_CONFIG", '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE "ID" = 4 AND "AGENT_ID" = 'simple-assistant' AND "VERSION_NUMBER" = 'v1.0.0'
  AND "TENANT_ID" = 1 AND "CREATE_BY" = 'system'
  AND "GRAPH_CONFIG" LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE "PUBLIC"."AGENT_VERSION"
SET "GRAPH_CONFIG" = REPLACE("GRAPH_CONFIG", '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE "ID" = 5 AND "AGENT_ID" = 'rag-knowledge-agent' AND "VERSION_NUMBER" = 'v1.0.0'
  AND "TENANT_ID" = 1 AND "CREATE_BY" = 'system'
  AND "GRAPH_CONFIG" LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE "PUBLIC"."AGENT_VERSION"
SET "GRAPH_CONFIG" = REPLACE("GRAPH_CONFIG", '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE "ID" = 6 AND "AGENT_ID" = 'smart-agent' AND "VERSION_NUMBER" = 'v1.0.0'
  AND "TENANT_ID" = 1 AND "CREATE_BY" = 'system'
  AND "GRAPH_CONFIG" LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE "PUBLIC"."AGENT_VERSION"
SET "GRAPH_CONFIG" = REPLACE("GRAPH_CONFIG", '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE "ID" = 7 AND "AGENT_ID" = 'practice' AND "VERSION_NUMBER" = 'v1.0.0'
  AND "TENANT_ID" = 1 AND "CREATE_BY" = 'system'
  AND "GRAPH_CONFIG" LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE "PUBLIC"."AGENT_VERSION"
SET "GRAPH_CONFIG" = REPLACE("GRAPH_CONFIG", '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE "ID" = 8 AND "AGENT_ID" = 'teacher' AND "VERSION_NUMBER" = 'v1.0.0'
  AND "TENANT_ID" = 1 AND "CREATE_BY" = 'system'
  AND "GRAPH_CONFIG" LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE "PUBLIC"."AGENT_VERSION"
SET "GRAPH_CONFIG" = REPLACE("GRAPH_CONFIG", '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE "ID" = 9 AND "AGENT_ID" = 'planner' AND "VERSION_NUMBER" = 'v1.0.0'
  AND "TENANT_ID" = 1 AND "CREATE_BY" = 'system'
  AND "GRAPH_CONFIG" LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE "PUBLIC"."AGENT_VERSION"
SET "GRAPH_CONFIG" = REPLACE("GRAPH_CONFIG", '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE "ID" = 10 AND "AGENT_ID" = 'report' AND "VERSION_NUMBER" = 'v1.0.0'
  AND "TENANT_ID" = 1 AND "CREATE_BY" = 'system'
  AND "GRAPH_CONFIG" LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE "PUBLIC"."AGENT_VERSION"
SET "GRAPH_CONFIG" = REPLACE("GRAPH_CONFIG", '"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"', '"platform":"DEEPSEEK","modelName":"deepseek-flash"')
WHERE "ID" = 11 AND "AGENT_ID" = 'exam' AND "VERSION_NUMBER" = 'v1.0.0'
  AND "TENANT_ID" = 1 AND "CREATE_BY" = 'system'
  AND "GRAPH_CONFIG" LIKE '%"platform":"SILICON_FLOW","modelName":"Qwen/Qwen3-8B"%';

UPDATE "PUBLIC"."AGENT_DEF"
SET "DESCRIPTION" = REPLACE("DESCRIPTION", '使用硅基流动 Qwen3-8B 模型', '使用 DeepSeek deepseek-flash 模型')
WHERE "ID" BETWEEN 1 AND 11 AND "CREATE_BY" = 'system'
  AND "DESCRIPTION" LIKE '%使用硅基流动 Qwen3-8B 模型%';
