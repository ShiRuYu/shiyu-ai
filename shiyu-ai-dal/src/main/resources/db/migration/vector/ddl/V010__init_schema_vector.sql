-- Vector/RAG 相关表
-- 注意：embedding 以 JSON 数组文本存储，H2 不原生支持向量类型
-- VectorStore（Qdrant/HNSW）负责向量索引，H2 仅作为 source of truth

CREATE TABLE IF NOT EXISTS `knowledge_chunk` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `document_id`   BIGINT       NOT NULL,
    `content`       TEXT         NOT NULL,
    `embedding`     TEXT         DEFAULT NULL COMMENT 'JSON float array',
    `metadata`      TEXT         DEFAULT NULL COMMENT 'JSON map',
    `chunk_index`   INT          DEFAULT 0,
    `tenant_id`     BIGINT       DEFAULT NULL,
    `create_by`     VARCHAR(64)  DEFAULT NULL,
    `create_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    `update_by`     VARCHAR(64)  DEFAULT NULL,
    `update_time`   TIMESTAMP    DEFAULT NULL,
    `status`        TINYINT      DEFAULT 1,
    `del_flag`      TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_kc_document` (`document_id`)
);
