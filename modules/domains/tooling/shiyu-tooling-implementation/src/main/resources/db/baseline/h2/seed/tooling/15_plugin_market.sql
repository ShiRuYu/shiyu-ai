INSERT INTO "PUBLIC"."PLUGIN_MARKET_ENTRY"
    ("ID", "VERSION", "SOURCE", "MANIFEST", "SIGNATURE", "PUBLISHER_KEY", "PERMISSIONS_JSON", "CHECKSUM", "UPDATE_POLICY", "PUBLISHED_AT", "ENABLED")
VALUES ('edu-calculator', '1.0.0', 'https://plugins.example.invalid/edu-calculator-1.0.0.jar',
        '{"name":"教育计算器","description":"为数学练习提供安全的表达式计算工具","entrypoint":"com.shiyu.plugin.education.CalculatorPlugin"}',
        NULL, NULL, '["calculator:read"]', 'demo-checksum-edu-calculator', 'MANUAL',
        TIMESTAMP '2026-09-08 08:00:00', FALSE);
