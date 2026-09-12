package com.shiyu.ai.agent.implementation.evaluation.persistence;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalDataset;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalMetric;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalResult;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalRun;
import com.shiyu.ai.agent.implementation.evaluation.port.EvaluationRepository;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import tools.jackson.core.type.TypeReference;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * {@code JdbcEvaluationRepository} 定义智能体模块的持久化端口，隔离领域逻辑与具体存储实现。
 */
@Repository
public class JdbcEvaluationRepository implements EvaluationRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * {@code JdbcEvaluationRepository} 创建并初始化当前类型实例。
     *
     * @param dataSource 参数值，用于执行当前操作。
     */
    public JdbcEvaluationRepository(@Qualifier("agentDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * {@code insertDataset} 执行当前类型定义的业务操作。
     *
     * @param d 参数值，用于执行当前操作。
     */
    @Override
    public void insertDataset(EvalDataset d) {
        jdbc.update(
                "INSERT INTO AGENT_EVAL_DATASET"
                        + " (ID,TENANT_ID,OWNER_USER_ID,NAME,DESCRIPTION,CREATED_AT) VALUES"
                        + " (?,?,?,?,?,?)",
                d.id(),
                d.tenantId(),
                d.ownerUserId(),
                d.name(),
                d.description(),
                ts(d.createdAt()));
    }

    /**
     * {@code findDataset} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<EvalDataset> findDataset(String id, TenantId tenantId, long ownerUserId) {
        return jdbc
                .query(
                        "SELECT * FROM AGENT_EVAL_DATASET WHERE ID=? AND TENANT_ID=? AND"
                                + " OWNER_USER_ID=?",
                        (rs, n) ->
                                new EvalDataset(
                                        rs.getString("ID"),
                                        rs.getLong("TENANT_ID"),
                                        rs.getLong("OWNER_USER_ID"),
                                        rs.getString("NAME"),
                                        rs.getString("DESCRIPTION"),
                                        rs.getTimestamp("CREATED_AT").toInstant()),
                        id,
                        tenantId.value(),
                        ownerUserId)
                .stream()
                .findFirst();
    }

    /**
     * {@code insertCase} 执行当前类型定义的业务操作。
     *
     * @param c 参数值，用于执行当前操作。
     */
    @Override
    public void insertCase(EvalCase c) {
        jdbc.update(
                "INSERT INTO AGENT_EVAL_CASE"
                    + " (ID,DATASET_ID,TENANT_ID,INPUT_TEXT,EXPECTED_TEXT,METADATA_JSON,CREATED_AT)"
                    + " VALUES (?,?,?,?,?,?,?)",
                c.id(),
                c.datasetId(),
                c.tenantId(),
                c.input(),
                c.expected(),
                JSONUtils.toJsonString(c.metadata()),
                ts(c.createdAt()));
    }

    /**
     * {@code listCases} 查询并返回当前操作所需的数据。
     *
     * @param datasetId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<EvalCase> listCases(String datasetId, TenantId tenantId) {
        return jdbc.query(
                "SELECT * FROM AGENT_EVAL_CASE WHERE DATASET_ID=? AND TENANT_ID=? ORDER BY"
                        + " CREATED_AT,ID",
                (rs, n) ->
                        new EvalCase(
                                rs.getString("ID"),
                                rs.getString("DATASET_ID"),
                                rs.getLong("TENANT_ID"),
                                rs.getString("INPUT_TEXT"),
                                rs.getString("EXPECTED_TEXT"),
                                parseMap(rs.getString("METADATA_JSON")),
                                rs.getTimestamp("CREATED_AT").toInstant()),
                datasetId,
                tenantId.value());
    }

    /**
     * {@code insertRun} 执行当前类型定义的业务操作。
     *
     * @param r 参数值，用于执行当前操作。
     */
    @Override
    public void insertRun(EvalRun r) {
        jdbc.update(
                "INSERT INTO AGENT_EVAL_RUN"
                    + " (ID,DATASET_ID,TENANT_ID,OWNER_USER_ID,APP_VERSION_ID,METRIC,STATUS,PASS_RATE,RESULTS_JSON,CREATED_AT,COMPLETED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                r.id(),
                r.datasetId(),
                r.tenantId(),
                r.ownerUserId(),
                r.appVersionId(),
                r.metric().name(),
                r.status(),
                r.passRate(),
                JSONUtils.toJsonString(r.results()),
                ts(r.createdAt()),
                r.completedAt() == null ? null : ts(r.completedAt()));
    }

    /**
     * {@code findRun} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<EvalRun> findRun(String id, TenantId tenantId, long ownerUserId) {
        return jdbc
                .query(
                        "SELECT * FROM AGENT_EVAL_RUN WHERE ID=? AND TENANT_ID=? AND"
                                + " OWNER_USER_ID=?",
                        (rs, n) -> mapRun(rs),
                        id,
                        tenantId.value(),
                        ownerUserId)
                .stream()
                .findFirst();
    }

    private EvalRun mapRun(java.sql.ResultSet rs) throws java.sql.SQLException {
        List<Map<String, Object>> raw =
                JSONUtils.parseObject(
                        rs.getString("RESULTS_JSON"),
                        new TypeReference<List<Map<String, Object>>>() {});
        List<EvalResult> results =
                raw == null
                        ? List.of()
                        : raw.stream()
                                .map(
                                        v ->
                                                new EvalResult(
                                                        String.valueOf(v.get("caseId")),
                                                        EvalMetric.valueOf(
                                                                String.valueOf(v.get("metric"))),
                                                        number(v.get("score")),
                                                        Boolean.TRUE.equals(v.get("passed")),
                                                        String.valueOf(
                                                                v.getOrDefault("detail", ""))))
                                .toList();
        return new EvalRun(
                rs.getString("ID"),
                rs.getString("DATASET_ID"),
                rs.getLong("TENANT_ID"),
                rs.getLong("OWNER_USER_ID"),
                rs.getString("APP_VERSION_ID"),
                EvalMetric.valueOf(rs.getString("METRIC")),
                rs.getString("STATUS"),
                rs.getDouble("PASS_RATE"),
                results,
                rs.getTimestamp("CREATED_AT").toInstant(),
                rs.getTimestamp("COMPLETED_AT") == null
                        ? null
                        : rs.getTimestamp("COMPLETED_AT").toInstant());
    }

    private Map<String, Object> parseMap(String value) {
        return value == null || value.isBlank() ? Map.of() : JSONUtils.parseMap(value);
    }

    private double number(Object value) {
        return value instanceof Number n
                ? n.doubleValue()
                : Double.parseDouble(String.valueOf(value));
    }

    private static Timestamp ts(java.time.Instant value) {
        return Timestamp.from(value == null ? java.time.Instant.now() : value);
    }
}
