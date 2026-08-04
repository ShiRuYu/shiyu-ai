CREATE TABLE IF NOT EXISTS `knowledge_document_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL,
    `space_id` BIGINT NOT NULL,
    `source_document_id` BIGINT NOT NULL,
    `target_document_id` BIGINT NOT NULL,
    `relation_type` VARCHAR(32) NOT NULL,
    `status` TINYINT DEFAULT 1,
    `del_flag` TINYINT DEFAULT 0,
    `create_by` VARCHAR(64) DEFAULT NULL,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_knowledge_document_relation`
    ON `knowledge_document_relation` (`tenant_id`, `space_id`, `source_document_id`, `target_document_id`, `relation_type`);
CREATE INDEX IF NOT EXISTS `idx_knowledge_document_relation_source`
    ON `knowledge_document_relation` (`tenant_id`, `space_id`, `source_document_id`);
CREATE INDEX IF NOT EXISTS `idx_knowledge_document_relation_target`
    ON `knowledge_document_relation` (`tenant_id`, `space_id`, `target_document_id`);
