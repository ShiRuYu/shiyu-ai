-- ============================================
-- Data: 教育业务菜单与权限补充
-- 新增: 学习中心/练习中心/考试中心/复习中心/数据中心/AI助手
-- ============================================

-- ==============================
-- 教育业务菜单
-- ==============================
INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (100, '成长记录', 'GrowthRecord', 'CATALOG', NULL, 1, '/growth', '/growth/profile', 'lucide:heart', '', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (101, '人物管理', 'GrowthProfile', 'MENU', 100, 1, '/growth/profile', 'lucide:users', '/record/profile/list', TRUE, 1, 1, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (102, '时间轴', 'GrowthTimeline', 'MENU', 100, 1, '/growth/timeline', 'lucide:timeline', '/record/timeline/list', TRUE, 1, 2, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (104, '标签管理', 'GrowthTag', 'MENU', 100, 1, '/growth/tag', 'lucide:tags', '/record/tags/list', TRUE, 1, 4, 0, 'system', 'system');

-- ==============================
-- 角色-菜单 关联（教师/学生/家长角色）
-- ==============================
-- 教师角色拥有教育业务菜单
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 2, 100, 1, 1, 0, 'system', 'system'),
       (3, 2, 101, 1, 1, 0, 'system', 'system'),
       (3, 2, 102, 1, 1, 0, 'system', 'system'),
       (3, 2, 104, 1, 1, 0, 'system', 'system');

-- 学生角色拥有教育业务菜单
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 2, 100, 1, 1, 0, 'system', 'system'),
       (4, 2, 101, 1, 1, 0, 'system', 'system'),
       (4, 2, 102, 1, 1, 0, 'system', 'system');

-- 家长角色拥有教育业务菜单
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 2, 100, 1, 1, 0, 'system', 'system'),
       (5, 2, 101, 1, 1, 0, 'system', 'system'),
       (5, 2, 102, 1, 1, 0, 'system', 'system');
