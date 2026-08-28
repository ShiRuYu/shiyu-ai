package com.shiyu.ai.conversation.implementation.persistence.repository;

import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.common.core.utils.JSONUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class JdbcConversationRepository implements ConversationRepository {
    private final JdbcTemplate jdbc;

    public JdbcConversationRepository(@Qualifier("agentDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public void insertConversation(Conversation c) {
        jdbc.update("INSERT INTO CHAT_CONVERSATION (ID,TENANT_ID,OWNER_USER_ID,SCENE_TYPE,TITLE,STATUS,PARENT_CONVERSATION_ID,BRANCH_FROM_MESSAGE_ID,ACTIVE_LEAF_MESSAGE_ID,ROLLING_SUMMARY,PLATFORM,MODEL,VERSION,CREATED_AT,UPDATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                c.id(), c.tenantId(), c.ownerUserId(), c.sceneType(), c.title(), c.status().name(), c.parentConversationId(), c.branchFromMessageId(), c.activeLeafMessageId(), c.rollingSummary(), c.platform(), c.model(), c.version(), ts(c.createdAt()), ts(c.updatedAt()));
    }

    @Override
    public Optional<Conversation> findConversation(String id, TenantId tenantId, long ownerUserId) {
        return jdbc.query("SELECT * FROM CHAT_CONVERSATION WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND STATUS<>'DELETED'", this::mapConversation, id, tenantId.value(), ownerUserId).stream().findFirst();
    }

    @Override
    public List<Conversation> listConversations(TenantId tenantId, long ownerUserId, int limit, int offset) {
        return jdbc.query("SELECT * FROM CHAT_CONVERSATION WHERE TENANT_ID=? AND OWNER_USER_ID=? AND STATUS<>'DELETED' ORDER BY UPDATED_AT DESC LIMIT ? OFFSET ?", this::mapConversation, tenantId.value(), ownerUserId, Math.min(Math.max(limit, 1), 100), Math.max(offset, 0));
    }

    @Override public List<Conversation> listBranches(String parentConversationId, TenantId tenantId, long ownerUserId) {
        return jdbc.query("SELECT * FROM CHAT_CONVERSATION WHERE PARENT_CONVERSATION_ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND STATUS<>'DELETED' ORDER BY UPDATED_AT DESC", this::mapConversation, parentConversationId, tenantId.value(), ownerUserId);
    }

    @Override
    public int updateConversation(Conversation c, long expectedVersion) {
        return jdbc.update("UPDATE CHAT_CONVERSATION SET TITLE=?,STATUS=?,ACTIVE_LEAF_MESSAGE_ID=?,ROLLING_SUMMARY=?,VERSION=?,UPDATED_AT=? WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND VERSION=?",
                c.title(), c.status().name(), c.activeLeafMessageId(), c.rollingSummary(), c.version(), ts(c.updatedAt()), c.id(), c.tenantId(), c.ownerUserId(), expectedVersion);
    }

    @Override
    public void insertMessage(ConversationMessage m) {
        Long tenant = jdbc.queryForObject("SELECT TENANT_ID FROM CHAT_CONVERSATION WHERE ID=?", Long.class, m.conversationId());
        if (tenant == null) throw new IllegalArgumentException("conversation not found");
        jdbc.update("INSERT INTO CHAT_MESSAGE (ID,TENANT_ID,CONVERSATION_ID,PARENT_MESSAGE_ID,SOURCE_MESSAGE_ID,ROLE,CONTENT,CONTENT_PARTS,TOOL_CALL,STATUS,SEQUENCE,GENERATION_ID,CREATED_AT,UPDATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                m.id(), tenant, m.conversationId(), m.parentMessageId(), m.sourceMessageId(), m.role().name(), m.textContent(), JSONUtils.toJsonString(m.contentParts()), JSONUtils.toJsonString(m.toolCall()), m.status().name(), m.sequence(), m.generationId(), ts(m.createdAt()), ts(m.updatedAt()));
    }

    @Override
    public Optional<ConversationMessage> findMessage(String id, TenantId tenantId, long ownerUserId) {
        return jdbc.query("SELECT m.* FROM CHAT_MESSAGE m JOIN CHAT_CONVERSATION c ON c.ID=m.CONVERSATION_ID WHERE m.ID=? AND m.TENANT_ID=? AND c.OWNER_USER_ID=?", this::mapMessage, id, tenantId.value(), ownerUserId).stream().findFirst();
    }

    @Override
    public List<ConversationMessage> listMessages(String conversationId, TenantId tenantId, long ownerUserId, int limit) {
        return jdbc.query("SELECT m.* FROM CHAT_MESSAGE m JOIN CHAT_CONVERSATION c ON c.ID=m.CONVERSATION_ID WHERE m.CONVERSATION_ID=? AND m.TENANT_ID=? AND c.OWNER_USER_ID=? ORDER BY m.SEQUENCE DESC LIMIT ?", this::mapMessage, conversationId, tenantId.value(), ownerUserId, Math.min(Math.max(limit, 1), 1000));
    }
    @Override public int deleteConversation(String id, TenantId tenantId, long ownerUserId) {
        return jdbc.update("UPDATE CHAT_CONVERSATION SET STATUS='DELETED',VERSION=VERSION+1,UPDATED_AT=CURRENT_TIMESTAMP WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND STATUS<>'DELETED'", id, tenantId.value(), ownerUserId);
    }
    @Override public int deleteMessage(String id, TenantId tenantId, long ownerUserId) {
        return jdbc.update("DELETE FROM CHAT_MESSAGE WHERE ID=? AND TENANT_ID=? AND CONVERSATION_ID IN (SELECT ID FROM CHAT_CONVERSATION WHERE TENANT_ID=? AND OWNER_USER_ID=?)",
                id, tenantId.value(), tenantId.value(), ownerUserId);
    }

    private Conversation mapConversation(ResultSet r, int n) throws java.sql.SQLException {
        return new Conversation(r.getString("ID"), r.getLong("TENANT_ID"), r.getLong("OWNER_USER_ID"), r.getString("SCENE_TYPE"), r.getString("TITLE"), ConversationStatus.valueOf(r.getString("STATUS")), r.getString("PARENT_CONVERSATION_ID"), r.getString("BRANCH_FROM_MESSAGE_ID"), r.getString("ACTIVE_LEAF_MESSAGE_ID"), r.getString("ROLLING_SUMMARY"), r.getString("PLATFORM"), r.getString("MODEL"), r.getLong("VERSION"), instant(r.getTimestamp("CREATED_AT")), instant(r.getTimestamp("UPDATED_AT")));
    }

    private ConversationMessage mapMessage(ResultSet r, int n) throws java.sql.SQLException {
        List<ContentPart> parts = java.util.Optional.ofNullable(r.getString("CONTENT_PARTS"))
                .map(json -> JSONUtils.parseArray(json, ContentPart.class)).orElse(List.of(ContentPart.text(r.getString("CONTENT"))));
        java.util.Map<String, Object> toolCall = java.util.Optional.ofNullable(r.getString("TOOL_CALL"))
                .map(JSONUtils::parseMap).orElse(java.util.Map.of());
        return new ConversationMessage(r.getString("ID"), r.getString("CONVERSATION_ID"), r.getString("PARENT_MESSAGE_ID"), r.getString("SOURCE_MESSAGE_ID"), MessageRole.valueOf(r.getString("ROLE")), parts, toolCall, MessageStatus.valueOf(r.getString("STATUS")), r.getInt("SEQUENCE"), r.getString("GENERATION_ID"), instant(r.getTimestamp("CREATED_AT")), instant(r.getTimestamp("UPDATED_AT")));
    }

    private static Timestamp ts(Instant i) { return Timestamp.from(i == null ? Instant.now() : i); }
    private static Instant instant(Timestamp t) { return t == null ? null : t.toInstant(); }
}

