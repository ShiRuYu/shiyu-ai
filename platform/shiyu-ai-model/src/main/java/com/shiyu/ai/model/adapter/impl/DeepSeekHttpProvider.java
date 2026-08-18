package com.shiyu.ai.model.adapter.impl;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.model.chat.ChatMessage;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DeepSeek's OpenAI-compatible transport, kept separate from the generic
 * LangChain adapter so reasoning, usage-only frames and tool-call deltas are
 * not lost during provider translation.
 */
public final class DeepSeekHttpProvider {
    private final HttpClient client;
    private final String endpoint;
    private final String apiKey;
    private final String defaultModel;
    private final Duration requestTimeout;

    public DeepSeekHttpProvider(String baseUrl, String apiKey, String defaultModel) {
        this(baseUrl, apiKey, defaultModel, Duration.ofSeconds(60), HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10)).build());
    }

    DeepSeekHttpProvider(String baseUrl, String apiKey, String defaultModel, Duration requestTimeout, HttpClient client) {
        String base = Objects.toString(baseUrl, "https://api.deepseek.com").replaceAll("/+$", "");
        this.endpoint = base.endsWith("/v1") ? base + "/chat/completions" : base + "/v1/chat/completions";
        this.apiKey = Objects.toString(apiKey, "");
        this.defaultModel = Objects.toString(defaultModel, "deepseek-v4-flash");
        this.requestTimeout = requestTimeout == null ? Duration.ofSeconds(60) : requestTimeout;
        this.client = client;
    }

    public boolean isAvailable() { return !apiKey.isBlank(); }

    public ChatResponse chat(ChatRequest request) {
        HttpResponse<String> response = send(request, false);
        Map<String, Object> body = parseBody(response);
        Map<String, Object> choice = firstMap(body.get("choices"));
        Map<String, Object> message = map(choice.get("message"));
        String content = string(message.get("content"));
        String reasoning = string(message.get("reasoning_content"));
        List<ChatResponse.ToolCall> calls = parseToolCalls(message.get("tool_calls"));
        Map<String, Object> usage = map(body.get("usage"));
        int input = number(usage.get("prompt_tokens"));
        int output = number(usage.get("completion_tokens"));
        int total = number(usage.get("total_tokens"));
        boolean estimated = usage.isEmpty();
        if (estimated) {
            input = estimateInput(request);
            output = Math.max(1, content.length() / 4);
            total = input + output;
        }
        String finish = string(choice.get("finish_reason"));
        return ChatResponse.builder().success(true).eventType("COMPLETED").content(content)
                .reasoningContent(reasoning).platform("DEEPSEEK").model(string(body.get("model"), request.getModel()))
                .toolCalls(calls).promptTokens(input).completionTokens(output).totalTokens(total)
                .estimatedUsage(estimated).providerRequestId(string(body.get("id"), response.headers().firstValue("X-Request-ID").orElse(null)))
                .finishReason(finish).build();
    }

    public Flux<ChatResponse> stream(ChatRequest request) {
        return Flux.<ChatResponse>create(sink -> {
            AtomicReference<Thread> worker = new AtomicReference<>(Thread.currentThread());
            sink.onCancel(() -> { Thread thread = worker.get(); if (thread != null) thread.interrupt(); });
            try {
                HttpRequest httpRequest = buildRequest(request, true);
                HttpResponse<java.io.InputStream> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() / 100 != 2) throw new DeepSeekProviderException("DeepSeek HTTP " + response.statusCode() + (response.headers().firstValue("Retry-After").map(v -> " retry-after=" + v).orElse("")));
                Map<Integer, String> toolIds = new LinkedHashMap<>();
                Map<Integer, String> toolNames = new LinkedHashMap<>();
                Map<Integer, StringBuilder> toolArgs = new LinkedHashMap<>();
                boolean emittedBlock = false;
                boolean terminal = false;
                boolean usageEmitted = false;
                int prompt = 0, output = 0, total = 0;
                boolean estimated = true;
                String model = request.getModel();
                String requestId = response.headers().firstValue("X-Request-ID").orElse(null);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) continue;
                    String data = line.substring(5).trim();
                    if (data.isBlank()) continue;
                    if ("[DONE]".equals(data)) break;
                    Map<String, Object> body;
                    try { body = JSONUtils.parseObject(data, Map.class); }
                    catch (RuntimeException ex) { throw new DeepSeekProviderException("malformed SSE frame", ex); }
                    model = string(body.get("model"), model);
                    requestId = string(body.get("id"), requestId);
                    Map<String, Object> usage = map(body.get("usage"));
                    if (!usage.isEmpty()) {
                        prompt = number(usage.get("prompt_tokens")); output = number(usage.get("completion_tokens")); total = number(usage.get("total_tokens")); estimated = false;
                        // DeepSeek may send usage in a frame with no choices
                        // (especially when stream_options.include_usage is
                        // enabled).  Surface it immediately instead of
                        // waiting for a finish frame that may not repeat it.
                        sink.next(ChatResponse.builder().success(true).eventType("USAGE")
                                .platform("DEEPSEEK").model(model)
                                .promptTokens(prompt).completionTokens(output).totalTokens(total)
                                .estimatedUsage(false).providerRequestId(requestId).build());
                        usageEmitted = true;
                    }
                    Map<String, Object> choice = firstMap(body.get("choices"));
                    Map<String, Object> delta = map(choice.get("delta"));
                    if (!delta.isEmpty()) {
                        if (!emittedBlock) { sink.next(ChatResponse.builder().success(true).eventType("BLOCK_STARTED").platform("DEEPSEEK").model(model).build()); emittedBlock = true; }
                        String reasoning = string(delta.get("reasoning_content"));
                        if (!reasoning.isBlank()) sink.next(ChatResponse.builder().success(true).eventType("REASONING_DELTA").reasoningContent(reasoning).platform("DEEPSEEK").model(model).providerRequestId(requestId).build());
                        String text = string(delta.get("content"));
                        if (!text.isBlank()) sink.next(ChatResponse.builder().success(true).eventType("DELTA").content(text).platform("DEEPSEEK").model(model).providerRequestId(requestId).build());
                        List<Map<String, Object>> deltas = maps(delta.get("tool_calls"));
                        for (Map<String, Object> tool : deltas) {
                            int index = number(tool.get("index"));
                            String id = string(tool.get("id"));
                            Map<String, Object> fn = map(tool.get("function"));
                            String name = string(fn.get("name"));
                            String args = string(fn.get("arguments"));
                            if (!id.isBlank()) toolIds.put(index, id);
                            if (!name.isBlank()) toolNames.put(index, name);
                            if (!args.isBlank()) toolArgs.computeIfAbsent(index, ignoredIndex -> new StringBuilder()).append(args);
                            sink.next(ChatResponse.builder().success(true).eventType("TOOL_CALL").toolCallId(toolIds.get(index)).toolName(toolNames.get(index)).toolArguments(args).platform("DEEPSEEK").model(model).providerRequestId(requestId).build());
                        }
                    }
                    String finish = string(choice.get("finish_reason"));
                    if (!finish.isBlank() && !"null".equals(finish)) {
                        if (!usageEmitted) {
                            sink.next(ChatResponse.builder().success(true).eventType("USAGE").platform("DEEPSEEK").model(model).promptTokens(prompt == 0 ? estimateInput(request) : prompt).completionTokens(output).totalTokens(total == 0 ? prompt + output : total).estimatedUsage(estimated).providerRequestId(requestId).finishReason(finish).build());
                        }
                        sink.next(ChatResponse.builder().success(true).eventType("BLOCK_COMPLETED").platform("DEEPSEEK").model(model).providerRequestId(requestId).finishReason(finish).build());
                        sink.next(ChatResponse.builder().success(true).eventType("COMPLETED").platform("DEEPSEEK").model(model).providerRequestId(requestId).finishReason(finish).build());
                        terminal = true;
                    }
                }}
                if (!terminal) throw new DeepSeekProviderException("DeepSeek stream ended before a terminal event");
                sink.complete();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                sink.next(ChatResponse.builder().success(false).eventType("CANCELLED").platform("DEEPSEEK").model(request.getModel()).build());
                sink.complete();
            } catch (Throwable ex) {
                sink.next(ChatResponse.builder().success(false).eventType("FAILED").errorMessage("deepseek provider call failed").platform("DEEPSEEK").model(request.getModel()).build());
                sink.error(ex);
            }
        }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    private HttpResponse<String> send(ChatRequest request, boolean stream) {
        try {
            HttpResponse<String> response = client.send(buildRequest(request, stream), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) throw providerError(response.statusCode(), response.body(), response.headers().firstValue("Retry-After").orElse(null));
            return response;
        } catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new DeepSeekProviderException("request interrupted", ex); }
        catch (java.io.IOException ex) { throw new DeepSeekProviderException("request failed", ex); }
    }

    private HttpRequest buildRequest(ChatRequest request, boolean stream) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", request.getModel() == null || request.getModel().isBlank() ? defaultModel : request.getModel());
        body.put("messages", request.getMessages().stream().map(this::message).toList());
        body.put("stream", stream);
        if (stream) body.put("stream_options", Map.of("include_usage", true));
        if (request.getTemperature() != null) body.put("temperature", request.getTemperature());
        if (request.getMaxOutputTokens() != null) body.put("max_tokens", request.getMaxOutputTokens());
        if (request.getReasoningEffort() != null && !request.getReasoningEffort().isBlank()) body.put("reasoning_effort", request.getReasoningEffort());
        if (request.getTools() != null && !request.getTools().isEmpty()) body.put("tools", request.getTools().stream().map(tool -> Map.of("type", "function", "function", Map.of("name", tool.name(), "description", Objects.toString(tool.description(), ""), "parameters", JSONUtils.parseObject(Objects.toString(tool.parametersJson(), "{}"), Map.class)))).toList());
        return HttpRequest.newBuilder(URI.create(endpoint)).timeout(requestTimeout).header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(JSONUtils.toJsonString(body))).build();
    }

    private Map<String, Object> message(ChatMessage message) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("role", message.role().toLowerCase());
        List<Object> parts = new ArrayList<>();
        List<Object> toolCalls = new ArrayList<>();
        StringBuilder reasoning = new StringBuilder();
        boolean toolResult = false;
        for (ChatMessage.ContentPart part : message.content()) {
            if ("tool_call".equalsIgnoreCase(part.type())) {
                toolCalls.add(Map.of("id", Objects.toString(part.toolCallId(), "tool"), "type", "function", "function", Map.of("name", Objects.toString(part.toolName(), "tool"), "arguments", Objects.toString(part.toolArguments(), "{}"))));
            } else if ("tool_result".equalsIgnoreCase(part.type())) {
                value.put("role", "tool"); value.put("tool_call_id", Objects.toString(part.toolCallId(), "tool")); parts.add(Objects.toString(part.text(), "")); toolResult = true;
            } else if ("reasoning".equalsIgnoreCase(part.type())) {
                if (part.text() != null) reasoning.append(part.text());
            } else if ("text".equalsIgnoreCase(part.type())) {
                parts.add(Objects.toString(part.text(), ""));
            } else if ("image".equalsIgnoreCase(part.type())) {
                if (part.uri() == null || part.uri().isBlank()) throw new IllegalArgumentException("image content requires a uri");
                parts.add(Map.of("type", "image_url", "image_url", Map.of("url", part.uri())));
            } else if ("audio".equalsIgnoreCase(part.type()) || "file".equalsIgnoreCase(part.type())) {
                // DeepSeek's current chat contract does not accept audio/file
                // parts. Fail closed instead of silently dropping user input.
                throw new IllegalArgumentException("DeepSeek does not support " + part.type() + " content parts");
            } else {
                throw new IllegalArgumentException("unsupported DeepSeek content part: " + part.type());
            }
        }
        value.put("content", parts.isEmpty() ? "" : parts.size() == 1 && parts.get(0) instanceof String ? parts.get(0) : parts);
        if (!toolCalls.isEmpty()) value.put("tool_calls", toolCalls);
        if (!reasoning.isEmpty()) value.put("reasoning_content", reasoning.toString());
        if (toolResult && parts.isEmpty()) value.put("content", "");
        return value;
    }

    private Map<String, Object> parseBody(HttpResponse<String> response) {
        try {
            Map<String, Object> body = JSONUtils.parseObject(response.body(), Map.class);
            if (body == null || body.isEmpty()) throw new IllegalArgumentException("empty response body");
            return body;
        } catch (RuntimeException ex) { throw new DeepSeekProviderException("malformed response", ex); }
    }
    private static Map<String, Object> map(Object value) { return value instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of(); }
    private static List<Map<String, Object>> maps(Object value) { if (!(value instanceof List<?> list)) return List.of(); return list.stream().filter(v -> v instanceof Map<?, ?>).map(v -> (Map<String, Object>) v).toList(); }
    private static Map<String, Object> firstMap(Object value) { return maps(value).stream().findFirst().orElse(Map.of()); }
    private static String string(Object value) { return value == null ? "" : String.valueOf(value); }
    private static String string(Object value, String fallback) { String s = string(value); return s.isBlank() ? Objects.toString(fallback, "") : s; }
    private static int number(Object value) { if (value instanceof Number n) return n.intValue(); try { return Integer.parseInt(string(value)); } catch (Exception ignored) { return 0; } }
    private static List<ChatResponse.ToolCall> parseToolCalls(Object value) { return maps(value).stream().map(call -> { Map<String, Object> fn = map(call.get("function")); return new ChatResponse.ToolCall(string(call.get("id")), string(fn.get("name")), string(fn.get("arguments"))); }).toList(); }
    private static int estimateInput(ChatRequest request) { return Math.max(1, request.getMessages().stream().mapToInt(m -> m.content().stream().map(ChatMessage.ContentPart::text).filter(Objects::nonNull).mapToInt(String::length).sum() / 4).sum()); }
    private static DeepSeekProviderException providerError(int status, String body, String retryAfter) { return new DeepSeekProviderException("DeepSeek HTTP " + status + (retryAfter == null ? "" : " retry-after=" + retryAfter) + ": " + Objects.toString(body, "")); }

    public static final class DeepSeekProviderException extends RuntimeException { public DeepSeekProviderException(String message) { super(message); } public DeepSeekProviderException(String message, Throwable cause) { super(message, cause); } }
}
