package com.shiyu.ai.conversation;

import com.shiyu.ai.conversation.domain.Conversation;
import com.shiyu.ai.conversation.domain.ConversationMessage;
import com.shiyu.ai.conversation.domain.ConversationStatus;
import com.shiyu.ai.conversation.domain.ContentPart;
import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.conversation.domain.GenerationEvent;
import com.shiyu.ai.conversation.domain.GenerationEventType;
import com.shiyu.ai.conversation.domain.GenerationStatus;
import com.shiyu.ai.conversation.domain.MessageRole;
import com.shiyu.ai.conversation.domain.MessageStatus;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.conversation.port.GenerationRepository;
import com.shiyu.ai.runtime.AiRun;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.runtime.AiRunEventType;
import com.shiyu.ai.runtime.AiRuntimePort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

@Service
public class ConversationService {
    private final ConversationRepository repository;
    private final GenerationRepository generationRepository;

    public ConversationService(ConversationRepository repository, GenerationRepository generationRepository) {
        this.repository = repository;
        this.generationRepository = generationRepository;
    }

    public Conversation create(TenantId tenantId, long ownerUserId, String sceneType, String title,
                               String platform, String model, String systemPrompt) {
        tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
        Instant now = Instant.now();
        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(), tenantId.value(), ownerUserId, sceneType,
                title == null || title.isBlank() ? "New conversation" : title.trim(),
                ConversationStatus.ACTIVE, null, null, null, null, platform, model, 0, now, now);
        repository.insertConversation(conversation);
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            appendMessage(conversation, null, MessageRole.SYSTEM, systemPrompt, null, null);
            return repository.findConversation(conversation.id(), tenantId, ownerUserId).orElse(conversation);
        }
        return conversation;
    }

    public Conversation branch(Conversation source, String messageId) {
        Instant now = Instant.now();
        Conversation branch = new Conversation(UUID.randomUUID().toString(), source.tenantId(), source.ownerUserId(), source.sceneType(), source.title(), ConversationStatus.ACTIVE, source.id(), messageId, messageId, source.rollingSummary(), source.platform(), source.model(), 0, now, now);
        repository.insertConversation(branch);
        return branch;
    }

    public ConversationMessage appendUserMessage(Conversation conversation, String content) {
        return appendMessage(conversation, conversation.activeLeafMessageId(), MessageRole.USER, content, null, null);
    }

    public ConversationMessage appendMessage(Conversation conversation, String parentMessageId, MessageRole role,
                                              String content, List<ContentPart> parts, java.util.Map<String, Object> toolCall) {
        return appendMessage(conversation, parentMessageId, role, content, parts, toolCall, null);
    }

    public ConversationMessage appendMessage(Conversation conversation, String parentMessageId, MessageRole role,
                                              String content, List<ContentPart> parts, java.util.Map<String, Object> toolCall,
                                              String sourceMessageId) {
        if (conversation.status() != ConversationStatus.ACTIVE) {
            throw new IllegalStateException("conversation is not active");
        }
        List<ConversationMessage> current = repository.listMessages(
                conversation.id(), new TenantId(conversation.tenantId()), conversation.ownerUserId(), 1000);
        int sequence = current.stream().mapToInt(ConversationMessage::sequence).max().orElse(-1) + 1;
        Instant now = Instant.now();
        ConversationMessage message = new ConversationMessage(
                UUID.randomUUID().toString(), conversation.id(),
                parentMessageId, sourceMessageId, role,
                parts == null ? List.of(ContentPart.text(content)) : parts,
                toolCall, MessageStatus.COMPLETED, sequence, null, now, now);
        repository.insertMessage(message);
        int updated = repository.updateConversation(new Conversation(conversation.id(), conversation.tenantId(), conversation.ownerUserId(),
                conversation.sceneType(), conversation.title(), conversation.status(), conversation.parentConversationId(),
                conversation.branchFromMessageId(), message.id(), conversation.rollingSummary(), conversation.platform(),
                conversation.model(), conversation.version() + 1, conversation.updatedAt(), now), conversation.version());
        if (updated != 1) {
            // The message insert and conversation CAS are separate repository
            // operations. Remove the newly-created node when another writer
            // wins the conversation version, otherwise a failed append leaves
            // an unreachable message in the source-of-truth table.
            repository.deleteMessage(message.id(), new TenantId(conversation.tenantId()), conversation.ownerUserId());
            throw new IllegalStateException("conversation was modified; retry against the latest active leaf");
        }
        return message;
    }

    public GenerationRun createGeneration(Conversation conversation, ConversationMessage userMessage,
                                       String platform, String model) {
        return createGeneration(conversation, userMessage, platform, model, null);
    }

    public GenerationRun createGeneration(Conversation conversation, ConversationMessage userMessage,
                                          String platform, String model, String speakerId) {
        if (conversation == null || userMessage == null || !conversation.id().equals(userMessage.conversationId())) {
            throw new IllegalArgumentException("generation input does not belong to conversation");
        }
        if (conversation.activeLeafMessageId() != null && !conversation.activeLeafMessageId().equals(userMessage.id())) {
            throw new IllegalStateException("generation input is not the active conversation leaf");
        }
        if (generationRepository.hasRunning(conversation.id(), userMessage.id(), new TenantId(conversation.tenantId()))) {
            throw new IllegalStateException("a generation is already running for this message");
        }
        Instant now = Instant.now();
        GenerationRun generation = new GenerationRun(UUID.randomUUID().toString(), conversation.id(), userMessage.id(),
                null, speakerId, platform, model, GenerationStatus.CREATED, 0, 0, 0, null, -1, false, 0, now, now);
        generationRepository.insert(generation);
        return generation;
    }

    /** Persist a completed stateless-facade request when an API caller explicitly opts into store=true. */
    public GenerationRun recordCompletedGeneration(Conversation conversation, ConversationMessage input,
                                                   String answer, String platform, String model,
                                                   long promptTokens, long completionTokens) {
        return recordCompletedGeneration(conversation, input, answer, platform, model, promptTokens, completionTokens, null, null);
    }

    /**
     * Persists an explicitly stored OpenAI-compatible response while projecting
     * its generation facts into the already-created Runtime run. Runtime is the
     * physical event source; the GenerationRepository remains the durable
     * generation projection for deployments that intentionally omit Runtime.
     */
    public GenerationRun recordCompletedGeneration(Conversation conversation, ConversationMessage input,
                                                   String answer, String platform, String model,
                                                   long promptTokens, long completionTokens,
                                                   AiRuntimePort runtime, AiRun runtimeRun) {
        conversation = repository.findConversation(conversation.id(), new TenantId(conversation.tenantId()), conversation.ownerUserId())
                .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        GenerationRun created = createGeneration(conversation, input, platform, model);
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        if (generationRepository.update(running, created.version()) != 1) {
            throw new IllegalStateException("stored generation admission conflict");
        }
        Instant now = Instant.now();
        String content = answer == null ? "" : answer;
        ConversationMessage assistant = new ConversationMessage(UUID.randomUUID().toString(), conversation.id(), input.id(), null,
                MessageRole.ASSISTANT, List.of(ContentPart.text(content)), java.util.Map.of(), MessageStatus.COMPLETED,
                input.sequence() + 1, created.id(), now, now);
        repository.insertMessage(assistant);
        Conversation latest = repository.findConversation(conversation.id(), new TenantId(conversation.tenantId()), conversation.ownerUserId()).orElse(conversation);
        Conversation updated = new Conversation(latest.id(), latest.tenantId(), latest.ownerUserId(), latest.sceneType(), latest.title(), latest.status(),
                latest.parentConversationId(), latest.branchFromMessageId(), assistant.id(), latest.rollingSummary(), latest.platform(), latest.model(),
                latest.version() + 1, latest.createdAt(), now);
        if (repository.updateConversation(updated, latest.version()) != 1) {
            repository.deleteMessage(assistant.id(), new TenantId(conversation.tenantId()), conversation.ownerUserId());
            GenerationRun failed = running.transition(GenerationStatus.FAILED);
            GenerationRun failedState = new GenerationRun(failed.id(), failed.conversationId(), failed.inputMessageId(), null,
                    failed.speakerId(), failed.platform(), failed.model(), failed.status(), failed.promptTokens(), failed.completionTokens(),
                    java.time.Duration.between(created.createdAt(), Instant.now()).toMillis(), "conversation_modified",
                    failed.lastEventSequence(), false, failed.version(), failed.createdAt(), Instant.now());
            generationRepository.update(failedState, running.version());
            if (runtime == null || runtimeRun == null) {
                generationRepository.appendEvent(new GenerationEvent(created.id(), 0, GenerationEventType.STARTED, "{}", now), new TenantId(conversation.tenantId()));
                generationRepository.appendEvent(new GenerationEvent(created.id(), 1, GenerationEventType.FAILED, "conversation_modified", Instant.now()), new TenantId(conversation.tenantId()));
            }
            if (runtime != null && runtimeRun != null) {
                try { runtime.finish(runtimeRun.id(), runtimeRun.tenantId(), runtimeRun.ownerUserId().value(), com.shiyu.ai.runtime.AiRunStatus.FAILED, "conversation_modified"); }
                catch (RuntimeException ignored) { }
            }
            throw new IllegalStateException("conversation was modified while storing generation");
        }
        if (runtime != null && runtimeRun != null) {
            runtimeRun = runtime.linkGeneration(runtimeRun, created.id());
            runtime.append(runtimeRun, AiRunEventType.MODEL_DELTA, "{}", true);
            runtime.append(runtimeRun, AiRunEventType.MODEL_USAGE,
                    "{\"promptTokens\":" + promptTokens + ",\"completionTokens\":" + completionTokens + ",\"estimated\":false}", true);
            runtime.recordUsage(runtimeRun.id(), runtimeRun.tenantId(), runtimeRun.ownerUserId().value(), promptTokens, completionTokens, false, null);
            runtime.append(runtimeRun, AiRunEventType.MODEL_COMPLETED, "{}", true);
            runtime.finish(runtimeRun.id(), runtimeRun.tenantId(), runtimeRun.ownerUserId().value(), com.shiyu.ai.runtime.AiRunStatus.COMPLETED, null);
        } else {
            generationRepository.appendEvent(new GenerationEvent(created.id(), 0, GenerationEventType.STARTED, "{}", now), new TenantId(conversation.tenantId()));
            if (!content.isEmpty()) generationRepository.appendEvent(new GenerationEvent(created.id(), 1, GenerationEventType.DELTA, content, now), new TenantId(conversation.tenantId()));
            generationRepository.appendEvent(new GenerationEvent(created.id(), 2, GenerationEventType.USAGE,
                    "{\"promptTokens\":" + promptTokens + ",\"completionTokens\":" + completionTokens + ",\"estimated\":false}", now), new TenantId(conversation.tenantId()));
        }
        GenerationRun completed = running.transition(GenerationStatus.COMPLETED);
        completed = new GenerationRun(completed.id(), completed.conversationId(), completed.inputMessageId(), assistant.id(), completed.speakerId(),
                completed.platform(), completed.model(), completed.status(), promptTokens, completionTokens,
                java.time.Duration.between(created.createdAt(), now).toMillis(), null, 3, false, completed.version(), completed.createdAt(), now);
        if (runtimeRun != null) completed = completed.withRuntimeRunId(runtimeRun.id());
        if (generationRepository.update(completed, running.version()) != 1) {
            throw new IllegalStateException("stored generation completion conflict");
        }
        if (runtime == null || runtimeRun == null) {
            generationRepository.appendEvent(new GenerationEvent(created.id(), 3, GenerationEventType.COMPLETED, "{}", now), new TenantId(conversation.tenantId()));
        }
        return completed;
    }
}
