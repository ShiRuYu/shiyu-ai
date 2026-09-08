-- Realistic usage seed for the default tenant and admin user.
INSERT INTO "PUBLIC"."GOVERNANCE_USAGE_RECORD"
    ("ID", "TENANT_ID", "USER_ID", "CORRELATION_ID", "SOURCE_TYPE", "SOURCE_ID",
     "INPUT_TOKENS", "OUTPUT_TOKENS", "COST", "OCCURRED_AT", "USAGE_TYPE", "LATENCY_MS", "SESSION_ID", "EXT_INFO")
VALUES ('demo-usage-20260908-001', 1, 2, 'demo-chat-20260908-001', 'CHAT_GENERATION',
        'demo-generation-20260908-001', 842, 316, 0.00421000,
        TIMESTAMP '2026-09-08 09:15:00', 'METERED', 1280, 'demo-session-20260908',
        '{"platform":"DEEPSEEK","model":"deepseek-v4-flash","environment":"demo"}');

INSERT INTO "PUBLIC"."GOVERNANCE_USAGE_RECORD"
    ("ID", "TENANT_ID", "USER_ID", "CORRELATION_ID", "SOURCE_TYPE", "SOURCE_ID",
     "INPUT_TOKENS", "OUTPUT_TOKENS", "COST", "OCCURRED_AT", "USAGE_TYPE", "LATENCY_MS", "SESSION_ID", "EXT_INFO")
VALUES ('demo-usage-20260908-002', 1, 2, 'demo-chat-knowledge-20260908-001', 'CHAT_GENERATION',
        'demo-generation-knowledge-20260908-001', 516, 118, 0.00207000,
        TIMESTAMP '2026-09-08 10:03:12', 'METERED', 940, 'demo-session-knowledge-20260908',
        '{"appId":"knowledge-qa-app","platform":"DEEPSEEK","model":"deepseek-v4-flash","environment":"demo"}');
