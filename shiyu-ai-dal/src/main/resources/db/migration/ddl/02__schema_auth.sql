-- ============================================
-- Schema: schema_auth
-- ============================================


CREATE TABLE IF NOT EXISTS `tenant` (
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

CREATE INDEX IF NOT EXISTS `idx_tenant_code` ON `tenant` (`code`);

COMMENT ON TABLE `tenant` IS '租户表';


CREATE TABLE IF NOT EXISTS `user` (
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

CREATE INDEX IF NOT EXISTS `idx_username` ON `user` (`username`);

COMMENT ON TABLE `user` IS '用户表';


CREATE TABLE IF NOT EXISTS `role` (
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

CREATE INDEX IF NOT EXISTS `idx_code` ON `role` (`code`);

COMMENT ON TABLE `role` IS '角色表';


CREATE TABLE IF NOT EXISTS `menu` (
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

CREATE INDEX IF NOT EXISTS `idx_parent_id` ON `menu` (`parent_id`);

COMMENT ON TABLE `menu` IS '菜单表';


CREATE TABLE IF NOT EXISTS `user_workspace_role` (
    `user_id`      BIGINT NOT NULL COMMENT '用户 ID',
    `workspace_id` BIGINT NOT NULL COMMENT '工作空间 ID',
    `role_id`      BIGINT NOT NULL COMMENT '角色 ID',
    `tenant_id`    BIGINT NOT NULL COMMENT '租户ID',
    `create_by`    VARCHAR(64)   COMMENT '创建者',
    `create_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    VARCHAR(64)   COMMENT '更新者',
    `update_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`, `workspace_id`, `role_id`)
);

CREATE INDEX IF NOT EXISTS `idx_uwr_workspace` ON `user_workspace_role` (`workspace_id`);

CREATE INDEX IF NOT EXISTS `idx_uwr_role` ON `user_workspace_role` (`role_id`);

COMMENT ON TABLE `user_workspace_role` IS '用户空间角色关联表';


CREATE TABLE IF NOT EXISTS `role_workspace_menu` (
    `role_id`      BIGINT NOT NULL COMMENT '角色 ID',
    `workspace_id` BIGINT NOT NULL COMMENT '工作空间 ID',
    `menu_id`      BIGINT NOT NULL COMMENT '菜单 ID',
    `tenant_id`    BIGINT NOT NULL COMMENT '租户ID',
    PRIMARY KEY (`role_id`, `workspace_id`, `menu_id`)
);

CREATE INDEX IF NOT EXISTS `idx_rwm_menu_id` ON `role_workspace_menu` (`menu_id`);

COMMENT ON TABLE `role_workspace_menu` IS '角色工作空间菜单关联表';


CREATE TABLE IF NOT EXISTS `auth_code` (
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

CREATE INDEX IF NOT EXISTS `idx_role_code` ON `auth_code` (`role_id`, `code`);

CREATE INDEX IF NOT EXISTS `idx_auth_code` ON `auth_code` (`code`);

COMMENT ON TABLE `auth_code` IS '权限码表';


CREATE TABLE IF NOT EXISTS `workspace` (
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

CREATE INDEX IF NOT EXISTS `idx_workspace_parent_id` ON `workspace` (`parent_id`);

COMMENT ON TABLE `workspace` IS '工作空间表';


CREATE TABLE IF NOT EXISTS `user_workspace_role` (
    `user_id`      BIGINT NOT NULL COMMENT '用户 ID',
    `workspace_id` BIGINT NOT NULL COMMENT '工作空间 ID',
    `role_id`      BIGINT NOT NULL COMMENT '角色 ID',
    `tenant_id`    BIGINT NOT NULL COMMENT '租户ID',
    `create_by`    VARCHAR(64)   COMMENT '创建者',
    `create_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    VARCHAR(64)   COMMENT '更新者',
    `update_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`, `workspace_id`, `role_id`)
);

CREATE INDEX IF NOT EXISTS `idx_uwr_workspace` ON `user_workspace_role` (`workspace_id`);

CREATE INDEX IF NOT EXISTS `idx_uwr_role` ON `user_workspace_role` (`role_id`);

COMMENT ON TABLE `user_workspace_role` IS '用户空间角色关联表';

ALTER TABLE `tenant` ALTER COLUMN `id` RESTART WITH 100;

ALTER TABLE `user` ALTER COLUMN `id` RESTART WITH 100;

ALTER TABLE `role` ALTER COLUMN `id` RESTART WITH 100;

ALTER TABLE `menu` ALTER COLUMN `id` RESTART WITH 1000;

ALTER TABLE `auth_code` ALTER COLUMN `id` RESTART WITH 100;

ALTER TABLE `workspace` ALTER COLUMN `id` RESTART WITH 100;

