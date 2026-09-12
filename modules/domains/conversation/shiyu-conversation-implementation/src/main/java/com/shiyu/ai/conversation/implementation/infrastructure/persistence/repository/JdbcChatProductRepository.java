package com.shiyu.ai.conversation.implementation.infrastructure.persistence.repository;

import com.shiyu.ai.common.core.jdbc.JdbcDialect;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.conversation.implementation.domain.chat.*;
import com.shiyu.ai.conversation.implementation.domain.port.ChatProductRepository;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * {@code JdbcChatProductRepository} 定义会话模块的持久化端口，隔离领域逻辑与具体存储实现。
 */
@Component
public class JdbcChatProductRepository implements ChatProductRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;
    /**
     * dialect 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final JdbcDialect dialect;

    /**
     * {@code JdbcChatProductRepository} 创建并初始化当前类型实例。
     *
     * @param dataSource 参数值，用于执行当前操作。
     */
    public JdbcChatProductRepository(@Qualifier("agentDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.dialect = JdbcDialect.detect(jdbc);
    }

    /**
     * {@code saveCharacter} 写入或更新当前模块中的业务数据。
     *
     * @param asset 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public CharacterAsset saveCharacter(CharacterAsset asset) {
        jdbc.update(
                dialect.upsert(
                        "CHAT_CHARACTER_ASSET",
                        List.of(
                                "ID",
                                "TENANT_ID",
                                "OWNER_USER_ID",
                                "CARD_JSON",
                                "VISIBILITY",
                                "PNG_DATA",
                                "CREATED_AT",
                                "UPDATED_AT"),
                        "?, ?, ?, ?, ?, ?, ?, ?",
                        List.of("ID"),
                        List.of(
                                "TENANT_ID",
                                "OWNER_USER_ID",
                                "CARD_JSON",
                                "VISIBILITY",
                                "PNG_DATA",
                                "CREATED_AT",
                                "UPDATED_AT")),
                asset.id(),
                asset.tenantId(),
                asset.ownerUserId(),
                JSONUtils.toJsonString(asset.card()),
                asset.visibility(),
                asset.pngData(),
                ts(asset.createdAt()),
                ts(asset.updatedAt()));
        return asset;
    }

    /**
     * {@code findCharacter} 查询并返回当前操作所需的数据。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<CharacterAsset> findCharacter(TenantId tenant, long owner, String id) {
        long value = tenant(tenant);
        return jdbc
                .query(
                        "SELECT * FROM CHAT_CHARACTER_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=?"
                                + " AND ID=?",
                        (r, n) -> character(r, value, owner),
                        value,
                        owner,
                        id)
                .stream()
                .findFirst();
    }

    /**
     * {@code findCharacterForAccess} 查询并返回当前操作所需的数据。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param requester 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<CharacterAsset> findCharacterForAccess(
            TenantId tenant, long requester, String id) {
        long value = tenant(tenant);
        return jdbc
                .query(
                        "SELECT * FROM CHAT_CHARACTER_ASSET WHERE TENANT_ID=? AND ID=? AND"
                                + " (OWNER_USER_ID=? OR VISIBILITY IN ('PUBLIC','TENANT'))",
                        (r, n) -> character(r, value, r.getLong("OWNER_USER_ID")),
                        value,
                        id,
                        requester)
                .stream()
                .findFirst();
    }

    /**
     * {@code listCharacters} 查询并返回当前操作所需的数据。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<CharacterAsset> listCharacters(TenantId tenant, long owner) {
        long value = tenant(tenant);
        return jdbc.query(
                "SELECT * FROM CHAT_CHARACTER_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY"
                        + " UPDATED_AT DESC,ID ASC",
                (r, n) -> character(r, value, owner),
                value,
                owner);
    }

    /**
     * {@code deleteCharacter} 释放或移除当前操作涉及的资源。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void deleteCharacter(TenantId tenant, long owner, String id) {
        jdbc.update(
                "DELETE FROM CHAT_CHARACTER_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?",
                tenant(tenant),
                owner,
                id);
    }

    /**
     * {@code savePersona} 写入或更新当前模块中的业务数据。
     *
     * @param asset 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PersonaAsset savePersona(PersonaAsset asset) {
        jdbc.update(
                dialect.upsert(
                        "CHAT_PERSONA_ASSET",
                        List.of(
                                "ID",
                                "TENANT_ID",
                                "OWNER_USER_ID",
                                "PERSONA_JSON",
                                "CREATED_AT",
                                "UPDATED_AT"),
                        "?, ?, ?, ?, ?, ?",
                        List.of("ID"),
                        List.of(
                                "TENANT_ID",
                                "OWNER_USER_ID",
                                "PERSONA_JSON",
                                "CREATED_AT",
                                "UPDATED_AT")),
                asset.id(),
                asset.tenantId(),
                asset.ownerUserId(),
                JSONUtils.toJsonString(asset.persona()),
                ts(asset.createdAt()),
                ts(asset.updatedAt()));
        return asset;
    }

    /**
     * {@code findPersona} 查询并返回当前操作所需的数据。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<PersonaAsset> findPersona(TenantId tenant, long owner, String id) {
        long value = tenant(tenant);
        return jdbc
                .query(
                        "SELECT * FROM CHAT_PERSONA_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? AND"
                                + " ID=?",
                        (r, n) ->
                                new PersonaAsset(
                                        r.getString("ID"),
                                        value,
                                        owner,
                                        JSONUtils.parseObject(
                                                r.getString("PERSONA_JSON"), Persona.class),
                                        instant(r.getTimestamp("CREATED_AT")),
                                        instant(r.getTimestamp("UPDATED_AT"))),
                        value,
                        owner,
                        id)
                .stream()
                .findFirst();
    }

    /**
     * {@code listPersonas} 查询并返回当前操作所需的数据。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<PersonaAsset> listPersonas(TenantId tenant, long owner) {
        long value = tenant(tenant);
        return jdbc.query(
                "SELECT * FROM CHAT_PERSONA_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY"
                        + " UPDATED_AT DESC,ID ASC",
                (r, n) ->
                        new PersonaAsset(
                                r.getString("ID"),
                                value,
                                owner,
                                JSONUtils.parseObject(r.getString("PERSONA_JSON"), Persona.class),
                                instant(r.getTimestamp("CREATED_AT")),
                                instant(r.getTimestamp("UPDATED_AT"))),
                value,
                owner);
    }

    /**
     * {@code deletePersona} 释放或移除当前操作涉及的资源。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void deletePersona(TenantId tenant, long owner, String id) {
        jdbc.update(
                "DELETE FROM CHAT_PERSONA_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?",
                tenant(tenant),
                owner,
                id);
    }

    /**
     * {@code saveLorebook} 写入或更新当前模块中的业务数据。
     *
     * @param asset 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public LorebookAsset saveLorebook(LorebookAsset asset) {
        jdbc.update(
                dialect.upsert(
                        "CHAT_LOREBOOK_ASSET",
                        List.of(
                                "ID",
                                "TENANT_ID",
                                "OWNER_USER_ID",
                                "ENTRY_JSON",
                                "CREATED_AT",
                                "UPDATED_AT"),
                        "?, ?, ?, ?, ?, ?",
                        List.of("ID"),
                        List.of(
                                "TENANT_ID",
                                "OWNER_USER_ID",
                                "ENTRY_JSON",
                                "CREATED_AT",
                                "UPDATED_AT")),
                asset.id(),
                asset.tenantId(),
                asset.ownerUserId(),
                JSONUtils.toJsonString(asset.entry()),
                ts(asset.createdAt()),
                ts(asset.updatedAt()));
        return asset;
    }

    /**
     * {@code findLorebook} 查询并返回当前操作所需的数据。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<LorebookAsset> findLorebook(TenantId tenant, long owner, String id) {
        long value = tenant(tenant);
        return jdbc
                .query(
                        "SELECT * FROM CHAT_LOREBOOK_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=?"
                                + " AND ID=?",
                        (r, n) ->
                                new LorebookAsset(
                                        r.getString("ID"),
                                        value,
                                        owner,
                                        JSONUtils.parseObject(
                                                r.getString("ENTRY_JSON"), LorebookEntry.class),
                                        instant(r.getTimestamp("CREATED_AT")),
                                        instant(r.getTimestamp("UPDATED_AT"))),
                        value,
                        owner,
                        id)
                .stream()
                .findFirst();
    }

    /**
     * {@code listLorebooks} 查询并返回当前操作所需的数据。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<LorebookAsset> listLorebooks(TenantId tenant, long owner) {
        long value = tenant(tenant);
        return jdbc.query(
                "SELECT * FROM CHAT_LOREBOOK_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY"
                        + " UPDATED_AT DESC,ID ASC",
                (r, n) ->
                        new LorebookAsset(
                                r.getString("ID"),
                                value,
                                owner,
                                JSONUtils.parseObject(
                                        r.getString("ENTRY_JSON"), LorebookEntry.class),
                                instant(r.getTimestamp("CREATED_AT")),
                                instant(r.getTimestamp("UPDATED_AT"))),
                value,
                owner);
    }

    /**
     * {@code deleteLorebook} 释放或移除当前操作涉及的资源。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void deleteLorebook(TenantId tenant, long owner, String id) {
        jdbc.update(
                "DELETE FROM CHAT_LOREBOOK_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?",
                tenant(tenant),
                owner,
                id);
    }

    /**
     * {@code savePrompt} 写入或更新当前模块中的业务数据。
     *
     * @param version 参数值，用于执行当前操作。
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PromptTemplateVersion savePrompt(
            PromptTemplateVersion version, TenantId tenant, long owner) {
        long value = tenant(tenant);
        jdbc.update(
                "INSERT INTO CHAT_PROMPT_TEMPLATE"
                    + " (ID,TENANT_ID,OWNER_USER_ID,TEMPLATE_ID,VERSION,STATUS,BODY,VARIABLE_SCHEMA,TEST_CASES,CREATED_AT,PUBLISHED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                version.id(),
                value,
                owner,
                version.templateId(),
                version.version(),
                version.status(),
                version.body(),
                JSONUtils.toJsonString(version.variableSchema()),
                JSONUtils.toJsonString(version.testCases()),
                ts(version.createdAt()),
                version.publishedAt() == null ? null : ts(version.publishedAt()));
        return version;
    }

    /**
     * {@code listPrompts} 查询并返回当前操作所需的数据。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param templateId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<PromptTemplateVersion> listPrompts(TenantId tenant, long owner, String templateId) {
        long value = tenant(tenant);
        return jdbc.query(
                "SELECT * FROM CHAT_PROMPT_TEMPLATE WHERE TENANT_ID=? AND OWNER_USER_ID=? AND (? IS"
                        + " NULL OR TEMPLATE_ID=?) ORDER BY TEMPLATE_ID, VERSION DESC, ID ASC",
                (r, n) ->
                        new PromptTemplateVersion(
                                r.getString("ID"),
                                r.getString("TEMPLATE_ID"),
                                r.getInt("VERSION"),
                                r.getString("STATUS"),
                                r.getString("BODY"),
                                JSONUtils.parseObject(
                                        r.getString("VARIABLE_SCHEMA"),
                                        new tools.jackson.core.type.TypeReference<
                                                java.util.Map<String, String>>() {}),
                                JSONUtils.parseObject(
                                        r.getString("TEST_CASES"),
                                        new tools.jackson.core.type.TypeReference<
                                                java.util.List<String>>() {}),
                                instant(r.getTimestamp("CREATED_AT")),
                                instant(r.getTimestamp("PUBLISHED_AT"))),
                value,
                owner,
                templateId,
                templateId);
    }

    /**
     * {@code saveGroup} 写入或更新当前模块中的业务数据。
     *
     * @param asset 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public GroupChatAsset saveGroup(GroupChatAsset asset) {
        jdbc.update(
                dialect.upsert(
                        "CHAT_GROUP_CHAT",
                        List.of(
                                "ID",
                                "TENANT_ID",
                                "OWNER_USER_ID",
                                "NAME",
                                "PARTICIPANTS_JSON",
                                "SPEAKER_POLICY",
                                "MAX_TURNS",
                                "TOKEN_BUDGET",
                                "CREATED_AT",
                                "UPDATED_AT"),
                        "?, ?, ?, ?, ?, ?, ?, ?, ?, ?",
                        List.of("ID"),
                        List.of(
                                "TENANT_ID",
                                "OWNER_USER_ID",
                                "NAME",
                                "PARTICIPANTS_JSON",
                                "SPEAKER_POLICY",
                                "MAX_TURNS",
                                "TOKEN_BUDGET",
                                "CREATED_AT",
                                "UPDATED_AT")),
                asset.id(),
                asset.tenantId(),
                asset.ownerUserId(),
                asset.group().name(),
                JSONUtils.toJsonString(asset.group().participants()),
                asset.group().speakerPolicy().name(),
                asset.group().maxTurns(),
                asset.group().tokenBudget(),
                ts(asset.createdAt()),
                ts(asset.updatedAt()));
        return asset;
    }

    /**
     * {@code findGroup} 查询并返回当前操作所需的数据。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<GroupChatAsset> findGroup(TenantId tenant, long owner, String id) {
        long value = tenant(tenant);
        return jdbc
                .query(
                        "SELECT * FROM CHAT_GROUP_CHAT WHERE TENANT_ID=? AND OWNER_USER_ID=? AND"
                                + " ID=?",
                        (r, n) -> group(r, value, owner),
                        value,
                        owner,
                        id)
                .stream()
                .findFirst();
    }

    /**
     * {@code listGroups} 查询并返回当前操作所需的数据。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<GroupChatAsset> listGroups(TenantId tenant, long owner) {
        long value = tenant(tenant);
        return jdbc.query(
                "SELECT * FROM CHAT_GROUP_CHAT WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY"
                        + " UPDATED_AT DESC,ID ASC",
                (r, n) -> group(r, value, owner),
                value,
                owner);
    }

    /**
     * {@code deleteGroup} 释放或移除当前操作涉及的资源。
     *
     * @param tenant 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void deleteGroup(TenantId tenant, long owner, String id) {
        jdbc.update(
                "DELETE FROM CHAT_GROUP_CHAT WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?",
                tenant(tenant),
                owner,
                id);
    }

    @SuppressWarnings("unchecked")
    private GroupChatAsset group(java.sql.ResultSet r, long tenant, long owner)
            throws java.sql.SQLException {
        List<GroupChat.Participant> participants =
                ((List<?>) JSONUtils.parseObject(r.getString("PARTICIPANTS_JSON"), List.class))
                        .stream()
                                .map(
                                        value ->
                                                JSONUtils.parseObject(
                                                        JSONUtils.toJsonString(value),
                                                        GroupChat.Participant.class))
                                .toList();
        GroupChat chat =
                new GroupChat(
                        r.getString("ID"),
                        r.getString("NAME"),
                        participants,
                        SpeakerPolicy.valueOf(r.getString("SPEAKER_POLICY")),
                        r.getInt("MAX_TURNS"),
                        r.getInt("TOKEN_BUDGET"));
        return new GroupChatAsset(
                chat.id(),
                tenant,
                owner,
                chat,
                instant(r.getTimestamp("CREATED_AT")),
                instant(r.getTimestamp("UPDATED_AT")));
    }

    private static Timestamp ts(Instant i) {
        return Timestamp.from(i == null ? Instant.now() : i);
    }

    private static Instant instant(Timestamp t) {
        return t == null ? null : t.toInstant();
    }

    private static long tenant(TenantId tenant) {
        return java.util.Objects.requireNonNull(tenant, "tenantId must not be null").value();
    }

    private static CharacterAsset character(java.sql.ResultSet r, long tenant, long owner)
            throws java.sql.SQLException {
        return new CharacterAsset(
                r.getString("ID"),
                tenant,
                owner,
                JSONUtils.parseObject(r.getString("CARD_JSON"), CharacterCardV2.class),
                r.getString("VISIBILITY"),
                r.getBytes("PNG_DATA"),
                instant(r.getTimestamp("CREATED_AT")),
                instant(r.getTimestamp("UPDATED_AT")));
    }
}
