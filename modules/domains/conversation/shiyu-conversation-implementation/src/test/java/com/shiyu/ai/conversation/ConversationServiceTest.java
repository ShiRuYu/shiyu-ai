package com.shiyu.ai.conversation;

import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.conversation.port.GenerationRepository;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.runtime.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConversationServiceTest {
    private final ConversationRepository conversations = mock(ConversationRepository.class);
    private final GenerationRepository generations = mock(GenerationRepository.class);
    private final ConversationService service = new ConversationService(conversations, generations);
    private final Instant now = Instant.now();

    @Test
    void createsConversationWithDefaultTitleAndPersistsSystemPrompt() {
        when(conversations.updateConversation(any(Conversation.class), anyLong())).thenReturn(1);
        Conversation created = service.create(new TenantId(7), 8, "chat", " ", "OPENAI", "gpt", "be concise");

        assertEquals("New conversation", created.title());
        assertEquals(7, created.tenantId());
        verify(conversations).insertConversation(any(Conversation.class));
        verify(conversations).insertMessage(argThat(message -> message.role() == MessageRole.SYSTEM && message.textContent().equals("be concise")));
        verify(conversations).updateConversation(any(Conversation.class), eq(0L));
    }

    @Test
    void appendingDeletesMessageWhenConversationCasLoses() {
        Conversation conversation = conversation("c1", "m0", 3);
        when(conversations.listMessages("c1", new TenantId(7), 8, 1000)).thenReturn(List.of(message("m0", "c1", 0)));
        when(conversations.updateConversation(any(Conversation.class), eq(3L))).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.appendUserMessage(conversation, "hello"));
        verify(conversations).deleteMessage(anyString(), eq(new TenantId(7)), eq(8L));
    }

    @Test
    void generationRejectsWrongConversationInactiveLeafAndRunningDuplicate() {
        Conversation conversation = conversation("c1", "m1", 1);
        ConversationMessage wrong = message("m2", "other", 0);
        assertThrows(IllegalArgumentException.class, () -> service.createGeneration(conversation, wrong, "OPENAI", "gpt"));

        ConversationMessage stale = message("m0", "c1", 0);
        assertThrows(IllegalStateException.class, () -> service.createGeneration(conversation, stale, "OPENAI", "gpt"));

        ConversationMessage input = message("m1", "c1", 1);
        when(generations.hasRunning("c1", "m1", new TenantId(7))).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.createGeneration(conversation, input, "OPENAI", "gpt"));
    }

    @Test
    void recordsCompletedGenerationAndEvents() {
        Conversation conversation = conversation("c1", "m1", 1);
        ConversationMessage input = message("m1", "c1", 1);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(generations.hasRunning("c1", "m1", new TenantId(7))).thenReturn(false);
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        when(conversations.updateConversation(any(Conversation.class), anyLong())).thenReturn(1);

        GenerationRun completed = service.recordCompletedGeneration(conversation, input, "answer", "OPENAI", "gpt", 4, 6);

        assertEquals(GenerationStatus.COMPLETED, completed.status());
        assertEquals(4, completed.promptTokens());
        assertEquals(6, completed.completionTokens());
        verify(conversations).insertMessage(argThat(message -> message.role() == MessageRole.ASSISTANT && message.textContent().equals("answer")));
        verify(generations, atLeast(4)).appendEvent(any(GenerationEvent.class), eq(new TenantId(7)));
    }

    @Test
    void recordsConflictByDeletingAssistantAndFailingGeneration() {
        Conversation conversation = conversation("c1", "m1", 1);
        ConversationMessage input = message("m1", "c1", 1);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(generations.hasRunning("c1", "m1", new TenantId(7))).thenReturn(false);
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        when(conversations.updateConversation(any(Conversation.class), anyLong())).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.recordCompletedGeneration(conversation, input, "answer", "OPENAI", "gpt", 1, 1));
        verify(conversations).deleteMessage(anyString(), eq(new TenantId(7)), eq(8L));
        verify(generations).appendEvent(argThat(event -> event.type() == GenerationEventType.FAILED), eq(new TenantId(7)));
    }

    @Test
    void recordsCompletedGenerationThroughRuntimeProjection() {
        Conversation conversation = conversation("c1", "m1", 1);
        ConversationMessage input = message("m1", "c1", 1);
        AiRuntimePort runtime = mock(AiRuntimePort.class);
        AiRun runtimeRun = new AiRun("r1", new TenantId(7), new UserId(8), null, null, AiRunSource.API, "openai", null, null,
                "c1", null, null, "gpt", "hash", AiRunStatus.RUNNING, 0, 0, false, null, now, null, null, 0);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(generations.hasRunning("c1", "m1", new TenantId(7))).thenReturn(false);
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        when(conversations.updateConversation(any(Conversation.class), anyLong())).thenReturn(1);
        when(runtime.linkGeneration(eq(runtimeRun), anyString())).thenReturn(runtimeRun);
        GenerationRun completed = service.recordCompletedGeneration(conversation, input, "answer", "OPENAI", "gpt", 4, 6, runtime, runtimeRun);
        assertEquals(GenerationStatus.COMPLETED, completed.status());
        assertEquals("r1", completed.runtimeRunId());
        verify(runtime).recordUsage(eq("r1"), eq(new TenantId(7L)), eq(8L), eq(4L), eq(6L), eq(false), isNull());
        verify(runtime).finish(eq("r1"), eq(new TenantId(7L)), eq(8L), eq(AiRunStatus.COMPLETED), isNull());
        verify(generations, never()).appendEvent(any(), any(TenantId.class));
    }

    @Test
    void rejectsMissingConversationAndGenerationCompletionCasConflict() {
        Conversation conversation = conversation("c1", "m1", 1);
        ConversationMessage input = message("m1", "c1", 1);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.recordCompletedGeneration(conversation, input, null, "OPENAI", "gpt", 0, 0));

        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(generations.hasRunning("c1", "m1", new TenantId(7))).thenReturn(false);
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1, 0);
        when(conversations.updateConversation(any(Conversation.class), anyLong())).thenReturn(1);
        assertThrows(IllegalStateException.class, () -> service.recordCompletedGeneration(conversation, input, "", "OPENAI", "gpt", 0, 0));
    }

    @Test
    void runtimeFailureWhilePersistingConflictIsContained() {
        Conversation conversation = conversation("c1", "m1", 1);
        ConversationMessage input = message("m1", "c1", 1);
        AiRuntimePort runtime = mock(AiRuntimePort.class);
        AiRun runtimeRun = new AiRun("r-fail", new TenantId(7), new UserId(8), null, null, AiRunSource.API, "openai", null, null,
                "c1", null, null, "gpt", "hash", AiRunStatus.RUNNING, 0, 0, false, null, now, null, null, 0);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(generations.hasRunning("c1", "m1", new TenantId(7))).thenReturn(false);
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        when(conversations.updateConversation(any(Conversation.class), anyLong())).thenReturn(0);
        doThrow(new IllegalStateException("runtime unavailable")).when(runtime).finish(anyString(), any(TenantId.class), anyLong(), eq(AiRunStatus.FAILED), anyString());
        assertThrows(IllegalStateException.class, () -> service.recordCompletedGeneration(conversation, input, "answer", "OPENAI", "gpt", 1, 1, runtime, runtimeRun));
    }

    private Conversation conversation(String id, String leaf, long version) {
        return new Conversation(id, 7, 8, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, leaf, null, "OPENAI", "gpt", version, now, now);
    }

    private ConversationMessage message(String id, String conversationId, int sequence) {
        return new ConversationMessage(id, conversationId, null, null, MessageRole.USER,
                List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, sequence, null, now, now);
    }
}
