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
