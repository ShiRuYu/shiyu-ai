-- ============================================
-- 知识图谱初始化脚本
-- 使用 agent 数据源
-- ============================================

-- ============================================
-- 1. 知识点表
-- ============================================
DROP TABLE IF EXISTS `knowledge`;
CREATE TABLE `knowledge` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
    `code`            VARCHAR(50)  NOT NULL UNIQUE COMMENT '知识点编码',
    `name`            VARCHAR(200) NOT NULL COMMENT '名称',
    `subject_code`    VARCHAR(20)  NOT NULL COMMENT '学科编码',
    `grade`           INT          NOT NULL COMMENT '年级 0幼儿园 1~12',
    `grade_level`     VARCHAR(10)  DEFAULT NULL COMMENT '学段 K0/K1/K2/K3',
    `description`     TEXT         DEFAULT NULL COMMENT '描述',
    `difficulty`      TINYINT      DEFAULT 2 COMMENT '1~4',
    `estimated_time`  INT          DEFAULT 45 COMMENT '预估学习时长(分钟)',
    `suitable_age`    VARCHAR(50)  DEFAULT NULL COMMENT '适合年龄范围',
    `status`          TINYINT      DEFAULT 1 COMMENT '0停 1启',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE INDEX `idx_knowledge_subject` ON `knowledge` (`subject_code`);
CREATE INDEX `idx_knowledge_grade` ON `knowledge` (`grade`);
COMMENT ON TABLE `knowledge` IS '知识点表';

-- ============================================
-- 2. 知识点关系表
-- ============================================
DROP TABLE IF EXISTS `knowledge_relation`;
CREATE TABLE `knowledge_relation` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT,
    `source_id`     BIGINT      NOT NULL COMMENT '源知识点ID',
    `target_id`     BIGINT      NOT NULL COMMENT '目标知识点ID',
    `relation_type` VARCHAR(20) NOT NULL COMMENT 'PRE/NEXT/INCLUDE/RELATED/SIMILAR/BELONG',
    `weight`        DOUBLE      DEFAULT 1.0,
    `create_time`   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX `uk_kr_source_target_type` ON `knowledge_relation` (`source_id`, `target_id`, `relation_type`);
CREATE INDEX `idx_kr_source` ON `knowledge_relation` (`source_id`);
CREATE INDEX `idx_kr_target` ON `knowledge_relation` (`target_id`);
COMMENT ON TABLE `knowledge_relation` IS '知识点关系表';

-- ============================================
-- 3. 种子数据: 数学知识点 (七年级)
-- ============================================
INSERT INTO `knowledge` (`id`, `code`, `name`, `subject_code`, `grade`, `grade_level`, `description`, `difficulty`, `estimated_time`, `suitable_age`, `status`) VALUES
(1,  'math_natural',    '自然数',   'MATH', 7, 'K2', '用来表示物体个数的数: 0,1,2,3,...', 1, 30, '12-13', 1),
(2,  'math_integer',    '整数',     'MATH', 7, 'K2', '正整数、零和负整数的统称', 1, 30, '12-13', 1),
(3,  'math_numberline', '数轴',     'MATH', 7, 'K2', '规定了原点、正方向和单位长度的直线', 2, 45, '12-14', 1),
(4,  'math_opposite',   '相反数',   'MATH', 7, 'K2', '只有符号不同的两个数互为相反数', 2, 45, '12-14', 1),
(5,  'math_absval',     '绝对值',   'MATH', 7, 'K2', '一个数在数轴上对应的点到原点的距离', 2, 45, '12-14', 1),
(6,  'math_rational',   '有理数',   'MATH', 7, 'K2', '整数和分数的统称', 2, 45, '12-14', 1),
(7,  'math_linear_fn',  '一次函数', 'MATH', 8, 'K2', 'y=kx+b (k≠0) 形式的函数', 3, 60, '13-15', 1),
(8,  'math_quad_fn',   '二次函数', 'MATH', 9, 'K2', 'y=ax²+bx+c (a≠0) 形式的函数', 3, 60, '14-16', 1),
(9,  'math_function',   '函数',     'MATH', 8, 'K2', '两个变量之间的对应关系', 3, 60, '13-15', 1),
(10, 'math_derivative', '导数',     'MATH', 12,'K3', '函数在某一点的变化率', 4, 90, '17-18', 1);

-- ============================================
-- 4. 种子数据: 知识点关系 (PRE = 前置知识)
-- ============================================
INSERT INTO `knowledge_relation` (`source_id`, `target_id`, `relation_type`, `weight`) VALUES
(2,  1,  'PRE',   1.0),
(3,  2,  'PRE',   1.0),
(4,  3,  'PRE',   1.0),
(5,  4,  'PRE',   1.0),
(6,  5,  'PRE',   1.0),
(9,  6,  'PRE',   1.0),
(7,  9,  'PRE',   1.0),
(8,  7,  'PRE',   1.0),
(10, 8,  'PRE',   1.0),
(10, 9,  'PRE',   0.8),
(5,  3,  'RELATED', 0.6),
(7,  8,  'SIMILAR', 0.7);

ALTER TABLE `knowledge` ALTER COLUMN `id` RESTART WITH 100;
ALTER TABLE `knowledge_relation` ALTER COLUMN `id` RESTART WITH 100;

-- ============================================
-- 5. 文档知识表 (教材/讲义/参考资料)
-- ============================================
DROP TABLE IF EXISTS `knowledge_document`;
CREATE TABLE `knowledge_document` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    `title`           VARCHAR(255) NOT NULL COMMENT '文档标题',
    `content`         TEXT         NOT NULL COMMENT '文档内容',
    `doc_type`        VARCHAR(20)  DEFAULT 'ARTICLE' COMMENT '文档类型 ARTICLE/TEXTBOOK/LECTURE/REFERENCE',
    `source`          VARCHAR(255) DEFAULT NULL COMMENT '来源',
    `author`          VARCHAR(128) DEFAULT NULL COMMENT '作者',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE INDEX `idx_kd_type` ON `knowledge_document` (`doc_type`);
COMMENT ON TABLE `knowledge_document` IS '文档知识表';

-- ============================================
-- 6. 文档-知识点关联表 (多对多)
-- ============================================
DROP TABLE IF EXISTS `knowledge_doc_relation`;
CREATE TABLE `knowledge_doc_relation` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT,
    `doc_id`        BIGINT      NOT NULL COMMENT '文档ID',
    `knowledge_id`  BIGINT      NOT NULL COMMENT '知识点ID',
    `relation_type` VARCHAR(20) DEFAULT 'RELATED' COMMENT '关联类型',
    `create_time`   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_kdr_doc_knowledge` (`doc_id`, `knowledge_id`)
);
CREATE INDEX `idx_kdr_knowledge` ON `knowledge_doc_relation` (`knowledge_id`);
COMMENT ON TABLE `knowledge_doc_relation` IS '文档-知识点关联表';

-- ============================================
-- 7. 能力值表 (Bloom Taxonomy 持久化)
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
COMMENT ON TABLE `ability' IS '能力值表';

-- ============================================
-- 8. 教材版本表 (知识点分层)
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
-- 9. 章节表 (教材的章节结构)
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
-- 10. 知识点-教材章节关联
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
