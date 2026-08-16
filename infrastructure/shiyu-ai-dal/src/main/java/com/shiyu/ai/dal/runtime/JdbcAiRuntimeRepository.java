package com.shiyu.ai.dal.runtime;

import com.shiyu.ai.runtime.*;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JdbcAiRuntimeRepository implements AiRunRepository {
    private final JdbcTemplate jdbc;
    public JdbcAiRuntimeRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public void insert(AiRun r) {
        jdbc.update("INSERT INTO AI_RUN (ID,TENANT_ID,OWNER_USER_ID,APP_ID,APP_VERSION_ID,SOURCE_TYPE,SOURCE_ID,PARENT_RUN_ID,TRACE_ID,CONVERSATION_ID,GENERATION_ID,EXECUTION_ID,MODEL,PROMPT_HASH,STATUS,PROMPT_TOKENS,COMPLETION_TOKENS,ESTIMATED_USAGE,COST_SNAPSHOT,CREATED_AT,COMPLETED_AT,ERROR_CODE,VERSION) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                r.id(),r.tenantId(),r.ownerUserId(),r.appId(),r.appVersionId(),r.sourceType().name(),r.sourceId(),r.parentRunId(),r.traceId(),r.conversationId(),r.generationId(),r.executionId(),r.model(),r.promptHash(),r.status().name(),r.promptTokens(),r.completionTokens(),r.estimatedUsage(),r.costSnapshot(),Timestamp.from(r.createdAt()),r.completedAt()==null?null:Timestamp.from(r.completedAt()),r.errorCode(),r.version());
    }
    @Override public Optional<AiRun> find(String id,long tenant,long owner) { return jdbc.query("SELECT * FROM AI_RUN WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=?", rs -> rs.next()?Optional.of(mapRun(rs)):Optional.empty(), id,tenant,owner); }
    @Override public Optional<AiRun> findByGeneration(String generationId,long tenant,long owner) { return jdbc.query("SELECT * FROM AI_RUN WHERE GENERATION_ID=? AND TENANT_ID=? AND OWNER_USER_ID=? ORDER BY CREATED_AT DESC LIMIT 1", rs -> rs.next()?Optional.of(mapRun(rs)):Optional.empty(), generationId,tenant,owner); }
    @Override public Optional<AiRun> findByExecution(String executionId,long tenant,long owner) { return jdbc.query("SELECT * FROM AI_RUN WHERE EXECUTION_ID=? AND TENANT_ID=? AND OWNER_USER_ID=? ORDER BY CREATED_AT DESC LIMIT 1", rs -> rs.next()?Optional.of(mapRun(rs)):Optional.empty(), executionId,tenant,owner); }
    @Override public int update(AiRun r,long expected) { return jdbc.update("UPDATE AI_RUN SET STATUS=?,PROMPT_TOKENS=?,COMPLETION_TOKENS=?,ESTIMATED_USAGE=?,COST_SNAPSHOT=?,COMPLETED_AT=?,ERROR_CODE=?,VERSION=? WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND VERSION=?",r.status().name(),r.promptTokens(),r.completionTokens(),r.estimatedUsage(),r.costSnapshot(),r.completedAt()==null?null:Timestamp.from(r.completedAt()),r.errorCode(),r.version(),r.id(),r.tenantId(),r.ownerUserId(),expected); }
    @Override public long appendEvent(AiRunEvent e) {
        List<AiRunEvent> existing = jdbc.query("SELECT RUN_ID,TENANT_ID,SEQ,TYPE,PAYLOAD,REDACTED,CREATED_AT FROM AI_RUN_EVENT WHERE RUN_ID=? AND TENANT_ID=? AND SEQ=?",
                (rs, n) -> new AiRunEvent(rs.getString("RUN_ID"), rs.getLong("TENANT_ID"), rs.getLong("SEQ"), AiRunEventType.valueOf(rs.getString("TYPE")), rs.getString("PAYLOAD"), rs.getBoolean("REDACTED"), rs.getTimestamp("CREATED_AT").toInstant()), e.runId(), e.tenantId(), e.seq());
        if (!existing.isEmpty()) {
            AiRunEvent old = existing.get(0);
            if (old.type() != e.type() || !java.util.Objects.equals(old.payload(), e.payload()) || old.redacted() != e.redacted()) {
                throw new IllegalStateException("event sequence already contains a different payload");
            }
            return e.seq();
        }
        jdbc.update("INSERT INTO AI_RUN_EVENT (RUN_ID,TENANT_ID,SEQ,TYPE,PAYLOAD,REDACTED,CREATED_AT) VALUES (?,?,?,?,?,?,?)",
                e.runId(), e.tenantId(), e.seq(), e.type().name(), e.payload(), e.redacted(), Timestamp.from(e.createdAt()));
        return e.seq();
    }
    @Override public List<AiRunEvent> events(String id,long tenant,long owner,long after,int limit) { find(id,tenant,owner).orElseThrow(() -> new IllegalArgumentException("run not found")); return jdbc.query("SELECT RUN_ID,TENANT_ID,SEQ,TYPE,PAYLOAD,REDACTED,CREATED_AT FROM AI_RUN_EVENT WHERE RUN_ID=? AND TENANT_ID=? AND SEQ>? ORDER BY SEQ LIMIT ?",(rs,n)->new AiRunEvent(rs.getString("RUN_ID"),rs.getLong("TENANT_ID"),rs.getLong("SEQ"),AiRunEventType.valueOf(rs.getString("TYPE")),rs.getString("PAYLOAD"),rs.getBoolean("REDACTED"),rs.getTimestamp("CREATED_AT").toInstant()),id,tenant,after,Math.max(1,Math.min(limit,100_000))); }
    private AiRun mapRun(java.sql.ResultSet rs) throws java.sql.SQLException { return new AiRun(rs.getString("ID"),rs.getLong("TENANT_ID"),rs.getLong("OWNER_USER_ID"),rs.getString("APP_ID"),rs.getString("APP_VERSION_ID"),AiRunSource.valueOf(rs.getString("SOURCE_TYPE")),rs.getString("SOURCE_ID"),rs.getString("PARENT_RUN_ID"),rs.getString("TRACE_ID"),rs.getString("CONVERSATION_ID"),rs.getString("GENERATION_ID"),rs.getString("EXECUTION_ID"),rs.getString("MODEL"),rs.getString("PROMPT_HASH"),AiRunStatus.valueOf(rs.getString("STATUS")),rs.getLong("PROMPT_TOKENS"),rs.getLong("COMPLETION_TOKENS"),rs.getBoolean("ESTIMATED_USAGE"),rs.getString("COST_SNAPSHOT"),rs.getTimestamp("CREATED_AT").toInstant(),rs.getTimestamp("COMPLETED_AT")==null?null:rs.getTimestamp("COMPLETED_AT").toInstant(),rs.getString("ERROR_CODE"),rs.getLong("VERSION")); }
}
