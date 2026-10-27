-- 从 H2 源资源生成的 PostgreSQL 资源。
-- 不要直接编辑此生成文件；请修改源资源后重新生成。
-- 转换来源：modules/domains/model/shiyu-model-implementation/src/main/resources/db/updates/model/20261011_deepseek_flash.sql

UPDATE "model_ai_model"
SET "model_name" = 'deepseek-flash',
    "display_name" = 'DeepSeek Flash',
    "update_time" = CURRENT_TIMESTAMP
WHERE "id" = 4 AND "platform_id" = 2 AND "tenant_id" = 1
  AND "model_name" = 'deepseek-v4-flash' AND "create_by" = 'system';

UPDATE "model_ai_model"
SET "model_name" = 'deepseek-v4-pro',
    "display_name" = 'DeepSeek V4 Pro',
    "update_time" = CURRENT_TIMESTAMP
WHERE "id" = 5 AND "platform_id" = 2 AND "tenant_id" = 1
  AND "model_name" = 'deepseek-reasoner' AND "create_by" = 'system';

UPDATE "model_ai_platform"
SET "available_models" = '["deepseek-flash","deepseek-v4-pro"]',
    "update_time" = CURRENT_TIMESTAMP
WHERE "id" = 2 AND "tenant_id" = 1 AND "code" = 'DEEPSEEK'
  AND "available_models" = '["deepseek-v4-flash","deepseek-reasoner"]'
  AND "create_by" = 'system';
