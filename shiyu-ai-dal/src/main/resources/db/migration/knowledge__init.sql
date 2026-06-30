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
    `description`     TEXT         DEFAULT NULL COMMENT '描述',
    `difficulty`      TINYINT      DEFAULT 2 COMMENT '1~4',
    `category`        VARCHAR(64)  DEFAULT NULL COMMENT '分类（如: math/phys/chem）',
    `tags`            VARCHAR(500) DEFAULT NULL COMMENT '标签（JSON数组，如 ["代数","函数"]）',
    `status`          TINYINT      DEFAULT 1 COMMENT '0停 1启',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
CREATE INDEX `idx_knowledge_category` ON `knowledge` (`category`);
COMMENT ON TABLE `knowledge` IS '知识点表（通用，不绑定教育域）';

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

-- ============================================-- 3. 文档知识表 (教材/讲义/参考资料)
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
-- 4. 文档-知识点关联表 (多对多)
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