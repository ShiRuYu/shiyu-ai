package com.shiyu.ai.conversation.chat;

import com.shiyu.ai.conversation.domain.ContentPart;
import com.shiyu.ai.conversation.domain.ConversationMessage;
import com.shiyu.ai.conversation.domain.MessageRole;
import com.shiyu.ai.conversation.domain.MessageStatus;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationExchangeCodecTest {

    @Test
    void roundTripsJsonlAndMarkdownWithTextParts() {
        List<ConversationMessage> messages = List.of(
                message("u", MessageRole.USER, "hello"),
                message("a", MessageRole.ASSISTANT, "world"));

        String jsonl = ConversationExchangeCodec.toJsonl(messages);
        assertEquals(List.of("USER:hello", "ASSISTANT:world"),
                ConversationExchangeCodec.fromJsonl(jsonl).stream()
                        .map(item -> item.role() + ":" + item.content()).toList());
        assertTrue(ConversationExchangeCodec.toMarkdown(messages).contains("## USER"));
        assertEquals(List.of("USER:hello", "ASSISTANT:world"),
                ConversationExchangeCodec.fromMarkdown(ConversationExchangeCodec.toMarkdown(messages)).stream()
                        .map(item -> item.role() + ":" + item.content()).toList());
    }

    @Test
    void importsContentPartsAndIgnoresBlankInput() {
        assertTrue(ConversationExchangeCodec.fromJsonl(null).isEmpty());
        assertTrue(ConversationExchangeCodec.fromMarkdown(" \n").isEmpty());
        String line = "{\"role\":\"assistant\",\"contentParts\":[{\"type\":\"text\",\"text\":\"a\"},{\"type\":\"image\",\"text\":\"ignored\"}]}";
        var imported = ConversationExchangeCodec.fromJsonl(line + "\n\n");
        assertEquals(1, imported.size());
        assertEquals("ASSISTANT", imported.getFirst().role());
        assertEquals("a", imported.getFirst().content());
    }

    @Test
    void appliesImportDefaultsAndMarkdownRoleBoundaries() {
        var imported = ConversationExchangeCodec.fromJsonl(
                "{\"textContent\":\"fallback\"}\n" +
                        "{\"role\":\"system\",\"textContent\":\" \" ,\"contentParts\":[{\"type\":\"image\"}]}\n");
        assertEquals("USER", imported.get(0).role());
        assertEquals("fallback", imported.get(0).content());
        assertEquals("SYSTEM", imported.get(1).role());
        assertEquals("", imported.get(1).content());

        var markdown = ConversationExchangeCodec.fromMarkdown(
                "text before header\n## assistant\nanswer\n## user\nquestion\n## ignored\n");
        assertEquals(3, markdown.size());
        assertEquals("USER", markdown.get(0).role());
        assertEquals("text before header", markdown.get(0).content());
        assertEquals("ASSISTANT", markdown.get(1).role());
        assertEquals("answer", markdown.get(1).content());
        assertEquals("USER", markdown.get(2).role());
        assertEquals("question", markdown.get(2).content());
    }

    @Test
    void roundTripsCharacterCardJsonAndPngMetadata() throws Exception {
        CharacterCardV2 card = new CharacterCardV2(
                null, "teacher", "description", "scenario", "hello",
                List.of("user: hi"), "be helpful", Map.of("x", 1), 0);
        assertEquals("teacher", CharacterCardCodec.fromJson(CharacterCardCodec.toJson(card)).name());
        byte[] png = CharacterCardCodec.toPng(card, new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB));
        CharacterCardV2 decoded = CharacterCardCodec.fromPng(png);
        assertEquals("teacher", decoded.name());
        assertEquals("chara_card_v2", decoded.spec());
    }

    private static ConversationMessage message(String id, MessageRole role, String text) {
        Instant now = Instant.now();
        return new ConversationMessage(id, "conversation", null, null, role,
                List.of(ContentPart.text(text)), Map.of(), MessageStatus.COMPLETED,
                0, null, now, now);
    }
}
