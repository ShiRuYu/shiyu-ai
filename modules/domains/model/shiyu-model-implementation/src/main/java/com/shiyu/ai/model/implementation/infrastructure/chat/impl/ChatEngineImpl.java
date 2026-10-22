package com.shiyu.ai.model.implementation.infrastructure.chat.impl;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatMessage;
import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;
import com.shiyu.ai.model.implementation.domain.event.ModelCallEvent;
import com.shiyu.ai.model.implementation.infrastructure.chat.cancellation.ProviderRequestCancellation;
import com.shiyu.ai.model.implementation.infrastructure.service.ModelManager;
import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ModelProviderCapabilities;
import com.shiyu.ai.model.implementation.infrastructure.gateway.model.ModelRoutePolicy;
import com.shiyu.ai.model.implementation.infrastructure.gateway.service.ModelRouter;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest.Builder;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 提供 对话 Engine Impl 所属基础设施的适配、存储或运行支持。
 */
@Slf4j
@Service
public class ChatEngineImpl implements ChatEngine {
    /**
     * modelManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ModelManager modelManager;
    /**
     * eventPublisher 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ApplicationEventPublisher eventPublisher;
    /**
     * modelRouter 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ModelRouter modelRouter;

    /**
     * 执行 对话 Engine Impl 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param modelManager 用于完成本次业务处理的 modelManager 参数。
     * @param eventPublisher 用于完成本次业务处理的 eventPublisher 参数。
     * @param modelRouter 用于完成本次业务处理的 modelRouter 参数。
     */
    @Autowired
    public ChatEngineImpl(
            ModelManager modelManager,
            ApplicationEventPublisher eventPublisher,
            ModelRouter modelRouter) {
        this.modelManager = modelManager;
        this.eventPublisher = eventPublisher;
        this.modelRouter = modelRouter;
    }

    /**
     * 处理聊天engineimpl。
     *
     * @param modelManager modelManager 参数。
     * @param eventPublisher eventPublisher 参数。
     *
     * @return 处理结果。
     */
    public ChatEngineImpl(ModelManager modelManager, ApplicationEventPublisher eventPublisher) {
        this(modelManager, eventPublisher, new ModelRouter());
    }

    /**
     * 执行 对话 Engine Impl 相关业务数据，并返回处理结果。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 对话 Engine Impl 相关操作生成的结果数据。
     */
    @Override
    public ChatResponse chat(com.shiyu.ai.model.contract.model.ChatRequest request) {
        String error = validate(request);
        if (error != null) return error(error, request);
        return TenantScope.withTenant(new TenantId(request.getTenantId()),
                () -> chatForTenant(request));
    }

    /**
     * 将已校验的请求租户绑定到当前线程后调用模型。
     *
     * @param request 已校验的聊天请求
     * @return 模型响应或经过脱敏处理的失败结果
     */
    private ChatResponse chatForTenant(ChatRequest request) {
        long started = System.currentTimeMillis();
        if (request.getModelRouteId() != null
                && !request.getModelRouteId().isBlank()
                && routeTenant(request) != null) {
            try {
                return routedChat(request, started);
            } catch (Exception ex) {
                log.error(
                        "model route failed route={} errorCode={}",
                        request.getModelRouteId(),
                        ex.getClass().getSimpleName());
                return error("model call failed", request);
            }
        }
        try {
            ChatRequest resolved = resolveRequest(request, false);
            if (isDeepSeekPlatform(resolved)) {
                requireDeepSeekProvider();
                ChatResponse response = modelManager.getDeepSeekProvider().chat(resolved);
                publishUsageValues(resolved, response, started);
                return response;
            }
            ChatModel model =
                    modelManager.getChatModel(resolved.getPlatform(), resolved.getModel());
            if (model == null) throw new IllegalStateException("model is unavailable");
            dev.langchain4j.model.chat.response.ChatResponse response =
                    model.chat(nativeRequest(resolved));
            TokenUsage usage = response.tokenUsage();
            String actual =
                    response.modelName() == null ? resolved.getModel() : response.modelName();
            publishUsage(resolved, actual, usage, started);
            String content =
                    response.aiMessage() == null
                            ? ""
                            : Objects.toString(response.aiMessage().text(), "");
            List<ChatResponse.ToolCall> toolCalls =
                    response.aiMessage() == null
                            ? List.of()
                            : response.aiMessage().toolExecutionRequests().stream()
                                    .map(
                                            call ->
                                                    new ChatResponse.ToolCall(
                                                            call.id(),
                                                            call.name(),
                                                            call.arguments()))
                                    .toList();
            int promptTokens = usage == null ? estimateInput(resolved) : input(usage);
            int completionTokens = usage == null ? estimate(content) : output(usage);
            return ChatResponse.builder()
                    .success(true)
                    .eventType("COMPLETED")
                    .content(content)
                    .platform(resolved.getPlatform())
                    .model(actual)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(usage == null ? promptTokens + completionTokens : total(usage))
                    .estimatedUsage(usage == null)
                    .toolCalls(toolCalls)
                    .build();
        } catch (Exception ex) {
            log.error(
                    "model chat failed platform={} model={} errorCode={}",
                    request.getPlatform(),
                    request.getModel(),
                    ex.getClass().getSimpleName());
            return error("model call failed", request);
        }
    }

    private ChatResponse routedChat(ChatRequest request, long started) {
        java.util.Set<String> required = new java.util.HashSet<>(java.util.Set.of("chat"));
        if (request.getTools() != null && !request.getTools().isEmpty()) required.add("tool_calls");
        TenantId tenantId = routeTenant(request);
        ModelRoutePolicy policy = modelRouter.requirePolicy(request.getModelRouteId(), tenantId);
        ChatResponse result =
                modelRouter.executeWithFallback(
                        request.getModelRouteId(),
                        tenantId,
                        required,
                        candidate ->
                                TenantScope.withTenant(
                                    tenantId,
                                    () -> {
                                        ChatRequest resolved =
                                                routeRequest(request, candidate, policy, false);
                                        if (isDeepSeekPlatform(resolved)) {
                                            requireDeepSeekProvider();
                                            ChatResponse response =
                                                    modelManager
                                                            .getDeepSeekProvider()
                                                            .chat(resolved);
                                            publishUsageValues(resolved, response, started);
                                            return response;
                                        }
                                        ChatModel model =
                                                modelManager.getChatModel(
                                                        resolved.getPlatform(),
                                                        resolved.getModel());
                                        if (model == null)
                                            throw new IllegalStateException(
                                                    "model is unavailable");
                                        var response = model.chat(nativeRequest(resolved));
                                        TokenUsage usage = response.tokenUsage();
                                        String actual =
                                                response.modelName() == null
                                                        ? resolved.getModel()
                                                        : response.modelName();
                                        publishUsage(resolved, actual, usage, started);
                                        String content =
                                                response.aiMessage() == null
                                                        ? ""
                                                        : Objects.toString(
                                                                response.aiMessage().text(), "");
                                        int promptTokens =
                                                usage == null
                                                        ? estimateInput(resolved)
                                                        : input(usage);
                                        int completionTokens =
                                                usage == null ? estimate(content) : output(usage);
                                        List<ChatResponse.ToolCall> toolCalls =
                                                response.aiMessage() == null
                                                        ? List.of()
                                                        : response.aiMessage()
                                                                .toolExecutionRequests()
                                                                .stream()
                                                                .map(
                                                                        call ->
                                                                                new ChatResponse
                                                                                        .ToolCall(
                                                                                        call.id(),
                                                                                        call.name(),
                                                                                        call.arguments()))
                                                                .toList();
                                        return ChatResponse.builder()
                                                .success(true)
                                                .eventType("COMPLETED")
                                                .content(content)
                                                .platform(resolved.getPlatform())
                                                .model(actual)
                                                .promptTokens(promptTokens)
                                                .completionTokens(completionTokens)
                                                .totalTokens(
                                                        usage == null
                                                                ? promptTokens + completionTokens
                                                                : total(usage))
                                                .estimatedUsage(usage == null)
                                                .toolCalls(toolCalls)
                                                .build();
                                    }));
        return result;
    }

    /**
     * 执行 对话 Engine Impl 相关业务数据，并返回处理结果。
     *
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 对话 Engine Impl 相关操作生成的结果数据。
     */
    @Override
    public Flux<ChatResponse> stream(ChatRequest request) {
        String error = validate(request);
        if (error != null) return Flux.error(new IllegalArgumentException(error));
        if (request.getModelRouteId() != null
                && !request.getModelRouteId().isBlank()
                && routeTenant(request) != null) {
            return routedStream(request);
        }
        return Flux.defer(
                () -> TenantScope.withTenant(new TenantId(request.getTenantId()), () -> {
                    Sinks.Many<ChatResponse> sink = Sinks.many().unicast().onBackpressureBuffer();
                    long started = System.currentTimeMillis();
                    AtomicBoolean emittedDelta = new AtomicBoolean();
                    ProviderRequestCancellation cancellation =
                            new ProviderRequestCancellation();
                    try {
                        ChatRequest resolved = resolveRequest(request, true);
                        if (isDeepSeekPlatform(resolved)) {
                            requireDeepSeekProvider();
                            return modelManager.getDeepSeekProvider().stream(resolved)
                                    .doOnNext(
                                            event -> {
                                                if ("USAGE".equals(event.getEventType()))
                                                    publishUsageValues(resolved, event, started);
                                            });
                        }
                        StreamingChatModel model =
                                modelManager.getStreamingChatModel(
                                        resolved.getPlatform(), resolved.getModel());
                        if (model == null)
                            return Flux.error(
                                    new IllegalStateException("streaming model is unavailable"));
                        String actual = resolved.getModel();
                        sink.tryEmitNext(
                                ChatResponse.builder()
                                        .success(true)
                                        .eventType("BLOCK_STARTED")
                                        .blockIndex(0)
                                        .platform(resolved.getPlatform())
                                        .model(actual)
                                        .build());
                        ProviderRequestCancellation.Scope cancellationScope =
                                ProviderRequestCancellation.enter(cancellation);
                        try {
                            model.chat(
                                nativeRequest(resolved),
                                new StreamingChatResponseHandler() {
                                    /**
                                     * 将模型返回的非空文本片段转成对客户端可见的 DELTA 事件。
                                     *
                                     * @param text 本次收到的文本片段
                                     */
                                    @Override
                                    public void onPartialResponse(String text) {
                                        if (text == null || text.isEmpty()) return;
                                        emittedDelta.set(true);
                                        sink.tryEmitNext(
                                                ChatResponse.builder()
                                                        .success(true)
                                                        .eventType("DELTA")
                                                        .content(text)
                                                        .platform(resolved.getPlatform())
                                                        .model(actual)
                                                        .build());
                                    }

                                    /**
                                     * 将模型的非空思考片段转成 REASONING_DELTA 事件。
                                     *
                                     * @param thinking 本次收到的思考片段
                                     */
                                    @Override
                                    public void onPartialThinking(
                                            dev.langchain4j.model.chat.response.PartialThinking
                                                    thinking) {
                                        if (thinking == null
                                                || thinking.text() == null
                                                || thinking.text().isEmpty()) return;
                                        sink.tryEmitNext(
                                                ChatResponse.builder()
                                                        .success(true)
                                                        .eventType("REASONING_DELTA")
                                                        .reasoningContent(thinking.text())
                                                        .platform(resolved.getPlatform())
                                                        .model(actual)
                                                        .build());
                                    }

                                    /**
                                     * 将工具调用片段的标识、名称和参数发给流式订阅方。
                                     *
                                     * @param call 模型返回的工具调用片段
                                     */
                                    @Override
                                    public void onPartialToolCall(PartialToolCall call) {
                                        sink.tryEmitNext(
                                                ChatResponse.builder()
                                                        .success(true)
                                                        .eventType("TOOL_CALL")
                                                        .platform(resolved.getPlatform())
                                                        .model(actual)
                                                        .toolCallId(call.id())
                                                        .toolName(call.name())
                                                        .toolArguments(call.partialArguments())
                                                        .build());
                                    }

                                    /**
                                     * 汇总令牌用量和最终回复，并结束本次模型输出流。
                                     *
                                     * @param response 模型返回的完整响应
                                     */
                                    @Override
                                    public void onCompleteResponse(
                                            dev.langchain4j.model.chat.response.ChatResponse
                                                    response) {
                                        TokenUsage usage = response.tokenUsage();
                                        publishUsage(
                                                resolved,
                                                response.modelName() == null
                                                        ? actual
                                                        : response.modelName(),
                                                usage,
                                                started);
                                        String content =
                                                response.aiMessage() == null
                                                        ? ""
                                                        : Objects.toString(
                                                                response.aiMessage().text(), "");
                                        String thinking =
                                                response.aiMessage() == null
                                                        ? null
                                                        : response.aiMessage().thinking();
                                        // through
                                        // clients
                                        // thinking.
                                        if (!emittedDelta.get() && !content.isBlank()) {
                                            emittedDelta.set(true);
                                            sink.tryEmitNext(
                                                    ChatResponse.builder()
                                                            .success(true)
                                                            .eventType("DELTA")
                                                            .content(content)
                                                            .platform(request.getPlatform())
                                                            .model(actual)
                                                            .build());
                                        }
                                        int promptTokens =
                                                usage == null
                                                        ? estimateInput(request)
                                                        : input(usage);
                                        int completionTokens =
                                                usage == null ? estimate(content) : output(usage);
                                        sink.tryEmitNext(
                                                ChatResponse.builder()
                                                        .success(true)
                                                        .eventType("USAGE")
                                                        .platform(request.getPlatform())
                                                        .model(actual)
                                                        .promptTokens(promptTokens)
                                                        .completionTokens(completionTokens)
                                                        .totalTokens(
                                                                usage == null
                                                                        ? promptTokens
                                                                                + completionTokens
                                                                        : total(usage))
                                                        .estimatedUsage(usage == null)
                                                        .providerRequestId(response.id())
                                                        .finishReason(
                                                                response.finishReason() == null
                                                                        ? null
                                                                        : response.finishReason()
                                                                                .name())
                                                        .build());
                                        sink.tryEmitNext(
                                                ChatResponse.builder()
                                                        .success(true)
                                                        .eventType("BLOCK_COMPLETED")
                                                        .content(content)
                                                        .reasoningContent(thinking)
                                                        .platform(request.getPlatform())
                                                        .model(actual)
                                                        .providerRequestId(response.id())
                                                        .finishReason(
                                                                response.finishReason() == null
                                                                        ? null
                                                                        : response.finishReason()
                                                                                .name())
                                                        .build());
                                        sink.tryEmitNext(
                                                ChatResponse.builder()
                                                        .success(true)
                                                        .eventType("COMPLETED")
                                                        .platform(request.getPlatform())
                                                        .model(actual)
                                                        .providerRequestId(response.id())
                                                        .finishReason(
                                                                response.finishReason() == null
                                                                        ? null
                                                                        : response.finishReason()
                                                                                .name())
                                                        .build());
                                        sink.tryEmitComplete();
                                    }

                                    /**
                                     * 向订阅方发布模型调用失败事件并终止输出流。
                                     *
                                     * @param throwable 模型调用产生的异常
                                     */
                                    @Override
                                    public void onError(Throwable throwable) {
                                        sink.tryEmitNext(
                                                ChatResponse.builder()
                                                        .success(false)
                                                        .eventType("FAILED")
                                                        .errorMessage("model call failed")
                                                        .platform(request.getPlatform())
                                                        .model(actual)
                                                        .build());
                                        sink.tryEmitError(throwable);
                                    }
                                });
                        } finally {
                            cancellationScope.close();
                        }
                    } catch (Throwable throwable) {
                        sink.tryEmitError(throwable);
                    }
                    return sink.asFlux()
                            .doFinally(ignored -> cancellation.cancel())
                            .subscribeOn(Schedulers.boundedElastic());
                }));
    }

    private Flux<ChatResponse> routedStream(ChatRequest request) {
        return Flux.defer(() -> routedStreamAttempt(request));
    }

    private Flux<ChatResponse> routedStreamAttempt(ChatRequest request) {
        java.util.Set<String> required = new java.util.HashSet<>(java.util.Set.of("stream"));
        if (request.getTools() != null && !request.getTools().isEmpty()) required.add("tool_calls");
        TenantId tenantId = routeTenant(request);
        List<ModelProviderCapabilities> candidates =
                modelRouter.candidates(request.getModelRouteId(), tenantId, required);
        if (candidates.isEmpty())
            return Flux.error(
                    new IllegalStateException("no healthy model matches required capabilities"));
        ModelRoutePolicy policy = modelRouter.requirePolicy(request.getModelRouteId(), tenantId);
        java.util.concurrent.atomic.AtomicBoolean visibleOutput =
                new java.util.concurrent.atomic.AtomicBoolean();
        java.util.function.Function<Integer, Flux<ChatResponse>> attempt =
                new java.util.function.Function<>() {
                    /**
                     * {@code apply} 执行当前类型定义的业务操作。
                     *
                     * @param index 参数值，用于执行当前操作。
                     *
                     * @return 返回当前操作产生的结果。
                     */
                    @Override
                    public Flux<ChatResponse> apply(Integer index) {
                        ModelProviderCapabilities candidate = candidates.get(index);
                        if (!modelRouter.tryAcquire(tenantId, candidate)) {
                            if (policy.fallbackOnError() && index + 1 < candidates.size())
                                return apply(index + 1);
                            return Flux.error(
                                    new IllegalStateException(
                                            "no healthy model matches required capabilities"));
                        }
                        ChatRequest routedRequest =
                                routeRequest(request, candidate, policy, true);
                        Flux<ChatResponse> current =
                                routeTimeout(
                                                ChatEngineImpl.this.stream(routedRequest),
                                                policy.timeoutMs())
                                        .doOnNext(
                                                event -> {
                                                    if ("DELTA".equals(event.getEventType())
                                                            || "REASONING_DELTA"
                                                                    .equals(event.getEventType())
                                                            || "TOOL_CALL"
                                                                    .equals(event.getEventType()))
                                                        visibleOutput.set(true);
                                                })
                                        .doOnError(
                                                error ->
                                                        modelRouter.markFailure(
                                                                tenantId,
                                                                candidate.provider(),
                                                                candidate.model(),
                                                                error.getClass().getSimpleName()))
                                                        .doOnComplete(
                                                                () ->
                                                                        modelRouter.markSuccess(
                                                                                tenantId, candidate));
                        if (!policy.fallbackOnError() || index + 1 >= candidates.size())
                            return current;
                        return current.onErrorResume(
                                error ->
                                        visibleOutput.get() ? Flux.error(error) : apply(index + 1));
                    }
                };
        return attempt.apply(0);
    }

    private Flux<ChatResponse> routeTimeout(Flux<ChatResponse> source, int timeoutMs) {
        return Flux.defer(
                () -> {
                    Instant deadline = Instant.now().plusMillis(timeoutMs);
                    return source.timeout(
                            Mono.delay(Duration.ofMillis(timeoutMs)),
                            ignored -> {
                                Duration remaining = Duration.between(Instant.now(), deadline);
                                return Mono.delay(
                                        remaining.isNegative() ? Duration.ZERO : remaining);
                            });
                });
    }

    private ChatRequest routeRequest(
            ChatRequest request,
            ModelProviderCapabilities candidate,
            ModelRoutePolicy policy,
            boolean clearRoute) {
        ChatRequest resolved =
                clearRoute
                        ? copyWithoutRoute(request, candidate.provider(), candidate.model())
                        : copyWithModel(request, candidate.provider(), candidate.model());
        long maxOutputTokens = Math.min(policy.maxTokens(), candidate.maxOutputTokens());
        if (request.getMaxOutputTokens() != null && request.getMaxOutputTokens() > 0)
            maxOutputTokens = Math.min(maxOutputTokens, request.getMaxOutputTokens());
        resolved.setMaxOutputTokens((int) maxOutputTokens);
        return resolved;
    }

    private dev.langchain4j.model.chat.request.ChatRequest nativeRequest(ChatRequest request) {
        Builder builder =
                dev.langchain4j.model.chat.request.ChatRequest.builder()
                        .messages(request.getMessages().stream().map(this::nativeMessage).toList())
                        .modelName(request.getModel());
        if (request.getTemperature() != null) builder.temperature(request.getTemperature());
        if (request.getMaxOutputTokens() != null)
            builder.maxOutputTokens(request.getMaxOutputTokens());
        if (request.getTools() != null && !request.getTools().isEmpty()) {
            builder.toolSpecifications(request.getTools().stream().map(this::nativeTool).toList());
        }
        return builder.build();
    }

    private ChatRequest resolveRequest(ChatRequest request, boolean streaming) {
        if ("default".equalsIgnoreCase(request.getPlatform())) {
            String platform = modelManager.getDefaultPlatform();
            if (platform == null || platform.isBlank()
                    || "default".equalsIgnoreCase(platform)) {
                throw new IllegalStateException("default model platform is unavailable");
            }
            return copyWithoutRoute(request, platform, request.getModel());
        }
        return request;
    }

    private ChatRequest copyWithModel(ChatRequest request, String platform, String model) {
        return ChatRequest.builder()
                .platform(platform)
                .model(model)
                .generationRunId(request.getGenerationRunId())
                .modelRouteId(request.getModelRouteId())
                .tenantId(request.getTenantId())
                .messages(request.getMessages())
                .chatType(request.getChatType())
                .temperature(request.getTemperature())
                .maxOutputTokens(request.getMaxOutputTokens())
                .reasoningEffort(request.getReasoningEffort())
                .tools(request.getTools())
                .build();
    }

    private ChatRequest copyWithoutRoute(ChatRequest request, String platform, String model) {
        return ChatRequest.builder()
                .platform(platform)
                .model(model)
                .generationRunId(request.getGenerationRunId())
                .tenantId(request.getTenantId())
                .userId(request.getUserId())
                .messages(request.getMessages())
                .chatType(request.getChatType())
                .temperature(request.getTemperature())
                .maxOutputTokens(request.getMaxOutputTokens())
                .reasoningEffort(request.getReasoningEffort())
                .tools(request.getTools())
                .build();
    }

    private ToolSpecification nativeTool(ChatRequest.ToolDefinition tool) {
        if (tool == null || tool.name() == null || tool.name().isBlank()) {
            throw new IllegalArgumentException("tool name is required");
        }
        String parameters =
                tool.parametersJson() == null || tool.parametersJson().isBlank()
                        ? "{}"
                        : tool.parametersJson();
        String json =
                com.shiyu.ai.common.foundation.utils.JSONUtils.toJsonString(
                        java.util.Map.of(
                                "name",
                                tool.name(),
                                "description",
                                tool.description() == null ? "" : tool.description(),
                                "parameters",
                                com.shiyu.ai.common.foundation.utils.JSONUtils.parseObject(
                                        parameters, java.util.Map.class)));
        return ToolSpecification.fromJson(json);
    }

    private dev.langchain4j.data.message.ChatMessage nativeMessage(
            com.shiyu.ai.model.contract.model.ChatMessage message) {
        String role = message.role().toUpperCase(java.util.Locale.ROOT);
        String text =
                message.content().stream()
                        .map(ChatMessage.ContentPart::text)
                        .filter(Objects::nonNull)
                        .reduce("", String::concat);
        if ("SYSTEM".equals(role)) return SystemMessage.from(text);
        if ("TOOL".equals(role)) {
            ChatMessage.ContentPart result =
                    message.content().isEmpty() ? null : message.content().get(0);
            String id =
                    result == null || result.toolCallId() == null ? "tool" : result.toolCallId();
            String name = result == null || result.toolName() == null ? "tool" : result.toolName();
            return ToolExecutionResultMessage.from(id, name, text);
        }
        if ("ASSISTANT".equals(role)) {
            List<ToolExecutionRequest> calls =
                    message.content().stream()
                            .filter(p -> "tool_call".equalsIgnoreCase(p.type()))
                            .map(
                                    p ->
                                            ToolExecutionRequest.builder()
                                                    .id(p.toolCallId())
                                                    .name(p.toolName())
                                                    .arguments(
                                                            Objects.toString(
                                                                    p.toolArguments(),
                                                                    Objects.toString(
                                                                            p.text(), "{}")))
                                                    .build())
                            .toList();
            return calls.isEmpty() ? AiMessage.from(text) : AiMessage.from(text, calls);
        }
        List<Content> contents = new ArrayList<>();
        for (ChatMessage.ContentPart part : message.content()) {
            if ("image".equalsIgnoreCase(part.type()) && part.uri() != null)
                contents.add(ImageContent.from(part.uri()));
            else if ("audio".equalsIgnoreCase(part.type()) && part.uri() != null)
                contents.add(AudioContent.from(part.uri()));
            else if ("file".equalsIgnoreCase(part.type()))
                throw new IllegalArgumentException(
                        "configured provider does not support file content parts");
            else if (part.text() != null) contents.add(TextContent.from(part.text()));
        }
        return UserMessage.from(contents.isEmpty() ? List.of(TextContent.from(text)) : contents);
    }

    private String validate(ChatRequest request) {
        if (request == null || request.getPlatform() == null || request.getPlatform().isBlank())
            return "platform is required";
        if (request.getMessages() == null || request.getMessages().isEmpty())
            return "messages cannot be empty";
        for (ChatMessage message : request.getMessages()) {
            if (message == null || message.role() == null || message.role().isBlank())
                return "message role is required";
            for (ChatMessage.ContentPart part : message.content()) {
                if (part == null || part.type() == null || part.type().isBlank())
                    return "content part type is required";
                String type = part.type().toLowerCase(java.util.Locale.ROOT);
                if (!java.util.Set.of(
                                "text",
                                "reasoning",
                                "image",
                                "audio",
                                "file",
                                "tool_call",
                                "tool_result")
                        .contains(type)) {
                    return "unsupported content part type: " + type;
                }
            }
        }
        if (request.getTenantId() <= 0) return "tenantId is required";
        return null;
    }

    private ChatResponse error(String message, ChatRequest request) {
        return ChatResponse.builder()
                .success(false)
                .eventType("FAILED")
                .errorMessage(message)
                .platform(request == null ? null : request.getPlatform())
                .model(request == null ? null : request.getModel())
                .build();
    }

    private void publishUsage(ChatRequest request, String model, TokenUsage usage, long started) {
        eventPublisher.publishEvent(
                new ModelCallEvent(
                        request.getPlatform(),
                        model,
                        input(usage),
                        output(usage),
                        System.currentTimeMillis() - started,
                        request.getGenerationRunId(),
                        eventTenant(request),
                        eventUser(request)));
    }

    private void publishUsageValues(ChatRequest request, ChatResponse response, long started) {
        if (response == null) return;
        eventPublisher.publishEvent(
                new ModelCallEvent(
                        request.getPlatform(),
                        response.getModel() == null ? request.getModel() : response.getModel(),
                        response.getPromptTokens() == null ? 0 : response.getPromptTokens(),
                        response.getCompletionTokens() == null ? 0 : response.getCompletionTokens(),
                        System.currentTimeMillis() - started,
                        request.getGenerationRunId(),
                        eventTenant(request),
                        eventUser(request)));
    }

    private boolean isDeepSeekPlatform(ChatRequest request) {
        return request != null && "DEEPSEEK".equalsIgnoreCase(request.getPlatform());
    }

    private TenantId routeTenant(ChatRequest request) {
        if (request == null || request.getTenantId() <= 0) return null;
        return new TenantId(request.getTenantId());
    }

    private TenantId eventTenant(ChatRequest request) {
        return request == null || request.getTenantId() <= 0
                ? null
                : new TenantId(request.getTenantId());
    }

    private UserId eventUser(ChatRequest request) {
        return request == null || request.getUserId() <= 0 ? null : new UserId(request.getUserId());
    }

    private void requireDeepSeekProvider() {
        if (modelManager.getDeepSeekProvider() == null
                || !modelManager.getDeepSeekProvider().isAvailable()) {
            throw new IllegalStateException("DeepSeek structured provider is not configured");
        }
    }

    private int input(TokenUsage usage) {
        return usage == null || usage.inputTokenCount() == null ? 0 : usage.inputTokenCount();
    }

    private int output(TokenUsage usage) {
        return usage == null || usage.outputTokenCount() == null ? 0 : usage.outputTokenCount();
    }

    private int total(TokenUsage usage) {
        return usage == null || usage.totalTokenCount() == null
                ? input(usage) + output(usage)
                : usage.totalTokenCount();
    }

    private int estimateInput(ChatRequest request) {
        return Math.max(
                1,
                request.getMessages().stream()
                        .mapToInt(
                                m ->
                                        estimate(
                                                m.content().stream()
                                                        .map(ChatMessage.ContentPart::text)
                                                        .filter(Objects::nonNull)
                                                        .reduce("", String::concat)))
                        .sum());
    }

    private int estimate(String value) {
        return Math.max(1, Objects.toString(value, "").length() / 4);
    }
}
