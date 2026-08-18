-- Role-oriented platform navigation. This is additive and idempotent so a v3
-- database can adopt the same information architecture as a fresh install.
UPDATE "PUBLIC"."AUTH_MENU" SET "SHOW"=FALSE WHERE "CODE" IN ('AgentChatConfig', 'EduAiChat');
-- The legacy chat-debug entry remains addressable for old bookmarks, but its
-- page is now the unified workspace. Keeping a real component prevents a
-- hidden route from breaking the front-end menu contract during route loading.
UPDATE "PUBLIC"."AUTH_MENU"
SET "COMPONENT"='/workspace/chat/index'
WHERE "CODE"='AgentChatConfig';

MERGE INTO "PUBLIC"."AUTH_MENU" ("ID","NAME","CODE","TYPE","PARENT_ID","TENANT_ID","PATH","REDIRECT","ICON","COMPONENT","DESCRIPTION","SHOW","STATUS","ORDER","DEL_FLAG","CREATE_BY","CREATE_TIME","UPDATE_BY","UPDATE_TIME") KEY("ID") VALUES
(2000,'工作台','Workbench','CATALOG',NULL,1,'/workbench','/dashboard/overview','lucide:layout-dashboard','','待办、最近会话、审批与运行状态',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2010,'AI 工作区','AiWorkspace','CATALOG',NULL,1,'/workspace','/workspace/chat','lucide:sparkles','','Chat、Agent、RAG 与历史会话',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2020,'应用开发','AppStudio','CATALOG',NULL,1,'/app-studio','/app-studio','lucide:blocks','','AI App、Agent、Prompt 与 Evaluation',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2030,'知识中心','KnowledgeCenter','CATALOG',NULL,1,'/knowledge-center','/knowledge-center','lucide:brain-circuit','','知识空间、检索与图谱洞察',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2040,'运行观测','Observability','CATALOG',NULL,1,'/observability','/observability','lucide:activity','','Run、Trace、Usage 与工具审批',TRUE,1,5,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2050,'教育空间','EducationWorkspace','CATALOG',NULL,1,'/education-center','/learning/course/list','lucide:graduation-cap','','学习、练习、复习与 AI 辅学',TRUE,1,6,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2060,'个人记录','PersonalRecord','CATALOG',NULL,1,'/record','/record/timeline/list','lucide:notebook-tabs','','时间轴、人物与内容',TRUE,1,7,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2070,'平台管理','PlatformAdmin','CATALOG',NULL,1,'/platform-admin','/platform-admin','lucide:shield-cog','','身份、模型、插件与运维',TRUE,1,8,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2011,'Chat 对话','WorkspaceChat','MENU',2010,1,'/workspace/chat',NULL,'lucide:message-circle','/workspace/chat/index','沉浸式聊天工作区',TRUE,1,1,0,'system',CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
(2012,'Agent 执行','WorkspaceAgent','MENU',2010,1,'/workspace/agent',NULL,'lucide:bot','/workspace/agent/index','运行已发布 Agent App',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2013,'RAG 检索','WorkspaceRag','MENU',2010,1,'/workspace/rag',NULL,'lucide:search-check','/workspace/rag/index','检索与引用工作区',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2021,'AI App','AiAppStudio','MENU',2020,1,'/app-studio/apps',NULL,'lucide:app-window','/app-studio/index','应用发布与版本',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2022,'Prompt Studio','PromptStudio','MENU',2020,1,'/app-studio/prompts',NULL,'lucide:pen-line','/agent/intent/list','Prompt 版本、预览与评测',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2023,'评测中心','EvaluationStudio','MENU',2020,1,'/app-studio/evaluations',NULL,'lucide:chart-no-axes-combined','/knowledge/evaluations/index','数据集、运行与回归门槛',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2041,'运行记录','RunObservability','MENU',2040,1,'/observability/runs',NULL,'lucide:list-tree','/observability/index','Run 与 Trace 时间线',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2042,'工具审批','ToolApprovals','MENU',2040,1,'/observability/approvals',NULL,'lucide:badge-check','/observability/index','高风险工具审批与审计',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2071,'模型与 Provider','PlatformModels','MENU',2070,1,'/platform-admin/models',NULL,'lucide:cpu','/platform-admin/index','模型能力、路由与健康',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2072,'插件市场','PluginMarket','MENU',2070,1,'/platform-admin/plugins',NULL,'lucide:puzzle','/platform-admin/index','签名插件、权限与审计',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP);

MERGE INTO "PUBLIC"."AUTH_ROLE_SCOPE_MENU" ("ROLE_ID","MENU_ID","TENANT_ID","STATUS","DEL_FLAG","CREATE_BY","CREATE_TIME","UPDATE_BY","UPDATE_TIME") KEY("ROLE_ID","TENANT_ID","MENU_ID")
SELECT R."ID", M."ID", 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM "PUBLIC"."AUTH_ROLE" R CROSS JOIN "PUBLIC"."AUTH_MENU" M
WHERE R."ID" IN (1,2,3) AND M."ID" BETWEEN 2000 AND 2072;

MERGE INTO "PUBLIC"."AUTH_TENANT_MENU" ("TENANT_ID","MENU_ID","STATUS","CREATE_TIME","UPDATE_TIME") KEY("TENANT_ID","MENU_ID")
SELECT 1, M."ID", 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "PUBLIC"."AUTH_MENU" M WHERE M."ID" BETWEEN 2000 AND 2072;
