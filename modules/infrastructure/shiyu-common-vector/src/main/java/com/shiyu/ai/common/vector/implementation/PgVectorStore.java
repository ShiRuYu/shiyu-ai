package com.shiyu.ai.common.vector.implementation;

import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.common.vector.model.VectorSearchRequest;
import com.shiyu.ai.common.vector.model.VectorSearchType;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 在 PostgreSQL 中持久化 namespace 隔离的向量，提供统一余弦分数和精确/近似检索。
 */
public final class PgVectorStore implements VectorStore {

    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;
    /**
     * namespace 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String namespace;
    /**
     * 维度，表示当前对象中的对应属性。
     */
    private final int dimension;
    /** 将 ANN 扫描设置限制在当前事务，避免污染连接池中的后续请求。 */
    private final TransactionTemplate transactions;

    /**
     * 执行 Pg 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param jdbc 用于完成本次业务处理的 jdbc 参数。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param dimension 用于完成本次业务处理的 dimension 参数。
     */
    public PgVectorStore(JdbcTemplate jdbc, String namespace, int dimension) {
        this.jdbc = Objects.requireNonNull(jdbc, "JdbcTemplate must not be null");
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("Vector store namespace must not be blank");
        }
        if (dimension <= 0 || dimension > 16000) {
            throw new IllegalArgumentException("Vector dimension must be between 1 and 16000");
        }
        this.namespace = namespace;
        this.dimension = dimension;
        this.transactions = new TransactionTemplate(
                new DataSourceTransactionManager(Objects.requireNonNull(jdbc.getDataSource(), "DataSource must not be null")));
        transactions.executeWithoutResult(status -> {
            // IF NOT EXISTS 本身不能消除并发创建表或索引时的系统目录竞争。
            jdbc.execute("SELECT pg_advisory_xact_lock(746821044, 5)");
            initializeSchema();
        });
    }

    /**
     * 执行 Pg 向量 相关业务数据，并返回处理结果。
     *
     * @return 返回 Pg 向量 相关操作生成的结果数据。
     */
    @Override
    public String type() {
        return "pgvector";
    }

    /**
     * 执行 Pg 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param record 用于完成本次业务处理的 record 参数。
     */
    @Override
    public void upsert(VectorRecord record) {
        Objects.requireNonNull(record, "Vector record must not be null");
        validateVector(record.vector());
        String metadata =
                JSONUtils.toJsonString(record.metadata() == null ? Map.of() : record.metadata());
        jdbc.update(
                """
                INSERT INTO shiyu_vector_item
                    (vector_namespace, vector_id, dimension, embedding, metadata, updated_at)
                VALUES (?, ?, ?, CAST(? AS %s), CAST(? AS jsonb), CURRENT_TIMESTAMP)
                ON CONFLICT (vector_namespace, vector_id) DO UPDATE SET
                    dimension = EXCLUDED.dimension,
                    embedding = EXCLUDED.embedding,
                    metadata = EXCLUDED.metadata,
                    updated_at = CURRENT_TIMESTAMP
                """
                        .formatted(vectorType()),
                namespace,
                record.id(),
                dimension,
                literal(record.vector()),
                metadata);
    }

    /**
     * 查询 Pg 向量 相关业务数据，并返回处理结果。
     *
     * @param queryVector 用于完成本次业务处理的 queryVector 参数。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<VectorRecord> search(float[] queryVector, int topK) {
        return search(VectorSearchRequest.builder().queryVector(queryVector).topK(topK).build());
    }

    /**
     * 查询 Pg 向量 相关业务数据，并返回处理结果。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<VectorRecord> search(VectorSearchRequest request) {
        Objects.requireNonNull(request, "Vector search request must not be null");
        validateVector(request.getQueryVector());
        if (request.getTopK() <= 0) return List.of();

        String vectorType = vectorType();
        String vectorColumn = "embedding::" + vectorType;
        StringBuilder sql =
                new StringBuilder(
                        "SELECT vector_id, embedding::text AS embedding_text, metadata::text AS"
                                + " metadata_text, 1 - ("
                                + vectorColumn
                                + " <=> CAST(? AS "
                                + vectorType
                                + ")) / 2.0 AS score FROM shiyu_vector_item WHERE vector_namespace = ?"
                                + " AND dimension = " + dimension
                                + " AND vector_dims(embedding::vector) = " + dimension);
        List<Object> arguments =
                new ArrayList<>(List.of(literal(request.getQueryVector()), namespace));
        appendMetadataFilters(sql, arguments, request.getFilter());
        sql.append(" AND 1 - (")
                .append(vectorColumn)
                .append(" <=> CAST(? AS ")
                .append(vectorType)
                .append(")) / 2.0 >= ?");
        arguments.add(literal(request.getQueryVector()));
        arguments.add(request.getMinScore());
        if (request.getSearchType() == VectorSearchType.EXACT) {
            sql.append(" ORDER BY score DESC");
        } else {
            sql.append(" ORDER BY ")
                    .append(vectorColumn)
                    .append(" <=> CAST(? AS ")
                    .append(vectorType)
                    .append(")");
            arguments.add(literal(request.getQueryVector()));
        }
        sql.append(" LIMIT ?");
        arguments.add(request.getTopK());

        if (request.getSearchType() == VectorSearchType.ANN && dimension <= 2000) {
            List<VectorRecord> matches = transactions.execute(status -> {
                jdbc.execute("SET LOCAL hnsw.iterative_scan = strict_order");
                return query(sql.toString(), arguments);
            });
            if (matches.size() < request.getTopK()) {
                // namespace/元数据过滤可能耗尽 ANN 候选；精确补查避免已有数据返回空结果。
                return search(VectorSearchRequest.builder()
                        .queryVector(request.getQueryVector()).topK(request.getTopK())
                        .minScore(request.getMinScore()).filter(request.getFilter())
                        .searchType(VectorSearchType.EXACT).build());
            }
            return matches;
        }
        return query(sql.toString(), arguments);
    }

    private List<VectorRecord> query(String sql, List<Object> arguments) {
        return jdbc.query(
                sql,
                statement -> {
                    for (int i = 0; i < arguments.size(); i++) {
                        statement.setObject(i + 1, arguments.get(i));
                    }
                },
                (resultSet, rowNum) -> {
                    Map<String, Object> metadata =
                            parseMetadata(resultSet.getString("metadata_text"));
                    metadata.put("_score", resultSet.getDouble("score"));
                    return new VectorRecord(
                            resultSet.getString("vector_id"),
                            parseVector(resultSet.getString("embedding_text")),
                            metadata);
                });
    }

    /**
     * 删除或移除 Pg 向量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
     */
    @Override
    public void delete(String id) {
        jdbc.update(
                "DELETE FROM shiyu_vector_item WHERE vector_namespace = ? AND vector_id = ?",
                namespace,
                id);
    }

    /**
     * 执行 Pg 向量 相关业务操作，并维护必要的状态和协作关系。
     */
    @Override
    public void rebuild() {
        jdbc.update("DELETE FROM shiyu_vector_item WHERE vector_namespace = ?", namespace);
    }

    /**
     * 执行 Pg 向量 相关业务数据，并返回处理结果。
     *
     * @return 返回 Pg 向量 相关操作生成的结果数据。
     */
    @Override
    public int size() {
        Integer count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM shiyu_vector_item WHERE vector_namespace = ?",
                        Integer.class,
                        namespace);
        return count == null ? 0 : count;
    }

    @Override
    public int count(Map<String, Object> filter) {
        StringBuilder sql =
                new StringBuilder(
                        "SELECT COUNT(*) FROM shiyu_vector_item WHERE vector_namespace = ? AND dimension = ?");
        List<Object> arguments = new ArrayList<>(List.of(namespace, dimension));
        Map<String, Object> filters = filter == null ? Map.of() : filter;
        appendMetadataFilters(sql, arguments, filters);
        Integer count =
                jdbc.queryForObject(
                        sql.toString(), Integer.class, arguments.toArray(new Object[0]));
        return count == null ? 0 : count;
    }

    private void appendMetadataFilters(
            StringBuilder sql, List<Object> arguments, Map<String, Object> filters) {
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            if (entry.getValue() == null) {
                sql.append(
                        " AND (metadata -> ? = 'null'::jsonb OR NOT jsonb_exists(metadata, ?))");
                arguments.add(entry.getKey());
                arguments.add(entry.getKey());
            } else {
                sql.append(" AND metadata -> ? = CAST(? AS jsonb)");
                arguments.add(entry.getKey());
                arguments.add(JSONUtils.toJsonString(entry.getValue()));
            }
        }
    }

    private void initializeSchema() {
        Boolean extensionPresent =
                jdbc.queryForObject(
                        "SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'vector')",
                        Boolean.class);
        if (!Boolean.TRUE.equals(extensionPresent)) {
            throw new IllegalStateException(
                    "PostgreSQL extension 'vector' is required; install pgvector before startup");
        }
        jdbc.execute(
                """
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
        Integer mismatched =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM shiyu_vector_item WHERE vector_namespace = ? "
                                + "AND (dimension <> ? OR vector_dims(embedding::vector) <> ?)",
                        Integer.class,
                        namespace,
                        dimension,
                        dimension);
        if (mismatched != null && mismatched > 0) {
            throw new IllegalStateException(
                    "Vector dimension mismatch in namespace "
                            + namespace
                            + ": expected "
                            + dimension);
        }
        // vector 的 HNSW 索引最多支持 2000 维；更高维度保留原始精度并使用精确扫描。
        if (dimension > 2000) return;
        String indexName = "shiyu_vector_item_embedding_d" + dimension;
        jdbc.execute(
                "CREATE INDEX IF NOT EXISTS "
                        + indexName
                        + " ON shiyu_vector_item USING hnsw ((embedding::vector("
                        + dimension
                        + ")) vector_cosine_ops)"
                        + " WHERE dimension = "
                        + dimension
                        + " AND vector_dims(embedding::vector) = "
                        + dimension);
    }

    private void validateVector(float[] vector) {
        if (vector == null || vector.length != dimension) {
            throw new IllegalArgumentException(
                    "Vector dimension mismatch: expected "
                            + dimension
                            + ", actual "
                            + (vector == null ? 0 : vector.length));
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
