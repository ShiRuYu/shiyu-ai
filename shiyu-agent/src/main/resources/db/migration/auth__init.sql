-- ============================================
-- 数据库初始化脚本
-- 包含表结构创建和初始数据插入
-- ============================================

-- ============================================
-- 1. 创建表结构
-- ============================================

-- 租户表
DROP TABLE IF EXISTS `tenant`;
CREATE TABLE `tenant` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '租户ID',
    `code`          VARCHAR(64)  NOT NULL COMMENT '租户编码',
    `name`          VARCHAR(128) COMMENT '租户名称',
    `contact_name`  VARCHAR(64)  COMMENT '联系人',
    `contact_phone` VARCHAR(20)  COMMENT '联系电话',
    `address`       VARCHAR(255) COMMENT '地址',
    `domain`        VARCHAR(255) COMMENT '域名',
    `intro`         VARCHAR(500) COMMENT '简介',
    `status`        CHAR(1)      DEFAULT '1' COMMENT '状态（1正常 0停用）',
    `del_flag`      TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`     VARCHAR(64)  COMMENT '创建者',
    `create_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     VARCHAR(64)  COMMENT '更新者',
    `update_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE INDEX `idx_tenant_code` ON `tenant` (`code`);
COMMENT ON TABLE `tenant` IS '租户表';

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) COMMENT '密码',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
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
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
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
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
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
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    PRIMARY KEY (`user_id`, `role_id`)
);
CREATE INDEX `idx_role_id` ON `user_role` (`role_id`);
COMMENT ON TABLE `user_role` IS '用户角色关联表';

-- 角色菜单关联表
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu` (
    `role_id` BIGINT NOT NULL COMMENT '角色 ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单 ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
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
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
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
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
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

-- 用户空间角色关联表
DROP TABLE IF EXISTS `user_workspace_role`;
CREATE TABLE `user_workspace_role` (
    `user_id`      BIGINT NOT NULL COMMENT '用户 ID',
    `workspace_id` BIGINT NOT NULL COMMENT '工作空间 ID',
    `role_id`      BIGINT NOT NULL COMMENT '角色 ID',
    `tenant_id`    BIGINT NOT NULL COMMENT '租户ID',
    PRIMARY KEY (`user_id`, `workspace_id`, `role_id`)
);
CREATE INDEX `idx_uwr_workspace` ON `user_workspace_role` (`workspace_id`);
CREATE INDEX `idx_uwr_role` ON `user_workspace_role` (`role_id`);
COMMENT ON TABLE `user_workspace_role` IS '用户空间角色关联表';

-- ============================================
-- 2. 初始化租户数据
-- ============================================

INSERT INTO `tenant` (`id`, `code`, `name`, `contact_name`, `contact_phone`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 'default', '默认租户', 'Admin', '13800000000', '1', 0, '0', '0');

-- ============================================
-- 3. 初始化用户数据
-- ============================================

INSERT INTO `user` (`id`, `username`, `password`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (0, 'vben', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, '1', 0, '0', NOW(), '0', NOW(), 'Vben', NULL, NULL, NULL, NULL);

INSERT INTO `user` (`id`, `username`, `password`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (1, 'admin', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, '1', 0, '0', NOW(), '0', NOW(), 'Admin', NULL, NULL, NULL, NULL);

INSERT INTO `user` (`id`, `username`, `password`, `tenant_id`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (2, 'jack', '$2a$10$upTL84vHb86f9vMVMn4m8uOGqGr9Pedo.CCsg.XmZ62xhU2IIHJvy', 1, '1', 0, '0', NOW(), '0', NOW(), 'Jack', NULL, NULL, NULL, NULL);

-- ============================================
-- 4. 初始化角色数据
-- ============================================

INSERT INTO `role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) 
VALUES (0, 'super', '超级管理员', 1, '1', '拥有系统所有权限', 0, '0', NOW(), '0', NOW());

INSERT INTO `role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) 
VALUES (1, 'admin', '管理员', 1, '1', '系统管理员角色', 0, '0', NOW(), '0', NOW());

INSERT INTO `role` (`id`, `code`, `name`, `tenant_id`, `status`, `remark`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) 
VALUES (2, 'user', '普通用户', 1, '1', '普通用户角色', 0, '0', NOW(), '0', NOW());

-- ============================================
-- 5. 初始化用户角色关联数据
-- ============================================

INSERT INTO `user_role` (`user_id`, `role_id`, `tenant_id`) VALUES (0, 0, 1);
INSERT INTO `user_role` (`user_id`, `role_id`, `tenant_id`) VALUES (1, 1, 1);
INSERT INTO `user_role` (`user_id`, `role_id`, `tenant_id`) VALUES (2, 2, 1);

-- ============================================
-- 5.1 初始化用户空间角色数据
-- ============================================

INSERT INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`)
VALUES (0, 0, 0, 1);

INSERT INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`)
VALUES (1, 0, 1, 1);

INSERT INTO `user_workspace_role` (`user_id`, `workspace_id`, `role_id`, `tenant_id`)
VALUES (2, 0, 2, 1);

-- ============================================
-- 6. 初始化工作空间数据
-- ============================================

INSERT INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (0, 0, '默认空间', 1, 0, NULL, NULL, NULL, '1', '系统默认工作空间', 0, '0', '0');

INSERT INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 0, '总公司', 1, 1, 'Vben', '15888888888', 'vben@shiyu.com', '1', '公司顶层组织', 0, '0', '0');

INSERT INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (2, 1, '技术部', 1, 1, NULL, NULL, NULL, '1', '研发技术部门', 0, '0', '0');

INSERT INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (3, 1, '产品部', 1, 2, NULL, NULL, NULL, '1', '产品部门', 0, '0', '0');

INSERT INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (4, 2, '前端组', 1, 1, NULL, NULL, NULL, '1', NULL, 0, '0', '0');

INSERT INTO `workspace` (`id`, `parent_id`, `name`, `tenant_id`, `order`, `leader`, `phone`, `email`, `status`, `remark`, `del_flag`, `create_by`, `update_by`)
VALUES (5, 2, '后端组', 1, 2, NULL, NULL, NULL, '1', NULL, 0, '0', '0');

-- ============================================
-- 7. 初始化菜单数据
-- ============================================

-- ==================== Dashboard 模块 ====================

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (1, '仪表盘', 'Dashboard', 'MENU', NULL, 1, '/dashboard', '/analytics', 'lucide:layout-dashboard', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, '1', -1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (2, '分析页', 'Analytics', 'MENU', 1, 1, '/analytics', NULL, 'lucide:area-chart', '/dashboard/analytics/index', '', TRUE, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (3, '工作空间', 'Workspace', 'MENU', 1, 1, '/workspace', NULL, 'carbon:workspace', '/dashboard/workspace/index', '', TRUE, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

-- ==================== 系统管理模块 ====================

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (100, '系统管理', 'System', 'MENU', NULL, 1, '/system', NULL, 'ion:settings-outline', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, '1', 9997, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (201, '菜单管理', 'SystemMenu', 'MENU', 100, 1, '/system/menu', NULL, 'carbon:menu', '/system/menu/list', '', TRUE, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20101, '新增菜单', 'System:Menu:Create', 'BUTTON', 201, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20102, '编辑菜单', 'System:Menu:Edit', 'BUTTON', 201, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20103, '删除菜单', 'System:Menu:Delete', 'BUTTON', 201, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 3, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (202, '工作空间管理', 'SystemWorkspace', 'MENU', 100, 1, '/system/workspace', NULL, 'carbon:container-services', '/system/workspace/list', '', TRUE, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20201, '新增工作空间', 'System:Workspace:Create', 'BUTTON', 202, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20202, '编辑工作空间', 'System:Workspace:Edit', 'BUTTON', 202, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 2, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20203, '删除工作空间', 'System:Workspace:Delete', 'BUTTON', 202, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, '1', 3, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (203, '角色管理', 'SystemRole', 'MENU', 100, 1, '/system/role', NULL, 'carbon:user-role', '/system/role/list', '', TRUE, NULL, NULL, TRUE, '1', 3, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20301, '角色查询', 'system:role:query', 'BUTTON', 203, 1, NULL, NULL, NULL, NULL, NULL, 'GET', '查询角色列表', TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20302, '角色新增', 'system:role:create', 'BUTTON', 203, 1, NULL, NULL, NULL, NULL, NULL, 'POST', '新增角色', TRUE, '1', 2, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20303, '角色修改', 'system:role:update', 'BUTTON', 203, 1, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改角色信息', TRUE, '1', 3, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20304, '角色删除', 'system:role:delete', 'BUTTON', 203, 1, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除角色', TRUE, '1', 4, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (204, '字典管理', 'SystemDict', 'MENU', 100, 1, '/system/dict', NULL, 'carbon:data-table', '/common/dict/list', '', TRUE, NULL, NULL, TRUE, '1', 4, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20401, '字典查询', 'system:dict:query', 'BUTTON', 204, 1, NULL, NULL, NULL, NULL, NULL, 'GET', '查询字典列表', TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20402, '字典新增', 'system:dict:create', 'BUTTON', 204, 1, NULL, NULL, NULL, NULL, NULL, 'POST', '新增字典', TRUE, '1', 2, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20403, '字典修改', 'system:dict:update', 'BUTTON', 204, 1, NULL, NULL, NULL, NULL, NULL, 'PATCH', '修改字典', TRUE, '1', 3, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (20404, '字典删除', 'system:dict:delete', 'BUTTON', 204, 1, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除字典', TRUE, '1', 4, 0, '0', '0');

-- ==================== 日常记录模块 ====================

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (400, '日常记录', 'Record', 'MENU', NULL, 1, '/record', NULL, 'mdi:book-open-page-variant', '', '', FALSE, NULL, '日常记录管理目录', TRUE, '1', 5, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (401, '人物管理', 'RecordProfile', 'MENU', 400, 1, '/record/profile', NULL, 'mdi:account-multiple', '/record/profile/list', '', TRUE, NULL, '人物信息管理', TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40101, '人物查询', 'record:profile:query', 'BUTTON', 401, 1, NULL, NULL, NULL, NULL, NULL, 'GET', '查询人物列表', TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40102, '人物新增', 'record:profile:add', 'BUTTON', 401, 1, NULL, NULL, NULL, NULL, NULL, 'POST', '新增人物', TRUE, '1', 2, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40103, '人物修改', 'record:profile:edit', 'BUTTON', 401, 1, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改人物信息', TRUE, '1', 3, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40104, '人物删除', 'record:profile:remove', 'BUTTON', 401, 1, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除人物', TRUE, '1', 4, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (402, '时间轴管理', 'RecordTimeline', 'MENU', 400, 1, '/record/timeline', NULL, 'mdi:timeline', '/record/timeline/list', '', TRUE, NULL, '时间轴事件管理', TRUE, '1', 2, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40201, '时间轴查询', 'record:timeline:query', 'BUTTON', 402, 1, NULL, NULL, NULL, NULL, NULL, 'GET', '查询时间轴事件列表', TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40202, '时间轴新增', 'record:timeline:add', 'BUTTON', 402, 1, NULL, NULL, NULL, NULL, NULL, 'POST', '新增时间轴事件', TRUE, '1', 2, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40203, '时间轴修改', 'record:timeline:edit', 'BUTTON', 402, 1, NULL, NULL, NULL, NULL, NULL, 'PUT', '修改时间轴事件', TRUE, '1', 3, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (40204, '时间轴删除', 'record:timeline:remove', 'BUTTON', 402, 1, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除时间轴事件', TRUE, '1', 4, 0, '0', '0');

-- ==================== 智能体模块 ====================

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (500, '智能体', 'Agent', 'MENU', NULL, 1, '/agent', NULL, 'carbon:ibm-watson-assistant', '', '', FALSE, NULL, 'AI智能体管理', TRUE, '1', 6, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (501, 'Agent管理', 'AgentAdminList', 'MENU', 500, 1, '/agent/admin/list', NULL, 'carbon:cube', '/agent/admin/agent-list', '', TRUE, NULL, 'Agent 注册与管理', TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (503, '平台管理', 'CommonPlatform', 'MENU', 500, 1, '/agent/platform', NULL, 'carbon:cloud', '/agent/platform/list', '', TRUE, NULL, 'AI平台管理', TRUE, '1', 3, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50301, '平台查询', 'common:platform:query', 'BUTTON', 503, 1, NULL, NULL, NULL, NULL, NULL, 'GET', '查询平台列表', TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50302, '平台新增', 'common:platform:create', 'BUTTON', 503, 1, NULL, NULL, NULL, NULL, NULL, 'POST', '新增平台', TRUE, '1', 2, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50303, '平台修改', 'common:platform:update', 'BUTTON', 503, 1, NULL, NULL, NULL, NULL, NULL, 'PATCH', '修改平台', TRUE, '1', 3, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50304, '平台删除', 'common:platform:delete', 'BUTTON', 503, 1, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除平台', TRUE, '1', 4, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (504, '模型管理', 'CommonModel', 'MENU', 500, 1, '/agent/model', NULL, 'carbon:ai-generate', '/agent/model/list', '', TRUE, NULL, 'AI模型管理', TRUE, '1', 4, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50401, '模型查询', 'common:model:query', 'BUTTON', 504, 1, NULL, NULL, NULL, NULL, NULL, 'GET', '查询模型列表', TRUE, '1', 1, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50402, '模型新增', 'common:model:create', 'BUTTON', 504, 1, NULL, NULL, NULL, NULL, NULL, 'POST', '新增模型', TRUE, '1', 2, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50403, '模型修改', 'common:model:update', 'BUTTON', 504, 1, NULL, NULL, NULL, NULL, NULL, 'PATCH', '修改模型', TRUE, '1', 3, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (50404, '模型删除', 'common:model:delete', 'BUTTON', 504, 1, NULL, NULL, NULL, NULL, NULL, 'DELETE', '删除模型', TRUE, '1', 4, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (505, '版本管理', 'AgentVersion', 'MENU', 500, 1, '/agent/admin/edit', NULL, 'carbon:version', '/agent/admin/agent-edit', '', FALSE, NULL, 'Agent 版本管理与 Graph 编排（隐藏菜单，请从编辑页面进入）', FALSE, '1', 5, 0, '0', '0');

INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `tenant_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `status`, `order`, `del_flag`, `create_by`, `update_by`) 
VALUES (507, '意图管理', 'AgentIntent', 'MENU', 500, 1, '/agent/intent', NULL, 'carbon:idea', '/agent/intent/list', '', TRUE, NULL, '意图定义管理', TRUE, '1', 7, 0, '0', '0');

-- ============================================
-- 8. 初始化角色菜单关联数据
-- ============================================

-- super 角色的权限（完整权限 - 所有菜单）
INSERT INTO `role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES 
(0, 1, 1), (0, 2, 1), (0, 3, 1), 
(0, 100, 1), (0, 201, 1), (0, 20101, 1), (0, 20102, 1), (0, 20103, 1), (0, 202, 1), (0, 20201, 1), (0, 20202, 1), (0, 20203, 1), (0, 203, 1), (0, 20301, 1), (0, 20302, 1), (0, 20303, 1), (0, 20304, 1), (0, 204, 1), (0, 20401, 1), (0, 20402, 1), (0, 20403, 1), (0, 20404, 1),
(0, 400, 1), (0, 401, 1), (0, 40101, 1), (0, 40102, 1), (0, 40103, 1), (0, 40104, 1), (0, 402, 1), (0, 40201, 1), (0, 40202, 1), (0, 40203, 1), (0, 40204, 1),
(0, 500, 1), (0, 501, 1), (0, 503, 1), (0, 50301, 1), (0, 50302, 1), (0, 50303, 1), (0, 50304, 1), (0, 504, 1), (0, 50401, 1), (0, 50402, 1), (0, 50403, 1), (0, 50404, 1), (0, 505, 1), (0, 507, 1);

-- admin 角色的权限（管理员权限 - 不包含 super 专属）
INSERT INTO `role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES 
(1, 1, 1), (1, 2, 1), (1, 3, 1), 
(1, 100, 1), (1, 201, 1), (1, 20101, 1), (1, 20102, 1), (1, 20103, 1), (1, 202, 1), (1, 20201, 1), (1, 20202, 1), (1, 20203, 1), (1, 203, 1), (1, 20301, 1), (1, 20302, 1), (1, 20303, 1), (1, 20304, 1), (1, 204, 1), (1, 20401, 1), (1, 20402, 1), (1, 20403, 1), (1, 20404, 1),
(1, 400, 1), (1, 401, 1), (1, 40101, 1), (1, 40102, 1), (1, 40103, 1), (1, 40104, 1), (1, 402, 1), (1, 40201, 1), (1, 40202, 1), (1, 40203, 1), (1, 40204, 1),
(1, 500, 1), (1, 501, 1), (1, 503, 1), (1, 50301, 1), (1, 50302, 1), (1, 50303, 1), (1, 50304, 1), (1, 504, 1), (1, 50401, 1), (1, 50402, 1), (1, 50403, 1), (1, 50404, 1), (1, 505, 1), (1, 507, 1);

-- user 角色的权限（基础权限 - 仅 Dashboard + Agent 可见）
INSERT INTO `role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES 
(2, 1, 1), (2, 2, 1), (2, 3, 1),
(2, 500, 1), (2, 501, 1), (2, 503, 1), (2, 504, 1), (2, 507, 1);

-- ============================================
-- 9. 初始化权限码数据
-- ============================================

INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (1, 'AC_100100', '权限码 100100', 1, 1, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (2, 'AC_100110', '权限码 100110', 1, 1, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (3, 'AC_100120', '权限码 100120', 1, 1, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (4, 'AC_100010', '权限码 100010', 1, 1, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (5, 'AC_100010', '权限码 100010', 2, 1, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (6, 'AC_100020', '权限码 100020', 2, 1, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (7, 'AC_100030', '权限码 100030', 2, 1, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (8, 'AC_1000001', '权限码 1000001', 3, 1, '1', 0, '0', '0');
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `tenant_id`, `status`, `del_flag`, `create_by`, `update_by`) 
VALUES (9, 'AC_1000002', '权限码 1000002', 3, 1, '1', 0, '0', '0');

-- ==================== 重置自增序列 ====================
ALTER TABLE `tenant` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `user` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `role` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `menu` ALTER COLUMN `id` RESTART WITH 1000;
ALTER TABLE `auth_code` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `workspace` ALTER COLUMN `id` RESTART WITH 100;
