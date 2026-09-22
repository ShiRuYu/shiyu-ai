package com.shiyu.ai.agent.implementation.evaluation.persistence;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalCase;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalDataset;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalMetric;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalResult;
import com.shiyu.ai.agent.implementation.evaluation.model.EvalRun;
import com.shiyu.ai.agent.implementation.evaluation.port.EvaluationRepository;

import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

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
 * 负责 Jdbc Evaluation 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Repository
public class JdbcEvaluationRepository implements EvaluationRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * 执行 Jdbc Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dataSource 用于完成本次业务处理的 dataSource 参数。
     */
    public JdbcEvaluationRepository(@Qualifier("agentDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * 创建或保存 Jdbc Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param d 用于完成本次业务处理的 d 参数。
     */
    @Override
    public void insertDataset(EvalDataset d) {
        requireTenant(d.tenantId());
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
     * 查询 Jdbc Evaluation 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<EvalDataset> findDataset(String id, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
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
     * 创建或保存 Jdbc Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param c 用于完成本次业务处理的 c 参数。
     */
    @Override
    public void insertCase(EvalCase c) {
        requireTenant(c.tenantId());
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
     * 查询 Jdbc Evaluation 相关业务数据，并返回处理结果。
     *
     * @param datasetId 用于定位dataset的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<EvalCase> listCases(String datasetId, TenantId tenantId) {
        TenantScope.requireMatches(tenantId);
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
     * 创建或保存 Jdbc Evaluation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param r 用于完成本次业务处理的 r 参数。
     */
    @Override
    public void insertRun(EvalRun r) {
        requireTenant(r.tenantId());
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
     * 查询 Jdbc Evaluation 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<EvalRun> findRun(String id, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
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

    private static void requireTenant(long tenantId) {
        TenantScope.requireMatches(new TenantId(tenantId));
    }
}
