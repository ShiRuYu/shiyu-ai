-- Unified difficulty semantics for every knowledge space.
CREATE TABLE IF NOT EXISTS `knowledge_difficulty_scale` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(64)  NOT NULL,
    `name`        VARCHAR(128) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `level_count` INT          NOT NULL DEFAULT 5,
    `tenant_id`   BIGINT       NOT NULL,
    `status`      TINYINT      DEFAULT 1,
    `del_flag`    TINYINT      DEFAULT 0,
    `create_by`   VARCHAR(64)  DEFAULT NULL,
    `create_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`   VARCHAR(64)  DEFAULT NULL,
    `update_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE UNIQUE INDEX IF NOT EXISTS `uk_knowledge_difficulty_scale_tenant_code`
    ON `knowledge_difficulty_scale` (`tenant_id`, `code`);

CREATE TABLE IF NOT EXISTS `knowledge_difficulty_scale_level` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `scale_id`    BIGINT       NOT NULL,
    `level`       INT          NOT NULL,
    `label`       VARCHAR(128) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `tenant_id`   BIGINT       NOT NULL,
    `status`      TINYINT      DEFAULT 1,
    `del_flag`    TINYINT      DEFAULT 0,
    `create_by`   VARCHAR(64)  DEFAULT NULL,
    `create_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`   VARCHAR(64)  DEFAULT NULL,
    `update_time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE UNIQUE INDEX IF NOT EXISTS `uk_knowledge_difficulty_scale_level`
    ON `knowledge_difficulty_scale_level` (`tenant_id`, `scale_id`, `level`);

ALTER TABLE `knowledge_space`
    ADD COLUMN IF NOT EXISTS `difficulty_scale_id` BIGINT DEFAULT 1;

ALTER TABLE `knowledge_base`
    ADD COLUMN IF NOT EXISTS `difficulty_level` INT DEFAULT NULL;

UPDATE `knowledge_base`
SET `difficulty_level` = `difficulty`
WHERE `difficulty_level` IS NULL;

UPDATE `knowledge_space`
SET `difficulty_scale_id` = 1
WHERE `difficulty_scale_id` IS NULL;

INSERT IGNORE INTO `knowledge_difficulty_scale`
    (`id`, `code`, `name`, `description`, `level_count`, `tenant_id`, `status`, `del_flag`)
VALUES
    (1, 'default-5', '通用五级难度', '适用于企业知识、教育知识和技术文档的通用难度量表', 5, 1, 1, 0);

INSERT IGNORE INTO `knowledge_difficulty_scale_level`
    (`scale_id`, `level`, `label`, `description`, `tenant_id`, `status`, `del_flag`)
VALUES
    (1, 1, '基础', '入门、事实识记或直接操作', 1, 1, 0),
    (1, 2, '简单', '理解并完成常规应用', 1, 1, 0),
    (1, 3, '中等', '需要综合多个知识点', 1, 1, 0),
    (1, 4, '困难', '需要较强分析和迁移能力', 1, 1, 0),
    (1, 5, '专家', '复杂场景、创新或深度推理', 1, 1, 0);
