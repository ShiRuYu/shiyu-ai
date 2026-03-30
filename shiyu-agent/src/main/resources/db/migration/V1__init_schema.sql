-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL COMMENT '用户 ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
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
