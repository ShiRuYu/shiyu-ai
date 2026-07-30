-- 租户可用菜单与权限码绑定。
-- menu/auth_code 是平台资源定义，以下两张表决定租户实际可用资源。

CREATE TABLE IF NOT EXISTS `auth_tenant_menu` (
    `tenant_id`   BIGINT NOT NULL COMMENT '租户ID',
    `menu_id`     BIGINT NOT NULL COMMENT '菜单ID',
    `status`      TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`tenant_id`, `menu_id`),
    INDEX `idx_tenant_menu_menu` (`menu_id`)
);

CREATE TABLE IF NOT EXISTS `auth_tenant_auth_code` (
    `tenant_id`    BIGINT NOT NULL COMMENT '租户ID',
    `auth_code_id` BIGINT NOT NULL COMMENT '权限码ID',
    `status`       TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `create_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time`  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`tenant_id`, `auth_code_id`),
    INDEX `idx_tenant_auth_code_code` (`auth_code_id`)
);

-- 历史数据兼容：已有菜单和角色权限关系的租户自动获得对应资源。
INSERT IGNORE INTO `auth_tenant_menu` (`tenant_id`, `menu_id`)
SELECT DISTINCT `tenant_id`, `menu_id`
FROM `auth_role_scope_menu`
WHERE `tenant_id` IS NOT NULL AND `menu_id` IS NOT NULL;

INSERT IGNORE INTO `auth_tenant_auth_code` (`tenant_id`, `auth_code_id`)
SELECT DISTINCT `tenant_id`, `auth_code_id`
FROM `auth_role_scope_auth_code`
WHERE `tenant_id` IS NOT NULL AND `auth_code_id` IS NOT NULL;
