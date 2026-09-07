package com.shiyu.ai.conversation.web;

import com.shiyu.ai.kernel.context.TenantId;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.conversation.*;
import com.shiyu.ai.conversation.chat.ConversationExchangeCodec;
import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConversationControllerTest {
    private final ConversationService service = mock(ConversationService.class);
    private final ConversationRepository conversations = mock(ConversationRepository.class);
    private final GenerationRunner runner = mock(GenerationRunner.class);
    private final IdempotencyRepository idempotency = mock(IdempotencyRepository.class);
    private final GenerationRepository generations = mock(GenerationRepository.class);
    private final ConversationPromptService prompts = mock(ConversationPromptService.class);
    private final ConversationImportPreviewStore imports = new ConversationImportPreviewStore();
    private final ConversationController controller = new ConversationController(service, conversations, runner, idempotency, generations, imports, prompts);
    private final Instant now = Instant.now();
    private final Conversation conversation = new Conversation("c1", 7, 8, "chat", "Chat", ConversationStatus.ACTIVE,
            null, null, "m1", null, "OPENAI", "gpt", 0, now, now);
    private final ConversationMessage message = new ConversationMessage("m1", "c1", null, null, MessageRole.USER,
            List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, 0, null, now, now);

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
    void coversConversationQueriesMutationsAndExports() {
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(conversations.listConversations(new TenantId(7), 8, 20, 0)).thenReturn(List.of(conversation));
        when(conversations.listMessages("c1", new TenantId(7), 8, 1000)).thenReturn(List.of(message));
        when(conversations.findMessage("m1", new TenantId(7), 8)).thenReturn(Optional.of(message));
        when(conversations.updateConversation(any(Conversation.class), eq(0L))).thenReturn(1);
        when(conversations.deleteConversation("c1", new TenantId(7), 8)).thenReturn(1);

        assertEquals(List.of(conversation), controller.list(20, 0).getData());
        assertEquals(conversation, controller.detail("c1").getData());
        assertEquals(List.of(message), controller.messages("c1", 1000).getData());
        ConversationController.UpdateConversationRequest update = new ConversationController.UpdateConversationRequest();
        update.setTitle("Renamed");
        assertEquals("Renamed", controller.update("c1", update).getData().title());
        assertTrue(controller.delete("c1").isSuccess());
        assertTrue(controller.activeLeaf("c1", "m1", null).isSuccess());
        when(conversations.listBranches("c1", new TenantId(7), 8)).thenReturn(List.of(conversation));
        assertEquals(List.of(conversation), controller.branches("c1").getData());

        Object json = controller.export("c1", null);
        assertTrue(json instanceof Result<?>);
        Object jsonl = controller.export("c1", "jsonl");
        assertTrue(jsonl instanceof ResponseEntity<?>);
        Object markdown = controller.export("c1", "markdown");
        assertTrue(markdown instanceof ResponseEntity<?>);
    }

    @Test
    void createsIdempotentlyAndPreviewsThenImportsPayload() {
        ConversationController.CreateConversationRequest create = new ConversationController.CreateConversationRequest();
        create.setTitle("Chat");
        when(service.create(new TenantId(7), 8, "general", "Chat", null, null, null)).thenReturn(conversation);
        assertEquals(conversation, controller.create(null, create).getData());

        String jsonl = "{\"role\":\"user\",\"contentParts\":[{\"type\":\"text\",\"text\":\"hello\"}]}";
        ConversationController.ImportRequest request = new ConversationController.ImportRequest();
        request.setContent(jsonl);
        ConversationImportPreviewStore.Preview preview = controller.importPreview(request).getData();
        assertNotNull(preview);

        ConversationController.ImportRequest confirmed = new ConversationController.ImportRequest();
        confirmed.setPreviewToken(preview.token());
        confirmed.setContent(jsonl);
        when(service.create(eq(new TenantId(7L)), eq(8L), anyString(), any(), any(), any(), any())).thenReturn(conversation);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        assertEquals(conversation, controller.importConversation(confirmed).getData());
        verify(service).appendMessage(eq(conversation), any(), eq(MessageRole.USER), eq("hello"), isNull(), isNull());
    }

    @Test
    void rejectsMissingActorAndInvalidPayload() {
        UserContextHolder.clearContext();
        assertThrows(RuntimeException.class, () -> controller.list(20, 0));
        installActor();
        ConversationController.MessageRequest messageRequest = new ConversationController.MessageRequest();
        assertThrows(RuntimeException.class, () -> controller.message("c1", null, messageRequest));
        ConversationController.ImportRequest invalid = new ConversationController.ImportRequest();
        assertThrows(RuntimeException.class, () -> controller.importPreview(invalid));
    }

    @Test
    void previewsPromptBranchesAndMapsImportFormats() {
        Conversation rag = new Conversation("c1", 7, 8, "knowledge-rag", "Chat", ConversationStatus.ACTIVE,
                null, null, "m1", null, "OPENAI", "gpt", 0, now, now);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(rag));
        when(conversations.listMessages("c1", new TenantId(7), 8, 1000)).thenReturn(List.of(message));
        var context = new com.shiyu.ai.runtime.ContextItem("doc", "d1", "reference", 0.9, null, List.of(), "tenant", now);
        when(prompts.assemble(eq(rag), anyList(), eq(new TenantId(7L)), eq(8L))).thenReturn(new ConversationPromptService.PromptAssembly(
                List.of(message), List.of(com.shiyu.ai.model.chat.ChatMessage.text("user", "hello")),
                List.of(context), new com.shiyu.ai.runtime.ContextTrace("trace", new TenantId(7), "hello", List.of("d1"), "rag", now)));
        assertEquals(2, controller.promptPreview("c1").getData().sources().size());

        when(conversations.findMessage("m1", new TenantId(7), 8)).thenReturn(Optional.of(message));
        when(service.branch(rag, "m1")).thenReturn(rag);
        assertEquals(rag, controller.branch("c1", "m1").getData());
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.branch("c1", "other"));

        ConversationController.ImportRequest markdown = new ConversationController.ImportRequest();
        markdown.setFormat("markdown"); markdown.setContent("## User\nhello\n");
        var markdownPreview = controller.importPreview(markdown).getData();
        assertNotNull(markdownPreview);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.importPreview(null));
        ConversationController.UpdateConversationRequest status = new ConversationController.UpdateConversationRequest();
        status.setStatus("archived");
        when(conversations.updateConversation(any(), eq(0L))).thenReturn(0);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.update("c1", status));
    }

    @Test
    void handlesCreateAndMessageIdempotencyAndAdmissionBranches() {
        ConversationController.CreateConversationRequest create = new ConversationController.CreateConversationRequest();
        create.setTitle("Chat");
        when(idempotency.find(new TenantId(7), 8, "conversation.create", "key")).thenReturn(Optional.of("c1"));
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        assertEquals(conversation, controller.create("key", create).getData());

        when(idempotency.find(new TenantId(7), 8, "conversation.create", "new")).thenReturn(Optional.empty());
        when(service.create(new TenantId(7), 8, "general", "Chat", null, null, null)).thenReturn(conversation);
        when(idempotency.claim(new TenantId(7), 8, "conversation.create", "new", "c1")).thenReturn(false);
        assertEquals(conversation, controller.create("new", create).getData());

        ConversationController.MessageRequest request = new ConversationController.MessageRequest();
        request.setContent("next");
        GenerationRun run = new GenerationRun("g1", "c1", "m2", null, null, "OPENAI", "gpt", GenerationStatus.CREATED,
                0, 0, 0, null, -1, false, 0, now, now);
        ConversationMessage appended = new ConversationMessage("m2", "c1", "m1", null, MessageRole.USER,
                List.of(ContentPart.text("next")), Map.of(), MessageStatus.COMPLETED, 1, null, now, now);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(service.appendUserMessage(any(), eq("next"))).thenReturn(appended);
        when(service.createGeneration(any(), eq(appended), isNull(), isNull())).thenReturn(run);
        when(idempotency.claim(new TenantId(7), 8, "conversation.message:c1", "message-key", "g1")).thenReturn(true);
        assertEquals(run, controller.message("c1", "message-key", request).getData());
        verify(runner).start(run, new TenantId(7), 8);

        when(idempotency.find(new TenantId(7), 8, "conversation.message:c1", "existing")).thenReturn(Optional.of("g1"));
        when(generations.find("g1", new TenantId(7), 8)).thenReturn(Optional.of(run));
        assertEquals(run, controller.message("c1", "existing", request).getData());

        when(idempotency.find(new TenantId(7), 8, "conversation.message:c1", "denied")).thenReturn(Optional.empty());
        when(service.createGeneration(any(), eq(appended), isNull(), isNull())).thenThrow(new IllegalStateException("busy"));
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.message("c1", "denied", request));
    }

    @Test
    void coversIdempotencyConflictAndMutationFailureBranches() {
        ConversationController.CreateConversationRequest create = new ConversationController.CreateConversationRequest();
        create.setTitle("Chat");
        when(service.create(new TenantId(7), 8, "general", "Chat", null, null, null)).thenReturn(conversation);
        when(idempotency.find(new TenantId(7), 8, "conversation.create", "claim-race")).thenReturn(Optional.empty());
        when(idempotency.claim(new TenantId(7), 8, "conversation.create", "claim-race", "c1")).thenReturn(false);
        assertEquals(conversation, controller.create("claim-race", create).getData());

        ConversationController.UpdateConversationRequest update = new ConversationController.UpdateConversationRequest();
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(conversations.updateConversation(any(Conversation.class), eq(0L))).thenReturn(1);
        assertEquals(conversation.status(), controller.update("c1", update).getData().status());
        when(conversations.deleteConversation("c1", new TenantId(7), 8)).thenReturn(0);
        assertThrows(IllegalArgumentException.class, () -> controller.delete("c1"));

        ConversationController.MessageRequest request = new ConversationController.MessageRequest();
        request.setContent("admit");
        GenerationRun run = new GenerationRun("g-race", "c1", "m2", null, null, "OPENAI", "gpt", GenerationStatus.CREATED,
                0, 0, 0, null, -1, false, 0, now, now);
        when(service.appendUserMessage(any(), eq("admit"))).thenReturn(message);
        when(service.createGeneration(any(), eq(message), isNull(), isNull())).thenReturn(run);
        when(idempotency.claim(new TenantId(7), 8, "conversation.message:c1", "race", "g-race")).thenReturn(false);
        when(idempotency.find(new TenantId(7), 8, "conversation.message:c1", "race")).thenReturn(Optional.empty());
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.message("c1", "race", request));

        when(idempotency.claim(new TenantId(7), 8, "conversation.message:c1", "quota", "g-race")).thenReturn(true);
        doThrow(new GenerationAdmissionException("QUOTA_EXCEEDED")).when(runner).start(run, new TenantId(7), 8);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.message("c1", "quota", request));
    }

    @Test
    void validatesActiveLeafOwnershipAndImportRoleFallback() {
        ConversationController.ActiveLeafRequest body = new ConversationController.ActiveLeafRequest();
        body.setMessageId("m1");
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(conversations.findMessage("m1", new TenantId(7), 8)).thenReturn(Optional.of(message));
        assertTrue(controller.activeLeaf("c1", null, body).isSuccess());
        body.setMessageId(" ");
        assertThrows(IllegalArgumentException.class, () -> controller.activeLeaf("c1", null, body));
        when(conversations.findMessage("other", new TenantId(7), 8)).thenReturn(Optional.of(new ConversationMessage("other", "other-conversation", null, null,
                MessageRole.USER, List.of(ContentPart.text("x")), Map.of(), MessageStatus.COMPLETED, 0, null, now, now)));
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.activeLeaf("c1", "other", null));

        String unknownRole = "{\"role\":\"unknown\",\"contentParts\":[{\"type\":\"text\",\"text\":\"x\"}]}";
        ConversationController.ImportRequest preview = new ConversationController.ImportRequest();
        preview.setContent(unknownRole);
        var issued = controller.importPreview(preview).getData();
        ConversationController.ImportRequest confirm = new ConversationController.ImportRequest();
        confirm.setPreviewToken(issued.token()); confirm.setContent(unknownRole);
        when(service.create(eq(new TenantId(7L)), eq(8L), anyString(), any(), any(), any(), any())).thenReturn(conversation);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        assertEquals(conversation, controller.importConversation(confirm).getData());
        verify(service, atLeastOnce()).appendMessage(eq(conversation), any(), eq(MessageRole.USER), eq("x"), isNull(), isNull());
    }

    @Test
    void mapsConversationMessageToModelMessage() throws Exception {
        ConversationMessage rich = new ConversationMessage("m-rich", "c1", null, null, MessageRole.ASSISTANT,
                List.of(new ContentPart("image", "caption", "https://img", "image/png", Map.of())), Map.of(), MessageStatus.COMPLETED,
                0, null, now, now);
        Method method = ConversationController.class.getDeclaredMethod("toModelMessage", ConversationMessage.class);
        method.setAccessible(true);
        com.shiyu.ai.model.chat.ChatMessage mapped = (com.shiyu.ai.model.chat.ChatMessage) method.invoke(controller, rich);
        assertEquals("assistant", mapped.role());
        assertEquals("image", mapped.content().getFirst().type());
        assertTrue(new ConversationController.PromptPreview(List.of(), List.of(), 0, false, true,
                "local", Map.of(), "hash", null, null).contextItems().isEmpty());
        GenerationEvent defaults = new GenerationEvent("g1", 0, GenerationEventType.DELTA, null, null);
        assertEquals("", defaults.payload());
        assertNotNull(defaults.createdAt());
        var mismatchPreview = imports.issue(new TenantId(7), 8, "md", "# title", List.of());
        assertThrows(IllegalArgumentException.class, () -> imports.consume(new TenantId(7), 8, mismatchPreview.token(), "markdown", "# changed"));
        var storePreview = imports.issue(new TenantId(7), 8, "md", "# title", List.of());
        assertTrue(imports.consume(new TenantId(7), 8, storePreview.token(), "markdown", "# title").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> imports.consume(new TenantId(7), 8, storePreview.token(), "markdown", "# title"));
        try (org.mockito.MockedStatic<MessageDigest> digests = mockStatic(MessageDigest.class)) {
            digests.when(() -> MessageDigest.getInstance("SHA-256")).thenThrow(new NoSuchAlgorithmException("missing"));
            assertThrows(IllegalStateException.class, () -> imports.issue(new TenantId(7), 8, "jsonl", "payload", List.of()));
        }
    }
}
