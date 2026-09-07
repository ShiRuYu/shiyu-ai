package com.shiyu.ai.conversation.web;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.conversation.ConversationService;
import com.shiyu.ai.conversation.GenerationRunner;
import com.shiyu.ai.conversation.domain.Conversation;
import com.shiyu.ai.conversation.domain.ConversationMessage;
import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.conversation.domain.MessageRole;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.conversation.port.GenerationRepository;
import com.shiyu.ai.conversation.port.IdempotencyRepository;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Top-level message mutation endpoints kept separate from the conversation resource. */
@RestController
@RequestMapping("/api/conversation/messages")
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
    public Result<ConversationMessage> edit(@PathVariable String messageId,
                                             @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                             @Valid @RequestBody EditRequest request) {
        String operation = "message.edit:" + messageId;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = idempotency.find(tenant(), user(), operation, idempotencyKey)
                    .flatMap(id -> repository.findMessage(id, tenant(), user()));
            if (existing.isPresent()) return Result.success(existing.get());
        }
        ConversationMessage original = repository.findMessage(messageId, tenant(), user())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "message not found"));
        if (request == null || request.content == null || request.content.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "message content is required");
        }
        Conversation conversation = repository.findConversation(original.conversationId(), tenant(), user())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "conversation not found"));
        // An edit is a new node under the original parent; the old message remains
        // available as a branch candidate and is never overwritten.
        ConversationMessage edited = conversations.appendMessage(conversation, original.parentMessageId(), original.role(), request.content, null, null, original.id());
        if (idempotencyKey != null && !idempotencyKey.isBlank() && !idempotency.claim(tenant(), user(), operation, idempotencyKey, edited.id())) {
            var existing = idempotency.find(tenant(), user(), operation, idempotencyKey)
                    .flatMap(id -> repository.findMessage(id, tenant(), user()));
            if (existing.isPresent()) return Result.success(existing.get());
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "idempotency key is already in use");
        }
        return Result.success(edited);
    }

    @PostMapping("/{messageId}/generations")
    public Result<GenerationRun> retry(@PathVariable String messageId,
                                       @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                       @RequestBody(required = false) RetryRequest request) {
        TenantId tenant = tenant();
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
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "only user messages can be generated");
        }
        Conversation conversation = repository.findConversation(input.conversationId(), tenant, user)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "conversation not found"));
        // Retrying a user turn creates a sibling assistant candidate. Make the
        // retried user node the active leaf before assembling the prompt so the
        // model does not accidentally include the previously selected answer.
        if (!messageId.equals(conversation.activeLeafMessageId())) {
            Conversation active = new Conversation(conversation.id(), conversation.tenantId(), conversation.ownerUserId(),
                    conversation.sceneType(), conversation.title(), conversation.status(), conversation.parentConversationId(),
                    conversation.branchFromMessageId(), input.id(), conversation.rollingSummary(), conversation.platform(),
                    conversation.model(), conversation.version() + 1, conversation.createdAt(), java.time.Instant.now());
            if (repository.updateConversation(active, conversation.version()) != 1) {
                throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "conversation was modified");
            }
            conversation = active;
        }
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

    private TenantId tenant() { return new TenantId(ActorContextHttpAdapter.tenantId()); }
    private long user() { return ActorContextHttpAdapter.userId(); }

    @Data public static class EditRequest { private String content; }
    @Data public static class RetryRequest { private String platform; private String model; }
}
