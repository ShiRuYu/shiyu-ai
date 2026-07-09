-- ============================================
-- Data: auth — 租户/用户/角色/工作空间/菜单/权限
-- ============================================

-- 默认租户
INSERT IGNORE INTO `tenant` (`id`, `code`, `name`, `contact_name`, `contact_phone`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 'default', '默认租户', 'Admin', '13800000000', 1, 0, 'system', 'system');

-- ==============================
-- 用户（密码均为 vben123456）
-- ==============================
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `tenant_id`, `workspace_id`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (0, 'vben', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 1, 0, 'system', 'system', 'Vben', 'vben@example.com');

INSERT IGNORE INTO `user` (`id`, `username`, `password`, `tenant_id`, `workspace_id`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (1, 'admin', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 1, 0, 'system', 'system', 'Admin', 'admin@example.com');

INSERT IGNORE INTO `user` (`id`, `username`, `password`, `tenant_id`, `workspace_id`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (2, 'jack', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 1, 0, 'system', 'system', 'Jack', 'jack@example.com');

INSERT IGNORE INTO `user` (`id`, `username`, `password`, `tenant_id`, `workspace_id`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (3, 'teacher01', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 1, 0, 'system', 'system', '张老师', 'teacher01@example.com');

INSERT IGNORE INTO `user` (`id`, `username`, `password`, `tenant_id`, `workspace_id`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (4, 'student01', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 1, 0, 'system', 'system', '王小明', 'student01@example.com');

INSERT IGNORE INTO `user` (`id`, `username`, `password`, `tenant_id`, `workspace_id`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (5, 'parent01', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 1, 0, 'system', 'system', '李家长', 'parent01@example.com');

-- ==============================
-- 角色
-- ==============================
INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `workspace_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (0, 'super', '超级管理员', 1, 0, 1, '拥有系统所有权限', 0, 'system', 'system');

INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `workspace_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 'admin', '管理员', 1, 0, 1, '系统管理员', 0, 'system', 'system');

INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `workspace_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 'user', '普通用户', 1, 0, 1, '普通用户', 0, 'system', 'system');

INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `workspace_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 'teacher', '教师', 1, 0, 1, '教师角色，可管理教务和使用AI助手', 0, 'system', 'system');

INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `workspace_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 'student', '学生', 1, 0, 1, '学生角色，可使用学习/练习/考试/复习/AI助手', 0, 'system', 'system');

INSERT IGNORE INTO `role` (`id`, `code`, `name`, `tenant_id`, `workspace_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 'parent', '家长', 1, 0, 1, '家长角色，可查看数据中心和学习报告', 0, 'system', 'system');

-- ==============================
-- 工作空间
-- ==============================
INSERT IGNORE INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (0, 0, '默认空间', 1, 1, 'Admin', '13800000000', 'admin@example.com', 1, '系统默认工作空间', 0, 'system', 'system');

INSERT IGNORE INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 0, '技术部', 1, 1, 'Jack', '13900000001', 'jack@example.com', 1, '技术研发部门', 0, 'system', 'system');

INSERT IGNORE INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 0, '教务部', 1, 2, '张老师', '13900000002', 'teacher01@example.com', 1, '教育教学部门', 0, 'system', 'system');

INSERT IGNORE INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 0, '销售部', 1, 3, 'Sales', '13900000003', 'sales@example.com', 1, '市场营销部门', 0, 'system', 'system');

INSERT IGNORE INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 0, '财务部', 1, 4, 'Finance', '13900000004', 'finance@example.com', 1, '财务管理部门', 0, 'system', 'system');

-- ==============================
-- 用户-空间-角色 关联
-- ==============================
INSERT IGNORE INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (0, 0, 0, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 0, 0, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 0, 2, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 2, 3, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 2, 4, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 2, 5, 1, 1, 0, 'system', 'system');

-- ==============================
-- 菜单（默认系统）
-- ==============================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1, '系统管理', 'System', 'CATALOG', NULL, 1, '/system', '/system/user', 'lucide:settings', '', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (2, '用户管理', 'SystemUser', 'MENU', 1, 1, '/system/user', 'lucide:user', '/system/user/index', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (3, '角色管理', 'SystemRole', 'MENU', 1, 1, '/system/role', 'lucide:shield', '/system/role/index', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (4, '菜单管理', 'SystemMenu', 'MENU', 1, 1, '/system/menu', 'lucide:menu', '/system/menu/index', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (5, '租户管理', 'SystemTenant', 'MENU', 1, 1, '/system/tenant', 'lucide:building-2', '/system/tenant/index', TRUE, 1, 4, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (6, '工作空间', 'SystemWorkspace', 'MENU', 1, 1, '/system/workspace', 'lucide:layers', '/system/workspace/index', TRUE, 1, 5, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (7, '字典管理', 'SystemDict', 'MENU', 1, 1, '/system/dict', 'lucide:book-type', '/system/dict/index', TRUE, 1, 6, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (11, '权限码管理', 'SystemAuthCode', 'MENU', 1, 1, '/system/auth-code', 'lucide:shield-check', '/system/auth-code/index', TRUE, 1, 7, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (8, 'AI 平台', 'SystemAiPlatform', 'MENU', 1, 1, '/system/ai-platform', 'lucide:bot', '/agent/platform/list', TRUE, 1, 8, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (9, 'AI 模型', 'SystemAiModel', 'MENU', 1, 1, '/system/ai-model', 'lucide:cpu', '/agent/model/list', TRUE, 1, 8, 0, 'system', 'system');


-- ==============================
-- 菜单（业务模块）
-- ==============================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (10, '智能体', 'Agent', 'CATALOG', NULL, 1, '/agent', '/agent/admin/list', 'carbon:bot', '', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (11, 'Agent 管理', 'AgentAdmin', 'MENU', 10, 1, '/agent/admin/list', 'carbon:development', '/agent/admin/agent-list', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (12, '平台管理', 'AgentPlatform', 'MENU', 10, 1, '/agent/platform', 'carbon:bare-metal-server', '/agent/platform/list', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (13, '模型管理', 'AgentModel', 'MENU', 10, 1, '/agent/model', 'carbon:ibm-watson-machine-learning', '/agent/model/list', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (14, '对话调试', 'AgentChatConfig', 'MENU', 10, 1, '/agent/chat-config', 'carbon:chat', '/agent/chat-config/index', TRUE, 1, 4, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (15, '意图管理', 'AgentIntent', 'MENU', 10, 1, '/agent/intent', 'carbon:task', '/agent/intent/list', TRUE, 1, 5, 0, 'system', 'system');

-- 知识库管理 CATALOG
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (70, '知识库管理', 'Knowledge', 'CATALOG', NULL, 1, '/knowledge', '/knowledge/list', 'lucide:library', '', TRUE, 1, 70, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (71, '知识点管理', 'KnowledgeList', 'MENU', 70, 1, '/knowledge/list', 'carbon:concept', '/knowledge-engine/knowledge-list/list', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (72, '知识图谱', 'KnowledgeGraph', 'MENU', 70, 1, '/knowledge/graph', 'carbon:network-3', '/knowledge-engine/knowledge-graph/index', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (73, '文档管理', 'KnowledgeDocument', 'MENU', 70, 1, '/knowledge/document', 'carbon:document', '/knowledge-engine/document/list', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (74, '索引管理', 'KnowledgeIndex', 'MENU', 70, 1, '/knowledge/index', 'carbon:data-class', '/knowledge-engine/index-rebuild/list', TRUE, 1, 4, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (75, '知识关系', 'KnowledgeRelation', 'MENU', 70, 1, '/knowledge/relation', 'carbon:flow', '/knowledge-engine/knowledge-relation/index', TRUE, 1, 5, 0, 'system', 'system');

-- 教育管理 CATALOG
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (40, '教育管理', 'EduAdmin', 'CATALOG', NULL, 1, '/edu/admin', '/edu/subject', 'lucide:graduation-cap', '', TRUE, 1, 40, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (41, '学科管理', 'EduSubject', 'MENU', 40, 1, '/edu/subject', 'carbon:category', '/education-admin/subject/list', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (42, '教材管理', 'EduTextbook', 'MENU', 40, 1, '/edu/textbook', 'carbon:book', '/education-admin/textbook/list', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (43, '章节管理', 'EduChapter', 'MENU', 40, 1, '/edu/chapter', 'carbon:list', '/education-admin/chapter/list', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (44, '课程管理', 'EduCourse', 'MENU', 40, 1, '/edu/course', 'carbon:task', '/education-admin/course-admin/list', TRUE, 1, 4, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (45, '题库管理', 'EduQuestion', 'MENU', 40, 1, '/edu/question', 'carbon:quiz', '/education-admin/question-admin/list', TRUE, 1, 5, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (46, '考试管理', 'EduExam', 'MENU', 40, 1, '/edu/exam', 'carbon:exam', '/education-admin/exam-admin/list', TRUE, 1, 6, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (47, '学生管理', 'EduStudent', 'MENU', 40, 1, '/edu/student', 'carbon:user-avatar', '/education-admin/student/list', TRUE, 1, 7, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (48, '学习计划', 'EduPlan', 'MENU', 40, 1, '/edu/plan', 'carbon:plan', '/education-admin/plan/list', TRUE, 1, 8, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (49, '复习任务', 'EduReview', 'MENU', 40, 1, '/edu/review', 'carbon:review', '/education-admin/review/list', TRUE, 1, 9, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (50, '学情分析', 'EduAnalytics', 'MENU', 40, 1, '/edu/analytics', 'carbon:analytics', '/education-admin/analytics/index', TRUE, 1, 10, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (51, '资源管理', 'EduResource', 'MENU', 40, 1, '/edu/resource', 'carbon:folder', '/education-admin/resource-admin/list', TRUE, 1, 11, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (52, '错题管理', 'EduWrongQuestion', 'MENU', 40, 1, '/edu/wrong-question', 'carbon:error', '/education-admin/wrong-question/list', TRUE, 1, 12, 0, 'system', 'system');

-- 日常记录 CATALOG
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (80, '日常记录', 'Record', 'CATALOG', NULL, 1, '/record', '/record/profile', 'carbon:document', '', TRUE, 1, 80, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (81, '人物管理', 'RecordProfile', 'MENU', 80, 1, '/record/profile', 'carbon:user-profile', '/record/profile/list', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (82, '时间轴', 'RecordTimeline', 'MENU', 80, 1, '/record/timeline', 'carbon:time', '/record/timeline/list', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (83, '记录列表', 'RecordRecords', 'MENU', 80, 1, '/record/records', 'carbon:list', '/record/records/list', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (84, '标签管理', 'RecordTags', 'MENU', 80, 1, '/record/tags', 'carbon:tag', '/record/tags/list', TRUE, 1, 4, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (85, '媒体管理', 'RecordMedia', 'MENU', 80, 1, '/record/media', 'carbon:media', '/record/media/list', TRUE, 1, 5, 0, 'system', 'system');

-- 文件管理
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (90, '文件管理', 'FileManager', 'MENU', NULL, 1, '/file', 'carbon:folder', '/file/list', TRUE, 1, 90, 0, 'system', 'system');
-- ==============================
-- 角色-工作空间-菜单 关联（super 角色拥有全部菜单）
-- ==============================
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (0, 0, 1, 1, 1, 0, 'system', 'system'),
       (0, 0, 2, 1, 1, 0, 'system', 'system'),
       (0, 0, 3, 1, 1, 0, 'system', 'system'),
       (0, 0, 4, 1, 1, 0, 'system', 'system'),
       (0, 0, 5, 1, 1, 0, 'system', 'system'),
       (0, 0, 6, 1, 1, 0, 'system', 'system'),
       (0, 0, 7, 1, 1, 0, 'system', 'system'),
       (0, 0, 11, 1, 1, 0, 'system', 'system'),
       (0, 0, 8, 1, 1, 0, 'system', 'system'),
       (0, 0, 9, 1, 1, 0, 'system', 'system'),
       (0, 0, 100, 1, 1, 0, 'system', 'system'),
       (0, 0, 101, 1, 1, 0, 'system', 'system'),
       (0, 0, 102, 1, 1, 0, 'system', 'system'),
       (0, 0, 104, 1, 1, 0, 'system', 'system'),
       (0, 0, 1500, 1, 1, 0, 'system', 'system'),
       (0, 0, 1501, 1, 1, 0, 'system', 'system'),
       (0, 0, 1502, 1, 1, 0, 'system', 'system'),
       (0, 0, 1503, 1, 1, 0, 'system', 'system'),
       (0, 0, 1504, 1, 1, 0, 'system', 'system'),
       (0, 0, 1505, 1, 1, 0, 'system', 'system'),
       (0, 0, 1506, 1, 1, 0, 'system', 'system'),
       (0, 0, 1507, 1, 1, 0, 'system', 'system'),
       (0, 0, 10, 1, 1, 0, 'system', 'system'),
       (0, 0, 11, 1, 1, 0, 'system', 'system'),
       (0, 0, 12, 1, 1, 0, 'system', 'system'),
       (0, 0, 13, 1, 1, 0, 'system', 'system'),
       (0, 0, 14, 1, 1, 0, 'system', 'system'),
       (0, 0, 15, 1, 1, 0, 'system', 'system'),
       (0, 0, 40, 1, 1, 0, 'system', 'system'),
       (0, 0, 41, 1, 1, 0, 'system', 'system'),
       (0, 0, 42, 1, 1, 0, 'system', 'system'),
       (0, 0, 43, 1, 1, 0, 'system', 'system'),
       (0, 0, 44, 1, 1, 0, 'system', 'system'),
       (0, 0, 45, 1, 1, 0, 'system', 'system'),
       (0, 0, 46, 1, 1, 0, 'system', 'system'),
       (0, 0, 47, 1, 1, 0, 'system', 'system'),
       (0, 0, 48, 1, 1, 0, 'system', 'system'),
       (0, 0, 49, 1, 1, 0, 'system', 'system'),
       (0, 0, 50, 1, 1, 0, 'system', 'system'),
       (0, 0, 51, 1, 1, 0, 'system', 'system'),
       (0, 0, 52, 1, 1, 0, 'system', 'system'),
       (0, 0, 70, 1, 1, 0, 'system', 'system'),
       (0, 0, 71, 1, 1, 0, 'system', 'system'),
       (0, 0, 72, 1, 1, 0, 'system', 'system'),
       (0, 0, 73, 1, 1, 0, 'system', 'system'),
       (0, 0, 74, 1, 1, 0, 'system', 'system'),
       (0, 0, 75, 1, 1, 0, 'system', 'system'),
       (0, 0, 80, 1, 1, 0, 'system', 'system'),
       (0, 0, 81, 1, 1, 0, 'system', 'system'),
       (0, 0, 82, 1, 1, 0, 'system', 'system'),
       (0, 0, 83, 1, 1, 0, 'system', 'system'),
       (0, 0, 84, 1, 1, 0, 'system', 'system'),
       (0, 0, 85, 1, 1, 0, 'system', 'system'),
       (0, 0, 90, 1, 1, 0, 'system', 'system');

-- admin 角色拥有系统管理 + 智能体 + 知识库 + 教育管理 + 日常记录 + 文件管理菜单
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 0, 1, 1, 1, 0, 'system', 'system'),
       (1, 0, 2, 1, 1, 0, 'system', 'system'),
       (1, 0, 3, 1, 1, 0, 'system', 'system'),
       (1, 0, 4, 1, 1, 0, 'system', 'system'),
       (1, 0, 5, 1, 1, 0, 'system', 'system'),
       (1, 0, 6, 1, 1, 0, 'system', 'system'),
       (1, 0, 7, 1, 1, 0, 'system', 'system'),
       (1, 0, 11, 1, 1, 0, 'system', 'system'),
       (1, 0, 8, 1, 1, 0, 'system', 'system'),
       (1, 0, 9, 1, 1, 0, 'system', 'system'),
       (1, 0, 10, 1, 1, 0, 'system', 'system'),
       (1, 0, 11, 1, 1, 0, 'system', 'system'),
       (1, 0, 12, 1, 1, 0, 'system', 'system'),
       (1, 0, 13, 1, 1, 0, 'system', 'system'),
       (1, 0, 14, 1, 1, 0, 'system', 'system'),
       (1, 0, 15, 1, 1, 0, 'system', 'system'),
       (1, 0, 40, 1, 1, 0, 'system', 'system'),
       (1, 0, 41, 1, 1, 0, 'system', 'system'),
       (1, 0, 42, 1, 1, 0, 'system', 'system'),
       (1, 0, 43, 1, 1, 0, 'system', 'system'),
       (1, 0, 44, 1, 1, 0, 'system', 'system'),
       (1, 0, 45, 1, 1, 0, 'system', 'system'),
       (1, 0, 46, 1, 1, 0, 'system', 'system'),
       (1, 0, 47, 1, 1, 0, 'system', 'system'),
       (1, 0, 48, 1, 1, 0, 'system', 'system'),
       (1, 0, 49, 1, 1, 0, 'system', 'system'),
       (1, 0, 50, 1, 1, 0, 'system', 'system'),
       (1, 0, 51, 1, 1, 0, 'system', 'system'),
       (1, 0, 52, 1, 1, 0, 'system', 'system'),
       (1, 0, 70, 1, 1, 0, 'system', 'system'),
       (1, 0, 71, 1, 1, 0, 'system', 'system'),
       (1, 0, 72, 1, 1, 0, 'system', 'system'),
       (1, 0, 73, 1, 1, 0, 'system', 'system'),
       (1, 0, 74, 1, 1, 0, 'system', 'system'),
       (1, 0, 75, 1, 1, 0, 'system', 'system'),
       (1, 0, 80, 1, 1, 0, 'system', 'system'),
       (1, 0, 81, 1, 1, 0, 'system', 'system'),
       (1, 0, 82, 1, 1, 0, 'system', 'system'),
       (1, 0, 83, 1, 1, 0, 'system', 'system'),
       (1, 0, 84, 1, 1, 0, 'system', 'system'),
       (1, 0, 85, 1, 1, 0, 'system', 'system'),
       (1, 0, 90, 1, 1, 0, 'system', 'system');

-- user 角色拥有教育中心菜单
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 0, 1500, 1, 1, 0, 'system', 'system'),
       (2, 0, 1501, 1, 1, 0, 'system', 'system'),
       (2, 0, 1502, 1, 1, 0, 'system', 'system'),
       (2, 0, 1503, 1, 1, 0, 'system', 'system'),
       (2, 0, 1504, 1, 1, 0, 'system', 'system'),
       (2, 0, 1506, 1, 1, 0, 'system', 'system'),
       (2, 0, 1507, 1, 1, 0, 'system', 'system');

-- ==============================
-- 权限码（系统管理）
-- ==============================
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (1, 'system:user:list', '查看用户列表', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (2, 'system:user:create', '创建用户', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (3, 'system:user:update', '更新用户', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (4, 'system:user:delete', '删除用户', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (5, 'system:role:list', '查看角色列表', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (6, 'system:role:create', '创建角色', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (7, 'system:role:update', '更新角色', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (8, 'system:role:delete', '删除角色', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (9, 'system:menu:list', '查看菜单列表', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (10, 'system:menu:create', '创建菜单', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (11, 'system:menu:update', '更新菜单', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (12, 'system:menu:delete', '删除菜单', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (13, 'system:tenant:list', '查看租户列表', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (14, 'system:tenant:create', '创建租户', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (15, 'system:tenant:update', '更新租户', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (16, 'system:tenant:delete', '删除租户', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (17, 'system:workspace:list', '查看工作空间', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (18, 'system:workspace:create', '创建工作空间', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (19, 'system:workspace:update', '更新工作空间', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (20, 'system:workspace:delete', '删除工作空间', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (21, 'system:dict:list', '查看字典', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (22, 'system:dict:create', '创建字典', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (23, 'system:dict:update', '更新字典', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (24, 'system:dict:delete', '删除字典', 0, 1, 0, 1, 'system', 'system');

-- ==============================
-- 权限码（智能体）
-- ==============================
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (25, 'agent:admin:list', '查看 Agent 列表', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (26, 'agent:admin:create', '创建 Agent', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (27, 'agent:admin:edit', '编辑 Agent', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (28, 'agent:admin:delete', '删除 Agent', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (29, 'agent:platform:list', '查看平台', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (30, 'agent:platform:create', '创建平台', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (31, 'agent:platform:edit', '编辑平台', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (32, 'agent:platform:delete', '删除平台', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (33, 'agent:platform:set-default', '设置默认平台', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (34, 'agent:model:list', '查看模型', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (35, 'agent:model:create', '创建模型', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (36, 'agent:model:edit', '编辑模型', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (37, 'agent:model:delete', '删除模型', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (38, 'agent:model:set-default', '设置默认模型', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (39, 'agent:chat:config', '对话调试', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (40, 'agent:intent:list', '查看意图', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (41, 'agent:intent:create', '创建意图', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (42, 'agent:intent:delete', '删除意图', 0, 1, 0, 1, 'system', 'system');

-- ==============================
-- 权限码（知识库管理）
-- ==============================
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (43, 'knowledge:list', '查看知识点', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (44, 'knowledge:create', '创建知识点', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (45, 'knowledge:edit', '编辑知识点', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (46, 'knowledge:delete', '删除知识点', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (47, 'knowledge:graph', '查看知识图谱', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (48, 'knowledge:document:list', '查看文档', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (49, 'knowledge:document:upload', '上传文档', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (50, 'knowledge:document:delete', '删除文档', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (51, 'knowledge:index:rebuild', '重建索引', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (52, 'knowledge:relation', '管理知识关系', 0, 1, 0, 1, 'system', 'system');

-- ==============================
-- 权限码（教育管理）
-- ==============================
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (53, 'edu:subject:list', '查看学科', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (54, 'edu:subject:create', '创建学科', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (55, 'edu:subject:edit', '编辑学科', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (56, 'edu:subject:delete', '删除学科', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (57, 'edu:textbook:list', '查看教材', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (58, 'edu:textbook:create', '创建教材', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (59, 'edu:textbook:edit', '编辑教材', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (60, 'edu:textbook:delete', '删除教材', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (61, 'edu:chapter:list', '查看章节', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (62, 'edu:chapter:create', '创建章节', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (63, 'edu:chapter:edit', '编辑章节', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (64, 'edu:chapter:delete', '删除章节', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (65, 'edu:course:list', '查看课程', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (66, 'edu:course:create', '创建课程', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (67, 'edu:course:edit', '编辑课程', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (68, 'edu:course:delete', '删除课程', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (69, 'edu:question:list', '查看题目', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (70, 'edu:question:create', '创建题目', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (71, 'edu:question:edit', '编辑题目', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (72, 'edu:question:delete', '删除题目', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (73, 'edu:exam:list', '查看考试', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (74, 'edu:exam:create', '创建考试', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (75, 'edu:exam:edit', '编辑考试', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (76, 'edu:exam:delete', '删除考试', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (77, 'edu:exam:publish', '发布考试', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (78, 'edu:student:list', '查看学生', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (79, 'edu:resource:list', '查看资源', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (80, 'edu:resource:upload', '上传资源', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (81, 'edu:resource:delete', '删除资源', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (82, 'edu:plan:list', '查看学习计划', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (83, 'edu:review:list', '查看复习任务', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (84, 'edu:analytics', '查看学情分析', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (85, 'edu:wrong-question', '查看错题管理', 0, 1, 0, 1, 'system', 'system');

-- ==============================
-- 权限码（日常记录）
-- ==============================
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (86, 'record:profile:list', '查看人物', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (87, 'record:profile:create', '创建人物', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (88, 'record:profile:edit', '编辑人物', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (89, 'record:profile:delete', '删除人物', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (90, 'record:timeline:list', '查看时间轴', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (91, 'record:timeline:create', '创建事件', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (92, 'record:timeline:delete', '删除事件', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (93, 'record:tags:list', '查看标签', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (94, 'record:media:list', '查看媒体', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (95, 'record:media:upload', '上传媒体', 0, 1, 0, 1, 'system', 'system');

-- ==============================
-- 权限码（文件管理）
-- ==============================
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (96, 'file:upload', '上传文件', 0, 1, 0, 1, 'system', 'system');
INSERT IGNORE INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `workspace_id`, `status`, `create_by`, `update_by`)
VALUES (97, 'file:delete', '删除文件', 0, 1, 0, 1, 'system', 'system');

