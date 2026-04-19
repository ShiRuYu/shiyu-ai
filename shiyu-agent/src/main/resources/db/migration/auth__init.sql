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
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `nick_name` VARCHAR(64) COMMENT '昵称',
    `gender` VARCHAR(10) COMMENT '性别',
    `avatar` VARCHAR(255) COMMENT '头像',
    `address` VARCHAR(255) COMMENT '地址',
    `email` VARCHAR(128) COMMENT '邮箱',
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX `idx_username` ON `user` (`username`);
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
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX `idx_code` ON `role` (`code`);
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
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
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
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX `idx_role_code` ON `auth_code` (`role_id`, `code`);
CREATE INDEX `idx_auth_code` ON `auth_code` (`code`);
COMMENT ON TABLE `auth_code` IS '权限码表';

-- ============================================
-- 2. 初始化用户数据（根据 API 文档模拟数据）
-- 注意：密码已使用 BCrypt 加密，原始密码均为: 123456
-- ============================================

-- 用户 vben (ID: 0) - super 角色
INSERT INTO `user` (`id`, `username`, `password`, `status`, `del_flag`, `create_time`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (0, 'vben', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', '1', 0, NOW(), NOW(), 'Vben', NULL, NULL, NULL, NULL);

-- 用户 admin (ID: 1)
INSERT INTO `user` (`id`, `username`, `password`, `status`, `del_flag`, `create_time`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (1, 'admin', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', '1', 0, NOW(), NOW(), 'Admin', NULL, NULL, NULL, NULL);

-- 用户 jack (ID: 2)
INSERT INTO `user` (`id`, `username`, `password`, `status`, `del_flag`, `create_time`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (2, 'jack', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', '1', 0, NOW(), NOW(), 'Jack', NULL, NULL, NULL, NULL);

-- ============================================
-- 3. 初始化角色数据
-- ============================================

-- 角色 super (ID: 0)
INSERT INTO `role` (`id`, `code`, `name`, `status`, `remark`, `del_flag`, `create_time`, `update_time`) 
VALUES (0, 'super', '超级管理员', '1', '拥有系统所有权限', 0, NOW(), NOW());

-- 角色 admin (ID: 1)
INSERT INTO `role` (`id`, `code`, `name`, `status`, `remark`, `del_flag`, `create_time`, `update_time`) 
VALUES (1, 'admin', '管理员', '1', '系统管理员角色', 0, NOW(), NOW());

-- 角色 user (ID: 2)
INSERT INTO `role` (`id`, `code`, `name`, `status`, `remark`, `del_flag`, `create_time`, `update_time`) 
VALUES (2, 'user', '普通用户', '1', '普通用户角色', 0, NOW(), NOW());

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
-- 5. 初始化菜单数据
-- ============================================

-- ==================== Dashboard 模块 ====================

-- 根菜单：Dashboard (ID: 1)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (1, '仪表盘', 'Dashboard', 'MENU', NULL, '/dashboard', '/analytics', 'lucide:layout-dashboard', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, '1', -1, 0);

-- Dashboard 子菜单：分析页 (ID: 2)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (2, '分析页', 'Analytics', 'MENU', 1, '/analytics', NULL, 'lucide:area-chart', '/dashboard/analytics/index', '', TRUE, NULL, NULL, TRUE, '1', 1, 0);

-- Dashboard 子菜单：工作空间 (ID: 3)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (3, '工作空间', 'Workspace', 'MENU', 1, '/workspace', NULL, 'carbon:workspace', '/dashboard/workspace/index', '', TRUE, NULL, NULL, TRUE, '1', 2, 0);

-- ==================== 系统管理模块 ====================

-- 根菜单：系统管理 (ID: 100)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (100, '系统管理', 'System', 'MENU', NULL, '/system', NULL, 'ion:settings-outline', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, '1', 9997, 0);

-- 系统管理子菜单：菜单管理 (ID: 201)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (201, '菜单管理', 'SystemMenu', 'MENU', 100, '/system/menu', NULL, 'carbon:menu', '/system/menu/list', '', TRUE, NULL, NULL, TRUE, '1', 1, 0);

-- 菜单管理按钮权限：新增 (ID: 20101)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (20101, '新增菜单', 'System:Menu:Create', 'BUTTON', 201, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 1, 0);

-- 菜单管理按钮权限：编辑 (ID: 20102)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (20102, '编辑菜单', 'System:Menu:Edit', 'BUTTON', 201, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 2, 0);

-- 菜单管理按钮权限：删除 (ID: 20103)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (20103, '删除菜单', 'System:Menu:Delete', 'BUTTON', 201, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 3, 0);

-- 系统管理子菜单：部门管理 (ID: 202)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (202, '部门管理', 'SystemDept', 'MENU', 100, '/system/dept', NULL, 'carbon:container-services', '/system/dept/list', '', TRUE, NULL, NULL, TRUE, '1', 2, 0);

-- 部门管理按钮权限：新增 (ID: 20201)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (20201, '新增部门', 'System:Dept:Create', 'BUTTON', 202, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 1, 0);

-- 部门管理按钮权限：编辑 (ID: 20202)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (20202, '编辑部门', 'System:Dept:Edit', 'BUTTON', 202, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 2, 0);

-- 部门管理按钮权限：删除 (ID: 20203)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (20203, '删除部门', 'System:Dept:Delete', 'BUTTON', 202, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 3, 0);

-- 系统管理子菜单：角色管理 (ID: 203)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (203, '角色管理', 'SystemRole', 'MENU', 100, '/system/role', NULL, 'carbon:user-role', '/system/role/list', '', TRUE, NULL, NULL, TRUE, '1', 3, 0);

-- 角色管理按钮权限：查询 (ID: 20301)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (20301, '角色查询', 'system:role:query', 'BUTTON', 203, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询角色列表', TRUE, '1', 1, 0);

-- 角色管理按钮权限：新增 (ID: 20302)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (20302, '角色新增', 'system:role:create', 'BUTTON', 203, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增角色', TRUE, '1', 2, 0);

-- 角色管理按钮权限：修改 (ID: 20303)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (20303, '角色修改', 'system:role:update', 'BUTTON', 203, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改角色信息', TRUE, '1', 3, 0);

-- 角色管理按钮权限：删除 (ID: 20304)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (20304, '角色删除', 'system:role:delete', 'BUTTON', 203, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除角色', TRUE, '1', 4, 0);

-- ==================== 日常记录模块 ====================

-- 根菜单：日常记录 (ID: 400)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (400, '日常记录', 'Record', 'MENU', NULL, '/record', NULL, 'mdi:book-open-page-variant', '', '', FALSE, NULL, '日常记录管理目录', TRUE, '1', 5, 0);

-- 日常记录子菜单：人物管理 (ID: 401)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (401, '人物管理', 'RecordProfile', 'MENU', 400, '/record/profile', NULL, 'mdi:account-multiple', '/record/profile/list', '', TRUE, NULL, '人物信息管理', TRUE, '1', 1, 0);

-- 人物管理按钮权限：查询 (ID: 40101)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (40101, '人物查询', 'record:profile:query', 'BUTTON', 401, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询人物列表', TRUE, '1', 1, 0);

-- 人物管理按钮权限：新增 (ID: 40102)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (40102, '人物新增', 'record:profile:add', 'BUTTON', 401, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增人物', TRUE, '1', 2, 0);

-- 人物管理按钮权限：修改 (ID: 40103)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (40103, '人物修改', 'record:profile:edit', 'BUTTON', 401, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改人物信息', TRUE, '1', 3, 0);

-- 人物管理按钮权限：删除 (ID: 40104)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (40104, '人物删除', 'record:profile:remove', 'BUTTON', 401, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除人物', TRUE, '1', 4, 0);

-- 日常记录子菜单：时间轴管理 (ID: 402)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (402, '时间轴管理', 'RecordTimeline', 'MENU', 400, '/record/timeline', NULL, 'mdi:timeline', '/record/timeline/list', '', TRUE, NULL, '时间轴事件管理', TRUE, '1', 2, 0);

-- 时间轴管理按钮权限：查询 (ID: 40201)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (40201, '时间轴查询', 'record:timeline:query', 'BUTTON', 402, NULL, NULL, NULL, NULL, NULL, NULL, 'GET', '查询时间轴事件列表', TRUE, '1', 1, 0);

-- 时间轴管理按钮权限：新增 (ID: 40202)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (40202, '时间轴新增', 'record:timeline:add', 'BUTTON', 402, NULL, NULL, NULL, NULL, NULL, NULL, 'POST', '新增时间轴事件', TRUE, '1', 2, 0);

-- 时间轴管理按钮权限：修改 (ID: 40203)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (40203, '时间轴修改', 'record:timeline:edit', 'BUTTON', 402, NULL, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改时间轴事件', TRUE, '1', 3, 0);

-- 时间轴管理按钮权限：删除 (ID: 40204)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (40204, '时间轴删除', 'record:timeline:remove', 'BUTTON', 402, NULL, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除时间轴事件', TRUE, '1', 4, 0);

-- ==================== 演示模块 ====================

-- 根菜单：Demos (ID: 300)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (300, '演示', 'Demos', 'MENU', NULL, '/demos', '/demos/access', 'ic:baseline-view-in-ar', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, '1', 1000, 0);

-- Demos 子菜单：权限演示 (ID: 301)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (301, '权限控制', 'AccessDemos', 'MENU', 300, '/demos/access', '/demos/access/page-control', 'mdi:cloud-key-outline', '', '', TRUE, NULL, NULL, TRUE, '1', 1, 0);

-- 权限演示子菜单：页面权限 (ID: 30101)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (30101, '页面权限', 'AccessPageControlDemo', 'MENU', 301, '/demos/access/page-control', NULL, 'mdi:page-previous-outline', '/demos/access/index', '', TRUE, NULL, NULL, TRUE, '1', 1, 0);

-- 权限演示子菜单：按钮权限 (ID: 30102)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (30102, '按钮权限', 'AccessButtonControlDemo', 'MENU', 301, '/demos/access/button-control', NULL, 'mdi:button-cursor', '/demos/access/button-control', '', TRUE, NULL, NULL, TRUE, '1', 2, 0);

-- 权限演示子菜单：菜单可见403 (ID: 30103)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (30103, '菜单可见403', 'AccessMenuVisible403Demo', 'MENU', 301, '/demos/access/menu-visible-403', NULL, 'mdi:button-cursor', '/demos/access/menu-visible-403', '', TRUE, NULL, NULL, TRUE, '1', 3, 0);

-- 权限演示子菜单：Super专属 (ID: 30104)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (30104, 'Super专属页面', 'AccessSuperVisibleDemo', 'MENU', 301, '/demos/access/super-visible', NULL, 'mdi:button-cursor', '/demos/access/super-visible', '', TRUE, NULL, NULL, TRUE, '1', 4, 0);

-- 权限演示子菜单：Admin专属 (ID: 30105)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (30105, 'Admin专属页面', 'AccessAdminVisibleDemo', 'MENU', 301, '/demos/access/admin-visible', NULL, 'mdi:button-cursor', '/demos/access/admin-visible', '', TRUE, NULL, NULL, TRUE, '1', 5, 0);

-- 权限演示子菜单：User专属 (ID: 30106)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (30106, 'User专属页面', 'AccessUserVisibleDemo', 'MENU', 301, '/demos/access/user-visible', NULL, 'mdi:button-cursor', '/demos/access/user-visible', '', TRUE, NULL, NULL, TRUE, '1', 6, 0);

-- ==================== Vben Admin 项目模块 ====================

-- 根菜单：Vben Admin (ID: 900)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (900, 'Vben Admin', 'Project', 'MENU', NULL, '/vben-admin', NULL, 'carbon:data-center', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, '1', 9998, 0);

-- Vben Admin 子菜单：文档 (ID: 901)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (901, 'Vben 文档', 'VbenDocument', 'MENU', 900, '/vben-admin/document', NULL, 'carbon:book', 'IFrameView', '', FALSE, NULL, 'https://doc.vben.pro', TRUE, '1', 1, 0);

-- Vben Admin 子菜单：GitHub (ID: 902)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (902, 'GitHub', 'VbenGithub', 'MENU', 900, '/vben-admin/github', NULL, 'carbon:logo-github', 'IFrameView', '', FALSE, NULL, 'https://github.com/vbenjs/vue-vben-admin', TRUE, '1', 2, 0);

-- Vben Admin 子菜单：Antdv (ID: 903) - 禁用状态
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (903, 'Ant Design Vue', 'VbenAntdv', 'MENU', 900, '/vben-admin/antdv', NULL, 'carbon:hexagon-vertical-solid', 'IFrameView', '', FALSE, NULL, 'https://ant.vben.pro', TRUE, '0', 3, 0);

-- ==================== 关于页面 ====================

-- 根菜单：关于 (ID: 1000)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`) 
VALUES (1000, '关于', 'About', 'MENU', NULL, '/about', NULL, 'lucide:copyright', '_core/about/index', '', TRUE, NULL, NULL, TRUE, '1', 9999, 0);

-- ============================================
-- 6. 初始化角色菜单关联数据
-- ============================================

-- super 角色的权限（完整权限 - 所有菜单）
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES 
(0, 1), (0, 2), (0, 3), 
(0, 100), (0, 201), (0, 20101), (0, 20102), (0, 20103), (0, 202), (0, 20201), (0, 20202), (0, 20203), (0, 203), (0, 20301), (0, 20302), (0, 20303), (0, 20304),
(0, 400), (0, 401), (0, 40101), (0, 40102), (0, 40103), (0, 40104), (0, 402), (0, 40201), (0, 40202), (0, 40203), (0, 40204),
(0, 300), (0, 301), (0, 30101), (0, 30102), (0, 30103), (0, 30104), (0, 30105), (0, 30106),
(0, 900), (0, 901), (0, 902), (0, 903),
(0, 1000);

-- admin 角色的权限（管理员权限 - 不包含 super 专属）
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES 
(1, 1), (1, 2), (1, 3), 
(1, 100), (1, 201), (1, 20101), (1, 20102), (1, 20103), (1, 202), (1, 20201), (1, 20202), (1, 20203), (1, 203), (1, 20301), (1, 20302), (1, 20303), (1, 20304),
(1, 400), (1, 401), (1, 40101), (1, 40102), (1, 40103), (1, 40104), (1, 402), (1, 40201), (1, 40202), (1, 40203), (1, 40204),
(1, 300), (1, 301), (1, 30101), (1, 30102), (1, 30103), (1, 30105),
(1, 900), (1, 901), (1, 902), (1, 903),
(1, 1000);

-- user 角色的权限（基础权限 - 仅 Dashboard）
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES 
(2, 1), (2, 2), (2, 3),
(2, 300), (2, 301), (2, 30101), (2, 30102), (2, 30103), (2, 30106),
(2, 900), (2, 901), (2, 902),
(2, 1000);

-- ============================================
-- 7. 初始化权限码数据（根据 API 文档模拟数据）
-- ============================================

-- vben 角色的权限码
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`) VALUES (1, 'AC_100100', '权限码 100100', 1, '1', 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`) VALUES (2, 'AC_100110', '权限码 100110', 1, '1', 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`) VALUES (3, 'AC_100120', '权限码 100120', 1, '1', 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`) VALUES (4, 'AC_100010', '权限码 100010', 1, '1', 0);

-- admin 角色的权限码
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`) VALUES (5, 'AC_100010', '权限码 100010', 2, '1', 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`) VALUES (6, 'AC_100020', '权限码 100020', 2, '1', 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`) VALUES (7, 'AC_100030', '权限码 100030', 2, '1', 0);

-- user 角色（jack）的权限码
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`) VALUES (8, 'AC_1000001', '权限码 1000001', 3, '1', 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `status`, `del_flag`) VALUES (9, 'AC_1000002', '权限码 1000002', 3, '1', 0);

-- ==================== 重置自增序列 ====================
-- H2数据库在手动插入ID后需要重置序列，避免主键冲突
ALTER TABLE `user` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `role` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `menu` ALTER COLUMN `id` RESTART WITH 1000;
ALTER TABLE `auth_code` ALTER COLUMN `id` RESTART WITH 100;
