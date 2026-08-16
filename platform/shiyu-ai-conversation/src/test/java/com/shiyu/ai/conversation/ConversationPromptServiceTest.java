package com.shiyu.ai.conversation;

import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.runtime.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConversationPromptServiceTest {
    @Test
    void previewAndExecutionAssemblyIncludeTheSameOptInContextMessage() {
        ContextItem item = new ContextItem("KNOWLEDGE_CHUNK", "chunk-1", "algebra reference", 0.9,
                new ContextCitation("Algebra", "doc-1", "1", null), List.of("document:doc-1"), "knowledge-access", Instant.now());
        ContextAssemblyService context = new ContextAssemblyService(List.of(query -> List.of(item)), new DefaultContextPolicy());
        ConversationPromptService service = new ConversationPromptService(context);
        Conversation conversation = new Conversation("c1", 1, 2, "rag", "RAG", ConversationStatus.ACTIVE,
                null, null, "m2", null, "p", "m", 1, Instant.now(), Instant.now());
        ConversationMessage user = new ConversationMessage("m2", "c1", null, null, MessageRole.USER,
                List.of(ContentPart.text("What is algebra?")), Map.of(), MessageStatus.COMPLETED, 1, null, Instant.now(), Instant.now());

        ConversationPromptService.PromptAssembly assembly = service.assemble(conversation, List.of(user), 1, 2);

        assertEquals(1, assembly.contextItems().size());
        assertEquals("system", assembly.modelMessages().get(0).role());
        assertTrue(assembly.modelMessages().get(0).content().get(0).text().contains("algebra reference"));
        assertEquals("user", assembly.modelMessages().get(1).role());
    }

    @Test
    void ordinaryChatDoesNotCallContextProviders() {
        ContextAssemblyService context = new ContextAssemblyService(List.of(query -> {
            throw new AssertionError("provider must not be called for chat");
        }), new DefaultContextPolicy());
        ConversationPromptService service = new ConversationPromptService(context);
        Conversation conversation = new Conversation("c1", 1, 2, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, "m2", null, "p", "m", 1, Instant.now(), Instant.now());
        ConversationMessage user = new ConversationMessage("m2", "c1", null, null, MessageRole.USER,
                List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, 1, null, Instant.now(), Instant.now());

        assertTrue(service.assemble(conversation, List.of(user), 1, 2).contextItems().isEmpty());
    }
}
