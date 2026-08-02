-- ============================================
-- Data: auth — 租户/用户/角色/子租户/菜单/权限
-- ============================================

-- 默认租户
INSERT IGNORE INTO `auth_tenant` (`id`, `code`, `name`, `contact_name`, `contact_phone`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 'default', '默认租户', 'Admin', '13800000000', 1, 0, 'system', 'system');

-- 默认租户配额
INSERT IGNORE INTO `auth_tenant_quota` (`tenant_id`, `max_agent_count`, `max_token_per_day`, `max_storage_mb`, `max_user_count`, `status`)
VALUES (1, 50, 5000000, 5120, 500, 1);

-- ==============================
-- 用户（密码均为 123456）
-- ==============================
INSERT IGNORE INTO `auth_user` (`id`, `username`, `password`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (1, 'vben', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 'system', 'system', 'Vben', 'vben@example.com');

INSERT IGNORE INTO `auth_user` (`id`, `username`, `password`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (2, 'admin', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 'system', 'system', 'Admin', 'admin@example.com');

INSERT IGNORE INTO `auth_user` (`id`, `username`, `password`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (3, 'jack', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 'system', 'system', 'Jack', 'jack@example.com');

INSERT IGNORE INTO `auth_user` (`id`, `username`, `password`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (4, 'teacher01', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 'system', 'system', '张老师', 'teacher01@example.com');

INSERT IGNORE INTO `auth_user` (`id`, `username`, `password`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (5, 'student01', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 'system', 'system', '王小明', 'student01@example.com');

INSERT IGNORE INTO `auth_user` (`id`, `username`, `password`, `status`, `del_flag`, `create_by`, `update_by`, `nick_name`, `email`)
VALUES (6, 'parent01', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, 0, 'system', 'system', '李家长', 'parent01@example.com');

-- ==============================
-- 角色
-- ==============================
INSERT IGNORE INTO `auth_role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 'super', '超级管理员', 1, 1, '拥有系统所有权限', 0, 'system', 'system');

INSERT IGNORE INTO `auth_role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 'admin', '管理员', 1, 1, '系统管理员', 0, 'system', 'system');

INSERT IGNORE INTO `auth_role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 'user', '普通用户', 1, 1, '普通用户', 0, 'system', 'system');

INSERT IGNORE INTO `auth_role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 'teacher', '教师', 3, 1, '教师角色，可管理教务和使用AI助手', 0, 'system', 'system');

INSERT IGNORE INTO `auth_role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 'student', '学生', 3, 1, '学生角色，可使用学习/练习/考试/复习/AI助手', 0, 'system', 'system');

INSERT IGNORE INTO `auth_role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (6, 'parent', '家长', 3, 1, '家长角色，可查看数据中心和学习报告', 0, 'system', 'system');

-- ==============================
-- 子租户（以默认租户为父节点的子租户）
-- ==============================
INSERT IGNORE INTO `auth_tenant` (`id`, `parent_id`, `code`, `name`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 1, 'sub-tenant-a', '子租户A', 1, 'Admin', '13800000000', 'admin@example.com', 1, '默认租户下属子租户A，用于办公管理', 0, 'system', 'system');

INSERT IGNORE INTO `auth_tenant` (`id`, `parent_id`, `code`, `name`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 1, 'sub-tenant-b', '子租户B', 2, 'Jack', '13900000001', 'jack@example.com', 1, '默认租户下属子租户B，用于技术研发', 0, 'system', 'system');

INSERT IGNORE INTO `auth_tenant` (`id`, `parent_id`, `code`, `name`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 1, 'sub-tenant-c', '子租户C', 3, '张老师', '13900000002', 'teacher@example.com', 1, '默认租户下属子租户C，用于教育教学', 0, 'system', 'system');

INSERT IGNORE INTO `auth_tenant` (`id`, `parent_id`, `code`, `name`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 1, 'sub-tenant-d', '子租户D', 4, 'Sales', '13900000003', 'sales@example.com', 1, '默认租户下属子租户D，用于市场营销', 0, 'system', 'system');

INSERT IGNORE INTO `auth_tenant` (`id`, `parent_id`, `code`, `name`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (6, 1, 'sub-tenant-e', '子租户E', 5, 'Finance', '13900000004', 'finance@example.com', 1, '默认租户下属子租户E，用于财务管理', 0, 'system', 'system');

-- ==============================
-- 用户-空间-角色 关联
-- ==============================
INSERT IGNORE INTO `auth_user_scope_role` (`user_id`, `tenant_id`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 1, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `auth_user_scope_role` (`user_id`, `tenant_id`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 1, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `auth_user_scope_role` (`user_id`, `tenant_id`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 1, 3, 1, 0, 'system', 'system');

INSERT IGNORE INTO `auth_user_scope_role` (`user_id`, `tenant_id`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 3, 4, 1, 0, 'system', 'system');

INSERT IGNORE INTO `auth_user_scope_role` (`user_id`, `tenant_id`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 3, 5, 1, 0, 'system', 'system');

INSERT IGNORE INTO `auth_user_scope_role` (`user_id`, `tenant_id`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (6, 3, 6, 1, 0, 'system', 'system');

-- ==============================
-- 菜单（默认系统）
-- ==============================
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1, '系统设置', 'SystemSettings', 'CATALOG', NULL, 1, '/system', '/system/user', 'lucide:settings', '', TRUE, 1, 9998, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (2, '用户管理', 'SystemUser', 'MENU', 1, 1, '/system/user', 'lucide:user', '/system/user/index', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (3, '角色管理', 'SystemRole', 'MENU', 1, 1, '/system/role', 'lucide:shield', '/system/role/index', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (4, '菜单管理', 'SystemMenu', 'MENU', 1, 1, '/system/menu', 'lucide:menu', '/system/menu/index', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (5, '租户管理', 'SystemTenant', 'MENU', 1, 1, '/system/tenant', 'lucide:building-2', '/system/tenant/index', TRUE, 1, 4, 0, 'system', 'system');


INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (7, '字典管理', 'SystemDict', 'MENU', 1, 1, '/system/dict', 'lucide:book-type', '/system/dict/index', TRUE, 1, 5, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (11, '权限码管理', 'SystemAuthCode', 'MENU', 1, 1, '/system/auth-code', 'lucide:shield-check', '/system/auth-code/index', TRUE, 1, 6, 0, 'system', 'system');

-- ==============================
-- 菜单（业务模块）
-- ==============================
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (10, 'AI管理', 'PlatformManager', 'CATALOG', NULL, 1, '/platform', '/agent/definition/list', 'carbon:bot', '', TRUE, 1, 10, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (16, 'Agent', 'AgentDefinition', 'MENU', 10, 1, '/agent/definition/list', 'carbon:development', '/agent/admin/agent-list', TRUE, 1, 1, 0, 'system', 'system');


-- Agent 编辑页隐藏路由（查看/编辑跳转用）
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1100, 'Agent 编辑', 'AgentDefinitionEdit', 'MENU', 10, 1, '/agent/admin/edit', 'carbon:development', '/agent/admin/agent-edit', FALSE, 1, 99, 0, 'system', 'system');
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (12, '平台管理', 'AgentPlatform', 'MENU', 10, 1, '/agent/platform', 'carbon:bare-metal-server', '/agent/platform/list', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (13, '模型管理', 'AgentModel', 'MENU', 10, 1, '/agent/model', 'carbon:ibm-watson-machine-learning', '/agent/model/list', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (14, '对话调试', 'AgentChatConfig', 'MENU', 10, 1, '/agent/chat-config', 'carbon:chat', '/agent/chat-config/index', TRUE, 1, 4, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (15, '意图管理', 'AgentIntent', 'MENU', 10, 1, '/agent/intent', 'carbon:task', '/agent/intent/list', TRUE, 1, 5, 0, 'system', 'system');

-- 知识库管理 CATALOG
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (70, '知识平台', 'KnowledgeEngine', 'CATALOG', NULL, 1, '/knowledge', '/knowledge/workbench', 'lucide:brain-circuit', '', TRUE, 1, 15, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (71, '知识资产', 'KnowledgeList', 'MENU', 70, 1, '/knowledge/assets', 'lucide:boxes', '/knowledge/assets/index', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (72, '图谱洞察', 'KnowledgeGraph', 'MENU', 70, 1, '/knowledge/graph', 'lucide:network', '/knowledge/graph/index', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (73, '企业知识工作台', 'KnowledgeDocument', 'MENU', 70, 1, '/knowledge/workbench', 'lucide:layout-dashboard', '/knowledge/workbench/index', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (74, '索引与任务', 'KnowledgeIndex', 'MENU', 70, 1, '/knowledge/index', 'lucide:database-zap', '/knowledge/index/index', TRUE, 1, 7, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (76, '空间管理', 'KnowledgeSpace', 'MENU', 70, 1, '/knowledge/spaces', 'lucide:layers-3', '/knowledge/spaces/index', TRUE, 1, 2, 0, 'system', 'system');
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (77, '文档中心', 'KnowledgeDocuments', 'MENU', 70, 1, '/knowledge/documents', 'lucide:file-stack', '/knowledge/documents/index', TRUE, 1, 4, 0, 'system', 'system');
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (78, '检索实验室', 'KnowledgeSearch', 'MENU', 70, 1, '/knowledge/search', 'lucide:search-check', '/knowledge/search/index', TRUE, 1, 6, 0, 'system', 'system');
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (79, '系统运维', 'KnowledgeOperations', 'MENU', 70, 1, '/knowledge/operations', 'lucide:server-cog', '/knowledge/operations/index', TRUE, 1, 8, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (86, '评测中心', 'KnowledgeEvaluations', 'MENU', 70, 1, '/knowledge/evaluations', 'lucide:chart-no-axes-combined', '/knowledge/evaluations/index', TRUE, 1, 10, 0, 'system', 'system');


-- Keep knowledge-platform menu labels readable for existing databases.
UPDATE `auth_menu` SET `name` = '知识平台', `path` = '/knowledge', `redirect` = '/knowledge/workbench' WHERE `code` = 'KnowledgeEngine' AND `tenant_id` = 1;
UPDATE `auth_menu` SET `name` = '企业知识工作台', `path` = '/knowledge/workbench', `component` = '/knowledge/workbench/index', `order` = 1 WHERE `code` = 'KnowledgeDocument' AND `tenant_id` = 1;
UPDATE `auth_menu` SET `name` = '空间管理', `path` = '/knowledge/spaces', `component` = '/knowledge/spaces/index', `order` = 2 WHERE `code` = 'KnowledgeSpace' AND `tenant_id` = 1;
UPDATE `auth_menu` SET `name` = '知识资产', `path` = '/knowledge/assets', `component` = '/knowledge/assets/index', `order` = 3 WHERE `code` = 'KnowledgeList' AND `tenant_id` = 1;
UPDATE `auth_menu` SET `name` = '文档中心', `path` = '/knowledge/documents', `component` = '/knowledge/documents/index', `order` = 4 WHERE `code` = 'KnowledgeDocuments' AND `tenant_id` = 1;
UPDATE `auth_menu` SET `name` = '图谱洞察', `path` = '/knowledge/graph', `component` = '/knowledge/graph/index', `order` = 5 WHERE `code` = 'KnowledgeGraph' AND `tenant_id` = 1;
-- Relationship editing is embedded in the graph insight canvas. Remove the
-- retired standalone menu and all tenant/role bindings, including legacy
-- cloned menu rows, so it cannot create a stale blank tab after an upgrade.
DELETE FROM `auth_role_scope_menu`
WHERE EXISTS (
    SELECT 1 FROM `auth_menu` m
    WHERE m.`id` = auth_role_scope_menu.`menu_id`
      AND m.`code` = 'KnowledgeRelation'
);
DELETE FROM `auth_tenant_menu`
WHERE EXISTS (
    SELECT 1 FROM `auth_menu` m
    WHERE m.`id` = auth_tenant_menu.`menu_id`
      AND m.`code` = 'KnowledgeRelation'
);
DELETE FROM `auth_menu` WHERE `code` = 'KnowledgeRelation';
UPDATE `auth_menu` SET `name` = '检索实验室', `path` = '/knowledge/search', `component` = '/knowledge/search/index', `order` = 7 WHERE `code` = 'KnowledgeSearch' AND `tenant_id` = 1;
UPDATE `auth_menu` SET `name` = '索引与任务', `path` = '/knowledge/index', `component` = '/knowledge/index/index', `order` = 8 WHERE `code` = 'KnowledgeIndex' AND `tenant_id` = 1;
UPDATE `auth_menu` SET `name` = '系统运维', `path` = '/knowledge/operations', `component` = '/knowledge/operations/index', `order` = 9 WHERE `code` = 'KnowledgeOperations' AND `tenant_id` = 1;
UPDATE `auth_menu` SET `name` = '评测中心', `path` = '/knowledge/evaluations', `component` = '/knowledge/evaluations/index', `order` = 10 WHERE `code` = 'KnowledgeEvaluations' AND `tenant_id` = 1;

-- 修复历史版本中 KnowledgeEvaluations 与 Record 共用菜单 ID 80 的数据冲突。
-- 先迁移旧的评测菜单及其授权，再插入真正的日常记录根菜单，保证两棵菜单树互不串联。
UPDATE `auth_role_scope_menu`
SET `menu_id` = 86
WHERE `tenant_id` = 1
  AND `menu_id` = 80
  AND EXISTS (SELECT 1 FROM `auth_menu` WHERE `id` = 80 AND `tenant_id` = 1 AND `code` = 'KnowledgeEvaluations');
UPDATE `auth_tenant_menu`
SET `menu_id` = 86
WHERE `tenant_id` = 1
  AND `menu_id` = 80
  AND EXISTS (SELECT 1 FROM `auth_menu` WHERE `id` = 80 AND `tenant_id` = 1 AND `code` = 'KnowledgeEvaluations');
DELETE FROM `auth_menu`
WHERE `id` = 80 AND `tenant_id` = 1 AND `code` = 'KnowledgeEvaluations';

-- 子租户菜单使用 source_menu_id + tenant_id * 100000 的克隆规则，同步修复历史克隆数据。
UPDATE `auth_role_scope_menu` r
SET `menu_id` = 86 + (`tenant_id` * 100000)
WHERE r.`menu_id` = 80 + (r.`tenant_id` * 100000)
  AND EXISTS (
      SELECT 1 FROM `auth_menu` m
      WHERE m.`id` = r.`menu_id`
        AND m.`tenant_id` = r.`tenant_id`
        AND m.`code` = 'KnowledgeEvaluations'
  );
UPDATE `auth_tenant_menu` tm
SET `menu_id` = 86 + (tm.`tenant_id` * 100000)
WHERE tm.`menu_id` = 80 + (tm.`tenant_id` * 100000)
  AND EXISTS (
      SELECT 1 FROM `auth_menu` m
      WHERE m.`id` = tm.`menu_id`
        AND m.`tenant_id` = tm.`tenant_id`
        AND m.`code` = 'KnowledgeEvaluations'
  );
DELETE FROM `auth_menu`
WHERE `id` = 80 + (`tenant_id` * 100000)
  AND `tenant_id` <> 1
  AND `code` = 'KnowledgeEvaluations';

-- ============================================
-- 教育空间 CATALOG（parent of 教育中心 + 学生端）
-- ============================================
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1500, '教育空间', 'EducationCenter', 'CATALOG', NULL, 1, '/education-center', '/learning/course', 'lucide:graduation-cap', '', '教育全场景统一入口', TRUE, 1, 20, 0, 'system', 'system');

-- 教育管理 CATALOG
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (40, '教育中心', 'EduAdmin', 'CATALOG', 1500, 1, '/edu/admin', '/edu/subject', 'carbon:settings', '', TRUE, 1, 20, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (41, '学科管理', 'EduSubject', 'MENU', 40, 1, '/edu/subject', 'carbon:category', '/education-admin/subject/list', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (42, '教材管理', 'EduTextbook', 'MENU', 40, 1, '/edu/textbook', 'carbon:book', '/education-admin/textbook/list', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (43, '章节管理', 'EduChapter', 'MENU', 40, 1, '/edu/chapter', 'carbon:list', '/education-admin/chapter/list', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (44, '课程管理', 'EduCourse', 'MENU', 40, 1, '/edu/course', 'carbon:task', '/education-admin/course-admin/list', TRUE, 1, 4, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (45, '题库管理', 'EduQuestion', 'MENU', 40, 1, '/edu/question', 'carbon:quiz', '/education-admin/question-admin/list', TRUE, 1, 5, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (46, '考试管理', 'EduExam', 'MENU', 40, 1, '/edu/exam', 'carbon:exam', '/education-admin/exam-admin/list', TRUE, 1, 6, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (47, '学生管理', 'EduStudent', 'MENU', 40, 1, '/edu/student', 'carbon:user-avatar', '/education-admin/student/list', TRUE, 1, 7, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (48, '学习计划', 'EduPlan', 'MENU', 40, 1, '/edu/plan', 'carbon:plan', '/education-admin/plan/list', TRUE, 1, 8, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (49, '复习任务', 'EduReview', 'MENU', 40, 1, '/edu/review', 'carbon:review', '/education-admin/review/list', TRUE, 1, 9, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (50, '学情分析', 'EduAnalytics', 'MENU', 40, 1, '/edu/analytics', 'carbon:analytics', '/education-admin/analytics/index', TRUE, 1, 10, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (51, '资源管理', 'EduResource', 'MENU', 40, 1, '/edu/resource', 'carbon:folder', '/education-admin/resource-admin/list', TRUE, 1, 11, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (52, '错题管理', 'EduWrongQuestion', 'MENU', 40, 1, '/edu/wrong-question', 'carbon:error', '/education-admin/wrong-question/list', TRUE, 1, 12, 0, 'system', 'system');

-- ============================================
-- 教育空间子菜单（学习/题库/考试/复习/AI助手/数据分析）
-- ============================================
-- 学习
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1501, '课程学习', 'EduLearningCourse', 'MENU', 1500, 1, '/learning/course', 'lucide:book', '/learning/course/list', '课程学习', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1502, '知识浏览', 'EduLearningKnowledge', 'MENU', 1500, 1, '/learning/knowledge', 'lucide:brain', '/learning/knowledge/list', '知识浏览', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1503, '学习计划', 'EduLearningPlan', 'MENU', 1500, 1, '/learning/plan', 'lucide:calendar-check', '/learning/plan/list', '学习计划', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1508, '学习资源', 'EduLearningResource', 'MENU', 1500, 1, '/learning/resource', 'lucide:folder-open', '/learning/resource/list', '学习资源', TRUE, 1, 4, 0, 'system', 'system');

-- 题库练习
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1510, '题库练习', 'EduPracticeQuestion', 'MENU', 1500, 1, '/practice/question', 'lucide:list-checks', '/practice/question/list', '题库练习', TRUE, 1, 5, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1511, '错题本', 'EduPracticeWrong', 'MENU', 1500, 1, '/practice/wrong', 'lucide:x-circle', '/practice/wrong-question/list', '错题本', TRUE, 1, 6, 0, 'system', 'system');

-- 考试
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1520, '在线考试', 'EduExamList', 'MENU', 1500, 1, '/exam/list', 'lucide:file-text', '/exam/exam-list/list', '在线考试', TRUE, 1, 7, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1521, 'AI 组卷', 'EduExamAi', 'MENU', 1500, 1, '/exam/ai-exam', 'lucide:sparkles', '/exam/ai-exam/index', 'AI组卷', TRUE, 1, 8, 0, 'system', 'system');

-- 复习
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1530, '今日复习', 'EduReviewToday', 'MENU', 1500, 1, '/review/today', 'lucide:calendar-days', '/review/today/list', '今日复习', TRUE, 1, 9, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1531, '复习历史', 'EduReviewHistory', 'MENU', 1500, 1, '/review/history', 'lucide:history', '/review/history/list', '复习历史', TRUE, 1, 10, 0, 'system', 'system');

-- AI 助手
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1540, 'AI 讲解', 'EduAiTeacher', 'MENU', 1500, 1, '/ai-tutor/teacher', 'lucide:graduation-cap', '/ai-tutor/teacher/index', 'AI讲解', TRUE, 1, 11, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1541, 'AI 出题', 'EduAiPractice', 'MENU', 1500, 1, '/ai-tutor/practice', 'lucide:pencil-ruler', '/ai-tutor/practice/index', 'AI出题', TRUE, 1, 12, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1542, 'AI 规划', 'EduAiPlanner', 'MENU', 1500, 1, '/ai-tutor/planner', 'lucide:route', '/ai-tutor/planner/index', 'AI规划', TRUE, 1, 13, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1543, 'AI 对话', 'EduAiChat', 'MENU', 1500, 1, '/ai-tutor/chat', 'lucide:message-circle', '/ai-tutor/chat/index', 'AI对话', TRUE, 1, 14, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1544, 'AI 报告', 'EduAiReport', 'MENU', 1500, 1, '/ai-tutor/report', 'lucide:file-output', '/ai-tutor/report-gen/index', 'AI报告', TRUE, 1, 15, 0, 'system', 'system');

-- 数据分析
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1550, '学习报告', 'EduAnalyticsReport', 'MENU', 1500, 1, '/analytics-center/report', 'lucide:file-bar-chart', '/analytics/report/index', '学习报告', TRUE, 1, 16, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1551, '能力雷达', 'EduAnalyticsRadar', 'MENU', 1500, 1, '/analytics-center/radar', 'lucide:radar', '/analytics/ability-radar/index', '能力雷达', TRUE, 1, 17, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1552, '学习趋势', 'EduAnalyticsTrend', 'MENU', 1500, 1, '/analytics-center/trend', 'lucide:trending-up', '/analytics/trend/index', '学习趋势', TRUE, 1, 18, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1553, '薄弱分析', 'EduAnalyticsWeak', 'MENU', 1500, 1, '/analytics-center/weak', 'lucide:alert-triangle', '/analytics/weak-points/list', '薄弱分析', TRUE, 1, 19, 0, 'system', 'system');

-- 教育空间隐藏路由（菜单不可见，但程序导航可用）
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1560, '课程详情', 'EduLearningCourseDetail', 'MENU', 1500, 1, '/learning/course/:id', 'lucide:book', '/learning/course/detail', '课程详情', FALSE, 1, 99, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1561, '课程学习', 'EduLearningCourseLearn', 'MENU', 1500, 1, '/learning/course/:courseId/learn', 'lucide:book', '/learning/course/learn', '课程学习页面', FALSE, 1, 99, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1562, '知识点详情', 'EduLearningKnowledgeDetail', 'MENU', 1500, 1, '/learning/knowledge/:id', 'lucide:brain', '/learning/knowledge/detail', '知识点详情', FALSE, 1, 99, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1563, '计划详情', 'EduLearningPlanDetail', 'MENU', 1500, 1, '/learning/plan/:id', 'lucide:calendar-check', '/learning/plan/detail', '计划详情', FALSE, 1, 99, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1564, '答题中', 'EduPracticeDoing', 'MENU', 1500, 1, '/practice/question/:id', 'lucide:list-checks', '/practice/question/practice', '答题中', FALSE, 1, 99, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1565, '考试中', 'EduExamTake', 'MENU', 1500, 1, '/exam/take/:id', 'lucide:file-text', '/exam/exam-list/take', '考试中', FALSE, 1, 99, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (1566, '考试结果', 'EduExamResult', 'MENU', 1500, 1, '/exam/result/:id', 'lucide:file-text', '/exam/exam-list/result', '考试结果', FALSE, 1, 99, 0, 'system', 'system');

-- 日常记录 CATALOG
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (80, '日常记录', 'Record', 'CATALOG', NULL, 1, '/record', '/record/profile', 'carbon:notebook', '', TRUE, 1, 30, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (81, '人物管理', 'RecordProfile', 'MENU', 80, 1, '/record/profile', 'carbon:user-profile', '/record/profile/list', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (82, '时间轴', 'RecordTimeline', 'MENU', 80, 1, '/record/timeline', 'carbon:time', '/record/timeline/list', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (83, '记录内容', 'RecordRecords', 'MENU', 80, 1, '/record/records', 'carbon:document', '/record/records/list', TRUE, 1, 3, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (84, '标签管理', 'RecordTags', 'MENU', 80, 1, '/record/tags', 'carbon:tag', '/record/tags/list', TRUE, 1, 4, 0, 'system', 'system');

INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (85, '附件管理', 'RecordMedia', 'MENU', 80, 1, '/record/media', 'carbon:attachment', '/record/media/list', TRUE, 1, 5, 0, 'system', 'system');

-- 文件管理
INSERT IGNORE INTO `auth_menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (90, '文件管理', 'FileManager', 'MENU', NULL, 1, '/file', 'carbon:folder', '/file/list', TRUE, 1, 90, 0, 'system', 'system');
-- ==============================
-- 角色-子租户-菜单 关联（super 角色 role_id=1 拥有全部菜单）
-- ==============================
INSERT IGNORE INTO `auth_role_scope_menu` (`role_id`, `tenant_id`, `menu_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 1, 1, 1, 0, 'system', 'system'),
(1, 1, 2, 1, 0, 'system', 'system'),
(1, 1, 3, 1, 0, 'system', 'system'),
(1, 1, 4, 1, 0, 'system', 'system'),
(1, 1, 5, 1, 0, 'system', 'system'),
(1, 1, 6, 1, 0, 'system', 'system'),
(1, 1, 7, 1, 0, 'system', 'system'),
(1, 1, 11, 1, 0, 'system', 'system'),
(1, 1, 100, 1, 0, 'system', 'system'),
(1, 1, 101, 1, 0, 'system', 'system'),
(1, 1, 102, 1, 0, 'system', 'system'),
(1, 1, 104, 1, 0, 'system', 'system'),
(1, 1, 1500, 1, 0, 'system', 'system'),
(1, 1, 1501, 1, 0, 'system', 'system'),
(1, 1, 1502, 1, 0, 'system', 'system'),
(1, 1, 1503, 1, 0, 'system', 'system'),
(1, 1, 1508, 1, 0, 'system', 'system'),
(1, 1, 1510, 1, 0, 'system', 'system'),
(1, 1, 1511, 1, 0, 'system', 'system'),
(1, 1, 1520, 1, 0, 'system', 'system'),
(1, 1, 1521, 1, 0, 'system', 'system'),
(1, 1, 1530, 1, 0, 'system', 'system'),
(1, 1, 1531, 1, 0, 'system', 'system'),
(1, 1, 1540, 1, 0, 'system', 'system'),
(1, 1, 1541, 1, 0, 'system', 'system'),
(1, 1, 1542, 1, 0, 'system', 'system'),
(1, 1, 1543, 1, 0, 'system', 'system'),
(1, 1, 1544, 1, 0, 'system', 'system'),
(1, 1, 1550, 1, 0, 'system', 'system'),
(1, 1, 1551, 1, 0, 'system', 'system'),
(1, 1, 1552, 1, 0, 'system', 'system'),
(1, 1, 1553, 1, 0, 'system', 'system'),
(1, 1, 1560, 1, 0, 'system', 'system'),
(1, 1, 1561, 1, 0, 'system', 'system'),
(1, 1, 1562, 1, 0, 'system', 'system'),
(1, 1, 1563, 1, 0, 'system', 'system'),
(1, 1, 1564, 1, 0, 'system', 'system'),
(1, 1, 1565, 1, 0, 'system', 'system'),
(1, 1, 1566, 1, 0, 'system', 'system'),
(1, 1, 10, 1, 0, 'system', 'system'),
(1, 1, 16, 1, 0, 'system', 'system'),
(1, 1, 1100, 1, 0, 'system', 'system'),
(1, 1, 12, 1, 0, 'system', 'system'),
(1, 1, 13, 1, 0, 'system', 'system'),
(1, 1, 14, 1, 0, 'system', 'system'),
(1, 1, 15, 1, 0, 'system', 'system'),
(1, 1, 40, 1, 0, 'system', 'system'),
(1, 1, 41, 1, 0, 'system', 'system'),
(1, 1, 42, 1, 0, 'system', 'system'),
(1, 1, 43, 1, 0, 'system', 'system'),
(1, 1, 44, 1, 0, 'system', 'system'),
(1, 1, 45, 1, 0, 'system', 'system'),
(1, 1, 46, 1, 0, 'system', 'system'),
(1, 1, 47, 1, 0, 'system', 'system'),
(1, 1, 48, 1, 0, 'system', 'system'),
(1, 1, 49, 1, 0, 'system', 'system'),
(1, 1, 50, 1, 0, 'system', 'system'),
(1, 1, 51, 1, 0, 'system', 'system'),
(1, 1, 52, 1, 0, 'system', 'system'),
(1, 1, 70, 1, 0, 'system', 'system'),
(1, 1, 71, 1, 0, 'system', 'system'),
(1, 1, 72, 1, 0, 'system', 'system'),
(1, 1, 73, 1, 0, 'system', 'system'),
(1, 1, 74, 1, 0, 'system', 'system'),
(1, 1, 75, 1, 0, 'system', 'system'),
(1, 1, 80, 1, 0, 'system', 'system'),
(1, 1, 81, 1, 0, 'system', 'system'),
(1, 1, 82, 1, 0, 'system', 'system'),
(1, 1, 83, 1, 0, 'system', 'system'),
(1, 1, 84, 1, 0, 'system', 'system'),
(1, 1, 85, 1, 0, 'system', 'system'),
(1, 1, 86, 1, 0, 'system', 'system'),
(1, 1, 90, 1, 0, 'system', 'system');

-- admin 角色（role_id=2）拥有系统管理 + 智能体 + 知识库 + 教育管理 + 日常记录 + 文件管理菜单
INSERT IGNORE INTO `auth_role_scope_menu` (`role_id`, `tenant_id`, `menu_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 1, 1, 1, 0, 'system', 'system'),
(2, 1, 2, 1, 0, 'system', 'system'),
(2, 1, 3, 1, 0, 'system', 'system'),
(2, 1, 4, 1, 0, 'system', 'system'),
(2, 1, 5, 1, 0, 'system', 'system'),
(2, 1, 6, 1, 0, 'system', 'system'),
(2, 1, 7, 1, 0, 'system', 'system'),
(2, 1, 11, 1, 0, 'system', 'system'),
(2, 1, 10, 1, 0, 'system', 'system'),
(2, 1, 16, 1, 0, 'system', 'system'),
(2, 1, 1100, 1, 0, 'system', 'system'),
(2, 1, 12, 1, 0, 'system', 'system'),
(2, 1, 13, 1, 0, 'system', 'system'),
(2, 1, 14, 1, 0, 'system', 'system'),
(2, 1, 15, 1, 0, 'system', 'system'),
(2, 1, 40, 1, 0, 'system', 'system'),
(2, 1, 41, 1, 0, 'system', 'system'),
(2, 1, 42, 1, 0, 'system', 'system'),
(2, 1, 43, 1, 0, 'system', 'system'),
(2, 1, 44, 1, 0, 'system', 'system'),
(2, 1, 45, 1, 0, 'system', 'system'),
(2, 1, 46, 1, 0, 'system', 'system'),
(2, 1, 47, 1, 0, 'system', 'system'),
(2, 1, 48, 1, 0, 'system', 'system'),
(2, 1, 49, 1, 0, 'system', 'system'),
(2, 1, 50, 1, 0, 'system', 'system'),
(2, 1, 51, 1, 0, 'system', 'system'),
(2, 1, 52, 1, 0, 'system', 'system'),
(2, 1, 1500, 1, 0, 'system', 'system'),
(2, 1, 1501, 1, 0, 'system', 'system'),
(2, 1, 1502, 1, 0, 'system', 'system'),
(2, 1, 1503, 1, 0, 'system', 'system'),
(2, 1, 1508, 1, 0, 'system', 'system'),
(2, 1, 1510, 1, 0, 'system', 'system'),
(2, 1, 1511, 1, 0, 'system', 'system'),
(2, 1, 1520, 1, 0, 'system', 'system'),
(2, 1, 1521, 1, 0, 'system', 'system'),
(2, 1, 1530, 1, 0, 'system', 'system'),
(2, 1, 1531, 1, 0, 'system', 'system'),
(2, 1, 1540, 1, 0, 'system', 'system'),
(2, 1, 1541, 1, 0, 'system', 'system'),
(2, 1, 1542, 1, 0, 'system', 'system'),
(2, 1, 1543, 1, 0, 'system', 'system'),
(2, 1, 1544, 1, 0, 'system', 'system'),
(2, 1, 1550, 1, 0, 'system', 'system'),
(2, 1, 1551, 1, 0, 'system', 'system'),
(2, 1, 1552, 1, 0, 'system', 'system'),
(2, 1, 1553, 1, 0, 'system', 'system'),
(2, 1, 1560, 1, 0, 'system', 'system'),
(2, 1, 1561, 1, 0, 'system', 'system'),
(2, 1, 1562, 1, 0, 'system', 'system'),
(2, 1, 1563, 1, 0, 'system', 'system'),
(2, 1, 1564, 1, 0, 'system', 'system'),
(2, 1, 1565, 1, 0, 'system', 'system'),
(2, 1, 1566, 1, 0, 'system', 'system'),
(2, 1, 70, 1, 0, 'system', 'system'),
(2, 1, 71, 1, 0, 'system', 'system'),
(2, 1, 72, 1, 0, 'system', 'system'),
(2, 1, 73, 1, 0, 'system', 'system'),
(2, 1, 74, 1, 0, 'system', 'system'),
(2, 1, 75, 1, 0, 'system', 'system'),
(2, 1, 80, 1, 0, 'system', 'system'),
(2, 1, 81, 1, 0, 'system', 'system'),
(2, 1, 82, 1, 0, 'system', 'system'),
(2, 1, 83, 1, 0, 'system', 'system'),
(2, 1, 84, 1, 0, 'system', 'system'),
(2, 1, 85, 1, 0, 'system', 'system'),
(2, 1, 86, 1, 0, 'system', 'system'),
(2, 1, 90, 1, 0, 'system', 'system');

-- user 角色（role_id=3）拥有教育中心菜单
INSERT IGNORE INTO `auth_role_scope_menu` (`role_id`, `tenant_id`, `menu_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 1, 1500, 1, 0, 'system', 'system'),
(3, 1, 1501, 1, 0, 'system', 'system'),
(3, 1, 1502, 1, 0, 'system', 'system'),
(3, 1, 1503, 1, 0, 'system', 'system'),
(3, 1, 1508, 1, 0, 'system', 'system'),
(3, 1, 1510, 1, 0, 'system', 'system'),
(3, 1, 1511, 1, 0, 'system', 'system'),
(3, 1, 1520, 1, 0, 'system', 'system'),
(3, 1, 1521, 1, 0, 'system', 'system'),
(3, 1, 1530, 1, 0, 'system', 'system'),
(3, 1, 1531, 1, 0, 'system', 'system'),
(3, 1, 1540, 1, 0, 'system', 'system'),
(3, 1, 1541, 1, 0, 'system', 'system'),
(3, 1, 1542, 1, 0, 'system', 'system'),
(3, 1, 1543, 1, 0, 'system', 'system'),
(3, 1, 1544, 1, 0, 'system', 'system'),
(3, 1, 1550, 1, 0, 'system', 'system'),
(3, 1, 1551, 1, 0, 'system', 'system'),
(3, 1, 1552, 1, 0, 'system', 'system'),
(3, 1, 1553, 1, 0, 'system', 'system'),
(3, 1, 1560, 1, 0, 'system', 'system'),
(3, 1, 1561, 1, 0, 'system', 'system'),
(3, 1, 1562, 1, 0, 'system', 'system'),
(3, 1, 1563, 1, 0, 'system', 'system'),
(3, 1, 1564, 1, 0, 'system', 'system'),
(3, 1, 1565, 1, 0, 'system', 'system'),
(3, 1, 1566, 1, 0, 'system', 'system');

-- 教师角色（role_id=4）拥有教育空间菜单（含隐藏路由）
INSERT IGNORE INTO `auth_role_scope_menu` (`role_id`, `tenant_id`, `menu_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 3, 1500, 1, 0, 'system', 'system'),
(4, 3, 1501, 1, 0, 'system', 'system'),
(4, 3, 1502, 1, 0, 'system', 'system'),
(4, 3, 1503, 1, 0, 'system', 'system'),
(4, 3, 1508, 1, 0, 'system', 'system'),
(4, 3, 1510, 1, 0, 'system', 'system'),
(4, 3, 1511, 1, 0, 'system', 'system'),
(4, 3, 1520, 1, 0, 'system', 'system'),
(4, 3, 1521, 1, 0, 'system', 'system'),
(4, 3, 1530, 1, 0, 'system', 'system'),
(4, 3, 1531, 1, 0, 'system', 'system'),
(4, 3, 1540, 1, 0, 'system', 'system'),
(4, 3, 1541, 1, 0, 'system', 'system'),
(4, 3, 1542, 1, 0, 'system', 'system'),
(4, 3, 1543, 1, 0, 'system', 'system'),
(4, 3, 1544, 1, 0, 'system', 'system'),
(4, 3, 1550, 1, 0, 'system', 'system'),
(4, 3, 1551, 1, 0, 'system', 'system'),
(4, 3, 1552, 1, 0, 'system', 'system'),
(4, 3, 1553, 1, 0, 'system', 'system'),
(4, 3, 1560, 1, 0, 'system', 'system'),
(4, 3, 1561, 1, 0, 'system', 'system'),
(4, 3, 1562, 1, 0, 'system', 'system'),
(4, 3, 1563, 1, 0, 'system', 'system'),
(4, 3, 1564, 1, 0, 'system', 'system'),
(4, 3, 1565, 1, 0, 'system', 'system'),
(4, 3, 1566, 1, 0, 'system', 'system');

-- 学生角色（role_id=5）拥有教育空间菜单（含隐藏路由）
INSERT IGNORE INTO `auth_role_scope_menu` (`role_id`, `tenant_id`, `menu_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 3, 1500, 1, 0, 'system', 'system'),
(5, 3, 1501, 1, 0, 'system', 'system'),
(5, 3, 1502, 1, 0, 'system', 'system'),
(5, 3, 1503, 1, 0, 'system', 'system'),
(5, 3, 1508, 1, 0, 'system', 'system'),
(5, 3, 1510, 1, 0, 'system', 'system'),
(5, 3, 1511, 1, 0, 'system', 'system'),
(5, 3, 1520, 1, 0, 'system', 'system'),
(5, 3, 1521, 1, 0, 'system', 'system'),
(5, 3, 1530, 1, 0, 'system', 'system'),
(5, 3, 1531, 1, 0, 'system', 'system'),
(5, 3, 1540, 1, 0, 'system', 'system'),
(5, 3, 1541, 1, 0, 'system', 'system'),
(5, 3, 1542, 1, 0, 'system', 'system'),
(5, 3, 1543, 1, 0, 'system', 'system'),
(5, 3, 1544, 1, 0, 'system', 'system'),
(5, 3, 1550, 1, 0, 'system', 'system'),
(5, 3, 1551, 1, 0, 'system', 'system'),
(5, 3, 1552, 1, 0, 'system', 'system'),
(5, 3, 1553, 1, 0, 'system', 'system'),
(5, 3, 1560, 1, 0, 'system', 'system'),
(5, 3, 1561, 1, 0, 'system', 'system'),
(5, 3, 1562, 1, 0, 'system', 'system'),
(5, 3, 1563, 1, 0, 'system', 'system'),
(5, 3, 1564, 1, 0, 'system', 'system'),
(5, 3, 1565, 1, 0, 'system', 'system'),
(5, 3, 1566, 1, 0, 'system', 'system');

-- 家长角色（role_id=6）拥有教育空间菜单（仅数据分析）
INSERT IGNORE INTO `auth_role_scope_menu` (`role_id`, `tenant_id`, `menu_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (6, 3, 1500, 1, 0, 'system', 'system'),
(6, 3, 1550, 1, 0, 'system', 'system'),
(6, 3, 1551, 1, 0, 'system', 'system'),
(6, 3, 1552, 1, 0, 'system', 'system'),
(6, 3, 1553, 1, 0, 'system', 'system');

-- 修正历史种子数据：教育子租户角色及其授权关系归属子租户 3。
UPDATE `auth_role`
SET `tenant_id` = 3
WHERE `id` IN (4, 5, 6);

UPDATE `auth_role_scope_menu`
SET `tenant_id` = 3
WHERE `role_id` IN (4, 5, 6)
  AND `tenant_id` = 3;

-- ==============================
-- 权限定义（auth_code）
-- auth_code 只定义权限，不绑定角色或租户；角色授权见 role_scope_auth_code。
-- ==============================
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 'system:user:list', '查看用户列表', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 'system:user:create', '创建用户', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 'system:user:update', '更新用户', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 'system:user:delete', '删除用户', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (116, 'system:user:password', '重置用户密码', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 'system:role:list', '查看角色列表', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (6, 'system:role:create', '创建角色', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (7, 'system:role:update', '更新角色', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (8, 'system:role:delete', '删除角色', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (115, 'system:role:assign', '分配角色权限', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (9, 'system:menu:list', '查看菜单列表', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (10, 'system:menu:create', '创建菜单', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (11, 'system:menu:update', '更新菜单', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (12, 'system:menu:delete', '删除菜单', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (13, 'system:tenant:list', '查看租户列表', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (14, 'system:tenant:create', '创建租户', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (15, 'system:tenant:update', '更新租户', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (16, 'system:tenant:delete', '删除租户', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (17, 'system:auth-code:list', '查看权限码列表', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (18, 'system:auth-code:create', '创建权限码', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (19, 'system:auth-code:update', '更新权限码', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (20, 'system:auth-code:delete', '删除权限码', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (21, 'system:dict:list', '查看字典', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (22, 'system:dict:create', '创建字典', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (23, 'system:dict:update', '更新字典', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (24, 'system:dict:delete', '删除字典', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (25, 'agent:admin:list', '查看 Agent 列表', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (26, 'agent:admin:create', '创建 Agent', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (27, 'agent:admin:edit', '编辑 Agent', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (28, 'agent:admin:delete', '删除 Agent', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (29, 'agent:platform:list', '查看平台', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (30, 'agent:platform:create', '创建平台', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (31, 'agent:platform:edit', '编辑平台', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (32, 'agent:platform:delete', '删除平台', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (33, 'agent:platform:set-default', '设置默认平台', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (34, 'agent:model:list', '查看模型', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (35, 'agent:model:create', '创建模型', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (36, 'agent:model:edit', '编辑模型', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (37, 'agent:model:delete', '删除模型', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (38, 'agent:model:set-default', '设置默认模型', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (39, 'agent:chat:config', '对话调试', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (40, 'agent:intent:list', '查看意图', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (41, 'agent:intent:create', '创建意图', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (42, 'agent:intent:delete', '删除意图', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (43, 'knowledge:list', '查看知识点', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (44, 'knowledge:create', '创建知识点', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (45, 'knowledge:edit', '编辑知识点', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (46, 'knowledge:delete', '删除知识点', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (47, 'knowledge:graph', '查看知识图谱', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (48, 'knowledge:document:list', '查看文档', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (49, 'knowledge:document:upload', '上传文档', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (50, 'knowledge:document:delete', '删除文档', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (51, 'knowledge:index:rebuild', '重建索引', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (52, 'knowledge:relation', '管理知识关系', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (53, 'edu:subject:list', '查看学科', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (54, 'edu:subject:create', '创建学科', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (55, 'edu:subject:edit', '编辑学科', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (56, 'edu:subject:delete', '删除学科', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (57, 'edu:textbook:list', '查看教材', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (58, 'edu:textbook:create', '创建教材', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (59, 'edu:textbook:edit', '编辑教材', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (60, 'edu:textbook:delete', '删除教材', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (61, 'edu:chapter:list', '查看章节', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (62, 'edu:chapter:create', '创建章节', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (63, 'edu:chapter:edit', '编辑章节', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (64, 'edu:chapter:delete', '删除章节', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (65, 'edu:course:list', '查看课程', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (66, 'edu:course:create', '创建课程', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (67, 'edu:course:edit', '编辑课程', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (68, 'edu:course:delete', '删除课程', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (69, 'edu:question:list', '查看题目', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (70, 'edu:question:create', '创建题目', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (71, 'edu:question:edit', '编辑题目', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (72, 'edu:question:delete', '删除题目', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (73, 'edu:exam:list', '查看考试', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (74, 'edu:exam:create', '创建考试', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (75, 'edu:exam:edit', '编辑考试', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (76, 'edu:exam:delete', '删除考试', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (77, 'edu:exam:publish', '发布考试', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (78, 'edu:student:list', '查看学生', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (79, 'edu:resource:list', '查看资源', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (80, 'edu:resource:upload', '上传资源', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (81, 'edu:resource:delete', '删除资源', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (82, 'edu:plan:list', '查看学习计划', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (83, 'edu:review:list', '查看复习任务', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (84, 'edu:analytics', '查看学情分析', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (85, 'edu:wrong-question', '查看错题管理', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (86, 'record:profile:list', '查看人物', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (87, 'record:profile:create', '创建人物', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (88, 'record:profile:edit', '编辑人物', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (89, 'record:profile:delete', '删除人物', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (90, 'record:timeline:list', '查看时间轴', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (91, 'record:timeline:create', '创建事件', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (92, 'record:timeline:delete', '删除事件', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (93, 'record:tags:list', '查看标签', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (94, 'record:media:list', '查看媒体', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (95, 'record:media:upload', '上传媒体', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (96, 'file:upload', '上传文件', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (97, 'file:delete', '删除文件', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (98, 'plugin:list', '查看插件列表', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (99, 'plugin:start', '启动插件', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (100, 'plugin:stop', '停止插件', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (101, 'plugin:uninstall', '卸载插件', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (102, 'plugin:scan', '扫描插件', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (103, 'tool:mcp:list', '查看 MCP 工具', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (104, 'tool:mcp:detail', '查看 MCP 工具详情', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (105, 'tool:mcp:execute', '执行 MCP 工具', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (106, 'tool:mcp:categories', '查看 MCP 工具分类', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (107, 'tool:mcp:stats', '查看 MCP 工具统计', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (108, 'usage:overview', '查看用量概览', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (109, 'usage:daily', '查看日用量', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (110, 'usage:weekly', '查看周用量', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (111, 'usage:monthly', '查看月用量', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (112, 'usage:model', '按模型查看用量', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (113, 'usage:llm', '查看 LLM 用量', 1, 0, 'system', 'system');
INSERT IGNORE INTO `auth_auth_code` (`id`, `code`, `name`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (114, 'usage:embedding', '查看 Embedding 用量', 1, 0, 'system', 'system');

-- ==============================
-- 角色-菜单关联（仅 CATALOG + MENU 展示关系）
-- ==============================
INSERT IGNORE INTO `auth_role_scope_menu` (`role_id`, `tenant_id`, `menu_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT r.id, 1, m.id, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM (SELECT 1 AS id UNION ALL SELECT 2) AS r
CROSS JOIN `auth_menu` m
WHERE m.`type` IN ('CATALOG', 'MENU');

-- 角色-权限授权：超级管理员和管理员默认拥有全部权限。
INSERT IGNORE INTO `auth_role_scope_auth_code` (`role_id`, `auth_code_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT r.id, a.id, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM (SELECT 1 AS id UNION ALL SELECT 2 AS id) AS r CROSS JOIN `auth_auth_code` a
WHERE a.status = 1 AND a.del_flag = 0;

-- 教师角色：教育权限。
INSERT IGNORE INTO `auth_role_scope_auth_code` (`role_id`, `auth_code_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 4, a.id, 3, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP FROM `auth_auth_code` a WHERE a.code LIKE 'edu:%' AND a.status = 1 AND a.del_flag = 0;

-- 学生角色：教育查看类权限。
INSERT IGNORE INTO `auth_role_scope_auth_code` (`role_id`, `auth_code_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 5, a.id, 3, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP FROM `auth_auth_code` a WHERE a.code IN ('edu:subject:list','edu:textbook:list','edu:chapter:list','edu:course:list','edu:question:list','edu:student:list','edu:plan:list','edu:review:list','edu:analytics','edu:wrong-question') AND a.status = 1 AND a.del_flag = 0;

-- 家长角色：学生信息和分析查看权限。
INSERT IGNORE INTO `auth_role_scope_auth_code` (`role_id`, `auth_code_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 6, a.id, 3, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP FROM `auth_auth_code` a WHERE a.code IN ('edu:student:list','edu:analytics','edu:plan:list','edu:review:list') AND a.status = 1 AND a.del_flag = 0;

-- ==============================
-- 为历史子租户补齐默认种子数据
--
-- menu 是租户私有数据，不能直接复用默认租户的 menu_id。
-- 这里为每个已有子租户复制默认租户菜单，并按
--   cloned_menu_id = source_menu_id + tenant_id * 100000
-- 建立稳定的父子菜单映射，保证 repeatable migration 可重复执行。
-- 新建租户由 TenantRepository.initializeTenantSecurity() 完成同样的初始化。
-- ==============================

-- 1. 复制默认租户菜单到每个已有子租户。
INSERT IGNORE INTO `auth_menu`
    (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`,
     `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`,
     `status`, `order`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT
    m.id + (t.id * 100000),
    m.name,
    m.code,
    m.type,
    CASE WHEN m.parent_id IS NULL THEN NULL
         ELSE m.parent_id + (t.id * 100000)
    END,
    t.id,
    m.path,
    m.redirect,
    m.icon,
    m.component,
    m.layout,
    m.keep_alive,
    m.method,
    m.description,
    m.show,
    m.status,
    m.`order`,
    m.del_flag,
    'system',
    CURRENT_TIMESTAMP,
    'system',
    CURRENT_TIMESTAMP
FROM `auth_tenant` t
CROSS JOIN `auth_menu` m
WHERE t.id <> 1
  AND t.status = 1
  AND t.del_flag = 0
  AND m.tenant_id = 1
  AND m.del_flag = 0;

-- 2. 每个租户可用全部默认菜单和全部默认权限码。
INSERT IGNORE INTO `auth_tenant_menu` (`tenant_id`, `menu_id`, `status`)
SELECT t.id, m.id, 1
FROM `auth_tenant` t
INNER JOIN `auth_menu` m ON m.tenant_id = t.id
WHERE t.status = 1
  AND t.del_flag = 0
  AND m.status = 1
  AND m.del_flag = 0;

INSERT IGNORE INTO `auth_tenant_auth_code` (`tenant_id`, `auth_code_id`, `status`)
SELECT t.id, a.id, 1
FROM `auth_tenant` t
CROSS JOIN `auth_auth_code` a
WHERE t.status = 1
  AND t.del_flag = 0
  AND a.status = 1
  AND a.del_flag = 0
  AND a.create_by = 'system';

-- 历史教育角色原来引用的是默认租户 menu_id。
-- 改为引用子租户 3 的菜单副本，避免出现 role_scope_menu 有记录但
-- menu.tenant_id 不匹配、最终查询不到菜单的情况。
INSERT IGNORE INTO `auth_role_scope_menu`
    (`role_id`, `tenant_id`, `menu_id`, `status`, `del_flag`,
     `create_by`, `create_time`, `update_by`, `update_time`)
SELECT rsm.role_id, 3, source_menu.id + (3 * 100000), rsm.status, rsm.del_flag,
       'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM `auth_role_scope_menu` rsm
INNER JOIN `auth_menu` source_menu
        ON source_menu.id = rsm.menu_id
       AND source_menu.tenant_id = 1
WHERE rsm.tenant_id = 3
  AND rsm.role_id IN (4, 5, 6);

DELETE FROM `auth_role_scope_menu`
WHERE tenant_id = 3
  AND role_id IN (4, 5, 6)
  AND EXISTS (
      SELECT 1
      FROM `auth_menu` source_menu
      WHERE source_menu.id = `auth_role_scope_menu`.menu_id
        AND source_menu.tenant_id = 1
  );

-- 3. 每个子租户只补齐租户超级管理员角色。
-- 已存在的 admin/user 角色不删除，避免破坏历史用户授权关系；
-- 但不再为新旧子租户自动创建这两个角色。
INSERT INTO `auth_role`
    (`code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
SELECT 'tenant_super', '租户超级管理员', t.id, 1, '租户超级管理员', 0, 'system', 'system'
FROM `auth_tenant` t
WHERE t.id <> 1
  AND t.status = 1
  AND t.del_flag = 0
  AND NOT EXISTS (
      SELECT 1 FROM `auth_role` r
      WHERE r.tenant_id = t.id AND r.code IN ('tenant_super', 'super') AND r.del_flag = 0
  );

-- 4. 租户超级管理员拥有本租户全部菜单、权限码。
-- 历史 admin 角色及其已有授权保留，但不再由种子数据继续扩充授权。
INSERT IGNORE INTO `auth_role_scope_menu`
    (`role_id`, `tenant_id`, `menu_id`, `status`, `del_flag`,
     `create_by`, `create_time`, `update_by`, `update_time`)
SELECT r.id, r.tenant_id, tm.menu_id, 1, 0,
       'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM `auth_role` r
INNER JOIN `auth_tenant_menu` tm ON tm.tenant_id = r.tenant_id AND tm.status = 1
WHERE r.status = 1
  AND r.del_flag = 0
  AND r.code IN ('tenant_super', 'super');

INSERT IGNORE INTO `auth_role_scope_auth_code`
    (`role_id`, `auth_code_id`, `tenant_id`, `status`, `del_flag`,
     `create_by`, `create_time`, `update_by`, `update_time`)
SELECT r.id, ta.auth_code_id, r.tenant_id, 1, 0,
       'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM `auth_role` r
INNER JOIN `auth_tenant_auth_code` ta ON ta.tenant_id = r.tenant_id AND ta.status = 1
WHERE r.status = 1
  AND r.del_flag = 0
  AND r.code IN ('tenant_super', 'super');

-- 5. 为每个已有子租户创建默认管理员用户，并绑定本租户超级管理员角色。
-- 用户名按租户编码生成，避免不同租户的默认用户重名。
INSERT INTO `auth_user`
    (`username`, `password`, `status`, `del_flag`, `create_by`, `update_by`,
     `nick_name`, `email`)
SELECT CONCAT(t.code, '_admin'),
       '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy',
       1, 0, 'system', 'system',
       CONCAT(t.name, '管理员'),
       CONCAT(t.code, '@example.com')
FROM `auth_tenant` t
WHERE t.id <> 1
  AND t.status = 1
  AND t.del_flag = 0
  AND NOT EXISTS (
      SELECT 1 FROM `auth_user` u
      WHERE u.username = CONCAT(t.code, '_admin')
  );

INSERT IGNORE INTO `auth_user_scope_role`
    (`user_id`, `tenant_id`, `role_id`, `status`, `del_flag`,
     `create_by`, `create_time`, `update_by`, `update_time`)
SELECT u.id, r.tenant_id, r.id, 1, 0,
       'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM `auth_user` u
INNER JOIN `auth_tenant` t ON u.username = CONCAT(t.code, '_admin')
INNER JOIN `auth_role` r ON r.tenant_id = t.id
                    AND r.code IN ('tenant_super', 'super')
                    AND r.status = 1
                    AND r.del_flag = 0
WHERE t.id <> 1
  AND t.status = 1
  AND t.del_flag = 0;

-- 6. 每个已有子租户补齐默认配额。
INSERT IGNORE INTO `auth_tenant_quota`
    (`tenant_id`, `max_agent_count`, `max_token_per_day`,
     `max_storage_mb`, `max_user_count`, `status`)
SELECT t.id, 50, 5000000, 5120, 500, 1
FROM `auth_tenant` t
WHERE t.status = 1
  AND t.del_flag = 0;

-- 清理历史上引用了不存在菜单的孤儿授权记录。
DELETE FROM `auth_role_scope_menu`
WHERE NOT EXISTS (
    SELECT 1
    FROM `auth_menu` m
    WHERE m.id = `auth_role_scope_menu`.menu_id
      AND m.tenant_id = `auth_role_scope_menu`.tenant_id
      AND m.del_flag = 0
);
