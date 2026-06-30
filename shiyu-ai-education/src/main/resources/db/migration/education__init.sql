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
-- 5. 种子数据: 数学知识点 (七年级)
-- ============================================
INSERT INTO `knowledge` (`id`, `code`, `name`, `description`, `difficulty`, `category`, `tags`) VALUES
(1, 'math_natural',    '自然数',   '用来表示物体个数的数: 0,1,2,3,...', 1, 'MATH', '["自然数","初等数学"]'),
(2, 'math_integer',    '整数',     '正整数、零和负整数的统称', 1, 'MATH', '["整数"]'),
(3, 'math_numberline', '数轴',     '规定了原点、正方向和单位长度的直线', 2, 'MATH', '["数轴"]'),
(4, 'math_opposite',   '相反数',   '只有符号不同的两个数互为相反数', 2, 'MATH', '["相反数"]'),
(5, 'math_absval',     '绝对值',   '一个数在数轴上对应的点到原点的距离', 2, 'MATH', '["绝对值"]'),
(6, 'math_rational',   '有理数',   '整数和分数的统称', 2, 'MATH', '["有理数"]'),
(7, 'math_linear_fn',  '一次函数', 'y=kx+b (k≠0) 形式的函数', 3, 'MATH', '["一次函数"]'),
(8, 'math_quad_fn',   '二次函数', 'y=ax²+bx+c (a≠0) 形式的函数', 3, 'MATH', '["二次函数"]'),
(9, 'math_function',   '函数',     '两个变量之间的对应关系', 3, 'MATH', '["函数"]'),
(10, 'math_derivative', '导数',     '函数在某一点的变化率', 4, 'MATH', '["导数","高等数学"]');

-- ============================================
-- 6. 种子数据: 知识点关系 (PRE = 前置知识)
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
