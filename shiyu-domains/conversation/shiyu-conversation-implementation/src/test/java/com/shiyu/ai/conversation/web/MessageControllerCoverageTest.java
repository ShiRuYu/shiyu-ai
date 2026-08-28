package com.shiyu.ai.conversation.web;

import com.shiyu.ai.kernel.context.TenantId;

import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.conversation.ConversationService;
import com.shiyu.ai.conversation.GenerationAdmissionException;
import com.shiyu.ai.conversation.GenerationRunner;
import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MessageControllerCoverageTest {
    private static final Instant NOW = Instant.now();
    private static final Conversation CONVERSATION = new Conversation("c1", 7, 9, "chat", "Chat", ConversationStatus.ACTIVE,
            null, null, "m1", null, "OPENAI", "gpt", 0, NOW, NOW);
    private static final ConversationMessage USER = new ConversationMessage("m1", "c1", null, null, MessageRole.USER,
            List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, 0, null, NOW, NOW);

    @Test
    void editsMessagesAndRetriesGenerationsWithIdempotency() {
        ConversationService conversations = mock(ConversationService.class);
        ConversationRepository repository = mock(ConversationRepository.class);
        GenerationRepository generations = mock(GenerationRepository.class);
        GenerationRunner runner = mock(GenerationRunner.class);
        IdempotencyRepository idempotency = mock(IdempotencyRepository.class);
        MessageController controller = new MessageController(conversations, repository, generations, runner, idempotency);
        ConversationMessage edited = new ConversationMessage("m2", "c1", null, null, MessageRole.USER, List.of(ContentPart.text("edited")), Map.of(), MessageStatus.COMPLETED, 0, "m1", NOW, NOW);
        GenerationRun generation = new GenerationRun("g1", "c1", "m1", null, "OPENAI", "gpt", GenerationStatus.CREATED, 0, 0, 0, null, -1, false, 0, NOW, NOW);
        when(repository.findMessage("m1", new TenantId(7), 9)).thenReturn(Optional.of(USER));
        when(repository.findConversation("c1", new TenantId(7), 9)).thenReturn(Optional.of(CONVERSATION));
        when(conversations.appendMessage(any(), isNull(), eq(MessageRole.USER), eq("edited"), isNull(), isNull(), eq("m1"))).thenReturn(edited);
        when(idempotency.claim(new TenantId(7), 9, "message.edit:m1", "edit-key", "m2")).thenReturn(true);
        when(conversations.createGeneration(any(), eq(USER), eq("OPENAI"), eq("gpt"))).thenReturn(generation);
        when(idempotency.claim(new TenantId(7), 9, "message.generation:m1", "gen-key", "g1")).thenReturn(true);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L); ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            MessageController.EditRequest edit = new MessageController.EditRequest(); edit.setContent("edited");
            assertEquals(edited, controller.edit("m1", "edit-key", edit).getData());
            MessageController.RetryRequest retry = new MessageController.RetryRequest(); retry.setPlatform("OPENAI"); retry.setModel("gpt");
            assertEquals(generation, controller.retry("m1", "gen-key", retry).getData());
            verify(runner).start(generation, new TenantId(7), 9);
        }
    }

    @Test
    void coversExistingKeysAndValidationFailures() {
        ConversationService conversations = mock(ConversationService.class); ConversationRepository repository = mock(ConversationRepository.class);
        GenerationRepository generations = mock(GenerationRepository.class); GenerationRunner runner = mock(GenerationRunner.class); IdempotencyRepository idempotency = mock(IdempotencyRepository.class);
        MessageController controller = new MessageController(conversations, repository, generations, runner, idempotency);
        GenerationRun generation = new GenerationRun("g1", "c1", "m1", null, "OPENAI", "gpt", GenerationStatus.CREATED, 0, 0, 0, null, -1, false, 0, NOW, NOW);
        when(idempotency.find(new TenantId(7), 9, "message.edit:m1", "existing")).thenReturn(Optional.of("m2"));
        when(repository.findMessage("m2", new TenantId(7), 9)).thenReturn(Optional.of(USER));
        when(idempotency.find(new TenantId(7), 9, "message.generation:m1", "existing")).thenReturn(Optional.of("g1"));
        when(generations.find("g1", new TenantId(7), 9)).thenReturn(Optional.of(generation));
        when(repository.findMessage("m1", new TenantId(7), 9)).thenReturn(Optional.of(USER)); when(repository.findConversation("c1", new TenantId(7), 9)).thenReturn(Optional.of(CONVERSATION));
        when(conversations.appendMessage(any(), any(), any(), anyString(), isNull(), isNull(), any())).thenReturn(USER);
        when(conversations.createGeneration(any(), eq(USER), isNull(), isNull())).thenReturn(generation);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L); ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            MessageController.EditRequest edit = new MessageController.EditRequest(); edit.setContent("x");
            assertEquals(USER, controller.edit("m1", "existing", edit).getData());
            assertEquals(generation, controller.retry("m1", "existing", null).getData());
            edit.setContent(" "); assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.edit("m1", null, edit));
            when(repository.findMessage("missing", new TenantId(7), 9)).thenReturn(Optional.empty());
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.edit("missing", null, edit));
            when(repository.findMessage("assistant", new TenantId(7), 9)).thenReturn(Optional.of(new ConversationMessage("assistant", "c1", null, null, MessageRole.ASSISTANT, List.of(ContentPart.text("a")), Map.of(), MessageStatus.COMPLETED, 0, null, NOW, NOW)));
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.retry("assistant", null, null));
            doThrow(new GenerationAdmissionException("QUOTA")).when(runner).start(generation, new TenantId(7), 9);
            when(idempotency.find(new TenantId(7), 9, "message.generation:m1", "quota")).thenReturn(Optional.empty());
            when(idempotency.claim(new TenantId(7), 9, "message.generation:m1", "quota", "g1")).thenReturn(true);
            assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.retry("m1", "quota", null));
        }
    }
}
