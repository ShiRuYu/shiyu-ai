package com.shiyu.ai.conversation.implementation.infrastructure.persistence.repository;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.conversation.implementation.domain.model.*;
import com.shiyu.ai.conversation.implementation.domain.port.ConversationRepository;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * 负责 Jdbc 会话 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class JdbcConversationRepository implements ConversationRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * 执行 Jdbc 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dataSource 用于完成本次业务处理的 dataSource 参数。
     */
    public JdbcConversationRepository(@Qualifier("agentDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * 创建或保存 Jdbc 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param c 用于完成本次业务处理的 c 参数。
     */
    @Override
    public void insertConversation(Conversation c) {
        requireTenant(c.tenantId());
        jdbc.update(
                "INSERT INTO CHAT_CONVERSATION"
                    + " (ID,TENANT_ID,OWNER_USER_ID,SCENE_TYPE,TITLE,STATUS,PARENT_CONVERSATION_ID,BRANCH_FROM_MESSAGE_ID,ACTIVE_LEAF_MESSAGE_ID,ROLLING_SUMMARY,PLATFORM,MODEL,VERSION,CREATED_AT,UPDATED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                c.id(),
                c.tenantId(),
                c.ownerUserId(),
                c.sceneType(),
                c.title(),
                c.status().name(),
                c.parentConversationId(),
                c.branchFromMessageId(),
                c.activeLeafMessageId(),
                c.rollingSummary(),
                c.platform(),
                c.model(),
                c.version(),
                ts(c.createdAt()),
                ts(c.updatedAt()));
    }

    /**
     * 查询 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<Conversation> findConversation(String id, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc
                .query(
                        "SELECT * FROM CHAT_CONVERSATION WHERE ID=? AND TENANT_ID=? AND"
                                + " OWNER_USER_ID=? AND STATUS<>'DELETED'",
                        this::mapConversation,
                        id,
                        tenantId.value(),
                        ownerUserId)
                .stream()
                .findFirst();
    }

    /**
     * 查询 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param limit 每页返回的数据数量。
     * @param offset 用于完成本次业务处理的 offset 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Conversation> listConversations(
            TenantId tenantId, long ownerUserId, int limit, int offset) {
        TenantScope.requireMatches(tenantId);
        return jdbc.query(
                "SELECT * FROM CHAT_CONVERSATION WHERE TENANT_ID=? AND OWNER_USER_ID=? AND"
                        + " STATUS<>'DELETED' ORDER BY UPDATED_AT DESC,ID ASC LIMIT ? OFFSET ?",
                this::mapConversation,
                tenantId.value(),
                ownerUserId,
                Math.min(Math.max(limit, 1), 100),
                Math.max(offset, 0));
    }

    /**
     * 查询 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param parentConversationId 用于定位parent 会话的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Conversation> listBranches(
            String parentConversationId, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc.query(
                "SELECT * FROM CHAT_CONVERSATION WHERE PARENT_CONVERSATION_ID=? AND TENANT_ID=? AND"
                        + " OWNER_USER_ID=? AND STATUS<>'DELETED' ORDER BY UPDATED_AT DESC,ID ASC",
                this::mapConversation,
                parentConversationId,
                tenantId.value(),
                ownerUserId);
    }

    /**
     * 更新或设置 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param c 用于完成本次业务处理的 c 参数。
     * @param expectedVersion 用于完成本次业务处理的 expectedVersion 参数。
     * @return 返回 Jdbc 会话 相关操作生成的结果数据。
     */
    @Override
    public int updateConversation(Conversation c, long expectedVersion) {
        requireTenant(c.tenantId());
        return jdbc.update(
                "UPDATE CHAT_CONVERSATION SET"
                    + " TITLE=?,STATUS=?,ACTIVE_LEAF_MESSAGE_ID=?,ROLLING_SUMMARY=?,VERSION=?,UPDATED_AT=?"
                    + " WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND VERSION=?",
                c.title(),
                c.status().name(),
                c.activeLeafMessageId(),
                c.rollingSummary(),
                c.version(),
                ts(c.updatedAt()),
                c.id(),
                c.tenantId(),
                c.ownerUserId(),
                expectedVersion);
    }

    /**
     * 创建或保存 Jdbc 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param m 用于完成本次业务处理的 m 参数。
     */
    @Override
    public void insertMessage(ConversationMessage m) {
        Long tenant =
                jdbc.queryForObject(
                        "SELECT TENANT_ID FROM CHAT_CONVERSATION WHERE ID=?",
                        Long.class,
                        m.conversationId());
        if (tenant == null) throw new IllegalArgumentException("conversation not found");
        requireTenant(tenant);
        jdbc.update(
                "INSERT INTO CHAT_MESSAGE"
                    + " (ID,TENANT_ID,CONVERSATION_ID,PARENT_MESSAGE_ID,SOURCE_MESSAGE_ID,ROLE,CONTENT,CONTENT_PARTS,TOOL_CALL,STATUS,SEQUENCE,GENERATION_ID,CREATED_AT,UPDATED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                m.id(),
                tenant,
                m.conversationId(),
                m.parentMessageId(),
                m.sourceMessageId(),
                m.role().name(),
                m.textContent(),
                JSONUtils.toJsonString(m.contentParts()),
                JSONUtils.toJsonString(m.toolCall()),
                m.status().name(),
                m.sequence(),
                m.generationId(),
                ts(m.createdAt()),
                ts(m.updatedAt()));
    }

    /**
     * 查询 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<ConversationMessage> findMessage(
            String id, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc
                .query(
                        "SELECT m.* FROM CHAT_MESSAGE m JOIN CHAT_CONVERSATION c ON"
                                + " c.ID=m.CONVERSATION_ID WHERE m.ID=? AND m.TENANT_ID=? AND"
                                + " c.OWNER_USER_ID=?",
                        this::mapMessage,
                        id,
                        tenantId.value(),
                        ownerUserId)
                .stream()
                .findFirst();
    }

    /**
     * 查询 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param conversationId 用于定位conversation的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ConversationMessage> listMessages(
            String conversationId, TenantId tenantId, long ownerUserId, int limit) {
        TenantScope.requireMatches(tenantId);
        return jdbc.query(
                "SELECT m.* FROM CHAT_MESSAGE m JOIN CHAT_CONVERSATION c ON c.ID=m.CONVERSATION_ID"
                    + " WHERE m.CONVERSATION_ID=? AND m.TENANT_ID=? AND c.OWNER_USER_ID=? ORDER BY"
                    + " m.SEQUENCE DESC,m.ID ASC LIMIT ?",
                this::mapMessage,
                conversationId,
                tenantId.value(),
                ownerUserId,
                Math.min(Math.max(limit, 1), 1000));
    }

    /**
     * 删除或移除 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 Jdbc 会话 相关操作生成的结果数据。
     */
    @Override
    public int deleteConversation(String id, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc.update(
                "UPDATE CHAT_CONVERSATION SET"
                    + " STATUS='DELETED',VERSION=VERSION+1,UPDATED_AT=CURRENT_TIMESTAMP WHERE ID=?"
                    + " AND TENANT_ID=? AND OWNER_USER_ID=? AND STATUS<>'DELETED'",
                id,
                tenantId.value(),
                ownerUserId);
    }

    /**
     * 删除或移除 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 Jdbc 会话 相关操作生成的结果数据。
     */
    @Override
    public int deleteMessage(String id, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc.update(
                "DELETE FROM CHAT_MESSAGE WHERE ID=? AND TENANT_ID=? AND CONVERSATION_ID IN (SELECT"
                        + " ID FROM CHAT_CONVERSATION WHERE TENANT_ID=? AND OWNER_USER_ID=?)",
                id,
                tenantId.value(),
                tenantId.value(),
                ownerUserId);
    }

    private Conversation mapConversation(ResultSet r, int n) throws java.sql.SQLException {
        return new Conversation(
                r.getString("ID"),
                r.getLong("TENANT_ID"),
                r.getLong("OWNER_USER_ID"),
                r.getString("SCENE_TYPE"),
                r.getString("TITLE"),
                ConversationStatus.valueOf(r.getString("STATUS")),
                r.getString("PARENT_CONVERSATION_ID"),
                r.getString("BRANCH_FROM_MESSAGE_ID"),
                r.getString("ACTIVE_LEAF_MESSAGE_ID"),
                r.getString("ROLLING_SUMMARY"),
                r.getString("PLATFORM"),
                r.getString("MODEL"),
                r.getLong("VERSION"),
                instant(r.getTimestamp("CREATED_AT")),
                instant(r.getTimestamp("UPDATED_AT")));
    }

    private ConversationMessage mapMessage(ResultSet r, int n) throws java.sql.SQLException {
        List<ContentPart> parts =
                java.util.Optional.ofNullable(r.getString("CONTENT_PARTS"))
                        .map(json -> JSONUtils.parseArray(json, ContentPart.class))
                        .orElse(List.of(ContentPart.text(r.getString("CONTENT"))));
        java.util.Map<String, Object> toolCall =
                java.util.Optional.ofNullable(r.getString("TOOL_CALL"))
                        .map(JSONUtils::parseMap)
                        .orElse(java.util.Map.of());
        return new ConversationMessage(
                r.getString("ID"),
                r.getString("CONVERSATION_ID"),
                r.getString("PARENT_MESSAGE_ID"),
                r.getString("SOURCE_MESSAGE_ID"),
                MessageRole.valueOf(r.getString("ROLE")),
                parts,
                toolCall,
                MessageStatus.valueOf(r.getString("STATUS")),
                r.getInt("SEQUENCE"),
                r.getString("GENERATION_ID"),
                instant(r.getTimestamp("CREATED_AT")),
                instant(r.getTimestamp("UPDATED_AT")));
    }

    private static Timestamp ts(Instant i) {
        return Timestamp.from(i == null ? Instant.now() : i);
    }

    private static Instant instant(Timestamp t) {
        return t == null ? null : t.toInstant();
    }

    private static void requireTenant(long tenantId) {
        TenantScope.requireMatches(new TenantId(tenantId));
    }
}
