  -- ============================================
-- 数据库初始化脚本
-- 包含表结构创建和初始数据插入
-- ============================================

-- ============================================
-- 1. 创建表结构
-- ============================================

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) COMMENT '密码',
    `status` CHAR(1) DEFAULT '1' COMMENT '状态（1正常 0停用）',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `nick_name` VARCHAR(64) COMMENT '昵称',
    `gender` VARCHAR(10) COMMENT '性别',
    `avatar` VARCHAR(255) COMMENT '头像',
    `address` VARCHAR(255) COMMENT '地址',
    `email` VARCHAR(128) COMMENT '邮箱',
    `ext_info` TEXT COMMENT '扩展信息(JSON)',
    PRIMARY KEY (`id`)
);
CREATE INDEX `idx_username` ON `user` (`username`);
COMMENT ON TABLE `user` IS '用户表';

-- 角色表
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
    `code` VARCHAR(64) NOT NULL COMMENT '角色编码',
    `name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `status` CHAR(1) DEFAULT '1' COMMENT '状态（1正常 0停用）',
    `remark` VARCHAR(500) COMMENT '备注',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE INDEX `idx_code` ON `role` (`code`);
COMMENT ON TABLE `role` IS '角色表';

-- 菜单表
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单 ID',
    `name` VARCHAR(64) NOT NULL COMMENT '菜单名称',
    `code` VARCHAR(64) NOT NULL COMMENT '菜单编码',
    `type` VARCHAR(20) NOT NULL COMMENT '菜单类型（MENU/BUTTON）',
    `parent_id` BIGINT COMMENT '父菜单 ID',
    `path` VARCHAR(255) COMMENT '路径',
    `redirect` VARCHAR(255) COMMENT '重定向地址',
    `icon` VARCHAR(64) COMMENT '图标',
    `component` VARCHAR(255) COMMENT '组件',
    `layout` VARCHAR(64) COMMENT '布局',
    `keep_alive` BOOLEAN COMMENT '是否缓存',
    `method` VARCHAR(20) COMMENT '请求方法',
    `description` VARCHAR(255) COMMENT '描述',
    `show` BOOLEAN DEFAULT TRUE COMMENT '是否显示',
    `status` CHAR(1) DEFAULT '1' COMMENT '状态（1正常 0停用）',
    `order` INT DEFAULT 0 COMMENT '排序',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE INDEX `idx_parent_id` ON `menu` (`parent_id`);
COMMENT ON TABLE `menu` IS '菜单表';

-- 用户角色关联表
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `role_id` BIGINT NOT NULL COMMENT '角色 ID',
    PRIMARY KEY (`user_id`, `role_id`)
);
CREATE INDEX `idx_role_id` ON `user_role` (`role_id`);
COMMENT ON TABLE `user_role` IS '用户角色关联表';

-- 角色菜单关联表
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu` (
    `role_id` BIGINT NOT NULL COMMENT '角色 ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单 ID',
    PRIMARY KEY (`role_id`, `menu_id`)
);
CREATE INDEX `idx_menu_id` ON `role_menu` (`menu_id`);
COMMENT ON TABLE `role_menu` IS '角色菜单关联表';

-- 权限码表
DROP TABLE IF EXISTS `auth_code`;
CREATE TABLE `auth_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限码 ID',
    `code` VARCHAR(64) NOT NULL COMMENT '权限编码',
    `name` VARCHAR(128) COMMENT '权限名称',
    `role_id` BIGINT NOT NULL COMMENT '角色 ID',
    `status` CHAR(1) DEFAULT '1' COMMENT '状态（1正常 0停用）',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE INDEX `idx_role_code` ON `auth_code` (`role_id`, `code`);
CREATE INDEX `idx_auth_code` ON `auth_code` (`code`);
COMMENT ON TABLE `auth_code` IS '权限码表';

-- 工作空间表
DROP TABLE IF EXISTS `workspace`;
CREATE TABLE `workspace` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工作空间 ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父工作空间 ID',
    `name` VARCHAR(64) NOT NULL COMMENT '工作空间名称',
    `order` INT DEFAULT 0 COMMENT '排序',
    `leader` VARCHAR(64) COMMENT '负责人',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `email` VARCHAR(64) COMMENT '邮箱',
    `status` CHAR(1) DEFAULT '1' COMMENT '状态（1正常 0停用）',
    `remark` VARCHAR(500) COMMENT '备注',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE INDEX `idx_workspace_parent_id` ON `workspace` (`parent_id`);
COMMENT ON TABLE `workspace` IS '工作空间表';

-- ============================================
-- 2. 初始化用户数据（根据 API 文档模拟数据）
-- 注意：密码已使用 BCrypt 加密，原始密码均为: 123456
-- ============================================

-- 用户 vben (ID: 0) - super 角色
INSERT INTO `user` (`id`, `username`, `password`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (0, 'vben', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', '1', 0, '0', NOW(), '0', NOW(), 'Vben', NULL, NULL, NULL, NULL);

-- 用户 admin (ID: 1)
INSERT INTO `user` (`id`, `username`, `password`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (1, 'admin', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', '1', 0, '0', NOW(), '0', NOW(), 'Admin', NULL, NULL, NULL, NULL);

-- 用户 jack (ID: 2)
INSERT INTO `user` (`id`, `username`, `password`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (2, 'jack', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', '1', 0, '0', NOW(), '0', NOW(), 'Jack', NULL, NULL, NULL, NULL);

-- ============================================
-- 3. 初始化角色数据
-- ============================================

-- 角色 super (ID: 0)
INSERT INTO `role` (`id`, `code`, `name`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) 
VALUES (0, 'super', '超级管理员', '1', '拥有系统所有权限', 0, '0', NOW(), '0', NOW());

-- 角色 admin (ID: 1)
INSERT INTO `role` (`id`, `code`, `name`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) 
VALUES (1, 'admin', '管理员', '1', '系统管理员角色', 0, '0', NOW(), '0', NOW());

-- 角色 user (ID: 2)
INSERT INTO `role` (`id`, `code`, `name`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) 
VALUES (2, 'user', '普通用户', '1', '普通用户角色', 0, '0', NOW(), '0', NOW());

-- ============================================
-- 4. 初始化用户角色关联数据
-- ============================================

-- 用户 vben 分配 super 角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (0, 0);

-- 用户 admin 分配 admin 角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 用户 jack 分配 user 角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (2, 2);

-- ============================================
-- 4.1 初始化工作空间数据
-- ============================================

INSERT INTO `workspace` (`id`, `parent_id`, `name`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 0, '总公司', 1, 'Vben', '15888888888', 'vben@shiyu.com', '1', '公司顶层组织', 0, '0', '0');

INSERT INTO `workspace` (`id`, `parent_id`, `name`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 1, '技术部', 1, NULL, NULL, NULL, '1', '研发技术部门', 0, '0', '0');

INSERT INTO `workspace` (`id`, `parent_id`, `name`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 1, '产品部', 2, NULL, NULL, NULL, '1', '产品部门', 0, '0', '0');

INSERT INTO `workspace` (`id`, `parent_id`, `name`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 2, '前端组', 1, NULL, NULL, NULL, '1', NULL, 0, '0', '0');

INSERT INTO `workspace` (`id`, `parent_id`, `name`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 2, '后端组', 2, NULL, NULL, NULL, '1', NULL, 0, '0', '0');

-- ============================================
-- 5. 初始化菜单数据
-- ============================================

-- ==================== Dashboard 模块 ====================

-- 根菜单：Dashboard (ID: 1)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (1, '仪表盘', 'Dashboard', 'MENU', NULL, '/dashboard', '/analytics', 'lucide:layout-dashboard', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, '1', -1, 0, '0', '0');

-- Dashboard 子菜单：分析页 (ID: 2)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (2, '分析页', 'Analytics', 'MENU', 1, '/analytics', NULL, 'lucide:area-chart', '/dashboard/analytics/index', '', TRUE, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

-- Dashboard 子菜单：工作空间 (ID: 3)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (3, '工作空间', 'Workspace', 'MENU', 1, '/workspace', NULL, 'carbon:workspace', '/dashboard/workspace/index', '', TRUE, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

-- ==================== 系统管理模块 ====================

-- 根菜单：系统管理 (ID: 100)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (100, '系统管理', 'System', 'MENU', NULL, '/system', NULL, 'ion:settings-outline', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, '1', 9997, 0, '0', '0');

-- 系统管理子菜单：菜单管理 (ID: 201)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (201, '菜单管理', 'SystemMenu', 'MENU', 100, '/system/menu', NULL, 'carbon:menu', '/system/menu/list', '', TRUE, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

-- 菜单管理按钮权限：新增 (ID: 20101)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20101, '新增菜单', 'System:Menu:Create', 'BUTTON', 201, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

-- 菜单管理按钮权限：编辑 (ID: 20102)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20102, '编辑菜单', 'System:Menu:Edit', 'BUTTON', 201, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

-- 菜单管理按钮权限：删除 (ID: 20103)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20103, '删除菜单', 'System:Menu:Delete', 'BUTTON', 201, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 3, 0, '0', '0');

-- 系统管理子菜单：工作空间管理 (ID: 202)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (202, '工作空间管理', 'SystemWorkspace', 'MENU', 100, '/system/workspace', NULL, 'carbon:container-services', '/system/workspace/list', '', TRUE, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

-- 工作空间管理按钮权限：新增 (ID: 20201)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20201, '新增工作空间', 'System:Workspace:Create', 'BUTTON', 202, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

-- 工作空间管理按钮权限：编辑 (ID: 20202)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20202, '编辑工作空间', 'System:Workspace:Edit', 'BUTTON', 202, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

-- 工作空间管理按钮权限：删除 (ID: 20203)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20203, '删除工作空间', 'System:Workspace:Delete', 'BUTTON', 202, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 3, 0, '0', '0');

-- 系统管理子菜单：角色管理 (ID: 203)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (203, '角色管理', 'SystemRole', 'MENU', 100, '/system/role', NULL, 'carbon:user-role', '/system/role/list', '', TRUE, NULL, NULL, TRUE, '1', 3, 0, '0', '0');

-- 角色管理按钮权限：查询 (ID: 20301)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20301, '角色查询', 'system:role:query', 'BUTTON', 203, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询角色列表', TRUE, '1', 1, 0, '0', '0');

-- 角色管理按钮权限：新增 (ID: 20302)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20302, '角色新增', 'system:role:create', 'BUTTON', 203, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增角色', TRUE, '1', 2, 0, '0', '0');

-- 角色管理按钮权限：修改 (ID: 20303)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20303, '角色修改', 'system:role:update', 'BUTTON', 203, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改角色信息', TRUE, '1', 3, 0, '0', '0');

-- 角色管理按钮权限：删除 (ID: 20304)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20304, '角色删除', 'system:role:delete', 'BUTTON', 203, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除角色', TRUE, '1', 4, 0, '0', '0');

-- ==================== 日常记录模块 ====================

-- 根菜单：日常记录 (ID: 400)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (400, '日常记录', 'Record', 'MENU', NULL, '/record', NULL, 'mdi:book-open-page-variant', '', '', FALSE, NULL, '日常记录管理目录', TRUE, '1', 5, 0, '0', '0');

-- 日常记录子菜单：人物管理 (ID: 401)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (401, '人物管理', 'RecordProfile', 'MENU', 400, '/record/profile', NULL, 'mdi:account-multiple', '/record/profile/list', '', TRUE, NULL, '人物信息管理', TRUE, '1', 1, 0, '0', '0');

-- 人物管理按钮权限：查询 (ID: 40101)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40101, '人物查询', 'record:profile:query', 'BUTTON', 401, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询人物列表', TRUE, '1', 1, 0, '0', '0');

-- 人物管理按钮权限：新增 (ID: 40102)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40102, '人物新增', 'record:profile:add', 'BUTTON', 401, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增人物', TRUE, '1', 2, 0, '0', '0');

-- 人物管理按钮权限：修改 (ID: 40103)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40103, '人物修改', 'record:profile:edit', 'BUTTON', 401, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改人物信息', TRUE, '1', 3, 0, '0', '0');

-- 人物管理按钮权限：删除 (ID: 40104)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40104, '人物删除', 'record:profile:remove', 'BUTTON', 401, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除人物', TRUE, '1', 4, 0, '0', '0');

-- 日常记录子菜单：时间轴管理 (ID: 402)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (402, '时间轴管理', 'RecordTimeline', 'MENU', 400, '/record/timeline', NULL, 'mdi:timeline', '/record/timeline/list', '', TRUE, NULL, '时间轴事件管理', TRUE, '1', 2, 0, '0', '0');

-- 时间轴管理按钮权限：查询 (ID: 40201)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40201, '时间轴查询', 'record:timeline:query', 'BUTTON', 402, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询时间轴事件列表', TRUE, '1', 1, 0, '0', '0');

-- 时间轴管理按钮权限：新增 (ID: 40202)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40202, '时间轴新增', 'record:timeline:add', 'BUTTON', 402, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增时间轴事件', TRUE, '1', 2, 0, '0', '0');

-- 时间轴管理按钮权限：修改 (ID: 40203)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40203, '时间轴修改', 'record:timeline:edit', 'BUTTON', 402, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改时间轴事件', TRUE, '1', 3, 0, '0', '0');

-- 时间轴管理按钮权限：删除 (ID: 40204)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40204, '时间轴删除', 'record:timeline:remove', 'BUTTON', 402, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除时间轴事件', TRUE, '1', 4, 0, '0', '0');

-- ==================== 字典管理模块 ====================

-- 系统管理子菜单：字典管理 (ID: 204)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (204, '字典管理', 'SystemDict', 'MENU', 100, '/system/dict', NULL, 'carbon:data-table', '/common/dict/list', '', TRUE, NULL, NULL, TRUE, '1', 4, 0, '0', '0');

-- 字典管理按钮权限：查询 (ID: 20401)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20401, '字典查询', 'system:dict:query', 'BUTTON', 204, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询字典列表', TRUE, '1', 1, 0, '0', '0');

-- 字典管理按钮权限：新增 (ID: 20402)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20402, '字典新增', 'system:dict:create', 'BUTTON', 204, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增字典', TRUE, '1', 2, 0, '0', '0');

-- 字典管理按钮权限：修改 (ID: 20403)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20403, '字典修改', 'system:dict:update', 'BUTTON', 204, NULL, NULL, NULL, NULL, NULL, NULL, 'PATCH', '修改字典', TRUE, '1', 3, 0, '0', '0');

-- 字典管理按钮权限：删除 (ID: 20404)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20404, '字典删除', 'system:dict:delete', 'BUTTON', 204, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除字典', TRUE, '1', 4, 0, '0', '0');

-- ==================== 智能体模块 ====================

-- 根菜单：智能体 (ID: 500)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (500, '智能体', 'Agent', 'MENU', NULL, '/agent', NULL, 'carbon:ibm-watson-assistant', '', '', FALSE, NULL, 'AI智能体管理', TRUE, '1', 6, 0, '0', '0');

-- 智能体子菜单：Agent管理 (ID: 501)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (501, 'Agent管理', 'AgentAdminList', 'MENU', 500, '/agent/admin/list', NULL, 'carbon:cube', '/agent/admin/agent-list', '', TRUE, NULL, 'Agent 注册与管理', TRUE, '1', 1, 0, '0', '0');

-- 智能体子菜单：平台管理 (ID: 503)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (503, '平台管理', 'CommonPlatform', 'MENU', 500, '/agent/platform', NULL, 'carbon:cloud', '/agent/platform/list', '', TRUE, NULL, 'AI平台管理', TRUE, '1', 3, 0, '0', '0');

-- 平台管理按钮权限：查询 (ID: 50301)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50301, '平台查询', 'common:platform:query', 'BUTTON', 503, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询平台列表', TRUE, '1', 1, 0, '0', '0');

-- 平台管理按钮权限：新增 (ID: 50302)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50302, '平台新增', 'common:platform:create', 'BUTTON', 503, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增平台', TRUE, '1', 2, 0, '0', '0');

-- 平台管理按钮权限：修改 (ID: 50303)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50303, '平台修改', 'common:platform:update', 'BUTTON', 503, NULL, NULL, NULL, NULL, NULL, NULL, 'PATCH', '修改平台', TRUE, '1', 3, 0, '0', '0');

-- 平台管理按钮权限：删除 (ID: 50304)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50304, '平台删除', 'common:platform:delete', 'BUTTON', 503, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除平台', TRUE, '1', 4, 0, '0', '0');

-- 智能体子菜单：模型管理 (ID: 504)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (504, '模型管理', 'CommonModel', 'MENU', 500, '/agent/model', NULL, 'carbon:ai-generate', '/agent/model/list', '', TRUE, NULL, 'AI模型管理', TRUE, '1', 4, 0, '0', '0');

-- 模型管理按钮权限：查询 (ID: 50401)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50401, '模型查询', 'common:model:query', 'BUTTON', 504, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询模型列表', TRUE, '1', 1, 0, '0', '0');

-- 模型管理按钮权限：新增 (ID: 50402)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50402, '模型新增', 'common:model:create', 'BUTTON', 504, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增模型', TRUE, '1', 2, 0, '0', '0');

-- 模型管理按钮权限：修改 (ID: 50403)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50403, '模型修改', 'common:model:update', 'BUTTON', 504, NULL, NULL, NULL, NULL, NULL, NULL, 'PATCH', '修改模型', TRUE, '1', 3, 0, '0', '0');

-- 模型管理按钮权限：删除 (ID: 50404)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50404, '模型删除', 'common:model:delete', 'BUTTON', 504, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除模型', TRUE, '1', 4, 0, '0', '0');

-- 智能体子菜单：版本管理 (ID: 505)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (505, '版本管理', 'AgentVersion', 'MENU', 500, '/agent/admin/edit', NULL, 'carbon:version', '/agent/admin/agent-edit', '', FALSE, NULL, 'Agent 版本管理与 Graph 编排（隐藏菜单，请从编辑页面进入）', FALSE, '1', 5, 0, '0', '0');

-- 版本管理已整合到编辑页面中，Graph编排入口移除

-- 智能体子菜单：意图定义管理 (ID: 507)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (507, '意图管理', 'AgentIntent', 'MENU', 500, '/agent/intent', NULL, 'carbon:idea', '/agent/intent/list', '', TRUE, NULL, '意图定义管理', TRUE, '1', 7, 0, '0', '0');


-- 已删除 - 前端 vben.ts 路由已清空

-- ============================================
-- ============================================

-- super 角色的权限（完整权限 - 所有菜单）
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES 
(0, 1), (0, 2), (0, 3), 
(0, 100), (0, 201), (0, 20101), (0, 20102), (0, 20103), (0, 202), (0, 20201), (0, 20202), (0, 20203), (0, 203), (0, 20301), (0, 20302), (0, 20303), (0, 20304), (0, 204), (0, 20401), (0, 20402), (0, 20403), (0, 20404),
(0, 400), (0, 401), (0, 40101), (0, 40102), (0, 40103), (0, 40104), (0, 402), (0, 40201), (0, 40202), (0, 40203), (0, 40204),
(0, 500), (0, 501), (0, 503), (0, 50301), (0, 50302), (0, 50303), (0, 50304), (0, 504), (0, 50401), (0, 50402), (0, 50403), (0, 50404), (0, 505), (0, 507);

-- admin 角色的权限（管理员权限 - 不包含 super 专属）
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES 
(1, 1), (1, 2), (1, 3), 
(1, 100), (1, 201), (1, 20101), (1, 20102), (1, 20103), (1, 202), (1, 20201), (1, 20202), (1, 20203), (1, 203), (1, 20301), (1, 20302), (1, 20303), (1, 20304), (1, 204), (1, 20401), (1, 20402), (1, 20403), (1, 20404),
(1, 400), (1, 401), (1, 40101), (1, 40102), (1, 40103), (1, 40104), (1, 402), (1, 40201), (1, 40202), (1, 40203), (1, 40204),
(1, 500), (1, 501), (1, 503), (1, 50301), (1, 50302), (1, 50303), (1, 50304), (1, 504), (1, 50401), (1, 50402), (1, 50403), (1, 50404), (1, 505), (1, 507);

-- user 角色的权限（基础权限 - 仅 Dashboard）
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES 
(2, 1), (2, 2), (2, 3),
(2, 500), (2, 501), (2, 503), (2, 504), (2, 507);

-- ============================================
-- 7. 初始化权限码数据（根据 API 文档模拟数据）
-- ============================================

-- vben 角色的权限码
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`) VALUES (1, 'AC_100100', '权限码 100100', 1, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`) VALUES (2, 'AC_100110', '权限码 100110', 1, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`) VALUES (3, 'AC_100120', '权限码 100120', 1, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`) VALUES (4, 'AC_100010', '权限码 100010', 1, '1', 0, '0', '0');

-- admin 角色的权限码
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`) VALUES (5, 'AC_100010', '权限码 100010', 2, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`) VALUES (6, 'AC_100020', '权限码 100020', 2, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`) VALUES (7, 'AC_100030', '权限码 100030', 2, '1', 0, '0', '0');

-- user 角色（jack）的权限码
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`) VALUES (8, 'AC_1000001', '权限码 1000001', 3, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`, `create_by`, `update_by`) VALUES (9, 'AC_1000002', '权限码 1000002', 3, '1', 0, '0', '0');

-- ==================== 重置自增序列 ====================
-- H2数据库在手动插入ID后需要重置序列，避免主键冲突
ALTER TABLE `user` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `role` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `menu` ALTER COLUMN `id` RESTART WITH 1000;
ALTER TABLE `auth_code` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `workspace` ALTER COLUMN `id` RESTART WITH 100;
