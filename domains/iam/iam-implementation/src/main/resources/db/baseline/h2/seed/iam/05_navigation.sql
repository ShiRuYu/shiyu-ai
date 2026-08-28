-- Canonical platform navigation for the v3 information architecture.
-- Legacy menu records are removed deliberately: no hidden aliases or old URL
-- compatibility entries remain in the published menu contract.
DELETE FROM "PUBLIC"."AUTH_ROLE_SCOPE_MENU"
WHERE "MENU_ID" IN (10,12,13,14,15,16,40,41,42,43,44,45,46,47,48,49,50,51,52,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,1100,
  1500,1501,1502,1503,1508,1510,1511,1520,1521,1530,1531,1540,1541,1542,1543,1544,1550,1551,1552,1553,1560,1561,1562,1563,1564,1565,1566,1600,1610,1620,1630,1640);
DELETE FROM "PUBLIC"."AUTH_TENANT_MENU"
WHERE "MENU_ID" IN (10,12,13,14,15,16,40,41,42,43,44,45,46,47,48,49,50,51,52,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,1100,
  1500,1501,1502,1503,1508,1510,1511,1520,1521,1530,1531,1540,1541,1542,1543,1544,1550,1551,1552,1553,1560,1561,1562,1563,1564,1565,1566,1600,1610,1620,1630,1640);
DELETE FROM "PUBLIC"."AUTH_MENU"
WHERE "ID" IN (10,12,13,14,15,16,40,41,42,43,44,45,46,47,48,49,50,51,52,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,1100,
  1500,1501,1502,1503,1508,1510,1511,1520,1521,1530,1531,1532,1540,1541,1542,1543,1544,1550,1551,1552,1553,1560,1561,1562,1563,1564,1565,1566,1600,1610,1620,1630,1640);

MERGE INTO "PUBLIC"."AUTH_MENU" ("ID","NAME","CODE","TYPE","PARENT_ID","TENANT_ID","PATH","REDIRECT","ICON","COMPONENT","DESCRIPTION","SHOW","STATUS","ORDER","DEL_FLAG","CREATE_BY","CREATE_TIME","UPDATE_BY","UPDATE_TIME") KEY("ID") VALUES
(2000,'工作台','Workbench','CATALOG',NULL,1,'/workbench','/workbench/overview','lucide:layout-dashboard','','待办、最近会话、审批与运行状态',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2010,'AI 工作区','AiWorkspace','CATALOG',NULL,1,'/workspace','/workspace/chat','lucide:sparkles','','Chat、Agent、RAG 与历史会话',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2020,'应用开发','AppStudio','CATALOG',NULL,1,'/app-studio','/app-studio/apps','lucide:blocks','','AI App、Agent、Prompt 与 Evaluation',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2030,'知识中心','KnowledgeCenter','CATALOG',NULL,1,'/knowledge-center','/knowledge-center/spaces','lucide:brain-circuit','','知识空间、检索与图谱洞察',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2040,'运行观测','Observability','CATALOG',NULL,1,'/observability','/observability/runs','lucide:activity','','Run、Trace、Usage 与工具审批',TRUE,1,5,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2050,'教育空间','EducationWorkspace','CATALOG',NULL,1,'/education-center','/education-center/learning','lucide:graduation-cap','','学习、练习、复习与 AI 辅学',TRUE,1,6,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2060,'个人记录','PersonalRecord','CATALOG',NULL,1,'/record','/record/timeline','lucide:notebook-tabs','','时间轴、人物与内容',TRUE,1,7,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2070,'平台管理','PlatformAdmin','CATALOG',NULL,1,'/platform-admin','/platform-admin/models','lucide:shield-cog','','身份、模型、插件与运维',TRUE,1,8,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2011,'Chat 对话','WorkspaceChat','MENU',2010,1,'/workspace/chat',NULL,'lucide:message-circle','/workspace/chat/index','沉浸式聊天工作区',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2012,'Agent 执行','WorkspaceAgent','MENU',2010,1,'/workspace/agent',NULL,'lucide:bot','/workspace/agent/index','运行已发布 Agent App',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2013,'RAG 检索','WorkspaceRag','MENU',2010,1,'/workspace/rag',NULL,'lucide:search-check','/workspace/rag/index','检索与引用工作区',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2021,'AI App','AiAppStudio','MENU',2020,1,'/app-studio/apps',NULL,'lucide:app-window','/app-studio/index','应用发布与版本',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2022,'Agent Studio','AgentStudio','MENU',2020,1,'/app-studio/agents',NULL,'lucide:bot','/app-studio/agents/index','Agent 图、版本与执行配置',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2023,'Prompt Studio','PromptStudio','MENU',2020,1,'/app-studio/prompts',NULL,'lucide:pen-line','/app-studio/prompts/index','Prompt 版本、预览与变量校验',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2024,'评测中心','EvaluationStudio','MENU',2020,1,'/app-studio/evaluations',NULL,'lucide:chart-no-axes-combined','/app-studio/evaluations/index','数据集、运行与回归门槛',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2025,'意图路由','IntentRouter','MENU',2020,1,'/app-studio/intents',NULL,'lucide:route','/app-studio/intents/index','查询意图和路由策略',TRUE,1,5,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2031,'知识空间','KnowledgeSpaces','MENU',2030,1,'/knowledge-center/spaces',NULL,'lucide:layers-3','/knowledge-center/spaces/index','空间、成员与权限',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2032,'文档中心','KnowledgeDocuments','MENU',2030,1,'/knowledge-center/documents',NULL,'lucide:file-stack','/knowledge-center/documents/index','文档、版本与索引状态',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2033,'检索实验室','KnowledgeSearch','MENU',2030,1,'/knowledge-center/search',NULL,'lucide:search-check','/knowledge-center/search/index','检索策略、引用与评分',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2034,'图谱洞察','KnowledgeGraph','MENU',2030,1,'/knowledge-center/graph',NULL,'lucide:network','/knowledge-center/graph/index','关系图谱与路径解释',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2035,'知识评测','KnowledgeEvaluations','MENU',2030,1,'/knowledge-center/evaluations',NULL,'lucide:chart-no-axes-combined','/knowledge-center/evaluations/index','知识检索评测',TRUE,1,5,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2041,'运行记录','RunObservability','MENU',2040,1,'/observability/runs',NULL,'lucide:list-tree','/observability/index','Run 与 Trace 时间线',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2042,'工具审批','ToolApprovals','MENU',2040,1,'/observability/approvals',NULL,'lucide:badge-check','/observability/approvals/index','高风险工具审批与审计',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2071,'模型与 Provider','PlatformModels','MENU',2070,1,'/platform-admin/models',NULL,'lucide:cpu','/platform-admin/models/index','模型能力、路由与健康',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2072,'插件市场','PluginMarket','MENU',2070,1,'/platform-admin/plugins',NULL,'lucide:puzzle','/platform-admin/plugins/index','签名插件、权限与审计',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2073,'配额与审计','PlatformQuotas','MENU',2070,1,'/platform-admin/quotas',NULL,'lucide:gauge','/platform-admin/quotas/index','租户配额、用量与审计',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2051,'学习','EducationLearning','MENU',2050,1,'/education-center/learning',NULL,'lucide:book-open','/education-center/learning/index','课程与学习资源',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2052,'练习','EducationPractice','MENU',2050,1,'/education-center/practice',NULL,'lucide:clipboard-check','/education-center/practice/index','题库、错题与考试',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2053,'AI 辅学','EducationTutor','MENU',2050,1,'/education-center/ai-tutor',NULL,'lucide:sparkles','/education-center/ai-tutor/index','讲解、出题、规划与报告',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2054,'学习分析','EducationAnalytics','MENU',2050,1,'/education-center/analytics',NULL,'lucide:chart-no-axes-combined','/education-center/analytics/index','学习报告与趋势',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2061,'人物','RecordProfiles','MENU',2060,1,'/record/profiles',NULL,'lucide:user-round','/record/profiles/index','人物与关系',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2062,'时间轴','RecordTimeline','MENU',2060,1,'/record/timeline',NULL,'lucide:timeline','/record/timeline/index','时间轴与事件',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2063,'内容','RecordContent','MENU',2060,1,'/record/content',NULL,'lucide:file-text','/record/content/index','记录内容与附件',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP);

MERGE INTO "PUBLIC"."AUTH_ROLE_SCOPE_MENU" ("ROLE_ID","MENU_ID","TENANT_ID","STATUS","DEL_FLAG","CREATE_BY","CREATE_TIME","UPDATE_BY","UPDATE_TIME") KEY("ROLE_ID","TENANT_ID","MENU_ID")
SELECT R."ID", M."ID", 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM "PUBLIC"."AUTH_ROLE" R CROSS JOIN "PUBLIC"."AUTH_MENU" M
WHERE R."ID" IN (1,2,3) AND M."ID" BETWEEN 2000 AND 2073;

MERGE INTO "PUBLIC"."AUTH_TENANT_MENU" ("TENANT_ID","MENU_ID","STATUS","CREATE_TIME","UPDATE_TIME") KEY("TENANT_ID","MENU_ID")
SELECT 1, M."ID", 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "PUBLIC"."AUTH_MENU" M WHERE M."ID" BETWEEN 2000 AND 2073;
