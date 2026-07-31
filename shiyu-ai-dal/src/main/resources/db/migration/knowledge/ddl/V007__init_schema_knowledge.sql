-- ============================================
-- Schema: schema_knowledge
-- ============================================


CREATE TABLE IF NOT EXISTS `knowledge_base` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
    `code`            VARCHAR(50)  NOT NULL COMMENT '知识点编码',
    `name`            VARCHAR(200) NOT NULL COMMENT '名称',
    `description`     TEXT         DEFAULT NULL COMMENT '描述',
    `difficulty`      TINYINT      DEFAULT 2 COMMENT '1~4',
    `category`        VARCHAR(64)  DEFAULT NULL COMMENT '分类（如: math/phys/chem）',
    `tags`            VARCHAR(500) DEFAULT NULL COMMENT '标签（JSON数组，如 ["代数","函数"]）',
    `tenant_id`       BIGINT       NOT NULL COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '0停 1启',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '0正常 1删除',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_knowledge_category` ON `knowledge_base` (`category`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_knowledge_tenant_code` ON `knowledge_base` (`tenant_id`, `code`);

COMMENT ON TABLE `knowledge_base` IS '知识点表（通用，不绑定教育域）';


CREATE TABLE IF NOT EXISTS `knowledge_relation` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT,
    `source_id`     BIGINT      NOT NULL COMMENT '源知识点ID',
    `target_id`     BIGINT      NOT NULL COMMENT '目标知识点ID',
    `relation_type` VARCHAR(20) NOT NULL COMMENT 'PRE/NEXT/INCLUDE/RELATED/SIMILAR/BELONG',
    `weight`        DOUBLE      DEFAULT 1.0,
    `tenant_id`     BIGINT      COMMENT '租户ID',
    `status`        TINYINT     DEFAULT 1 COMMENT '0停 1启',
    `del_flag`      TINYINT     DEFAULT 0 COMMENT '0正常 1删除',
    `create_by`     VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    `create_time`   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    `update_time`   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);

CREATE UNIQUE INDEX IF NOT EXISTS `uk_kr_source_target_type` ON `knowledge_relation` (`source_id`, `target_id`, `relation_type`);

CREATE INDEX IF NOT EXISTS `idx_kr_source` ON `knowledge_relation` (`source_id`);

CREATE INDEX IF NOT EXISTS `idx_kr_target` ON `knowledge_relation` (`target_id`);

COMMENT ON TABLE `knowledge_relation` IS '知识点关系表';


CREATE TABLE IF NOT EXISTS `knowledge_document` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    `title`           VARCHAR(255) NOT NULL COMMENT '文档标题',
    `content`         TEXT         NOT NULL COMMENT '文档内容',
    `doc_type`        VARCHAR(20)  DEFAULT 'ARTICLE' COMMENT '文档类型 ARTICLE/TEXTBOOK/LECTURE/REFERENCE',
    `source`          VARCHAR(255) DEFAULT NULL COMMENT '来源',
    `author`          VARCHAR(128) DEFAULT NULL COMMENT '作者',
    `tenant_id`       BIGINT       COMMENT '租户ID',
    `status`          TINYINT      DEFAULT 1 COMMENT '0停 1启',
    `del_flag`        TINYINT      DEFAULT 0 COMMENT '0正常 1删除',
    `create_by`       VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);

CREATE INDEX IF NOT EXISTS `idx_kd_type` ON `knowledge_document` (`doc_type`);

COMMENT ON TABLE `knowledge_document` IS '文档知识表';


CREATE TABLE IF NOT EXISTS `knowledge_doc_relation` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `space_id`      BIGINT       DEFAULT NULL COMMENT '知识空间ID',
    `doc_id`        BIGINT       NOT NULL COMMENT '文档ID',
    `knowledge_id`  BIGINT       NOT NULL COMMENT '知识点ID',
    `relation_type` VARCHAR(20)  DEFAULT 'RELATED' COMMENT '关联类型',
    `create_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `tenant_id`     BIGINT       COMMENT '租户ID',
    `create_by`     VARCHAR(64)  DEFAULT NULL COMMENT '创建者',
    `update_by`     VARCHAR(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `status`        TINYINT      DEFAULT 1 COMMENT '0停 1启',
    `del_flag`      TINYINT      DEFAULT 0 COMMENT '0正常 1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_kdr_doc_knowledge` (`doc_id`, `knowledge_id`)
);

CREATE INDEX IF NOT EXISTS `idx_kdr_knowledge` ON `knowledge_doc_relation` (`knowledge_id`);

COMMENT ON TABLE `knowledge_doc_relation` IS '文档-知识点关联表';




