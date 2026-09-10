package com.shiyu.ai.common.vector.implementation;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorSearchRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** PostgreSQL pgvector implementation of the common vector store SPI. */
public final class PgVectorStore implements VectorStore {

    private final JdbcTemplate jdbc;
    private final String namespace;
    private final int dimension;

    public PgVectorStore(JdbcTemplate jdbc, String namespace, int dimension) {
        this.jdbc = Objects.requireNonNull(jdbc, "JdbcTemplate must not be null");
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("Vector store namespace must not be blank");
        }
        if (dimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be greater than zero");
        }
        this.namespace = namespace;
        this.dimension = dimension;
        initializeSchema();
    }

    @Override
    public String type() {
        return "pgvector";
    }

    @Override
    public void upsert(VectorRecord record) {
        Objects.requireNonNull(record, "Vector record must not be null");
        validateVector(record.vector());
        String metadata = JSONUtils.toJsonString(record.metadata() == null ? Map.of() : record.metadata());
        jdbc.update("""
                        INSERT INTO shiyu_vector_item
                            (vector_namespace, vector_id, dimension, embedding, metadata, updated_at)
                        VALUES (?, ?, ?, CAST(? AS %s), CAST(? AS jsonb), CURRENT_TIMESTAMP)
                        ON CONFLICT (vector_namespace, vector_id) DO UPDATE SET
                            dimension = EXCLUDED.dimension,
                            embedding = EXCLUDED.embedding,
                            metadata = EXCLUDED.metadata,
                            updated_at = CURRENT_TIMESTAMP
                        """.formatted(vectorType()),
                namespace, record.id(), dimension, literal(record.vector()), metadata);
    }

    @Override
    public List<VectorRecord> search(float[] queryVector, int topK) {
        return search(VectorSearchRequest.builder().queryVector(queryVector).topK(topK).build());
    }

    @Override
    public List<VectorRecord> search(VectorSearchRequest request) {
        Objects.requireNonNull(request, "Vector search request must not be null");
        validateVector(request.getQueryVector());
        if (request.getTopK() <= 0) return List.of();

        String vectorType = vectorType();
        String vectorColumn = "embedding::" + vectorType;
        StringBuilder sql = new StringBuilder(
                "SELECT vector_id, embedding::text AS embedding_text, metadata::text AS metadata_text, "
                        + "1 - (" + vectorColumn + " <=> CAST(? AS " + vectorType + ")) AS score "
                        + "FROM shiyu_vector_item WHERE vector_namespace = ? AND dimension = ?");
        List<Object> arguments = new ArrayList<>(List.of(
                literal(request.getQueryVector()), namespace, dimension));
        for (Map.Entry<String, Object> entry : request.getFilter().entrySet()) {
            sql.append(" AND metadata ->> ? = ?");
            arguments.add(entry.getKey());
            arguments.add(String.valueOf(entry.getValue()));
        }
        sql.append(" AND 1 - (").append(vectorColumn).append(" <=> CAST(? AS ")
                .append(vectorType).append(")) >= ?");
        arguments.add(literal(request.getQueryVector()));
        arguments.add(request.getMinScore());
        sql.append(" ORDER BY ").append(vectorColumn).append(" <=> CAST(? AS ")
                .append(vectorType).append(") LIMIT ?");
        arguments.add(literal(request.getQueryVector()));
        arguments.add(request.getTopK());

        return jdbc.query(sql.toString(), statement -> {
            for (int i = 0; i < arguments.size(); i++) {
                statement.setObject(i + 1, arguments.get(i));
            }
        }, (resultSet, rowNum) -> {
            Map<String, Object> metadata = parseMetadata(resultSet.getString("metadata_text"));
            metadata.put("_score", resultSet.getDouble("score"));
            return new VectorRecord(
                    resultSet.getString("vector_id"),
                    parseVector(resultSet.getString("embedding_text")),
                    metadata);
        });
    }

    @Override
    public void delete(String id) {
        jdbc.update("DELETE FROM shiyu_vector_item WHERE vector_namespace = ? AND vector_id = ?",
                namespace, id);
    }

    @Override
    public void rebuild() {
        jdbc.update("DELETE FROM shiyu_vector_item WHERE vector_namespace = ?", namespace);
    }

    @Override
    public int size() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM shiyu_vector_item WHERE vector_namespace = ?", Integer.class, namespace);
        return count == null ? 0 : count;
    }

    private void initializeSchema() {
        Boolean extensionPresent = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'vector')", Boolean.class);
        if (!Boolean.TRUE.equals(extensionPresent)) {
            throw new IllegalStateException("PostgreSQL extension 'vector' is required; install pgvector before startup");
        }
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS shiyu_vector_item (
                    vector_namespace VARCHAR(512) NOT NULL,
                    vector_id VARCHAR(512) NOT NULL,
                    dimension INTEGER NOT NULL,
                    embedding vector NOT NULL,
                    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
                    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (vector_namespace, vector_id)
                )
                """);
        Integer mismatched = jdbc.queryForObject(
                "SELECT COUNT(*) FROM shiyu_vector_item WHERE vector_namespace = ? "
                        + "AND (dimension <> ? OR vector_dims(embedding::vector) <> ?)",
                Integer.class, namespace, dimension, dimension);
        if (mismatched != null && mismatched > 0) {
            throw new IllegalStateException("Vector dimension mismatch in namespace " + namespace
                    + ": expected " + dimension);
        }
        // pgvector HNSW indexes require a fixed dimension. The table remains
        // dimension-flexible so different tenants can migrate independently;
        // each dimension gets a partial index covering only valid rows.
        String indexName = "shiyu_vector_item_embedding_d" + dimension;
        jdbc.execute("CREATE INDEX IF NOT EXISTS " + indexName
                + " ON shiyu_vector_item USING hnsw ((embedding::vector(" + dimension + ")) vector_cosine_ops)"
                + " WHERE dimension = " + dimension + " AND vector_dims(embedding::vector) = " + dimension);
    }

    private void validateVector(float[] vector) {
        if (vector == null || vector.length != dimension) {
            throw new IllegalArgumentException("Vector dimension mismatch: expected " + dimension
                    + ", actual " + (vector == null ? 0 : vector.length));
        }
    }

    private String literal(float[] vector) {
        validateVector(vector);
        StringBuilder value = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) value.append(',');
            value.append(Float.toString(vector[i]));
        }
        return value.append(']').toString();
    }

    private String vectorType() {
        return "vector(" + dimension + ")";
    }

    private float[] parseVector(String value) {
        if (value == null || value.length() < 2) return new float[0];
        String body = value.substring(1, value.length() - 1).trim();
        if (body.isEmpty()) return new float[0];
        String[] parts = body.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) vector[i] = Float.parseFloat(parts[i].trim());
        return vector;
    }

    private Map<String, Object> parseMetadata(String value) {
        if (value == null || value.isBlank()) return new LinkedHashMap<>();
        Map<String, Object> parsed = JSONUtils.parseMap(value);
        return parsed == null ? new LinkedHashMap<>() : new LinkedHashMap<>(parsed);
    }
}
