-- ============================================
-- 将权限定义与角色授权拆分，并清理 BUTTON 菜单
-- 不使用外键，兼容已经执行过 V004 的旧数据库。
-- ============================================

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

-- 旧版本 auth_code 中的角色绑定迁移到授权表。
SELECT IF(
    EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'auth_code' AND column_name = 'role_id'
    ),
    'INSERT IGNORE INTO role_scope_auth_code (role_id, auth_code_id, scoped_tenant_id, tenant_id, status, del_flag, create_by, create_time, update_by, update_time) SELECT role_id, id, COALESCE(scoped_tenant_id, tenant_id), tenant_id, status, del_flag, create_by, create_time, update_by, update_time FROM auth_code',
    'SELECT 1'
) INTO @migrate_auth_code_sql;
PREPARE migrate_auth_code_stmt FROM @migrate_auth_code_sql;
EXECUTE migrate_auth_code_stmt;
DEALLOCATE PREPARE migrate_auth_code_stmt;

-- 删除旧的角色/租户字段；新建数据库时这些字段本来就不存在。
SELECT IF(
    EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'auth_code' AND column_name = 'role_id'
    ),
    'ALTER TABLE auth_code DROP COLUMN role_id, DROP COLUMN tenant_id, DROP COLUMN scoped_tenant_id',
    'SELECT 1'
) INTO @drop_auth_code_scope_sql;
PREPARE drop_auth_code_scope_stmt FROM @drop_auth_code_scope_sql;
EXECUTE drop_auth_code_scope_stmt;
DEALLOCATE PREPARE drop_auth_code_scope_stmt;

SELECT IF(
    NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'auth_code'
          AND index_name = 'uk_auth_code_code'
    ),
    'ALTER TABLE auth_code ADD UNIQUE KEY uk_auth_code_code (code)',
    'SELECT 1'
) INTO @add_auth_code_unique_sql;
PREPARE add_auth_code_unique_stmt FROM @add_auth_code_unique_sql;
EXECUTE add_auth_code_unique_stmt;
DEALLOCATE PREPARE add_auth_code_unique_stmt;

DELETE rsm
FROM `role_scope_menu` rsm
INNER JOIN `menu` m ON m.id = rsm.menu_id
WHERE m.type = 'BUTTON';

DELETE FROM `menu` WHERE `type` = 'BUTTON';
