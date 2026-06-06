-- ============================================
-- 通用数据初始化脚本
-- 包含字典等通用基础数据
-- 使用 auth 数据源 (authdb)
-- ============================================

-- ============================================
-- 1. 创建表结构
-- ============================================

-- 字典表
DROP TABLE IF EXISTS `dict`;
CREATE TABLE `dict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
    `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典键值',
    `dict_sort` INT DEFAULT 0 COMMENT '字典排序',
    `css_class` VARCHAR(100) COMMENT '样式属性',
    `list_class` VARCHAR(100) COMMENT '表格回显样式',
    `is_default` CHAR(1) DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
    `status` CHAR(1) DEFAULT '1' COMMENT '状态（1正常 0停用）',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_by` VARCHAR(64) COMMENT '创建者',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) COMMENT '更新者',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
    PRIMARY KEY (`id`)
);
CREATE INDEX `idx_dict_type` ON `dict` (`dict_type`);
CREATE INDEX `idx_dict_sort` ON `dict` (`dict_sort`);
COMMENT ON TABLE `dict` IS '字典表';

-- ============================================
-- 2. 初始化字典数据
-- ============================================

-- 时区字典（dict_type = 'timezone'）
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) 
VALUES (1, 'timezone', 'America/New_York (GMT-5)', 'America/New_York', 1, NULL, NULL, 'N', '1', '美国纽约时区', 'system', NOW(), 'system', NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) 
VALUES (2, 'timezone', 'Europe/London (GMT0)', 'Europe/London', 2, NULL, NULL, 'N', '1', '欧洲伦敦时区', 'system', NOW(), 'system', NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) 
VALUES (3, 'timezone', 'Asia/Shanghai (GMT+8)', 'Asia/Shanghai', 3, NULL, NULL, 'Y', '1', '亚洲上海时区', 'system', NOW(), 'system', NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) 
VALUES (4, 'timezone', 'Asia/Tokyo (GMT+9)', 'Asia/Tokyo', 4, NULL, NULL, 'N', '1', '亚洲东京时区', 'system', NOW(), 'system', NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) 
VALUES (5, 'timezone', 'Asia/Seoul (GMT+9)', 'Asia/Seoul', 5, NULL, NULL, 'N', '1', '亚洲首尔时区', 'system', NOW(), 'system', NOW(), '0');

-- ==================== 重置自增序列 ====================
-- H2数据库在手动插入ID后需要重置序列，避免主键冲突
ALTER TABLE `dict` ALTER COLUMN `id` RESTART WITH 100;
