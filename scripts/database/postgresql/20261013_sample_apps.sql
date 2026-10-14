-- Run once on existing PostgreSQL installations after a database backup.
INSERT INTO "ai_app"
    ("id", "tenant_id", "owner_user_id", "name", "description", "status",
     "published_version_id", "created_at", "updated_at")
SELECT 'edu-tutor-app', 1, 2, '数学学习助手',
       '面向七年级一元一次方程学习的教育应用示例。', 'PUBLISHED',
       'edu-tutor-v1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM "ai_app" WHERE "id" = 'edu-tutor-app');

INSERT INTO "ai_app_version"
    ("id", "app_id", "tenant_id", "version", "config_json", "status",
     "created_at", "published_at")
SELECT 'edu-tutor-v1', 'edu-tutor-app', 1, '1.0.0',
       '{"module":"education","platform":"DEEPSEEK","model":"deepseek-flash","agentId":"tutor-bot","agentVersion":"v1.0.0","knowledgeSpaceIds":[1],"resourceTypes":["VIDEO","PDF"],"executionType":"AGENT"}',
       'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "ai_app" app
WHERE app."id" = 'edu-tutor-app' AND app."tenant_id" = 1
  AND app."name" = '数学学习助手'
  AND app."published_version_id" = 'edu-tutor-v1'
  AND NOT EXISTS (SELECT 1 FROM "ai_app_version" WHERE "id" = 'edu-tutor-v1');

INSERT INTO "ai_app"
    ("id", "tenant_id", "owner_user_id", "name", "description", "status",
     "published_version_id", "created_at", "updated_at")
SELECT 'knowledge-qa-app', 1, 2, '知识库问答助手',
       '面向课程资料检索与引用回答的知识库应用示例。', 'PUBLISHED',
       'knowledge-qa-v1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM "ai_app" WHERE "id" = 'knowledge-qa-app');

INSERT INTO "ai_app_version"
    ("id", "app_id", "tenant_id", "version", "config_json", "status",
     "created_at", "published_at")
SELECT 'knowledge-qa-v1', 'knowledge-qa-app', 1, '1.0.0',
       '{"module":"knowledge","platform":"DEEPSEEK","model":"deepseek-flash","agentId":"rag-knowledge-agent","agentVersion":"v1.0.0","knowledgeSpaceIds":[1],"retrievalMode":"HYBRID","topK":5,"executionType":"AGENT"}',
       'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "ai_app" app
WHERE app."id" = 'knowledge-qa-app' AND app."tenant_id" = 1
  AND app."name" = '知识库问答助手'
  AND app."published_version_id" = 'knowledge-qa-v1'
  AND NOT EXISTS (SELECT 1 FROM "ai_app_version" WHERE "id" = 'knowledge-qa-v1');

UPDATE "ai_app" SET "owner_user_id" = 2
WHERE "tenant_id" = 1 AND "owner_user_id" = 1 AND "status" = 'PUBLISHED'
  AND (("id" = 'edu-tutor-app' AND "name" = '数学学习助手'
        AND "published_version_id" = 'edu-tutor-v1')
    OR ("id" = 'knowledge-qa-app' AND "name" = '知识库问答助手'
        AND "published_version_id" = 'knowledge-qa-v1'))
  AND EXISTS (SELECT 1 FROM "auth_user"
              WHERE "id" = 2 AND "username" = 'admin');
