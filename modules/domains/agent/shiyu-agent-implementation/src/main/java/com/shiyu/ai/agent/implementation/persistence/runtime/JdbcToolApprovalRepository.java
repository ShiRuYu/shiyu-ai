package com.shiyu.ai.agent.implementation.persistence.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.runtime.ToolApproval;
import com.shiyu.ai.runtime.ToolApprovalRepository;
import com.shiyu.ai.runtime.ToolApprovalStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Durable approval state; the in-memory implementation is only a test/fallback adapter. */
@Repository
public class JdbcToolApprovalRepository implements ToolApprovalRepository {
    private final JdbcTemplate jdbc;

    public JdbcToolApprovalRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public void insert(ToolApproval approval) {
        jdbc.update("INSERT INTO AI_TOOL_APPROVAL (ID,RUN_ID,TENANT_ID,OWNER_USER_ID,TOOL_NAME,ARGUMENTS_REDACTED,STATUS,CREATED_AT,DECIDED_AT,EXPIRES_AT) VALUES (?,?,?,?,?,?,?,?,?,?)",
                approval.id(), approval.runId(), approval.tenantId(), approval.ownerUserId(), approval.toolName(), approval.argumentsRedacted(),
                approval.status().name(), Timestamp.from(approval.createdAt()), approval.decidedAt() == null ? null : Timestamp.from(approval.decidedAt()), Timestamp.from(approval.expiresAt()));
    }

    @Override
    public List<ToolApproval> list(String runId, TenantId tenantId, long ownerUserId) {
        return jdbc.query("SELECT * FROM AI_TOOL_APPROVAL WHERE RUN_ID=? AND TENANT_ID=? AND OWNER_USER_ID=? ORDER BY CREATED_AT,ID",
                (rs, n) -> map(rs), runId, tenantId.value(), ownerUserId);
    }

    @Override
    public List<ToolApproval> listAll(TenantId tenantId, long ownerUserId) {
        return jdbc.query("SELECT * FROM AI_TOOL_APPROVAL WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY CREATED_AT DESC,ID",
                (rs, n) -> map(rs), tenantId.value(), ownerUserId);
    }

    @Override
    public Optional<ToolApproval> find(String id, TenantId tenantId, long ownerUserId) {
        return jdbc.query("SELECT * FROM AI_TOOL_APPROVAL WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=?",
                (rs, n) -> map(rs), id, tenantId.value(), ownerUserId).stream().findFirst();
    }

    @Override
    public int update(ToolApproval approval, ToolApprovalStatus expectedStatus) {
        return jdbc.update("UPDATE AI_TOOL_APPROVAL SET STATUS=?,DECIDED_AT=? WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND STATUS=?",
                approval.status().name(), approval.decidedAt() == null ? null : Timestamp.from(approval.decidedAt()), approval.id(),
                approval.tenantId(), approval.ownerUserId(), expectedStatus.name());
    }

    @Override
    public int expirePending(TenantId tenantId, long ownerUserId) {
        return jdbc.update("UPDATE AI_TOOL_APPROVAL SET STATUS='EXPIRED',DECIDED_AT=CURRENT_TIMESTAMP WHERE TENANT_ID=? AND OWNER_USER_ID=? AND STATUS='PENDING' AND EXPIRES_AT<CURRENT_TIMESTAMP", tenantId.value(), ownerUserId);
    }

    private ToolApproval map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new ToolApproval(rs.getString("ID"), rs.getString("RUN_ID"), rs.getLong("TENANT_ID"), rs.getLong("OWNER_USER_ID"),
                rs.getString("TOOL_NAME"), rs.getString("ARGUMENTS_REDACTED"), ToolApprovalStatus.valueOf(rs.getString("STATUS")),
                rs.getTimestamp("CREATED_AT").toInstant(), rs.getTimestamp("DECIDED_AT") == null ? null : rs.getTimestamp("DECIDED_AT").toInstant(),
                rs.getTimestamp("EXPIRES_AT") == null ? null : rs.getTimestamp("EXPIRES_AT").toInstant());
    }
}

