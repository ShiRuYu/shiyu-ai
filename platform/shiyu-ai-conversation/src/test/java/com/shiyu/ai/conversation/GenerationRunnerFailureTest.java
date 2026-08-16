package com.shiyu.ai.conversation;

import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.conversation.port.GenerationAdmission;
import com.shiyu.ai.conversation.port.GenerationRepository;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.runtime.AiRuntimeService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Regression for providers that fail before returning a reactive Publisher. */
class GenerationRunnerFailureTest {
    @Test
    void synchronousProviderFailureLeavesAReplayedFailedRun() {
        ChatEngine engine = mock(ChatEngine.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        ConversationRepository conversations = mock(ConversationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);

        Instant now = Instant.now();
        ConversationMessage input = new ConversationMessage("m1", "c1", null, null, MessageRole.USER,
                List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, 0, null, now, now);
        Conversation conversation = new Conversation("c1", 1, 2, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, "m1", null, "OPENAI", "model", 0, now, now);
        GenerationRun created = new GenerationRun("g1", "c1", "m1", null, "OPENAI", "model",
                GenerationStatus.CREATED, 0, 0, 0, null, -1, false, 0, now, now);
        GenerationRun running = created.transition(GenerationStatus.RUNNING);

        when(conversations.findMessage("m1", 1, 2)).thenReturn(Optional.of(input));
        when(conversations.findConversation("c1", 1, 2)).thenReturn(Optional.of(conversation));
        when(conversations.listMessages("c1", 1, 2, 1000)).thenReturn(List.of(input));
        when(generations.update(any(GenerationRun.class), anyLong())).thenReturn(1);
        when(generations.find("g1", 1, 2)).thenReturn(Optional.of(running));
        doThrow(new IllegalStateException("provider unavailable")).when(engine).stream(any());

        GenerationRunner runner = new GenerationRunner(engine, generations, conversations,
                new com.shiyu.ai.conversation.port.GenerationUsageSink() { }, admission,
                (AiRuntimeService) null, new ConversationPromptService(null));

        runner.start(created, 1, 2);

        verify(admission).release(eq(1L), argThat(run -> run.status() == GenerationStatus.FAILED));
        verify(generations, atLeastOnce()).appendEvent(argThat(event -> event.type() == GenerationEventType.FAILED), eq(1L));
        verify(generations, never()).appendEvent(argThat(event -> event.type() == GenerationEventType.COMPLETED), eq(1L));
        assertEquals(GenerationStatus.RUNNING, running.status());
    }
}
