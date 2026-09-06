package com.shiyu.ai.conversation.web;

import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.conversation.ConversationService;
import com.shiyu.ai.conversation.PromptAssembler;
import com.shiyu.ai.conversation.domain.ConversationMessage;
import com.shiyu.ai.conversation.domain.MessageRole;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatMessage;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.model.port.ModelRoutingPort;
import com.shiyu.ai.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** OpenAI-compatible facade. It is stateless unless store=true is explicitly requested. */
@RestController
@RequestMapping("/api")
public class OpenAiCompatibleController {
    private final ChatEngine chatEngine;
    private final ModelRoutingPort modelManager;
    private final ConversationService conversationService;
    private final ConversationRepository conversationRepository;
    private final AiRuntimePort runtime;

    public OpenAiCompatibleController(ChatEngine chatEngine, ModelRoutingPort modelManager, ConversationService conversationService, ConversationRepository conversationRepository, AiRuntimePort runtime) {
        this.chatEngine = chatEngine;
        this.modelManager = modelManager;
        this.conversationService = conversationService;
        this.conversationRepository = conversationRepository;
        this.runtime = runtime;
    }

    @GetMapping("/model/models")
    public Map<String, Object> models() {
        List<Map<String, Object>> data = modelManager.availableModels().stream()
                .map(model -> Map.<String, Object>of("id", model.id(), "object", "model", "owned_by", model.platform()))
                .toList();
        return Map.of("object", "list", "data", data);
    }

    @PostMapping(value = "/conversation/chat/completions", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public Object chatCompletions(@RequestBody ChatCompletionRequest request) {
        List<ChatMessage> messages = structured(request.messages());
        String provider = platform(request.model(), request.platform());
        ChatRequest modelRequest = ChatRequest.builder().platform(provider).model(request.model()).messages(messages)
                .tenantId(currentTenantId()).temperature(request.temperature()).maxOutputTokens(safeMaxOutputTokens(request.maxOutputTokens()))
                .userId(ActorContextHttpAdapter.userId())
                .reasoningEffort(request.reasoningEffort()).tools(tools(request.tools())).build();
        AiRun run = beginRun(request.model(), messages, request.conversationId());
        boolean store = Boolean.TRUE.equals(request.store()) || hasText(request.conversationId());
        if (Boolean.TRUE.equals(request.stream())) return stream(modelRequest, request.model(), provider, "chat.completion", store, request.conversationId(), messages, run);
        if (run != null) runtime.append(run, AiRunEventType.MODEL_STARTED, "{}", true);
        ChatResponse response;
        try { response = chatEngine.chat(modelRequest); }
        catch (RuntimeException ex) { failRun(run, ex); throw ex; }
        requireSuccessful(response, run);
        persistIfRequested(store, request.conversationId(), messages, response.getContent(), provider, request.model(), run);
        completeRun(run, response);
        return completion(request.model(), response);
    }

    @PostMapping(value = "/conversation/responses", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public Object responses(@RequestBody ResponsesRequest request) {
        List<Map<String, Object>> raw = messageMaps(request.input());
        List<ChatMessage> messages = raw.isEmpty() && request.input() instanceof String s
                ? List.of(ChatMessage.text("user", s)) : structured(raw);
        String provider = platform(request.model(), request.platform());
        ChatRequest modelRequest = ChatRequest.builder().platform(provider).model(request.model()).messages(messages)
                .tenantId(currentTenantId()).temperature(request.temperature()).maxOutputTokens(safeMaxOutputTokens(request.maxOutputTokens()))
                .userId(ActorContextHttpAdapter.userId())
                .reasoningEffort(request.reasoningEffort()).tools(tools(request.tools())).build();
        AiRun run = beginRun(request.model(), messages, request.conversationId());
        boolean store = Boolean.TRUE.equals(request.store()) || hasText(request.conversationId());
        if (Boolean.TRUE.equals(request.stream())) return stream(modelRequest, request.model(), provider, "response.output_text.delta", store, request.conversationId(), messages, run);
        if (run != null) runtime.append(run, AiRunEventType.MODEL_STARTED, "{}", true);
        ChatResponse response;
        try { response = chatEngine.chat(modelRequest); }
        catch (RuntimeException ex) { failRun(run, ex); throw ex; }
        requireSuccessful(response, run);
        persistIfRequested(store, request.conversationId(), messages, response.getContent(), provider, request.model(), run);
        completeRun(run, response);
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("type", "message"); output.put("role", "assistant");
        output.put("content", List.of(Map.of("type", "output_text", "text", Optional.ofNullable(response.getContent()).orElse(""))));
        if (response.getToolCalls() != null && !response.getToolCalls().isEmpty()) {
            output.put("tool_calls", response.getToolCalls().stream().map(call -> Map.of("id", call.id(), "type", "function",
                    "name", call.name(), "arguments", Optional.ofNullable(call.arguments()).orElse("{}"))).toList());
        }
        return Map.of("id", "resp_" + UUID.randomUUID(), "object", "response", "created", Instant.now().getEpochSecond(), "model", request.model(),
                "output", List.of(output), "usage", usage(response));
    }

    private Flux<ServerSentEvent<String>> stream(ChatRequest request, String model, String provider, String event, Boolean store, String conversationId, List<ChatMessage> storeMessages, AiRun run) {
        String id = "chatcmpl_" + UUID.randomUUID();
        boolean responses = event.startsWith("response.");
        StringBuilder answer = new StringBuilder();
        AtomicInteger seq = new AtomicInteger();
        java.util.concurrent.atomic.AtomicReference<String> terminal = new java.util.concurrent.atomic.AtomicReference<>();
        if (run != null) runtime.append(run, AiRunEventType.MODEL_STARTED, "{}", true);
        return chatEngine.stream(request).map(chunk -> {
            String type = chunk.getEventType() == null ? "DELTA" : chunk.getEventType();
            if ("COMPLETED".equals(type) || "FAILED".equals(type) || "CANCELLED".equals(type)) terminal.set(type);
            appendRuntimeEvent(run, chunk, type);
            Map<String, Object> payload;
            if ("TOOL_CALL".equals(type)) {
                payload = responses
                        ? Map.of("type", "response.function_call_arguments.delta", "response_id", id, "delta", Optional.ofNullable(chunk.getToolArguments()).orElse("{}"))
                        : Map.of("id", id, "object", "chat.completion.chunk", "model", model, "choices", List.of(Map.of("index", 0, "delta", Map.of(
                        "tool_call_id", Optional.ofNullable(chunk.getToolCallId()).orElse(""),
                        "name", Optional.ofNullable(chunk.getToolName()).orElse("tool"),
                        "arguments", Optional.ofNullable(chunk.getToolArguments()).orElse("{}")))));
            } else if ("REASONING_DELTA".equals(type)) {
                String reasoning = Optional.ofNullable(chunk.getReasoningContent()).orElse("");
                payload = responses
                        ? Map.of("type", "response.reasoning_text.delta", "response_id", id, "delta", reasoning)
                        : Map.of("id", id, "object", "chat.completion.chunk", "model", model, "choices", List.of(Map.of("index", 0, "delta", Map.of("reasoning_content", reasoning))));
            } else if ("USAGE".equals(type)) {
                payload = responses
                        ? Map.of("type", "response.usage", "response_id", id, "usage", usage(chunk))
                        : Map.of("id", id, "object", "chat.completion.chunk", "model", model, "choices", List.of(), "usage", usage(chunk));
            } else if ("COMPLETED".equals(type)) {
                payload = responses
                        ? Map.of("type", "response.completed", "response_id", id)
                        : Map.of("id", id, "object", "chat.completion.chunk", "model", model, "choices", List.of(Map.of("index", 0, "delta", Map.of(), "finish_reason", "stop")));
            } else if ("FAILED".equals(type) || "CANCELLED".equals(type)) {
                payload = responses
                        ? Map.of("type", "response." + type.toLowerCase(Locale.ROOT), "response_id", id,
                        "error", Optional.ofNullable(chunk.getErrorMessage()).orElse("model call failed"))
                        : Map.of("id", id, "object", "chat.completion.chunk", "model", model, "choices", List.of(),
                        "error", Optional.ofNullable(chunk.getErrorMessage()).orElse("model call failed"));
            } else {
                String text = Optional.ofNullable(chunk.getContent()).orElse("");
                answer.append(text);
                payload = responses
                        ? Map.of("type", "response.output_text.delta", "response_id", id, "delta", text)
                        : Map.of("id", id, "object", "chat.completion.chunk", "model", model, "choices", List.of(Map.of("index", 0, "delta", Map.of("content", text))));
            }
            return ServerSentEvent.<String>builder().id(String.valueOf(seq.getAndIncrement())).event(event).data(JSONUtils.toJsonString(payload)).build();
        }).concatWith(Flux.just(ServerSentEvent.<String>builder().id(String.valueOf(seq.getAndIncrement())).data("[DONE]").build()))
                .doOnComplete(() -> {
                    if ("COMPLETED".equals(terminal.get())) {
                        if (Boolean.TRUE.equals(store)) persistIfRequested(true, conversationId, storeMessages, answer.toString(), provider, model, run);
                        completeRun(run, null);
                    } else if ("CANCELLED".equals(terminal.get())) {
                        cancelRun(run);
                    } else {
                        failRun(run, new IllegalStateException("model stream ended without completion"));
                    }
                })
                .doOnError(error -> failRun(run, error));
    }

    private AiRun beginRun(String model, List<ChatMessage> messages, String conversationId) {
        long tenant = ActorContextHttpAdapter.tenantId(); long owner = ActorContextHttpAdapter.userId();
        return runtime.startRun(new AiRunContext(new TenantId(tenant), owner, null, null, conversationId, null, null, null, Map.of()), AiRunSource.API, "openai", model, JSONUtils.toJsonString(messages));
    }

    private long currentTenantId() {
        return ActorContextHttpAdapter.tenantId();
    }

    private Integer safeMaxOutputTokens(Integer requested) {
        if (requested == null) return null;
        return Math.max(1, Math.min(requested, 128_000));
    }
    private void appendRuntimeEvent(AiRun run, ChatResponse chunk, String type) {
        if (run == null) return;
        if ("USAGE".equals(type)) {
            int prompt = chunk.getPromptTokens() == null ? 0 : chunk.getPromptTokens();
            int completion = chunk.getCompletionTokens() == null ? 0 : chunk.getCompletionTokens();
            runtime.recordUsage(run.id(), run.tenantId(), run.ownerUserId().value(), prompt, completion, chunk.isEstimatedUsage(), null);
            runtime.append(run, AiRunEventType.MODEL_USAGE, JSONUtils.toJsonString(Map.of("promptTokens", prompt, "completionTokens", completion, "estimated", chunk.isEstimatedUsage())), true);
        } else if ("TOOL_CALL".equals(type)) runtime.append(run, AiRunEventType.MODEL_TOOL_CALL_DELTA, JSONUtils.toJsonString(Map.of("id", Optional.ofNullable(chunk.getToolCallId()).orElse(""), "name", Optional.ofNullable(chunk.getToolName()).orElse("tool"))), true);
        else if ("REASONING_DELTA".equals(type)) runtime.append(run, AiRunEventType.MODEL_REASONING_DELTA, "{}", true);
        else if ("BLOCK_STARTED".equals(type)) runtime.append(run, AiRunEventType.MODEL_BLOCK_STARTED, "{}", true);
        else if ("BLOCK_COMPLETED".equals(type)) runtime.append(run, AiRunEventType.MODEL_BLOCK_COMPLETED, "{}", true);
        else if (!"COMPLETED".equals(type)) runtime.append(run, AiRunEventType.MODEL_DELTA, "{}", true);
    }
    private void completeRun(AiRun run, ChatResponse response) {
        if (run == null) return;
        if (response != null) {
            int prompt = response.getPromptTokens() == null ? 0 : response.getPromptTokens();
            int completion = response.getCompletionTokens() == null ? estimate(response.getContent()) : response.getCompletionTokens();
            runtime.recordUsage(run.id(), run.tenantId(), run.ownerUserId().value(), prompt, completion, response.isEstimatedUsage(), null);
        }
        runtime.finish(run.id(), run.tenantId(), run.ownerUserId().value(), AiRunStatus.COMPLETED, null);
    }

    private void requireSuccessful(ChatResponse response, AiRun run) {
        if (response != null && response.isSuccess()) return;
        String message = response == null || response.getErrorMessage() == null || response.getErrorMessage().isBlank()
                ? "model call failed" : response.getErrorMessage();
        failRun(run, new IllegalStateException(message));
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_GATEWAY, "model call failed");
    }
    private void failRun(AiRun run, Throwable error) {
        if (run == null) return;
        runtime.finish(run.id(), run.tenantId(), run.ownerUserId().value(), AiRunStatus.FAILED, error == null ? "MODEL_ERROR" : error.getClass().getSimpleName());
    }
    private void cancelRun(AiRun run) {
        if (run == null) return;
        runtime.finish(run.id(), run.tenantId(), run.ownerUserId().value(), AiRunStatus.CANCELLED, "MODEL_CANCELLED");
    }

    private Map<String, Object> completion(String model, ChatResponse response) {
        String content = Optional.ofNullable(response.getContent()).orElse("");
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "assistant");
        message.put("content", content);
        if (response.getToolCalls() != null && !response.getToolCalls().isEmpty()) {
            message.put("tool_calls", response.getToolCalls().stream().map(call -> Map.of(
                    "id", call.id(), "type", "function", "function", Map.of(
                            "name", call.name(), "arguments", Optional.ofNullable(call.arguments()).orElse("{}")))).toList());
        }
        return Map.of("id", "chatcmpl_" + UUID.randomUUID(), "object", "chat.completion", "created", Instant.now().getEpochSecond(), "model", model,
                "choices", List.of(Map.of("index", 0, "message", message, "finish_reason", response.getToolCalls() == null || response.getToolCalls().isEmpty() ? "stop" : "tool_calls")), "usage", usage(response));
    }

    private Map<String, Integer> usage(ChatResponse response) {
        int p = response.getPromptTokens() == null ? estimate(response.getContent()) : response.getPromptTokens();
        int c = response.getCompletionTokens() == null ? estimate(response.getContent()) : response.getCompletionTokens();
        return Map.of("prompt_tokens", p, "completion_tokens", c, "total_tokens", response.getTotalTokens() == null ? p + c : response.getTotalTokens());
    }

    private int estimate(String value) { return Math.max(1, Optional.ofNullable(value).orElse("").length() / 4); }
    private String platform(String model, String requestedPlatform) {
        if (hasText(requestedPlatform)) return requestedPlatform.trim().toUpperCase(Locale.ROOT);
        if (hasText(model)) {
            return modelManager.resolvePlatform(model);
        }
        return modelManager.defaultPlatform();
    }

    private List<ChatMessage> structured(List<Map<String, Object>> messages) {
        if (messages == null) return List.of();
        return messages.stream().map(message -> {
            String role = String.valueOf(message.getOrDefault("role", "user"));
            Object raw = message.get("content");
            List<ChatMessage.ContentPart> content = new ArrayList<>();
            if (raw instanceof String text) content.add(new ChatMessage.ContentPart("text", text, null, null));
            else if (raw instanceof Collection<?> parts) for (Object part : parts) content.add(contentPart(part));
            else if (raw != null) content.add(new ChatMessage.ContentPart("text", String.valueOf(raw), null, null));
            if ("assistant".equalsIgnoreCase(role) && message.get("tool_calls") instanceof Collection<?> calls) {
                for (Object call : calls) {
                    if (!(call instanceof Map<?, ?> map)) continue;
                    Object fn = map.get("function");
                    if (fn instanceof Map<?, ?> function) content.add(new ChatMessage.ContentPart("tool_call",
                            String.valueOf(value(function, "arguments", "{}")),
                            null, null,
                            String.valueOf(value(map, "id", "tool-call")),
                            String.valueOf(value(function, "name", "tool")),
                            String.valueOf(value(function, "arguments", "{}")), null));
                }
            }
            if ("tool".equalsIgnoreCase(role)) {
                String id = String.valueOf(message.getOrDefault("tool_call_id", "tool"));
                content = List.of(new ChatMessage.ContentPart("tool_result", textContent(content), null, null, id, String.valueOf(message.getOrDefault("name", "tool")), null, null));
            }
            return new ChatMessage(role, content);
        }).toList();
    }

    private ChatMessage.ContentPart contentPart(Object value) {
        if (!(value instanceof Map<?, ?> map)) return new ChatMessage.ContentPart("text", String.valueOf(value), null, null);
        String type = String.valueOf(value(map, "type", "text"));
        Object text = map.containsKey("text") ? map.get("text") : map.get("content");
        Object uri = map.get("image_url");
        if (uri instanceof Map<?, ?> image) uri = image.get("url");
        if ("input_text".equals(type)) type = "text";
        if ("input_image".equals(type)) type = "image";
        if ("input_audio".equals(type)) type = "audio";
        return new ChatMessage.ContentPart(type, text == null ? null : String.valueOf(text), uri == null ? null : String.valueOf(uri),
                map.get("mime_type") == null ? null : String.valueOf(map.get("mime_type")));
    }

    private String textContent(List<ChatMessage.ContentPart> parts) {
        return parts.stream().map(ChatMessage.ContentPart::text).filter(Objects::nonNull).reduce("", String::concat);
    }

    private List<ChatRequest.ToolDefinition> tools(List<Map<String, Object>> raw) {
        if (raw == null) return List.of();
        List<ChatRequest.ToolDefinition> result = new ArrayList<>();
        for (Map<String, Object> item : raw) {
            if (item == null) continue;
            Object function = item.get("function");
            Map<?, ?> fn = function instanceof Map<?, ?> map ? map : item;
            String name = String.valueOf(value(fn, "name", ""));
            if (name.isBlank()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "tool function name is required");
            Object schema = fn.get("parameters");
            result.add(new ChatRequest.ToolDefinition(name, String.valueOf(value(fn, "description", "")), schema == null ? "{}" : JSONUtils.toJsonString(schema)));
        }
        return List.copyOf(result);
    }

    private Object value(Map<?, ?> map, String key, Object fallback) {
        Object value = map.get(key);
        return value == null ? fallback : value;
    }

    @SuppressWarnings("unchecked") private List<Map<String, Object>> messageMaps(Object input) {
        if (!(input instanceof Collection<?> collection)) return List.of();
        return collection.stream().filter(Map.class::isInstance).map(value -> (Map<String, Object>) value).toList();
    }

    private void persistIfRequested(Boolean store, String conversationId, List<ChatMessage> messages, String answer, String provider, String model, AiRun runtimeRun) {
        if (!Boolean.TRUE.equals(store)) return;
        long tenant = ActorContextHttpAdapter.tenantId(); long owner = ActorContextHttpAdapter.userId();
        var conversation = hasText(conversationId)
                ? conversationRepository.findConversation(conversationId, new TenantId(tenant), owner)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "conversation not found"))
                : conversationService.create(new TenantId(tenant), owner, provider, provider + " API", provider, model, null);
        // OpenAI clients normally send the complete visible history on every
        // request.  Persist only the suffix that is not already present on the
        // active message path; otherwise store=true would duplicate the entire
        // conversation on every call.
        List<ConversationMessage> existingPath = PromptAssembler.activePath(
                conversationRepository.listMessages(conversation.id(), new TenantId(tenant), owner, 1000).reversed(),
                conversation.activeLeafMessageId(), 1000);
        int existingCursor = 0;
        ConversationMessage lastUser = null;
        for (ChatMessage message : messages) {
            var current = conversationRepository.findConversation(conversation.id(), new TenantId(tenant), owner).orElse(conversation);
            MessageRole role; try { role = MessageRole.valueOf(message.role().toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { role = MessageRole.USER; }
            String text = message.content().stream().map(ChatMessage.ContentPart::text).filter(Objects::nonNull).reduce("", String::concat);
            if (existingCursor < existingPath.size() && sameMessage(existingPath.get(existingCursor), role, text)) {
                if (role == MessageRole.USER) lastUser = existingPath.get(existingCursor);
                existingCursor++;
                continue;
            }
            var saved = conversationService.appendMessage(current, current.activeLeafMessageId(), role, text, null, null);
            if (role == MessageRole.USER) lastUser = saved;
        }
        if (lastUser != null) {
            int promptTokens = messages.stream().mapToInt(message -> estimate(message.content().stream().map(ChatMessage.ContentPart::text).filter(Objects::nonNull).reduce("", String::concat))).sum();
            int completionTokens = estimate(answer);
            conversationService.recordCompletedGeneration(conversation, lastUser, answer, provider, model, promptTokens, completionTokens, runtime, runtimeRun);
        }
    }

    private boolean sameMessage(ConversationMessage existing, MessageRole role, String text) {
        return existing.role() == role && Objects.equals(existing.textContent(), text);
    }

    public record ChatCompletionRequest(String model, String platform, List<Map<String, Object>> messages, Boolean stream, Boolean store,
                                        Double temperature, @JsonAlias({"max_tokens", "max_output_tokens"}) Integer maxOutputTokens,
                                        List<Map<String, Object>> tools,
                                        @JsonAlias({"conversation_id", "shiyu_conversation_id"}) String conversationId,
                                        @JsonAlias({"reasoning_effort"}) String reasoningEffort) {}
    public record ResponsesRequest(String model, String platform, Object input, Boolean stream, Boolean store,
                                   Double temperature, @JsonAlias({"max_output_tokens", "max_tokens"}) Integer maxOutputTokens,
                                   List<Map<String, Object>> tools,
                                   @JsonAlias({"conversation_id", "shiyu_conversation_id"}) String conversationId,
                                   @JsonAlias({"reasoning_effort"}) String reasoningEffort) {}

    private boolean hasText(String value) { return value != null && !value.isBlank(); }
}
