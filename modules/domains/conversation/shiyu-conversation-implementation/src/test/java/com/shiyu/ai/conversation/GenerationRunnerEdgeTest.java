package com.shiyu.ai.conversation;

import com.shiyu.ai.kernel.context.TenantId;

import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.*;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GenerationRunnerEdgeTest {
    @Test
    void rejectsMissingTenantBeforeAccessingConversationData() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);

        assertThrows(NullPointerException.class,
                () -> runner(engine, generations, conversations).start(generation("g-null-tenant"), null, 2));
        verifyNoInteractions(conversations, generations, engine);
    }

    @Test
    void rejectsMissingInputConversationAndStaleConversationLeaf() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationRun created = generation("g-edge");
        when(conversations.findMessage("m1", new TenantId(1), 2)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> runner(engine, generations, conversations).start(created, new TenantId(1), 2));
        verify(conversations).findMessage("m1", new TenantId(1), 2);

        ConversationMessage input = message("m1", "c1", 0);
        when(conversations.findMessage("m1", new TenantId(1), 2)).thenReturn(Optional.of(input));
        when(conversations.findConversation("c1", new TenantId(1), 2)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> runner(engine, generations, conversations).start(created, new TenantId(1), 2));

        Conversation stale = conversation("c1", "other");
        when(conversations.findConversation("c1", new TenantId(1), 2)).thenReturn(Optional.of(stale));
        assertThrows(IllegalStateException.class, () -> runner(engine, generations, conversations).start(created, new TenantId(1), 2));
    }

    @Test
    void releasesAdmissionWhenInitialGenerationCasLoses() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        GenerationRun created = generation("g-cas-loss");
        setupBasic(conversations, generations);
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(0);
        assertThrows(IllegalStateException.class, () -> runner(engine, generations, conversations,
                mock(GenerationUsageSink.class), admission).start(created, new TenantId(1), 2));
        verify(admission).release(any(), eq(created));
    }

    @Test
    void handlesNullAndUnknownProviderStreamsAsFailedRuns() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        GenerationUsageSink usage = mock(GenerationUsageSink.class);
        setupBasic(conversations, generations);
        GenerationRun created = generation("g-stream");
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        when(generations.find("g-stream", new TenantId(1), 2)).thenReturn(Optional.of(running));
        when(engine.stream(any())).thenReturn(null);
        runner(engine, generations, conversations, usage, admission).start(created, new TenantId(1), 2);
        verify(usage).failed(any(GenerationRun.class));

        reset(engine, generations, admission, usage);
        setupBasic(conversations, generations);
        when(generations.find("g-stream", new TenantId(1), 2)).thenReturn(Optional.of(running));
        when(engine.stream(any())).thenReturn(Flux.just(
                ChatResponse.builder().eventType("UNKNOWN_EVENT").content("x").build(),
                ChatResponse.builder().eventType("FAILED").errorMessage("provider_failed").build()));
        runner(engine, generations, conversations, usage, admission).start(created, new TenantId(1), 2);
        verify(admission, atLeastOnce()).release(any(), any(GenerationRun.class));
    }

    @Test
    void restoresAssistantWhenConversationCasFailsAfterProviderCompletion() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        GenerationUsageSink usage = mock(GenerationUsageSink.class);
        setupBasic(conversations, generations);
        GenerationRun created = generation("g-cas");
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        when(generations.find("g-cas", new TenantId(1), 2)).thenReturn(Optional.of(running));
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1, 0);
        when(engine.stream(any())).thenReturn(Flux.just(ChatResponse.builder().eventType("DELTA").content("answer").build(), ChatResponse.builder().eventType("COMPLETED").build()));
        when(conversations.updateConversation(any(), anyLong())).thenReturn(0);
        runner(engine, generations, conversations, usage, admission).start(created, new TenantId(1), 2);
        verify(conversations).deleteMessage(anyString(), eq(new TenantId(1L)), eq(2L));
    }

    private static GenerationRunner runner(ChatEngine engine, GenerationRepository generations, ConversationRepository conversations) {
        return runner(engine, generations, conversations, mock(GenerationUsageSink.class), mock(GenerationAdmission.class));
    }

    private static GenerationRunner runner(ChatEngine engine, GenerationRepository generations, ConversationRepository conversations,
                                           GenerationUsageSink usage, GenerationAdmission admission) {
        return new GenerationRunner(engine, generations, conversations, usage, admission, null, new ConversationPromptService(null));
    }

    private static void setupBasic(ConversationRepository conversations, GenerationRepository generations) {
        when(conversations.findMessage("m1", new TenantId(1), 2)).thenReturn(Optional.of(message("m1", "c1", 0)));
        when(conversations.findConversation("c1", new TenantId(1), 2)).thenReturn(Optional.of(conversation("c1", "m1")));
        when(conversations.listMessages("c1", new TenantId(1), 2, 1000)).thenReturn(List.of(message("m1", "c1", 0)));
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
    }

    private static GenerationRun generation(String id) {
        Instant now = Instant.now();
        return new GenerationRun(id, "c1", "m1", null, null, "OPENAI", "gpt", GenerationStatus.CREATED,
                0, 0, 0, null, -1, false, 0, now, now);
    }

    private static Conversation conversation(String id, String leaf) {
        Instant now = Instant.now();
        return new Conversation(id, 1, 2, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, leaf, null, "OPENAI", "gpt", 0, now, now);
    }

    private static ConversationMessage message(String id, String conversationId, int sequence) {
        Instant now = Instant.now();
        return new ConversationMessage(id, conversationId, null, null, MessageRole.USER,
                List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, sequence, null, now, now);
    }

}
