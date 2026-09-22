package com.shiyu.ai.conversation.implementation.web.controller;

import com.shiyu.ai.conversation.implementation.domain.chat.codec.ConversationExchangeCodec;

import com.shiyu.ai.agent.contract.runtime.ContextItem;
import com.shiyu.ai.common.foundation.api.Result;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
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
 * 处理 会话 相关的 Web 请求，并将请求转换为应用服务调用。
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
     * 执行 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param conversations 用于完成本次业务处理的 conversations 参数。
     * @param conversationRepository 用于完成本次业务处理的 conversationRepository 参数。
     * @param generationRunner 用于完成本次业务处理的 generationRunner 参数。
     * @param idempotency 用于完成本次业务处理的 idempotency 参数。
     * @param generationRepository 用于完成本次业务处理的 generationRepository 参数。
     * @param importPreviews 用于完成本次业务处理的 importPreviews 参数。
     * @param promptService 用于完成本次业务处理的 promptService 参数。
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
     * 创建或保存 会话 相关业务数据，并返回处理结果。
     *
     * @param idempotencyKey 用于完成本次业务处理的 idempotencyKey 参数。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 会话 相关操作生成的结果数据。
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
     * 查询 会话 相关业务数据，并返回处理结果。
     *
     * @param limit 每页返回的数据数量。
     * @param offset 用于完成本次业务处理的 offset 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
     */
    @GetMapping("/{id}")
    public Result<Conversation> detail(@PathVariable String id) {
        return Result.success(
                conversationRepository
                        .findConversation(id, currentTenant(), currentUser())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found")));
    }

    /**
     * 执行 会话 相关业务数据，并返回处理结果。
     *
     * @param messages 用于完成本次业务处理的 messages 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 执行 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
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
     * 删除或移除 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        if (conversationRepository.deleteConversation(id, currentTenant(), currentUser()) != 1)
            throw new IllegalArgumentException("conversation not found");
        return Result.success();
    }

    /**
     * 执行 会话 相关业务数据，并返回处理结果。
     *
     * @param messages 用于完成本次业务处理的 messages 参数。
     * @return 返回 会话 相关操作生成的结果数据。
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
     * 执行 会话 相关业务数据，并返回处理结果。
     *
     * @param generations 用于完成本次业务处理的 generations 参数。
     * @return 返回 会话 相关操作生成的结果数据。
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
     * 执行 会话 相关业务数据，并返回处理结果。
     *
     * @param leaf 用于完成本次业务处理的 leaf 参数。
     * @return 返回 会话 相关操作生成的结果数据。
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
     * 查询 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param preview 用于完成本次业务处理的 preview 参数。
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
     * 执行 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param branches 用于完成本次业务处理的 branches 参数。
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
     * 查询 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param branches 用于完成本次业务处理的 branches 参数。
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
                            com.shiyu.ai.conversation.implementation.domain.chat.codec
                                    .ConversationExchangeCodec.toJsonl(messages));
        if ("markdown".equalsIgnoreCase(format) || "md".equalsIgnoreCase(format))
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(MediaType.TEXT_MARKDOWN)
                    .body(
                            com.shiyu.ai.conversation.implementation.domain.chat.codec
                                    .ConversationExchangeCodec.toMarkdown(messages));
        return Result.success(messages);
    }

   /**
    * 执行 会话 相关业务操作，并维护必要的状态和协作关系。
    *
    * @param import 用于完成本次业务处理的 import 参数。
    * @param APPLICATION_JSON_VALUE 用于完成本次业务处理的 APPLICATION_JSON_VALUE 参数。
    * @param TEXT_PLAIN_VALUE 用于完成本次业务处理的 TEXT_PLAIN_VALUE 参数。
    */
   @PostMapping(
           value = "/import",
           consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
   public Result<Conversation> importConversation(@RequestBody ImportRequest request) {
       validateImportPayload(request);
        List<
                        com.shiyu.ai.conversation.implementation.domain.chat.codec
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
     * 执行 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param preview 用于完成本次业务处理的 preview 参数。
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
                        com.shiyu.ai.conversation.implementation.domain.chat.codec
                                .ConversationExchangeCodec.ImportedMessage>
                imported =
                        "markdown".equals(format) || "md".equals(format)
                                ? com.shiyu.ai.conversation.implementation.domain.chat.codec
                                        .ConversationExchangeCodec.fromMarkdown(request.content)
                                : com.shiyu.ai.conversation.implementation.domain.chat.codec
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
     * 封装 Create 会话 操作所需的请求条件和输入数据。
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
     * 封装 消息 操作所需的请求条件和输入数据。
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
     * 封装 Active Leaf 操作所需的请求条件和输入数据。
     */
    @Data
    public static class ActiveLeafRequest {
        private String messageId;
    }

    /**
     * 封装 Update 会话 操作所需的请求条件和输入数据。
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
     * 封装 Import 操作所需的请求条件和输入数据。
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
     * 封装 提示词 Segment 相关的不可变数据及其字段约束。
     */
    public record PromptSegment(String source, String content, long estimatedTokens) {}

    /**
     * 封装 提示词 Preview 相关的不可变数据及其字段约束。
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
