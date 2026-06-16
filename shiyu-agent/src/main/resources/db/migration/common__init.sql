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
    `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
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
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) 
VALUES (1, 'timezone', 'America/New_York (GMT-5)', 'America/New_York', 1, 1, NULL, NULL, 'N', '1', '美国纽约时区', '0', NOW(), '0', NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) 
VALUES (2, 'timezone', 'Europe/London (GMT0)', 'Europe/London', 1, 2, NULL, NULL, 'N', '1', '欧洲伦敦时区', '0', NOW(), '0', NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) 
VALUES (3, 'timezone', 'Asia/Shanghai (GMT+8)', 'Asia/Shanghai', 1, 3, NULL, NULL, 'Y', '1', '亚洲上海时区', '0', NOW(), '0', NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) 
VALUES (4, 'timezone', 'Asia/Tokyo (GMT+9)', 'Asia/Tokyo', 1, 4, NULL, NULL, 'N', '1', '亚洲东京时区', '0', NOW(), '0', NOW(), '0');

INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) 
VALUES (5, 'timezone', 'Asia/Seoul (GMT+9)', 'Asia/Seoul', 1, 5, NULL, NULL, 'N', '1', '亚洲首尔时区', '0', NOW(), '0', NOW(), '0');

-- 意图编码字典（dict_type = 'INTENT_CODE'）
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (6, 'INTENT_CODE', '闲聊', 'CHITCHAT', 1, 1, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (7, 'INTENT_CODE', '问答', 'QUESTION', 1, 2, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (8, 'INTENT_CODE', '计算器', 'CALCULATOR', 1, 3, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (9, 'INTENT_CODE', '查询', 'QUERY', 1, 4, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (10, 'INTENT_CODE', '建议', 'SUGGESTION', 1, 5, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (11, 'INTENT_CODE', '投诉', 'COMPLAINT', 1, 6, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (12, 'INTENT_CODE', '技术支持', 'TECHNICAL_SUPPORT', 1, 7, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (13, 'INTENT_CODE', '产品咨询', 'PRODUCT_INQUIRY', 1, 8, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (14, 'INTENT_CODE', '订单处理', 'ORDER_PROCESSING', 1, 9, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (15, 'INTENT_CODE', '预约', 'APPOINTMENT', 1, 10, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (16, 'INTENT_CODE', '导航', 'NAVIGATION', 1, 11, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (17, 'INTENT_CODE', '娱乐', 'ENTERTAINMENT', 1, 12, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (18, 'INTENT_CODE', '教育', 'EDUCATION', 1, 13, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (19, 'INTENT_CODE', '健康', 'HEALTH', 1, 14, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (20, 'INTENT_CODE', '金融', 'FINANCE', 1, 15, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (21, 'INTENT_CODE', '购物', 'SHOPPING', 1, 16, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (22, 'INTENT_CODE', '旅行', 'TRAVEL', 1, 17, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (23, 'INTENT_CODE', '天气查询', 'WEATHER', 1, 18, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (24, 'INTENT_CODE', '新闻', 'NEWS', 1, 19, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (25, 'INTENT_CODE', '翻译', 'TRANSLATION', 1, 20, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (26, 'INTENT_CODE', '代码帮助', 'CODE_HELP', 1, 21, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (27, 'INTENT_CODE', '写作辅助', 'WRITING_ASSISTANCE', 1, 22, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (28, 'INTENT_CODE', '数据分析', 'DATA_ANALYSIS', 1, 23, '1', '意图编码');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (29, 'INTENT_CODE', '未知', 'UNKNOWN', 1, 24, '1', '意图编码');

-- 意图分类字典（dict_type = 'INTENT_CATEGORY'）
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (30, 'INTENT_CATEGORY', '会话', 'CONVERSATION', 1, 1, '1', '意图分类');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (31, 'INTENT_CATEGORY', '知识问答', 'KNOWLEDGE', 1, 2, '1', '意图分类');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (32, 'INTENT_CATEGORY', '任务', 'TASK', 1, 3, '1', '意图分类');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (33, 'INTENT_CATEGORY', '搜索', 'SEARCH', 1, 4, '1', '意图分类');
INSERT INTO `dict` (`id`, `dict_type`, `dict_label`, `dict_value`, `tenant_id`, `dict_sort`, `status`, `remark`)
VALUES (34, 'INTENT_CATEGORY', '技术支持', 'TECHNICAL', 1, 5, '1', '意图分类');

-- ==================== 重置自增序列 ====================
ALTER TABLE `dict` ALTER COLUMN `id` RESTART WITH 100;
