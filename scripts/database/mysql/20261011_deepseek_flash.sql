-- 备份数据库后，在现有 MySQL 数据库执行一次。
UPDATE model_ai_model
SET model_name = 'deepseek-flash',
    display_name = 'DeepSeek Flash',
    update_time = CURRENT_TIMESTAMP
WHERE id = 4 AND platform_id = 2 AND tenant_id = 1
  AND model_name = 'deepseek-v4-flash' AND create_by = 'system';

UPDATE model_ai_model
SET model_name = 'deepseek-v4-pro',
    display_name = 'DeepSeek V4 Pro',
    update_time = CURRENT_TIMESTAMP
WHERE id = 5 AND platform_id = 2 AND tenant_id = 1
  AND model_name = 'deepseek-reasoner' AND create_by = 'system';

UPDATE model_ai_platform
SET available_models = '["deepseek-flash","deepseek-v4-pro"]',
    update_time = CURRENT_TIMESTAMP
WHERE id = 2 AND tenant_id = 1 AND code = 'DEEPSEEK'
  AND available_models = '["deepseek-v4-flash","deepseek-reasoner"]'
  AND create_by = 'system';
