package com.shiyu.ai.conversation.implementation.web.controller;

import com.shiyu.ai.agent.contract.runtime.ContextItem;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.conversation.implementation.application.ConversationPromptService;
import com.shiyu.ai.conversation.implementation.application.ConversationService;
import com.shiyu.ai.conversation.implementation.application.GenerationRunner;
import com.shiyu.ai.conversation.implementation.application.PromptSafety;
import com.shiyu.ai.conversation.implementation.domain.model.*;
import com.shiyu.ai.conversation.implementation.domain.port.ConversationRepository;
import com.shiyu.ai.conversation.implementation.domain.port.GenerationRepository;
import com.shiyu.ai.conversation.implementation.domain.port.IdempotencyRepository;
import com.shiyu.ai.conversation.implementation.web.support.imports.ConversationImportPreviewStore;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.contract.model.ChatMessage;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code ConversationController} 是会话模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
 */
@Tag(name = "Conversation Platform")
@RestController
@RequestMapping("/api/conversation/conversations")
public class ConversationController {
    /**
     * conversations 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ConversationService conversations;
    /**
     * conversationRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ConversationRepository conversationRepository;
    /**
     * generationRunner 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final GenerationRunner generationRunner;
    /**
     * idempotency 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final IdempotencyRepository idempotency;
    /**
     * 生成仓储，表示当前对象中的对应属性。
     */
    private final GenerationRepository generationRepository;
    /**
     * importPreviews 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ConversationImportPreviewStore importPreviews;
    /**
     * 提示词服务，表示当前对象中的对应属性。
     */
    private final ConversationPromptService promptService;

    /**
     * {@code ConversationController} 创建并初始化当前类型实例。
     *
     * @param conversations 参数值，用于执行当前操作。
     * @param conversationRepository 参数值，用于执行当前操作。
     * @param generationRunner 参数值，用于执行当前操作。
     * @param idempotency 参数值，用于执行当前操作。
     * @param generationRepository 参数值，用于执行当前操作。
     * @param importPreviews 参数值，用于执行当前操作。
     * @param promptService 参数值，用于执行当前操作。
     */
    public ConversationController(
            ConversationService conversations,
            ConversationRepository conversationRepository,
            GenerationRunner generationRunner,
            IdempotencyRepository idempotency,
            GenerationRepository generationRepository,
            ConversationImportPreviewStore importPreviews,
            ConversationPromptService promptService) {
        this.conversations = conversations;
        this.conversationRepository = conversationRepository;
        this.generationRunner = generationRunner;
        this.idempotency = idempotency;
        this.generationRepository = generationRepository;
        this.importPreviews = importPreviews;
        this.promptService = promptService;
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param idempotencyKey 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping
    public Result<Conversation> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateConversationRequest request) {
        long userId = currentUser();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing =
                    idempotency
                            .find(currentTenant(), userId, "conversation.create", idempotencyKey)
                            .flatMap(
                                    resource ->
                                            conversationRepository.findConversation(
                                                    resource, currentTenant(), userId));
            if (existing.isPresent()) return Result.success(existing.get());
        }
        Conversation created =
                conversations.create(
                        currentTenant(),
                        userId,
                        request.sceneType,
                        request.title,
                        request.platform,
                        request.model,
                        request.systemPrompt);
        if (idempotencyKey != null
                && !idempotencyKey.isBlank()
                && !idempotency.claim(
                        currentTenant(),
                        userId,
                        "conversation.create",
                        idempotencyKey,
                        created.id())) {
            return idempotency
                    .find(currentTenant(), userId, "conversation.create", idempotencyKey)
                    .flatMap(
                            resource ->
                                    conversationRepository.findConversation(
                                            resource, currentTenant(), userId))
                    .map(Result::success)
                    .orElse(Result.success(created));
        }
        return Result.success(created);
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param limit 参数值，用于执行当前操作。
     * @param offset 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping
    public Result<List<Conversation>> list(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return Result.success(
                conversationRepository.listConversations(
                        currentTenant(), currentUser(), limit, offset));
    }

    /**
     * {@code detail} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/{id}")
    public Result<Conversation> detail(@PathVariable String id) {
        return Result.success(
                conversationRepository
                        .findConversation(id, currentTenant(), currentUser())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found")));
    }

    /**
     * {@code messages} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/{id}/messages")
    public Result<List<ConversationMessage>> messages(
            @PathVariable String id, @RequestParam(defaultValue = "1000") int limit) {
        conversationRepository
                .findConversation(id, currentTenant(), currentUser())
                .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        return Result.success(
                conversationRepository
                        .listMessages(id, currentTenant(), currentUser(), limit)
                        .reversed());
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PatchMapping("/{id}")
    public Result<Conversation> update(
            @PathVariable String id, @RequestBody UpdateConversationRequest request) {
        Conversation current =
                conversationRepository
                        .findConversation(id, currentTenant(), currentUser())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        ConversationStatus status =
                request.status == null
                        ? current.status()
                        : ConversationStatus.valueOf(request.status.toUpperCase());
        Conversation next =
                new Conversation(
                        current.id(),
                        current.tenantId(),
                        current.ownerUserId(),
                        current.sceneType(),
                        request.title == null ? current.title() : request.title,
                        status,
                        current.parentConversationId(),
                        current.branchFromMessageId(),
                        current.activeLeafMessageId(),
                        current.rollingSummary(),
                        current.platform(),
                        current.model(),
                        current.version() + 1,
                        current.createdAt(),
                        java.time.Instant.now());
        if (conversationRepository.updateConversation(next, current.version()) != 1)
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "conversation was modified");
        return Result.success(next);
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        if (conversationRepository.deleteConversation(id, currentTenant(), currentUser()) != 1)
            throw new IllegalArgumentException("conversation not found");
        return Result.success();
    }

    /**
     * {@code message} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param idempotencyKey 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/{id}/messages")
    public Result<GenerationRun> message(
            @PathVariable String id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody MessageRequest request) {
        return messageInternal(id, idempotencyKey, request);
    }

    private Result<GenerationRun> messageInternal(
            String id, String idempotencyKey, MessageRequest request) {
        if (request == null || request.content == null || request.content.isBlank())
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                    "message content is required");
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing =
                    idempotency
                            .find(
                                    currentTenant(),
                                    currentUser(),
                                    "conversation.message:" + id,
                                    idempotencyKey)
                            .flatMap(
                                    resource ->
                                            generations()
                                                    .find(
                                                            resource,
                                                            currentTenant(),
                                                            currentUser()));
            if (existing.isPresent()) return Result.success(existing.get());
        }
        Conversation conversation =
                conversationRepository
                        .findConversation(id, currentTenant(), currentUser())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        ConversationMessage message =
                conversations.appendUserMessage(conversation, request.content);
        conversation =
                conversationRepository
                        .findConversation(id, currentTenant(), currentUser())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        GenerationRun run;
        try {
            run =
                    conversations.createGeneration(
                            conversation, message, request.platform, request.model);
        } catch (IllegalStateException conflict) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, conflict.getMessage(), conflict);
        }
        if (idempotencyKey != null
                && !idempotencyKey.isBlank()
                && !idempotency.claim(
                        currentTenant(),
                        currentUser(),
                        "conversation.message:" + id,
                        idempotencyKey,
                        run.id())) {
            var existing =
                    idempotency
                            .find(
                                    currentTenant(),
                                    currentUser(),
                                    "conversation.message:" + id,
                                    idempotencyKey)
                            .flatMap(
                                    resource ->
                                            generations()
                                                    .find(
                                                            resource,
                                                            currentTenant(),
                                                            currentUser()));
            if (existing.isPresent()) return Result.success(existing.get());
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "idempotency key is already in use");
        }
        try {
            generationRunner.start(run, currentTenant(), currentUser());
        } catch (
                com.shiyu.ai.conversation.implementation.application.GenerationAdmissionException
                        denied) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                    denied.errorCode(),
                    denied);
        }
        return Result.success(run);
    }

    /**
     * {@code generation} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param idempotencyKey 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/{id}/generations")
    public Result<GenerationRun> generation(
            @PathVariable String id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody MessageRequest request) {
        return message(id, idempotencyKey, request);
    }

    private GenerationRepository generations() {
        return generationRepository;
    }

    /**
     * {@code activeLeaf} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param messageId 参数值，用于执行当前操作。
     * @param body 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/{id}/active-leaf")
    public Result<Void> activeLeaf(
            @PathVariable String id,
            @RequestParam(required = false) String messageId,
            @RequestBody(required = false) ActiveLeafRequest body) {
        if (messageId == null && body != null) messageId = body.messageId;
        if (messageId == null || messageId.isBlank())
            throw new IllegalArgumentException("messageId is required");
        Conversation c =
                conversationRepository
                        .findConversation(id, currentTenant(), currentUser())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        conversationRepository
                .findMessage(messageId, currentTenant(), currentUser())
                .filter(m -> id.equals(m.conversationId()))
                .orElseThrow(
                        () ->
                                new org.springframework.web.server.ResponseStatusException(
                                        org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                                        "message does not belong to conversation"));
        conversationRepository.updateConversation(
                new Conversation(
                        c.id(),
                        c.tenantId(),
                        c.ownerUserId(),
                        c.sceneType(),
                        c.title(),
                        c.status(),
                        c.parentConversationId(),
                        c.branchFromMessageId(),
                        messageId,
                        c.rollingSummary(),
                        c.platform(),
                        c.model(),
                        c.version() + 1,
                        c.createdAt(),
                        java.time.Instant.now()),
                c.version());
        return Result.success();
    }

    /**
     * {@code promptPreview} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/{id}/prompt-preview")
    public Result<PromptPreview> promptPreview(@PathVariable String id) {
        Conversation c =
                conversationRepository
                        .findConversation(id, currentTenant(), currentUser())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        ConversationPromptService.PromptAssembly assembly =
                promptService.assemble(
                        c,
                        conversationRepository
                                .listMessages(id, currentTenant(), currentUser(), 1000)
                                .reversed(),
                        currentTenant(),
                        currentUser());
        List<ConversationMessage> messages = assembly.conversationMessages();
        long estimated =
                assembly.modelMessages().stream()
                        .mapToLong(
                                m ->
                                        m.content().stream()
                                                .mapToLong(
                                                        p -> PromptSafety.estimateTokens(p.text()))
                                                .sum())
                        .sum();
        List<PromptSegment> segments = new java.util.ArrayList<>();
        messages.stream()
                .filter(m -> m.role() == MessageRole.SYSTEM)
                .forEach(
                        m ->
                                segments.add(
                                        new PromptSegment(
                                                m.role().name(),
                                                m.textContent(),
                                                PromptSafety.estimateTokens(m.textContent()))));
        for (ContextItem item : assembly.contextItems())
            segments.add(
                    new PromptSegment(
                            item.sourceType() + ":" + item.sourceId(),
                            item.content(),
                            PromptSafety.estimateTokens(item.content())));
        messages.stream()
                .filter(m -> m.role() != MessageRole.SYSTEM)
                .forEach(
                        m ->
                                segments.add(
                                        new PromptSegment(
                                                m.role().name(),
                                                m.textContent(),
                                                PromptSafety.estimateTokens(m.textContent()))));
        String canonicalPrompt = JSONUtils.toJsonString(assembly.modelMessages());
        return Result.success(
                new PromptPreview(
                        messages,
                        segments,
                        estimated,
                        false,
                        true,
                        "local-character-estimate",
                        java.util.Map.of("platform", c.platform(), "model", c.model()),
                        sha256(canonicalPrompt),
                        assembly.contextItems(),
                        assembly.contextTrace()));
    }

    /**
     * {@code branch} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param messageId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/{id}/branches")
    public Result<Conversation> branch(@PathVariable String id, @RequestParam String messageId) {
        Conversation source =
                conversationRepository
                        .findConversation(id, currentTenant(), currentUser())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        conversationRepository
                .findMessage(messageId, currentTenant(), currentUser())
                .filter(m -> id.equals(m.conversationId()))
                .orElseThrow(
                        () ->
                                new org.springframework.web.server.ResponseStatusException(
                                        org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                                        "message does not belong to conversation"));
        return Result.success(conversations.branch(source, messageId));
    }

    /**
     * {@code branches} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/{id}/branches")
    public Result<List<Conversation>> branches(@PathVariable String id) {
        conversationRepository
                .findConversation(id, currentTenant(), currentUser())
                .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        return Result.success(
                conversationRepository.listBranches(id, currentTenant(), currentUser()));
    }

    /**
     * {@code export} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param format 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping(value = "/{id}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object export(@PathVariable String id, @RequestParam(required = false) String format) {
        conversationRepository
                .findConversation(id, currentTenant(), currentUser())
                .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        List<ConversationMessage> messages =
                conversationRepository
                        .listMessages(id, currentTenant(), currentUser(), 1000)
                        .reversed();
        if ("jsonl".equalsIgnoreCase(format))
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(
                            com.shiyu.ai.conversation.implementation.domain.chat
                                    .ConversationExchangeCodec.toJsonl(messages));
        if ("markdown".equalsIgnoreCase(format) || "md".equalsIgnoreCase(format))
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(MediaType.TEXT_MARKDOWN)
                    .body(
                            com.shiyu.ai.conversation.implementation.domain.chat
                                    .ConversationExchangeCodec.toMarkdown(messages));
        return Result.success(messages);
    }

    /**
     * {@code importConversation} 执行当前类型定义的业务操作。
     * 导入会话字段并返回创建后的会话。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
   @PostMapping(
           value = "/import",
           consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
   public Result<Conversation> importConversation(@RequestBody ImportRequest request) {
       validateImportPayload(request);
        List<
                        com.shiyu.ai.conversation.implementation.domain.chat
                                .ConversationExchangeCodec.ImportedMessage>
                imported =
                        importPreviews.consume(
                                currentTenant(),
                                currentUser(),
                                request.previewToken,
                                request.format,
                                request.content);
        Conversation conversation =
                conversations.create(
                        currentTenant(),
                        currentUser(),
                        request.sceneType,
                        request.title,
                        request.platform,
                        request.model,
                        request.systemPrompt);
        for (var item : imported) {
            MessageRole role;
            try {
                role = MessageRole.valueOf(item.role().toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                role = MessageRole.USER;
            }
            Conversation current =
                    conversationRepository
                            .findConversation(conversation.id(), currentTenant(), currentUser())
                            .orElseThrow();
            conversations.appendMessage(
                    current, current.activeLeafMessageId(), role, item.content(), null, null);
        }
        return Result.success(
                conversationRepository
                        .findConversation(conversation.id(), currentTenant(), currentUser())
                        .orElse(conversation));
    }

    /**
     * {@code importPreview} 执行当前类型定义的业务操作。
     *
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/import/preview")
    public Result<ConversationImportPreviewStore.Preview> importPreview(
            @RequestBody ImportRequest request) {
        validateImportPayload(request);
        String format =
                request.format == null
                        ? "jsonl"
                        : request.format.toLowerCase(java.util.Locale.ROOT);
        List<
                        com.shiyu.ai.conversation.implementation.domain.chat
                                .ConversationExchangeCodec.ImportedMessage>
                imported =
                        "markdown".equals(format) || "md".equals(format)
                                ? com.shiyu.ai.conversation.implementation.domain.chat
                                        .ConversationExchangeCodec.fromMarkdown(request.content)
                                : com.shiyu.ai.conversation.implementation.domain.chat
                                        .ConversationExchangeCodec.fromJsonl(request.content);
        if (imported.size() > 10_000)
            throw new IllegalArgumentException("import contains too many messages");
        return Result.success(
                importPreviews.issue(
                        currentTenant(), currentUser(), format, request.content, imported));
    }

    private void validateImportPayload(ImportRequest request) {
        if (request == null || request.content == null || request.content.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                    "import content is required");
        }
        if (request.content.getBytes(java.nio.charset.StandardCharsets.UTF_8).length
                > 4 * 1024 * 1024) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT,
                    "import payload exceeds 4 MiB");
        }
    }

    private TenantId currentTenant() {
        return new TenantId(ActorContextHttpAdapter.tenantId());
    }

    private long currentUser() {
        return ActorContextHttpAdapter.userId();
    }

    /**
     * {@code CreateConversationRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class CreateConversationRequest {
        private String sceneType = "general";
        /**
         * 标题，表示当前对象中的对应属性。
         */
        private String title;
        /**
         * platform 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String platform;
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String model;
        /**
         * systemPrompt 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String systemPrompt;
    }

    /**
     * {@code MessageRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class MessageRequest {
        private String content;
        /**
         * platform 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String platform;
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String model;
    }

    /**
     * {@code ActiveLeafRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class ActiveLeafRequest {
        private String messageId;
    }

    /**
     * {@code UpdateConversationRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class UpdateConversationRequest {
        private String title;
        /**
         * 状态，表示当前对象中的对应属性。
         */
        private String status;
    }

    /**
     * {@code ImportRequest} 表示会话模块的请求参数，承载调用方提交的输入数据。
     */
    @Data
    public static class ImportRequest {
        private String format = "jsonl";
        /**
         * previewToken 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String previewToken;
        /**
         * 标题，表示当前对象中的对应属性。
         */
        private String title;
        /**
         * sceneType 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String sceneType = "general";
        /**
         * platform 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String platform;
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String model;
        /**
         * systemPrompt 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String systemPrompt;
        /**
         * 内容，表示当前对象中的对应属性。
         */
        private String content;
    }

    /**
     * {@code PromptSegment} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param source 来源，表示该记录组件承载的数据。
     * @param content 内容，表示该记录组件承载的数据。
     * @param estimatedTokens 预计令牌数，表示该记录组件承载的数据。
     */
    public record PromptSegment(String source, String content, long estimatedTokens) {}

    /**
     * {@code PromptPreview} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param messages 消息列表，表示该记录组件承载的数据。
     * @param sources sources 属性，表示该记录组件承载的数据。
     * @param estimatedTokens 预计令牌数，表示该记录组件承载的数据。
     * @param truncated truncated 属性，表示该记录组件承载的数据。
     * @param estimated estimated 属性，表示该记录组件承载的数据。
     * @param estimator estimator 属性，表示该记录组件承载的数据。
     * @param modelParameters modelParameters 属性，表示该记录组件承载的数据。
     * @param promptHash promptHash 属性，表示该记录组件承载的数据。
     * @param contextItems contextItems 属性，表示该记录组件承载的数据。
     * @param contextTrace contextTrace 属性，表示该记录组件承载的数据。
     */
    public record PromptPreview(
            List<ConversationMessage> messages,
            List<PromptSegment> sources,
            long estimatedTokens,
            boolean truncated,
            boolean estimated,
            String estimator,
            java.util.Map<String, Object> modelParameters,
            String promptHash,
            List<ContextItem> contextItems,
            com.shiyu.ai.agent.contract.runtime.ContextTrace contextTrace) {
        public PromptPreview {
            contextItems = contextItems == null ? List.of() : List.copyOf(contextItems);
        }
    }

    private ChatMessage toModelMessage(ConversationMessage message) {
        return new ChatMessage(
                message.role().name().toLowerCase(),
                message.contentParts().stream()
                        .map(
                                p ->
                                        new ChatMessage.ContentPart(
                                                p.type(), p.text(), p.mediaUri(), p.mimeType()))
                        .toList());
    }

    private String sha256(String value) {
        try {
            return java.util.HexFormat.of()
                    .formatHex(
                            java.security.MessageDigest.getInstance("SHA-256")
                                    .digest(
                                            value.getBytes(
                                                    java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException ex) {
            throw new IllegalStateException("unable to hash prompt", ex);
        }
    }
}
