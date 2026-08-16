package com.shiyu.ai.dal.runtime;

import com.shiyu.ai.runtime.*;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JdbcToolApprovalRepository implements ToolApprovalRepository {
    private final JdbcTemplate jdbc;
    public JdbcToolApprovalRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public void insert(ToolApproval a) { jdbc.update("INSERT INTO AI_TOOL_APPROVAL (ID,RUN_ID,TENANT_ID,OWNER_USER_ID,TOOL_NAME,ARGUMENTS_REDACTED,STATUS,CREATED_AT,DECIDED_AT) VALUES (?,?,?,?,?,?,?,?,?)", a.id(), a.runId(), a.tenantId(), a.ownerUserId(), a.toolName(), a.argumentsRedacted(), a.status().name(), Timestamp.from(a.createdAt()), a.decidedAt() == null ? null : Timestamp.from(a.decidedAt())); }
    @Override public List<ToolApproval> list(String runId, long tenantId, long ownerUserId) { return jdbc.query("SELECT * FROM AI_TOOL_APPROVAL WHERE RUN_ID=? AND TENANT_ID=? AND OWNER_USER_ID=? ORDER BY CREATED_AT", (rs, n) -> map(rs), runId, tenantId, ownerUserId); }
    @Override public Optional<ToolApproval> find(String id, long tenantId, long ownerUserId) { return jdbc.query("SELECT * FROM AI_TOOL_APPROVAL WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=?", rs -> rs.next() ? Optional.of(map(rs)) : Optional.empty(), id, tenantId, ownerUserId); }
    @Override public int update(ToolApproval a, ToolApprovalStatus expected) { return jdbc.update("UPDATE AI_TOOL_APPROVAL SET STATUS=?,DECIDED_AT=? WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND STATUS=?", a.status().name(), a.decidedAt() == null ? null : Timestamp.from(a.decidedAt()), a.id(), a.tenantId(), a.ownerUserId(), expected.name()); }
    private ToolApproval map(java.sql.ResultSet rs) throws java.sql.SQLException { return new ToolApproval(rs.getString("ID"), rs.getString("RUN_ID"), rs.getLong("TENANT_ID"), rs.getLong("OWNER_USER_ID"), rs.getString("TOOL_NAME"), rs.getString("ARGUMENTS_REDACTED"), ToolApprovalStatus.valueOf(rs.getString("STATUS")), rs.getTimestamp("CREATED_AT").toInstant(), rs.getTimestamp("DECIDED_AT") == null ? null : rs.getTimestamp("DECIDED_AT").toInstant()); }
}
