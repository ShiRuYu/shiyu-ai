package com.shiyu.ai.web.conversation;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.conversation.ConversationService;
import com.shiyu.ai.conversation.GenerationRunner;
import com.shiyu.ai.conversation.domain.Conversation;
import com.shiyu.ai.conversation.domain.ConversationMessage;
import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.conversation.domain.MessageRole;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.conversation.port.GenerationRepository;
import com.shiyu.ai.conversation.port.IdempotencyRepository;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Top-level message mutation endpoints kept separate from the conversation resource. */
@RestController
@RequestMapping("/messages")
public class MessageController {
    private final ConversationService conversations;
    private final ConversationRepository repository;
    private final GenerationRepository generations;
    private final GenerationRunner runner;
    private final IdempotencyRepository idempotency;

    public MessageController(ConversationService conversations, ConversationRepository repository,
                             GenerationRepository generations, GenerationRunner runner,
                             IdempotencyRepository idempotency) {
        this.conversations = conversations;
        this.repository = repository;
        this.generations = generations;
        this.runner = runner;
        this.idempotency = idempotency;
    }

    @PostMapping("/{messageId}/edits")
    public Result<ConversationMessage> edit(@PathVariable String messageId, @Valid @RequestBody EditRequest request) {
        ConversationMessage original = repository.findMessage(messageId, tenant(), user())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "message not found"));
        if (request == null || request.content == null || request.content.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "message content is required");
        }
        Conversation conversation = repository.findConversation(original.conversationId(), tenant(), user())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "conversation not found"));
        // An edit is a new node under the original parent; the old message remains
        // available as a branch candidate and is never overwritten.
        return Result.success(conversations.appendMessage(conversation, original.parentMessageId(), original.role(), request.content, null, null, original.id()));
    }

    @PostMapping("/{messageId}/generations")
    public Result<GenerationRun> retry(@PathVariable String messageId,
                                       @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                       @RequestBody(required = false) RetryRequest request) {
        long tenant = tenant();
        long user = user();
        String operation = "message.generation:" + messageId;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = idempotency.find(tenant, user, operation, idempotencyKey)
                    .flatMap(id -> generations.find(id, tenant, user));
            if (existing.isPresent()) return Result.success(existing.get());
        }
        ConversationMessage input = repository.findMessage(messageId, tenant, user)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "message not found"));
        if (input.role() != MessageRole.USER) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "only user messages can be generated");
        }
        Conversation conversation = repository.findConversation(input.conversationId(), tenant, user)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "conversation not found"));
        String platform = request == null ? null : request.platform;
        String model = request == null ? null : request.model;
        GenerationRun run;
        try {
            run = conversations.createGeneration(conversation, input, platform, model);
        } catch (IllegalStateException conflict) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, conflict.getMessage(), conflict);
        }
        if (idempotencyKey != null && !idempotencyKey.isBlank() && !idempotency.claim(tenant, user, operation, idempotencyKey, run.id())) {
            var existing = idempotency.find(tenant, user, operation, idempotencyKey)
                    .flatMap(id -> generations.find(id, tenant, user));
            if (existing.isPresent()) return Result.success(existing.get());
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "idempotency key is already in use");
        }
        try {
            runner.start(run, tenant, user);
        } catch (com.shiyu.ai.conversation.GenerationAdmissionException denied) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, denied.errorCode(), denied);
        }
        return Result.success(run);
    }

    private long tenant() { Long id = UserContextHolder.getCurrentTenantId(); if (id == null) throw new IllegalStateException("tenant context is required"); return id; }
    private long user() { Long id = UserContextHolder.getUserId(); if (id == null) throw new IllegalStateException("login is required"); return id; }

    @Data public static class EditRequest { private String content; }
    @Data public static class RetryRequest { private String platform; private String model; }
}
