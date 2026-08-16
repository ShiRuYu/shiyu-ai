package com.shiyu.ai.conversation;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.conversation.domain.*;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.conversation.port.GenerationRepository;
import com.shiyu.ai.conversation.port.GenerationUsageSink;
import com.shiyu.ai.conversation.port.GenerationAdmission;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatMessage;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.runtime.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Runs a structured request while persisting every externally visible event. */
@Service
public class GenerationRunner {
    private final ChatEngine chatEngine;
    private final GenerationRepository generations;
    private final ConversationRepository conversations;
    private final GenerationUsageSink usageSink;
    private final GenerationAdmission admission;
    private final AiRuntimeService runtime;
    private final ConversationPromptService promptService;

    public GenerationRunner(ChatEngine chatEngine, GenerationRepository generations, ConversationRepository conversations) {
        this(chatEngine, generations, conversations, new GenerationUsageSink() { }, new GenerationAdmission() { }, null, null);
    }

    public GenerationRunner(ChatEngine chatEngine, GenerationRepository generations, ConversationRepository conversations,
                            GenerationUsageSink usageSink, GenerationAdmission admission) {
        this(chatEngine, generations, conversations, usageSink, admission, null, null);
    }

    public GenerationRunner(ChatEngine chatEngine, GenerationRepository generations, ConversationRepository conversations,
                            GenerationUsageSink usageSink, GenerationAdmission admission, AiRuntimeService runtime) {
        this(chatEngine, generations, conversations, usageSink, admission, runtime, null);
    }

    @Autowired
    public GenerationRunner(ChatEngine chatEngine, GenerationRepository generations, ConversationRepository conversations,
                            GenerationUsageSink usageSink, GenerationAdmission admission, AiRuntimeService runtime,
                            ConversationPromptService promptService) {
        this.chatEngine = chatEngine;
        this.generations = generations;
        this.conversations = conversations;
        this.usageSink = usageSink == null ? new GenerationUsageSink() { } : usageSink;
        this.admission = admission == null ? new GenerationAdmission() { } : admission;
        this.runtime = runtime;
        this.promptService = promptService == null ? new ConversationPromptService(null) : promptService;
    }

    /** Spring-friendly constructor retaining the optional outbound boundaries. */
    public GenerationRunner(ChatEngine chatEngine, GenerationRepository generations, ConversationRepository conversations,
                            GenerationUsageSink usageSink) {
        this(chatEngine, generations, conversations, usageSink, new GenerationAdmission() { }, null);
    }

    public void start(GenerationRun created, long tenantId, long ownerUserId) {
        ConversationMessage input = conversations.findMessage(created.inputMessageId(), tenantId, ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("input message not found"));
        Conversation conversation = conversations.findConversation(input.conversationId(), tenantId, ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
        ConversationPromptService.PromptAssembly assembly = promptService.assemble(conversation,
                conversations.listMessages(conversation.id(), tenantId, ownerUserId, 1000).reversed(), tenantId, ownerUserId);
        List<ChatMessage> messages = assembly.modelMessages();
        int estimatedPromptTokens = messages.stream().mapToInt(message -> message.content().stream()
                .mapToLong(part -> PromptSafety.estimateTokens(part.text())).sum() > Integer.MAX_VALUE
                ? Integer.MAX_VALUE : (int) message.content().stream().mapToLong(part -> PromptSafety.estimateTokens(part.text())).sum()).sum();
        admission.reserve(tenantId, created, estimatedPromptTokens);
        GenerationRun running = created.transition(GenerationStatus.RUNNING);
        if (generations.update(running, created.version()) != 1) {
            admission.release(tenantId, created);
            throw new IllegalStateException("generation is already running");
        }
        AtomicInteger nextSequence = new AtomicInteger(0);
        append(running, tenantId, nextSequence.getAndIncrement(), GenerationEventType.STARTED, "{}");
        AtomicReference<GenerationRun> state = new AtomicReference<>(running);
        AtomicReference<AiRun> runtimeState = new AtomicReference<>();
        if (runtime != null) {
            try {
                runtimeState.set(runtime.startRun(new AiRunContext(tenantId, ownerUserId, null, null, conversation.id(), created.id(), null, null, java.util.Map.of()), AiRunSource.GENERATION, created.id(), created.model(), JSONUtils.toJsonString(messages)));
                runtime.append(runtimeState.get(), AiRunEventType.PROMPT_ASSEMBLED, JSONUtils.toJsonString(java.util.Map.of("messageCount", messages.size(), "estimatedTokens", estimatedPromptTokens)), true);
                runtime.append(runtimeState.get(), AiRunEventType.MODEL_STARTED, "{}", true);
            }
            catch (RuntimeException ignored) { /* runtime tracing is non-blocking */ }
        }
        StringBuilder answer = new StringBuilder();
        try {
            Flux<ChatResponse> stream = chatEngine.stream(ChatRequest.builder().platform(created.platform()).model(created.model())
                    .generationRunId(created.id()).messages(messages).build());
            if (stream == null) throw new IllegalStateException("chat engine returned no stream");
            stream.subscribe(value -> onEvent(value, state, runtimeState, answer, tenantId, ownerUserId, nextSequence),
                    error -> finishFailure(state, runtimeState, tenantId, ownerUserId, nextSequence, error),
                    () -> finishSuccess(state, runtimeState, tenantId, ownerUserId, nextSequence, answer.toString()));
        } catch (Throwable error) {
            // Provider implementations may fail before returning a Publisher (configuration,
            // serialization, or synchronous admission errors). Persist the same terminal
            // event as an asynchronous provider failure so a run never remains RUNNING.
            finishFailure(state, runtimeState, tenantId, ownerUserId, nextSequence, error);
        }
    }

    private void onEvent(ChatResponse value, AtomicReference<GenerationRun> state, AtomicReference<AiRun> runtimeState, StringBuilder answer,
                         long tenantId, long ownerUserId, AtomicInteger nextSequence) {
        GenerationRun persisted = generations.find(state.get().id(), tenantId, ownerUserId).orElse(state.get());
        if (persisted.status() == GenerationStatus.CANCELLED || persisted.cancelRequested()) return;
        String type = value.getEventType() == null ? "DELTA" : value.getEventType();
        if ("COMPLETED".equals(type)) return;
        if ("FAILED".equals(type) || "CANCELLED".equals(type)) {
            finishProviderTerminal(state, runtimeState, tenantId, ownerUserId, nextSequence,
                    "CANCELLED".equals(type) ? GenerationStatus.CANCELLED : GenerationStatus.FAILED,
                    value.getErrorMessage());
            return;
        }
        GenerationEventType eventType;
        try { eventType = GenerationEventType.valueOf(type); } catch (IllegalArgumentException ex) { eventType = GenerationEventType.DELTA; }
        if (eventType == GenerationEventType.DELTA && value.getContent() != null) answer.append(value.getContent());
        String payload = switch (eventType) {
            case DELTA -> Objects.toString(value.getContent(), "");
            case TOOL_CALL -> JSONUtils.toJsonString(java.util.Map.of("id", value.getToolCallId(), "name", value.getToolName(), "arguments", Objects.toString(value.getToolArguments(), "{}")));
            case USAGE -> JSONUtils.toJsonString(java.util.Map.of("promptTokens", value.getPromptTokens(), "completionTokens", value.getCompletionTokens(), "totalTokens", value.getTotalTokens(), "estimated", value.isEstimatedUsage()));
            default -> "{}";
        };
        int sequence = nextSequence.getAndIncrement();
        append(state.get(), tenantId, sequence, eventType, payload);
        if (runtime != null && runtimeState.get() != null) {
            AiRunEventType runtimeType = eventType == GenerationEventType.TOOL_CALL ? AiRunEventType.TOOL_REQUESTED : eventType == GenerationEventType.USAGE ? AiRunEventType.MODEL_USAGE : AiRunEventType.MODEL_DELTA;
            try { runtime.append(runtimeState.get(), runtimeType, payload, true); } catch (RuntimeException ignored) { }
        }
        if (eventType == GenerationEventType.USAGE) {
            GenerationRun before = state.get();
            GenerationRun usage = withUsageAndSequence(before, value, sequence, before.version() + 1);
            if (generations.update(usage, before.version()) == 1) state.set(usage);
            if (runtime != null && runtimeState.get() != null) {
                try { runtime.recordUsage(runtimeState.get().id(), tenantId, ownerUserId,
                        value.getPromptTokens() == null ? usage.promptTokens() : value.getPromptTokens(),
                        value.getCompletionTokens() == null ? usage.completionTokens() : value.getCompletionTokens(),
                        value.isEstimatedUsage(), null); } catch (RuntimeException ignored) { }
            }
        } else {
            state.updateAndGet(s -> withUsageAndSequence(s, value, sequence, s.version()));
        }
    }

    private void finishProviderTerminal(AtomicReference<GenerationRun> state, AtomicReference<AiRun> runtimeState,
                                        long tenantId, long ownerUserId, AtomicInteger nextSequence,
                                        GenerationStatus terminal, String errorCode) {
        GenerationRun current = generations.find(state.get().id(), tenantId, ownerUserId).orElse(state.get());
        if (current.status() != GenerationStatus.RUNNING) return;
        GenerationRun next = current.transition(terminal);
        next = new GenerationRun(next.id(), next.conversationId(), next.inputMessageId(), next.assistantMessageId(), next.speakerId(),
                next.platform(), next.model(), next.status(), next.promptTokens(), next.completionTokens(),
                java.time.Duration.between(current.createdAt(), Instant.now()).toMillis(),
                terminal == GenerationStatus.FAILED ? Objects.toString(errorCode, "provider_failed") : null,
                next.lastEventSequence(), terminal == GenerationStatus.CANCELLED, next.version(), next.createdAt(), Instant.now());
        if (generations.update(next, current.version()) != 1) return;
        append(next, tenantId, nextSequence.getAndIncrement(),
                terminal == GenerationStatus.CANCELLED ? GenerationEventType.CANCELLED : GenerationEventType.FAILED,
                terminal == GenerationStatus.CANCELLED ? "{}" : Objects.toString(errorCode, "provider_failed"));
        admission.release(tenantId, next);
        if (terminal == GenerationStatus.FAILED) usageSink.failed(next);
        state.set(next);
        if (runtime != null && runtimeState.get() != null) {
            try {
                runtime.finish(runtimeState.get().id(), tenantId, ownerUserId,
                        terminal == GenerationStatus.CANCELLED ? AiRunStatus.CANCELLED : AiRunStatus.FAILED,
                        terminal == GenerationStatus.CANCELLED ? null : Objects.toString(errorCode, "provider_failed"));
            } catch (RuntimeException ignored) { }
        }
    }

    private void finishSuccess(AtomicReference<GenerationRun> state, AtomicReference<AiRun> runtimeState, long tenantId, long ownerUserId,
                               AtomicInteger nextSequence, String answer) {
        GenerationRun current = generations.find(state.get().id(), tenantId, ownerUserId).orElse(state.get());
        if (current.status() != GenerationStatus.RUNNING || current.cancelRequested()) return;
        ConversationMessage input = conversations.findMessage(current.inputMessageId(), tenantId, ownerUserId).orElseThrow();
        Conversation conversation = conversations.findConversation(input.conversationId(), tenantId, ownerUserId).orElseThrow();
        ConversationMessage assistant = new ConversationMessage(java.util.UUID.randomUUID().toString(), conversation.id(), input.id(), null,
                MessageRole.ASSISTANT, List.of(ContentPart.text(answer)), java.util.Map.of(), MessageStatus.COMPLETED,
                input.sequence() + 1, current.id(), Instant.now(), Instant.now());
        conversations.insertMessage(assistant);
        Conversation updated = new Conversation(conversation.id(), conversation.tenantId(), conversation.ownerUserId(), conversation.sceneType(), conversation.title(), conversation.status(),
                conversation.parentConversationId(), conversation.branchFromMessageId(), assistant.id(), conversation.rollingSummary(), conversation.platform(), conversation.model(), conversation.version() + 1, conversation.createdAt(), Instant.now());
        if (conversations.updateConversation(updated, conversation.version()) != 1) {
            conversations.deleteMessage(assistant.id(), tenantId, ownerUserId);
            finishFailure(state, runtimeState, tenantId, ownerUserId, nextSequence, new IllegalStateException("conversation was modified"));
            return;
        }
        int sequence = nextSequence.getAndIncrement();
        GenerationRun completed = current.transition(GenerationStatus.COMPLETED);
        completed = new GenerationRun(completed.id(), completed.conversationId(), completed.inputMessageId(), assistant.id(), completed.speakerId(), completed.platform(), completed.model(), completed.status(),
                completed.promptTokens(), Math.max(completed.completionTokens(), Math.max(1, answer.length() / 4)),
                java.time.Duration.between(current.createdAt(), Instant.now()).toMillis(), null, sequence, false, completed.version(), completed.createdAt(), Instant.now());
        if (generations.update(completed, current.version()) == 1) {
            append(completed, tenantId, sequence, GenerationEventType.COMPLETED, "{}");
            admission.settle(tenantId, completed);
                usageSink.completed(completed);
            if (runtime != null && runtimeState.get() != null) try { runtime.finish(runtimeState.get().id(), tenantId, ownerUserId, AiRunStatus.COMPLETED, null); } catch (RuntimeException ignored) { }
        } else {
            // Cancellation or another terminal transition won the run CAS after the
            // conversation update. Restore the leaf only if nobody changed it again,
            // then remove the uncommitted assistant candidate.
            Conversation restored = new Conversation(conversation.id(), conversation.tenantId(), conversation.ownerUserId(), conversation.sceneType(), conversation.title(), conversation.status(),
                    conversation.parentConversationId(), conversation.branchFromMessageId(), conversation.activeLeafMessageId(), conversation.rollingSummary(), conversation.platform(), conversation.model(), updated.version() + 1, conversation.createdAt(), Instant.now());
            conversations.updateConversation(restored, updated.version());
            conversations.deleteMessage(assistant.id(), tenantId, ownerUserId);
        }
    }

    private void finishFailure(AtomicReference<GenerationRun> state, AtomicReference<AiRun> runtimeState, long tenantId, long ownerUserId, AtomicInteger nextSequence, Throwable error) {
        GenerationRun current = state.get();
        GenerationRun persisted = generations.find(current.id(), tenantId, ownerUserId).orElse(current);
        if (persisted.status() != GenerationStatus.RUNNING) return;
        GenerationRun failed = persisted.transition(GenerationStatus.FAILED);
        failed = new GenerationRun(failed.id(), failed.conversationId(), failed.inputMessageId(), failed.assistantMessageId(), failed.speakerId(), failed.platform(), failed.model(), failed.status(),
                failed.promptTokens(), failed.completionTokens(), 0, error.getClass().getSimpleName(), failed.lastEventSequence(), false, failed.version(), failed.createdAt(), Instant.now());
        if (generations.update(failed, persisted.version()) != 1) {
            // A cancellation or another terminal callback won the CAS. Do not
            // append a contradictory FAILED event or settle admission twice.
            return;
        }
        append(failed, tenantId, nextSequence.getAndIncrement(), GenerationEventType.FAILED, error.getClass().getSimpleName());
        admission.release(tenantId, failed);
        usageSink.failed(failed);
        state.set(failed);
        if (runtime != null && runtimeState.get() != null) try { runtime.finish(runtimeState.get().id(), tenantId, ownerUserId, AiRunStatus.FAILED, error.getClass().getSimpleName()); } catch (RuntimeException ignored) { }
    }

    private void append(GenerationRun run, long tenantId, int sequence, GenerationEventType type, String payload) {
        generations.appendEvent(new GenerationEvent(run.id(), sequence, type, payload, Instant.now()), tenantId);
    }

    private GenerationRun withUsageAndSequence(GenerationRun s, ChatResponse value, int sequence, long version) {
        long prompt = value.getPromptTokens() == null ? s.promptTokens() : value.getPromptTokens();
        long completion = value.getCompletionTokens() == null ? s.completionTokens() : value.getCompletionTokens();
        return new GenerationRun(s.id(), s.conversationId(), s.inputMessageId(), s.assistantMessageId(), s.speakerId(), s.platform(), s.model(), s.status(), prompt, completion,
                s.latencyMs(), s.errorCode(), sequence, s.cancelRequested(), version, s.createdAt(), Instant.now());
    }
}
