package com.shiyu.ai.conversation.implementation.infrastructure.persistence.repository;

import com.shiyu.ai.conversation.implementation.domain.chat.model.LorebookEntry;

import com.shiyu.ai.conversation.implementation.domain.chat.model.PromptTemplateVersion;

import com.shiyu.ai.conversation.implementation.domain.chat.model.CharacterCardV2;

import com.shiyu.ai.conversation.implementation.domain.chat.model.Persona;

import com.shiyu.ai.conversation.implementation.domain.chat.model.GroupChatAsset;

import com.shiyu.ai.conversation.implementation.domain.chat.model.GroupChat;

import com.shiyu.ai.conversation.implementation.domain.chat.model.LorebookAsset;

import com.shiyu.ai.conversation.implementation.domain.chat.model.SpeakerPolicy;

import com.shiyu.ai.conversation.implementation.domain.chat.model.CharacterAsset;

import com.shiyu.ai.conversation.implementation.domain.chat.model.PersonaAsset;

import com.shiyu.ai.common.foundation.jdbc.JdbcDialect;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.conversation.implementation.domain.port.ChatProductRepository;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * 负责 Jdbc 对话 Product 的持久化查询、保存和删除，并维护数据访问边界。
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
     * 执行 Jdbc 对话 Product 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dataSource 用于完成本次业务处理的 dataSource 参数。
     */
    public JdbcChatProductRepository(@Qualifier("agentDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.dialect = JdbcDialect.detect(jdbc);
    }

    /**
     * 创建或保存 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param asset 用于完成本次业务处理的 asset 参数。
     * @return 返回 Jdbc 对话 Product 相关操作生成的结果数据。
     */
    @Override
    public CharacterAsset saveCharacter(CharacterAsset asset) {
        tenant(new TenantId(asset.tenantId()));
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
     * 查询 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 查询 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param requester 用于完成本次业务处理的 requester 参数。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 查询 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 删除或移除 Jdbc 对话 Product 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param id 用于定位目标业务对象的标识。
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
     * 创建或保存 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param asset 用于完成本次业务处理的 asset 参数。
     * @return 返回 Jdbc 对话 Product 相关操作生成的结果数据。
     */
    @Override
    public PersonaAsset savePersona(PersonaAsset asset) {
        tenant(new TenantId(asset.tenantId()));
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
     * 查询 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 查询 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 删除或移除 Jdbc 对话 Product 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param id 用于定位目标业务对象的标识。
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
     * 创建或保存 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param asset 用于完成本次业务处理的 asset 参数。
     * @return 返回 Jdbc 对话 Product 相关操作生成的结果数据。
     */
    @Override
    public LorebookAsset saveLorebook(LorebookAsset asset) {
        tenant(new TenantId(asset.tenantId()));
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
     * 查询 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 查询 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 删除或移除 Jdbc 对话 Product 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param id 用于定位目标业务对象的标识。
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
     * 创建或保存 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param version 用于完成本次业务处理的 version 参数。
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @return 返回 Jdbc 对话 Product 相关操作生成的结果数据。
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
     * 查询 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param templateId 用于定位template的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 创建或保存 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param asset 用于完成本次业务处理的 asset 参数。
     * @return 返回 Jdbc 对话 Product 相关操作生成的结果数据。
     */
    @Override
    public GroupChatAsset saveGroup(GroupChatAsset asset) {
        tenant(new TenantId(asset.tenantId()));
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
     * 查询 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
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
     * 查询 Jdbc 对话 Product 相关业务数据，并返回处理结果。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 删除或移除 Jdbc 对话 Product 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenant 当前操作涉及的租户标识。
     * @param owner 用于完成本次业务处理的 owner 参数。
     * @param id 用于定位目标业务对象的标识。
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
        TenantId value = java.util.Objects.requireNonNull(tenant, "tenantId must not be null");
        TenantScope.requireMatches(value);
        return value.value();
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
