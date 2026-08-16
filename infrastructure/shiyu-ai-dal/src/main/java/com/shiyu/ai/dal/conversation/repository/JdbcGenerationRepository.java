package com.shiyu.ai.dal.conversation.repository;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.conversation.domain.GenerationEvent;
import com.shiyu.ai.conversation.domain.GenerationEventType;
import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.conversation.domain.GenerationStatus;
import com.shiyu.ai.conversation.port.GenerationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class JdbcGenerationRepository implements GenerationRepository {
    private final JdbcTemplate jdbc;

    public JdbcGenerationRepository(@Qualifier("agentDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public void insert(GenerationRun g) {
        Long tenant = jdbc.queryForObject("SELECT TENANT_ID FROM CHAT_CONVERSATION WHERE ID=?", Long.class, g.conversationId());
        if (tenant == null) throw new IllegalArgumentException("conversation not found");
        jdbc.update("INSERT INTO CHAT_GENERATION_RUN (ID,TENANT_ID,CONVERSATION_ID,INPUT_MESSAGE_ID,ASSISTANT_MESSAGE_ID,SPEAKER_ID,PLATFORM,MODEL,STATUS,PROMPT_TOKENS,COMPLETION_TOKENS,LATENCY_MS,ERROR_CODE,LAST_EVENT_SEQUENCE,CANCEL_REQUESTED,VERSION,CREATED_AT,UPDATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                g.id(), tenant, g.conversationId(), g.inputMessageId(), g.assistantMessageId(), g.speakerId(), g.platform(), g.model(), g.status().name(), g.promptTokens(), g.completionTokens(), g.latencyMs(), g.errorCode(), g.lastEventSequence(), g.cancelRequested(), g.version(), ts(g.createdAt()), ts(g.updatedAt()));
    }

    @Override
    public Optional<GenerationRun> find(String id, long tenantId, long ownerUserId) {
        return jdbc.query("SELECT g.* FROM CHAT_GENERATION_RUN g JOIN CHAT_CONVERSATION c ON c.ID=g.CONVERSATION_ID WHERE g.ID=? AND g.TENANT_ID=? AND c.OWNER_USER_ID=?", this::map, id, tenantId, ownerUserId).stream().findFirst();
    }
    @Override public boolean hasRunning(String conversationId, String inputMessageId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM CHAT_GENERATION_RUN WHERE CONVERSATION_ID=? AND INPUT_MESSAGE_ID=? AND STATUS='RUNNING'", Integer.class, conversationId, inputMessageId);
        return count != null && count > 0;
    }
    @Override public boolean hasRunningConversation(String conversationId, long tenantId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM CHAT_GENERATION_RUN WHERE CONVERSATION_ID=? AND TENANT_ID=? AND STATUS='RUNNING'", Integer.class, conversationId, tenantId);
        return count != null && count > 0;
    }
    @Override public List<GenerationRun> listConversation(String conversationId, long tenantId, int limit) {
        return jdbc.query("SELECT g.* FROM CHAT_GENERATION_RUN g WHERE g.CONVERSATION_ID=? AND g.TENANT_ID=? ORDER BY g.CREATED_AT LIMIT ?", this::map, conversationId, tenantId, Math.min(Math.max(limit, 1), 1000));
    }

    @Override
    public int update(GenerationRun g, long expectedVersion) {
        return jdbc.update("UPDATE CHAT_GENERATION_RUN SET STATUS=?,PROMPT_TOKENS=?,COMPLETION_TOKENS=?,LATENCY_MS=?,ERROR_CODE=?,LAST_EVENT_SEQUENCE=?,CANCEL_REQUESTED=?,VERSION=?,UPDATED_AT=? WHERE ID=? AND VERSION=?",
                g.status().name(), g.promptTokens(), g.completionTokens(), g.latencyMs(), g.errorCode(), g.lastEventSequence(), g.cancelRequested(), g.version(), ts(g.updatedAt()), g.id(), expectedVersion);
    }

    @Override
    public synchronized void appendEvent(GenerationEvent e, long tenantId) {
        // Allocate the sequence at the persistence boundary. Callers may have a
        // local hint, but concurrent producers (runner/cancel/recovery) must not
        // be able to reuse it.
        int sequence = nextEventSequence(e.generationRunId());
        jdbc.update("INSERT INTO CHAT_GENERATION_EVENT (GENERATION_RUN_ID,TENANT_ID,SEQ,TYPE,PAYLOAD,CREATED_AT) VALUES (?,?,?,?,?,?)",
                e.generationRunId(), tenantId, sequence, e.type().name(), e.payload(), ts(e.createdAt()));
        jdbc.update("UPDATE CHAT_GENERATION_RUN SET LAST_EVENT_SEQUENCE=? WHERE ID=?", sequence, e.generationRunId());
    }

    @Override
    public List<GenerationEvent> listEvents(String generationId, int afterSequence, int limit) {
        return jdbc.query("SELECT * FROM CHAT_GENERATION_EVENT WHERE GENERATION_RUN_ID=? AND SEQ>? ORDER BY SEQ LIMIT ?",
                (r, n) -> new GenerationEvent(generationId, r.getInt("SEQ"), GenerationEventType.valueOf(r.getString("TYPE")), r.getString("PAYLOAD"), r.getTimestamp("CREATED_AT").toInstant()),
                generationId, afterSequence, Math.min(Math.max(limit, 1), 1000));
    }
    public int nextEventSequence(String generationId) {
        Integer max = jdbc.queryForObject("SELECT COALESCE(MAX(SEQ),-1)+1 FROM CHAT_GENERATION_EVENT WHERE GENERATION_RUN_ID=?", Integer.class, generationId);
        return max == null ? 0 : max;
    }

    private GenerationRun map(ResultSet r, int n) throws java.sql.SQLException {
        return new GenerationRun(r.getString("ID"), r.getString("CONVERSATION_ID"), r.getString("INPUT_MESSAGE_ID"), r.getString("ASSISTANT_MESSAGE_ID"), r.getString("SPEAKER_ID"), r.getString("PLATFORM"), r.getString("MODEL"), GenerationStatus.valueOf(r.getString("STATUS")), r.getLong("PROMPT_TOKENS"), r.getLong("COMPLETION_TOKENS"), r.getLong("LATENCY_MS"), r.getString("ERROR_CODE"), r.getInt("LAST_EVENT_SEQUENCE"), r.getBoolean("CANCEL_REQUESTED"), r.getLong("VERSION"), r.getTimestamp("CREATED_AT").toInstant(), r.getTimestamp("UPDATED_AT").toInstant());
    }

    private static Timestamp ts(Instant i) { return Timestamp.from(i == null ? Instant.now() : i); }
}
