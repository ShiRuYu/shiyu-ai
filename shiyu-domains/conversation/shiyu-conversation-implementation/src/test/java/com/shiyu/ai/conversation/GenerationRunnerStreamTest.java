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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class GenerationRunnerStreamTest {
    @Test
    void projectsStreamingEventsAndPersistsAssistantOnCompletion() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        GenerationUsageSink usage = mock(GenerationUsageSink.class);
        Instant now = Instant.now();
        ConversationMessage input = message("m1", "c1", 0);
        Conversation conversation = conversation("c1", "m1", 0);
        GenerationRun created = generation("g1");
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        when(conversations.findMessage("m1", new TenantId(1), 2)).thenReturn(Optional.of(input));
        when(conversations.findConversation("c1", new TenantId(1), 2)).thenReturn(Optional.of(conversation));
        when(conversations.listMessages("c1", new TenantId(1), 2, 1000)).thenReturn(List.of(input));
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        when(generations.find("g1", new TenantId(1), 2)).thenReturn(Optional.of(running));
        when(conversations.updateConversation(any(Conversation.class), anyLong())).thenReturn(1);
        when(engine.stream(any())).thenReturn(Flux.just(
                ChatResponse.builder().eventType("DELTA").content("hi ").build(),
                ChatResponse.builder().eventType("REASONING_DELTA").reasoningContent("think").build(),
                ChatResponse.builder().eventType("TOOL_CALL").toolCallId("tool-1").toolName("search").toolArguments("{}").build(),
                ChatResponse.builder().eventType("USAGE").promptTokens(3).completionTokens(2).totalTokens(5).build(),
                ChatResponse.builder().eventType("COMPLETED").build()));

        GenerationRunner runner = new GenerationRunner(engine, generations, conversations, usage, admission, null, new ConversationPromptService(null));
        runner.start(created, new TenantId(1), 2);

        verify(conversations).insertMessage(argThat(message -> message.role() == MessageRole.ASSISTANT && message.textContent().equals("hi ")));
        verify(admission).reserve(any(), eq(created), anyInt());
        verify(admission).settle(any(), argThat(run -> run.status() == GenerationStatus.COMPLETED));
        verify(usage).completed(any(GenerationRun.class), eq(new TenantId(1L)), eq(new UserId(2L)));
        verify(generations, atLeastOnce()).appendEvent(argThat(event -> event.type() == GenerationEventType.TOOL_CALL), eq(new TenantId(1L)));
    }

    @Test
    void convertsReactiveFailureIntoFailedRunAndReleasesAdmission() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        GenerationUsageSink usage = mock(GenerationUsageSink.class);
        ConversationMessage input = message("m1", "c1", 0);
        Conversation conversation = conversation("c1", "m1", 0);
        GenerationRun created = generation("g1");
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        when(conversations.findMessage("m1", new TenantId(1), 2)).thenReturn(Optional.of(input));
        when(conversations.findConversation("c1", new TenantId(1), 2)).thenReturn(Optional.of(conversation));
        when(conversations.listMessages("c1", new TenantId(1), 2, 1000)).thenReturn(List.of(input));
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        when(generations.find("g1", new TenantId(1), 2)).thenReturn(Optional.of(running));
        when(engine.stream(any())).thenReturn(Flux.error(new IllegalStateException("provider down")));

        new GenerationRunner(engine, generations, conversations, usage, admission, null, new ConversationPromptService(null)).start(created, new TenantId(1), 2);

        verify(admission).release(any(), argThat(run -> run.status() == GenerationStatus.FAILED));
        verify(usage).failed(argThat(run -> run.status() == GenerationStatus.FAILED));
        verify(generations).appendEvent(argThat(event -> event.type() == GenerationEventType.FAILED), eq(new TenantId(1L)));
    }

    @Test
    void turnsProviderCancellationIntoCancelledRunWithoutUsageFailure() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        GenerationUsageSink usage = mock(GenerationUsageSink.class);
        ConversationMessage input = message("m1", "c1", 0);
        Conversation conversation = conversation("c1", "m1", 0);
        GenerationRun created = generation("g1");
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        when(conversations.findMessage("m1", new TenantId(1), 2)).thenReturn(Optional.of(input));
        when(conversations.findConversation("c1", new TenantId(1), 2)).thenReturn(Optional.of(conversation));
        when(conversations.listMessages("c1", new TenantId(1), 2, 1000)).thenReturn(List.of(input));
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        when(generations.find("g1", new TenantId(1), 2)).thenReturn(Optional.of(running), Optional.of(running), Optional.of(running.transition(GenerationStatus.CANCELLED)));
        when(engine.stream(any())).thenReturn(Flux.just(ChatResponse.builder().eventType("CANCELLED").build()));

        new GenerationRunner(engine, generations, conversations, usage, admission, null, new ConversationPromptService(null)).start(created, new TenantId(1), 2);

        verify(admission).release(any(), argThat(run -> run.status() == GenerationStatus.CANCELLED));
        verify(usage, never()).failed(any());
        verify(generations).appendEvent(argThat(event -> event.type() == GenerationEventType.CANCELLED), eq(new TenantId(1L)));
    }

    @Test
    void recordsRuntimeProjectionForSuccessfulGeneration() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        AiRuntimePort runtime = mock(AiRuntimePort.class);
        ConversationMessage input = message("m1", "c1", 0);
        Conversation conversation = conversation("c1", "m1", 0);
        GenerationRun created = generation("g-runtime");
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        Instant now = Instant.now();
        AiRun runtimeRun = new AiRun("runtime-1", new TenantId(1), new UserId(2), null, null, AiRunSource.GENERATION, "g-runtime", null, null,
                "c1", "g-runtime", null, "model", "hash", AiRunStatus.RUNNING, 0, 0, false, null, now, null, null, 0);
        when(conversations.findMessage("m1", new TenantId(1), 2)).thenReturn(Optional.of(input));
        when(conversations.findConversation("c1", new TenantId(1), 2)).thenReturn(Optional.of(conversation));
        when(conversations.listMessages("c1", new TenantId(1), 2, 1000)).thenReturn(List.of(input));
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        when(generations.find("g-runtime", new TenantId(1), 2)).thenReturn(Optional.of(running));
        when(conversations.updateConversation(any(), anyLong())).thenReturn(1);
        when(runtime.startRun(any(), eq(AiRunSource.GENERATION), eq("g-runtime"), eq("gpt"), anyString())).thenReturn(runtimeRun);
        when(engine.stream(any())).thenReturn(Flux.just(ChatResponse.builder().eventType("DELTA").content("answer").build(), ChatResponse.builder().eventType("COMPLETED").build()));

        new GenerationRunner(engine, generations, conversations, new GenerationUsageSink() { }, admission, runtime, new ConversationPromptService(null))
                .start(created, new TenantId(1), 2);

        verify(runtime, atLeastOnce()).append(eq(runtimeRun), any(), anyString(), eq(true), anyString(), any(), any());
        verify(runtime).finish(eq("runtime-1"), eq(new TenantId(1L)), eq(2L), eq(AiRunStatus.COMPLETED), isNull());
        verify(generations, never()).appendEvent(any(), any(TenantId.class));
    }

    @Test
    void convertsRuntimeStartupFailureIntoFailedGeneration() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        AiRuntimePort runtime = mock(AiRuntimePort.class);
        ConversationMessage input = message("m1", "c1", 0);
        Conversation conversation = conversation("c1", "m1", 0);
        GenerationRun created = generation("g-runtime-fail");
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        when(conversations.findMessage("m1", new TenantId(1), 2)).thenReturn(Optional.of(input));
        when(conversations.findConversation("c1", new TenantId(1), 2)).thenReturn(Optional.of(conversation));
        when(conversations.listMessages("c1", new TenantId(1), 2, 1000)).thenReturn(List.of(input));
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        when(generations.find("g-runtime-fail", new TenantId(1), 2)).thenReturn(Optional.of(running));
        when(runtime.startRun(any(), eq(AiRunSource.GENERATION), eq("g-runtime-fail"), eq("gpt"), anyString())).thenThrow(new IllegalStateException("runtime unavailable"));

        new GenerationRunner(engine, generations, conversations, new GenerationUsageSink() { }, admission, runtime, new ConversationPromptService(null))
                .start(created, new TenantId(1), 2);

        verify(admission).release(any(), argThat(run -> run.status() == GenerationStatus.FAILED));
        verify(engine, never()).stream(any());
    }

    private static GenerationRun generation(String id) {
        Instant now = Instant.now();
        return new GenerationRun(id, "c1", "m1", null, null, "OPENAI", "gpt", GenerationStatus.CREATED,
                0, 0, 0, null, -1, false, 0, now, now);
    }

    private static Conversation conversation(String id, String leaf, long version) {
        Instant now = Instant.now();
        return new Conversation(id, 1, 2, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, leaf, null, "OPENAI", "gpt", version, now, now);
    }

    private static ConversationMessage message(String id, String conversationId, int sequence) {
        Instant now = Instant.now();
        return new ConversationMessage(id, conversationId, null, null, MessageRole.USER,
                List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, sequence, null, now, now);
    }
}
