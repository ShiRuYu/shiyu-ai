package com.shiyu.ai.conversation;

import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.conversation.domain.GenerationStatus;
import com.shiyu.ai.conversation.domain.ConversationMessage;
import com.shiyu.ai.conversation.domain.ContentPart;
import com.shiyu.ai.conversation.domain.MessageRole;
import com.shiyu.ai.conversation.domain.MessageStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConversationDomainTest {
    @Test void generationLifecycleRejectsTerminalMutation() {
        GenerationRun created = new GenerationRun("run", "conversation", "message", null, "OPENAI", "model", GenerationStatus.CREATED, 0, 0, 0, null, -1, false, 0, Instant.now(), Instant.now());
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        assertEquals(GenerationStatus.RUNNING, running.status());
        assertEquals(GenerationStatus.COMPLETED, running.transition(GenerationStatus.COMPLETED).status());
        assertThrows(IllegalStateException.class, () -> created.transition(GenerationStatus.COMPLETED));
    }

    @Test void groupSpeakerSurvivesLifecycleTransitions() {
        GenerationRun created = new GenerationRun("run", "conversation", "message", null, "character-1",
                "OPENAI", "model", GenerationStatus.CREATED, 0, 0, 0, null, -1, false, 0, Instant.now(), Instant.now());
        assertEquals("character-1", created.transition(GenerationStatus.RUNNING).speakerId());
    }

    @Test void promptSafetyRedactsSecretsAndEstimates() {
        assertFalse(PromptSafety.redact("Authorization: Bearer abc123 apiKey=secret").contains("abc123"));
        assertTrue(PromptSafety.estimateTokens("hello world") > 0);
    }

    @Test void promptAssemblerKeepsStructuredMessageOrderAndTailBudget() {
        List<ConversationMessage> messages = List.of(message("system", MessageRole.SYSTEM, 0), message("first", MessageRole.USER, 1), message("second", MessageRole.ASSISTANT, 2));
        assertEquals(List.of("first", "second"), PromptAssembler.assemble(messages, 2).stream().map(ConversationMessage::textContent).toList());
        assertEquals(MessageRole.SYSTEM, PromptAssembler.assemble(messages, 10).get(0).role());
    }

    @Test void promptAssemblerFollowsActiveLeafInsteadOfSiblingSequence() {
        ConversationMessage root = message("root", MessageRole.USER, 0);
        ConversationMessage active = new ConversationMessage("active", "conversation", root.id(), null,
                MessageRole.ASSISTANT, List.of(ContentPart.text("active")), Map.of(), MessageStatus.COMPLETED,
                1, null, Instant.now(), Instant.now());
        ConversationMessage sibling = new ConversationMessage("sibling", "conversation", root.id(), null,
                MessageRole.ASSISTANT, List.of(ContentPart.text("sibling")), Map.of(), MessageStatus.COMPLETED,
                2, null, Instant.now(), Instant.now());
        List<ConversationMessage> path = PromptAssembler.activePath(List.of(root, active, sibling), "active", 10);
        assertEquals(List.of("root", "active"), path.stream().map(ConversationMessage::textContent).toList());
    }

    private ConversationMessage message(String text, MessageRole role, int sequence) {
        Instant now = Instant.now();
        return new ConversationMessage(text, "conversation", null, null, role, List.of(ContentPart.text(text)), Map.of(), MessageStatus.COMPLETED, sequence, null, now, now);
    }
}
