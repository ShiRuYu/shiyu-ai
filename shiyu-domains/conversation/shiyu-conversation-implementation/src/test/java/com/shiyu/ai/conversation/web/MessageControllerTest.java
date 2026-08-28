package com.shiyu.ai.conversation.web;

import com.shiyu.ai.kernel.context.TenantId;

import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.conversation.ConversationService;
import com.shiyu.ai.conversation.GenerationRunner;
import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MessageControllerTest {
    private final ConversationService service = mock(ConversationService.class);
    private final ConversationRepository conversations = mock(ConversationRepository.class);
    private final GenerationRepository generations = mock(GenerationRepository.class);
    private final GenerationRunner runner = mock(GenerationRunner.class);
    private final IdempotencyRepository idempotency = mock(IdempotencyRepository.class);
    private final MessageController controller = new MessageController(service, conversations, generations, runner, idempotency);
    private final Instant now = Instant.now();
    private final Conversation conversation = new Conversation("c1", 7, 8, "chat", "Chat", ConversationStatus.ACTIVE,
            null, null, "m1", null, "OPENAI", "gpt", 0, now, now);
    private final ConversationMessage input = new ConversationMessage("m1", "c1", null, null, MessageRole.USER,
            List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, 0, null, now, now);
    private final ConversationMessage edited = new ConversationMessage("m2", "c1", null, "m1", MessageRole.USER,
            List.of(ContentPart.text("updated")), Map.of(), MessageStatus.COMPLETED, 1, null, now, now);

    @BeforeEach
    void installActor() {
        UserContext actor = new UserContext();
        actor.setUserId(8L);
        actor.setCurrentTenantId(7L);
        actor.setHomeTenantId(7L);
        UserContextHolder.setContext(actor);
    }

    @AfterEach
    void clearActor() { UserContextHolder.clearContext(); }

    @Test
    void editsMessageAndSupportsIdempotentReplay() {
        when(conversations.findMessage("m1", new TenantId(7), 8)).thenReturn(Optional.of(input));
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(service.appendMessage(eq(conversation), isNull(), eq(MessageRole.USER), eq("updated"), isNull(), isNull(), eq("m1"))).thenReturn(edited);
        MessageController.EditRequest request = new MessageController.EditRequest();
        request.setContent("updated");

        assertEquals(edited, controller.edit("m1", null, request).getData());
        when(idempotency.find(new TenantId(7), 8, "message.edit:m1", "key")).thenReturn(Optional.of("m2"));
        when(conversations.findMessage("m2", new TenantId(7), 8)).thenReturn(Optional.of(edited));
        assertEquals(edited, controller.edit("m1", "key", request).getData());
    }

    @Test
    void retriesUserMessageAndStartsGeneration() {
        GenerationRun run = new GenerationRun("g1", "c1", "m1", null, null, "OPENAI", "gpt", GenerationStatus.CREATED,
                0, 0, 0, null, -1, false, 0, now, now);
        when(conversations.findMessage("m1", new TenantId(7), 8)).thenReturn(Optional.of(input));
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(service.createGeneration(conversation, input, null, null)).thenReturn(run);
        when(idempotency.claim(any(TenantId.class), anyLong(), anyString(), anyString(), anyString())).thenReturn(true);
        MessageController.RetryRequest request = new MessageController.RetryRequest();

        assertEquals(run, controller.retry("m1", "key", request).getData());
        verify(runner).start(run, new TenantId(7), 8);
    }

    @Test
    void rejectsInvalidEditAndNonUserRetry() {
        when(conversations.findMessage("m1", new TenantId(7), 8)).thenReturn(Optional.of(input));
        MessageController.EditRequest blank = new MessageController.EditRequest();
        blank.setContent(" ");
        assertThrows(RuntimeException.class, () -> controller.edit("m1", null, blank));

        ConversationMessage assistant = new ConversationMessage("a1", "c1", "m1", null, MessageRole.ASSISTANT,
                List.of(ContentPart.text("answer")), Map.of(), MessageStatus.COMPLETED, 1, null, now, now);
        when(conversations.findMessage("a1", new TenantId(7), 8)).thenReturn(Optional.of(assistant));
        assertThrows(RuntimeException.class, () -> controller.retry("a1", null, null));
    }

    @Test
    void handlesIdempotencyRaceAndMovesRetryLeafWithConflictMapping() {
        when(conversations.findMessage("m1", new TenantId(7), 8)).thenReturn(Optional.of(input));
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(service.appendMessage(any(), any(), eq(MessageRole.USER), eq("updated"), isNull(), isNull(), eq("m1"))).thenReturn(edited);
        when(idempotency.claim(new TenantId(7), 8, "message.edit:m1", "key", "m2")).thenReturn(false);
        when(idempotency.find(new TenantId(7), 8, "message.edit:m1", "key")).thenReturn(Optional.empty());
        MessageController.EditRequest edit = new MessageController.EditRequest(); edit.setContent("updated");
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.edit("m1", "key", edit));

        Conversation branched = new Conversation("c1", 7, 8, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, "old-leaf", null, "OPENAI", "gpt", 2, now, now);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(branched));
        GenerationRun run = new GenerationRun("g2", "c1", "m1", null, null, "OPENAI", "gpt", GenerationStatus.CREATED,
                0, 0, 0, null, -1, false, 0, now, now);
        when(conversations.updateConversation(any(Conversation.class), eq(2L))).thenReturn(1);
        when(service.createGeneration(any(), eq(input), isNull(), isNull())).thenReturn(run);
        assertEquals(run, controller.retry("m1", null, null).getData());
        verify(conversations).updateConversation(argThat(c -> "m1".equals(c.activeLeafMessageId())), eq(2L));

        when(conversations.updateConversation(any(Conversation.class), eq(2L))).thenReturn(0);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.retry("m1", null, null));
    }
}
