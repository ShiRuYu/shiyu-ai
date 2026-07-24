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
-- 不使用 MySQL 的 SELECT ... INTO @var + PREPARE 动态 SQL：
-- H2 不支持该语法，而且新库没有这些旧字段，直接引用也会失败。
-- 先补齐为可空的临时字段；旧库中字段已存在时 IF NOT EXISTS 不做任何修改。
ALTER TABLE `auth_code` ADD COLUMN IF NOT EXISTS `role_id` BIGINT;
ALTER TABLE `auth_code` ADD COLUMN IF NOT EXISTS `tenant_id` BIGINT;
ALTER TABLE `auth_code` ADD COLUMN IF NOT EXISTS `scoped_tenant_id` BIGINT;

INSERT INTO role_scope_auth_code
    (role_id, auth_code_id, scoped_tenant_id, tenant_id, status, del_flag,
     create_by, create_time, update_by, update_time)
SELECT ac.role_id, ac.id, COALESCE(ac.scoped_tenant_id, ac.tenant_id),
       ac.tenant_id, ac.status, ac.del_flag, ac.create_by, ac.create_time,
       ac.update_by, ac.update_time
FROM auth_code ac
WHERE ac.role_id IS NOT NULL
  AND COALESCE(ac.scoped_tenant_id, ac.tenant_id) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM role_scope_auth_code rsac
      WHERE rsac.role_id = ac.role_id
        AND rsac.auth_code_id = ac.id
        AND rsac.scoped_tenant_id = COALESCE(ac.scoped_tenant_id, ac.tenant_id)
  );

-- 删除旧的角色/租户字段；新建数据库中这里是上面补出的临时字段。
ALTER TABLE `auth_code` DROP COLUMN IF EXISTS `role_id`;
ALTER TABLE `auth_code` DROP COLUMN IF EXISTS `tenant_id`;
ALTER TABLE `auth_code` DROP COLUMN IF EXISTS `scoped_tenant_id`;

DELETE FROM `role_scope_menu`
WHERE `menu_id` IN (
    SELECT `id`
    FROM `menu`
    WHERE `type` = 'BUTTON'
);

DELETE FROM `menu` WHERE `type` = 'BUTTON';
