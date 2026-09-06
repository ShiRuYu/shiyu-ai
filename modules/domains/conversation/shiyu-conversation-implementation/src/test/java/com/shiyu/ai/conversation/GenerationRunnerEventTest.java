package com.shiyu.ai.conversation;

import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.*;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.runtime.AiRuntimePort;
import com.shiyu.ai.runtime.AiRun;
import com.shiyu.ai.runtime.AiRunSource;
import com.shiyu.ai.runtime.AiRunStatus;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GenerationRunnerEventTest {
    @Test
    void projectsEveryProviderEventAndUpdatesUsage() throws Exception {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        GenerationRun running = generation(GenerationStatus.RUNNING);
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(running));
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        GenerationRunner runner = new GenerationRunner(engine, generations, conversations, new GenerationUsageSink() { }, admission);
        Method onEvent = GenerationRunner.class.getDeclaredMethod("onEvent", ChatResponse.class, AtomicReference.class,
                AtomicReference.class, StringBuilder.class, long.class, long.class, AtomicInteger.class);
        onEvent.setAccessible(true);
        AtomicReference<GenerationRun> state = new AtomicReference<>(running);
        AtomicReference<com.shiyu.ai.runtime.AiRun> runtime = new AtomicReference<>();
        StringBuilder answer = new StringBuilder(); AtomicInteger sequence = new AtomicInteger();
        onEvent.invoke(runner, ChatResponse.builder().eventType("DELTA").content("hello").build(), state, runtime, answer, 7L, 8L, sequence);
        onEvent.invoke(runner, ChatResponse.builder().eventType("REASONING_DELTA").reasoningContent("think").build(), state, runtime, answer, 7L, 8L, sequence);
        onEvent.invoke(runner, ChatResponse.builder().eventType("TOOL_CALL").toolCallId("t1").toolName("search").toolArguments("{}").build(), state, runtime, answer, 7L, 8L, sequence);
        onEvent.invoke(runner, ChatResponse.builder().eventType("USAGE").promptTokens(2).completionTokens(3).totalTokens(5).estimatedUsage(true).build(), state, runtime, answer, 7L, 8L, sequence);
        onEvent.invoke(runner, ChatResponse.builder().eventType("BLOCK_STARTED").build(), state, runtime, answer, 7L, 8L, sequence);
        onEvent.invoke(runner, ChatResponse.builder().eventType("BLOCK_COMPLETED").build(), state, runtime, answer, 7L, 8L, sequence);
        onEvent.invoke(runner, ChatResponse.builder().eventType("UNKNOWN").build(), state, runtime, answer, 7L, 8L, sequence);
        assertEquals("hello", answer.toString());
        verify(generations, atLeast(6)).appendEvent(any(GenerationEvent.class), eq(new TenantId(7L)));

        AtomicReference<GenerationRun> cancelled = new AtomicReference<>(running);
        onEvent.invoke(runner, ChatResponse.builder().eventType("CANCELLED").build(), cancelled, runtime, new StringBuilder(), 7L, 8L, new AtomicInteger());
        assertEquals(GenerationStatus.CANCELLED, cancelled.get().status());
    }

    @Test
    void ignoresEventsAfterCancellationAndMapsProviderFailure() throws Exception {
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        GenerationRun cancelled = generation(GenerationStatus.CANCELLED);
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(cancelled));
        GenerationRunner runner = new GenerationRunner(mock(ChatEngine.class), generations, conversations, new GenerationUsageSink() { }, admission);
        Method onEvent = GenerationRunner.class.getDeclaredMethod("onEvent", ChatResponse.class, AtomicReference.class,
                AtomicReference.class, StringBuilder.class, long.class, long.class, AtomicInteger.class);
        onEvent.setAccessible(true);
        AtomicReference<GenerationRun> state = new AtomicReference<>(cancelled);
        onEvent.invoke(runner, ChatResponse.builder().eventType("DELTA").content("ignored").build(), state,
                new AtomicReference<>(), new StringBuilder(), 7L, 8L, new AtomicInteger());
        verify(generations, never()).appendEvent(any(), any(TenantId.class));
    }

    @Test
    void finalizesAssistantMessageAndHandlesConversationCasLoss() throws Exception {
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        GenerationUsageSink usage = mock(GenerationUsageSink.class);
        Instant now = Instant.now();
        Conversation conversation = new Conversation("c1", 7, 8, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, "m1", null, "OPENAI", "gpt", 0, now, now);
        ConversationMessage input = new ConversationMessage("m1", "c1", null, null, MessageRole.USER,
                List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, 0, null, now, now);
        GenerationRun running = generation(GenerationStatus.RUNNING);
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(running));
        when(conversations.findMessage("m1", new TenantId(7), 8)).thenReturn(Optional.of(input));
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(conversations.updateConversation(any(Conversation.class), eq(0L))).thenReturn(1);
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        GenerationRunner runner = new GenerationRunner(mock(ChatEngine.class), generations, conversations, usage, admission);
        Method finish = GenerationRunner.class.getDeclaredMethod("finishSuccess", AtomicReference.class, AtomicReference.class,
                long.class, long.class, AtomicInteger.class, String.class);
        finish.setAccessible(true);
        finish.invoke(runner, new AtomicReference<>(running), new AtomicReference<>(), 7L, 8L, new AtomicInteger(), "answer");
        verify(conversations).insertMessage(argThat(message -> message.role() == MessageRole.ASSISTANT && "answer".equals(message.textContent())));
        verify(admission).settle(any(), argThat(value -> value.status() == GenerationStatus.COMPLETED));
        verify(usage).completed(any(), eq(new TenantId(7L)), eq(new UserId(8L)));

        when(conversations.updateConversation(any(Conversation.class), eq(0L))).thenReturn(0);
        finish.invoke(runner, new AtomicReference<>(running), new AtomicReference<>(), 7L, 8L, new AtomicInteger(), "answer");
        verify(conversations, atLeastOnce()).deleteMessage(anyString(), eq(new TenantId(7L)), eq(8L));
    }

    @Test
    void recordsSynchronousProviderFailureAndDoesNotOverwriteLostCas() throws Exception {
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        GenerationUsageSink usage = mock(GenerationUsageSink.class);
        GenerationRun running = generation(GenerationStatus.RUNNING);
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(running));
        when(generations.update(any(GenerationRun.class), eq(0L))).thenReturn(1);
        GenerationRunner runner = new GenerationRunner(mock(ChatEngine.class), generations, conversations, usage, admission);
        Method finish = GenerationRunner.class.getDeclaredMethod("finishFailure", AtomicReference.class, AtomicReference.class,
                long.class, long.class, AtomicInteger.class, Throwable.class);
        finish.setAccessible(true);
        AtomicReference<GenerationRun> state = new AtomicReference<>(running);
        finish.invoke(runner, state, new AtomicReference<>(), 7L, 8L, new AtomicInteger(), new IllegalStateException("provider down"));
        assertEquals(GenerationStatus.FAILED, state.get().status());
        verify(admission).release(any(), argThat(value -> value.status() == GenerationStatus.FAILED));
        verify(usage).failed(any());

        when(generations.update(any(GenerationRun.class), eq(0L))).thenReturn(0);
        finish.invoke(runner, new AtomicReference<>(running), new AtomicReference<>(), 7L, 8L, new AtomicInteger(), new RuntimeException("lost"));
        verify(admission, times(1)).release(any(), any());
    }

    @Test
    void mapsRuntimeEventTypesAndUsesPersistedUsageWhenProviderOmitsCounts() throws Exception {
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        AiRuntimePort runtime = mock(AiRuntimePort.class);
        GenerationRun base = generation(GenerationStatus.RUNNING);
        GenerationRun running = new GenerationRun(base.id(), base.conversationId(), base.inputMessageId(), base.assistantMessageId(),
                base.speakerId(), base.platform(), base.model(), base.status(), 4, 6, base.latencyMs(), base.errorCode(),
                base.lastEventSequence(), base.cancelRequested(), base.version(), base.createdAt(), base.updatedAt());
        AiRun run = new AiRun("runtime", new TenantId(7), new UserId(8), null, null, AiRunSource.GENERATION, "g1", null, null,
                "c1", "g1", null, "gpt", "hash", AiRunStatus.RUNNING, 4, 6, false, null,
                Instant.now(), null, null, 0);
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(running));
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        GenerationRunner runner = new GenerationRunner(mock(ChatEngine.class), generations, conversations,
                new GenerationUsageSink() { }, mock(GenerationAdmission.class), runtime, new ConversationPromptService(null));
        Method onEvent = GenerationRunner.class.getDeclaredMethod("onEvent", ChatResponse.class, AtomicReference.class,
                AtomicReference.class, StringBuilder.class, long.class, long.class, AtomicInteger.class);
        onEvent.setAccessible(true);
        AtomicReference<GenerationRun> state = new AtomicReference<>(running);
        AtomicReference<AiRun> runtimeState = new AtomicReference<>(run);
        AtomicInteger sequence = new AtomicInteger();
        onEvent.invoke(runner, ChatResponse.builder().eventType("TOOL_CALL").toolCallId("t").toolName("search").toolArguments("{}").build(), state, runtimeState, new StringBuilder(), 7L, 8L, sequence);
        onEvent.invoke(runner, ChatResponse.builder().eventType("REASONING_DELTA").reasoningContent("think").build(), state, runtimeState, new StringBuilder(), 7L, 8L, sequence);
        onEvent.invoke(runner, ChatResponse.builder().eventType("BLOCK_STARTED").build(), state, runtimeState, new StringBuilder(), 7L, 8L, sequence);
        onEvent.invoke(runner, ChatResponse.builder().eventType("BLOCK_COMPLETED").build(), state, runtimeState, new StringBuilder(), 7L, 8L, sequence);
        onEvent.invoke(runner, ChatResponse.builder().eventType("USAGE").build(), state, runtimeState, new StringBuilder(), 7L, 8L, sequence);

        verify(runtime, atLeast(5)).append(eq(run), any(), anyString(), eq(true), anyString(), any(), any());
        verify(runtime).recordUsage(eq("runtime"), eq(new TenantId(7L)), eq(8L), eq(4L), eq(6L), eq(false), isNull());
    }

    private static GenerationRun generation(GenerationStatus status) {
        Instant now = Instant.now();
        return new GenerationRun("g1", "c1", "m1", null, null, "OPENAI", "gpt", status,
                0, 0, 0, null, -1, false, 0, now, now);
    }
}
