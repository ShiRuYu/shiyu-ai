-- ============================================
-- Schema: schema_auth
-- ============================================


CREATE TABLE IF NOT EXISTS `tenant` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '租户ID',
    `parent_id`       BIGINT       DEFAULT NULL COMMENT '父租户ID（null=根租户）',
    `code`            VARCHAR(64)  NOT NULL COMMENT '租户编码',
    `name`            VARCHAR(128) COMMENT '租户名称',
    `contact_name`    VARCHAR(64)  COMMENT '联系人',
    `contact_phone`   VARCHAR(20)  COMMENT '联系电话',
    `address`         VARCHAR(255) COMMENT '地址',
    `domain`          VARCHAR(255) COMMENT '域名',
    `intro`           VARCHAR(500) COMMENT '简介',
    `order`           INT          DEFAULT 0 COMMENT '排序',
    `leader`          VARCHAR(64)  COMMENT '负责人',
    `phone`           VARCHAR(20)  COMMENT '联系电话（备用）',
    `email`           VARCHAR(128) COMMENT '邮箱',
    `remark`          VARCHAR(500) COMMENT '备注',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`       VARCHAR(64)  COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_tenant_code` ON `tenant` (`code`);

COMMENT ON TABLE `tenant` IS '租户表';


CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) COMMENT '密码',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `scoped_tenant_id` BIGINT COMMENT '作用域租户ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
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
    `scoped_tenant_id` BIGINT COMMENT '作用域租户ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
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
    `type` VARCHAR(20) NOT NULL COMMENT '菜单类型（CATALOG/MENU）',
    `parent_id` BIGINT COMMENT '父菜单 ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `scoped_tenant_id` BIGINT COMMENT '作用域租户ID',
    `path` VARCHAR(255) COMMENT '路径',
    `redirect` VARCHAR(255) COMMENT '重定向地址',
    `icon` VARCHAR(64) COMMENT '图标',
    `component` VARCHAR(255) COMMENT '组件',
    `layout` VARCHAR(64) COMMENT '布局',
    `keep_alive` BOOLEAN COMMENT '是否缓存',
    `method` VARCHAR(20) COMMENT '请求方法',
    `description` VARCHAR(255) COMMENT '描述',
    `show` BOOLEAN DEFAULT TRUE COMMENT '是否显示',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
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


CREATE TABLE IF NOT EXISTS `user_scope_role` (
    `user_id`      BIGINT NOT NULL COMMENT '用户 ID',
    `scoped_tenant_id` BIGINT NOT NULL COMMENT '工作空间 ID',
    `role_id`      BIGINT NOT NULL COMMENT '角色 ID',
    `tenant_id`    BIGINT NOT NULL COMMENT '租户ID',
    `status`       TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`     TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`    VARCHAR(64)   COMMENT '创建者',
    `create_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    VARCHAR(64)   COMMENT '更新者',
    `update_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`, `scoped_tenant_id`, `role_id`)
);

CREATE INDEX IF NOT EXISTS `idx_usr_scope` ON `user_scope_role` (`scoped_tenant_id`);

CREATE INDEX IF NOT EXISTS `idx_usr_role` ON `user_scope_role` (`role_id`);

COMMENT ON TABLE `user_scope_role` IS '用户作用域角色关联表';


CREATE TABLE IF NOT EXISTS `role_scope_menu` (
    `role_id`      BIGINT NOT NULL COMMENT '角色 ID',
    `scoped_tenant_id` BIGINT NOT NULL COMMENT '工作空间 ID',
    `menu_id`      BIGINT NOT NULL COMMENT '菜单 ID',
    `tenant_id`    BIGINT NOT NULL COMMENT '租户ID',
    `status`       TINYINT      DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`     TINYINT      DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by`    VARCHAR(64)   COMMENT '创建者',
    `create_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    VARCHAR(64)   COMMENT '更新者',
    `update_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`role_id`, `scoped_tenant_id`, `menu_id`)
);

CREATE INDEX IF NOT EXISTS `idx_rsm_menu_id` ON `role_scope_menu` (`menu_id`);

COMMENT ON TABLE `role_scope_menu` IS '角色作用域菜单关联表';


CREATE TABLE IF NOT EXISTS `auth_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限码 ID',
    `code` VARCHAR(64) NOT NULL COMMENT '权限编码',
    `name` VARCHAR(128) COMMENT '权限名称',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_code_code` (`code`)
);

CREATE INDEX IF NOT EXISTS `idx_auth_code` ON `auth_code` (`code`);

COMMENT ON TABLE `auth_code` IS '权限定义表';


CREATE TABLE IF NOT EXISTS `role_scope_auth_code` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `auth_code_id` BIGINT NOT NULL COMMENT '权限定义ID',
    `scoped_tenant_id` BIGINT NOT NULL COMMENT '作用域租户ID',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0：正常 1：已删除）',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`role_id`, `auth_code_id`, `scoped_tenant_id`)
);

CREATE INDEX IF NOT EXISTS `idx_rsac_auth_code` ON `role_scope_auth_code` (`auth_code_id`);
CREATE INDEX IF NOT EXISTS `idx_rsac_scope` ON `role_scope_auth_code` (`scoped_tenant_id`, `tenant_id`);

COMMENT ON TABLE `role_scope_auth_code` IS '角色作用域权限授权表';

