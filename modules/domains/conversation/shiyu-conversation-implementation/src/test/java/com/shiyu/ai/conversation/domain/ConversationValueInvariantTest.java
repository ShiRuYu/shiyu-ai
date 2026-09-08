package com.shiyu.ai.conversation.domain;

import com.shiyu.ai.conversation.PromptSafety;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConversationValueInvariantTest {
    @Test
    void validatesConversationAndMessageRequiredFieldsAndNormalizesCollections() {
        assertThrows(IllegalArgumentException.class, () -> new Conversation("", 1, 2, "chat", "", ConversationStatus.ACTIVE, null, null, null, null, null, null, 0, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Conversation("c", 1, 2, "", "", ConversationStatus.ACTIVE, null, null, null, null, null, null, 0, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Conversation("c", 1, 2, "chat", "", null, null, null, null, null, null, null, 0, null, null));

        ConversationMessage message = new ConversationMessage("m", "c", null, null, MessageRole.USER, null, null, MessageStatus.COMPLETED, 1, null, Instant.now(), null);
        assertEquals(Map.of(), message.toolCall());
        assertEquals("", message.textContent());
        assertThrows(IllegalArgumentException.class, () -> new ConversationMessage("", "c", null, null, MessageRole.USER, null, null, MessageStatus.COMPLETED, 1, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new ConversationMessage("m", "", null, null, MessageRole.USER, null, null, MessageStatus.COMPLETED, 1, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new ConversationMessage("m", "c", null, null, null, null, null, MessageStatus.COMPLETED, 1, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new ConversationMessage("m", "c", null, null, MessageRole.USER, null, null, null, 1, null, null, null));
    }

    @Test
    void estimatesAndRedactsPromptSafely() {
        assertNull(PromptSafety.redact(null));
        assertEquals("", PromptSafety.redact(""));
        assertEquals(0, PromptSafety.estimateTokens(null));
        assertEquals(0, PromptSafety.estimateTokens("   "));
        assertEquals(1, PromptSafety.estimateTokens("你好"));
        assertEquals("token [REDACTED]", PromptSafety.redact("token api-key=secret"));
        assertEquals("[REDACTED]", PromptSafety.redact("Bearer abc.def"));
    }
}
