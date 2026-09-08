-- Realistic knowledge-base demo content for the default tenant.
INSERT INTO "PUBLIC"."KNOWLEDGE_SPACE"
    ("ID", "CODE", "NAME", "DESCRIPTION", "ACCESS_MODE", "REVIEW_MODE", "EMBEDDING_PROFILE", "RERANK_PROFILE", "CHUNK_STRATEGY", "CHUNK_SIZE", "CHUNK_OVERLAP", "ACTIVE_INDEX_VERSION", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY", "DIFFICULTY_SCALE_ID", "BINDING_MODE", "DOMAIN_CODE")
VALUES (1, 'DEMO_EDU_MATH', '七年级数学知识库', '用于数学辅导问答的课程资料与例题知识库', 'PRIVATE', 'OPTIONAL', 'text-embedding-v3', 'default', 'HEADING', 800, 100, 1, 1, 1, 0, 'system', 'system', 1, 'OPTIONAL', 'EDUCATION');

INSERT INTO "PUBLIC"."KNOWLEDGE_BASE"
    ("ID", "CODE", "NAME", "DESCRIPTION", "DIFFICULTY", "CATEGORY", "TAGS", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY", "SPACE_ID", "DIFFICULTY_LEVEL")
VALUES (1001, 'MATH_LINEAR_EQUATION', '一元一次方程', '含有一个未知数且未知数次数为一的整式方程', 2, 'math', '["代数","方程","七年级"]', 1, 1, 0, 'system', 'system', 1, 2);

INSERT INTO "PUBLIC"."KNOWLEDGE_DOCUMENT"
    ("ID", "TITLE", "CONTENT", "DOC_TYPE", "SOURCE", "AUTHOR", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY", "SPACE_ID", "CURRENT_VERSION_ID", "LIFECYCLE_STATUS", "PARSE_STATUS", "STORAGE_PROVIDER", "FILE_SIZE", "MIME_TYPE")
VALUES (1001, '一元一次方程学习讲义', '一元一次方程的基本步骤：去括号、移项、合并同类项、系数化为一。', 'LECTURE', '七年级数学课程组', '拾羽实验学校数学组', 1, 1, 0, 'system', 'system', 1, 1001, 'PUBLISHED', 'READY', 'local', 86, 'text/plain');

INSERT INTO "PUBLIC"."KNOWLEDGE_DOCUMENT_VERSION"
    ("ID", "DOCUMENT_ID", "SPACE_ID", "VERSION_NO", "TITLE", "CONTENT", "STORAGE_PROVIDER", "FILE_SIZE", "MIME_TYPE", "LIFECYCLE_STATUS", "PARSE_STATUS", "MODEL_PROFILE", "PUBLISHED_AT", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1001, 1001, 1, 1, '一元一次方程学习讲义', '一元一次方程的基本步骤：去括号、移项、合并同类项、系数化为一。', 'local', 86, 'text/plain', 'PUBLISHED', 'READY', 'default', TIMESTAMP '2026-09-08 08:30:00', 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."KNOWLEDGE_DOC_RELATION"
    ("ID", "SPACE_ID", "DOC_ID", "KNOWLEDGE_ID", "RELATION_TYPE", "TENANT_ID", "CREATE_BY", "STATUS", "DEL_FLAG")
VALUES (1001, 1, 1001, 1001, 'PRIMARY', 1, 'system', 1, 0);

INSERT INTO "PUBLIC"."VECTOR_KNOWLEDGE_CHUNK"
    ("ID", "DOCUMENT_ID", "CONTENT", "EMBEDDING", "METADATA", "CHUNK_INDEX", "TENANT_ID", "CREATE_BY", "STATUS", "DEL_FLAG", "SPACE_ID", "VERSION_ID", "EMBEDDING_MODEL", "EMBEDDING_DIMENSION", "PAGE_NUMBER", "SECTION_PATH", "TOKEN_COUNT")
VALUES (1001, 1001, '解一元一次方程时，先化简方程两边，再把含未知数的项移到一边，常数项移到另一边，最后求出未知数。', NULL, '{"subject":"数学","grade":7,"source":"一元一次方程学习讲义"}', 0, 1, 'system', 1, 0, 1, 1001, 'text-embedding-v3', 1536, 1, '一、解方程步骤', 34);

INSERT INTO "PUBLIC"."KNOWLEDGE_INGESTION_JOB"
    ("ID", "JOB_KEY", "JOB_TYPE", "SPACE_ID", "DOCUMENT_ID", "VERSION_ID", "ACTOR_USER_ID", "JOB_STATUS", "STAGE", "PROGRESS", "ATTEMPTS", "MAX_ATTEMPTS", "CHECKPOINT_DATA", "FINISHED_TIME", "LOCK_VERSION", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1001, 'demo-document-1001-v1', 'DOCUMENT_UPLOAD', 1, 1001, 1001, 2, 'SUCCEEDED', 'INDEXED', 100, 1, 3,
        '{"parsed":true,"chunkCount":1,"embeddingModel":"text-embedding-v3","vectorStored":true}',
        TIMESTAMP '2026-09-08 08:32:14', 1, 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."KNOWLEDGE_AUDIT_LOG"
    ("ID", "SPACE_ID", "RESOURCE_TYPE", "RESOURCE_ID", "ACTION", "DETAIL_JSON", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "CREATE_TIME", "UPDATE_BY", "UPDATE_TIME")
VALUES (1001, 1, 'DOCUMENT', 1001, 'INGESTION_COMPLETED',
        '{"jobKey":"demo-document-1001-v1","parseStatus":"READY","vectorChunks":1,"embeddingModel":"text-embedding-v3"}',
        1, 1, 0, 'system', TIMESTAMP '2026-09-08 08:32:14', 'system', TIMESTAMP '2026-09-08 08:32:14');

INSERT INTO "PUBLIC"."KNOWLEDGE_EVALUATION_CASE"
    ("ID", "SPACE_ID", "QUESTION", "EXPECTED_DOC_IDS", "EXPECTED_ANSWER", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "CREATE_TIME", "UPDATE_BY", "UPDATE_TIME")
VALUES (1001, 1, '一元一次方程移项时需要注意什么？', '[1001]',
        '移项后需要同时改变该项的符号，再合并同类项并求解。',
        1, 1, 0, 'system', TIMESTAMP '2026-09-08 08:35:00', 'system', TIMESTAMP '2026-09-08 08:35:00');
