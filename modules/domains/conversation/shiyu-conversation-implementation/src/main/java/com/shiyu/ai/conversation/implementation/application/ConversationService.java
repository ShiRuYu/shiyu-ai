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
import com.shiyu.ai.conversation.implementation.domain.port.IdempotencyRepository;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 提供 会话 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
    private final IdempotencyRepository idempotencyRepository;

    /**
     * 执行 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param repository 用于完成本次业务处理的 repository 参数。
     * @param generationRepository 用于完成本次业务处理的 generationRepository 参数。
     */
    public ConversationService(
            ConversationRepository repository, GenerationRepository generationRepository) {
        this(repository, generationRepository, null);
    }

    @Autowired
    public ConversationService(
            ConversationRepository repository,
            GenerationRepository generationRepository,
            IdempotencyRepository idempotencyRepository) {
        this.repository = repository;
        this.generationRepository = generationRepository;
        this.idempotencyRepository = idempotencyRepository;
    }

    /**
     * 创建或保存 会话 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param sceneType 用于完成本次业务处理的 sceneType 参数。
     * @param title 用于完成本次业务处理的 title 参数。
     * @param platform 用于完成本次业务处理的 platform 参数。
     * @param model 用于完成本次业务处理的 model 参数。
     * @param systemPrompt 用于完成本次业务处理的 systemPrompt 参数。
     * @return 返回 会话 相关操作生成的结果数据。
     */
    public Conversation create(
            TenantId tenantId,
            long ownerUserId,
            String sceneType,
            String title,
            String platform,
            String model,
            String systemPrompt) {
        return createWithId(
                UUID.randomUUID().toString(),
                tenantId,
                ownerUserId,
                sceneType,
                title,
                platform,
                model,
                systemPrompt);
    }

    @Transactional
    public Conversation createIdempotent(
            TenantId tenantId,
            long ownerUserId,
            String sceneType,
            String title,
            String platform,
            String model,
            String systemPrompt,
            String operation,
            String key,
            String fingerprint,
            String conversationId) {
        if (idempotencyRepository == null
                || !idempotencyRepository.claim(
                        tenantId,
                        ownerUserId,
                        operation,
                        key,
                        fingerprint,
                        conversationId)) {
            throw new IllegalStateException("idempotency key is already in use");
        }
        return createWithId(
                conversationId,
                tenantId,
                ownerUserId,
                sceneType,
                title,
                platform,
                model,
                systemPrompt);
    }

    private Conversation createWithId(
            String conversationId,
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
                        conversationId,
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
     * 执行 会话 相关业务数据，并返回处理结果。
     *
     * @param source 用于完成本次业务处理的 source 参数。
     * @param messageId 用于定位message的标识。
     * @return 返回 会话 相关操作生成的结果数据。
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
     * 执行 会话 相关业务数据，并返回处理结果。
     *
     * @param conversation 用于完成本次业务处理的 conversation 参数。
     * @param content 用于完成本次业务处理的 content 参数。
     * @return 返回 会话 相关操作生成的结果数据。
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

    @Transactional
    public GenerationRun createMessageGeneration(
            Conversation conversation,
            String content,
            String platform,
            String model,
            IdempotencyRepository idempotency,
            String operation,
            String key,
            String fingerprint,
            String runId,
            String inputMessageId,
            String speakerId) {
        TenantId tenantId = new TenantId(conversation.tenantId());
        if (key != null
                && !key.isBlank()
                && !idempotency.claim(
                        tenantId,
                        conversation.ownerUserId(),
                        operation,
                        key,
                        fingerprint,
                        runId)) {
            throw new IllegalStateException("idempotency key is already in use");
        }
        ConversationMessage message =
                appendMessageInternal(
                        conversation,
                        conversation.activeLeafMessageId(),
                        MessageRole.USER,
                        content,
                        null,
                        null,
                        null,
                        true,
                        inputMessageId);
        Conversation latest =
                repository
                        .findConversation(conversation.id(), tenantId, conversation.ownerUserId())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        GenerationRun run =
                newGeneration(latest, message, speakerId, platform, model, runId);
        generationRepository.insert(run);
        return run;
    }

    @Transactional
    public GenerationRun createGenerationIdempotent(
            Conversation conversation,
            ConversationMessage input,
            String platform,
            String model,
            String operation,
            String key,
            String fingerprint,
            String runId,
            boolean moveLeaf) {
        TenantId tenantId = new TenantId(conversation.tenantId());
        if (idempotencyRepository == null
                || !idempotencyRepository.claim(
                        tenantId,
                        conversation.ownerUserId(),
                        operation,
                        key,
                        fingerprint,
                        runId)) {
            throw new IllegalStateException("idempotency key is already in use");
        }
        Conversation latest =
                repository
                        .findConversation(conversation.id(), tenantId, conversation.ownerUserId())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        if (!moveLeaf && !input.id().equals(latest.activeLeafMessageId())) {
            throw new IllegalStateException("conversation was modified");
        }
        if (moveLeaf && !input.id().equals(latest.activeLeafMessageId())) {
            Conversation moved =
                    new Conversation(
                            latest.id(), latest.tenantId(), latest.ownerUserId(), latest.sceneType(),
                            latest.title(), latest.status(), latest.parentConversationId(),
                            latest.branchFromMessageId(), input.id(), latest.rollingSummary(),
                            latest.platform(), latest.model(), latest.version() + 1,
                            latest.createdAt(), Instant.now());
            if (repository.updateConversation(moved, latest.version()) != 1) {
                throw new IllegalStateException("conversation was modified");
            }
            latest = moved;
        }
        GenerationRun run = newGeneration(latest, input, null, platform, model, runId);
        generationRepository.insert(run);
        return run;
    }

    @Transactional
    public void rollbackRejectedGeneration(
            GenerationRun run,
            TenantId tenantId,
            long ownerUserId,
            boolean deleteInputMessage,
            boolean restoreLeaf,
            String previousLeaf,
            String operation,
            String key) {
        if (generationRepository.deleteCreated(run.id(), tenantId, ownerUserId) != 1) {
            return;
        }
        if (restoreLeaf || deleteInputMessage) {
            Conversation conversation =
                    repository.findConversation(run.conversationId(), tenantId, ownerUserId).orElse(null);
            if (conversation != null && restoreLeaf
                    && run.inputMessageId().equals(conversation.activeLeafMessageId())) {
                Conversation restored =
                        new Conversation(
                                conversation.id(), conversation.tenantId(), conversation.ownerUserId(),
                                conversation.sceneType(), conversation.title(), conversation.status(),
                                conversation.parentConversationId(), conversation.branchFromMessageId(),
                                previousLeaf, conversation.rollingSummary(), conversation.platform(),
                                conversation.model(), conversation.version() + 1,
                                conversation.createdAt(), Instant.now());
                int restoredRows = repository.updateConversation(restored, conversation.version());
                if (restoredRows != 1) {
                    throw new IllegalStateException(
                            "conversation was modified while rolling back rejected generation");
                }
            }
            if (conversation != null
                    && deleteInputMessage
                    && (!restoreLeaf
                            || run.inputMessageId().equals(conversation.activeLeafMessageId()))) {
                repository.deleteMessage(run.inputMessageId(), tenantId, ownerUserId);
            }
        }
        if (idempotencyRepository != null && key != null && !key.isBlank()) {
            idempotencyRepository.deleteClaim(
                    tenantId, ownerUserId, operation, key, run.id());
        }
    }

    /**
     * 执行 会话 相关业务数据，并返回处理结果。
     *
     * @param conversation 用于完成本次业务处理的 conversation 参数。
     * @param parentMessageId 用于定位parent 消息的标识。
     * @param role 用于完成本次业务处理的 role 参数。
     * @param content 用于完成本次业务处理的 content 参数。
     * @param parts 用于完成本次业务处理的 parts 参数。
     * @param toolCall 用于完成本次业务处理的 toolCall 参数。
     * @return 返回 会话 相关操作生成的结果数据。
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
     * 执行 会话 相关业务数据，并返回处理结果。
     *
     * @param conversation 用于完成本次业务处理的 conversation 参数。
     * @param parentMessageId 用于定位parent 消息的标识。
     * @param role 用于完成本次业务处理的 role 参数。
     * @param content 用于完成本次业务处理的 content 参数。
     * @param parts 用于完成本次业务处理的 parts 参数。
     * @param toolCall 用于完成本次业务处理的 toolCall 参数。
     * @param sourceMessageId 用于定位source 消息的标识。
     * @return 返回 会话 相关操作生成的结果数据。
     */
    public ConversationMessage appendMessage(
            Conversation conversation,
            String parentMessageId,
            MessageRole role,
            String content,
            List<ContentPart> parts,
            java.util.Map<String, Object> toolCall,
            String sourceMessageId) {
        return appendMessageInternal(conversation, parentMessageId, role, content,
                parts, toolCall, sourceMessageId, true, UUID.randomUUID().toString());
    }

    @Transactional
    public ConversationMessage appendMessageIdempotent(
            Conversation conversation,
            String parentMessageId,
            MessageRole role,
            String content,
            String sourceMessageId,
            IdempotencyRepository idempotency,
            String operation,
            String key,
            String fingerprint,
            String messageId) {
        TenantId tenantId = new TenantId(conversation.tenantId());
        if (!idempotency.claim(
                tenantId,
                conversation.ownerUserId(),
                operation,
                key,
                fingerprint,
                messageId)) {
            throw new IllegalStateException("idempotency key is already in use");
        }
        return appendMessageInternal(
                conversation,
                parentMessageId,
                role,
                content,
                null,
                null,
                sourceMessageId,
                true,
                messageId);
    }

    /** 在先前的分支上保存回复，同时保持当前选中的活跃轮次不变。 */
    public ConversationMessage appendMessagePreservingLeaf(
            Conversation conversation,
            String parentMessageId,
            MessageRole role,
            String content,
            List<ContentPart> parts,
            java.util.Map<String, Object> toolCall) {
        return appendMessageInternal(conversation, parentMessageId, role, content,
                parts, toolCall, null, false, UUID.randomUUID().toString());
    }

    private ConversationMessage appendMessageInternal(
            Conversation conversation,
            String parentMessageId,
            MessageRole role,
            String content,
            List<ContentPart> parts,
            java.util.Map<String, Object> toolCall,
            String sourceMessageId,
            boolean advanceLeaf,
            String messageId) {
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
                        messageId,
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
                                advanceLeaf ? message.id() : conversation.activeLeafMessageId(),
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
     * 创建或保存 会话 相关业务数据，并返回处理结果。
     *
     * @param conversation 用于完成本次业务处理的 conversation 参数。
     * @param userMessage 用于完成本次业务处理的 userMessage 参数。
     * @param platform 用于完成本次业务处理的 platform 参数。
     * @param model 用于完成本次业务处理的 model 参数。
     * @return 返回 会话 相关操作生成的结果数据。
     */
    public GenerationRun createGeneration(
            Conversation conversation,
            ConversationMessage userMessage,
            String platform,
            String model) {
        return createGeneration(conversation, userMessage, platform, model, null);
    }

    /**
     * 创建或保存 会话 相关业务数据，并返回处理结果。
     *
     * @param conversation 用于完成本次业务处理的 conversation 参数。
     * @param userMessage 用于完成本次业务处理的 userMessage 参数。
     * @param platform 用于完成本次业务处理的 platform 参数。
     * @param model 用于完成本次业务处理的 model 参数。
     * @param speakerId 用于定位speaker的标识。
     * @return 返回 会话 相关操作生成的结果数据。
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
        GenerationRun generation =
                newGeneration(conversation, userMessage, speakerId, platform, model, null);
        generationRepository.insert(generation);
        return generation;
    }

    private GenerationRun newGeneration(
            Conversation conversation,
            ConversationMessage userMessage,
            String speakerId,
            String platform,
            String model,
            String id) {
        Instant now = Instant.now();
        return new GenerationRun(
                        id == null ? UUID.randomUUID().toString() : id,
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
        Conversation latest =
                repository
                        .findConversation(
                                conversation.id(),
                                new TenantId(conversation.tenantId()),
                                conversation.ownerUserId())
                        .orElse(conversation);
        Conversation updated =
                new Conversation(
                        latest.id(), latest.tenantId(), latest.ownerUserId(), latest.sceneType(),
                        latest.title(), latest.status(), latest.parentConversationId(),
                        latest.branchFromMessageId(), assistant.id(), latest.rollingSummary(),
                        latest.platform(), latest.model(), latest.version() + 1,
                        latest.createdAt(), now);
        try {
            if (runtime != null && runtimeRun != null) {
                runtimeRun = runtime.linkGeneration(runtimeRun, created.id());
                runtime.append(runtimeRun, AiRunEventType.MODEL_DELTA, "{}", true);
                runtime.append(
                        runtimeRun,
                        AiRunEventType.MODEL_USAGE,
                        "{\"promptTokens\":" + promptTokens + ",\"completionTokens\":"
                                + completionTokens + ",\"estimated\":false}",
                        true);
                runtime.recordUsage(
                        runtimeRun.id(), runtimeRun.tenantId(), runtimeRun.ownerUserId().value(),
                        promptTokens, completionTokens, false, null);
            } else {
                generationRepository.appendEvent(
                        new GenerationEvent(created.id(), 0, GenerationEventType.STARTED, "{}", now),
                        new TenantId(conversation.tenantId()));
                if (!content.isEmpty())
                    generationRepository.appendEvent(
                            new GenerationEvent(created.id(), 1, GenerationEventType.DELTA, content, now),
                            new TenantId(conversation.tenantId()));
                generationRepository.appendEvent(
                        new GenerationEvent(
                                created.id(), 2, GenerationEventType.USAGE,
                                "{\"promptTokens\":" + promptTokens + ",\"completionTokens\":"
                                        + completionTokens + ",\"estimated\":false}", now),
                        new TenantId(conversation.tenantId()));
            }
            GenerationRun completed = running.transition(GenerationStatus.COMPLETED);
            completed =
                    new GenerationRun(
                            completed.id(), completed.conversationId(), completed.inputMessageId(),
                            assistant.id(), completed.speakerId(), completed.platform(), completed.model(),
                            completed.status(), promptTokens, completionTokens,
                            java.time.Duration.between(created.createdAt(), now).toMillis(), null,
                            3, false, completed.version(), completed.createdAt(), now,
                            runtimeRun == null ? null : runtimeRun.id());
            List<AiRunEventType> finalRuntimeEvents =
                    runtimeRun == null
                            ? List.of()
                            : List.of(
                                    AiRunEventType.MODEL_COMPLETED,
                                    AiRunEventType.STEP_COMPLETED,
                                    AiRunEventType.TURN_COMPLETED);
            if (repository.completeGeneration(
                            completed, running.version(), assistant, updated, latest.version(),
                            finalRuntimeEvents) != 1) {
                throw new IllegalStateException("stored generation completion conflict");
            }
            return completed;
        } catch (RuntimeException failure) {
            GenerationRun failed = running.transition(GenerationStatus.FAILED);
            String errorCode =
                    "stored generation completion conflict".equals(failure.getMessage())
                            ? "conversation_modified"
                            : failure.getClass().getSimpleName();
            GenerationRun terminal =
                    new GenerationRun(
                            failed.id(), failed.conversationId(), failed.inputMessageId(), null,
                            failed.speakerId(), failed.platform(), failed.model(), failed.status(),
                            promptTokens, completionTokens,
                            java.time.Duration.between(created.createdAt(), Instant.now()).toMillis(),
                            errorCode, 3, false, failed.version(), failed.createdAt(), Instant.now(),
                            runtimeRun == null ? null : runtimeRun.id());
            try {
                if (repository.transitionRunningGeneration(
                                terminal, running.version(), conversation.ownerUserId(), List.of())
                        == 1
                        && runtimeRun == null) {
                    generationRepository.appendEvent(
                            new GenerationEvent(
                                    created.id(), 3, GenerationEventType.FAILED, errorCode, Instant.now()),
                            new TenantId(conversation.tenantId()));
                }
            } catch (RuntimeException compensationFailure) {
                failure.addSuppressed(compensationFailure);
            }
            throw failure;
        }
    }
}
