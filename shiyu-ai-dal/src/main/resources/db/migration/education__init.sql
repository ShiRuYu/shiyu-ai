-- ============================================
-- 教育业务域初始化脚本
-- 使用 agent 数据源
-- ============================================

-- ============================================
-- 1. 能力值表 (Bloom Taxonomy 持久化)
-- ============================================
DROP TABLE IF EXISTS `ability`;
CREATE TABLE `ability` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `student_id`      BIGINT       NOT NULL COMMENT '学生ID',
    `knowledge_id`    BIGINT       NOT NULL COMMENT '知识点ID',
    `remember`        DOUBLE       DEFAULT 0 COMMENT '记忆',
    `understand`      DOUBLE       DEFAULT 0 COMMENT '理解',
    `apply`           DOUBLE       DEFAULT 0 COMMENT '应用',
    `analyze`         DOUBLE       DEFAULT 0 COMMENT '分析',
    `evaluate`        DOUBLE       DEFAULT 0 COMMENT '评价',
    `create_score`    DOUBLE       DEFAULT 0 COMMENT '创造',
    `overall_mastery` DOUBLE       DEFAULT 0 COMMENT '总体掌握度',
    `last_update`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ability_student_knowledge` (`student_id`, `knowledge_id`)
);
COMMENT ON TABLE `ability` IS '能力值表';

-- ============================================
-- 2. 教材版本表
-- ============================================
DROP TABLE IF EXISTS `textbook`;
CREATE TABLE `textbook` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(128) NOT NULL COMMENT '教材名称',
    `subject_code`    VARCHAR(20)  NOT NULL COMMENT '学科编码',
    `grade`           INT          NOT NULL COMMENT '年级',
    `publisher`       VARCHAR(64)  NOT NULL COMMENT '出版社(人教版/北师大版等)',
    `isbn`            VARCHAR(32)  DEFAULT NULL COMMENT 'ISBN',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
COMMENT ON TABLE `textbook` IS '教材版本表';

-- ============================================
-- 3. 章节表
-- ============================================
DROP TABLE IF EXISTS `chapter`;
CREATE TABLE `chapter` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `textbook_id`     BIGINT       NOT NULL COMMENT '教材ID',
    `parent_id`       BIGINT       DEFAULT NULL COMMENT '父章节ID',
    `name`            VARCHAR(128) NOT NULL COMMENT '章节名称',
    `chapter_order`   INT          DEFAULT 0 COMMENT '排序',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_chapter_textbook` (`textbook_id`),
    KEY `idx_chapter_parent` (`parent_id`)
);
COMMENT ON TABLE `chapter` IS '章节表';

-- ============================================
-- 4. 知识点-教材章节关联
-- ============================================
DROP TABLE IF EXISTS `knowledge_textbook`;
CREATE TABLE `knowledge_textbook` (
    `id`              BIGINT NOT NULL AUTO_INCREMENT,
    `knowledge_id`    BIGINT NOT NULL COMMENT '知识点ID',
    `textbook_id`     BIGINT NOT NULL COMMENT '教材ID',
    `chapter_id`      BIGINT DEFAULT NULL COMMENT '章节ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_kt_knowledge_textbook` (`knowledge_id`, `textbook_id`)
);
COMMENT ON TABLE `knowledge_textbook` IS '知识点-教材章节关联表';

-- ============================================
-- 5. 种子数据: 数学知识点 (七年级) — 已迁移至 knowledge__init.sql
-- ============================================

-- ============================================
-- 6. 种子数据: 知识点关系 (PRE = 前置知识) — 已迁移至 knowledge__init.sql
-- ============================================
