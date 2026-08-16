package com.shiyu.ai.dal.chat.repository;

import com.shiyu.ai.chat.product.*;
import com.shiyu.ai.common.core.utils.JSONUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class JdbcChatProductRepository implements ChatProductRepository {
    private final JdbcTemplate jdbc;
    public JdbcChatProductRepository(@Qualifier("agentDataSource") DataSource dataSource) { this.jdbc = new JdbcTemplate(dataSource); }

    @Override public CharacterAsset saveCharacter(CharacterAsset asset) {
        jdbc.update("MERGE INTO CHAT_CHARACTER_ASSET (ID,TENANT_ID,OWNER_USER_ID,CARD_JSON,VISIBILITY,PNG_DATA,CREATED_AT,UPDATED_AT) KEY(ID) VALUES (?,?,?,?,?,?,?,?)",
                asset.id(), asset.tenantId(), asset.ownerUserId(), JSONUtils.toJsonString(asset.card()), asset.visibility(), asset.pngData(), ts(asset.createdAt()), ts(asset.updatedAt()));
        return asset;
    }
    @Override public Optional<CharacterAsset> findCharacter(long tenant, long owner, String id) {
        return jdbc.query("SELECT * FROM CHAT_CHARACTER_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?", (r,n) -> character(r, tenant, owner), tenant, owner, id).stream().findFirst();
    }
    @Override public Optional<CharacterAsset> findCharacterForAccess(long tenant, long requester, String id) {
        return jdbc.query("SELECT * FROM CHAT_CHARACTER_ASSET WHERE TENANT_ID=? AND ID=? AND (OWNER_USER_ID=? OR VISIBILITY IN ('PUBLIC','TENANT'))", (r,n) -> character(r, tenant, r.getLong("OWNER_USER_ID")), tenant, id, requester).stream().findFirst();
    }
    @Override public List<CharacterAsset> listCharacters(long tenant, long owner) {
        return jdbc.query("SELECT * FROM CHAT_CHARACTER_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY UPDATED_AT DESC", (r,n) -> character(r, tenant, owner), tenant, owner);
    }
    @Override public void deleteCharacter(long tenant, long owner, String id) { jdbc.update("DELETE FROM CHAT_CHARACTER_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?", tenant, owner, id); }

    @Override public PersonaAsset savePersona(PersonaAsset asset) {
        jdbc.update("MERGE INTO CHAT_PERSONA_ASSET (ID,TENANT_ID,OWNER_USER_ID,PERSONA_JSON,CREATED_AT,UPDATED_AT) KEY(ID) VALUES (?,?,?,?,?,?)", asset.id(), asset.tenantId(), asset.ownerUserId(), JSONUtils.toJsonString(asset.persona()), ts(asset.createdAt()), ts(asset.updatedAt())); return asset;
    }
    @Override public Optional<PersonaAsset> findPersona(long tenant, long owner, String id) { return jdbc.query("SELECT * FROM CHAT_PERSONA_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?", (r,n) -> new PersonaAsset(r.getString("ID"), tenant, owner, JSONUtils.parseObject(r.getString("PERSONA_JSON"), Persona.class), instant(r.getTimestamp("CREATED_AT")), instant(r.getTimestamp("UPDATED_AT"))), tenant, owner, id).stream().findFirst(); }
    @Override public List<PersonaAsset> listPersonas(long tenant, long owner) { return jdbc.query("SELECT * FROM CHAT_PERSONA_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY UPDATED_AT DESC", (r,n) -> new PersonaAsset(r.getString("ID"), tenant, owner, JSONUtils.parseObject(r.getString("PERSONA_JSON"), Persona.class), instant(r.getTimestamp("CREATED_AT")), instant(r.getTimestamp("UPDATED_AT"))), tenant, owner); }
    @Override public void deletePersona(long tenant, long owner, String id) { jdbc.update("DELETE FROM CHAT_PERSONA_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?", tenant, owner, id); }

    @Override public LorebookAsset saveLorebook(LorebookAsset asset) {
        jdbc.update("MERGE INTO CHAT_LOREBOOK_ASSET (ID,TENANT_ID,OWNER_USER_ID,ENTRY_JSON,CREATED_AT,UPDATED_AT) KEY(ID) VALUES (?,?,?,?,?,?)", asset.id(), asset.tenantId(), asset.ownerUserId(), JSONUtils.toJsonString(asset.entry()), ts(asset.createdAt()), ts(asset.updatedAt())); return asset;
    }
    @Override public Optional<LorebookAsset> findLorebook(long tenant, long owner, String id) { return jdbc.query("SELECT * FROM CHAT_LOREBOOK_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?", (r,n) -> new LorebookAsset(r.getString("ID"), tenant, owner, JSONUtils.parseObject(r.getString("ENTRY_JSON"), LorebookEntry.class), instant(r.getTimestamp("CREATED_AT")), instant(r.getTimestamp("UPDATED_AT"))), tenant, owner, id).stream().findFirst(); }
    @Override public List<LorebookAsset> listLorebooks(long tenant, long owner) { return jdbc.query("SELECT * FROM CHAT_LOREBOOK_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY UPDATED_AT DESC", (r,n) -> new LorebookAsset(r.getString("ID"), tenant, owner, JSONUtils.parseObject(r.getString("ENTRY_JSON"), LorebookEntry.class), instant(r.getTimestamp("CREATED_AT")), instant(r.getTimestamp("UPDATED_AT"))), tenant, owner); }
    @Override public void deleteLorebook(long tenant, long owner, String id) { jdbc.update("DELETE FROM CHAT_LOREBOOK_ASSET WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?", tenant, owner, id); }

    @Override public PromptTemplateVersion savePrompt(PromptTemplateVersion version, long tenant, long owner) {
        jdbc.update("INSERT INTO CHAT_PROMPT_TEMPLATE (ID,TENANT_ID,OWNER_USER_ID,TEMPLATE_ID,VERSION,STATUS,BODY,VARIABLE_SCHEMA,TEST_CASES,CREATED_AT,PUBLISHED_AT) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                version.id(), tenant, owner, version.templateId(), version.version(), version.status(), version.body(), JSONUtils.toJsonString(version.variableSchema()), JSONUtils.toJsonString(version.testCases()), ts(version.createdAt()), version.publishedAt() == null ? null : ts(version.publishedAt())); return version;
    }
    @Override public List<PromptTemplateVersion> listPrompts(long tenant, long owner, String templateId) {
        return jdbc.query("SELECT * FROM CHAT_PROMPT_TEMPLATE WHERE TENANT_ID=? AND OWNER_USER_ID=? AND (? IS NULL OR TEMPLATE_ID=?) ORDER BY TEMPLATE_ID, VERSION DESC", (r,n) -> new PromptTemplateVersion(r.getString("ID"), r.getString("TEMPLATE_ID"), r.getInt("VERSION"), r.getString("STATUS"), r.getString("BODY"), JSONUtils.parseObject(r.getString("VARIABLE_SCHEMA"), java.util.Map.class), JSONUtils.parseObject(r.getString("TEST_CASES"), List.class), instant(r.getTimestamp("CREATED_AT")), instant(r.getTimestamp("PUBLISHED_AT"))), tenant, owner, templateId, templateId);
    }
    @Override public GroupChatAsset saveGroup(GroupChatAsset asset) {
        jdbc.update("MERGE INTO CHAT_GROUP_CHAT (ID,TENANT_ID,OWNER_USER_ID,NAME,PARTICIPANTS_JSON,SPEAKER_POLICY,MAX_TURNS,TOKEN_BUDGET,CREATED_AT,UPDATED_AT) KEY(ID) VALUES (?,?,?,?,?,?,?,?,?,?)",
                asset.id(), asset.tenantId(), asset.ownerUserId(), asset.group().name(), JSONUtils.toJsonString(asset.group().participants()), asset.group().speakerPolicy().name(), asset.group().maxTurns(), asset.group().tokenBudget(), ts(asset.createdAt()), ts(asset.updatedAt()));
        return asset;
    }
    @Override public Optional<GroupChatAsset> findGroup(long tenant, long owner, String id) {
        return jdbc.query("SELECT * FROM CHAT_GROUP_CHAT WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?", (r,n) -> group(r, tenant, owner), tenant, owner, id).stream().findFirst();
    }
    @Override public List<GroupChatAsset> listGroups(long tenant, long owner) {
        return jdbc.query("SELECT * FROM CHAT_GROUP_CHAT WHERE TENANT_ID=? AND OWNER_USER_ID=? ORDER BY UPDATED_AT DESC", (r,n) -> group(r, tenant, owner), tenant, owner);
    }
    @Override public void deleteGroup(long tenant, long owner, String id) { jdbc.update("DELETE FROM CHAT_GROUP_CHAT WHERE TENANT_ID=? AND OWNER_USER_ID=? AND ID=?", tenant, owner, id); }

    @SuppressWarnings("unchecked")
    private GroupChatAsset group(java.sql.ResultSet r, long tenant, long owner) throws java.sql.SQLException {
        List<GroupChat.Participant> participants = ((List<?>) JSONUtils.parseObject(r.getString("PARTICIPANTS_JSON"), List.class)).stream()
                .map(value -> JSONUtils.parseObject(JSONUtils.toJsonString(value), GroupChat.Participant.class)).toList();
        GroupChat chat = new GroupChat(r.getString("ID"), r.getString("NAME"), participants, SpeakerPolicy.valueOf(r.getString("SPEAKER_POLICY")), r.getInt("MAX_TURNS"), r.getInt("TOKEN_BUDGET"));
        return new GroupChatAsset(chat.id(), tenant, owner, chat, instant(r.getTimestamp("CREATED_AT")), instant(r.getTimestamp("UPDATED_AT")));
    }
    private static Timestamp ts(Instant i) { return Timestamp.from(i == null ? Instant.now() : i); }
    private static Instant instant(Timestamp t) { return t == null ? null : t.toInstant(); }
    private static CharacterAsset character(java.sql.ResultSet r, long tenant, long owner) throws java.sql.SQLException {
        return new CharacterAsset(r.getString("ID"), tenant, owner, JSONUtils.parseObject(r.getString("CARD_JSON"), CharacterCardV2.class), r.getString("VISIBILITY"), r.getBytes("PNG_DATA"), instant(r.getTimestamp("CREATED_AT")), instant(r.getTimestamp("UPDATED_AT")));
    }
}
