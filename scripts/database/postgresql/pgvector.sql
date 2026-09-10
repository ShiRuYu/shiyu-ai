-- Optional infrastructure objects for provider=pgvector.
-- Run as a PostgreSQL role allowed to install the vector extension.
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS shiyu_vector_item (
    vector_namespace VARCHAR(512) NOT NULL,
    vector_id VARCHAR(512) NOT NULL,
    dimension INTEGER NOT NULL CHECK (dimension > 0),
    embedding vector NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (vector_namespace, vector_id)
);

-- HNSW indexes are created by PgVectorStore per dimension. pgvector requires
-- a fixed dimension for each HNSW index while the shared table supports
-- independent tenant migrations with different dimensions.
