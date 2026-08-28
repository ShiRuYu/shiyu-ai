package com.shiyu.ai.conversation.web;

import com.shiyu.ai.kernel.context.TenantId;

import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.conversation.ConversationService;
import com.shiyu.ai.conversation.GenerationRunner;
import com.shiyu.ai.conversation.chat.*;
import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.conversation.port.GenerationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChatProductControllerTest {
    private final ChatProductRepository repository = mock(ChatProductRepository.class);
    private final ConversationRepository conversations = mock(ConversationRepository.class);
    private final ConversationService conversationService = mock(ConversationService.class);
    private final GenerationRunner generationRunner = mock(GenerationRunner.class);
    private final GenerationRepository generations = mock(GenerationRepository.class);
    private final CharacterImportPreviewStore previews = new CharacterImportPreviewStore();
    private final ChatProductController controller = new ChatProductController(repository, conversations,
            conversationService, generationRunner, generations, previews);
    private final CharacterCardV2 card = new CharacterCardV2(null, "Ava", "desc", "scene", "hello",
            List.of("user: hi"), "system", Map.of(), 2);
    private final Instant now = Instant.now();

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setUserId(8L);
        context.setCurrentTenantId(7L);
        context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
    }

    @AfterEach
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void characterPersonaLorebookAndImportEndpointsUseTenantActor() throws Exception {
        CharacterAsset character = new CharacterAsset("ch1", 7, 8, card, "PRIVATE", now, now);
        when(repository.saveCharacter(any())).thenReturn(character);
        ChatProductController.CharacterRequest request = new ChatProductController.CharacterRequest();
        request.card = card;
        assertEquals(character, controller.createCharacter(request).getData());
        assertThrows(IllegalArgumentException.class, () -> controller.createCharacter(null));
        request.card = new CharacterCardV2(null, "", null, null, null, null, null, null, 2);
        assertThrows(IllegalArgumentException.class, () -> controller.createCharacter(request));
        when(repository.listCharacters(new TenantId(7), 8)).thenReturn(List.of(character));
        when(repository.findCharacterForAccess(new TenantId(7), 8, "ch1")).thenReturn(Optional.of(character));
        assertEquals(1, controller.characters().getData().size());
        assertEquals(character, controller.character("ch1").getData());
        assertThrows(IllegalArgumentException.class, () -> controller.character("missing"));
        controller.deleteCharacter("ch1");
        assertArrayEquals(CharacterCardCodec.toPng(card, null), controller.exportCharacter("ch1").getBody());

        byte[] json = CharacterCardCodec.toJson(card).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        MockMultipartFile jsonFile = new MockMultipartFile("file", "card.json", "application/json", json);
        var preview = controller.previewCharacterImport(jsonFile).getData();
        assertNotNull(preview.token());
        when(repository.saveCharacter(any())).thenReturn(character);
        assertEquals(character, controller.importCharacter(jsonFile, preview.token()).getData());
        assertThrows(IllegalArgumentException.class, () -> controller.importCharacter(jsonFile, preview.token()));

        Persona persona = new Persona("p1", 8, "Ava", "student", "warm", "PRIVATE", Map.of());
        PersonaAsset personaAsset = new PersonaAsset("p1", 7, 8, persona, now, now);
        when(repository.savePersona(any())).thenReturn(personaAsset);
        assertEquals(personaAsset, controller.createPersona(persona).getData());
        when(repository.listPersonas(new TenantId(7), 8)).thenReturn(List.of(personaAsset));
        when(repository.findPersona(new TenantId(7), 8, "p1")).thenReturn(Optional.of(personaAsset));
        assertEquals(1, controller.personas().getData().size());
        assertEquals(personaAsset, controller.persona("p1").getData());
        controller.deletePersona("p1");

        LorebookEntry entry = new LorebookEntry("l1", List.of("magic"), "context", 1, "before", 50, true);
        LorebookAsset lore = new LorebookAsset("l1", 7, 8, entry, now, now);
        when(repository.saveLorebook(any())).thenReturn(lore);
        assertEquals(lore, controller.createLorebook(entry).getData());
        when(repository.listLorebooks(new TenantId(7), 8)).thenReturn(List.of(lore));
        when(repository.findLorebook(new TenantId(7), 8, "l1")).thenReturn(Optional.of(lore));
        assertEquals(1, controller.lorebooks().getData().size());
        assertEquals(lore, controller.lorebook("l1").getData());
        controller.deleteLorebook("l1");
    }

    @Test
    void promptStudioValidatesVersionsVariablesAndRenders() {
        ChatProductController.PromptRequest create = new ChatProductController.PromptRequest();
        create.templateId = "welcome";
        create.version = 1;
        create.body = "Hello {{name}}";
        create.variableSchema = Map.of("name", "string");
        create.testCases = List.of("Hi {{name}}");
        PromptTemplateVersion draft = new PromptTemplateVersion("p1", "welcome", 1, "DRAFT", create.body,
                create.variableSchema, create.testCases, now, null);
        when(repository.listPrompts(new TenantId(7), 8, "welcome")).thenReturn(List.of());
        when(repository.savePrompt(any(), eq(new TenantId(7L)), eq(8L))).thenReturn(draft);
        assertEquals(draft, controller.createPrompt(create).getData());
        create.body = "{{undeclared}}";
        assertThrows(ResponseStatusException.class, () -> controller.createPrompt(create));
        when(repository.listPrompts(new TenantId(7), 8, "welcome")).thenReturn(List.of(draft));
        create.body = draft.body();
        assertThrows(ResponseStatusException.class, () -> controller.createPrompt(create));

        ChatProductController.PublishRequest publish = new ChatProductController.PublishRequest();
        publish.version = 1;
        PromptTemplateVersion published = new PromptTemplateVersion("p2", "welcome", 2, "PUBLISHED", draft.body(),
                draft.variableSchema(), draft.testCases(), now, now);
        when(repository.savePrompt(any(), eq(new TenantId(7L)), eq(8L))).thenReturn(published);
        assertEquals(published, controller.publishPrompt("welcome", publish).getData());
        publish.version = 0;
        assertThrows(ResponseStatusException.class, () -> controller.publishPrompt("welcome", publish));

        when(repository.listPrompts(new TenantId(7), 8, "welcome")).thenReturn(List.of(draft, published));
        ChatProductController.DiffRequest diff = new ChatProductController.DiffRequest();
        diff.fromVersion = 1; diff.toVersion = 2;
        assertNotNull(controller.diffPrompt("welcome", diff).getData());
        ChatProductController.PromptPreviewRequest preview = new ChatProductController.PromptPreviewRequest();
        preview.body = "Hi {{name}}"; preview.variables = Map.of("name", "Ava");
        assertEquals("Hi Ava", controller.preview(preview).getData().rendered());
        ChatProductController.PromptTestRequest test = new ChatProductController.PromptTestRequest();
        test.version = 1; test.variables = Map.of("name", "Ava");
        assertEquals(List.of("Hi Ava"), controller.testPrompt("welcome", test).getData().renderedCases());
    }

    @Test
    void groupEndpointsPlanTurnsAndRejectInvalidRuns() {
        GroupChat.Participant ava = new GroupChat.Participant("ava", "Ava", "ch1");
        GroupChat group = new GroupChat("g1", "Study", List.of(ava), SpeakerPolicy.MANUAL, 2, 100);
        GroupChatAsset asset = new GroupChatAsset("g1", 7, 8, group, now, now);
        when(repository.saveGroup(any())).thenReturn(asset);
        ChatProductController.GroupRequest request = new ChatProductController.GroupRequest();
        request.name = "Study"; request.participants = List.of(ava); request.speakerPolicy = SpeakerPolicy.MANUAL;
        assertEquals(asset, controller.createGroup(request).getData());
        when(repository.listGroups(new TenantId(7), 8)).thenReturn(List.of(asset));
        when(repository.findGroup(new TenantId(7), 8, "g1")).thenReturn(Optional.of(asset));
        assertEquals(1, controller.groups().getData().size());
        assertEquals(asset, controller.group("g1").getData());
        ChatProductController.TurnRequest turn = new ChatProductController.TurnRequest();
        turn.requestedSpeakerId = "ava";
        assertEquals("ava", controller.nextSpeaker("g1", turn).getData().participant().id());
        controller.deleteGroup("g1");

        ChatProductController.TurnRunRequest invalid = new ChatProductController.TurnRunRequest();
        assertThrows(ResponseStatusException.class, () -> controller.runTurn("g1", invalid));
        invalid.conversationId = "c1"; invalid.content = "hello";
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> controller.runTurn("g1", invalid));
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(new Conversation("c1", 7, 8,
                "chat", "Chat", ConversationStatus.ACTIVE, null, null, null, null, "OPENAI", "gpt", 0, now, now)));
        when(generations.hasRunningConversation("c1", new TenantId(7))).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> controller.runTurn("g1", invalid));
    }

    @Test
    void startsGroupTurnAndReturnsExhaustedPlanWithoutStartingProvider() {
        GroupChat.Participant ava = new GroupChat.Participant("ava", "Ava", "ch1");
        GroupChat group = new GroupChat("g1", "Study", List.of(ava), SpeakerPolicy.ROUND_ROBIN, 1, 100);
        GroupChatAsset asset = new GroupChatAsset("g1", 7, 8, group, now, now);
        when(repository.findGroup(new TenantId(7), 8, "g1")).thenReturn(Optional.of(asset));
        Conversation conversation = new Conversation("c1", 7, 8, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, null, null, "OPENAI", "gpt", 0, now, now);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(generations.hasRunningConversation("c1", new TenantId(7))).thenReturn(false);
        GenerationRun completed = new GenerationRun("old", "c1", "m", null, "ava", "OPENAI", "gpt",
                GenerationStatus.COMPLETED, 0, 10, 0, null, 1, false, 0, now, now);
        when(generations.listConversation("c1", new TenantId(7), 1000)).thenReturn(List.of(completed));
        ChatProductController.TurnRunRequest exhausted = new ChatProductController.TurnRunRequest();
        exhausted.conversationId = "c1"; exhausted.content = "next";
        assertTrue(controller.runTurn("g1", exhausted).getData().decision().exhausted());
        verifyNoInteractions(conversationService, generationRunner);

        GroupChat open = new GroupChat("g2", "Study", List.of(ava), SpeakerPolicy.ROUND_ROBIN, 2, 100);
        when(repository.findGroup(new TenantId(7), 8, "g2")).thenReturn(Optional.of(new GroupChatAsset("g2", 7, 8, open, now, now)));
        when(generations.listConversation("c1", new TenantId(7), 1000)).thenReturn(List.of());
        ConversationMessage input = new ConversationMessage("m1", "c1", null, null, MessageRole.USER,
                List.of(ContentPart.text("next")), Map.of(), MessageStatus.COMPLETED, 0, null, now, now);
        when(conversationService.appendUserMessage(conversation, "next")).thenReturn(input);
        when(conversations.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        GenerationRun created = new GenerationRun("g-new", "c1", "m1", null, "ava", "OPENAI", "gpt",
                GenerationStatus.CREATED, 0, 0, 0, null, -1, false, 0, now, now);
        when(conversationService.createGeneration(conversation, input, "OPENAI", "gpt", "ava")).thenReturn(created);
        exhausted = new ChatProductController.TurnRunRequest(); exhausted.conversationId = "c1"; exhausted.content = "next";
        assertEquals("g-new", controller.runTurn("g2", exhausted).getData().generationId());
        verify(generationRunner).start(created, new TenantId(7), 8);
    }

    @Test
    void coversPngImportExportPromptEdgeCasesAndAdmissionErrors() throws Exception {
        byte[] png = CharacterCardCodec.toPng(card, null);
        MockMultipartFile pngFile = new MockMultipartFile("file", "card.png", "image/png", png);
        var pngPreview = controller.previewCharacterImport(pngFile).getData();
        assertNotNull(pngPreview.card());
        CharacterAsset storedPng = new CharacterAsset("png", 7, 8, card, "PRIVATE", png, now, now);
        when(repository.saveCharacter(any())).thenReturn(storedPng);
        assertEquals(storedPng, controller.importCharacter(pngFile, pngPreview.token()).getData());
        when(repository.findCharacterForAccess(new TenantId(7), 8, "png")).thenReturn(Optional.of(storedPng));
        assertArrayEquals(png, controller.exportCharacter("png").getBody());

        ChatProductController.PromptRequest publishedRequest = new ChatProductController.PromptRequest();
        publishedRequest.templateId = "published"; publishedRequest.version = 1; publishedRequest.status = "PUBLISHED";
        publishedRequest.body = "hello"; publishedRequest.testCases = List.of(); publishedRequest.variableSchema = Map.of();
        when(repository.listPrompts(new TenantId(7), 8, "published")).thenReturn(List.of());
        when(repository.savePrompt(any(), eq(new TenantId(7L)), eq(8L))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals("PUBLISHED", controller.createPrompt(publishedRequest).getData().status());

        ChatProductController.PromptPreviewRequest preview = new ChatProductController.PromptPreviewRequest();
        assertEquals("", controller.preview(preview).getData().rendered());
        ChatProductController.PromptTestRequest test = new ChatProductController.PromptTestRequest();
        assertThrows(ResponseStatusException.class, () -> controller.testPrompt("published", test));
        test.version = 1;
        when(repository.listPrompts(new TenantId(7), 8, "published")).thenReturn(List.of(new PromptTemplateVersion("p", "published", 1, "DRAFT", "{{x}}", Map.of("x", "string"), List.of("{{x}}"), now, null)));
        assertEquals(List.of("{{x}}"), controller.testPrompt("published", test).getData().renderedCases());

        ChatProductController.DiffRequest diff = new ChatProductController.DiffRequest(); diff.fromVersion = 1; diff.toVersion = 2;
        PromptTemplateVersion from = new PromptTemplateVersion("p1", "published", 1, "DRAFT", "same\nold", Map.of(), List.of(), now, null);
        PromptTemplateVersion to = new PromptTemplateVersion("p2", "published", 2, "DRAFT", "same\nnew\nextra", Map.of(), List.of(), now, null);
        when(repository.listPrompts(new TenantId(7), 8, "published")).thenReturn(List.of(from, to));
        assertEquals(3, controller.diffPrompt("published", diff).getData().changes().size());

        when(repository.findGroup(new TenantId(7), 8, "g1")).thenReturn(Optional.of(new GroupChatAsset("g1", 7, 8,
                new GroupChat("g1", "Study", List.of(new GroupChat.Participant("ava", "Ava", "ch1")), SpeakerPolicy.ROUND_ROBIN, 2, 100), now, now)));
        assertNotNull(controller.nextSpeaker("g1", null).getData());
        assertThrows(IllegalArgumentException.class, () -> controller.group("missing"));
    }

    @Test
    void rejectsMissingModelAndMapsGenerationAdmissionConflict() {
        GroupChat.Participant ava = new GroupChat.Participant("ava", "Ava", "ch1");
        GroupChat group = new GroupChat("g3", "Study", List.of(ava), SpeakerPolicy.ROUND_ROBIN, 2, 100);
        when(repository.findGroup(new TenantId(7), 8, "g3")).thenReturn(Optional.of(new GroupChatAsset("g3", 7, 8, group, now, now)));
        Conversation noModel = new Conversation("c3", 7, 8, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, null, null, null, null, 0, now, now);
        when(conversations.findConversation("c3", new TenantId(7), 8)).thenReturn(Optional.of(noModel));
        when(generations.hasRunningConversation("c3", new TenantId(7))).thenReturn(false);
        when(generations.listConversation("c3", new TenantId(7), 1000)).thenReturn(List.of());
        ChatProductController.TurnRunRequest request = new ChatProductController.TurnRunRequest(); request.conversationId = "c3"; request.content = "hello";
        assertThrows(ResponseStatusException.class, () -> controller.runTurn("g3", request));
    }

    @Test
    void coversGroupTurnValidationPlatformOverridesAndAdmissionFailures() {
        GroupChat.Participant ava = new GroupChat.Participant("ava", "Ava", "ch1");
        GroupChat group = new GroupChat("g4", "Study", List.of(ava), SpeakerPolicy.ROUND_ROBIN, 3, 100);
        GroupChatAsset asset = new GroupChatAsset("g4", 7, 8, group, now, now);
        Conversation conversation = new Conversation("c4", 7, 8, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, null, null, "OPENAI", "gpt", 0, now, now);
        when(repository.findGroup(new TenantId(7), 8, "g4")).thenReturn(Optional.of(asset));
        when(conversations.findConversation("c4", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(generations.hasRunningConversation("c4", new TenantId(7))).thenReturn(false);
        GenerationRun ignoredFailed = new GenerationRun("failed", "c4", "m", null, "ava", "OPENAI", "gpt",
                GenerationStatus.FAILED, 0, 4, 0, "ERR", 1, false, 0, now, now);
        GenerationRun ignoredNoSpeaker = new GenerationRun("no-speaker", "c4", "m", null, null, "OPENAI", "gpt",
                GenerationStatus.COMPLETED, 0, 5, 0, null, 1, false, 0, now, now);
        when(generations.listConversation("c4", new TenantId(7), 1000)).thenReturn(List.of(ignoredFailed, ignoredNoSpeaker));

        ChatProductController.TurnRunRequest missingContent = new ChatProductController.TurnRunRequest();
        missingContent.conversationId = "c4";
        assertThrows(ResponseStatusException.class, () -> controller.runTurn("g4", missingContent));
        ChatProductController.TurnRunRequest explicit = new ChatProductController.TurnRunRequest();
        explicit.conversationId = "c4"; explicit.content = "hello"; explicit.platform = "ANTHROPIC"; explicit.model = "claude";
        ConversationMessage input = new ConversationMessage("m4", "c4", null, null, MessageRole.USER,
                List.of(ContentPart.text("hello")), Map.of(), MessageStatus.COMPLETED, 0, null, now, now);
        GenerationRun run = new GenerationRun("run4", "c4", "m4", null, "ava", "ANTHROPIC", "claude",
                GenerationStatus.CREATED, 0, 0, 0, null, -1, false, 0, now, now);
        when(conversationService.appendUserMessage(conversation, "hello")).thenReturn(input);
        when(conversationService.createGeneration(conversation, input, "ANTHROPIC", "claude", "ava")).thenReturn(run);
        assertEquals("run4", controller.runTurn("g4", explicit).getData().generationId());
        verify(generationRunner).start(run, new TenantId(7), 8);

        when(conversationService.appendUserMessage(conversation, "boom")).thenThrow(new IllegalStateException("duplicate"));
        explicit.content = "boom";
        assertThrows(ResponseStatusException.class, () -> controller.runTurn("g4", explicit));

        AtomicInteger latestLookup = new AtomicInteger();
        when(conversations.findConversation("c4", new TenantId(7), 8)).thenAnswer(invocation ->
                latestLookup.getAndIncrement() == 0 ? Optional.of(conversation) : Optional.empty());
        when(conversationService.appendUserMessage(conversation, "missing latest")).thenReturn(input);
        explicit.content = "missing latest";
        assertThrows(IllegalArgumentException.class, () -> controller.runTurn("g4", explicit));

        when(conversations.findConversation("c4", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(conversationService.appendUserMessage(conversation, "quota")).thenReturn(input);
        when(conversationService.createGeneration(conversation, input, "ANTHROPIC", "claude", "ava")).thenReturn(run);
        doThrow(new com.shiyu.ai.conversation.GenerationAdmissionException("QUOTA_EXCEEDED"))
                .when(generationRunner).start(run, new TenantId(7), 8);
        explicit.content = "quota";
        assertThrows(ResponseStatusException.class, () -> controller.runTurn("g4", explicit));
        assertThrows(IllegalArgumentException.class, () -> controller.runTurn("missing", explicit));
    }
}
