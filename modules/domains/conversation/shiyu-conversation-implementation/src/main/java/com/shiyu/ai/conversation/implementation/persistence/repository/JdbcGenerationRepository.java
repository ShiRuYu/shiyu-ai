package com.shiyu.ai.conversation.implementation.persistence.repository;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.conversation.domain.GenerationEvent;
import com.shiyu.ai.conversation.domain.GenerationEventType;
import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.conversation.domain.GenerationStatus;
import com.shiyu.ai.conversation.port.GenerationRepository;
import com.shiyu.ai.runtime.AiRunEventType;
import com.shiyu.ai.runtime.AiRunRepository;
import com.shiyu.ai.runtime.AiRun;
import com.shiyu.ai.runtime.AiRunStatus;
import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
public class JdbcGenerationRepository implements GenerationRepository {
    private final JdbcTemplate jdbc;
    private final AiRunRepository runtimeRuns;
    private final long recoveryTimeoutMs;

    public JdbcGenerationRepository(@Qualifier("agentDataSource") DataSource dataSource, AiRunRepository runtimeRuns) {
        this(dataSource, runtimeRuns, 300_000L);
    }

    @Autowired
    public JdbcGenerationRepository(@Qualifier("agentDataSource") DataSource dataSource, AiRunRepository runtimeRuns,
                                    @Value("${shiyu.conversation.recovery-timeout-ms:300000}") long recoveryTimeoutMs) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.runtimeRuns = runtimeRuns;
        this.recoveryTimeoutMs = Math.max(1000L, recoveryTimeoutMs);
    }

    @Override
    @Transactional
    public void insert(GenerationRun g) {
        Long tenant = jdbc.queryForObject("SELECT TENANT_ID FROM CHAT_CONVERSATION WHERE ID=?", Long.class, g.conversationId());
        if (tenant == null) throw new IllegalArgumentException("conversation not found");
        try {
            jdbc.update("INSERT INTO CHAT_GENERATION_ACTIVE (TENANT_ID,CONVERSATION_ID,INPUT_MESSAGE_ID,GENERATION_ID,CREATED_AT) VALUES (?,?,?,?,?)",
                    tenant, g.conversationId(), g.inputMessageId(), g.id(), ts(g.createdAt()));
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            throw new IllegalStateException("a generation is already running for this message", ex);
        }
        jdbc.update("INSERT INTO CHAT_GENERATION_RUN (ID,TENANT_ID,CONVERSATION_ID,INPUT_MESSAGE_ID,ASSISTANT_MESSAGE_ID,RUNTIME_RUN_ID,SPEAKER_ID,PLATFORM,MODEL,STATUS,PROMPT_TOKENS,COMPLETION_TOKENS,LATENCY_MS,ERROR_CODE,LAST_EVENT_SEQUENCE,CANCEL_REQUESTED,VERSION,CREATED_AT,UPDATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                g.id(), tenant, g.conversationId(), g.inputMessageId(), g.assistantMessageId(), g.runtimeRunId(), g.speakerId(), g.platform(), g.model(), g.status().name(), g.promptTokens(), g.completionTokens(), g.latencyMs(), g.errorCode(), g.lastEventSequence(), g.cancelRequested(), g.version(), ts(g.createdAt()), ts(g.updatedAt()));
    }

    @Override
    public Optional<GenerationRun> find(String id, TenantId tenantId, long ownerUserId) {
        return jdbc.query("SELECT g.* FROM CHAT_GENERATION_RUN g JOIN CHAT_CONVERSATION c ON c.ID=g.CONVERSATION_ID WHERE g.ID=? AND g.TENANT_ID=? AND c.OWNER_USER_ID=?", this::map, id, tenantId.value(), ownerUserId).stream().findFirst();
    }
    @Override public boolean hasRunning(String conversationId, String inputMessageId, TenantId tenantId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM CHAT_GENERATION_ACTIVE WHERE CONVERSATION_ID=? AND INPUT_MESSAGE_ID=? AND TENANT_ID=?", Integer.class, conversationId, inputMessageId, tenantId.value());
        return count != null && count > 0;
    }
    @Override public boolean hasRunningConversation(String conversationId, TenantId tenantId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM CHAT_GENERATION_ACTIVE WHERE CONVERSATION_ID=? AND TENANT_ID=?", Integer.class, conversationId, tenantId.value());
        return count != null && count > 0;
    }
    @Override public List<GenerationRun> listConversation(String conversationId, TenantId tenantId, int limit) {
        return jdbc.query("SELECT g.* FROM CHAT_GENERATION_RUN g WHERE g.CONVERSATION_ID=? AND g.TENANT_ID=? ORDER BY g.CREATED_AT LIMIT ?", this::map, conversationId, tenantId.value(), Math.min(Math.max(limit, 1), 1000));
    }

    /**
     * Reclaims generation reservations left by a crashed process.  A running
     * provider is only considered abandoned after the configurable grace
     * period, so an ordinary slow stream is never cancelled by this task.
     */
    @Scheduled(fixedDelayString = "${shiyu.conversation.recovery-delay-ms:30000}")
    @Transactional
    public void recoverStaleGenerations() {
        recoverStaleGenerations(recoveryTimeoutMs);
    }

    @Transactional
    public int recoverStaleGenerations(long timeoutMs) {
        long grace = Math.max(1000L, timeoutMs);
        Timestamp cutoff = Timestamp.from(Instant.now().minusMillis(grace));
        List<StaleGeneration> stale = jdbc.query(
                "SELECT g.*,c.OWNER_USER_ID FROM CHAT_GENERATION_RUN g JOIN CHAT_CONVERSATION c ON c.ID=g.CONVERSATION_ID "
                        + "WHERE g.STATUS IN ('CREATED','RUNNING') AND g.UPDATED_AT<? ORDER BY g.UPDATED_AT LIMIT 100",
                (rs, rowNum) -> new StaleGeneration(map(rs, rowNum), rs.getLong("OWNER_USER_ID")), cutoff);
        int recovered = 0;
        for (StaleGeneration abandoned : stale) {
            GenerationRun current = abandoned.run();
            GenerationRun failed = current.transition(GenerationStatus.FAILED);
            failed = new GenerationRun(failed.id(), failed.conversationId(), failed.inputMessageId(), failed.assistantMessageId(),
                    failed.speakerId(), failed.platform(), failed.model(), failed.status(), failed.promptTokens(), failed.completionTokens(),
                    Duration.between(current.createdAt(), Instant.now()).toMillis(), "SERVICE_RESTART", failed.lastEventSequence(),
                    false, failed.version(), failed.createdAt(), Instant.now(), failed.runtimeRunId());
            if (update(failed, current.version()) != 1) continue;
            recovered++;
            if (failed.runtimeRunId() != null && !failed.runtimeRunId().isBlank()) {
                try {
                    AiRun run = runtimeRuns.find(failed.runtimeRunId(), new TenantId(currentTenant(current)), abandoned.ownerUserId()).orElse(null);
                    if (run != null && run.status() != AiRunStatus.COMPLETED && run.status() != AiRunStatus.FAILED && run.status() != AiRunStatus.CANCELLED) {
                        AiRun failedRun = new AiRun(run.id(), run.tenantId(), run.ownerUserId(), run.appId(), run.appVersionId(),
                                run.sourceType(), run.sourceId(), run.parentRunId(), run.traceId(), run.conversationId(),
                                run.generationId(), run.executionId(), run.model(), run.promptHash(), AiRunStatus.FAILED,
                                run.promptTokens(), run.completionTokens(), run.estimatedUsage(), run.costSnapshot(), run.createdAt(),
                                Instant.now(), "SERVICE_RESTART", run.version() + 1, run.lastEventSeq());
                        runtimeRuns.updateTerminalAndAppend(failedRun, run.version(), AiRunEventType.RUN_FAILED,
                                "{\"errorCode\":\"SERVICE_RESTART\"}", true);
                    }
                } catch (RuntimeException ignored) {
                    // Generation state and its admission reservation are the
                    // local recovery boundary; a missing runtime projection is
                    // safe to repair on the next runtime reconciliation pass.
                }
            }
        }
        return recovered;
    }

    @Override
    public int update(GenerationRun g, long expectedVersion) {
        int updated = jdbc.update("UPDATE CHAT_GENERATION_RUN SET STATUS=?,PROMPT_TOKENS=?,COMPLETION_TOKENS=?,LATENCY_MS=?,ERROR_CODE=?,LAST_EVENT_SEQUENCE=?,CANCEL_REQUESTED=?,VERSION=?,UPDATED_AT=?,RUNTIME_RUN_ID=COALESCE(?,RUNTIME_RUN_ID) WHERE ID=? AND VERSION=?",
                g.status().name(), g.promptTokens(), g.completionTokens(), g.latencyMs(), g.errorCode(), g.lastEventSequence(), g.cancelRequested(), g.version(), ts(g.updatedAt()), g.runtimeRunId(), g.id(), expectedVersion);
        if (updated == 1 && (g.status() == GenerationStatus.COMPLETED || g.status() == GenerationStatus.FAILED || g.status() == GenerationStatus.CANCELLED)) {
            jdbc.update("DELETE FROM CHAT_GENERATION_ACTIVE WHERE GENERATION_ID=?", g.id());
        }
        return updated;
    }

    @Override
    public void appendEvent(GenerationEvent e, TenantId tenantId) {
        GenerationRuntimeLink link = runtimeLink(e.generationRunId(), tenantId);
        runtimeRuns.appendNextEvent(link.runtimeRunId(), tenantId, link.ownerUserId(), runtimeType(e.type()),
                e.payload(), true, e.createdAt());
    }

    @Override
    public List<GenerationEvent> listEvents(String generationId, TenantId tenantId, int afterSequence, int limit) {
        return jdbc.query("SELECT e.SEQ,e.TYPE,e.PAYLOAD,e.CREATED_AT FROM AI_RUN_EVENT e JOIN CHAT_GENERATION_RUN g ON g.RUNTIME_RUN_ID=e.RUN_ID WHERE e.GENERATION_ID=? AND e.TENANT_ID=? AND g.TENANT_ID=? AND e.SEQ>? ORDER BY e.SEQ LIMIT ?",
                (r, n) -> new GenerationEvent(generationId, r.getInt("SEQ"), generationType(r.getString("TYPE")), r.getString("PAYLOAD"), r.getTimestamp("CREATED_AT").toInstant()),
                generationId, tenantId.value(), tenantId.value(), afterSequence, Math.min(Math.max(limit, 1), 1000));
    }
    public int nextEventSequence(String generationId, TenantId tenantId) {
        Integer max = jdbc.queryForObject("SELECT COALESCE(MAX(e.SEQ),-1)+1 FROM AI_RUN_EVENT e JOIN CHAT_GENERATION_RUN g ON g.RUNTIME_RUN_ID=e.RUN_ID WHERE e.GENERATION_ID=? AND e.TENANT_ID=? AND g.TENANT_ID=?", Integer.class, generationId, tenantId.value(), tenantId.value());
        return max == null ? 0 : max;
    }

    private GenerationRuntimeLink runtimeLink(String generationId, TenantId tenantId) {
        GenerationRuntimeLink link = jdbc.query("SELECT g.RUNTIME_RUN_ID,c.OWNER_USER_ID FROM CHAT_GENERATION_RUN g JOIN CHAT_CONVERSATION c ON c.ID=g.CONVERSATION_ID WHERE g.ID=? AND g.TENANT_ID=?",
                rs -> rs.next() ? new GenerationRuntimeLink(rs.getString("RUNTIME_RUN_ID"), rs.getLong("OWNER_USER_ID")) : null,
                generationId, tenantId.value());
        if (link == null) throw new IllegalArgumentException("generation not found");
        return link;
    }

    private AiRunEventType runtimeType(GenerationEventType type) {
        return switch (type) {
            case STARTED -> AiRunEventType.RUN_STARTED;
            case BLOCK_STARTED -> AiRunEventType.MODEL_BLOCK_STARTED;
            case DELTA -> AiRunEventType.MODEL_DELTA;
            case REASONING_DELTA -> AiRunEventType.MODEL_REASONING_DELTA;
            case TOOL_CALL -> AiRunEventType.MODEL_TOOL_CALL_DELTA;
            case BLOCK_COMPLETED -> AiRunEventType.MODEL_BLOCK_COMPLETED;
            case USAGE -> AiRunEventType.MODEL_USAGE;
            case COMPLETED -> AiRunEventType.RUN_COMPLETED;
            case FAILED -> AiRunEventType.RUN_FAILED;
            case CANCELLED -> AiRunEventType.RUN_CANCELLED;
        };
    }

    private GenerationEventType generationType(String type) {
        return switch (AiRunEventType.valueOf(type)) {
            case RUN_STARTED -> GenerationEventType.STARTED;
            case MODEL_BLOCK_STARTED -> GenerationEventType.BLOCK_STARTED;
            case MODEL_DELTA -> GenerationEventType.DELTA;
            case MODEL_REASONING_DELTA -> GenerationEventType.REASONING_DELTA;
            case MODEL_TOOL_CALL_DELTA -> GenerationEventType.TOOL_CALL;
            case MODEL_BLOCK_COMPLETED, MODEL_COMPLETED -> GenerationEventType.BLOCK_COMPLETED;
            case MODEL_USAGE -> GenerationEventType.USAGE;
            case RUN_COMPLETED -> GenerationEventType.COMPLETED;
            case RUN_FAILED -> GenerationEventType.FAILED;
            case RUN_CANCELLED -> GenerationEventType.CANCELLED;
            default -> GenerationEventType.DELTA;
        };
    }

    private record GenerationRuntimeLink(String runtimeRunId, long ownerUserId) {
        private GenerationRuntimeLink {
            if (runtimeRunId == null || runtimeRunId.isBlank()) throw new IllegalStateException("generation runtime run is not linked");
        }
    }

    private GenerationRun map(ResultSet r, int n) throws java.sql.SQLException {
        return new GenerationRun(r.getString("ID"), r.getString("CONVERSATION_ID"), r.getString("INPUT_MESSAGE_ID"), r.getString("ASSISTANT_MESSAGE_ID"), r.getString("SPEAKER_ID"), r.getString("PLATFORM"), r.getString("MODEL"), GenerationStatus.valueOf(r.getString("STATUS")), r.getLong("PROMPT_TOKENS"), r.getLong("COMPLETION_TOKENS"), r.getLong("LATENCY_MS"), r.getString("ERROR_CODE"), r.getInt("LAST_EVENT_SEQUENCE"), r.getBoolean("CANCEL_REQUESTED"), r.getLong("VERSION"), r.getTimestamp("CREATED_AT").toInstant(), r.getTimestamp("UPDATED_AT").toInstant(), r.getString("RUNTIME_RUN_ID"));
    }

    private long currentTenant(GenerationRun run) {
        Long tenant = jdbc.queryForObject("SELECT TENANT_ID FROM CHAT_GENERATION_RUN WHERE ID=?", Long.class, run.id());
        if (tenant == null) throw new IllegalStateException("generation tenant not found");
        return tenant;
    }

    private record StaleGeneration(GenerationRun run, long ownerUserId) { }

    private static Timestamp ts(Instant i) { return Timestamp.from(i == null ? Instant.now() : i); }
}

