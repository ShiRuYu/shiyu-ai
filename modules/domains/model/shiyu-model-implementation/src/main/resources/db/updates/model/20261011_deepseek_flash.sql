UPDATE "MODEL_AI_MODEL"
SET "MODEL_NAME" = 'deepseek-flash',
    "DISPLAY_NAME" = 'DeepSeek Flash',
    "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" = 4 AND "PLATFORM_ID" = 2 AND "TENANT_ID" = 1
  AND "MODEL_NAME" = 'deepseek-v4-flash' AND "CREATE_BY" = 'system';

UPDATE "MODEL_AI_MODEL"
SET "MODEL_NAME" = 'deepseek-v4-pro',
    "DISPLAY_NAME" = 'DeepSeek V4 Pro',
    "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" = 5 AND "PLATFORM_ID" = 2 AND "TENANT_ID" = 1
  AND "MODEL_NAME" = 'deepseek-reasoner' AND "CREATE_BY" = 'system';

UPDATE "MODEL_AI_PLATFORM"
SET "AVAILABLE_MODELS" = '["deepseek-flash","deepseek-v4-pro"]',
    "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" = 2 AND "TENANT_ID" = 1 AND "CODE" = 'DEEPSEEK'
  AND "AVAILABLE_MODELS" = '["deepseek-v4-flash","deepseek-reasoner"]'
  AND "CREATE_BY" = 'system';
