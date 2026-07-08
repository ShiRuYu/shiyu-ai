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
VALUES (8, 'AI 平台', 'SystemAiPlatform', 'MENU', 1, 1, '/system/ai-platform', 'lucide:bot', '/agent/platform/list', TRUE, 1, 7, 0, 'system', 'system');

INSERT IGNORE INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `icon`, `component`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`)
VALUES (9, 'AI 模型', 'SystemAiModel', 'MENU', 1, 1, '/system/ai-model', 'lucide:cpu', '/agent/model/list', TRUE, 1, 8, 0, 'system', 'system');

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
       (0, 0, 1507, 1, 1, 0, 'system', 'system');

-- admin 角色拥有系统管理菜单
INSERT IGNORE INTO `role_workspace_menu` (`role_id`, `workspace_id`, `menu_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 0, 1, 1, 1, 0, 'system', 'system'),
       (1, 0, 2, 1, 1, 0, 'system', 'system'),
       (1, 0, 3, 1, 1, 0, 'system', 'system'),
       (1, 0, 4, 1, 1, 0, 'system', 'system'),
       (1, 0, 5, 1, 1, 0, 'system', 'system'),
       (1, 0, 6, 1, 1, 0, 'system', 'system'),
       (1, 0, 7, 1, 1, 0, 'system', 'system'),
       (1, 0, 8, 1, 1, 0, 'system', 'system'),
       (1, 0, 9, 1, 1, 0, 'system', 'system');

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
-- 权限码
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
