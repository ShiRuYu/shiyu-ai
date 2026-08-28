-- Final v4 navigation seed. It is executed only against an empty database;
-- legacy menu cleanup belongs to the operator-controlled database rebuild.
INSERT INTO "PUBLIC"."AUTH_MENU" ("ID","NAME","CODE","TYPE","PARENT_ID","TENANT_ID","PATH","REDIRECT","ICON","COMPONENT","DESCRIPTION","SHOW","STATUS","ORDER","DEL_FLAG","CREATE_BY","CREATE_TIME","UPDATE_BY","UPDATE_TIME") VALUES
(2000,'工作台','Workbench','CATALOG',NULL,1,'/workbench','/workbench/overview','lucide:layout-dashboard','','待办、最近会话、审批与运行状态',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2010,'AI 控制台','AiConsole','CATALOG',NULL,1,'/workspace','/workspace/chat','lucide:sparkles','','Chat、Agent、RAG 与历史会话',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2020,'应用开发','AppStudio','CATALOG',NULL,1,'/app-studio','/app-studio/apps','lucide:blocks','','AI App、Agent、Prompt 与 Evaluation',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2030,'知识中心','KnowledgeCenter','CATALOG',NULL,1,'/knowledge-center','/knowledge-center/spaces','lucide:brain-circuit','','知识空间、检索与图谱洞察',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2040,'运行观测','Observability','CATALOG',NULL,1,'/observability','/observability/runs','lucide:activity','','Run、Trace、Usage 与工具审批',TRUE,1,5,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2050,'教育中心','EducationCenter','CATALOG',NULL,1,'/education-center','/education-center/learning','lucide:graduation-cap','','学习、练习、复习与 AI 辅学',TRUE,1,6,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2060,'个人记录','PersonalRecord','CATALOG',NULL,1,'/record','/record/timeline','lucide:notebook-tabs','','时间轴、人物与内容',TRUE,1,7,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2070,'平台管理','PlatformAdmin','CATALOG',NULL,1,'/platform-admin','/platform-admin/models','lucide:shield-cog','','身份、模型、插件与运维',TRUE,1,8,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2011,'Chat 对话','ConversationChat','MENU',2010,1,'/workspace/chat',NULL,'lucide:message-circle','feature:conversation.chat','沉浸式聊天界面',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2012,'Agent 执行','AgentExecution','MENU',2010,1,'/workspace/agent',NULL,'lucide:bot','feature:agent.execution','运行已发布 Agent App',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2013,'RAG 检索','KnowledgeRetrieval','MENU',2010,1,'/workspace/rag',NULL,'lucide:search-check','feature:knowledge.retrieval','检索与引用中心',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2021,'AI App','AiAppStudio','MENU',2020,1,'/app-studio/apps',NULL,'lucide:app-window','feature:agent.apps','应用发布与版本',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2022,'Agent Studio','AgentStudio','MENU',2020,1,'/app-studio/agents',NULL,'lucide:bot','feature:agent.admin','Agent 图、版本与执行配置',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2023,'Prompt Studio','PromptStudio','MENU',2020,1,'/app-studio/prompts',NULL,'lucide:pen-line','feature:conversation.prompts','Prompt 版本、预览与变量校验',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2024,'评测中心','EvaluationStudio','MENU',2020,1,'/app-studio/evaluations',NULL,'lucide:chart-no-axes-combined','feature:knowledge.evaluations','数据集、运行与回归门槛',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2025,'意图路由','IntentRouter','MENU',2020,1,'/app-studio/intents',NULL,'lucide:route','feature:agent.intents','查询意图和路由策略',TRUE,1,5,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2031,'知识空间','KnowledgeSpaces','MENU',2030,1,'/knowledge-center/spaces',NULL,'lucide:layers-3','feature:knowledge.spaces','空间、成员与权限',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2032,'文档中心','KnowledgeDocuments','MENU',2030,1,'/knowledge-center/documents',NULL,'lucide:file-stack','feature:knowledge.documents','文档、版本与索引状态',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2033,'检索实验室','KnowledgeSearch','MENU',2030,1,'/knowledge-center/search',NULL,'lucide:search-check','feature:knowledge.search','检索策略、引用与评分',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2034,'图谱洞察','KnowledgeGraph','MENU',2030,1,'/knowledge-center/graph',NULL,'lucide:network','feature:knowledge.graph','关系图谱与路径解释',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2035,'知识评测','KnowledgeEvaluations','MENU',2030,1,'/knowledge-center/evaluations',NULL,'lucide:chart-no-axes-combined','feature:knowledge.evaluations','知识检索评测',TRUE,1,5,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2041,'运行记录','RunObservability','MENU',2040,1,'/observability/runs',NULL,'lucide:list-tree','feature:governance.observability','Run 与 Trace 时间线',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2042,'工具审批','ToolApprovals','MENU',2040,1,'/observability/approvals',NULL,'lucide:badge-check','feature:governance.approvals','高风险工具审批与审计',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2071,'模型与 Provider','PlatformModels','MENU',2070,1,'/platform-admin/models',NULL,'lucide:cpu','feature:model.models','模型能力、路由与健康',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2072,'插件市场','PluginMarket','MENU',2070,1,'/platform-admin/plugins',NULL,'lucide:puzzle','feature:tooling.plugins','签名插件、权限与审计',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2073,'配额与审计','PlatformQuotas','MENU',2070,1,'/platform-admin/quotas',NULL,'lucide:gauge','feature:governance.quotas','租户配额、用量与审计',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2051,'学习','EducationLearning','MENU',2050,1,'/education-center/learning',NULL,'lucide:book-open','feature:education.learning','课程与学习资源',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2052,'练习','EducationPractice','MENU',2050,1,'/education-center/practice',NULL,'lucide:clipboard-check','feature:education.practice','题库、错题与考试',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2053,'AI 辅学','EducationTutor','MENU',2050,1,'/education-center/ai-tutor',NULL,'lucide:sparkles','feature:education.tutor','讲解、出题、规划与报告',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2054,'学习分析','EducationAnalytics','MENU',2050,1,'/education-center/analytics',NULL,'lucide:chart-no-axes-combined','feature:education.analytics','学习报告与趋势',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2061,'人物','RecordProfiles','MENU',2060,1,'/record/profiles',NULL,'lucide:user-round','feature:record.profiles','人物与关系',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2062,'时间轴','RecordTimeline','MENU',2060,1,'/record/timeline',NULL,'lucide:timeline','feature:record.timeline','时间轴与事件',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2063,'内容','RecordContent','MENU',2060,1,'/record/content',NULL,'lucide:file-text','feature:record.content','记录内容与附件',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP);

INSERT INTO "PUBLIC"."AUTH_ROLE_SCOPE_MENU" ("ROLE_ID","MENU_ID","TENANT_ID","STATUS","DEL_FLAG","CREATE_BY","CREATE_TIME","UPDATE_BY","UPDATE_TIME")
SELECT R."ID", M."ID", 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM "PUBLIC"."AUTH_ROLE" R CROSS JOIN "PUBLIC"."AUTH_MENU" M
WHERE R."ID" IN (1,2,3) AND M."ID" BETWEEN 2000 AND 2073;

INSERT INTO "PUBLIC"."AUTH_TENANT_MENU" ("TENANT_ID","MENU_ID","STATUS","CREATE_TIME","UPDATE_TIME")
SELECT 1, M."ID", 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "PUBLIC"."AUTH_MENU" M WHERE M."ID" BETWEEN 2000 AND 2073;
