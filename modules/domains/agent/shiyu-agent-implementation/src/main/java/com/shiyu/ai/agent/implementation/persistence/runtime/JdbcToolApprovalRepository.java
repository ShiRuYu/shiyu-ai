package com.shiyu.ai.agent.implementation.persistence.runtime;

import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.port.ToolApprovalRepository;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * 负责 Jdbc 工具 Approval 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Repository
public class JdbcToolApprovalRepository implements ToolApprovalRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * 执行 Jdbc 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param jdbc 用于完成本次业务处理的 jdbc 参数。
     */
    public JdbcToolApprovalRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 创建或保存 Jdbc 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approval 用于完成本次业务处理的 approval 参数。
     */
    @Override
    public void insert(ToolApproval approval) {
        TenantScope.requireMatches(new TenantId(approval.tenantId()));
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
     * 查询 Jdbc 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param runId 用于定位run的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ToolApproval> list(String runId, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc.query(
                "SELECT * FROM AI_TOOL_APPROVAL WHERE RUN_ID=? AND TENANT_ID=? AND OWNER_USER_ID=?"
                        + " ORDER BY CREATED_AT,ID",
                (rs, n) -> map(rs),
                runId,
                tenantId.value(),
                ownerUserId);
    }

    /**
     * 查询 Jdbc 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ToolApproval> listAll(TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc.query(
                "SELECT * FROM AI_TOOL_APPROVAL WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY"
                        + " CREATED_AT DESC,ID",
                (rs, n) -> map(rs),
                tenantId.value(),
                ownerUserId);
    }

    /**
     * 查询 Jdbc 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<ToolApproval> find(String id, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
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
     * 更新或设置 Jdbc 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param approval 用于完成本次业务处理的 approval 参数。
     * @param expectedStatus 用于完成本次业务处理的 expectedStatus 参数。
     * @return 返回 Jdbc 工具 Approval 相关操作生成的结果数据。
     */
    @Override
    public int update(ToolApproval approval, ToolApprovalStatus expectedStatus) {
        TenantScope.requireMatches(new TenantId(approval.tenantId()));
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
     * 执行 Jdbc 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 Jdbc 工具 Approval 相关操作生成的结果数据。
     */
    @Override
    public int expirePending(TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
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
