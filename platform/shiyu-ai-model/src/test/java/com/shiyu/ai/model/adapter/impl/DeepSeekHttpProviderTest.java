package com.shiyu.ai.model.adapter.impl;

import com.shiyu.ai.model.chat.ChatMessage;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DeepSeekHttpProviderTest {
    private HttpServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
    }

    @AfterEach
    void tearDown() { if (server != null) server.stop(0); }

    @Test
    void mapsStructuredResponseAndUsage() {
        server.createContext("/v1/chat/completions", exchange -> {
            String response = "{\"id\":\"req-1\",\"model\":\"deepseek-v4-flash\",\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"answer\",\"reasoning_content\":\"think\",\"tool_calls\":[]},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":11,\"completion_tokens\":3,\"total_tokens\":14}}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        DeepSeekHttpProvider provider = provider();
        ChatResponse result = provider.chat(request());
        assertTrue(result.isSuccess());
        assertEquals("answer", result.getContent());
        assertEquals("think", result.getReasoningContent());
        assertEquals(11, result.getPromptTokens());
        assertEquals("req-1", result.getProviderRequestId());
    }

    @Test
    void parsesReasoningToolAndUsageSseFrames() {
        server.createContext("/v1/chat/completions", exchange -> {
            String response = "data: {\"id\":\"req-2\",\"model\":\"deepseek-v4-flash\",\"choices\":[{\"delta\":{\"reasoning_content\":\"r\"},\"finish_reason\":null}]}\n\n"
                    + "data: {\"choices\":[{\"delta\":{\"content\":\"a\",\"tool_calls\":[{\"index\":0,\"id\":\"call-1\",\"function\":{\"name\":\"lookup\",\"arguments\":\"{\\\"q\\\":\\\"x\\\"}\"}}]},\"finish_reason\":null}]}\n\n"
                    + "data: {\"choices\":[{\"delta\":{},\"finish_reason\":\"tool_calls\"}],\"usage\":{\"prompt_tokens\":4,\"completion_tokens\":2,\"total_tokens\":6}}\n\n"
                    + "data: [DONE]\n\n";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        List<ChatResponse> events = provider().stream(request()).collectList().block();
        assertNotNull(events);
        assertTrue(events.stream().anyMatch(e -> "REASONING_DELTA".equals(e.getEventType())));
        assertTrue(events.stream().anyMatch(e -> "DELTA".equals(e.getEventType()) && "a".equals(e.getContent())));
        assertTrue(events.stream().anyMatch(e -> "TOOL_CALL".equals(e.getEventType()) && "call-1".equals(e.getToolCallId())));
        assertTrue(events.stream().anyMatch(e -> "USAGE".equals(e.getEventType()) && !e.isEstimatedUsage()));
        assertEquals("COMPLETED", events.get(events.size() - 1).getEventType());
    }

    @Test
    void emitsUsageWhenUsageOnlyFramePrecedesTerminalFrame() {
        server.createContext("/v1/chat/completions", exchange -> {
            String response = "data: {\"id\":\"req-3\",\"choices\":[{\"delta\":{\"content\":\"ok\"},\"finish_reason\":null}]}\n\n"
                    + "data: {\"id\":\"req-3\",\"usage\":{\"prompt_tokens\":8,\"completion_tokens\":2,\"total_tokens\":10},\"choices\":[]}\n\n"
                    + "data: {\"id\":\"req-3\",\"choices\":[{\"delta\":{},\"finish_reason\":\"stop\"}]}\n\n"
                    + "data: [DONE]\n\n";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        List<ChatResponse> events = provider().stream(request()).collectList().block();
        assertNotNull(events);
        assertEquals(1, events.stream().filter(e -> "USAGE".equals(e.getEventType())).count());
        ChatResponse usage = events.stream().filter(e -> "USAGE".equals(e.getEventType())).findFirst().orElseThrow();
        assertEquals(8, usage.getPromptTokens());
        assertFalse(usage.isEstimatedUsage());
    }

    @Test
    void preservesReasoningAsProviderAssistantField() {
        AtomicReference<String> body = new AtomicReference<>();
        server.createContext("/v1/chat/completions", exchange -> {
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String response = "{\"id\":\"req-4\",\"model\":\"deepseek-v4-flash\",\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"ok\"},\"finish_reason\":\"stop\"}]}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        ChatMessage assistant = new ChatMessage("assistant", List.of(
                new ChatMessage.ContentPart("reasoning", "think", null, null),
                new ChatMessage.ContentPart("text", "ok", null, null)));
        ChatRequest request = ChatRequest.builder().platform("DEEPSEEK").model("deepseek-v4-flash")
                .messages(List.of(assistant)).build();
        provider().chat(request);
        assertNotNull(body.get());
        assertTrue(body.get().contains("reasoning_content"));
        assertTrue(body.get().contains("think"));
    }

    private DeepSeekHttpProvider provider() {
        return new DeepSeekHttpProvider("http://127.0.0.1:" + server.getAddress().getPort(), "test-key", "deepseek-v4-flash");
    }

    private ChatRequest request() {
        return ChatRequest.builder().platform("DEEPSEEK").model("deepseek-v4-flash")
                .messages(List.of(ChatMessage.text("user", "hello"))).build();
    }
}
