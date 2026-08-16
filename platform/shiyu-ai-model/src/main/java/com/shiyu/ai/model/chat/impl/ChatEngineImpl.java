package com.shiyu.ai.model.chat.impl;

import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatMessage;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.model.event.ModelCallEvent;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.AudioContent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class ChatEngineImpl implements ChatEngine {
    private final ModelManager modelManager;
    private final ApplicationEventPublisher eventPublisher;

    public ChatEngineImpl(ModelManager modelManager, ApplicationEventPublisher eventPublisher) {
        this.modelManager = modelManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ChatResponse chat(com.shiyu.ai.model.chat.ChatRequest request) {
        String error = validate(request);
        if (error != null) return error(error, request);
        long started = System.currentTimeMillis();
        try {
            ChatModel model = modelManager.getChatModel(request.getPlatform(), request.getModel());
            if (model == null) throw new IllegalStateException("model is unavailable");
            dev.langchain4j.model.chat.response.ChatResponse response = model.chat(nativeRequest(request));
            TokenUsage usage = response.tokenUsage();
            String actual = response.modelName() == null ? request.getModel() : response.modelName();
            publishUsage(request, actual, usage, started);
            String content = response.aiMessage() == null ? "" : Objects.toString(response.aiMessage().text(), "");
            List<ChatResponse.ToolCall> toolCalls = response.aiMessage() == null ? List.of()
                    : response.aiMessage().toolExecutionRequests().stream()
                    .map(call -> new ChatResponse.ToolCall(call.id(), call.name(), call.arguments()))
                    .toList();
            int promptTokens = usage == null ? estimateInput(request) : input(usage);
            int completionTokens = usage == null ? estimate(content) : output(usage);
            return ChatResponse.builder().success(true).eventType("COMPLETED")
                    .content(content).platform(request.getPlatform()).model(actual).promptTokens(promptTokens)
                    .completionTokens(completionTokens).totalTokens(usage == null ? promptTokens + completionTokens : total(usage)).estimatedUsage(usage == null)
                    .toolCalls(toolCalls).build();
        } catch (Exception ex) {
            log.error("model chat failed platform={} model={} errorCode={}", request.getPlatform(), request.getModel(), ex.getClass().getSimpleName());
            return error("model call failed", request);
        }
    }

    @Override
    public Flux<ChatResponse> stream(ChatRequest request) {
        String error = validate(request);
        if (error != null) return Flux.error(new IllegalArgumentException(error));
        return Flux.defer(() -> {
            Sinks.Many<ChatResponse> sink = Sinks.many().unicast().onBackpressureBuffer();
            long started = System.currentTimeMillis();
            AtomicBoolean emittedDelta = new AtomicBoolean();
            try {
                StreamingChatModel model = modelManager.getStreamingChatModel(request.getPlatform(), request.getModel());
                if (model == null) return Flux.error(new IllegalStateException("streaming model is unavailable"));
                String actual = request.getModel();
                model.chat(nativeRequest(request), new StreamingChatResponseHandler() {
                    @Override public void onPartialResponse(String text) {
                        if (text == null || text.isEmpty()) return;
                        emittedDelta.set(true);
                        sink.tryEmitNext(ChatResponse.builder().success(true).eventType("DELTA").content(text).platform(request.getPlatform()).model(actual).build());
                    }
                    @Override public void onPartialToolCall(PartialToolCall call) {
                        sink.tryEmitNext(ChatResponse.builder().success(true).eventType("TOOL_CALL")
                                .platform(request.getPlatform()).model(actual).toolCallId(call.id()).toolName(call.name()).toolArguments(call.partialArguments()).build());
                    }
                    @Override public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                        TokenUsage usage = response.tokenUsage();
                        publishUsage(request, response.modelName() == null ? actual : response.modelName(), usage, started);
                        String content = response.aiMessage() == null ? "" : Objects.toString(response.aiMessage().text(), "");
                        // Reasoning-first providers may not emit answer deltas through
                        // onPartialResponse. Flush the final answer once so SSE clients
                        // receive content even when the provider only streams thinking.
                        if (!emittedDelta.get() && !content.isBlank()) {
                            emittedDelta.set(true);
                            sink.tryEmitNext(ChatResponse.builder().success(true).eventType("DELTA").content(content).platform(request.getPlatform()).model(actual).build());
                        }
                        int promptTokens = usage == null ? estimateInput(request) : input(usage);
                        int completionTokens = usage == null ? estimate(content) : output(usage);
                        sink.tryEmitNext(ChatResponse.builder().success(true).eventType("USAGE")
                                .platform(request.getPlatform()).model(actual).promptTokens(promptTokens).completionTokens(completionTokens)
                                .totalTokens(usage == null ? promptTokens + completionTokens : total(usage)).estimatedUsage(usage == null).build());
                        sink.tryEmitNext(ChatResponse.builder().success(true).eventType("COMPLETED").platform(request.getPlatform()).model(actual).build());
                        sink.tryEmitComplete();
                    }
                    @Override public void onError(Throwable throwable) {
                        sink.tryEmitNext(ChatResponse.builder().success(false).eventType("FAILED").errorMessage("model call failed").platform(request.getPlatform()).model(actual).build());
                        sink.tryEmitError(throwable);
                    }
                });
            } catch (Throwable throwable) {
                sink.tryEmitError(throwable);
            }
            return sink.asFlux().subscribeOn(Schedulers.boundedElastic());
        });
    }

    private dev.langchain4j.model.chat.request.ChatRequest nativeRequest(ChatRequest request) {
        Builder builder = dev.langchain4j.model.chat.request.ChatRequest.builder()
                .messages(request.getMessages().stream().map(this::nativeMessage).toList())
                .modelName(request.getModel());
        if (request.getTemperature() != null) builder.temperature(request.getTemperature());
        if (request.getMaxOutputTokens() != null) builder.maxOutputTokens(request.getMaxOutputTokens());
        if (request.getTools() != null && !request.getTools().isEmpty()) {
            builder.toolSpecifications(request.getTools().stream().map(this::nativeTool).toList());
        }
        return builder.build();
    }

    private ToolSpecification nativeTool(ChatRequest.ToolDefinition tool) {
        if (tool == null || tool.name() == null || tool.name().isBlank()) {
            throw new IllegalArgumentException("tool name is required");
        }
        String parameters = tool.parametersJson() == null || tool.parametersJson().isBlank()
                ? "{}" : tool.parametersJson();
        // ToolSpecification.fromJson lets LangChain4j validate and preserve
        // the provider-neutral JSON Schema without a vendor-specific mapper.
        String json = com.shiyu.ai.common.core.utils.JSONUtils.toJsonString(java.util.Map.of(
                "name", tool.name(), "description", tool.description() == null ? "" : tool.description(),
                "parameters", com.shiyu.ai.common.core.utils.JSONUtils.parseObject(parameters, java.util.Map.class)));
        return ToolSpecification.fromJson(json);
    }

    private dev.langchain4j.data.message.ChatMessage nativeMessage(com.shiyu.ai.model.chat.ChatMessage message) {
        String role = message.role().toUpperCase(java.util.Locale.ROOT);
        String text = message.content().stream().map(ChatMessage.ContentPart::text).filter(Objects::nonNull).reduce("", String::concat);
        if ("SYSTEM".equals(role)) return SystemMessage.from(text);
        if ("TOOL".equals(role)) {
            ChatMessage.ContentPart result = message.content().isEmpty() ? null : message.content().get(0);
            String id = result == null || result.uri() == null ? "tool" : result.uri();
            String name = result == null || result.mimeType() == null ? "tool" : result.mimeType();
            return ToolExecutionResultMessage.from(id, name, text);
        }
        if ("ASSISTANT".equals(role)) {
            List<ToolExecutionRequest> calls = message.content().stream().filter(p -> "tool_call".equalsIgnoreCase(p.type()))
                    .map(p -> ToolExecutionRequest.builder().id(p.uri()).name(p.mimeType()).arguments(Objects.toString(p.text(), "{}")).build()).toList();
            return calls.isEmpty() ? AiMessage.from(text) : AiMessage.from(text, calls);
        }
        List<Content> contents = new ArrayList<>();
        for (ChatMessage.ContentPart part : message.content()) {
            if ("image".equalsIgnoreCase(part.type()) && part.uri() != null) contents.add(ImageContent.from(part.uri()));
            else if ("audio".equalsIgnoreCase(part.type()) && part.uri() != null) contents.add(AudioContent.from(part.uri()));
            else if (part.text() != null) contents.add(TextContent.from(part.text()));
        }
        return UserMessage.from(contents.isEmpty() ? List.of(TextContent.from(text)) : contents);
    }

    private String validate(ChatRequest request) {
        if (request == null || request.getPlatform() == null || request.getPlatform().isBlank()) return "platform is required";
        if (request.getMessages() == null || request.getMessages().isEmpty()) return "messages cannot be empty";
        return null;
    }

    private ChatResponse error(String message, ChatRequest request) {
        return ChatResponse.builder().success(false).eventType("FAILED").errorMessage(message)
                .platform(request == null ? null : request.getPlatform()).model(request == null ? null : request.getModel()).build();
    }

    private void publishUsage(ChatRequest request, String model, TokenUsage usage, long started) {
        eventPublisher.publishEvent(new ModelCallEvent(request.getPlatform(), model,
                input(usage), output(usage), System.currentTimeMillis() - started, request.getGenerationRunId()));
    }

    private int input(TokenUsage usage) { return usage == null || usage.inputTokenCount() == null ? 0 : usage.inputTokenCount(); }
    private int output(TokenUsage usage) { return usage == null || usage.outputTokenCount() == null ? 0 : usage.outputTokenCount(); }
    private int total(TokenUsage usage) { return usage == null || usage.totalTokenCount() == null ? input(usage) + output(usage) : usage.totalTokenCount(); }
    private int estimateInput(ChatRequest request) { return Math.max(1, request.getMessages().stream().mapToInt(m -> estimate(m.content().stream().map(ChatMessage.ContentPart::text).filter(Objects::nonNull).reduce("", String::concat))).sum()); }
    private int estimate(String value) { return Math.max(1, Objects.toString(value, "").length() / 4); }
}
