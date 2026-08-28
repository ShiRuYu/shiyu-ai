package com.shiyu.ai.agent.implementation.persistence.runtime;

import com.shiyu.ai.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
        jdbc.update("INSERT INTO AI_RUN (ID,TENANT_ID,OWNER_USER_ID,APP_ID,APP_VERSION_ID,SOURCE_TYPE,SOURCE_ID,PARENT_RUN_ID,TRACE_ID,CONVERSATION_ID,GENERATION_ID,EXECUTION_ID,MODEL,PROMPT_HASH,STATUS,PROMPT_TOKENS,COMPLETION_TOKENS,ESTIMATED_USAGE,COST_SNAPSHOT,CREATED_AT,COMPLETED_AT,ERROR_CODE,LAST_EVENT_SEQ,VERSION) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                r.id(),r.tenantId().value(),r.ownerUserId().value(),r.appId(),r.appVersionId(),r.sourceType().name(),r.sourceId(),r.parentRunId(),r.traceId(),r.conversationId(),r.generationId(),r.executionId(),r.model(),r.promptHash(),r.status().name(),r.promptTokens(),r.completionTokens(),r.estimatedUsage(),r.costSnapshot(),Timestamp.from(r.createdAt()),r.completedAt()==null?null:Timestamp.from(r.completedAt()),r.errorCode(),0L,r.version());
    }
    @Override public Optional<AiRun> find(String id,TenantId tenant,long owner) { return jdbc.query("SELECT * FROM AI_RUN WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=?", rs -> rs.next()?Optional.of(mapRun(rs)):Optional.empty(), id,tenant(tenant),owner); }
    @Override public List<AiRun> list(TenantId tenant,long owner,int limit) { return jdbc.query("SELECT * FROM AI_RUN WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY CREATED_AT DESC LIMIT ?", (rs, rowNum) -> mapRun(rs), tenant(tenant), owner, Math.max(1, Math.min(limit, 500))); }
    @Override public Optional<AiRun> findByGeneration(String generationId,TenantId tenant,long owner) { return jdbc.query("SELECT * FROM AI_RUN WHERE GENERATION_ID=? AND TENANT_ID=? AND OWNER_USER_ID=? ORDER BY CREATED_AT DESC LIMIT 1", rs -> rs.next()?Optional.of(mapRun(rs)):Optional.empty(), generationId,tenant(tenant),owner); }
    @Override public int linkGeneration(String runId, TenantId tenant, long owner, String generationId) {
        return jdbc.update("UPDATE AI_RUN SET GENERATION_ID=? WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND GENERATION_ID IS NULL", generationId, runId, tenant(tenant), owner);
    }
    @Override public Optional<AiRun> findByExecution(String executionId,TenantId tenant,long owner) { return jdbc.query("SELECT * FROM AI_RUN WHERE EXECUTION_ID=? AND TENANT_ID=? AND OWNER_USER_ID=? ORDER BY CREATED_AT DESC LIMIT 1", rs -> rs.next()?Optional.of(mapRun(rs)):Optional.empty(), executionId,tenant(tenant),owner); }
    @Override public int update(AiRun r,long expected) { return jdbc.update("UPDATE AI_RUN SET STATUS=?,PROMPT_TOKENS=?,COMPLETION_TOKENS=?,ESTIMATED_USAGE=?,COST_SNAPSHOT=?,COMPLETED_AT=?,ERROR_CODE=?,VERSION=? WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND VERSION=?",r.status().name(),r.promptTokens(),r.completionTokens(),r.estimatedUsage(),r.costSnapshot(),r.completedAt()==null?null:Timestamp.from(r.completedAt()),r.errorCode(),r.version(),r.id(),r.tenantId().value(),r.ownerUserId().value(),expected); }
    @Override
    @Transactional
    public AiRun updateTerminalAndAppend(AiRun run, long expectedVersion, AiRunEventType eventType,
                                         String payload, boolean redacted) {
        if (update(run, expectedVersion) != 1) throw new IllegalStateException("run was modified");
        long seq = appendNextEvent(run.id(), run.tenantId(), run.ownerUserId().value(), eventType,
                payload == null ? "{}" : payload, redacted, Instant.now());
        return run.withLastEventSeq(seq);
    }
    @Override
    @Transactional
    public long appendNextEvent(String runId, TenantId tenantId, long ownerUserId, AiRunEventType type,
                                String payload, boolean redacted, Instant createdAt) {
        return appendNextEvent(runId, tenantId, ownerUserId, type, payload, redacted, createdAt, null, null, null);
    }

    @Override
    @Transactional
    public long appendNextEvent(String runId, TenantId tenantId, long ownerUserId, AiRunEventType type,
                                String payload, boolean redacted, Instant createdAt,
                                String turnId, String stepId, String providerRequestId) {
        // Lock the run row before allocating a sequence.  Increment-then-read
        // is racy under H2's default connection pool: another writer can
        // advance LAST_EVENT_SEQ between the two statements and both writers
        // would attempt to persist the same sequence.
        Long current = jdbc.query("SELECT LAST_EVENT_SEQ FROM AI_RUN WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? FOR UPDATE",
                rs -> rs.next() ? rs.getLong(1) : null, runId, tenant(tenantId), ownerUserId);
        if (current == null) throw new IllegalArgumentException("run not found");
        List<AiRunEvent> terminalEvents = jdbc.query("SELECT * FROM AI_RUN_EVENT WHERE RUN_ID=? AND TENANT_ID=? AND TYPE IN ('RUN_COMPLETED','RUN_FAILED','RUN_CANCELLED') ORDER BY SEQ LIMIT 1",
                this::mapEvent, runId, tenant(tenantId));
        if (!terminalEvents.isEmpty()) {
                AiRunEvent existing = terminalEvents.get(0);
                if (!terminal(type)) throw new IllegalStateException("run is already terminal");
                if (existing.type() != type || !java.util.Objects.equals(existing.payload(), payload) || existing.redacted() != redacted) {
                    throw new IllegalStateException("run already has a different terminal event");
                }
                return existing.seq();
        }
        long seq = current + 1;
        int updated = jdbc.update("UPDATE AI_RUN SET LAST_EVENT_SEQ=? WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND LAST_EVENT_SEQ=?",
                seq, runId, tenant(tenantId), ownerUserId, current);
        if (updated != 1) throw new IllegalStateException("run event sequence was modified");
        try {
            jdbc.update("INSERT INTO AI_RUN_EVENT (RUN_ID,TENANT_ID,SEQ,TYPE,SCHEMA_VERSION,TURN_ID,STEP_ID,PARENT_EVENT_SEQ,CONVERSATION_ID,GENERATION_ID,EXECUTION_ID,APP_ID,APP_VERSION_ID,PROVIDER_REQUEST_ID,TRACE_ID,PAYLOAD,REDACTED,CREATED_AT) " +
                            "SELECT ?,TENANT_ID,?, ?,1,?,?,CASE WHEN ? > 1 THEN ? - 1 ELSE NULL END,CONVERSATION_ID,GENERATION_ID,EXECUTION_ID,APP_ID,APP_VERSION_ID,?,TRACE_ID,?,?,? FROM AI_RUN WHERE ID=? AND TENANT_ID=?",
                    runId, seq, type.name(), turnId, stepId, seq, seq, providerRequestId, payload == null ? "{}" : payload, redacted,
                    Timestamp.from(createdAt == null ? Instant.now() : createdAt), runId, tenant(tenantId));
        } catch (org.springframework.dao.DuplicateKeyException duplicate) {
            AiRunEvent existing = jdbc.queryForObject("SELECT * FROM AI_RUN_EVENT WHERE RUN_ID=? AND TENANT_ID=? AND SEQ=?",
                    this::mapEvent, runId, tenant(tenantId), seq);
            if (existing.type() != type || !java.util.Objects.equals(existing.payload(), payload) || existing.redacted() != redacted)
                throw new IllegalStateException("event sequence already contains a different payload", duplicate);
        }
        return seq;
    }

    private boolean terminal(AiRunEventType type) {
        return type == AiRunEventType.RUN_COMPLETED || type == AiRunEventType.RUN_FAILED || type == AiRunEventType.RUN_CANCELLED;
    }
    @Override
    @Transactional
    public long appendEvent(AiRunEvent e) {
        // Explicit sequence writes are used by import/replay tooling and tests,
        // but must still participate in the same row lock as live appends. A
        // MAX(SEQ) check alone allows two concurrent importers to observe the
        // same value and violate AI_RUN.LAST_EVENT_SEQ.
        Long runLast = jdbc.query("SELECT LAST_EVENT_SEQ FROM AI_RUN WHERE ID=? AND TENANT_ID=? FOR UPDATE",
                rs -> rs.next() ? rs.getLong(1) : null, e.runId(), e.tenantId().value());
        Long max = jdbc.queryForObject("SELECT COALESCE(MAX(SEQ),0) FROM AI_RUN_EVENT WHERE RUN_ID=? AND TENANT_ID=?", Long.class, e.runId(), e.tenantId().value());
        List<AiRunEvent> existing = jdbc.query("SELECT * FROM AI_RUN_EVENT WHERE RUN_ID=? AND TENANT_ID=? AND SEQ=?",
                this::mapEvent, e.runId(), e.tenantId().value(), e.seq());
        if (!existing.isEmpty()) {
            AiRunEvent old = existing.get(0);
            if (old.type() != e.type() || !java.util.Objects.equals(old.payload(), e.payload()) || old.redacted() != e.redacted()) {
                throw new IllegalStateException("event sequence already contains a different payload");
            }
            return e.seq();
        }
        if (max != null && e.seq() != max + 1) throw new IllegalStateException("run event sequence must be contiguous");
        if (runLast != null && e.seq() != runLast + 1) throw new IllegalStateException("run event sequence must follow AI_RUN.LAST_EVENT_SEQ");
        Integer terminalCount = jdbc.queryForObject("SELECT COUNT(*) FROM AI_RUN_EVENT WHERE RUN_ID=? AND TENANT_ID=? AND TYPE IN ('RUN_COMPLETED','RUN_FAILED','RUN_CANCELLED')", Integer.class, e.runId(), e.tenantId().value());
        if (terminalCount != null && terminalCount > 0) throw new IllegalStateException("run is already terminal");
        jdbc.update("INSERT INTO AI_RUN_EVENT (RUN_ID,TENANT_ID,SEQ,TYPE,SCHEMA_VERSION,TURN_ID,STEP_ID,PARENT_EVENT_SEQ,CONVERSATION_ID,GENERATION_ID,EXECUTION_ID,APP_ID,APP_VERSION_ID,PROVIDER_REQUEST_ID,TRACE_ID,PAYLOAD,REDACTED,CREATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                e.runId(), e.tenantId().value(), e.seq(), e.type().name(), e.schemaVersion(), e.turnId(), e.stepId(), e.parentEventSeq(),
                e.conversationId(), e.generationId(), e.executionId(), e.appId(), e.appVersionId(), e.providerRequestId(), e.traceId(),
                e.payload(), e.redacted(), Timestamp.from(e.createdAt()));
        if (runLast != null) {
            if (jdbc.update("UPDATE AI_RUN SET LAST_EVENT_SEQ=? WHERE ID=? AND TENANT_ID=? AND LAST_EVENT_SEQ=?",
                    e.seq(), e.runId(), e.tenantId().value(), runLast) != 1) {
                throw new IllegalStateException("run event sequence was modified");
            }
        }
        return e.seq();
    }
    @Override public List<AiRunEvent> events(String id,TenantId tenant,long owner,long after,int limit) { find(id,tenant,owner).orElseThrow(() -> new IllegalArgumentException("run not found")); return jdbc.query("SELECT * FROM AI_RUN_EVENT WHERE RUN_ID=? AND TENANT_ID=? AND SEQ>? ORDER BY SEQ LIMIT ?",this::mapEvent,id,tenant.value(),after,Math.max(1,Math.min(limit,100_000))); }
    private AiRunEvent mapEvent(java.sql.ResultSet rs, int n) throws java.sql.SQLException {
        return new AiRunEvent(rs.getString("RUN_ID"), new TenantId(rs.getLong("TENANT_ID")), rs.getLong("SEQ"), AiRunEventType.valueOf(rs.getString("TYPE")),
                rs.getInt("SCHEMA_VERSION"), rs.getString("TURN_ID"), rs.getString("STEP_ID"), (Long) rs.getObject("PARENT_EVENT_SEQ"),
                rs.getString("CONVERSATION_ID"), rs.getString("GENERATION_ID"), rs.getString("EXECUTION_ID"), rs.getString("APP_ID"),
                rs.getString("APP_VERSION_ID"), rs.getString("PROVIDER_REQUEST_ID"), rs.getString("TRACE_ID"), rs.getString("PAYLOAD"),
                rs.getBoolean("REDACTED"), rs.getTimestamp("CREATED_AT").toInstant());
    }
    private AiRun mapRun(java.sql.ResultSet rs) throws java.sql.SQLException { return new AiRun(rs.getString("ID"),new TenantId(rs.getLong("TENANT_ID")),new UserId(rs.getLong("OWNER_USER_ID")),rs.getString("APP_ID"),rs.getString("APP_VERSION_ID"),AiRunSource.valueOf(rs.getString("SOURCE_TYPE")),rs.getString("SOURCE_ID"),rs.getString("PARENT_RUN_ID"),rs.getString("TRACE_ID"),rs.getString("CONVERSATION_ID"),rs.getString("GENERATION_ID"),rs.getString("EXECUTION_ID"),rs.getString("MODEL"),rs.getString("PROMPT_HASH"),AiRunStatus.valueOf(rs.getString("STATUS")),rs.getLong("PROMPT_TOKENS"),rs.getLong("COMPLETION_TOKENS"),rs.getBoolean("ESTIMATED_USAGE"),rs.getString("COST_SNAPSHOT"),rs.getTimestamp("CREATED_AT").toInstant(),rs.getTimestamp("COMPLETED_AT")==null?null:rs.getTimestamp("COMPLETED_AT").toInstant(),rs.getString("ERROR_CODE"),rs.getLong("VERSION"),rs.getLong("LAST_EVENT_SEQ")); }
    private static long tenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId.value();
    }
}

