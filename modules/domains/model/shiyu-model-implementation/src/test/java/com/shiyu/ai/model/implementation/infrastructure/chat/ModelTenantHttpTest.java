package com.shiyu.ai.model.implementation.infrastructure.chat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.contract.model.ChatMessage;
import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;
import com.shiyu.ai.model.implementation.domain.model.AiModelBO;
import com.shiyu.ai.model.implementation.domain.model.AiPlatformBO;
import com.shiyu.ai.model.implementation.domain.port.repository.AiModelRepository;
import com.shiyu.ai.model.implementation.domain.port.repository.AiPlatformRepository;
import com.shiyu.ai.model.implementation.infrastructure.chat.impl.ChatEngineImpl;
import com.shiyu.ai.model.implementation.infrastructure.config.PlatformProperties;
import com.shiyu.ai.model.implementation.infrastructure.service.ModelManager;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** 验证实际模型 HTTP 请求使用对应租户的平台地址和凭据。 */
class ModelTenantHttpTest {
    @Test
    void ordinaryAndStreamingCallsReachOnlyTheRequestedTenantsEndpoint() throws Exception {
        AtomicInteger ownerCalls = new AtomicInteger();
        AtomicInteger otherCalls = new AtomicInteger();
        HttpServer owner = endpoint("owner", "owner-key", ownerCalls);
        HttpServer other = endpoint("other", "other-key", otherCalls);
        try {
            AiPlatformRepository platforms = mock(AiPlatformRepository.class);
            AiModelRepository models = mock(AiModelRepository.class);
            AiPlatformBO ownerPlatform = platform(1L, owner, null);
            AiPlatformBO otherPlatform = platform(2L, other, "other-key");
            when(platforms.selectAllEnabled(new TenantId(9L))).thenReturn(List.of(ownerPlatform));
            when(platforms.selectByCode(new TenantId(10L), "OPENAI"))
                    .thenReturn(otherPlatform);
            when(models.selectDefaultByPlatformId(new TenantId(9L), 1L))
                    .thenReturn(model("owner-model"));
            when(models.selectDefaultByPlatformId(new TenantId(10L), 2L))
                    .thenReturn(model("other-model"));
            PlatformProperties properties = new PlatformProperties();
            properties.setTenantId(9L);
            properties.getOpenai().setApiKey("owner-key");
            properties.init();
            ModelManager manager = new ModelManager(platforms, models, properties);
            manager.reloadFromDb();
            ChatEngineImpl engine = new ChatEngineImpl(manager,
                    mock(ApplicationEventPublisher.class));

            ChatResponse ownerReply = engine.chat(request(9L, "owner-model"));
            ChatResponse otherReply = engine.chat(request(10L, "other-model"));
            assertTrue(ownerReply.isSuccess(), ownerReply.getContent());
            assertEquals("owner", ownerReply.getContent());
            assertTrue(otherReply.isSuccess(), otherReply.getContent());
            assertEquals("other", otherReply.getContent());
            assertEquals(1, ownerCalls.get());
            assertEquals(1, otherCalls.get());

            List<ChatResponse> ownerEvents = engine.stream(request(9L, "owner-model"))
                    .collectList().block();
            List<ChatResponse> otherEvents = engine.stream(request(10L, "other-model"))
                    .collectList().block();
            assertNotNull(ownerEvents);
            assertNotNull(otherEvents);
            assertTrue(ownerEvents.stream().anyMatch(event ->
                    "owner".equals(event.getContent())), ownerEvents.toString());
            assertTrue(otherEvents.stream().anyMatch(event ->
                    "other".equals(event.getContent())), otherEvents.toString());
            assertEquals(2, ownerCalls.get());
            assertEquals(2, otherCalls.get());
        } finally {
            owner.stop(0);
            other.stop(0);
        }
    }

    private static HttpServer endpoint(String name, String key, AtomicInteger calls)
            throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            String authorization = exchange.getRequestHeaders().getFirst("Authorization");
            if (!("Bearer " + key).equals(authorization)) {
                exchange.sendResponseHeaders(401, -1);
                exchange.close();
                return;
            }
            calls.incrementAndGet();
            String body = new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
            boolean stream = body.matches("(?s).*\\\"stream\\\"\\s*:\\s*true.*");
            String response = stream
                    ? "data: {\"id\":\"req\",\"model\":\"" + name
                            + "-model\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\""
                            + name + "\"},\"finish_reason\":null}]}\n\n"
                            + "data: [DONE]\n\n"
                    : "{\"id\":\"req\",\"model\":\"" + name
                            + "-model\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\""
                            + name + "\"},\"finish_reason\":\"stop\"}]}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type",
                    stream ? "text/event-stream" : "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return server;
    }

    private static AiPlatformBO platform(long id, HttpServer server, String key) {
        AiPlatformBO platform = new AiPlatformBO();
        platform.setId(id);
        platform.setCode("OPENAI");
        platform.setStatus(1);
        platform.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
        platform.setApiKey(key);
        return platform;
    }

    private static AiModelBO model(String name) {
        AiModelBO model = new AiModelBO();
        model.setModelName(name);
        return model;
    }

    private static ChatRequest request(long tenantId, String model) {
        return ChatRequest.builder().tenantId(tenantId).platform("OPENAI").model(model)
                .messages(List.of(ChatMessage.text("user", "hello"))).build();
    }
}
