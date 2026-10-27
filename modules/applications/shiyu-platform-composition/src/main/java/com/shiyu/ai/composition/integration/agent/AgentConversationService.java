package com.shiyu.ai.composition.integration.agent;

import com.shiyu.ai.agent.implementation.runtime.port.AgentConversationCompletionPort;
import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.conversation.implementation.application.ConversationService;
import com.shiyu.ai.conversation.implementation.domain.model.Conversation;
import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;
import com.shiyu.ai.conversation.implementation.domain.model.MessageRole;
import com.shiyu.ai.conversation.implementation.domain.port.ConversationRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantScope;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;

/** 将已发布 Agent 应用的执行关联到已登录用户的会话。 */
@Service
@RequiredArgsConstructor
public class AgentConversationService implements AgentConversationCompletionPort {
    private final ConversationRepository repository;
    private final ConversationService conversations;
    private final AiRuntimeService runtime;
    private final AgentRuntime agents;

    /** 已获批的执行从检查点继续后，将结果追加到会话中。 */
    @Override
    public void completeApprovedRun(ActorContext actor, AiRun run, Execution execution) {
        if (actor == null || run == null || execution == null
                || run.conversationId() == null || !execution.getStatus().isTerminal()
                || !actor.tenantId().equals(run.tenantId())
                || actor.userId().value() != run.ownerUserId().value()
                || !execution.getExecutionId().equals(run.executionId())) {
            throw new IllegalArgumentException("resumed conversation outcome is not owned");
        }
        TenantScope.withTenant(actor.tenantId(), () -> {
            for (int attempt = 0; attempt < 8; attempt++) {
                List<ConversationMessage> messages = repository.listMessages(run.conversationId(),
                        actor.tenantId(), actor.userId().value(), 1000);
                if (messages.stream().anyMatch(message -> matchesExecution(message, run.id(),
                        execution.getExecutionId()) && isFinalTrace(message.toolCall()))) {
                    return null;
                }
                ConversationMessage pending = messages.stream()
                        .filter(message -> matchesExecution(message, run.id(),
                                execution.getExecutionId())
                                && "PAUSED".equals(message.toolCall().get("status")))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "pending Agent conversation message not found"));
                Conversation conversation = repository.findConversation(run.conversationId(),
                        actor.tenantId(), actor.userId().value())
                        .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
                String status = execution.getStatus().name();
                String content = execution.getStatus() == ExecutionStatus.COMPLETED
                        ? outputContent(execution.getOutput())
                        : execution.getStatus() == ExecutionStatus.CANCELLED
                                ? "已停止" : "Agent 执行失败";
                Map<String, Object> trace = new LinkedHashMap<>();
                trace.put("appId", run.appId());
                trace.put("status", status);
                trace.put("executionId", execution.getExecutionId());
                trace.put("runtimeRunId", run.id());
                try {
                    if (pending.id().equals(conversation.activeLeafMessageId())) {
                        conversations.appendMessage(conversation, pending.id(),
                                MessageRole.ASSISTANT, content, null, trace);
                    } else {
                        conversations.appendMessagePreservingLeaf(conversation, pending.id(),
                                MessageRole.ASSISTANT, content, null, trace);
                    }
                    return null;
                } catch (IllegalStateException conflict) {
                    if (!"conversation was modified; retry against the latest active leaf"
                            .equals(conflict.getMessage()) || attempt == 7) throw conflict;
                }
            }
            throw new IllegalStateException("conversation changed too often");
        });
    }

    private static boolean matchesExecution(ConversationMessage message,
            String runId, String executionId) {
        return message.role() == MessageRole.ASSISTANT
                && message.toolCall() != null
                && runId.equals(message.toolCall().get("runtimeRunId"))
                && executionId.equals(message.toolCall().get("executionId"));
    }

    private static boolean isFinalTrace(Map<String, Object> trace) {
        return trace != null && ("COMPLETED".equals(trace.get("status"))
                || "FAILED".equals(trace.get("status"))
                || "CANCELLED".equals(trace.get("status")));
    }

    private static String outputContent(Object output) {
        return output instanceof String value ? value
                : output instanceof Map<?, ?> state && state.get("content") instanceof String text
                        ? text : JSONUtils.toJsonString(output);
    }

    /** 持久化输入，并以流式方式返回状态、输出和已持久化的消息标识。 */
    public Flux<Map<String, Object>> execute(ActorContext actor, String conversationId,
            String appId, String appVersionId, String prompt, String sourceMessageId) {
        if (actor == null || actor.tenantId() == null || actor.userId() == null
                || conversationId == null || conversationId.isBlank()
                || appId == null || appId.isBlank() || prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Agent conversation request is incomplete");
        }
        var version = runtime.requirePublishedVersion(appId, actor.tenantId(),
                actor.userId().value());
        if (appVersionId != null && !appVersionId.isBlank()
                && !version.id().equals(appVersionId)) {
            throw new IllegalStateException("only the currently published app version can execute");
        }
        Map<String, Object> config = version.configJson() == null
                || version.configJson().isBlank()
                ? Map.of() : JSONUtils.parseMap(version.configJson());
        String agentId = config.get("agentId") == null
                ? null : String.valueOf(config.get("agentId"));
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalStateException("published app does not declare agentId");
        }
        ConversationMessage input = TenantScope.withTenant(actor.tenantId(), () -> {
            Conversation conversation = repository.findConversation(conversationId,
                    actor.tenantId(), actor.userId().value())
                    .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
            if (!"agent".equals(conversation.sceneType())) {
                throw new IllegalArgumentException("conversation is not an Agent conversation");
            }
            if (sourceMessageId != null && !sourceMessageId.isBlank()) {
                ConversationMessage source = repository.findMessage(sourceMessageId,
                        actor.tenantId(), actor.userId().value())
                        .orElseThrow(() -> new IllegalArgumentException("source message not found"));
                if (!conversationId.equals(source.conversationId())
                        || source.role() != MessageRole.USER) {
                    throw new IllegalArgumentException("source message is not an owned user turn");
                }
                return conversations.appendMessage(conversation,
                        conversation.activeLeafMessageId(), MessageRole.USER, prompt, null,
                        null, sourceMessageId);
            }
            return conversations.appendUserMessage(conversation, prompt);
        });
        Map<String, Object> graphInput = new LinkedHashMap<>();
        graphInput.put("prompt", prompt);
        graphInput.put("query", prompt);
        graphInput.put("chatType", "STREAM");
        if (config.get("platform") instanceof String platform && !platform.isBlank()) {
            graphInput.put("platform", platform);
        }
        if (config.get("model") instanceof String model && !model.isBlank()) {
            graphInput.put("model", model);
        }
        graphInput.put("__appId", appId);
        graphInput.put("__appVersionId", version.id());
        graphInput.put("__conversationId", conversationId);
        AtomicReference<String> executionId = new AtomicReference<>();
        AtomicReference<String> runId = new AtomicReference<>();
        AtomicBoolean terminalSaved = new AtomicBoolean();
        AtomicBoolean cancellationRequested = new AtomicBoolean();
        String agentVersion = config.get("agentVersion") == null
                ? null : String.valueOf(config.get("agentVersion"));
        Flux<Map<String, Object>> stream;
        try {
            stream = agents.executeStream(actor, agentId, agentVersion, graphInput);
        } catch (RuntimeException setupFailure) {
            saveTerminal(actor, conversationId, input.id(), appId, null, null,
                    "FAILED", "Agent 执行失败", terminalSaved);
            throw setupFailure;
        }
        return Flux.<Map<String, Object>>create(sink -> {
            Disposable.Swap upstream = Disposables.swap();
            sink.onCancel(() -> {
                upstream.dispose();
                saveCancellationOutcome(actor, conversationId, input.id(), appId,
                        executionId.get(), runId.get(), terminalSaved);
            });
            upstream.update(stream
                    .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                    .onErrorResume(error -> Flux.just(Map.of("status", "FAILED")))
                    .concatWith(Flux.defer(() -> terminalSaved.get()
                            ? Flux.empty() : Flux.just(Map.of("status", "FAILED"))))
                    .map(event -> {
                        Map<String, Object> projected = new LinkedHashMap<>(event);
                        if (event.get("state") instanceof Map<?, ?> state) {
                            projected.put("state", publicValue(state));
                        }
                        if (event.get("output") instanceof Map<?, ?> output) {
                            projected.put("output", publicValue(output));
                        }
                        if (event.get("executionId") instanceof String id) executionId.set(id);
                        if (event.get("runtimeRunId") instanceof String id) runId.set(id);
                        if ("STARTED".equals(event.get("status"))) {
                            projected.put("inputMessageId", input.id());
                        } else if ("COMPLETED".equals(event.get("status"))) {
                            Object output = projected.get("output");
                            String content = output instanceof String value ? value
                                    : output instanceof Map<?, ?> state
                                            && state.get("content") instanceof String text
                                                    ? text : JSONUtils.toJsonString(output);
                            projected.put("assistantMessageId", saveTerminal(actor,
                                    conversationId, input.id(), appId, executionId.get(),
                                    runId.get(), "COMPLETED", content, terminalSaved));
                        } else if ("CANCELLED".equals(event.get("status"))) {
                            projected.put("assistantMessageId", saveTerminal(actor,
                                    conversationId, input.id(), appId, executionId.get(),
                                    runId.get(), "CANCELLED", "已停止", terminalSaved));
                        } else if ("PAUSED".equals(event.get("status"))) {
                            projected.put("assistantMessageId", saveTerminal(actor,
                                    conversationId, input.id(), appId, executionId.get(),
                                    runId.get(), "PAUSED", "工具调用等待审批，请前往工具审批页面处理。",
                                    terminalSaved));
                        } else if ("FAILED".equals(event.get("status"))) {
                            projected.put("assistantMessageId", saveTerminal(actor,
                                    conversationId, input.id(), appId, executionId.get(),
                                    runId.get(), "FAILED", "Agent 执行失败", terminalSaved));
                        }
                        return projected;
                    })
                    .subscribe(sink::next, sink::error, sink::complete));
        });
    }

    private static Object publicValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> publicMap = new LinkedHashMap<>();
            map.forEach((key, item) -> {
                if (key instanceof String name && !name.startsWith("__")) {
                    publicMap.put(name, publicValue(item));
                }
            });
            return publicMap;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(AgentConversationService::publicValue).toList();
        }
        return value;
    }

    private String saveCancellationOutcome(ActorContext actor, String conversationId,
            String inputId, String appId, String executionId, String runtimeRunId,
            AtomicBoolean saved) {
        if (executionId != null) {
            try {
                Execution execution = agents.getExecution(actor, executionId);
                if (execution != null) {
                    String status = execution.getStatus().name();
                    String content = switch (execution.getStatus()) {
                        case COMPLETED -> outputContent(execution.getOutput());
                        case FAILED -> "Agent 执行失败";
                        case CANCELLED -> "已停止";
                        case PAUSED -> "工具调用等待审批，请前往工具审批页面处理。";
                        default -> null;
                    };
                    if (content != null) {
                        return saveTerminal(actor, conversationId, inputId, appId,
                                executionId, runtimeRunId, status, content, saved);
                    }
                }
            } catch (RuntimeException ignored) {
                // 无法读取执行状态时，回退到客户端断开连接的处理结果。
            }
        }
        return saveTerminal(actor, conversationId, inputId, appId, executionId,
                runtimeRunId, "CANCELLED", "已停止", saved);
    }

    private String saveTerminal(ActorContext actor, String conversationId, String inputId,
            String appId, String executionId, String runtimeRunId, String status,
            String content, AtomicBoolean saved) {
        if (!saved.compareAndSet(false, true)) return "";
        try {
            return TenantScope.withTenant(actor.tenantId(), () -> {
                Map<String, Object> trace = new LinkedHashMap<>();
                trace.put("appId", appId);
                trace.put("status", status);
                if (executionId != null) trace.put("executionId", executionId);
                if (runtimeRunId != null) trace.put("runtimeRunId", runtimeRunId);
                for (int attempt = 0; attempt < 8; attempt++) {
                    Conversation conversation = repository.findConversation(conversationId,
                            actor.tenantId(), actor.userId().value())
                            .orElseThrow(() -> new IllegalArgumentException("conversation not found"));
                    try {
                        ConversationMessage reply = inputId.equals(conversation.activeLeafMessageId())
                                ? conversations.appendMessage(conversation, inputId,
                                        MessageRole.ASSISTANT, content, null, trace)
                                : conversations.appendMessagePreservingLeaf(conversation, inputId,
                                        MessageRole.ASSISTANT, content, null, trace);
                        return reply.id();
                    } catch (IllegalStateException conflict) {
                        if (!"conversation was modified; retry against the latest active leaf"
                                .equals(conflict.getMessage()) || attempt == 7) throw conflict;
                    }
                }
                throw new IllegalStateException("conversation changed too often");
            });
        } catch (RuntimeException failure) {
            saved.set(false);
            throw failure;
        }
    }
}
