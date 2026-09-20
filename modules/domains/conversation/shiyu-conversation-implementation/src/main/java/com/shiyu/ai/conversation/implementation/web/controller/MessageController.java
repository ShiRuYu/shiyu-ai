package com.shiyu.ai.conversation.implementation.web.controller;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.conversation.implementation.application.ConversationService;
import com.shiyu.ai.conversation.implementation.application.GenerationRunner;
import com.shiyu.ai.conversation.implementation.domain.model.Conversation;
import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;
import com.shiyu.ai.conversation.implementation.domain.model.MessageRole;
import com.shiyu.ai.conversation.implementation.domain.port.ConversationRepository;
import com.shiyu.ai.conversation.implementation.domain.port.GenerationRepository;
import com.shiyu.ai.conversation.implementation.domain.port.IdempotencyRepository;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 处理 消息 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/conversation/messages")
public class MessageController {
    /**
     * conversations 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ConversationService conversations;
    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final ConversationRepository repository;
    /**
     * generations 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final GenerationRepository generations;
    /**
     * runner 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final GenerationRunner runner;
    /**
     * idempotency 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final IdempotencyRepository idempotency;

    /**
     * 执行 消息 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param conversations 用于完成本次业务处理的 conversations 参数。
     * @param repository 用于完成本次业务处理的 repository 参数。
     * @param generations 用于完成本次业务处理的 generations 参数。
     * @param runner 用于完成本次业务处理的 runner 参数。
     * @param idempotency 用于完成本次业务处理的 idempotency 参数。
     */
    public MessageController(
            ConversationService conversations,
            ConversationRepository repository,
            GenerationRepository generations,
            GenerationRunner runner,
            IdempotencyRepository idempotency) {
        this.conversations = conversations;
        this.repository = repository;
        this.generations = generations;
        this.runner = runner;
        this.idempotency = idempotency;
    }

    /**
     * 执行 消息 相关业务数据，并返回处理结果。
     *
     * @param edits 用于完成本次业务处理的 edits 参数。
     * @return 返回 消息 相关操作生成的结果数据。
     */
    @PostMapping("/{messageId}/edits")
    public Result<ConversationMessage> edit(
            @PathVariable String messageId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody EditRequest request) {
        String operation = "message.edit:" + messageId;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing =
                    idempotency
                            .find(tenant(), user(), operation, idempotencyKey)
                            .flatMap(id -> repository.findMessage(id, tenant(), user()));
            if (existing.isPresent()) return Result.success(existing.get());
        }
        ConversationMessage original =
                repository
                        .findMessage(messageId, tenant(), user())
                        .orElseThrow(
                                () ->
                                        new org.springframework.web.server.ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "message not found"));
        if (request == null || request.content == null || request.content.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "message content is required");
        }
        Conversation conversation =
                repository
                        .findConversation(original.conversationId(), tenant(), user())
                        .orElseThrow(
                                () ->
                                        new org.springframework.web.server.ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "conversation not found"));
        ConversationMessage edited =
                conversations.appendMessage(
                        conversation,
                        original.parentMessageId(),
                        original.role(),
                        request.content,
                        null,
                        null,
                        original.id());
        if (idempotencyKey != null
                && !idempotencyKey.isBlank()
                && !idempotency.claim(tenant(), user(), operation, idempotencyKey, edited.id())) {
            var existing =
                    idempotency
                            .find(tenant(), user(), operation, idempotencyKey)
                            .flatMap(id -> repository.findMessage(id, tenant(), user()));
            if (existing.isPresent()) return Result.success(existing.get());
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.CONFLICT, "idempotency key is already in use");
        }
        return Result.success(edited);
    }

    /**
     * 执行 消息 相关业务数据，并返回处理结果。
     *
     * @param generations 用于完成本次业务处理的 generations 参数。
     * @return 返回 消息 相关操作生成的结果数据。
     */
    @PostMapping("/{messageId}/generations")
    public Result<GenerationRun> retry(
            @PathVariable String messageId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) RetryRequest request) {
        TenantId tenant = tenant();
        long user = user();
        String operation = "message.generation:" + messageId;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing =
                    idempotency
                            .find(tenant, user, operation, idempotencyKey)
                            .flatMap(id -> generations.find(id, tenant, user));
            if (existing.isPresent()) return Result.success(existing.get());
        }
        ConversationMessage input =
                repository
                        .findMessage(messageId, tenant, user)
                        .orElseThrow(
                                () ->
                                        new org.springframework.web.server.ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "message not found"));
        if (input.role() != MessageRole.USER) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT, "only user messages can be generated");
        }
        Conversation conversation =
                repository
                        .findConversation(input.conversationId(), tenant, user)
                        .orElseThrow(
                                () ->
                                        new org.springframework.web.server.ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "conversation not found"));
        if (!messageId.equals(conversation.activeLeafMessageId())) {
            Conversation active =
                    new Conversation(
                            conversation.id(),
                            conversation.tenantId(),
                            conversation.ownerUserId(),
                            conversation.sceneType(),
                            conversation.title(),
                            conversation.status(),
                            conversation.parentConversationId(),
                            conversation.branchFromMessageId(),
                            input.id(),
                            conversation.rollingSummary(),
                            conversation.platform(),
                            conversation.model(),
                            conversation.version() + 1,
                            conversation.createdAt(),
                            java.time.Instant.now());
            if (repository.updateConversation(active, conversation.version()) != 1) {
                throw new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.CONFLICT, "conversation was modified");
            }
            conversation = active;
        }
        String platform = request == null ? null : request.platform;
        String model = request == null ? null : request.model;
        GenerationRun run;
        try {
            run = conversations.createGeneration(conversation, input, platform, model);
        } catch (IllegalStateException conflict) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.CONFLICT, conflict.getMessage(), conflict);
        }
        if (idempotencyKey != null
                && !idempotencyKey.isBlank()
                && !idempotency.claim(tenant, user, operation, idempotencyKey, run.id())) {
            var existing =
                    idempotency
                            .find(tenant, user, operation, idempotencyKey)
                            .flatMap(id -> generations.find(id, tenant, user));
            if (existing.isPresent()) return Result.success(existing.get());
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.CONFLICT, "idempotency key is already in use");
        }
        try {
            runner.start(run, tenant, user);
        } catch (
                com.shiyu.ai.conversation.implementation.application.GenerationAdmissionException
                        denied) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS, denied.errorCode(), denied);
        }
        return Result.success(run);
    }

    private TenantId tenant() {
        return new TenantId(ActorContextHttpAdapter.tenantId());
    }

    private long user() {
        return ActorContextHttpAdapter.userId();
    }

    /**
     * 封装 Edit 操作所需的请求条件和输入数据。
     */
    @Data
    public static class EditRequest {
        private String content;
    }

    /**
     * 封装 Retry 操作所需的请求条件和输入数据。
     */
    @Data
    public static class RetryRequest {
        private String platform;
        /**
         * model 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String model;
    }
}
