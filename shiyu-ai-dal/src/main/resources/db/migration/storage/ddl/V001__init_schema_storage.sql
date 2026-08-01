CREATE TABLE IF NOT EXISTS `storage_object` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT,
    `tenant_id`        BIGINT       NOT NULL,
    `space_id`         BIGINT       DEFAULT NULL,
    `namespace`        VARCHAR(255) NOT NULL,
    `object_key`       VARCHAR(1024) NOT NULL,
    `storage_provider` VARCHAR(32)   NOT NULL DEFAULT 'local',
    `original_name`    VARCHAR(512)  NOT NULL,
    `content_type`     VARCHAR(128)  DEFAULT 'application/octet-stream',
    `file_size`        BIGINT       NOT NULL DEFAULT 0,
    `checksum`         VARCHAR(64)  DEFAULT NULL,
    `status`           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    `metadata_json`    CLOB         DEFAULT NULL,
    `create_by`        VARCHAR(64)  DEFAULT NULL,
    `create_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`        VARCHAR(64)  DEFAULT NULL,
    `update_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `deleted_at`       TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY (`id`)
);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_storage_object_tenant_key`
    ON `storage_object` (`tenant_id`, `object_key`);
CREATE INDEX IF NOT EXISTS `idx_storage_object_namespace`
    ON `storage_object` (`tenant_id`, `namespace`, `status`, `create_time`);
CREATE INDEX IF NOT EXISTS `idx_storage_object_checksum`
    ON `storage_object` (`tenant_id`, `namespace`, `checksum`);

CREATE TABLE IF NOT EXISTS `storage_upload_session` (
    `session_id`       VARCHAR(64)  NOT NULL,
    `tenant_id`        BIGINT       NOT NULL,
    `space_id`         BIGINT       DEFAULT NULL,
    `namespace`        VARCHAR(255) NOT NULL,
    `file_name`        VARCHAR(512) NOT NULL,
    `content_type`     VARCHAR(128) DEFAULT 'application/octet-stream',
    `expected_size`    BIGINT       NOT NULL,
    `expected_checksum` VARCHAR(64) DEFAULT NULL,
    `total_chunks`     INT          NOT NULL,
    `status`           VARCHAR(20)  NOT NULL DEFAULT 'UPLOADING',
    `temp_path`        VARCHAR(1024) DEFAULT NULL,
    `error_message`    VARCHAR(2000) DEFAULT NULL,
    `last_heartbeat`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `expires_at`       TIMESTAMP    DEFAULT NULL,
    `create_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_time`      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`session_id`)
);
CREATE INDEX IF NOT EXISTS `idx_storage_upload_session_tenant`
    ON `storage_upload_session` (`tenant_id`, `status`, `update_time`);

CREATE TABLE IF NOT EXISTS `storage_upload_chunk` (
    `session_id`       VARCHAR(64) NOT NULL,
    `chunk_index`      INT         NOT NULL,
    `chunk_size`       BIGINT      NOT NULL DEFAULT 0,
    `chunk_checksum`   VARCHAR(64) DEFAULT NULL,
    `status`           VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
    `uploaded_at`      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`session_id`, `chunk_index`)
);
