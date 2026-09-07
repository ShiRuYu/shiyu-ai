package com.shiyu.ai.model.chat;

import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.model.adapter.impl.DeepSeekHttpProvider;
import com.shiyu.ai.model.chat.impl.ChatEngineImpl;
import com.shiyu.ai.model.gateway.ModelRouter;
import com.shiyu.ai.model.gateway.ModelRoutePolicy;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.output.FinishReason;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatEngineImplTest {
    @Test
    void chatHandlesNullAiMessageAndPreservesProviderUsageMetadata() {
        ModelManager manager = mock(ModelManager.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ChatModel model = mock(ChatModel.class);
        when(manager.getChatModel("OPENAI", "gpt")).thenReturn(model);
        AiMessage aiMessage = mock(AiMessage.class);
        when(aiMessage.text()).thenReturn(null);
        when(aiMessage.toolExecutionRequests()).thenReturn(List.of());
        when(model.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class))).thenReturn(
                dev.langchain4j.model.chat.response.ChatResponse.builder()
                        .modelName("provider-gpt").aiMessage(aiMessage)
                        .tokenUsage(new dev.langchain4j.model.output.TokenUsage(4, 6, 10))
                        .build());

        ChatResponse response = new ChatEngineImpl(manager, publisher).chat(request("OPENAI", "gpt"));

        assertTrue(response.isSuccess());
        assertEquals("provider-gpt", response.getModel());
        assertEquals("", response.getContent());
        assertEquals(4, response.getPromptTokens());
        assertEquals(6, response.getCompletionTokens());
        assertEquals(10, response.getTotalTokens());
        assertTrue(response.getToolCalls().isEmpty());
    }

    @Test
    void streamEmitsProviderMetadataAndFailedEventWhenCallbackErrors() {
        ModelManager manager = mock(ModelManager.class);
        StreamingChatModel model = mock(StreamingChatModel.class);
        when(manager.getStreamingChatModel("OPENAI", "gpt")).thenReturn(model);
        doAnswer(invocation -> {
            var handler = invocation.getArgument(1, dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class);
            handler.onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse.builder()
                    .modelName("provider-gpt").id("request-1").finishReason(FinishReason.STOP)
                    .aiMessage(AiMessage.from("done"))
                    .tokenUsage(new dev.langchain4j.model.output.TokenUsage(2, 3, 5)).build());
            return null;
        }).when(model).chat(any(dev.langchain4j.model.chat.request.ChatRequest.class),
                any(dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class));

        List<ChatResponse> events = new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class))
                .stream(request("OPENAI", "gpt")).collectList().block();
        assertNotNull(events);
        assertEquals("USAGE", events.stream().filter(e -> "USAGE".equals(e.getEventType())).findFirst().orElseThrow().getEventType());
        ChatResponse usage = events.stream().filter(e -> "USAGE".equals(e.getEventType())).findFirst().orElseThrow();
        assertEquals(2, usage.getPromptTokens());
        assertEquals(3, usage.getCompletionTokens());
        assertEquals("STOP", usage.getFinishReason());
        assertTrue(events.stream().anyMatch(e -> "DELTA".equals(e.getEventType()) && "done".equals(e.getContent())));

        StreamingChatModel failing = mock(StreamingChatModel.class);
        when(manager.getStreamingChatModel("OPENAI", "failed")).thenReturn(failing);
        doAnswer(invocation -> {
            var handler = invocation.getArgument(1, dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class);
            handler.onError(new IllegalStateException("provider failed"));
            return null;
        }).when(failing).chat(any(dev.langchain4j.model.chat.request.ChatRequest.class),
                any(dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class));
        List<ChatResponse> failedEvents = new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class))
                .stream(request("OPENAI", "failed")).onErrorResume(error -> Flux.empty()).collectList().block();
        assertNotNull(failedEvents);
        assertTrue(failedEvents.stream().anyMatch(e -> "FAILED".equals(e.getEventType())));
    }

    @Test
    void chatMapsProviderResponseAndPublishesUsage() {
        ModelManager manager = mock(ModelManager.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ChatModel model = mock(ChatModel.class);
        when(manager.getChatModel("OPENAI", "gpt")).thenReturn(model);
        when(model.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class))).thenReturn(dev.langchain4j.model.chat.response.ChatResponse.builder()
                .aiMessage(AiMessage.from("hello")).modelName("gpt").tokenUsage(new dev.langchain4j.model.output.TokenUsage(3, 2, 5)).build());

        ChatResponse response = new ChatEngineImpl(manager, publisher).chat(request("OPENAI", "gpt"));

        assertTrue(response.isSuccess());
        assertEquals("hello", response.getContent());
        assertEquals(3, response.getPromptTokens());
        assertEquals(2, response.getCompletionTokens());
        verify(publisher).publishEvent(any(Object.class));
    }

    @Test
    void rejectsUnattributedUsageWhenTenantIsMissing() {
        ModelManager manager = mock(ModelManager.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ChatModel model = mock(ChatModel.class);
        when(manager.getChatModel("OPENAI", "gpt")).thenReturn(model);
        when(model.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class))).thenReturn(
                dev.langchain4j.model.chat.response.ChatResponse.builder()
                        .aiMessage(AiMessage.from("hello")).modelName("gpt").build());

        ChatRequest request = ChatRequest.builder().platform("OPENAI").model("gpt")
                .tenantId(0).userId(0)
                .messages(List.of(ChatMessage.text("user", "hello"))).build();
        ChatResponse response = new ChatEngineImpl(manager, publisher).chat(request);
        assertFalse(response.isSuccess());
        assertEquals("tenantId is required", response.getErrorMessage());
        verifyNoInteractions(publisher, model);
    }

    @Test
    void rejectsNullRequestWhenBuildingUsageAttribution() throws Exception {
        ChatEngineImpl engine = new ChatEngineImpl(mock(ModelManager.class), mock(ApplicationEventPublisher.class));
        Method tenant = ChatEngineImpl.class.getDeclaredMethod("eventTenant", ChatRequest.class);
        Method user = ChatEngineImpl.class.getDeclaredMethod("eventUser", ChatRequest.class);
        tenant.setAccessible(true);
        user.setAccessible(true);
        assertNull(tenant.invoke(engine, new Object[]{null}));
        assertNull(user.invoke(engine, new Object[]{null}));
    }

    @Test
    void invalidAndUnavailableRequestsReturnStableFailure() {
        ModelManager manager = mock(ModelManager.class);
        ChatEngineImpl engine = new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class));

        ChatResponse invalid = engine.chat(ChatRequest.builder().platform("OPENAI").messages(List.of()).build());
        assertFalse(invalid.isSuccess());
        assertEquals("messages cannot be empty", invalid.getErrorMessage());

        ChatResponse unavailable = engine.chat(request("OPENAI", "missing"));
        assertFalse(unavailable.isSuccess());
        assertEquals("model call failed", unavailable.getErrorMessage());
    }

    @Test
    void streamEmitsLifecycleAndUsageEvents() {
        ModelManager manager = mock(ModelManager.class);
        StreamingChatModel model = mock(StreamingChatModel.class);
        when(manager.getStreamingChatModel("OPENAI", "gpt")).thenReturn(model);
        doAnswer(invocation -> {
            dev.langchain4j.model.chat.response.StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse("hel");
            handler.onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse.builder()
                    .aiMessage(AiMessage.from("hello")).modelName("gpt")
                    .tokenUsage(new dev.langchain4j.model.output.TokenUsage(3, 2, 5)).build());
            return null;
        }).when(model).chat(any(dev.langchain4j.model.chat.request.ChatRequest.class),
                any(dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class));

        List<ChatResponse> events = new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class))
                .stream(request("OPENAI", "gpt")).collectList().block();

        assertNotNull(events);
        assertEquals("BLOCK_STARTED", events.get(0).getEventType());
        assertTrue(events.stream().anyMatch(event -> "DELTA".equals(event.getEventType()) && "hel".equals(event.getContent())));
        assertTrue(events.stream().anyMatch(event -> "USAGE".equals(event.getEventType())));
        assertEquals("COMPLETED", events.get(events.size() - 1).getEventType());
    }

    @Test
    void streamPropagatesProviderErrorAfterFailureEvent() {
        ModelManager manager = mock(ModelManager.class);
        StreamingChatModel model = mock(StreamingChatModel.class);
        when(manager.getStreamingChatModel("OPENAI", "gpt")).thenReturn(model);
        doAnswer(invocation -> {
            dev.langchain4j.model.chat.response.StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onError(new IllegalStateException("down"));
            return null;
        }).when(model).chat(any(dev.langchain4j.model.chat.request.ChatRequest.class),
                any(dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class));

        Flux<ChatResponse> stream = new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class))
                .stream(request("OPENAI", "gpt"));
        assertThrows(Exception.class, () -> stream.collectList().block());
    }

    @Test
    void nativeMessageValidationRejectsUnsupportedFileContent() {
        ModelManager manager = mock(ModelManager.class);
        ChatModel model = mock(ChatModel.class);
        when(manager.getChatModel("OPENAI", "gpt")).thenReturn(model);
        ChatMessage file = new ChatMessage("user", List.of(new ChatMessage.ContentPart("file", null, "file://x", null)));
        ChatRequest request = ChatRequest.builder().platform("OPENAI").model("gpt").messages(List.of(file)).build();

        ChatResponse response = new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class)).chat(request);
        assertFalse(response.isSuccess());
        verifyNoInteractions(model);
    }

    @Test
    void chatEstimatesUsageAndMapsToolCallsWhenProviderOmitsUsage() {
        ModelManager manager = mock(ModelManager.class);
        ChatModel model = mock(ChatModel.class);
        when(manager.getChatModel("OPENAI", "gpt")).thenReturn(model);
        dev.langchain4j.agent.tool.ToolExecutionRequest call = dev.langchain4j.agent.tool.ToolExecutionRequest.builder()
                .id("call-1").name("search").arguments("{\"q\":\"hi\"}").build();
        when(model.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class))).thenReturn(
                dev.langchain4j.model.chat.response.ChatResponse.builder()
                        .aiMessage(AiMessage.from("answer", List.of(call))).build());

        ChatResponse response = new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class))
                .chat(request("OPENAI", "gpt"));

        assertTrue(response.isSuccess());
        assertTrue(response.isEstimatedUsage());
        assertEquals(1, response.getToolCalls().size());
        assertEquals("search", response.getToolCalls().get(0).name());
    }

    @Test
    void translatesSystemAssistantToolImageAndAudioMessages() {
        ModelManager manager = mock(ModelManager.class);
        ChatModel model = mock(ChatModel.class);
        when(manager.getChatModel("OPENAI", "gpt")).thenReturn(model);
        when(model.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class))).thenReturn(
                dev.langchain4j.model.chat.response.ChatResponse.builder().aiMessage(AiMessage.from("ok")).build());
        ChatMessage assistant = new ChatMessage("assistant", List.of(
                new ChatMessage.ContentPart("tool_call", "", null, null, "id", "search", "{}", 0)));
        ChatMessage tool = new ChatMessage("tool", List.of(
                new ChatMessage.ContentPart("tool_result", "done", null, null, "id", "search", null, 0)));
        List<ChatMessage> messages = List.of(ChatMessage.text("system", "rules"), assistant, tool,
                new ChatMessage("user", List.of(
                        new ChatMessage.ContentPart("image", null, "https://img", "image/png"),
                        new ChatMessage.ContentPart("audio", null, "https://audio", "audio/wav"))));
        ChatRequest request = ChatRequest.builder().platform("OPENAI").model("gpt").tenantId(7).userId(8)
                .messages(messages).build();

        assertTrue(new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class)).chat(request).isSuccess());
        verify(model).chat(any(dev.langchain4j.model.chat.request.ChatRequest.class));
    }

    @Test
    void translatesEmptyAssistantAndToolMessagesAndTextFallbackParts() {
        ModelManager manager = mock(ModelManager.class);
        ChatModel model = mock(ChatModel.class);
        when(manager.getChatModel("OPENAI", "gpt")).thenReturn(model);
        when(model.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class))).thenReturn(
                dev.langchain4j.model.chat.response.ChatResponse.builder().aiMessage(AiMessage.from("ok")).build());
        List<ChatMessage> messages = List.of(
                new ChatMessage("assistant", List.of(new ChatMessage.ContentPart("text", "assistant", null, null))),
                new ChatMessage("tool", List.of()),
                new ChatMessage("user", List.of(
                        new ChatMessage.ContentPart("image", null, null, "image/png"),
                        new ChatMessage.ContentPart("audio", null, null, "audio/wav"),
                        new ChatMessage.ContentPart("reasoning", "internal", null, null),
                        new ChatMessage.ContentPart("text", "visible", null, null))));
        ChatRequest request = ChatRequest.builder().platform("OPENAI").model("gpt").tenantId(1).messages(messages).build();
        assertTrue(new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class)).chat(request).isSuccess());
    }

    @Test
    void deepSeekProviderIsUsedForStructuredChat() {
        ModelManager manager = mock(ModelManager.class);
        DeepSeekHttpProvider provider = mock(DeepSeekHttpProvider.class);
        when(manager.getDeepSeekProvider()).thenReturn(provider);
        when(provider.isAvailable()).thenReturn(true);
        when(provider.chat(any())).thenReturn(ChatResponse.builder().success(true).content("deep").model("deepseek").build());

        ChatRequest request = request("DEEPSEEK", "deepseek-chat");
        ChatResponse response = new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class)).chat(request);

        assertTrue(response.isSuccess());
        assertEquals("deep", response.getContent());
        verify(provider).chat(request);
    }

    @Test
    void routesChatAndStreamThroughTenantScopedPolicy() {
        ModelManager manager = mock(ModelManager.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ModelRouter router = new ModelRouter();
        router.savePolicy(new ModelRoutePolicy("route-1", 7L, "Primary", List.of("OPENAI:gpt-4o"), 1000, true, 1000));
        ChatModel chat = mock(ChatModel.class);
        when(manager.getChatModel("OPENAI", "gpt-4o")).thenReturn(chat);
        when(chat.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class))).thenReturn(
                dev.langchain4j.model.chat.response.ChatResponse.builder().aiMessage(AiMessage.from("routed")).build());
        ChatRequest request = ChatRequest.builder().modelRouteId("route-1").tenantId(7).platform("OPENAI")
                .model("ignored").messages(List.of(ChatMessage.text("user", "hello"))).build();
        ChatEngineImpl engine = new ChatEngineImpl(manager, publisher, router);
        assertTrue(engine.chat(request).isSuccess());
        assertEquals("routed", engine.chat(request).getContent());

        StreamingChatModel streaming = mock(StreamingChatModel.class);
        when(manager.getStreamingChatModel("OPENAI", "gpt-4o")).thenReturn(streaming);
        doAnswer(invocation -> {
            dev.langchain4j.model.chat.response.StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse("route");
            handler.onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse.builder()
                    .aiMessage(AiMessage.from("route")).build());
            return null;
        }).when(streaming).chat(any(dev.langchain4j.model.chat.request.ChatRequest.class),
                any(dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class));
        List<ChatResponse> events = engine.stream(request).collectList().block();
        assertNotNull(events);
        assertTrue(events.stream().anyMatch(event -> "DELTA".equals(event.getEventType())));
    }

    @Test
    void rejectsRouteWithoutHealthyCandidates() {
        ModelManager manager = mock(ModelManager.class);
        ModelRouter router = new ModelRouter();
        router.markFailure("default", "configured", "down");
        router.markFailure("default", "configured", "down");
        router.markFailure("default", "configured", "down");
        router.savePolicy(new ModelRoutePolicy("route-2", 7L, "Missing", List.of("configured"), 1000, false, 1000));
        ChatRequest request = ChatRequest.builder().modelRouteId("route-2").tenantId(7).platform("OPENAI")
                .model("ignored").messages(List.of(ChatMessage.text("user", "hello"))).build();
        ChatEngineImpl engine = new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class), router);
        assertFalse(engine.chat(request).isSuccess());
        assertThrows(Exception.class, () -> engine.stream(request).collectList().block());
    }

    @Test
    void streamCoversThinkingToolCallsEmptyDeltasAndFinalFlush() {
        ModelManager manager = mock(ModelManager.class);
        StreamingChatModel model = mock(StreamingChatModel.class);
        when(manager.getStreamingChatModel("OPENAI", "gpt")).thenReturn(model);
        doAnswer(invocation -> {
            dev.langchain4j.model.chat.response.StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse("");
            handler.onPartialResponse(null);
            handler.onPartialThinking(null);
            PartialThinking emptyThinking = mock(PartialThinking.class);
            when(emptyThinking.text()).thenReturn("");
            handler.onPartialThinking(emptyThinking);
            handler.onPartialThinking(new PartialThinking("reason"));
            handler.onPartialToolCall(PartialToolCall.builder().index(0).id("call").name("search").partialArguments("{}").build());
            handler.onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse.builder()
                    .aiMessage(AiMessage.from("final")).modelName(null).tokenUsage(null).build());
            return null;
        }).when(model).chat(any(dev.langchain4j.model.chat.request.ChatRequest.class),
                any(dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class));

        List<ChatResponse> events = new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class))
                .stream(request("OPENAI", "gpt")).collectList().block();
        assertNotNull(events);
        assertTrue(events.stream().anyMatch(e -> "REASONING_DELTA".equals(e.getEventType())));
        assertTrue(events.stream().anyMatch(e -> "TOOL_CALL".equals(e.getEventType())));
        assertTrue(events.stream().anyMatch(e -> "DELTA".equals(e.getEventType()) && "final".equals(e.getContent())));
        assertEquals("COMPLETED", events.getLast().getEventType());
    }

    @Test
    void validatesMalformedMessagesAndHandlesDeepSeekAndUnavailableStreaming() {
        ChatEngineImpl engine = new ChatEngineImpl(mock(ModelManager.class), mock(ApplicationEventPublisher.class));
        assertEquals("platform is required", engine.chat(null).getErrorMessage());
        assertEquals("platform is required", engine.chat(ChatRequest.builder().messages(List.of()).build()).getErrorMessage());
        ChatMessage malformed = mock(ChatMessage.class);
        when(malformed.role()).thenReturn(null);
        when(malformed.content()).thenReturn(List.of());
        assertEquals("message role is required", engine.chat(ChatRequest.builder().platform("OPENAI").messages(List.of(malformed)).build()).getErrorMessage());
        ChatMessage badPart = new ChatMessage("user", List.of(new ChatMessage.ContentPart("unknown", "x", null, null)));
        assertEquals("unsupported content part type: unknown", engine.chat(ChatRequest.builder().platform("OPENAI").messages(List.of(badPart)).build()).getErrorMessage());

        ModelManager deepManager = mock(ModelManager.class);
        com.shiyu.ai.model.adapter.impl.DeepSeekHttpProvider deepSeek = mock(com.shiyu.ai.model.adapter.impl.DeepSeekHttpProvider.class);
        when(deepManager.getDeepSeekProvider()).thenReturn(deepSeek);
        when(deepSeek.isAvailable()).thenReturn(true);
        when(deepSeek.stream(any())).thenReturn(Flux.just(ChatResponse.builder().eventType("USAGE").build()));
        List<ChatResponse> deepEvents = new ChatEngineImpl(deepManager, mock(ApplicationEventPublisher.class))
                .stream(request("DEEPSEEK", "deepseek-chat")).collectList().block();
        assertEquals(1, deepEvents.size());

        ModelManager unavailable = mock(ModelManager.class);
        when(unavailable.getStreamingChatModel("OPENAI", "missing")).thenReturn(null);
        assertThrows(Exception.class, () -> new ChatEngineImpl(unavailable, mock(ApplicationEventPublisher.class))
                .stream(request("OPENAI", "missing")).collectList().block());
    }

    @Test
    void rejectsUnavailableDeepSeekAndHandlesNullProviderResponse() {
        ModelManager deepManager = mock(ModelManager.class);
        DeepSeekHttpProvider provider = mock(DeepSeekHttpProvider.class);
        when(deepManager.getDeepSeekProvider()).thenReturn(provider);
        when(provider.isAvailable()).thenReturn(false);
        ChatEngineImpl engine = new ChatEngineImpl(deepManager, mock(ApplicationEventPublisher.class));
        assertFalse(engine.chat(request("DEEPSEEK", "deepseek-chat")).isSuccess());
        assertThrows(Exception.class, () -> engine.stream(request("DEEPSEEK", "deepseek-chat")).collectList().block());

        ModelManager manager = mock(ModelManager.class);
        ChatModel model = mock(ChatModel.class);
        when(manager.getChatModel("OPENAI", "gpt")).thenReturn(model);
        when(model.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class))).thenReturn(null);
        assertFalse(new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class))
                .chat(request("OPENAI", "gpt")).isSuccess());
    }

    @Test
    void serializesToolDefinitionsAndRejectsMalformedToolDefinitions() {
        ModelManager manager = mock(ModelManager.class);
        ChatModel model = mock(ChatModel.class);
        when(manager.getChatModel("OPENAI", "gpt")).thenReturn(model);
        when(model.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class))).thenReturn(
                dev.langchain4j.model.chat.response.ChatResponse.builder().aiMessage(AiMessage.from("ok")).build());
        ChatRequest request = ChatRequest.builder().platform("OPENAI").model("gpt").tenantId(7).userId(8)
                .temperature(0.2).maxOutputTokens(32).messages(List.of(ChatMessage.text("user", "hello")))
                .tools(List.of(new ChatRequest.ToolDefinition("search", "search docs", "{\"type\":\"object\",\"properties\":{}}"))).build();
        assertTrue(new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class)).chat(request).isSuccess());
        ChatRequest malformed = ChatRequest.builder().platform("OPENAI").model("gpt")
                .messages(List.of(ChatMessage.text("user", "hello")))
                .tools(List.of(new ChatRequest.ToolDefinition("", null, "{}"))).build();
        assertFalse(new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class)).chat(malformed).isSuccess());
    }

    @Test
    void routesMultimodalToolRequestThroughCapabilitySelection() {
        ModelManager manager = mock(ModelManager.class);
        ModelRouter router = new ModelRouter();
        router.savePolicy(new ModelRoutePolicy("route-mm", 7L, "MM", List.of("OPENAI:gpt-4o"), 1000, true, 1000));
        ChatModel model = mock(ChatModel.class);
        when(manager.getChatModel("OPENAI", "gpt-4o")).thenReturn(model);
        when(model.chat(any(dev.langchain4j.model.chat.request.ChatRequest.class))).thenReturn(
                dev.langchain4j.model.chat.response.ChatResponse.builder().aiMessage(AiMessage.from("ok")).build());
        ChatRequest request = ChatRequest.builder().platform("OPENAI").model("ignored").modelRouteId("route-mm").tenantId(7)
                .messages(List.of(new ChatMessage("user", List.of(new ChatMessage.ContentPart("image", null, "https://img", "image/png")))))
                .tools(List.of(new ChatRequest.ToolDefinition("search", null, null))).build();
        assertFalse(new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class), router).chat(request).isSuccess());
    }

    @Test
    void routedChatUsesStructuredDeepSeekCandidate() {
        ModelManager manager = mock(ModelManager.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ModelRouter router = new ModelRouter();
        router.savePolicy(new ModelRoutePolicy("route-deep", 7L, "Deep", List.of("DEEPSEEK:deepseek-v4-flash"), 1000, false, 1000));
        DeepSeekHttpProvider provider = mock(DeepSeekHttpProvider.class);
        when(manager.getDeepSeekProvider()).thenReturn(provider);
        when(provider.isAvailable()).thenReturn(true);
        when(provider.chat(any())).thenReturn(ChatResponse.builder().success(true).content("deep").model("deepseek-v4-flash").build());

        ChatRequest request = ChatRequest.builder().modelRouteId("route-deep").tenantId(7).platform("OPENAI")
                .model("ignored").messages(List.of(ChatMessage.text("user", "hello"))).build();
        ChatResponse result = new ChatEngineImpl(manager, publisher, router).chat(request);

        assertTrue(result.isSuccess());
        assertEquals("deep", result.getContent());
        verify(provider).chat(any());
        verify(publisher).publishEvent(any(Object.class));
    }

    @Test
    void routedStreamFallsBackWhenFirstProviderFailsBeforeVisibleOutput() {
        ModelManager manager = mock(ModelManager.class);
        ModelRouter router = new ModelRouter();
        router.savePolicy(new ModelRoutePolicy("route-stream-fallback", 7L, "Fallback",
                List.of("OPENAI:gpt-4o", "default:configured"), 1000, true, 1000));
        StreamingChatModel first = mock(StreamingChatModel.class);
        StreamingChatModel second = mock(StreamingChatModel.class);
        when(manager.getStreamingChatModel("OPENAI", "gpt-4o")).thenReturn(first);
        when(manager.getStreamingChatModel("default", "configured")).thenReturn(second);
        doAnswer(invocation -> {
            var handler = invocation.getArgument(1, dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class);
            handler.onError(new IllegalStateException("first provider down"));
            return null;
        }).when(first).chat(any(dev.langchain4j.model.chat.request.ChatRequest.class),
                any(dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class));
        doAnswer(invocation -> {
            var handler = invocation.getArgument(1, dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class);
            handler.onPartialResponse("fallback");
            handler.onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse.builder()
                    .aiMessage(AiMessage.from("fallback")).build());
            return null;
        }).when(second).chat(any(dev.langchain4j.model.chat.request.ChatRequest.class),
                any(dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class));

        ChatRequest request = ChatRequest.builder().modelRouteId("route-stream-fallback").tenantId(7)
                .platform("OPENAI").model("ignored").messages(List.of(ChatMessage.text("user", "hello"))).build();
        List<ChatResponse> events = new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class), router)
                .stream(request).collectList().block();

        assertNotNull(events);
        assertTrue(events.stream().anyMatch(event -> "DELTA".equals(event.getEventType()) && "fallback".equals(event.getContent())));
        verify(second).chat(any(dev.langchain4j.model.chat.request.ChatRequest.class),
                any(dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class));
    }

    @Test
    void routedStreamDoesNotFallbackAfterVisibleOutput() {
        ModelManager manager = mock(ModelManager.class);
        ModelRouter router = new ModelRouter();
        router.savePolicy(new ModelRoutePolicy("route-visible", 7L, "Visible",
                List.of("OPENAI:gpt-4o", "default:configured"), 1000, true, 1000));
        StreamingChatModel first = mock(StreamingChatModel.class);
        StreamingChatModel second = mock(StreamingChatModel.class);
        when(manager.getStreamingChatModel("OPENAI", "gpt-4o")).thenReturn(first);
        when(manager.getStreamingChatModel("default", "configured")).thenReturn(second);
        doAnswer(invocation -> {
            var handler = invocation.getArgument(1, dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class);
            handler.onPartialResponse("visible");
            handler.onError(new IllegalStateException("after output"));
            return null;
        }).when(first).chat(any(dev.langchain4j.model.chat.request.ChatRequest.class),
                any(dev.langchain4j.model.chat.response.StreamingChatResponseHandler.class));
        ChatRequest request = ChatRequest.builder().modelRouteId("route-visible").tenantId(7)
                .platform("OPENAI").model("ignored").messages(List.of(ChatMessage.text("user", "hello"))).build();
        assertThrows(Exception.class, () -> new ChatEngineImpl(manager, mock(ApplicationEventPublisher.class), router)
                .stream(request).collectList().block());
        verifyNoInteractions(second);
    }

    private ChatRequest request(String platform, String model) {
        return ChatRequest.builder().platform(platform).model(model).tenantId(7).userId(8)
                .messages(List.of(ChatMessage.text("user", "hello"))).build();
    }
}
