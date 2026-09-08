package com.shiyu.ai.conversation.implementation.persistence.repository;

import com.shiyu.ai.conversation.chat.*;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcChatProductRepositoryTest {
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void persistsAndReadsAllChatProductAssetsWithTenantFilters() throws Exception {
        JdbcChatProductRepository repository = new JdbcChatProductRepository(mock(DataSource.class));
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        Field field = JdbcChatProductRepository.class.getDeclaredField("jdbc");
        field.setAccessible(true); field.set(repository, jdbc);
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        CharacterCardV2 card = new CharacterCardV2(null, "Ava", "desc", "scene", "hello", List.of(), "system", Map.of(), 2);
        CharacterAsset character = new CharacterAsset("ch1", 7, 8, card, "PRIVATE", now, now);
        PersonaAsset persona = new PersonaAsset("p1", 7, 8, new Persona("p1", 8, "Ava", "student", "warm", "PRIVATE", Map.of()), now, now);
        LorebookAsset lore = new LorebookAsset("l1", 7, 8, new LorebookEntry("l1", List.of("magic"), "context", 1, "before", 50, true), now, now);
        PromptTemplateVersion prompt = new PromptTemplateVersion("pr1", "welcome", 1, "DRAFT", "hello", Map.of("name", "string"), List.of("sample"), now, null);
        GroupChat group = new GroupChat("g1", "Study", List.of(new GroupChat.Participant("ava", "Ava", "ch1")), SpeakerPolicy.MANUAL, 3, 100);
        GroupChatAsset groupAsset = new GroupChatAsset("g1", 7, 8, group, now, now);

        assertSame(character, repository.saveCharacter(character));
        assertSame(persona, repository.savePersona(persona));
        assertSame(lore, repository.saveLorebook(lore));
        assertSame(prompt, repository.savePrompt(prompt, new TenantId(7), 8));
        assertSame(groupAsset, repository.saveGroup(groupAsset));
        repository.deleteCharacter(new TenantId(7), 8, "ch1"); repository.deletePersona(new TenantId(7), 8, "p1");
        repository.deleteLorebook(new TenantId(7), 8, "l1"); repository.deleteGroup(new TenantId(7), 8, "g1");
        verify(jdbc, times(9)).update(anyString(), any(Object[].class));

        ResultSet row = mock(ResultSet.class);
        when(row.getString("ID")).thenReturn("ch1", "p1", "l1", "pr1", "g1");
        when(row.getLong("OWNER_USER_ID")).thenReturn(8L);
        when(row.getString("CARD_JSON")).thenReturn(CharacterCardCodec.toJson(card));
        when(row.getString("VISIBILITY")).thenReturn("PRIVATE");
        when(row.getBytes("PNG_DATA")).thenReturn(null);
        when(row.getString("PERSONA_JSON")).thenReturn(com.shiyu.ai.common.core.utils.JSONUtils.toJsonString(persona.persona()));
        when(row.getString("ENTRY_JSON")).thenReturn(com.shiyu.ai.common.core.utils.JSONUtils.toJsonString(lore.entry()));
        when(row.getString("TEMPLATE_ID")).thenReturn("welcome");
        when(row.getInt("VERSION")).thenReturn(1);
        when(row.getString("STATUS")).thenReturn("DRAFT");
        when(row.getString("BODY")).thenReturn("hello");
        when(row.getString("VARIABLE_SCHEMA")).thenReturn("{\"name\":\"string\"}");
        when(row.getString("TEST_CASES")).thenReturn("[\"sample\"]");
        when(row.getString("NAME")).thenReturn("Study");
        when(row.getString("PARTICIPANTS_JSON")).thenReturn("[{\"id\":\"ava\",\"displayName\":\"Ava\",\"characterId\":\"ch1\"}]");
        when(row.getString("SPEAKER_POLICY")).thenReturn("MANUAL");
        when(row.getInt("MAX_TURNS")).thenReturn(3);
        when(row.getInt("TOKEN_BUDGET")).thenReturn(100);
        when(row.getTimestamp("CREATED_AT")).thenReturn(Timestamp.from(now));
        when(row.getTimestamp("UPDATED_AT")).thenReturn(Timestamp.from(now));
        when(row.getTimestamp("PUBLISHED_AT")).thenReturn(null);
        when(jdbc.query(anyString(), any(RowMapper.class), any(Object[].class))).thenAnswer(invocation -> {
            RowMapper mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(row, 0));
        });
        assertEquals("Ava", repository.findCharacter(new TenantId(7), 8, "ch1").orElseThrow().card().name());
        assertEquals(1, repository.listCharacters(new TenantId(7), 8).size());
        assertEquals("Ava", repository.findCharacterForAccess(new TenantId(7), 9, "ch1").orElseThrow().card().name());
        assertEquals("Ava", repository.findPersona(new TenantId(7), 8, "p1").orElseThrow().persona().name());
        assertEquals(1, repository.listPersonas(new TenantId(7), 8).size());
        assertEquals("context", repository.findLorebook(new TenantId(7), 8, "l1").orElseThrow().entry().content());
        assertEquals(1, repository.listLorebooks(new TenantId(7), 8).size());
        assertEquals("welcome", repository.listPrompts(new TenantId(7), 8, null).get(0).templateId());
        assertEquals("Study", repository.findGroup(new TenantId(7), 8, "g1").orElseThrow().group().name());
        assertEquals(1, repository.listGroups(new TenantId(7), 8).size());
    }
}
