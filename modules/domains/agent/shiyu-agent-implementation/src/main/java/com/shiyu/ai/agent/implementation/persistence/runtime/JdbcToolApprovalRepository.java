package com.shiyu.ai.agent.implementation.persistence.runtime;

import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.port.ToolApprovalRepository;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * JdbcToolApprovalRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
 */
@Repository
public class JdbcToolApprovalRepository implements ToolApprovalRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * {@code JdbcToolApprovalRepository} 创建并初始化当前类型实例。
     *
     * @param jdbc 参数值，用于执行当前操作。
     */
    public JdbcToolApprovalRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * {@code insert} 执行当前类型定义的业务操作。
     *
     * @param approval 参数值，用于执行当前操作。
     */
    @Override
    public void insert(ToolApproval approval) {
        jdbc.update(
                "INSERT INTO AI_TOOL_APPROVAL"
                    + " (ID,RUN_ID,TENANT_ID,OWNER_USER_ID,TOOL_NAME,ARGUMENTS_REDACTED,STATUS,CREATED_AT,DECIDED_AT,EXPIRES_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?)",
                approval.id(),
                approval.runId(),
                approval.tenantId(),
                approval.ownerUserId(),
                approval.toolName(),
                approval.argumentsRedacted(),
                approval.status().name(),
                Timestamp.from(approval.createdAt()),
                approval.decidedAt() == null ? null : Timestamp.from(approval.decidedAt()),
                Timestamp.from(approval.expiresAt()));
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param runId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ToolApproval> list(String runId, TenantId tenantId, long ownerUserId) {
        return jdbc.query(
                "SELECT * FROM AI_TOOL_APPROVAL WHERE RUN_ID=? AND TENANT_ID=? AND OWNER_USER_ID=?"
                        + " ORDER BY CREATED_AT,ID",
                (rs, n) -> map(rs),
                runId,
                tenantId.value(),
                ownerUserId);
    }

    /**
     * {@code listAll} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<ToolApproval> listAll(TenantId tenantId, long ownerUserId) {
        return jdbc.query(
                "SELECT * FROM AI_TOOL_APPROVAL WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY"
                        + " CREATED_AT DESC,ID",
                (rs, n) -> map(rs),
                tenantId.value(),
                ownerUserId);
    }

    /**
     * {@code find} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<ToolApproval> find(String id, TenantId tenantId, long ownerUserId) {
        return jdbc
                .query(
                        "SELECT * FROM AI_TOOL_APPROVAL WHERE ID=? AND TENANT_ID=? AND"
                                + " OWNER_USER_ID=?",
                        (rs, n) -> map(rs),
                        id,
                        tenantId.value(),
                        ownerUserId)
                .stream()
                .findFirst();
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param approval 参数值，用于执行当前操作。
     * @param expectedStatus 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int update(ToolApproval approval, ToolApprovalStatus expectedStatus) {
        return jdbc.update(
                "UPDATE AI_TOOL_APPROVAL SET STATUS=?,DECIDED_AT=? WHERE ID=? AND TENANT_ID=? AND"
                        + " OWNER_USER_ID=? AND STATUS=?",
                approval.status().name(),
                approval.decidedAt() == null ? null : Timestamp.from(approval.decidedAt()),
                approval.id(),
                approval.tenantId(),
                approval.ownerUserId(),
                expectedStatus.name());
    }

    /**
     * {@code expirePending} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int expirePending(TenantId tenantId, long ownerUserId) {
        return jdbc.update(
                "UPDATE AI_TOOL_APPROVAL SET STATUS='EXPIRED',DECIDED_AT=CURRENT_TIMESTAMP WHERE"
                        + " TENANT_ID=? AND OWNER_USER_ID=? AND STATUS='PENDING' AND"
                        + " EXPIRES_AT<CURRENT_TIMESTAMP",
                tenantId.value(),
                ownerUserId);
    }

    private ToolApproval map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ToolApproval(
                rs.getString("ID"),
                rs.getString("RUN_ID"),
                rs.getLong("TENANT_ID"),
                rs.getLong("OWNER_USER_ID"),
                rs.getString("TOOL_NAME"),
                rs.getString("ARGUMENTS_REDACTED"),
                ToolApprovalStatus.valueOf(rs.getString("STATUS")),
                rs.getTimestamp("CREATED_AT").toInstant(),
                rs.getTimestamp("DECIDED_AT") == null
                        ? null
                        : rs.getTimestamp("DECIDED_AT").toInstant(),
                rs.getTimestamp("EXPIRES_AT") == null
                        ? null
                        : rs.getTimestamp("EXPIRES_AT").toInstant());
    }
}
