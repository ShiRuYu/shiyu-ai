package com.shiyu.ai.conversation.implementation.web.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.conversation.implementation.application.ConversationService;
import com.shiyu.ai.conversation.implementation.application.GenerationRunner;
import com.shiyu.ai.conversation.implementation.domain.chat.*;
import com.shiyu.ai.conversation.implementation.domain.model.Conversation;
import com.shiyu.ai.conversation.implementation.domain.port.ChatProductRepository;
import com.shiyu.ai.conversation.implementation.domain.port.ConversationRepository;
import com.shiyu.ai.conversation.implementation.domain.port.GenerationRepository;
import com.shiyu.ai.conversation.implementation.web.support.imports.CharacterImportPreviewStore;
import com.shiyu.ai.kernel.context.TenantId;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * {@code ChatProductController} 是会话模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Tag(name = "Chat Product Assets")
@RestController
@RequestMapping("/api/conversation/chat-products")
public class ChatProductController {
    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final ChatProductRepository repository;
    /**
     * conversations 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ConversationRepository conversations;
    /**
     * conversationService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ConversationService conversationService;
    /**
     * generationRunner 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final GenerationRunner generationRunner;
    /**
     * 生成仓储，表示当前对象中的对应属性。
     */
    private final GenerationRepository generationRepository;
    /**
     * characterImportPreviews 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final CharacterImportPreviewStore characterImportPreviews;

    /**
     * {@code ChatProductController} 创建并初始化当前类型实例。
     *
     * @param repository 参数值，用于执行当前操作。
     * @param conversations 参数值，用于执行当前操作。
     * @param conversationService 参数值，用于执行当前操作。
     * @param generationRunner 参数值，用于执行当前操作。
     * @param generationRepository 参数值，用于执行当前操作。
     * @param characterImportPreviews 参数值，用于执行当前操作。
     */
    public ChatProductController(
            ChatProductRepository repository,
            ConversationRepository conversations,
            ConversationService conversationService,
            GenerationRunner generationRunner,
            GenerationRepository generationRepository,
            CharacterImportPreviewStore characterImportPreviews) {
        this.repository = repository;
        this.conversations = conversations;
        this.conversationService = conversationService;
        this.generationRunner = generationRunner;
        this.generationRepository = generationRepository;
        this.characterImportPreviews = characterImportPreviews;
    }

    /**
     * {@code createCharacter} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/characters")
    public Result<CharacterAsset> createCharacter(@RequestBody CharacterRequest request) {
        if (request == null
                || request.card == null
                || request.card.name() == null
                || request.card.name().isBlank())
            throw new IllegalArgumentException("character card name is required");
        Instant now = Instant.now();
        return Result.success(
                repository.saveCharacter(
                        new CharacterAsset(
                                id(),
                                tenant(),
                                user(),
                                request.card,
                                request.visibility,
                                now,
                                now)));
    }

    /**
     * {@code characters} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/characters")
    public Result<List<CharacterAsset>> characters() {
        return Result.success(repository.listCharacters(tenantId(), user()));
    }

    /**
     * {@code character} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/characters/{id}")
    public Result<CharacterAsset> character(@PathVariable String id) {
        return Result.success(
                repository
                        .findCharacterForAccess(tenantId(), user(), id)
                        .orElseThrow(() -> new IllegalArgumentException("character not found")));
    }

    /**
     * {@code deleteCharacter} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @DeleteMapping("/characters/{id}")
    public Result<Void> deleteCharacter(@PathVariable String id) {
        repository.deleteCharacter(tenantId(), user(), id);
        return Result.success();
   }

    /**
     * {@code previewCharacterImport} 执行当前类型定义的业务操作。
     *
     * @param file 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
   @PostMapping(
           value = "/characters/import/preview",
           consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   public Result<CharacterImportPreviewStore.Preview> previewCharacterImport(
           @RequestPart("file") MultipartFile file) throws Exception {
        byte[] original = file.getBytes();
        CharacterCardV2 card =
                file.getOriginalFilename() != null
                                && file.getOriginalFilename().toLowerCase().endsWith(".png")
                        ? CharacterCardCodec.fromPng(original)
                        : CharacterCardCodec.fromJson(
                                new String(original, java.nio.charset.StandardCharsets.UTF_8));
        return Result.success(
                characterImportPreviews.issue(
                        new TenantId(tenant()),
                        user(),
                        original,
                        file.getOriginalFilename(),
                        card));
    }

    /**
     * {@code importCharacter} 执行当前类型定义的业务操作。
     *
     * @param file 参数值，用于执行当前操作。
     * @param previewToken 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping(value = "/characters/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<CharacterAsset> importCharacter(
            @RequestPart("file") MultipartFile file, @RequestParam String previewToken)
            throws Exception {
        byte[] original = file.getBytes();
        CharacterCardV2 card =
                characterImportPreviews.consume(
                        new TenantId(tenant()),
                        user(),
                        previewToken,
                        original,
                        file.getOriginalFilename());
        Instant now = Instant.now();
        return Result.success(
                repository.saveCharacter(
                        new CharacterAsset(
                                id(),
                                tenant(),
                                user(),
                                card,
                                "PRIVATE",
                                file.getOriginalFilename() != null
                                                && file.getOriginalFilename()
                                                        .toLowerCase()
                                                        .endsWith(".png")
                                        ? original
                                        : null,
                                now,
                                now)));
    }

    /**
     * {@code exportCharacter} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping(value = "/characters/{id}/png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> exportCharacter(@PathVariable String id) throws Exception {
        CharacterAsset asset =
                repository
                        .findCharacterForAccess(tenantId(), user(), id)
                        .orElseThrow(() -> new IllegalArgumentException("character not found"));
        byte[] png =
                asset.pngData() == null
                        ? CharacterCardCodec.toPng(
                                asset.card(),
                                new java.awt.image.BufferedImage(
                                        1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB))
                        : asset.pngData();
        return ResponseEntity.ok(png);
    }

    /**
     * {@code createPersona} 写入或更新当前模块中的业务数据。
     *
     * @param persona 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/personas")
    public Result<PersonaAsset> createPersona(@RequestBody Persona persona) {
        Instant now = Instant.now();
        return Result.success(
                repository.savePersona(
                        new PersonaAsset(id(), tenant(), user(), persona, now, now)));
    }

    /**
     * {@code personas} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/personas")
    public Result<List<PersonaAsset>> personas() {
        return Result.success(repository.listPersonas(tenantId(), user()));
    }

    /**
     * {@code persona} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/personas/{id}")
    public Result<PersonaAsset> persona(@PathVariable String id) {
        return Result.success(
                repository
                        .findPersona(tenantId(), user(), id)
                        .orElseThrow(() -> new IllegalArgumentException("persona not found")));
    }

    /**
     * {@code deletePersona} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @DeleteMapping("/personas/{id}")
    public Result<Void> deletePersona(@PathVariable String id) {
        repository.deletePersona(tenantId(), user(), id);
        return Result.success();
    }

    /**
     * {@code createLorebook} 写入或更新当前模块中的业务数据。
     *
     * @param entry 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/lorebooks")
    public Result<LorebookAsset> createLorebook(@RequestBody LorebookEntry entry) {
        Instant now = Instant.now();
        return Result.success(
                repository.saveLorebook(
                        new LorebookAsset(id(), tenant(), user(), entry, now, now)));
    }

    /**
     * {@code lorebooks} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/lorebooks")
    public Result<List<LorebookAsset>> lorebooks() {
        return Result.success(repository.listLorebooks(tenantId(), user()));
    }

    /**
     * {@code lorebook} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/lorebooks/{id}")
    public Result<LorebookAsset> lorebook(@PathVariable String id) {
        return Result.success(
                repository
                        .findLorebook(tenantId(), user(), id)
                        .orElseThrow(
                                () -> new IllegalArgumentException("lorebook entry not found")));
    }

    /**
     * {@code deleteLorebook} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @DeleteMapping("/lorebooks/{id}")
    public Result<Void> deleteLorebook(@PathVariable String id) {
        repository.deleteLorebook(tenantId(), user(), id);
        return Result.success();
    }

    /**
     * {@code createPrompt} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/prompt-studio/templates")
    public Result<PromptTemplateVersion> createPrompt(@RequestBody PromptRequest request) {
        if (request == null
                || request.templateId == null
                || request.templateId.isBlank()
                || request.body == null)
            throw new IllegalArgumentException("templateId and body are required");
        List<PromptTemplateVersion> revisions =
                repository.listPrompts(tenantId(), user(), request.templateId);
        if (revisions.stream().anyMatch(v -> v.version() == request.version))
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "prompt version already exists");
        validateVariables(request.body, request.variableSchema);
        PromptTemplateVersion version =
                new PromptTemplateVersion(
                        id(),
                        request.templateId,
                        request.version,
                        request.status,
                        request.body,
                        request.variableSchema,
                        request.testCases,
                        Instant.now(),
                        "PUBLISHED".equals(request.status) ? Instant.now() : null);
        return Result.success(repository.savePrompt(version, tenantId(), user()));
    }

    /**
     * {@code prompts} 执行当前类型定义的业务操作。
     *
     * @param templateId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/prompt-studio/templates")
    public Result<List<PromptTemplateVersion>> prompts(
            @RequestParam(required = false) String templateId) {
        return Result.success(repository.listPrompts(tenantId(), user(), templateId));
    }

    /**
     * {@code publishPrompt} 执行当前模块定义的业务流程。
     *
     * @param templateId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/prompt-studio/templates/{templateId}/publish")
    public Result<PromptTemplateVersion> publishPrompt(
            @PathVariable String templateId, @RequestBody PublishRequest request) {
        if (request == null || request.version < 1)
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                    "draft version is required");
        PromptTemplateVersion draft =
                repository.listPrompts(tenantId(), user(), templateId).stream()
                        .filter(v -> v.version() == request.version && "DRAFT".equals(v.status()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "draft prompt version not found"));
        int nextVersion =
                repository.listPrompts(tenantId(), user(), templateId).stream()
                                .mapToInt(PromptTemplateVersion::version)
                                .max()
                                .orElse(0)
                        + 1;
        PromptTemplateVersion published =
                new PromptTemplateVersion(
                        id(),
                        templateId,
                        nextVersion,
                        "PUBLISHED",
                        draft.body(),
                        draft.variableSchema(),
                        draft.testCases(),
                        Instant.now(),
                        Instant.now());
        return Result.success(repository.savePrompt(published, tenantId(), user()));
    }

    /**
     * {@code diffPrompt} 执行当前类型定义的业务操作。
     *
     * @param templateId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/prompt-studio/templates/{templateId}/diff")
    public Result<PromptDiff> diffPrompt(
            @PathVariable String templateId, @RequestBody DiffRequest request) {
        if (request == null || request.fromVersion < 1 || request.toVersion < 1)
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                    "prompt versions are required");
        List<PromptTemplateVersion> revisions =
                repository.listPrompts(tenantId(), user(), templateId);
        PromptTemplateVersion from =
                revisions.stream()
                        .filter(v -> v.version() == request.fromVersion)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "source prompt version not found"));
        PromptTemplateVersion to =
                revisions.stream()
                        .filter(v -> v.version() == request.toVersion)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "target prompt version not found"));
        List<String> before = List.of(from.body().split("\\R", -1));
        List<String> after = List.of(to.body().split("\\R", -1));
        List<String> changes = new java.util.ArrayList<>();
        int max = Math.max(before.size(), after.size());
        for (int i = 0; i < max; i++) {
            String oldLine = i < before.size() ? before.get(i) : null;
            String newLine = i < after.size() ? after.get(i) : null;
            if (!java.util.Objects.equals(oldLine, newLine)) {
                if (oldLine != null) changes.add("- " + oldLine);
                if (newLine != null) changes.add("+ " + newLine);
            }
        }
        return Result.success(new PromptDiff(templateId, from.version(), to.version(), changes));
    }

    /**
     * {@code preview} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/prompt-studio/preview")
    public Result<PromptPreview> preview(@RequestBody PromptPreviewRequest request) {
        String rendered = request.body == null ? "" : request.body;
        if (request.variables != null)
            for (var entry : request.variables.entrySet())
                rendered =
                        rendered.replace(
                                "{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        return Result.success(
                new PromptPreview(
                        rendered,
                        request.variables == null
                                ? List.of()
                                : request.variables.keySet().stream().sorted().toList(),
                        Math.max(1, rendered.length() / 4)));
    }

    /**
     * 处理testprompt。
     *
     * @return 处理结果。
     */
    @PostMapping("/prompt-studio/templates/{templateId}/test")
    public Result<PromptTestRun> testPrompt(
            @PathVariable String templateId, @RequestBody PromptTestRequest request) {
        if (request == null || request.version < 1)
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                    "prompt version is required");
        PromptTemplateVersion version =
                repository.listPrompts(tenantId(), user(), templateId).stream()
                        .filter(item -> item.version() == request.version)
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalArgumentException("prompt version not found"));
        java.util.Map<String, Object> variables =
                request.variables == null ? java.util.Map.of() : request.variables;
        List<String> rendered =
                version.testCases().stream().map(sample -> render(sample, variables)).toList();
        return Result.success(
                new PromptTestRun(
                        templateId,
                        version.version(),
                        rendered,
                        Math.max(1, rendered.stream().mapToInt(String::length).sum() / 4)));
    }

    /**
     * {@code createGroup} 写入或更新当前模块中的业务数据。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/groups")
    public Result<GroupChatAsset> createGroup(@RequestBody GroupRequest request) {
        Instant now = Instant.now();
        GroupChat group =
                new GroupChat(
                        id(),
                        request.name,
                        request.participants,
                        request.speakerPolicy,
                        request.maxTurns,
                        request.tokenBudget);
        return Result.success(
                repository.saveGroup(
                        new GroupChatAsset(group.id(), tenant(), user(), group, now, now)));
    }

    /**
     * {@code groups} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/groups")
    public Result<List<GroupChatAsset>> groups() {
        return Result.success(repository.listGroups(tenantId(), user()));
    }

    /**
     * {@code group} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/groups/{id}")
    public Result<GroupChatAsset> group(@PathVariable String id) {
        return Result.success(
                repository
                        .findGroup(tenantId(), user(), id)
                        .orElseThrow(() -> new IllegalArgumentException("group not found")));
    }

    /**
     * {@code deleteGroup} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @DeleteMapping("/groups/{id}")
    public Result<Void> deleteGroup(@PathVariable String id) {
        repository.deleteGroup(tenantId(), user(), id);
        return Result.success();
    }

    /**
     * {@code nextSpeaker} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/groups/{id}/next-speaker")
    public Result<GroupTurnPlanner.TurnDecision> nextSpeaker(
            @PathVariable String id, @RequestBody(required = false) TurnRequest request) {
        GroupChatAsset asset =
                repository
                        .findGroup(tenantId(), user(), id)
                        .orElseThrow(() -> new IllegalArgumentException("group not found"));
        TurnRequest input = request == null ? new TurnRequest() : request;
        return Result.success(
                GroupTurnPlanner.next(
                        asset.group(),
                        input.completedSpeakerIds,
                        input.requestedSpeakerId,
                        input.consumedTokens));
    }

    /**
     * 执行turn。
     *
     * @return 处理结果。
     */
    @PostMapping("/groups/{id}/turn")
    public Result<GroupTurnRun> runTurn(
            @PathVariable String id, @RequestBody TurnRunRequest request) {
        if (request == null || request.conversationId == null || request.conversationId.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                    "conversationId is required");
        }
        if (request.content == null || request.content.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                    "content is required");
        }
        GroupChatAsset asset =
                repository
                        .findGroup(tenantId(), user(), id)
                        .orElseThrow(() -> new IllegalArgumentException("group not found"));
        Conversation conversation =
                conversations
                        .findConversation(
                                request.conversationId,
                                new com.shiyu.ai.kernel.context.TenantId(tenant()),
                                user())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        if (generationRepository.hasRunningConversation(
                request.conversationId, new com.shiyu.ai.kernel.context.TenantId(tenant()))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "a group turn is already running");
        }
        List<com.shiyu.ai.conversation.contract.model.GenerationRun> priorRuns =
                generationRepository.listConversation(
                        request.conversationId,
                        new com.shiyu.ai.kernel.context.TenantId(tenant()),
                        1000);
        List<String> completedSpeakers =
                priorRuns.stream()
                        .filter(
                                run ->
                                        run.status()
                                                        == com.shiyu.ai.conversation.contract.model
                                                                .GenerationStatus.COMPLETED
                                                && run.speakerId() != null)
                        .map(com.shiyu.ai.conversation.contract.model.GenerationRun::speakerId)
                        .toList();
        int consumedTokens =
                priorRuns.stream()
                        .filter(
                                run ->
                                        run.status()
                                                        == com.shiyu.ai.conversation.contract.model
                                                                .GenerationStatus.COMPLETED
                                                && run.speakerId() != null)
                        .mapToLong(
                                com.shiyu.ai.conversation.contract.model.GenerationRun
                                        ::completionTokens)
                        .mapToInt(value -> (int) Math.min(Integer.MAX_VALUE, Math.max(0, value)))
                        .sum();
        GroupTurnPlanner.TurnDecision decision =
                GroupTurnPlanner.next(
                        asset.group(),
                        completedSpeakers,
                        request.requestedSpeakerId,
                        consumedTokens);
        if (decision.exhausted()) return Result.success(new GroupTurnRun(decision, null, null));

        String platform =
                request.platform == null || request.platform.isBlank()
                        ? conversation.platform()
                        : request.platform;
        String model =
                request.model == null || request.model.isBlank()
                        ? conversation.model()
                        : request.model;
        if (platform == null || platform.isBlank() || model == null || model.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                    "platform and model are required");
        }
        GenerationRun run;
        try {
            var input = conversationService.appendUserMessage(conversation, request.content);
            var latestConversation =
                    conversations
                            .findConversation(
                                    request.conversationId,
                                    new com.shiyu.ai.kernel.context.TenantId(tenant()),
                                    user())
                            .orElseThrow(
                                    () -> new IllegalArgumentException("conversation not found"));
            run =
                    conversationService.createGeneration(
                            latestConversation,
                            input,
                            platform,
                            model,
                            decision.participant().id());
        } catch (IllegalStateException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
        try {
            generationRunner.start(run, new TenantId(tenant()), user());
        } catch (
                com.shiyu.ai.conversation.implementation.application.GenerationAdmissionException
                        denied) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                    denied.errorCode(),
                    denied);
        }
        return Result.success(new GroupTurnRun(decision, run.id(), run.speakerId()));
    }

    private String id() {
        return UUID.randomUUID().toString();
    }

    private TenantId tenantId() {
        return new TenantId(ActorContextHttpAdapter.tenantId());
    }

    private long tenant() {
        return tenantId().value();
    }

    private long user() {
        return ActorContextHttpAdapter.userId();
    }

    /**
     * {@code CharacterRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    public static class CharacterRequest {
        public CharacterCardV2 card;
        /**
         * visibility 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String visibility = "PRIVATE";
    }

    /**
     * {@code PromptRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    public static class PromptRequest {
        public String templateId;
        /**
         * 版本，表示当前对象中的对应属性。
         */
        public int version = 1;
        /**
         * 状态，表示当前对象中的对应属性。
         */
        public String status = "DRAFT";
        /**
         * body 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String body;
        /**
         * variableSchema 属性，保存当前对象中的业务数据或协作依赖。
         */
        public java.util.Map<String, String> variableSchema;
        /**
         * testCases 属性，保存当前对象中的业务数据或协作依赖。
         */
        public List<String> testCases;
    }

    /**
     * {@code PublishRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    public static class PublishRequest {
        public int version;
    }

    /**
     * {@code DiffRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    public static class DiffRequest {
        public int fromVersion;
        /**
         * toVersion 属性，保存当前对象中的业务数据或协作依赖。
         */
        public int toVersion;
    }

    /**
     * {@code GroupRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    public static class GroupRequest {
        public String name;
        /**
         * participants 属性，保存当前对象中的业务数据或协作依赖。
         */
        public List<GroupChat.Participant> participants;
        /**
         * speakerPolicy 属性，保存当前对象中的业务数据或协作依赖。
         */
        public SpeakerPolicy speakerPolicy = SpeakerPolicy.MANUAL;
        /**
         * maxTurns 属性，保存当前对象中的业务数据或协作依赖。
         */
        public int maxTurns = 20;
        /**
         * tokenBudget 属性，保存当前对象中的业务数据或协作依赖。
         */
        public int tokenBudget = 4000;
    }

    /**
     * {@code TurnRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    public static class TurnRequest {
        public List<String> completedSpeakerIds = List.of();
        /**
         * requestedSpeakerId 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String requestedSpeakerId;
        /**
         * consumedTokens 属性，保存当前对象中的业务数据或协作依赖。
         */
        public int consumedTokens;
    }

    /**
     * {@code TurnRunRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    public static class TurnRunRequest {
        public String conversationId;
        /**
         * 内容，表示当前对象中的对应属性。
         */
        public String content;
        /**
         * platform 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String platform;
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String model;
        public List<String> completedSpeakerIds = List.of();
        /**
         * requestedSpeakerId 属性，保存当前对象中的业务数据或协作依赖。
         */
        public String requestedSpeakerId;
        /**
         * consumedTokens 属性，保存当前对象中的业务数据或协作依赖。
         */
        public int consumedTokens;
    }

    /**
     * {@code GroupTurnRun} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param decision decision 属性，表示该记录组件承载的数据。
     * @param generationId generationId 属性，表示该记录组件承载的数据。
     * @param speakerId speakerId 属性，表示该记录组件承载的数据。
     */
    public record GroupTurnRun(
            GroupTurnPlanner.TurnDecision decision, String generationId, String speakerId) {}

    /**
     * {@code PromptPreviewRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    public static class PromptPreviewRequest {
        public String body;
        /**
         * variables 属性，保存当前对象中的业务数据或协作依赖。
         */
        public java.util.Map<String, Object> variables;
    }

    /**
     * {@code PromptTestRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    public static class PromptTestRequest {
        public int version;
        /**
         * variables 属性，保存当前对象中的业务数据或协作依赖。
         */
        public java.util.Map<String, Object> variables;
    }

    /**
     * {@code PromptPreview} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param rendered rendered 属性，表示该记录组件承载的数据。
     * @param variables 变量集合，表示该记录组件承载的数据。
     * @param estimatedTokens 预计令牌数，表示该记录组件承载的数据。
     */
    public record PromptPreview(String rendered, List<String> variables, int estimatedTokens) {}

    /**
     * {@code PromptTestRun} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param templateId templateId 属性，表示该记录组件承载的数据。
     * @param version version 属性，表示该记录组件承载的数据。
     * @param renderedCases renderedCases 属性，表示该记录组件承载的数据。
     * @param estimatedTokens 预计令牌数，表示该记录组件承载的数据。
     */
    public record PromptTestRun(
            String templateId, int version, List<String> renderedCases, int estimatedTokens) {}

    /**
     * {@code PromptDiff} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param templateId templateId 属性，表示该记录组件承载的数据。
     * @param fromVersion fromVersion 属性，表示该记录组件承载的数据。
     * @param toVersion toVersion 属性，表示该记录组件承载的数据。
     * @param changes changes 属性，表示该记录组件承载的数据。
     */
    public record PromptDiff(
            String templateId, int fromVersion, int toVersion, List<String> changes) {}

    private static String render(String body, java.util.Map<String, Object> variables) {
        String rendered = body == null ? "" : body;
        for (var entry : variables.entrySet())
            rendered =
                    rendered.replace(
                            "{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        return rendered;
    }

    private static void validateVariables(String body, java.util.Map<String, String> schema) {
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("\\{\\{\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*}}")
                        .matcher(body == null ? "" : body);
        java.util.Set<String> declared = schema == null ? java.util.Set.of() : schema.keySet();
        while (matcher.find())
            if (!declared.contains(matcher.group(1)))
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                        "undeclared prompt variable: " + matcher.group(1));
    }
}
