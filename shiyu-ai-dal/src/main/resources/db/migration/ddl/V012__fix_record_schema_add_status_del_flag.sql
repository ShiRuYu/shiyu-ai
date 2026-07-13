-- ============================================
-- V012: 补全 record 模块表缺失的 status/del_flag 字段
-- timeline_event / record / media / tag 缺少公共字段
-- profile 表已有，无需操作
-- ============================================

-- timeline_event
ALTER TABLE `timeline_event` ADD COLUMN IF NOT EXISTS `status` TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）';
ALTER TABLE `timeline_event` ADD COLUMN IF NOT EXISTS `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0正常 1删除）';

-- record
ALTER TABLE `record` ADD COLUMN IF NOT EXISTS `status` TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）';
ALTER TABLE `record` ADD COLUMN IF NOT EXISTS `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0正常 1删除）';

-- media
ALTER TABLE `media` ADD COLUMN IF NOT EXISTS `status` TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）';
ALTER TABLE `media` ADD COLUMN IF NOT EXISTS `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0正常 1删除）';

-- tag
ALTER TABLE `tag` ADD COLUMN IF NOT EXISTS `status` TINYINT DEFAULT 1 COMMENT '状态（1正常 0停用）';
ALTER TABLE `tag` ADD COLUMN IF NOT EXISTS `del_flag` TINYINT DEFAULT 0 COMMENT '删除标志（0正常 1删除）';
