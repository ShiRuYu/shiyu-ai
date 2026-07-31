-- Embedded enterprise knowledge engine: spaces, governance, persistent jobs and evaluation.

CREATE TABLE IF NOT EXISTS `knowledge_space` (
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT,
    `code`                  VARCHAR(64)  NOT NULL,
    `name`                  VARCHAR(200) NOT NULL,
    `description`           TEXT         DEFAULT NULL,
    `access_mode`           VARCHAR(20)  DEFAULT 'PRIVATE',
    `review_mode`           VARCHAR(20)  DEFAULT 'OPTIONAL',
    `embedding_profile`     VARCHAR(100) DEFAULT 'default',
    `rerank_profile`        VARCHAR(100) DEFAULT 'default',
    `chunk_strategy`        VARCHAR(32)  DEFAULT 'HEADING',
    `chunk_size`            INT          DEFAULT 800,
    `chunk_overlap`         INT          DEFAULT 100,
    `active_index_version`  BIGINT       DEFAULT 0,
    `tenant_id`             BIGINT       NOT NULL,
    `status`                TINYINT      DEFAULT 1,
    `del_flag`              TINYINT      DEFAULT 0,
    `create_by`             VARCHAR(64)  DEFAULT NULL,
    `create_time`           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`             VARCHAR(64)  DEFAULT NULL,
    `update_time`           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_knowledge_space_tenant_code`
    ON `knowledge_space` (`tenant_id`, `code`);

CREATE TABLE IF NOT EXISTS `knowledge_space_member` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `space_id`        BIGINT       NOT NULL,
    `principal_type`  VARCHAR(16)  NOT NULL,
    `principal_id`    BIGINT       NOT NULL,
    `space_role`      VARCHAR(16)  NOT NULL,
    `tenant_id`       BIGINT       NOT NULL,
    `status`          TINYINT      DEFAULT 1,
    `del_flag`        TINYINT      DEFAULT 0,
    `create_by`       VARCHAR(64)  DEFAULT NULL,
    `create_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`       VARCHAR(64)  DEFAULT NULL,
    `update_time`     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_knowledge_space_member`
    ON `knowledge_space_member` (`tenant_id`, `space_id`, `principal_type`, `principal_id`);

CREATE TABLE IF NOT EXISTS `knowledge_document_version` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT,
    `document_id`       BIGINT        NOT NULL,
    `space_id`          BIGINT        NOT NULL,
    `version_no`        INT           NOT NULL,
    `title`             VARCHAR(255)  NOT NULL,
    `content`           CLOB          DEFAULT NULL,
    `storage_provider`  VARCHAR(32)   DEFAULT 'local',
    `object_key`        VARCHAR(1024) DEFAULT NULL,
    `mime_type`         VARCHAR(128)  DEFAULT NULL,
    `file_size`         BIGINT        DEFAULT 0,
    `checksum`          VARCHAR(64)   DEFAULT NULL,
    `lifecycle_status`  VARCHAR(20)   DEFAULT 'DRAFT',
    `parse_status`      VARCHAR(20)   DEFAULT 'PENDING',
    `model_profile`     VARCHAR(100)  DEFAULT 'default',
    `published_at`      TIMESTAMP     DEFAULT NULL,
    `tenant_id`         BIGINT        NOT NULL,
    `status`            TINYINT       DEFAULT 1,
    `del_flag`          TINYINT       DEFAULT 0,
    `create_by`         VARCHAR(64)   DEFAULT NULL,
    `create_time`       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    `update_by`         VARCHAR(64)   DEFAULT NULL,
    `update_time`       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_knowledge_document_version`
    ON `knowledge_document_version` (`tenant_id`, `document_id`, `version_no`);
CREATE INDEX IF NOT EXISTS `idx_knowledge_document_version_space`
    ON `knowledge_document_version` (`tenant_id`, `space_id`, `lifecycle_status`);

CREATE TABLE IF NOT EXISTS `knowledge_review_record` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `document_id`    BIGINT       NOT NULL,
    `version_id`     BIGINT       NOT NULL,
    `action`         VARCHAR(20)  NOT NULL,
    `comment_text`   VARCHAR(1000) DEFAULT NULL,
    `tenant_id`      BIGINT       NOT NULL,
    `status`         TINYINT      DEFAULT 1,
    `del_flag`       TINYINT      DEFAULT 0,
    `create_by`      VARCHAR(64)  DEFAULT NULL,
    `create_time`    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`      VARCHAR(64)  DEFAULT NULL,
    `update_time`    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `knowledge_ingestion_job` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `job_key`         VARCHAR(128)  NOT NULL,
    `job_type`        VARCHAR(32)   NOT NULL,
    `space_id`        BIGINT        NOT NULL,
    `document_id`     BIGINT        DEFAULT NULL,
    `version_id`      BIGINT        DEFAULT NULL,
    `job_status`      VARCHAR(20)   DEFAULT 'PENDING',
    `stage`           VARCHAR(32)   DEFAULT 'QUEUED',
    `progress`        INT           DEFAULT 0,
    `attempts`        INT           DEFAULT 0,
    `max_attempts`    INT           DEFAULT 3,
    `error_message`   VARCHAR(2000) DEFAULT NULL,
    `checkpoint_data` CLOB          DEFAULT NULL,
    `heartbeat_time`  TIMESTAMP     DEFAULT NULL,
    `started_time`    TIMESTAMP     DEFAULT NULL,
    `finished_time`   TIMESTAMP     DEFAULT NULL,
    `lock_version`    BIGINT        DEFAULT 0,
    `tenant_id`       BIGINT        NOT NULL,
    `status`          TINYINT       DEFAULT 1,
    `del_flag`        TINYINT       DEFAULT 0,
    `create_by`       VARCHAR(64)   DEFAULT NULL,
    `create_time`     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    `update_by`       VARCHAR(64)   DEFAULT NULL,
    `update_time`     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_knowledge_ingestion_job_key`
    ON `knowledge_ingestion_job` (`tenant_id`, `job_key`);
CREATE INDEX IF NOT EXISTS `idx_knowledge_ingestion_job_poll`
    ON `knowledge_ingestion_job` (`tenant_id`, `job_status`, `create_time`);

CREATE TABLE IF NOT EXISTS `knowledge_audit_log` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT,
    `space_id`       BIGINT        DEFAULT NULL,
    `resource_type`  VARCHAR(32)   NOT NULL,
    `resource_id`    BIGINT        DEFAULT NULL,
    `action`         VARCHAR(32)   NOT NULL,
    `detail_json`    CLOB          DEFAULT NULL,
    `tenant_id`      BIGINT        NOT NULL,
    `status`         TINYINT       DEFAULT 1,
    `del_flag`       TINYINT       DEFAULT 0,
    `create_by`      VARCHAR(64)   DEFAULT NULL,
    `create_time`    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    `update_by`      VARCHAR(64)   DEFAULT NULL,
    `update_time`    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
CREATE INDEX IF NOT EXISTS `idx_knowledge_audit_resource`
    ON `knowledge_audit_log` (`tenant_id`, `resource_type`, `resource_id`);

CREATE TABLE IF NOT EXISTS `knowledge_evaluation_case` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `space_id`          BIGINT       NOT NULL,
    `question`          CLOB         NOT NULL,
    `expected_doc_ids`  VARCHAR(2000) DEFAULT NULL,
    `expected_answer`   CLOB         DEFAULT NULL,
    `tenant_id`         BIGINT       NOT NULL,
    `status`            TINYINT      DEFAULT 1,
    `del_flag`          TINYINT      DEFAULT 0,
    `create_by`         VARCHAR(64)  DEFAULT NULL,
    `create_time`       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`         VARCHAR(64)  DEFAULT NULL,
    `update_time`       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

ALTER TABLE `knowledge_base` ADD COLUMN IF NOT EXISTS `space_id` BIGINT DEFAULT NULL;
ALTER TABLE `knowledge_relation` ADD COLUMN IF NOT EXISTS `space_id` BIGINT DEFAULT NULL;
ALTER TABLE `knowledge_document` ADD COLUMN IF NOT EXISTS `space_id` BIGINT DEFAULT NULL;
ALTER TABLE `knowledge_document` ADD COLUMN IF NOT EXISTS `current_version_id` BIGINT DEFAULT NULL;
ALTER TABLE `knowledge_document` ADD COLUMN IF NOT EXISTS `lifecycle_status` VARCHAR(20) DEFAULT 'PUBLISHED';
ALTER TABLE `knowledge_document` ADD COLUMN IF NOT EXISTS `parse_status` VARCHAR(20) DEFAULT 'READY';
ALTER TABLE `knowledge_document` ADD COLUMN IF NOT EXISTS `storage_provider` VARCHAR(32) DEFAULT 'local';
ALTER TABLE `knowledge_document` ADD COLUMN IF NOT EXISTS `object_key` VARCHAR(1024) DEFAULT NULL;
ALTER TABLE `knowledge_document` ADD COLUMN IF NOT EXISTS `mime_type` VARCHAR(128) DEFAULT NULL;
ALTER TABLE `knowledge_document` ADD COLUMN IF NOT EXISTS `file_size` BIGINT DEFAULT 0;
ALTER TABLE `knowledge_document` ADD COLUMN IF NOT EXISTS `checksum` VARCHAR(64) DEFAULT NULL;
ALTER TABLE `knowledge_doc_relation` ADD COLUMN IF NOT EXISTS `tenant_id` BIGINT DEFAULT NULL;
ALTER TABLE `knowledge_doc_relation` ADD COLUMN IF NOT EXISTS `space_id` BIGINT DEFAULT NULL;
ALTER TABLE `vector_knowledge_chunk` ADD COLUMN IF NOT EXISTS `space_id` BIGINT DEFAULT NULL;
ALTER TABLE `vector_knowledge_chunk` ADD COLUMN IF NOT EXISTS `version_id` BIGINT DEFAULT NULL;
ALTER TABLE `vector_knowledge_chunk` ADD COLUMN IF NOT EXISTS `embedding_binary` BLOB DEFAULT NULL;
ALTER TABLE `vector_knowledge_chunk` ADD COLUMN IF NOT EXISTS `embedding_model` VARCHAR(100) DEFAULT NULL;
ALTER TABLE `vector_knowledge_chunk` ADD COLUMN IF NOT EXISTS `embedding_dimension` INT DEFAULT NULL;
ALTER TABLE `vector_knowledge_chunk` ADD COLUMN IF NOT EXISTS `page_number` INT DEFAULT NULL;
ALTER TABLE `vector_knowledge_chunk` ADD COLUMN IF NOT EXISTS `section_path` VARCHAR(1000) DEFAULT NULL;
ALTER TABLE `vector_knowledge_chunk` ADD COLUMN IF NOT EXISTS `start_offset` INT DEFAULT NULL;
ALTER TABLE `vector_knowledge_chunk` ADD COLUMN IF NOT EXISTS `end_offset` INT DEFAULT NULL;
ALTER TABLE `vector_knowledge_chunk` ADD COLUMN IF NOT EXISTS `token_count` INT DEFAULT NULL;

DROP INDEX IF EXISTS `uk_knowledge_tenant_code`;
CREATE UNIQUE INDEX IF NOT EXISTS `uk_knowledge_base_tenant_space_code`
    ON `knowledge_base` (`tenant_id`, `space_id`, `code`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_knowledge_relation_tenant_space_edge`
    ON `knowledge_relation` (`tenant_id`, `space_id`, `source_id`, `target_id`, `relation_type`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_knowledge_doc_relation_tenant`
    ON `knowledge_doc_relation` (`tenant_id`, `doc_id`, `knowledge_id`);
CREATE INDEX IF NOT EXISTS `idx_knowledge_base_space`
    ON `knowledge_base` (`tenant_id`, `space_id`);
CREATE INDEX IF NOT EXISTS `idx_knowledge_relation_space`
    ON `knowledge_relation` (`tenant_id`, `space_id`);
CREATE INDEX IF NOT EXISTS `idx_knowledge_document_space`
    ON `knowledge_document` (`tenant_id`, `space_id`, `lifecycle_status`);
CREATE INDEX IF NOT EXISTS `idx_knowledge_chunk_space`
    ON `vector_knowledge_chunk` (`tenant_id`, `space_id`, `document_id`);

UPDATE `knowledge_doc_relation` r
SET `tenant_id` = (
    SELECT d.`tenant_id` FROM `knowledge_document` d WHERE d.`id` = r.`doc_id`
)
WHERE r.`tenant_id` IS NULL;
