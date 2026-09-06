package com.shiyu.ai.conversation.web;

import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.conversation.ConversationService;
import com.shiyu.ai.conversation.domain.Conversation;
import com.shiyu.ai.conversation.domain.ConversationMessage;
import com.shiyu.ai.conversation.domain.ConversationStatus;
import com.shiyu.ai.conversation.port.ConversationRepository;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatMessage;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.model.port.ModelRoutingPort;
import com.shiyu.ai.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OpenAiCompatibleControllerTest {
    private final ChatEngine engine = mock(ChatEngine.class);
    private final ModelRoutingPort models = mock(ModelRoutingPort.class);
    private final ConversationService conversations = mock(ConversationService.class);
    private final ConversationRepository repository = mock(ConversationRepository.class);
    private final AiRuntimePort runtime = mock(AiRuntimePort.class);
    private final OpenAiCompatibleController controller = new OpenAiCompatibleController(engine, models, conversations, repository, runtime);
    private final AiRun run = new AiRun("run1", new TenantId(7), new UserId(8), null, null, AiRunSource.API, "openai", null, null,
            null, null, null, "gpt", "hash", AiRunStatus.RUNNING, 0, 0, false, null, Instant.now(), null, null, 0);

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setUserId(8L); context.setCurrentTenantId(7L); context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
        when(models.resolvePlatform("gpt")).thenReturn("OPENAI");
        when(models.defaultPlatform()).thenReturn("OPENAI");
        when(runtime.startRun(any(), any(), anyString(), anyString(), anyString())).thenReturn(run);
    }

    @AfterEach
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void servesModelsAndNonStreamingChatAndResponses() {
        when(models.availableModels()).thenReturn(List.of(new ModelRoutingPort.ModelDescriptor("gpt", "OPENAI")));
        assertEquals("list", controller.models().get("object"));
        ChatResponse response = ChatResponse.builder().success(true).content("hello").promptTokens(3).completionTokens(2).totalTokens(5).build();
        when(engine.chat(any())).thenReturn(response);
        var request = new OpenAiCompatibleController.ChatCompletionRequest("gpt", null,
                List.of(Map.of("role", "user", "content", "hi"), Map.of("role", "assistant", "content", List.of(Map.of("type", "input_text", "text", "ok")))),
                false, false, 0.3, 0, List.of(Map.of("function", Map.of("name", "search", "description", "find", "parameters", Map.of("type", "object")))), null, null);
        Object completion = controller.chatCompletions(request);
        assertTrue(((Map<?, ?>) completion).containsKey("choices"));
        assertEquals(1, ((Map<?, ?>) completion).get("usage") instanceof Map ? 1 : 0);

        var responses = new OpenAiCompatibleController.ResponsesRequest("gpt", "openai", "hello", false, false,
                null, 999999, null, null, null);
        Object output = controller.responses(responses);
        assertEquals("response", ((Map<?, ?>) output).get("object"));

        ChatResponse withToolCall = ChatResponse.builder().success(true).content("tool result")
                .toolCalls(List.of(new ChatResponse.ToolCall("call-1", "search", "{}"))).build();
        when(engine.chat(any())).thenReturn(withToolCall);
        Object toolOutput = controller.responses(responses);
        assertTrue(((Map<?, ?>) toolOutput).toString().contains("tool_calls"));
    }

    @Test
    void streamsAllEventKindsAndFinishesRuntime() {
        List<ChatResponse> chunks = List.of(
                ChatResponse.builder().eventType("REASONING_DELTA").reasoningContent("think").build(),
                ChatResponse.builder().eventType("TOOL_CALL").toolCallId("tc").toolName("search").toolArguments("{}").build(),
                ChatResponse.builder().eventType("USAGE").promptTokens(1).completionTokens(2).estimatedUsage(true).build(),
                ChatResponse.builder().eventType("BLOCK_STARTED").build(),
                ChatResponse.builder().eventType("BLOCK_COMPLETED").build(),
                ChatResponse.builder().eventType("DELTA").content("answer").build(),
                ChatResponse.builder().eventType("COMPLETED").build());
        when(engine.stream(any())).thenReturn(Flux.fromIterable(chunks));
        var request = new OpenAiCompatibleController.ChatCompletionRequest("gpt", null, List.of(Map.of("role", "user", "content", "hi")),
                true, false, null, null, null, null, null);
        Object value = controller.chatCompletions(request);
        assertTrue(value instanceof Flux<?>);
        List<?> events = ((Flux<?>) value).collectList().block();
        assertEquals(8, events.size());
        verify(runtime).recordUsage(eq("run1"), eq(new TenantId(7L)), eq(8L), eq(1L), eq(2L), eq(true), isNull());
        verify(runtime).finish("run1", new TenantId(7), 8, AiRunStatus.COMPLETED, null);
    }

    @Test
    void rejectsFailedModelsAndInvalidTools() {
        when(engine.chat(any())).thenReturn(ChatResponse.builder().success(false).errorMessage("provider down").build());
        var request = new OpenAiCompatibleController.ChatCompletionRequest("gpt", null,
                List.of(Map.of("role", "user", "content", "hi")), false, false, null, null, null, null, null);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.chatCompletions(request));
        var invalid = new OpenAiCompatibleController.ChatCompletionRequest("gpt", null, List.of(), false, false, null, null,
                List.of(Map.of("function", Map.of("description", "missing name"))), null, null);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.chatCompletions(invalid));
    }

    @Test
    void streamsResponsesToolReasoningUsageAndCancellationBranches() {
        when(engine.stream(any())).thenReturn(Flux.fromIterable(List.of(
                ChatResponse.builder().eventType("TOOL_CALL").toolArguments("{}").build(),
                ChatResponse.builder().eventType("REASONING_DELTA").reasoningContent("thinking").build(),
                ChatResponse.builder().eventType("USAGE").promptTokens(1).completionTokens(1).totalTokens(2).build(),
                ChatResponse.builder().eventType("DELTA").content("answer").build(),
                ChatResponse.builder().eventType("COMPLETED").build())));
        var responses = new OpenAiCompatibleController.ResponsesRequest("gpt", null,
                List.of(Map.of("role", "user", "content", List.of(Map.of("type", "input_text", "text", "hi")))),
                true, false, null, null, List.of(Map.of("name", "search")), null, null);
        List<?> completed = ((Flux<?>) controller.responses(responses)).collectList().block();
        assertEquals(6, completed.size());
        verify(runtime).finish("run1", new TenantId(7), 8, AiRunStatus.COMPLETED, null);

        when(engine.stream(any())).thenReturn(Flux.just(ChatResponse.builder().eventType("CANCELLED").build()));
        List<?> cancelled = ((Flux<?>) controller.responses(responses)).collectList().block();
        assertEquals(2, cancelled.size());
        verify(runtime).finish("run1", new TenantId(7), 8, AiRunStatus.CANCELLED, "MODEL_CANCELLED");
    }

    @Test
    void mapsStructuredInputsToolsAndPrivateResponseHelpers() throws Exception {
        List<Map<String, Object>> raw = List.of(
                Map.of("role", "user", "content", "hello"),
                Map.of("role", "assistant", "content", List.of(Map.of("type", "input_text", "text", "answer"), Map.of("type", "input_image", "image_url", Map.of("url", "https://img")))),
                Map.of("role", "assistant", "content", "", "tool_calls", List.of(Map.of("id", "call1", "function", Map.of("name", "search", "arguments", "{}")))),
                Map.of("role", "tool", "content", "result", "tool_call_id", "call1", "name", "search"),
                Map.of("role", "system", "content", 42));
        @SuppressWarnings("unchecked")
        List<ChatMessage> messages = (List<ChatMessage>) invoke("structured", new Class<?>[]{List.class}, raw);
        assertEquals(5, messages.size());
        assertEquals("tool_result", messages.get(3).content().getFirst().type());

        @SuppressWarnings("unchecked")
        List<ChatRequest.ToolDefinition> tools = (List<ChatRequest.ToolDefinition>) invoke("tools", new Class<?>[]{List.class},
                java.util.Arrays.asList(null, Map.of("function", Map.of("name", "search", "description", "find", "parameters", Map.of("type", "object")))));
        assertEquals("search", tools.getFirst().name());
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> invoke("tools", new Class<?>[]{List.class}, List.of(Map.of("function", Map.of("description", "missing")))));

        ChatResponse withTools = ChatResponse.builder().content("answer").promptTokens(2).completionTokens(3)
                .totalTokens(5).toolCalls(List.of(new ChatResponse.ToolCall("call1", "search", "{}"))).build();
        @SuppressWarnings("unchecked") Map<String, Object> completion = (Map<String, Object>) invoke("completion", new Class<?>[]{String.class, ChatResponse.class}, "gpt", withTools);
        assertTrue(completion.containsKey("choices"));
        assertEquals(2, ((Map<?, ?>) invoke("usage", new Class<?>[]{ChatResponse.class}, withTools)).get("prompt_tokens"));
        assertEquals("OPENAI", invoke("platform", new Class<?>[]{String.class, String.class}, "gpt", " openai "));
        assertEquals("OPENAI", invoke("platform", new Class<?>[]{String.class, String.class}, "gpt", null));
        assertEquals(128000, invoke("safeMaxOutputTokens", new Class<?>[]{Integer.class}, 999999));
        assertEquals(1, invoke("safeMaxOutputTokens", new Class<?>[]{Integer.class}, 0));
        assertNull(invoke("safeMaxOutputTokens", new Class<?>[]{Integer.class}, new Object[]{null}));

        @SuppressWarnings("unchecked") List<ChatMessage> fromInput = (List<ChatMessage>) invoke("messageMaps", new Class<?>[]{Object.class}, raw);
        assertEquals(5, fromInput.size());
        assertTrue(((List<?>) invoke("messageMaps", new Class<?>[]{Object.class}, "not-a-list")).isEmpty());
    }

    @Test
    void persistsExplicitStoreAndCoversRuntimeEventHelpers() throws Exception {
        Conversation conversation = new Conversation("c1", 7, 8, "chat", "Chat", ConversationStatus.ACTIVE,
                null, null, null, null, "OPENAI", "gpt", 0, Instant.now(), Instant.now());
        ConversationMessage input = new ConversationMessage("m1", "c1", null, null, com.shiyu.ai.conversation.domain.MessageRole.USER,
                List.of(com.shiyu.ai.conversation.domain.ContentPart.text("hello")), Map.of(), com.shiyu.ai.conversation.domain.MessageStatus.COMPLETED, 0, null, Instant.now(), Instant.now());
        when(repository.findConversation("c1", new TenantId(7), 8)).thenReturn(Optional.of(conversation));
        when(repository.listMessages("c1", new TenantId(7), 8, 1000)).thenReturn(List.of());
        when(conversations.appendMessage(any(), any(), any(), anyString(), isNull(), isNull())).thenReturn(input);
        invoke("persistIfRequested", new Class<?>[]{Boolean.class, String.class, List.class, String.class, String.class, String.class, AiRun.class},
                true, "c1", List.of(ChatMessage.text("user", "hello")), "answer", "OPENAI", "gpt", run);
        verify(conversations).recordCompletedGeneration(eq(conversation), eq(input), eq("answer"), eq("OPENAI"), eq("gpt"), anyLong(), anyLong(), eq(runtime), eq(run));

        invoke("appendRuntimeEvent", new Class<?>[]{AiRun.class, ChatResponse.class, String.class}, run,
                ChatResponse.builder().eventType("DELTA").content("x").build(), "DELTA");
        invoke("appendRuntimeEvent", new Class<?>[]{AiRun.class, ChatResponse.class, String.class}, run,
                ChatResponse.builder().eventType("USAGE").promptTokens(1).completionTokens(2).estimatedUsage(true).build(), "USAGE");
        invoke("appendRuntimeEvent", new Class<?>[]{AiRun.class, ChatResponse.class, String.class}, run,
                ChatResponse.builder().eventType("TOOL_CALL").toolCallId("c").toolName("search").build(), "TOOL_CALL");
        invoke("appendRuntimeEvent", new Class<?>[]{AiRun.class, ChatResponse.class, String.class}, run,
                ChatResponse.builder().eventType("REASONING_DELTA").build(), "REASONING_DELTA");
        invoke("appendRuntimeEvent", new Class<?>[]{AiRun.class, ChatResponse.class, String.class}, run,
                ChatResponse.builder().eventType("BLOCK_STARTED").build(), "BLOCK_STARTED");
        invoke("appendRuntimeEvent", new Class<?>[]{AiRun.class, ChatResponse.class, String.class}, run,
                ChatResponse.builder().eventType("BLOCK_COMPLETED").build(), "BLOCK_COMPLETED");
        invoke("appendRuntimeEvent", new Class<?>[]{AiRun.class, ChatResponse.class, String.class}, run,
                ChatResponse.builder().eventType("COMPLETED").build(), "COMPLETED");
        invoke("appendRuntimeEvent", new Class<?>[]{AiRun.class, ChatResponse.class, String.class}, null,
                ChatResponse.builder().eventType("DELTA").build(), "DELTA");
        verify(runtime, atLeast(6)).append(eq(run), any(), anyString(), eq(true));
    }

    @Test
    void coversOpenAiEdgeMappingsAndUnsuccessfulStreamTermination() throws Exception {
        @SuppressWarnings("unchecked")
        List<ChatMessage> empty = (List<ChatMessage>) invoke("structured", new Class<?>[]{List.class}, new Object[]{null});
        assertTrue(empty.isEmpty());
        @SuppressWarnings("unchecked")
        List<ChatMessage> mixed = (List<ChatMessage>) invoke("structured", new Class<?>[]{List.class}, List.of(
                Map.of("content", 42),
                Map.of("role", "assistant", "tool_calls", List.of("not-a-map", Map.of("function", "not-a-map"))),
                Map.of("role", "tool", "content", List.of(Map.of("type", "input_text", "text", "ok")), "name", "lookup")));
        assertEquals(3, mixed.size());
        assertEquals("tool_result", mixed.get(2).content().getFirst().type());

        ChatMessage.ContentPart image = (ChatMessage.ContentPart) invoke("contentPart", new Class<?>[]{Object.class},
                Map.of("type", "input_image", "image_url", Map.of("url", "https://img"), "mime_type", "image/png"));
        assertEquals("image", image.type());
        ChatMessage.ContentPart audio = (ChatMessage.ContentPart) invoke("contentPart", new Class<?>[]{Object.class},
                Map.of("type", "input_audio", "content", "sound"));
        assertEquals("audio", audio.type());
        assertEquals("text", ((ChatMessage.ContentPart) invoke("contentPart", new Class<?>[]{Object.class}, 9)).type());

        assertTrue(((List<?>) invoke("tools", new Class<?>[]{List.class}, new Object[]{null})).isEmpty());
        assertEquals("OPENAI", invoke("platform", new Class<?>[]{String.class, String.class}, null, null));
        assertEquals(1, invoke("estimate", new Class<?>[]{String.class}, ""));
        assertEquals(2, ((Map<?, ?>) invoke("usage", new Class<?>[]{ChatResponse.class}, ChatResponse.builder().content("").build())).get("total_tokens"));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> invoke("requireSuccessful", new Class<?>[]{ChatResponse.class, AiRun.class}, ChatResponse.builder().build(), run));
        invoke("failRun", new Class<?>[]{AiRun.class, Throwable.class}, run, null);
        invoke("cancelRun", new Class<?>[]{AiRun.class}, new Object[]{null});

        when(engine.stream(any())).thenReturn(Flux.just(ChatResponse.builder().eventType("DELTA").content("partial").build()));
        var incomplete = new OpenAiCompatibleController.ChatCompletionRequest("gpt", null,
                List.of(Map.of("role", "user", "content", "hi")), true, false, null, null, null, null, null);
        assertEquals(2, ((Flux<?>) controller.chatCompletions(incomplete)).collectList().block().size());
        when(engine.stream(any())).thenReturn(Flux.error(new IllegalStateException("stream down")));
        assertThrows(Exception.class, () -> ((Flux<?>) controller.chatCompletions(incomplete)).collectList().block());
        verify(runtime, atLeastOnce()).finish(eq("run1"), eq(new TenantId(7L)), eq(8L), eq(AiRunStatus.FAILED), anyString());
    }

    @Test
    void comparesExistingStoredMessagesByRoleAndText() throws Exception {
        ConversationMessage message = new ConversationMessage("m1", "c1", null, null,
                com.shiyu.ai.conversation.domain.MessageRole.USER,
                List.of(com.shiyu.ai.conversation.domain.ContentPart.text("hello")), Map.of(),
                com.shiyu.ai.conversation.domain.MessageStatus.COMPLETED, 0, null, Instant.now(), Instant.now());
        assertTrue((Boolean) invoke("sameMessage", new Class<?>[]{ConversationMessage.class, com.shiyu.ai.conversation.domain.MessageRole.class, String.class},
                message, com.shiyu.ai.conversation.domain.MessageRole.USER, "hello"));
        assertFalse((Boolean) invoke("sameMessage", new Class<?>[]{ConversationMessage.class, com.shiyu.ai.conversation.domain.MessageRole.class, String.class},
                message, com.shiyu.ai.conversation.domain.MessageRole.ASSISTANT, "hello"));
        assertFalse((Boolean) invoke("sameMessage", new Class<?>[]{ConversationMessage.class, com.shiyu.ai.conversation.domain.MessageRole.class, String.class},
                message, com.shiyu.ai.conversation.domain.MessageRole.USER, "different"));
    }

    @Test
    void streamsFailureAndDefaultDeltaEvents() {
        when(engine.stream(any())).thenReturn(Flux.fromIterable(List.of(
                ChatResponse.builder().eventType(null).content("fallback").build(),
                ChatResponse.builder().eventType("FAILED").build())));
        var request = new OpenAiCompatibleController.ChatCompletionRequest("gpt", null,
                List.of(Map.of("role", "user", "content", "hi")), true, false, null, null, null, null, null);
        List<?> events = ((Flux<?>) controller.chatCompletions(request)).collectList().block();
        assertEquals(3, events.size());
        verify(runtime).finish("run1", new TenantId(7), 8, AiRunStatus.FAILED, "IllegalStateException");
    }

    private Object invoke(String name, Class<?>[] types, Object... args) throws Exception {
        Method method = OpenAiCompatibleController.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        try {
            return method.invoke(controller, args);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof Exception exception) throw exception;
            if (cause instanceof Error error) throw error;
            throw ex;
        }
    }
}
