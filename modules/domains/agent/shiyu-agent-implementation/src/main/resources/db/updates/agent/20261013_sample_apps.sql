INSERT INTO "PUBLIC"."AI_APP"
    ("ID", "TENANT_ID", "OWNER_USER_ID", "NAME", "DESCRIPTION", "STATUS",
     "PUBLISHED_VERSION_ID", "CREATED_AT", "UPDATED_AT")
SELECT 'edu-tutor-app', 1, 2, '数学学习助手',
       '面向七年级一元一次方程学习的教育应用示例。', 'PUBLISHED',
       'edu-tutor-v1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM "PUBLIC"."AI_APP" WHERE "ID" = 'edu-tutor-app');

INSERT INTO "PUBLIC"."AI_APP_VERSION"
    ("ID", "APP_ID", "TENANT_ID", "VERSION", "CONFIG_JSON", "STATUS",
     "CREATED_AT", "PUBLISHED_AT")
SELECT 'edu-tutor-v1', 'edu-tutor-app', 1, '1.0.0',
       '{"module":"education","platform":"DEEPSEEK","model":"deepseek-flash","agentId":"tutor-bot","agentVersion":"v1.0.0","knowledgeSpaceIds":[1],"resourceTypes":["VIDEO","PDF"],"executionType":"AGENT"}',
       'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "PUBLIC"."AI_APP" app
WHERE app."ID" = 'edu-tutor-app' AND app."TENANT_ID" = 1
  AND app."NAME" = '数学学习助手'
  AND app."PUBLISHED_VERSION_ID" = 'edu-tutor-v1'
  AND NOT EXISTS (SELECT 1 FROM "PUBLIC"."AI_APP_VERSION" WHERE "ID" = 'edu-tutor-v1');

INSERT INTO "PUBLIC"."AI_APP"
    ("ID", "TENANT_ID", "OWNER_USER_ID", "NAME", "DESCRIPTION", "STATUS",
     "PUBLISHED_VERSION_ID", "CREATED_AT", "UPDATED_AT")
SELECT 'knowledge-qa-app', 1, 2, '知识库问答助手',
       '面向课程资料检索与引用回答的知识库应用示例。', 'PUBLISHED',
       'knowledge-qa-v1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM "PUBLIC"."AI_APP" WHERE "ID" = 'knowledge-qa-app');

INSERT INTO "PUBLIC"."AI_APP_VERSION"
    ("ID", "APP_ID", "TENANT_ID", "VERSION", "CONFIG_JSON", "STATUS",
     "CREATED_AT", "PUBLISHED_AT")
SELECT 'knowledge-qa-v1', 'knowledge-qa-app', 1, '1.0.0',
       '{"module":"knowledge","platform":"DEEPSEEK","model":"deepseek-flash","agentId":"rag-knowledge-agent","agentVersion":"v1.0.0","knowledgeSpaceIds":[1],"retrievalMode":"HYBRID","topK":5,"executionType":"AGENT"}',
       'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "PUBLIC"."AI_APP" app
WHERE app."ID" = 'knowledge-qa-app' AND app."TENANT_ID" = 1
  AND app."NAME" = '知识库问答助手'
  AND app."PUBLISHED_VERSION_ID" = 'knowledge-qa-v1'
  AND NOT EXISTS (SELECT 1 FROM "PUBLIC"."AI_APP_VERSION" WHERE "ID" = 'knowledge-qa-v1');

UPDATE "PUBLIC"."AI_APP" SET "OWNER_USER_ID" = 2
WHERE "TENANT_ID" = 1 AND "OWNER_USER_ID" = 1 AND "STATUS" = 'PUBLISHED'
  AND (("ID" = 'edu-tutor-app' AND "NAME" = '数学学习助手'
        AND "PUBLISHED_VERSION_ID" = 'edu-tutor-v1')
    OR ("ID" = 'knowledge-qa-app' AND "NAME" = '知识库问答助手'
        AND "PUBLISHED_VERSION_ID" = 'knowledge-qa-v1'))
  AND EXISTS (SELECT 1 FROM "PUBLIC"."AUTH_USER"
              WHERE "ID" = 2 AND "USERNAME" = 'admin');
