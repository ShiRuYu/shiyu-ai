  -- ============================================
-- 数据库初始化脚本
-- 包含表结构创建和初始数据插入
-- ============================================

-- ============================================
-- 1. 创建表结构
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL COMMENT '用户 ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) COMMENT '密码',
    `enable` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `nick_name` VARCHAR(64) COMMENT '昵称',
    `gender` VARCHAR(10) COMMENT '性别',
    `avatar` VARCHAR(255) COMMENT '头像',
    `address` VARCHAR(255) COMMENT '地址',
    `email` VARCHAR(128) COMMENT '邮箱',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_username` (`username`)
) COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT NOT NULL COMMENT '角色 ID',
    `code` VARCHAR(64) NOT NULL COMMENT '角色编码',
    `name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `enable` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_code` (`code`)
) COMMENT='角色表';

-- 菜单表
CREATE TABLE IF NOT EXISTS `menu` (
    `id` BIGINT NOT NULL COMMENT '菜单 ID',
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
    `enable` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `order` INT DEFAULT 0 COMMENT '排序',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`)
) COMMENT='菜单表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `role_id` BIGINT NOT NULL COMMENT '角色 ID',
    PRIMARY KEY (`user_id`, `role_id`),
    INDEX `idx_role_id` (`role_id`)
) COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS `role_menu` (
    `role_id` BIGINT NOT NULL COMMENT '角色 ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单 ID',
    PRIMARY KEY (`role_id`, `menu_id`),
    INDEX `idx_menu_id` (`menu_id`)
) COMMENT='角色菜单关联表';

-- 权限码表
CREATE TABLE IF NOT EXISTS `auth_code` (
    `id` BIGINT NOT NULL COMMENT '权限码 ID',
    `code` VARCHAR(64) NOT NULL COMMENT '权限编码',
    `name` VARCHAR(128) COMMENT '权限名称',
    `role_id` BIGINT NOT NULL COMMENT '角色 ID',
    `enable` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_role_code` (`role_id`, `code`),
    INDEX `idx_code` (`code`)
) COMMENT='权限码表';

-- 字典表
CREATE TABLE IF NOT EXISTS `dict` (
    `id` BIGINT NOT NULL COMMENT '字典ID',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
    `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典键值',
    `dict_sort` INT DEFAULT 0 COMMENT '字典排序',
    `css_class` VARCHAR(100) COMMENT '样式属性',
    `list_class` VARCHAR(100) COMMENT '表格回显样式',
    `is_default` CHAR(1) DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
    `status` CHAR(1) DEFAULT '1' COMMENT '状态（1正常 0停用）',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_dict_type` (`dict_type`),
    INDEX `idx_dict_sort` (`dict_sort`)
) COMMENT='字典表';

-- ============================================
-- 2. 初始化用户数据（根据 API 文档模拟数据）
-- ============================================

-- 用户 vben (ID: 0) - super 角色
INSERT INTO `user` (`id`, `username`, `password`, `enable`, `del_flag`, `create_time`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (0, 'vben', '123456', TRUE, 0, NOW(), NOW(), 'Vben', NULL, NULL, NULL, NULL);

-- 用户 admin (ID: 1)
INSERT INTO `user` (`id`, `username`, `password`, `enable`, `del_flag`, `create_time`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (1, 'admin', '123456', TRUE, 0, NOW(), NOW(), 'Admin', NULL, NULL, NULL, NULL);

-- 用户 jack (ID: 2)
INSERT INTO `user` (`id`, `username`, `password`, `enable`, `del_flag`, `create_time`, `update_time`, `nick_name`, `gender`, `avatar`, `address`, `email`) 
VALUES (2, 'jack', '123456', TRUE, 0, NOW(), NOW(), 'Jack', NULL, NULL, NULL, NULL);

-- ============================================
-- 3. 初始化角色数据
-- ============================================

-- 角色 super (ID: 0)
INSERT INTO `role` (`id`, `code`, `name`, `enable`, `del_flag`, `create_time`, `update_time`) 
VALUES (0, 'super', '超级管理员', TRUE, 0, NOW(), NOW());

-- 角色 admin (ID: 1)
INSERT INTO `role` (`id`, `code`, `name`, `enable`, `del_flag`, `create_time`, `update_time`) 
VALUES (1, 'admin', '管理员', TRUE, 0, NOW(), NOW());

-- 角色 user (ID: 2)
INSERT INTO `role` (`id`, `code`, `name`, `enable`, `del_flag`, `create_time`, `update_time`) 
VALUES (2, 'user', '普通用户', TRUE, 0, NOW(), NOW());

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
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (1, '仪表盘', 'Dashboard', 'MENU', NULL, '/dashboard', '/analytics', 'i-fe:home', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, TRUE, -1, 0);

-- Dashboard 子菜单：分析页 (ID: 2)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (2, '分析页', 'Analytics', 'MENU', 1, '/analytics', NULL, 'i-fe:bar-chart', '/dashboard/analytics/index', '', TRUE, NULL, NULL, TRUE, TRUE, 1, 0);

-- Dashboard 子菜单：工作空间 (ID: 3)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (3, '工作空间', 'Workspace', 'MENU', 1, '/workspace', NULL, 'carbon:workspace', '/dashboard/workspace/index', '', TRUE, NULL, NULL, TRUE, TRUE, 2, 0);

-- ==================== 系统管理模块 ====================

-- 根菜单：系统管理 (ID: 100)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (100, '系统管理', 'System', 'MENU', NULL, '/system', NULL, 'i-fe:settings', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, TRUE, 9997, 0);

-- 系统管理子菜单：菜单管理 (ID: 201)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (201, '菜单管理', 'SystemMenu', 'MENU', 100, '/system/menu', NULL, 'carbon:menu', '/system/menu/list', '', TRUE, NULL, NULL, TRUE, TRUE, 1, 0);

-- 菜单管理按钮权限：新增 (ID: 20101)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (20101, '新增菜单', 'System:Menu:Create', 'BUTTON', 201, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, TRUE, 1, 0);

-- 菜单管理按钮权限：编辑 (ID: 20102)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (20102, '编辑菜单', 'System:Menu:Edit', 'BUTTON', 201, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, TRUE, 2, 0);

-- 菜单管理按钮权限：删除 (ID: 20103)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (20103, '删除菜单', 'System:Menu:Delete', 'BUTTON', 201, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, TRUE, 3, 0);

-- 系统管理子菜单：部门管理 (ID: 202)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (202, '部门管理', 'SystemDept', 'MENU', 100, '/system/dept', NULL, 'carbon:container-services', '/system/dept/list', '', TRUE, NULL, NULL, TRUE, TRUE, 2, 0);

-- 部门管理按钮权限：新增 (ID: 20201)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (20201, '新增部门', 'System:Dept:Create', 'BUTTON', 202, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, TRUE, 1, 0);

-- 部门管理按钮权限：编辑 (ID: 20202)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (20202, '编辑部门', 'System:Dept:Edit', 'BUTTON', 202, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, TRUE, 2, 0);

-- 部门管理按钮权限：删除 (ID: 20203)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (20203, '删除部门', 'System:Dept:Delete', 'BUTTON', 202, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE, TRUE, 3, 0);

-- ==================== 演示模块 ====================

-- 根菜单：Demos (ID: 300)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (300, '演示', 'Demos', 'MENU', NULL, '/demos', '/demos/access', 'ic:baseline-view-in-ar', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, TRUE, 1000, 0);

-- Demos 子菜单：权限演示 (ID: 301)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (301, '权限控制', 'AccessDemos', 'MENU', 300, '/demos/access', '/demos/access/page-control', 'mdi:cloud-key-outline', '', '', TRUE, NULL, NULL, TRUE, TRUE, 1, 0);

-- 权限演示子菜单：页面权限 (ID: 30101)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (30101, '页面权限', 'AccessPageControlDemo', 'MENU', 301, '/demos/access/page-control', NULL, 'mdi:page-previous-outline', '/demos/access/index', '', TRUE, NULL, NULL, TRUE, TRUE, 1, 0);

-- 权限演示子菜单：按钮权限 (ID: 30102)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (30102, '按钮权限', 'AccessButtonControlDemo', 'MENU', 301, '/demos/access/button-control', NULL, 'mdi:button-cursor', '/demos/access/button-control', '', TRUE, NULL, NULL, TRUE, TRUE, 2, 0);

-- 权限演示子菜单：菜单可见403 (ID: 30103)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (30103, '菜单可见403', 'AccessMenuVisible403Demo', 'MENU', 301, '/demos/access/menu-visible-403', NULL, 'mdi:button-cursor', '/demos/access/menu-visible-403', '', TRUE, NULL, NULL, TRUE, TRUE, 3, 0);

-- 权限演示子菜单：Super专属 (ID: 30104)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (30104, 'Super专属页面', 'AccessSuperVisibleDemo', 'MENU', 301, '/demos/access/super-visible', NULL, 'mdi:button-cursor', '/demos/access/super-visible', '', TRUE, NULL, NULL, TRUE, TRUE, 4, 0);

-- 权限演示子菜单：Admin专属 (ID: 30105)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (30105, 'Admin专属页面', 'AccessAdminVisibleDemo', 'MENU', 301, '/demos/access/admin-visible', NULL, 'mdi:button-cursor', '/demos/access/admin-visible', '', TRUE, NULL, NULL, TRUE, TRUE, 5, 0);

-- 权限演示子菜单：User专属 (ID: 30106)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (30106, 'User专属页面', 'AccessUserVisibleDemo', 'MENU', 301, '/demos/access/user-visible', NULL, 'mdi:button-cursor', '/demos/access/user-visible', '', TRUE, TRUE, 6, 0);

-- ==================== Vben Admin 项目模块 ====================

-- 根菜单：Vben Admin (ID: 900)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (900, 'Vben Admin', 'Project', 'MENU', NULL, '/vben-admin', NULL, 'carbon:data-center', 'BasicLayout', '', TRUE, NULL, NULL, TRUE, TRUE, 9998, 0);

-- Vben Admin 子菜单：文档 (ID: 901)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (901, 'Vben 文档', 'VbenDocument', 'MENU', 900, '/vben-admin/document', NULL, 'carbon:book', 'IFrameView', '', FALSE, NULL, 'https://doc.vben.pro', TRUE, TRUE, 1, 0);

-- Vben Admin 子菜单：GitHub (ID: 902)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (902, 'GitHub', 'VbenGithub', 'MENU', 900, '/vben-admin/github', NULL, 'carbon:logo-github', 'IFrameView', '', FALSE, NULL, 'https://github.com/vbenjs/vue-vben-admin', TRUE, TRUE, 2, 0);

-- Vben Admin 子菜单：Antdv (ID: 903) - 禁用状态
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (903, 'Ant Design Vue', 'VbenAntdv', 'MENU', 900, '/vben-admin/antdv', NULL, 'carbon:hexagon-vertical-solid', 'IFrameView', '', FALSE, NULL, 'https://ant.vben.pro', TRUE, FALSE, 3, 0);

-- ==================== 关于页面 ====================

-- 根菜单：关于 (ID: 1000)
INSERT INTO `menu` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `redirect`, `icon`, `component`, `layout`, `keep_alive`, `method`, `description`, `show`, `enable`, `order`, `del_flag`) 
VALUES (1000, '关于', 'About', 'MENU', NULL, '/about', NULL, 'lucide:copyright', '_core/about/index', '', TRUE, NULL, NULL, TRUE, TRUE, 9999, 0);

-- ============================================
-- 6. 初始化角色菜单关联数据
-- ============================================

-- super 角色的权限（完整权限 - 所有菜单）
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES 
(0, 1), (0, 2), (0, 3), 
(0, 100), (0, 201), (0, 20101), (0, 20102), (0, 20103), (0, 202), (0, 20201), (0, 20202), (0, 20203),
(0, 300), (0, 301), (0, 30101), (0, 30102), (0, 30103), (0, 30104), (0, 30105), (0, 30106),
(0, 900), (0, 901), (0, 902), (0, 903),
(0, 1000);

-- admin 角色的权限（管理员权限 - 不包含 super 专属）
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES 
(1, 1), (1, 2), (1, 3), 
(1, 100), (1, 201), (1, 20101), (1, 20102), (1, 20103), (1, 202), (1, 20201), (1, 20202), (1, 20203),
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
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `enable`, `del_flag`) VALUES (1, 'AC_100100', '权限码 100100', 1, TRUE, 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `enable`, `del_flag`) VALUES (2, 'AC_100110', '权限码 100110', 1, TRUE, 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `enable`, `del_flag`) VALUES (3, 'AC_100120', '权限码 100120', 1, TRUE, 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `enable`, `del_flag`) VALUES (4, 'AC_100010', '权限码 100010', 1, TRUE, 0);

-- admin 角色的权限码
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `enable`, `del_flag`) VALUES (5, 'AC_100010', '权限码 100010', 2, TRUE, 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `enable`, `del_flag`) VALUES (6, 'AC_100020', '权限码 100020', 2, TRUE, 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `enable`, `del_flag`) VALUES (7, 'AC_100030', '权限码 100030', 2, TRUE, 0);

-- user 角色（jack）的权限码
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `enable`, `del_flag`) VALUES (8, 'AC_1000001', '权限码 1000001', 3, TRUE, 0);
INSERT INTO `auth_code` (`id`, `code`, `name`, `role_id`, `enable`, `del_flag`) VALUES (9, 'AC_1000002', '权限码 1000002', 3, TRUE, 0);

-- ============================================
-- 8. 初始化字典数据
-- ============================================

-- 时区字典（dict_type = 'timezone'）
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `del_flag`) 
VALUES (1, 'timezone', 'America/New_York (GMT-5)', 'America/New_York', 1, NULL, NULL, 'N', '1', '美国纽约时区', NOW(), NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `del_flag`) 
VALUES (2, 'timezone', 'Europe/London (GMT0)', 'Europe/London', 2, NULL, NULL, 'N', '1', '欧洲伦敦时区', NOW(), NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `del_flag`) 
VALUES (3, 'timezone', 'Asia/Shanghai (GMT+8)', 'Asia/Shanghai', 3, NULL, NULL, 'Y', '1', '亚洲上海时区', NOW(), NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `del_flag`) 
VALUES (4, 'timezone', 'Asia/Tokyo (GMT+9)', 'Asia/Tokyo', 4, NULL, NULL, 'N', '1', '亚洲东京时区', NOW(), NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`, `del_flag`) 
VALUES (5, 'timezone', 'Asia/Seoul (GMT+9)', 'Asia/Seoul', 5, NULL, NULL, 'N', '1', '亚洲首尔时区', NOW(), NOW(), '0');
