INSERT INTO "PUBLIC"."AI_APP" ("ID", "TENANT_ID", "OWNER_USER_ID", "NAME", "DESCRIPTION", "STATUS", "PUBLISHED_VERSION_ID", "CREATED_AT", "UPDATED_AT")
VALUES ('edu-tutor-app', 1, 1, '数学学习助手', '面向七年级一元一次方程学习的教育应用示例。', 'PUBLISHED', 'edu-tutor-v1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO "PUBLIC"."AI_APP_VERSION" ("ID", "APP_ID", "TENANT_ID", "VERSION", "CONFIG_JSON", "STATUS", "CREATED_AT", "PUBLISHED_AT")
VALUES ('edu-tutor-v1', 'edu-tutor-app', 1, '1.0.0',
        '{"module":"education","platform":"DEEPSEEK","model":"deepseek-v4-flash","agentId":"tutor-bot","agentVersion":"v1.0.0","knowledgeSpaceIds":[1],"resourceTypes":["VIDEO","PDF"],"executionType":"AGENT"}',
        'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO "PUBLIC"."AI_APP" ("ID", "TENANT_ID", "OWNER_USER_ID", "NAME", "DESCRIPTION", "STATUS", "PUBLISHED_VERSION_ID", "CREATED_AT", "UPDATED_AT")
VALUES ('knowledge-qa-app', 1, 1, '知识库问答助手', '面向课程资料检索与引用回答的知识库应用示例。', 'PUBLISHED', 'knowledge-qa-v1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO "PUBLIC"."AI_APP_VERSION" ("ID", "APP_ID", "TENANT_ID", "VERSION", "CONFIG_JSON", "STATUS", "CREATED_AT", "PUBLISHED_AT")
VALUES ('knowledge-qa-v1', 'knowledge-qa-app', 1, '1.0.0',
        '{"module":"knowledge","platform":"DEEPSEEK","model":"deepseek-v4-flash","agentId":"rag-knowledge-agent","agentVersion":"v1.0.0","knowledgeSpaceIds":[1],"retrievalMode":"HYBRID","topK":5,"executionType":"AGENT"}',
        'PUBLISHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
