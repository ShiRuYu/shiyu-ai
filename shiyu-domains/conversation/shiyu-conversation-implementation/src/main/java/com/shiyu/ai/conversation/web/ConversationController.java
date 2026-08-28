package com.shiyu.ai.conversation.web;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.conversation.ConversationService;
import com.shiyu.ai.conversation.GenerationRunner;
import com.shiyu.ai.conversation.PromptSafety;
import com.shiyu.ai.conversation.PromptAssembler;
import com.shiyu.ai.conversation.ConversationPromptService;
import com.shiyu.ai.runtime.ContextItem;
import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.conversation.port.IdempotencyRepository;
import com.shiyu.ai.conversation.port.GenerationRepository;
import com.shiyu.ai.model.chat.ChatMessage;
import com.shiyu.ai.kernel.context.TenantId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import org.springframework.http.MediaType;

@Tag(name = "Conversation Platform")
@RestController
@RequestMapping("/api/conversation/conversations")
public class ConversationController {
    private final ConversationService conversations;
    private final ConversationRepository conversationRepository;
    private final GenerationRunner generationRunner;
    private final IdempotencyRepository idempotency;
    private final GenerationRepository generationRepository;
    private final ConversationImportPreviewStore importPreviews;
    private final ConversationPromptService promptService;

    public ConversationController(ConversationService conversations, ConversationRepository conversationRepository, GenerationRunner generationRunner, IdempotencyRepository idempotency, GenerationRepository generationRepository, ConversationImportPreviewStore importPreviews, ConversationPromptService promptService) {
        this.conversations = conversations;
        this.conversationRepository = conversationRepository;
        this.generationRunner = generationRunner;
        this.idempotency = idempotency;
        this.generationRepository = generationRepository;
        this.importPreviews = importPreviews;
        this.promptService = promptService;
    }

    @PostMapping
    public Result<Conversation> create(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @Valid @RequestBody CreateConversationRequest request) {
        long userId = currentUser();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = idempotency.find(currentTenant(), userId, "conversation.create", idempotencyKey)
                    .flatMap(resource -> conversationRepository.findConversation(resource, currentTenant(), userId));
            if (existing.isPresent()) return Result.success(existing.get());
        }
        Conversation created = conversations.create(currentTenant(), userId, request.sceneType, request.title, request.platform, request.model, request.systemPrompt);
        if (idempotencyKey != null && !idempotencyKey.isBlank() && !idempotency.claim(currentTenant(), userId, "conversation.create", idempotencyKey, created.id())) {
            return idempotency.find(currentTenant(), userId, "conversation.create", idempotencyKey).flatMap(resource -> conversationRepository.findConversation(resource, currentTenant(), userId)).map(Result::success).orElse(Result.success(created));
        }
        return Result.success(created);
    }

    @GetMapping
    public Result<List<Conversation>> list(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset) {
        return Result.success(conversationRepository.listConversations(currentTenant(), currentUser(), limit, offset));
    }

    @GetMapping("/{id}")
    public Result<Conversation> detail(@PathVariable String id) {
        return Result.success(conversationRepository.findConversation(id, currentTenant(), currentUser()).orElseThrow(() -> new IllegalArgumentException("conversation not found")));
    }

    @GetMapping("/{id}/messages")
    public Result<List<ConversationMessage>> messages(@PathVariable String id, @RequestParam(defaultValue = "1000") int limit) {
        conversationRepository.findConversation(id, currentTenant(), currentUser()).orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        return Result.success(conversationRepository.listMessages(id, currentTenant(), currentUser(), limit).reversed());
    }

    @PatchMapping("/{id}")
    public Result<Conversation> update(@PathVariable String id, @RequestBody UpdateConversationRequest request) {
        Conversation current = conversationRepository.findConversation(id, currentTenant(), currentUser()).orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        ConversationStatus status = request.status == null ? current.status() : ConversationStatus.valueOf(request.status.toUpperCase());
        Conversation next = new Conversation(current.id(), current.tenantId(), current.ownerUserId(), current.sceneType(), request.title == null ? current.title() : request.title,
                status, current.parentConversationId(), current.branchFromMessageId(), current.activeLeafMessageId(), current.rollingSummary(), current.platform(), current.model(), current.version() + 1, current.createdAt(), java.time.Instant.now());
        if (conversationRepository.updateConversation(next, current.version()) != 1) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "conversation was modified");
        return Result.success(next);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) { if (conversationRepository.deleteConversation(id, currentTenant(), currentUser()) != 1) throw new IllegalArgumentException("conversation not found"); return Result.success(); }

    @PostMapping("/{id}/messages")
    public Result<GenerationRun> message(@PathVariable String id, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @Valid @RequestBody MessageRequest request) {
        return messageInternal(id, idempotencyKey, request);
    }

    private Result<GenerationRun> messageInternal(String id, String idempotencyKey, MessageRequest request) {
        if (request == null || request.content == null || request.content.isBlank()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "message content is required");
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = idempotency.find(currentTenant(), currentUser(), "conversation.message:" + id, idempotencyKey)
                    .flatMap(resource -> generations().find(resource, currentTenant(), currentUser()));
            if (existing.isPresent()) return Result.success(existing.get());
        }
        Conversation conversation = conversationRepository.findConversation(id, currentTenant(), currentUser()).orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        ConversationMessage message = conversations.appendUserMessage(conversation, request.content);
        // appendUserMessage advances the conversation's optimistic-lock
        // version and active leaf. Refresh before admission so the generation
        // is created against the exact leaf that was persisted, even when a
        // different request won a concurrent update.
        conversation = conversationRepository.findConversation(id, currentTenant(), currentUser())
                .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        GenerationRun run;
        try { run = conversations.createGeneration(conversation, message, request.platform, request.model); }
        catch (IllegalStateException conflict) { throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, conflict.getMessage(), conflict); }
        if (idempotencyKey != null && !idempotencyKey.isBlank() && !idempotency.claim(currentTenant(), currentUser(), "conversation.message:" + id, idempotencyKey, run.id())) {
            var existing = idempotency.find(currentTenant(), currentUser(), "conversation.message:" + id, idempotencyKey)
                    .flatMap(resource -> generations().find(resource, currentTenant(), currentUser()));
            if (existing.isPresent()) return Result.success(existing.get());
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "idempotency key is already in use");
        }
        try { generationRunner.start(run, currentTenant(), currentUser()); }
        catch (com.shiyu.ai.conversation.GenerationAdmissionException denied) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, denied.errorCode(), denied);
        }
        return Result.success(run);
    }

    @PostMapping("/{id}/generations")
    public Result<GenerationRun> generation(@PathVariable String id, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @Valid @RequestBody MessageRequest request) { return message(id, idempotencyKey, request); }

    private GenerationRepository generations() { return generationRepository; }

    @PostMapping("/{id}/active-leaf")
    public Result<Void> activeLeaf(@PathVariable String id, @RequestParam(required = false) String messageId, @RequestBody(required = false) ActiveLeafRequest body) {
        if (messageId == null && body != null) messageId = body.messageId;
        if (messageId == null || messageId.isBlank()) throw new IllegalArgumentException("messageId is required");
        Conversation c = conversationRepository.findConversation(id, currentTenant(), currentUser()).orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        conversationRepository.findMessage(messageId, currentTenant(), currentUser()).filter(m -> id.equals(m.conversationId())).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "message does not belong to conversation"));
        conversationRepository.updateConversation(new Conversation(c.id(), c.tenantId(), c.ownerUserId(), c.sceneType(), c.title(), c.status(), c.parentConversationId(), c.branchFromMessageId(), messageId, c.rollingSummary(), c.platform(), c.model(), c.version() + 1, c.createdAt(), java.time.Instant.now()), c.version());
        return Result.success();
    }

    @GetMapping("/{id}/prompt-preview")
    public Result<PromptPreview> promptPreview(@PathVariable String id) {
        Conversation c = conversationRepository.findConversation(id, currentTenant(), currentUser()).orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        ConversationPromptService.PromptAssembly assembly = promptService.assemble(c,
                conversationRepository.listMessages(id, currentTenant(), currentUser(), 1000).reversed(), currentTenant(), currentUser());
        List<ConversationMessage> messages = assembly.conversationMessages();
        long estimated = assembly.modelMessages().stream().mapToLong(m -> m.content().stream().mapToLong(p -> PromptSafety.estimateTokens(p.text())).sum()).sum();
        // Keep the source breakdown in the same order as the actual structured
        // request: system instructions, retrieved context, then the active
        // conversation path.  The context is intentionally shown as its own
        // cited segments even though the provider receives one bounded system
        // message containing those references.
        List<PromptSegment> segments = new java.util.ArrayList<>();
        messages.stream().filter(m -> m.role() == MessageRole.SYSTEM)
                .forEach(m -> segments.add(new PromptSegment(m.role().name(), m.textContent(), PromptSafety.estimateTokens(m.textContent()))));
        for (ContextItem item : assembly.contextItems()) segments.add(new PromptSegment(item.sourceType() + ":" + item.sourceId(), item.content(), PromptSafety.estimateTokens(item.content())));
        messages.stream().filter(m -> m.role() != MessageRole.SYSTEM)
                .forEach(m -> segments.add(new PromptSegment(m.role().name(), m.textContent(), PromptSafety.estimateTokens(m.textContent()))));
        String canonicalPrompt = JSONUtils.toJsonString(assembly.modelMessages());
        return Result.success(new PromptPreview(messages, segments, estimated, false, true, "local-character-estimate", java.util.Map.of("platform", c.platform(), "model", c.model()), sha256(canonicalPrompt), assembly.contextItems(), assembly.contextTrace()));
    }

    @PostMapping("/{id}/branches")
    public Result<Conversation> branch(@PathVariable String id, @RequestParam String messageId) {
        Conversation source = conversationRepository.findConversation(id, currentTenant(), currentUser()).orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        conversationRepository.findMessage(messageId, currentTenant(), currentUser()).filter(m -> id.equals(m.conversationId())).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "message does not belong to conversation"));
        return Result.success(conversations.branch(source, messageId));
    }

    @GetMapping("/{id}/branches")
    public Result<List<Conversation>> branches(@PathVariable String id) {
        conversationRepository.findConversation(id, currentTenant(), currentUser()).orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        return Result.success(conversationRepository.listBranches(id, currentTenant(), currentUser()));
    }

    @GetMapping(value = "/{id}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object export(@PathVariable String id, @RequestParam(required = false) String format) {
        conversationRepository.findConversation(id, currentTenant(), currentUser()).orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        List<ConversationMessage> messages = conversationRepository.listMessages(id, currentTenant(), currentUser(), 1000).reversed();
        if ("jsonl".equalsIgnoreCase(format)) return org.springframework.http.ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(com.shiyu.ai.conversation.chat.ConversationExchangeCodec.toJsonl(messages));
        if ("markdown".equalsIgnoreCase(format) || "md".equalsIgnoreCase(format)) return org.springframework.http.ResponseEntity.ok().contentType(MediaType.TEXT_MARKDOWN).body(com.shiyu.ai.conversation.chat.ConversationExchangeCodec.toMarkdown(messages));
        return Result.success(messages);
    }

    /** Imports JSONL or Markdown into a new conversation after the caller confirms the preview. */
    @PostMapping(value = "/import", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public Result<Conversation> importConversation(@RequestBody ImportRequest request) {
        validateImportPayload(request);
        List<com.shiyu.ai.conversation.chat.ConversationExchangeCodec.ImportedMessage> imported = importPreviews.consume(
                currentTenant(), currentUser(), request.previewToken, request.format, request.content);
        Conversation conversation = conversations.create(currentTenant(), currentUser(), request.sceneType, request.title,
                request.platform, request.model, request.systemPrompt);
        for (var item : imported) {
            MessageRole role;
            try { role = MessageRole.valueOf(item.role().toUpperCase(java.util.Locale.ROOT)); }
            catch (IllegalArgumentException ex) { role = MessageRole.USER; }
            Conversation current = conversationRepository.findConversation(conversation.id(), currentTenant(), currentUser()).orElseThrow();
            conversations.appendMessage(current, current.activeLeafMessageId(), role, item.content(), null, null);
        }
        return Result.success(conversationRepository.findConversation(conversation.id(), currentTenant(), currentUser()).orElse(conversation));
    }

    @PostMapping("/import/preview")
    public Result<ConversationImportPreviewStore.Preview> importPreview(@RequestBody ImportRequest request) {
        validateImportPayload(request);
        String format = request.format == null ? "jsonl" : request.format.toLowerCase(java.util.Locale.ROOT);
        List<com.shiyu.ai.conversation.chat.ConversationExchangeCodec.ImportedMessage> imported = "markdown".equals(format) || "md".equals(format)
                ? com.shiyu.ai.conversation.chat.ConversationExchangeCodec.fromMarkdown(request.content)
                : com.shiyu.ai.conversation.chat.ConversationExchangeCodec.fromJsonl(request.content);
        if (imported.size() > 10_000) throw new IllegalArgumentException("import contains too many messages");
        return Result.success(importPreviews.issue(currentTenant(), currentUser(), format, request.content, imported));
    }

    private void validateImportPayload(ImportRequest request) {
        if (request == null || request.content == null || request.content.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "import content is required");
        }
        if (request.content.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 4 * 1024 * 1024) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "import payload exceeds 4 MiB");
        }
    }

    private TenantId currentTenant() { return new TenantId(ActorContextHttpAdapter.tenantId()); }
    private long currentUser() { return ActorContextHttpAdapter.userId(); }

    @Data public static class CreateConversationRequest { private String sceneType = "general"; private String title; private String platform; private String model; private String systemPrompt; }
    @Data public static class MessageRequest { private String content; private String platform; private String model; }
    @Data public static class ActiveLeafRequest { private String messageId; }
    @Data public static class UpdateConversationRequest { private String title; private String status; }
    @Data public static class ImportRequest { private String format = "jsonl"; private String previewToken; private String title; private String sceneType = "general"; private String platform; private String model; private String systemPrompt; private String content; }
    public record PromptSegment(String source, String content, long estimatedTokens) {}
    public record PromptPreview(List<ConversationMessage> messages, List<PromptSegment> sources, long estimatedTokens,
                                boolean truncated, boolean estimated, String estimator, java.util.Map<String, Object> modelParameters,
                                String promptHash, List<ContextItem> contextItems, com.shiyu.ai.runtime.ContextTrace contextTrace) {
        public PromptPreview {
            contextItems = contextItems == null ? List.of() : List.copyOf(contextItems);
        }
    }

    private ChatMessage toModelMessage(ConversationMessage message) {
        return new ChatMessage(message.role().name().toLowerCase(), message.contentParts().stream()
                .map(p -> new ChatMessage.ContentPart(p.type(), p.text(), p.mediaUri(), p.mimeType())).toList());
    }

    private String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException ex) {
            throw new IllegalStateException("unable to hash prompt", ex);
        }
    }
}
