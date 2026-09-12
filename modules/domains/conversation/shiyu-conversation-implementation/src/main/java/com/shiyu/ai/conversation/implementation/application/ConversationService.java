package com.shiyu.ai.conversation.implementation.application;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunEventType;
import com.shiyu.ai.agent.contract.runtime.AiRuntimePort;
import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.conversation.contract.model.GenerationStatus;
import com.shiyu.ai.conversation.implementation.domain.model.ContentPart;
import com.shiyu.ai.conversation.implementation.domain.model.Conversation;
import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;
import com.shiyu.ai.conversation.implementation.domain.model.ConversationStatus;
import com.shiyu.ai.conversation.implementation.domain.model.GenerationEvent;
import com.shiyu.ai.conversation.implementation.domain.model.GenerationEventType;
import com.shiyu.ai.conversation.implementation.domain.model.MessageRole;
import com.shiyu.ai.conversation.implementation.domain.model.MessageStatus;
import com.shiyu.ai.conversation.implementation.domain.port.ConversationRepository;
import com.shiyu.ai.conversation.implementation.domain.port.GenerationRepository;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * {@code ConversationService} 定义会话模块的应用服务能力，供上层用例调用。
 */
@Service
public class ConversationService {
    /**
     * 仓储，表示当前对象中的对应属性。
     */
    private final ConversationRepository repository;
    /**
     * 生成仓储，表示当前对象中的对应属性。
     */
    private final GenerationRepository generationRepository;

    /**
     * {@code ConversationService} 创建并初始化当前类型实例。
     *
     * @param repository 参数值，用于执行当前操作。
     * @param generationRepository 参数值，用于执行当前操作。
     */
    public ConversationService(
            ConversationRepository repository, GenerationRepository generationRepository) {
        this.repository = repository;
        this.generationRepository = generationRepository;
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param sceneType 参数值，用于执行当前操作。
     * @param title 参数值，用于执行当前操作。
     * @param platform 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param systemPrompt 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Conversation create(
            TenantId tenantId,
            long ownerUserId,
            String sceneType,
            String title,
            String platform,
            String model,
            String systemPrompt) {
        tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
        Instant now = Instant.now();
        Conversation conversation =
                new Conversation(
                        UUID.randomUUID().toString(),
                        tenantId.value(),
                        ownerUserId,
                        sceneType,
                        title == null || title.isBlank() ? "New conversation" : title.trim(),
                        ConversationStatus.ACTIVE,
                        null,
                        null,
                        null,
                        null,
                        platform,
                        model,
                        0,
                        now,
                        now);
        repository.insertConversation(conversation);
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            appendMessage(conversation, null, MessageRole.SYSTEM, systemPrompt, null, null);
            return repository
                    .findConversation(conversation.id(), tenantId, ownerUserId)
                    .orElse(conversation);
        }
        return conversation;
    }

    /**
     * {@code branch} 执行当前类型定义的业务操作。
     *
     * @param source 参数值，用于执行当前操作。
     * @param messageId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Conversation branch(Conversation source, String messageId) {
        Instant now = Instant.now();
        Conversation branch =
                new Conversation(
                        UUID.randomUUID().toString(),
                        source.tenantId(),
                        source.ownerUserId(),
                        source.sceneType(),
                        source.title(),
                        ConversationStatus.ACTIVE,
                        source.id(),
                        messageId,
                        messageId,
                        source.rollingSummary(),
                        source.platform(),
                        source.model(),
                        0,
                        now,
                        now);
        repository.insertConversation(branch);
        return branch;
    }

    /**
     * {@code appendUserMessage} 执行当前类型定义的业务操作。
     *
     * @param conversation 参数值，用于执行当前操作。
     * @param content 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ConversationMessage appendUserMessage(Conversation conversation, String content) {
        return appendMessage(
                conversation,
                conversation.activeLeafMessageId(),
                MessageRole.USER,
                content,
                null,
                null);
    }

    /**
     * {@code appendMessage} 执行当前类型定义的业务操作。
     *
     * @param conversation 参数值，用于执行当前操作。
     * @param parentMessageId 参数值，用于执行当前操作。
     * @param role 参数值，用于执行当前操作。
     * @param content 参数值，用于执行当前操作。
     * @param parts 参数值，用于执行当前操作。
     * @param toolCall 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ConversationMessage appendMessage(
            Conversation conversation,
            String parentMessageId,
            MessageRole role,
            String content,
            List<ContentPart> parts,
            java.util.Map<String, Object> toolCall) {
        return appendMessage(conversation, parentMessageId, role, content, parts, toolCall, null);
    }

    /**
     * {@code appendMessage} 执行当前类型定义的业务操作。
     *
     * @param conversation 参数值，用于执行当前操作。
     * @param parentMessageId 参数值，用于执行当前操作。
     * @param role 参数值，用于执行当前操作。
     * @param content 参数值，用于执行当前操作。
     * @param parts 参数值，用于执行当前操作。
     * @param toolCall 参数值，用于执行当前操作。
     * @param sourceMessageId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ConversationMessage appendMessage(
            Conversation conversation,
            String parentMessageId,
            MessageRole role,
            String content,
            List<ContentPart> parts,
            java.util.Map<String, Object> toolCall,
            String sourceMessageId) {
        if (conversation.status() != ConversationStatus.ACTIVE) {
            throw new IllegalStateException("conversation is not active");
        }
        List<ConversationMessage> current =
                repository.listMessages(
                        conversation.id(),
                        new TenantId(conversation.tenantId()),
                        conversation.ownerUserId(),
                        1000);
        int sequence =
                current.stream().mapToInt(ConversationMessage::sequence).max().orElse(-1) + 1;
        Instant now = Instant.now();
        ConversationMessage message =
                new ConversationMessage(
                        UUID.randomUUID().toString(),
                        conversation.id(),
                        parentMessageId,
                        sourceMessageId,
                        role,
                        parts == null ? List.of(ContentPart.text(content)) : parts,
                        toolCall,
                        MessageStatus.COMPLETED,
                        sequence,
                        null,
                        now,
                        now);
        repository.insertMessage(message);
        int updated =
                repository.updateConversation(
                        new Conversation(
                                conversation.id(),
                                conversation.tenantId(),
                                conversation.ownerUserId(),
                                conversation.sceneType(),
                                conversation.title(),
                                conversation.status(),
                                conversation.parentConversationId(),
                                conversation.branchFromMessageId(),
                                message.id(),
                                conversation.rollingSummary(),
                                conversation.platform(),
                                conversation.model(),
                                conversation.version() + 1,
                                conversation.updatedAt(),
                                now),
                        conversation.version());
        if (updated != 1) {
            repository.deleteMessage(
                    message.id(),
                    new TenantId(conversation.tenantId()),
                    conversation.ownerUserId());
            throw new IllegalStateException(
                    "conversation was modified; retry against the latest active leaf");
        }
        return message;
    }

    /**
     * {@code createGeneration} 写入或更新当前模块中的业务数据。
     *
     * @param conversation 参数值，用于执行当前操作。
     * @param userMessage 参数值，用于执行当前操作。
     * @param platform 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public GenerationRun createGeneration(
            Conversation conversation,
            ConversationMessage userMessage,
            String platform,
            String model) {
        return createGeneration(conversation, userMessage, platform, model, null);
    }

    /**
     * {@code createGeneration} 写入或更新当前模块中的业务数据。
     *
     * @param conversation 参数值，用于执行当前操作。
     * @param userMessage 参数值，用于执行当前操作。
     * @param platform 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param speakerId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public GenerationRun createGeneration(
            Conversation conversation,
            ConversationMessage userMessage,
            String platform,
            String model,
            String speakerId) {
        if (conversation == null
                || userMessage == null
                || !conversation.id().equals(userMessage.conversationId())) {
            throw new IllegalArgumentException("generation input does not belong to conversation");
        }
        if (conversation.activeLeafMessageId() != null
                && !conversation.activeLeafMessageId().equals(userMessage.id())) {
            throw new IllegalStateException("generation input is not the active conversation leaf");
        }
        if (generationRepository.hasRunning(
                conversation.id(), userMessage.id(), new TenantId(conversation.tenantId()))) {
            throw new IllegalStateException("a generation is already running for this message");
        }
        Instant now = Instant.now();
        GenerationRun generation =
                new GenerationRun(
                        UUID.randomUUID().toString(),
                        conversation.id(),
                        userMessage.id(),
                        null,
                        speakerId,
                        platform,
                        model,
                        GenerationStatus.CREATED,
                        0,
                        0,
                        0,
                        null,
                        -1,
                        false,
                        0,
                        now,
                        now);
        generationRepository.insert(generation);
        return generation;
    }

    /**
     * 追加completed生成。
     *
     * @param conversation conversation 参数。
     * @param input input 参数。
     * @param answer answer 参数。
     * @param platform platform 参数。
     * @param model model 参数。
     * @param promptTokens promptTokens 参数。
     * @param completionTokens completionTokens 参数。
     *
     * @return 处理结果。
     */
    public GenerationRun recordCompletedGeneration(
            Conversation conversation,
            ConversationMessage input,
            String answer,
            String platform,
            String model,
            long promptTokens,
            long completionTokens) {
        return recordCompletedGeneration(
                conversation,
                input,
                answer,
                platform,
                model,
                promptTokens,
                completionTokens,
                null,
                null);
    }

    /**
     * 追加completed生成。
     *
     * @param conversation conversation 参数。
     * @param input input 参数。
     * @param answer answer 参数。
     * @param platform platform 参数。
     * @param model model 参数。
     * @param promptTokens promptTokens 参数。
     * @param completionTokens completionTokens 参数。
     * @param runtime runtime 参数。
     * @param runtimeRun runtimeRun 参数。
     *
     * @return 处理结果。
     */
    public GenerationRun recordCompletedGeneration(
            Conversation conversation,
            ConversationMessage input,
            String answer,
            String platform,
            String model,
            long promptTokens,
            long completionTokens,
            AiRuntimePort runtime,
            AiRun runtimeRun) {
        conversation =
                repository
                        .findConversation(
                                conversation.id(),
                                new TenantId(conversation.tenantId()),
                                conversation.ownerUserId())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        GenerationRun created = createGeneration(conversation, input, platform, model);
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        if (generationRepository.update(running, created.version()) != 1) {
            throw new IllegalStateException("stored generation admission conflict");
        }
        Instant now = Instant.now();
        String content = answer == null ? "" : answer;
        ConversationMessage assistant =
                new ConversationMessage(
                        UUID.randomUUID().toString(),
                        conversation.id(),
                        input.id(),
                        null,
                        MessageRole.ASSISTANT,
                        List.of(ContentPart.text(content)),
                        java.util.Map.of(),
                        MessageStatus.COMPLETED,
                        input.sequence() + 1,
                        created.id(),
                        now,
                        now);
        repository.insertMessage(assistant);
        Conversation latest =
                repository
                        .findConversation(
                                conversation.id(),
                                new TenantId(conversation.tenantId()),
                                conversation.ownerUserId())
                        .orElse(conversation);
        Conversation updated =
                new Conversation(
                        latest.id(),
                        latest.tenantId(),
                        latest.ownerUserId(),
                        latest.sceneType(),
                        latest.title(),
                        latest.status(),
                        latest.parentConversationId(),
                        latest.branchFromMessageId(),
                        assistant.id(),
                        latest.rollingSummary(),
                        latest.platform(),
                        latest.model(),
                        latest.version() + 1,
                        latest.createdAt(),
                        now);
        if (repository.updateConversation(updated, latest.version()) != 1) {
            repository.deleteMessage(
                    assistant.id(),
                    new TenantId(conversation.tenantId()),
                    conversation.ownerUserId());
            GenerationRun failed = running.transition(GenerationStatus.FAILED);
            GenerationRun failedState =
                    new GenerationRun(
                            failed.id(),
                            failed.conversationId(),
                            failed.inputMessageId(),
                            null,
                            failed.speakerId(),
                            failed.platform(),
                            failed.model(),
                            failed.status(),
                            failed.promptTokens(),
                            failed.completionTokens(),
                            java.time.Duration.between(created.createdAt(), Instant.now())
                                    .toMillis(),
                            "conversation_modified",
                            failed.lastEventSequence(),
                            false,
                            failed.version(),
                            failed.createdAt(),
                            Instant.now());
            generationRepository.update(failedState, running.version());
            if (runtime == null || runtimeRun == null) {
                generationRepository.appendEvent(
                        new GenerationEvent(
                                created.id(), 0, GenerationEventType.STARTED, "{}", now),
                        new TenantId(conversation.tenantId()));
                generationRepository.appendEvent(
                        new GenerationEvent(
                                created.id(),
                                1,
                                GenerationEventType.FAILED,
                                "conversation_modified",
                                Instant.now()),
                        new TenantId(conversation.tenantId()));
            }
            if (runtime != null && runtimeRun != null) {
                try {
                    runtime.finish(
                            runtimeRun.id(),
                            runtimeRun.tenantId(),
                            runtimeRun.ownerUserId().value(),
                            com.shiyu.ai.agent.contract.runtime.AiRunStatus.FAILED,
                            "conversation_modified");
                } catch (RuntimeException ignored) {
                }
            }
            throw new IllegalStateException("conversation was modified while storing generation");
        }
        if (runtime != null && runtimeRun != null) {
            runtimeRun = runtime.linkGeneration(runtimeRun, created.id());
            runtime.append(runtimeRun, AiRunEventType.MODEL_DELTA, "{}", true);
            runtime.append(
                    runtimeRun,
                    AiRunEventType.MODEL_USAGE,
                    "{\"promptTokens\":"
                            + promptTokens
                            + ",\"completionTokens\":"
                            + completionTokens
                            + ",\"estimated\":false}",
                    true);
            runtime.recordUsage(
                    runtimeRun.id(),
                    runtimeRun.tenantId(),
                    runtimeRun.ownerUserId().value(),
                    promptTokens,
                    completionTokens,
                    false,
                    null);
            runtime.append(runtimeRun, AiRunEventType.MODEL_COMPLETED, "{}", true);
            runtime.finish(
                    runtimeRun.id(),
                    runtimeRun.tenantId(),
                    runtimeRun.ownerUserId().value(),
                    com.shiyu.ai.agent.contract.runtime.AiRunStatus.COMPLETED,
                    null);
        } else {
            generationRepository.appendEvent(
                    new GenerationEvent(created.id(), 0, GenerationEventType.STARTED, "{}", now),
                    new TenantId(conversation.tenantId()));
            if (!content.isEmpty())
                generationRepository.appendEvent(
                        new GenerationEvent(
                                created.id(), 1, GenerationEventType.DELTA, content, now),
                        new TenantId(conversation.tenantId()));
            generationRepository.appendEvent(
                    new GenerationEvent(
                            created.id(),
                            2,
                            GenerationEventType.USAGE,
                            "{\"promptTokens\":"
                                    + promptTokens
                                    + ",\"completionTokens\":"
                                    + completionTokens
                                    + ",\"estimated\":false}",
                            now),
                    new TenantId(conversation.tenantId()));
        }
        GenerationRun completed = running.transition(GenerationStatus.COMPLETED);
        completed =
                new GenerationRun(
                        completed.id(),
                        completed.conversationId(),
                        completed.inputMessageId(),
                        assistant.id(),
                        completed.speakerId(),
                        completed.platform(),
                        completed.model(),
                        completed.status(),
                        promptTokens,
                        completionTokens,
                        java.time.Duration.between(created.createdAt(), now).toMillis(),
                        null,
                        3,
                        false,
                        completed.version(),
                        completed.createdAt(),
                        now);
        if (runtimeRun != null) completed = completed.withRuntimeRunId(runtimeRun.id());
        if (generationRepository.update(completed, running.version()) != 1) {
            throw new IllegalStateException("stored generation completion conflict");
        }
        if (runtime == null || runtimeRun == null) {
            generationRepository.appendEvent(
                    new GenerationEvent(created.id(), 3, GenerationEventType.COMPLETED, "{}", now),
                    new TenantId(conversation.tenantId()));
        }
        return completed;
    }
}
