-- Baseline 1 -> 2: converge navigation and group Education Space menus.
-- Business URLs and component paths remain unchanged; only menu metadata and ancestry change.

UPDATE "PUBLIC"."AUTH_MENU"
SET "NAME" = '系统管理', "DESCRIPTION" = '用户、权限、租户与平台基础设施',
    "ORDER" = 50, "UPDATE_BY" = 'system', "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" = 1;

UPDATE "PUBLIC"."AUTH_MENU"
SET "NAME" = 'Agent 平台', "DESCRIPTION" = 'Agent、模型、平台、意图与调试能力',
    "ORDER" = 10, "UPDATE_BY" = 'system', "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" = 10;

UPDATE "PUBLIC"."AUTH_MENU"
SET "NAME" = '知识引擎', "DESCRIPTION" = '知识空间、文档、图谱、检索与评测',
    "ORDER" = 20, "UPDATE_BY" = 'system', "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" = 70;

UPDATE "PUBLIC"."AUTH_MENU"
SET "PARENT_ID" = 1, "DESCRIPTION" = '平台文件与对象存储管理',
    "ORDER" = 7, "UPDATE_BY" = 'system', "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" = 90;

UPDATE "PUBLIC"."AUTH_MENU"
SET "NAME" = '教育配置', "CODE" = 'EduConfiguration',
    "PATH" = '/education-center/config', "REDIRECT" = '/edu/subject',
    "ICON" = 'lucide:settings-2',
    "DESCRIPTION" = '学科、教材、课程、题库与教学资源配置',
    "ORDER" = 6, "UPDATE_BY" = 'system', "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" = 40;

INSERT INTO "PUBLIC"."AUTH_MENU" VALUES
(1600, '学习', 'EduLearning', 'CATALOG', 1500, 1, '/education-center/learning', '/learning/course', 'lucide:book-open', '', NULL, NULL, NULL, '课程、知识、计划与学习资源', TRUE, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(1610, '练习与考试', 'EduPracticeExam', 'CATALOG', 1500, 1, '/education-center/practice-exam', '/practice/question', 'lucide:clipboard-check', '', NULL, NULL, NULL, '题库、错题、在线考试与 AI 组卷', TRUE, 1, 2, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(1620, '复习', 'EduReview', 'CATALOG', 1500, 1, '/education-center/review', '/review/today', 'lucide:history', '', NULL, NULL, NULL, '今日复习与复习历史', TRUE, 1, 3, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(1630, 'AI 辅学', 'EduAiTutor', 'CATALOG', 1500, 1, '/education-center/ai-tutor', '/ai-tutor/teacher', 'lucide:sparkles', '', NULL, NULL, NULL, 'AI 讲解、出题、规划、对话与报告', TRUE, 1, 4, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(1640, '学习分析', 'EduLearningAnalytics', 'CATALOG', 1500, 1, '/education-center/analytics', '/analytics-center/report', 'lucide:chart-no-axes-combined', '', NULL, NULL, NULL, '学习报告、能力、趋势与薄弱点分析', TRUE, 1, 5, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

UPDATE "PUBLIC"."AUTH_MENU" SET "PARENT_ID" = 1600, "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" IN (1501, 1502, 1503, 1508, 1560, 1561, 1562, 1563);
UPDATE "PUBLIC"."AUTH_MENU" SET "PARENT_ID" = 1610, "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" IN (1510, 1511, 1520, 1521, 1564, 1565, 1566);
UPDATE "PUBLIC"."AUTH_MENU" SET "PARENT_ID" = 1620, "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" IN (1530, 1531);
UPDATE "PUBLIC"."AUTH_MENU" SET "PARENT_ID" = 1630, "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" IN (1540, 1541, 1542, 1543, 1544);
UPDATE "PUBLIC"."AUTH_MENU" SET "PARENT_ID" = 1640, "UPDATE_TIME" = CURRENT_TIMESTAMP
WHERE "ID" IN (1550, 1551, 1552, 1553);

UPDATE "PUBLIC"."AUTH_MENU" SET "ORDER" = 1 WHERE "ID" IN (1510, 1530, 1540, 1550);
UPDATE "PUBLIC"."AUTH_MENU" SET "ORDER" = 2 WHERE "ID" IN (1511, 1531, 1541, 1551);
UPDATE "PUBLIC"."AUTH_MENU" SET "ORDER" = 3 WHERE "ID" IN (1520, 1542, 1552);
UPDATE "PUBLIC"."AUTH_MENU" SET "ORDER" = 4 WHERE "ID" IN (1521, 1543, 1553);
UPDATE "PUBLIC"."AUTH_MENU" SET "ORDER" = 5 WHERE "ID" = 1544;

-- Grant each new catalog wherever at least one descendant was already granted.
INSERT INTO "PUBLIC"."AUTH_TENANT_MENU" ("TENANT_ID", "MENU_ID", "STATUS", "CREATE_TIME", "UPDATE_TIME")
SELECT DISTINCT source."TENANT_ID", groups."MENU_ID", 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "PUBLIC"."AUTH_TENANT_MENU" source
JOIN (VALUES
    (1600, 1501), (1600, 1502), (1600, 1503), (1600, 1508),
    (1610, 1510), (1610, 1511), (1610, 1520), (1610, 1521),
    (1620, 1530), (1620, 1531),
    (1630, 1540), (1630, 1541), (1630, 1542), (1630, 1543), (1630, 1544),
    (1640, 1550), (1640, 1551), (1640, 1552), (1640, 1553)
) groups("MENU_ID", "CHILD_ID") ON groups."CHILD_ID" = source."MENU_ID"
WHERE NOT EXISTS (
    SELECT 1 FROM "PUBLIC"."AUTH_TENANT_MENU" target
    WHERE target."TENANT_ID" = source."TENANT_ID" AND target."MENU_ID" = groups."MENU_ID"
);

INSERT INTO "PUBLIC"."AUTH_ROLE_SCOPE_MENU"
("ROLE_ID", "MENU_ID", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "CREATE_TIME", "UPDATE_BY", "UPDATE_TIME")
SELECT DISTINCT source."ROLE_ID", groups."MENU_ID", source."TENANT_ID", 1, 0,
       'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM "PUBLIC"."AUTH_ROLE_SCOPE_MENU" source
JOIN (VALUES
    (1600, 1501), (1600, 1502), (1600, 1503), (1600, 1508),
    (1610, 1510), (1610, 1511), (1610, 1520), (1610, 1521),
    (1620, 1530), (1620, 1531),
    (1630, 1540), (1630, 1541), (1630, 1542), (1630, 1543), (1630, 1544),
    (1640, 1550), (1640, 1551), (1640, 1552), (1640, 1553)
) groups("MENU_ID", "CHILD_ID") ON groups."CHILD_ID" = source."MENU_ID"
WHERE source."DEL_FLAG" = 0
  AND NOT EXISTS (
    SELECT 1 FROM "PUBLIC"."AUTH_ROLE_SCOPE_MENU" target
    WHERE target."ROLE_ID" = source."ROLE_ID"
      AND target."TENANT_ID" = source."TENANT_ID"
      AND target."MENU_ID" = groups."MENU_ID"
  );
