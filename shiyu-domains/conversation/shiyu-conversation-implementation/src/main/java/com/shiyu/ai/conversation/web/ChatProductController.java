package com.shiyu.ai.conversation.web;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.conversation.chat.*;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.conversation.ConversationService;
import com.shiyu.ai.conversation.GenerationRunner;
import com.shiyu.ai.conversation.domain.Conversation;
import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.conversation.port.GenerationRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

@Tag(name = "Chat Product Assets")
@RestController
@RequestMapping("/api/conversation/chat-products")
public class ChatProductController {
    private final ChatProductRepository repository;
    private final ConversationRepository conversations;
    private final ConversationService conversationService;
    private final GenerationRunner generationRunner;
    private final GenerationRepository generationRepository;
    private final CharacterImportPreviewStore characterImportPreviews;
    public ChatProductController(ChatProductRepository repository, ConversationRepository conversations,
                                  ConversationService conversationService, GenerationRunner generationRunner,
                                  GenerationRepository generationRepository, CharacterImportPreviewStore characterImportPreviews) {
        this.repository = repository;
        this.conversations = conversations;
        this.conversationService = conversationService;
        this.generationRunner = generationRunner;
        this.generationRepository = generationRepository;
        this.characterImportPreviews = characterImportPreviews;
    }

    @PostMapping("/characters")
    public Result<CharacterAsset> createCharacter(@RequestBody CharacterRequest request) {
        if (request == null || request.card == null || request.card.name() == null || request.card.name().isBlank()) throw new IllegalArgumentException("character card name is required");
        Instant now = Instant.now();
        return Result.success(repository.saveCharacter(new CharacterAsset(id(), tenant(), user(), request.card, request.visibility, now, now)));
    }
    @GetMapping("/characters") public Result<List<CharacterAsset>> characters() { return Result.success(repository.listCharacters(tenantId(), user())); }
    @GetMapping("/characters/{id}") public Result<CharacterAsset> character(@PathVariable String id) { return Result.success(repository.findCharacterForAccess(tenantId(), user(), id).orElseThrow(() -> new IllegalArgumentException("character not found"))); }
    @DeleteMapping("/characters/{id}") public Result<Void> deleteCharacter(@PathVariable String id) { repository.deleteCharacter(tenantId(), user(), id); return Result.success(); }

    @PostMapping(value = "/characters/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<CharacterImportPreviewStore.Preview> previewCharacterImport(@RequestPart("file") MultipartFile file) throws Exception {
        byte[] original = file.getBytes();
        CharacterCardV2 card = file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".png")
                ? CharacterCardCodec.fromPng(original) : CharacterCardCodec.fromJson(new String(original, java.nio.charset.StandardCharsets.UTF_8));
        return Result.success(characterImportPreviews.issue(new TenantId(tenant()), user(), original, file.getOriginalFilename(), card));
    }

    @PostMapping(value = "/characters/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<CharacterAsset> importCharacter(@RequestPart("file") MultipartFile file, @RequestParam String previewToken) throws Exception {
        byte[] original = file.getBytes();
        CharacterCardV2 card = characterImportPreviews.consume(new TenantId(tenant()), user(), previewToken, original, file.getOriginalFilename());
        Instant now = Instant.now(); return Result.success(repository.saveCharacter(new CharacterAsset(id(), tenant(), user(), card, "PRIVATE", file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".png") ? original : null, now, now)));
    }
    @GetMapping(value = "/characters/{id}/png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> exportCharacter(@PathVariable String id) throws Exception {
        CharacterAsset asset = repository.findCharacterForAccess(tenantId(), user(), id).orElseThrow(() -> new IllegalArgumentException("character not found"));
        byte[] png = asset.pngData() == null ? CharacterCardCodec.toPng(asset.card(), new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB)) : asset.pngData();
        return ResponseEntity.ok(png);
    }

    @PostMapping("/personas")
    public Result<PersonaAsset> createPersona(@RequestBody Persona persona) { Instant now = Instant.now(); return Result.success(repository.savePersona(new PersonaAsset(id(), tenant(), user(), persona, now, now))); }
    @GetMapping("/personas") public Result<List<PersonaAsset>> personas() { return Result.success(repository.listPersonas(tenantId(), user())); }
    @GetMapping("/personas/{id}") public Result<PersonaAsset> persona(@PathVariable String id) { return Result.success(repository.findPersona(tenantId(), user(), id).orElseThrow(() -> new IllegalArgumentException("persona not found"))); }
    @DeleteMapping("/personas/{id}") public Result<Void> deletePersona(@PathVariable String id) { repository.deletePersona(tenantId(), user(), id); return Result.success(); }

    @PostMapping("/lorebooks")
    public Result<LorebookAsset> createLorebook(@RequestBody LorebookEntry entry) { Instant now = Instant.now(); return Result.success(repository.saveLorebook(new LorebookAsset(id(), tenant(), user(), entry, now, now))); }
    @GetMapping("/lorebooks") public Result<List<LorebookAsset>> lorebooks() { return Result.success(repository.listLorebooks(tenantId(), user())); }
    @GetMapping("/lorebooks/{id}") public Result<LorebookAsset> lorebook(@PathVariable String id) { return Result.success(repository.findLorebook(tenantId(), user(), id).orElseThrow(() -> new IllegalArgumentException("lorebook entry not found"))); }
    @DeleteMapping("/lorebooks/{id}") public Result<Void> deleteLorebook(@PathVariable String id) { repository.deleteLorebook(tenantId(), user(), id); return Result.success(); }

    @PostMapping("/prompt-studio/templates")
    public Result<PromptTemplateVersion> createPrompt(@RequestBody PromptRequest request) {
        if (request == null || request.templateId == null || request.templateId.isBlank() || request.body == null) throw new IllegalArgumentException("templateId and body are required");
        List<PromptTemplateVersion> revisions = repository.listPrompts(tenantId(), user(), request.templateId);
        if (revisions.stream().anyMatch(v -> v.version() == request.version)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "prompt version already exists");
        validateVariables(request.body, request.variableSchema);
        PromptTemplateVersion version = new PromptTemplateVersion(id(), request.templateId, request.version, request.status, request.body, request.variableSchema, request.testCases, Instant.now(), "PUBLISHED".equals(request.status) ? Instant.now() : null);
        return Result.success(repository.savePrompt(version, tenantId(), user()));
    }
    @GetMapping("/prompt-studio/templates") public Result<List<PromptTemplateVersion>> prompts(@RequestParam(required = false) String templateId) { return Result.success(repository.listPrompts(tenantId(), user(), templateId)); }

    @PostMapping("/prompt-studio/templates/{templateId}/publish")
    public Result<PromptTemplateVersion> publishPrompt(@PathVariable String templateId, @RequestBody PublishRequest request) {
        if (request == null || request.version < 1) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "draft version is required");
        PromptTemplateVersion draft = repository.listPrompts(tenantId(), user(), templateId).stream()
                .filter(v -> v.version() == request.version && "DRAFT".equals(v.status())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("draft prompt version not found"));
        int nextVersion = repository.listPrompts(tenantId(), user(), templateId).stream().mapToInt(PromptTemplateVersion::version).max().orElse(0) + 1;
        PromptTemplateVersion published = new PromptTemplateVersion(id(), templateId, nextVersion, "PUBLISHED", draft.body(), draft.variableSchema(), draft.testCases(), Instant.now(), Instant.now());
        return Result.success(repository.savePrompt(published, tenantId(), user()));
    }

    @PostMapping("/prompt-studio/templates/{templateId}/diff")
    public Result<PromptDiff> diffPrompt(@PathVariable String templateId, @RequestBody DiffRequest request) {
        if (request == null || request.fromVersion < 1 || request.toVersion < 1) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "prompt versions are required");
        List<PromptTemplateVersion> revisions = repository.listPrompts(tenantId(), user(), templateId);
        PromptTemplateVersion from = revisions.stream().filter(v -> v.version() == request.fromVersion).findFirst().orElseThrow(() -> new IllegalArgumentException("source prompt version not found"));
        PromptTemplateVersion to = revisions.stream().filter(v -> v.version() == request.toVersion).findFirst().orElseThrow(() -> new IllegalArgumentException("target prompt version not found"));
        List<String> before = List.of(from.body().split("\\R", -1));
        List<String> after = List.of(to.body().split("\\R", -1));
        List<String> changes = new java.util.ArrayList<>();
        int max = Math.max(before.size(), after.size());
        for (int i = 0; i < max; i++) {
            String oldLine = i < before.size() ? before.get(i) : null;
            String newLine = i < after.size() ? after.get(i) : null;
            if (!java.util.Objects.equals(oldLine, newLine)) { if (oldLine != null) changes.add("- " + oldLine); if (newLine != null) changes.add("+ " + newLine); }
        }
        return Result.success(new PromptDiff(templateId, from.version(), to.version(), changes));
    }

    @PostMapping("/prompt-studio/preview")
    public Result<PromptPreview> preview(@RequestBody PromptPreviewRequest request) {
        String rendered = request.body == null ? "" : request.body;
        if (request.variables != null) for (var entry : request.variables.entrySet()) rendered = rendered.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        return Result.success(new PromptPreview(rendered, request.variables == null ? List.of() : request.variables.keySet().stream().sorted().toList(), Math.max(1, rendered.length() / 4)));
    }

    /** Runs the immutable revision's saved samples without mutating or publishing it. */
    @PostMapping("/prompt-studio/templates/{templateId}/test")
    public Result<PromptTestRun> testPrompt(@PathVariable String templateId, @RequestBody PromptTestRequest request) {
        if (request == null || request.version < 1) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "prompt version is required");
        PromptTemplateVersion version = repository.listPrompts(tenantId(), user(), templateId).stream()
                .filter(item -> item.version() == request.version)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("prompt version not found"));
        java.util.Map<String, Object> variables = request.variables == null ? java.util.Map.of() : request.variables;
        List<String> rendered = version.testCases().stream().map(sample -> render(sample, variables)).toList();
        return Result.success(new PromptTestRun(templateId, version.version(), rendered, Math.max(1, rendered.stream().mapToInt(String::length).sum() / 4)));
    }

    @PostMapping("/groups")
    public Result<GroupChatAsset> createGroup(@RequestBody GroupRequest request) {
        Instant now = Instant.now();
        GroupChat group = new GroupChat(id(), request.name, request.participants, request.speakerPolicy, request.maxTurns, request.tokenBudget);
        return Result.success(repository.saveGroup(new GroupChatAsset(group.id(), tenant(), user(), group, now, now)));
    }
    @GetMapping("/groups") public Result<List<GroupChatAsset>> groups() { return Result.success(repository.listGroups(tenantId(), user())); }
    @GetMapping("/groups/{id}") public Result<GroupChatAsset> group(@PathVariable String id) { return Result.success(repository.findGroup(tenantId(), user(), id).orElseThrow(() -> new IllegalArgumentException("group not found"))); }
    @DeleteMapping("/groups/{id}") public Result<Void> deleteGroup(@PathVariable String id) { repository.deleteGroup(tenantId(), user(), id); return Result.success(); }

    @PostMapping("/groups/{id}/next-speaker")
    public Result<GroupTurnPlanner.TurnDecision> nextSpeaker(@PathVariable String id, @RequestBody(required = false) TurnRequest request) {
        GroupChatAsset asset = repository.findGroup(tenantId(), user(), id).orElseThrow(() -> new IllegalArgumentException("group not found"));
        TurnRequest input = request == null ? new TurnRequest() : request;
        return Result.success(GroupTurnPlanner.next(asset.group(), input.completedSpeakerIds, input.requestedSpeakerId, input.consumedTokens));
    }

    /** Selects one bounded speaker turn and immediately starts its durable GenerationRun. */
    @PostMapping("/groups/{id}/turn")
    public Result<GroupTurnRun> runTurn(@PathVariable String id, @RequestBody TurnRunRequest request) {
        if (request == null || request.conversationId == null || request.conversationId.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "conversationId is required");
        }
        if (request.content == null || request.content.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "content is required");
        }
        GroupChatAsset asset = repository.findGroup(tenantId(), user(), id)
                .orElseThrow(() -> new IllegalArgumentException("group not found"));
        Conversation conversation = conversations.findConversation(request.conversationId, new com.shiyu.ai.kernel.context.TenantId(tenant()), user())
                .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        if (generationRepository.hasRunningConversation(request.conversationId, new com.shiyu.ai.kernel.context.TenantId(tenant()))) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "a group turn is already running");
        }
        List<com.shiyu.ai.conversation.domain.GenerationRun> priorRuns = generationRepository.listConversation(request.conversationId, new com.shiyu.ai.kernel.context.TenantId(tenant()), 1000);
        List<String> completedSpeakers = priorRuns.stream()
                .filter(run -> run.status() == com.shiyu.ai.conversation.domain.GenerationStatus.COMPLETED && run.speakerId() != null)
                .map(com.shiyu.ai.conversation.domain.GenerationRun::speakerId).toList();
        int consumedTokens = priorRuns.stream()
                .filter(run -> run.status() == com.shiyu.ai.conversation.domain.GenerationStatus.COMPLETED && run.speakerId() != null)
                .mapToLong(com.shiyu.ai.conversation.domain.GenerationRun::completionTokens).mapToInt(value -> (int) Math.min(Integer.MAX_VALUE, Math.max(0, value))).sum();
        GroupTurnPlanner.TurnDecision decision = GroupTurnPlanner.next(asset.group(), completedSpeakers,
                request.requestedSpeakerId, consumedTokens);
        if (decision.exhausted()) return Result.success(new GroupTurnRun(decision, null, null));

        String platform = request.platform == null || request.platform.isBlank() ? conversation.platform() : request.platform;
        String model = request.model == null || request.model.isBlank() ? conversation.model() : request.model;
        if (platform == null || platform.isBlank() || model == null || model.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "platform and model are required");
        }
        GenerationRun run;
        try {
            var input = conversationService.appendUserMessage(conversation, request.content);
            var latestConversation = conversations.findConversation(request.conversationId, new com.shiyu.ai.kernel.context.TenantId(tenant()), user())
                    .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
            run = conversationService.createGeneration(latestConversation, input, platform, model, decision.participant().id());
        } catch (IllegalStateException ex) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
        try { generationRunner.start(run, new TenantId(tenant()), user()); }
        catch (com.shiyu.ai.conversation.GenerationAdmissionException denied) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, denied.errorCode(), denied);
        }
        return Result.success(new GroupTurnRun(decision, run.id(), run.speakerId()));
    }

    private String id() { return UUID.randomUUID().toString(); }
    private TenantId tenantId() { return new TenantId(ActorContextHttpAdapter.tenantId()); }
    private long tenant() { return tenantId().value(); }
    private long user() { return ActorContextHttpAdapter.userId(); }

    public static class CharacterRequest { public CharacterCardV2 card; public String visibility = "PRIVATE"; }
    public static class PromptRequest { public String templateId; public int version = 1; public String status = "DRAFT"; public String body; public java.util.Map<String,String> variableSchema; public List<String> testCases; }
    public static class PublishRequest { public int version; }
    public static class DiffRequest { public int fromVersion; public int toVersion; }
    public static class GroupRequest { public String name; public List<GroupChat.Participant> participants; public SpeakerPolicy speakerPolicy = SpeakerPolicy.MANUAL; public int maxTurns = 20; public int tokenBudget = 4000; }
    public static class TurnRequest { public List<String> completedSpeakerIds = List.of(); public String requestedSpeakerId; public int consumedTokens; }
    public static class TurnRunRequest {
        public String conversationId;
        public String content;
        public String platform;
        public String model;
        public List<String> completedSpeakerIds = List.of();
        public String requestedSpeakerId;
        public int consumedTokens;
    }
    public record GroupTurnRun(GroupTurnPlanner.TurnDecision decision, String generationId, String speakerId) { }
    public static class PromptPreviewRequest { public String body; public java.util.Map<String, Object> variables; }
    public static class PromptTestRequest { public int version; public java.util.Map<String, Object> variables; }
    public record PromptPreview(String rendered, List<String> variables, int estimatedTokens) { }
    public record PromptTestRun(String templateId, int version, List<String> renderedCases, int estimatedTokens) { }
    public record PromptDiff(String templateId, int fromVersion, int toVersion, List<String> changes) { }

    private static String render(String body, java.util.Map<String, Object> variables) {
        String rendered = body == null ? "" : body;
        for (var entry : variables.entrySet()) rendered = rendered.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        return rendered;
    }

    private static void validateVariables(String body, java.util.Map<String, String> schema) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{\\{\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*}}")
                .matcher(body == null ? "" : body);
        java.util.Set<String> declared = schema == null ? java.util.Set.of() : schema.keySet();
        while (matcher.find()) if (!declared.contains(matcher.group(1))) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "undeclared prompt variable: " + matcher.group(1));
    }
}
