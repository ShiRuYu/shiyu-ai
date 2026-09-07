package com.shiyu.ai.conversation;

import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.*;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.runtime.*;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GenerationRunnerBranchTest {
    @Test
    void ignoresProviderEventsAfterPersistedCancellation() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationUsageSink usage = mock(GenerationUsageSink.class);
        GenerationRun created = generation("g-cancelled");
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        GenerationRun cancelled = running.transition(GenerationStatus.CANCELLED);
        setupBasic(conversations, generations);
        when(generations.find("g-cancelled", new TenantId(1), 2)).thenReturn(Optional.of(cancelled));
        when(engine.stream(any())).thenReturn(Flux.just(
                ChatResponse.builder().eventType("DELTA").content("ignored").build(),
                ChatResponse.builder().eventType("COMPLETED").build()));

        runner(engine, generations, conversations, usage).start(created, new TenantId(1), 2);

        verify(conversations, never()).insertMessage(any());
        verify(usage, never()).completed(any(), any(TenantId.class), any(UserId.class));
    }

    @Test
    void runtimeProjectionFailureFinishesGenerationAsFailed() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationUsageSink usage = mock(GenerationUsageSink.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        AiRuntimePort runtime = mock(AiRuntimePort.class);
        GenerationRun created = generation("g-runtime-append");
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        setupBasic(conversations, generations);
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        when(generations.find("g-runtime-append", new TenantId(1), 2)).thenReturn(Optional.of(running));
        AiRun runtimeRun = new AiRun("r1", new TenantId(1), new UserId(2), null, null, AiRunSource.GENERATION,
                "g-runtime-append", null, null, "c1", "g-runtime-append", null, "gpt", "hash",
                AiRunStatus.RUNNING, 0, 0, false, null, Instant.now(), null, null, 0);
        when(runtime.startRun(any(), eq(AiRunSource.GENERATION), eq("g-runtime-append"), eq("gpt"), anyString()))
                .thenReturn(runtimeRun);
        doThrow(new IllegalStateException("runtime append failed")).when(runtime)
                .append(any(), eq(AiRunEventType.TURN_STARTED), anyString(), eq(true), anyString(), isNull(), isNull());

        runner(engine, generations, conversations, usage, admission, runtime).start(created, new TenantId(1), 2);

        verify(runtime).finish(eq("r1"), eq(new TenantId(1L)), eq(2L), eq(AiRunStatus.FAILED), eq("IllegalStateException"));
        verify(usage).failed(argThat(run -> run.status() == GenerationStatus.FAILED));
        verify(engine, never()).stream(any());
    }

    @Test
    void rejectsNonLeafInputBeforeReservingGeneration() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        setupBasic(conversations, generations);
        when(conversations.findConversation("c1", new TenantId(1), 2)).thenReturn(Optional.of(
                new Conversation("c1", 1, 2, "chat", "Chat", ConversationStatus.ACTIVE,
                        null, null, "different-leaf", null, "OPENAI", "gpt", 0, Instant.now(), Instant.now())));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> runner(engine, generations, conversations, mock(GenerationUsageSink.class), admission, null)
                        .start(generation("g-leaf"), new TenantId(1), 2));
        verify(admission, never()).reserve(any(), any(), anyInt());
        verify(engine, never()).stream(any());
    }

    @Test
    void releasesAdmissionWhenInitialGenerationCasIsLost() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        setupBasic(conversations, generations);
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(0);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> runner(engine, generations, conversations, mock(GenerationUsageSink.class), admission, null)
                        .start(generation("g-cas"), new TenantId(1), 2));
        verify(admission).reserve(any(), any(), anyInt());
        verify(admission).release(any(), any());
        verify(engine, never()).stream(any());
    }

    private static GenerationRunner runner(ChatEngine engine, GenerationRepository generations,
                                           ConversationRepository conversations, GenerationUsageSink usage) {
        return new GenerationRunner(engine, generations, conversations, usage, mock(GenerationAdmission.class), null,
                new ConversationPromptService(null));
    }

    private static GenerationRunner runner(ChatEngine engine, GenerationRepository generations,
                                           ConversationRepository conversations, GenerationUsageSink usage,
                                           GenerationAdmission admission, AiRuntimePort runtime) {
        return new GenerationRunner(engine, generations, conversations, usage, admission, runtime,
                new ConversationPromptService(null));
    }

    private static void setupBasic(ConversationRepository conversations, GenerationRepository generations) {
        when(conversations.findMessage("m1", new TenantId(1), 2)).thenReturn(Optional.of(message()));
        when(conversations.findConversation("c1", new TenantId(1), 2)).thenReturn(Optional.of(conversation()));
        when(conversations.listMessages("c1", new TenantId(1), 2, 1000)).thenReturn(List.of(message()));
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
    }

    private static GenerationRun generation(String id) {
        Instant now = Instant.now();
        return new GenerationRun(id, "c1", "m1", null, null, "OPENAI", "gpt", GenerationStatus.CREATED,
                0, 0, 0, null, -1, false, 0, now, now);
    }

    private static Conversation conversation() {
        Instant now = Instant.now();
        return new Conversation("c1", 1, 2, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, "m1", null, "OPENAI", "gpt", 0, now, now);
    }

    private static ConversationMessage message() {
        Instant now = Instant.now();
        return new ConversationMessage("m1", "c1", null, null, MessageRole.USER,
                List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, 0, null, now, now);
    }
}
