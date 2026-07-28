-- ============================================
-- Schema: schema_common
-- ============================================


CREATE TABLE IF NOT EXISTS `dict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
    `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典键值',
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
    `dict_sort` INT DEFAULT 0 COMMENT '字典排序',
    `css_class` VARCHAR(100) COMMENT '样式属性',
    `list_class` VARCHAR(100) COMMENT '表格回显样式',
    `is_default` CHAR(1) DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_dict_type` ON `dict` (`dict_type`);

CREATE INDEX IF NOT EXISTS `idx_dict_sort` ON `dict` (`dict_sort`);

COMMENT ON TABLE `dict` IS '字典表';

