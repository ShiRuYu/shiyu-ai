package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.domain.model.IntentDefBO;
import com.shiyu.ai.agent.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.agent.node.intent.IntentDefinition;
import com.shiyu.ai.agent.service.IntentService.IntentRecognitionResult;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class IntentServiceImplTest {
    private final ChatEngine chat = mock(ChatEngine.class);
    private final IntentServiceImpl service = new IntentServiceImpl(chat);

    @AfterEach
    void clearDefinitions() {
        IntentDefinitionFactory.reloadFromDb(List.of());
    }

    @Test
    void recognizesSupportedSlotIntentAndBuildsModelRequest() {
        IntentDefinitionFactory.reloadFromDb(List.of(definition("agent-1", "WEATHER", true)));
        when(chat.chat(any(ChatRequest.class))).thenReturn(ChatResponse.builder()
                .success(true)
                .content("```json {\"intentCode\":\"WEATHER\",\"intentName\":\"Weather\",\"confidence\":\"0.95\",\"slots\":{\"city\":\"Beijing\"}} ```")
                .build());

        IntentRecognitionResult result = service.recognize("agent-1", "conversation", "weather in Beijing", "CUSTOM", "model-1");

        assertTrue(result.success());
        assertEquals("WEATHER", result.intentCode());
        assertEquals(0.95, result.confidence());
        assertEquals("Beijing", result.slots().get("city"));
        ArgumentCaptor<ChatRequest> requestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(chat).chat(requestCaptor.capture());
        ChatRequest request = requestCaptor.getValue();
        assertEquals("CUSTOM", request.getPlatform());
        assertEquals("model-1", request.getModel());
        String prompt = request.getMessages().getFirst().content().getFirst().text();
        assertTrue(prompt.contains("WEATHER"));
        assertTrue(prompt.contains("slots"));
    }

    @Test
    void rejectsUnsupportedLowConfidenceAndInvalidResponses() {
        IntentDefinitionFactory.reloadFromDb(List.of(definition("default", "KNOWN", false)));
        when(chat.chat(any(ChatRequest.class))).thenReturn(
                ChatResponse.builder().success(true).content("{\"intentCode\":\"UNKNOWN\",\"intentName\":\"x\",\"confidence\":0.9}").build());
        IntentRecognitionResult unsupported = service.recognize(null, "conversation", "hello");
        assertFalse(unsupported.success());
        assertTrue(unsupported.errorMessage().contains("Unsupported"));

        when(chat.chat(any(ChatRequest.class))).thenReturn(
                ChatResponse.builder().success(true).content("{\"intentCode\":\"KNOWN\",\"intentName\":\"x\",\"confidence\":0.2}").build());
        IntentRecognitionResult low = service.recognize("other-agent", "conversation", "hello");
        assertFalse(low.success());
        assertTrue(low.errorMessage().contains("Low confidence"));

        when(chat.chat(any(ChatRequest.class))).thenReturn(
                ChatResponse.builder().success(true).content("not json").build());
        assertFalse(service.recognize("agent-1", "missing", "hello").success());
        when(chat.chat(any(ChatRequest.class))).thenThrow(new IllegalStateException("model down"));
        assertTrue(service.recognize("agent-1", "missing", "hello").errorMessage().contains("model down"));
    }

    @Test
    void mapsModelFailuresNullResponsesAndConfidenceVariants() {
        IntentDefinitionFactory.reloadFromDb(List.of(definition("agent-1", "KNOWN", false)));
        when(chat.chat(any(ChatRequest.class))).thenReturn(null);
        assertEquals("Unable to obtain model response", service.recognize("agent-1", "conversation", "hello").errorMessage());

        when(chat.chat(any(ChatRequest.class))).thenReturn(ChatResponse.builder()
                .success(false).errorMessage("quota exceeded").build());
        assertEquals("quota exceeded", service.recognize("agent-1", "conversation", "hello").errorMessage());

        when(chat.chat(any(ChatRequest.class))).thenReturn(ChatResponse.builder()
                .success(true).content("{\"intentCode\":\"KNOWN\",\"intentName\":\"x\",\"confidence\":\"bad\",\"slots\":[]}").build());
        IntentRecognitionResult badConfidence = service.recognize("agent-1", "conversation", "hello");
        assertFalse(badConfidence.success());
        assertEquals(0.0, badConfidence.confidence());

        when(chat.chat(any(ChatRequest.class))).thenReturn(ChatResponse.builder()
                .success(true).content("{\"intentCode\":\"KNOWN\",\"intentName\":\"x\",\"confidence\":1}").build());
        assertTrue(service.recognize("agent-1", "conversation", "hello").success());
        assertTrue(service.recognize("agent-1", "conversation", "hello").slots().isEmpty());
    }

    @Test
    void buildsSlotPromptWhenSchemaAndExamplesAreAbsent() throws Exception {
        IntentDefBO sparse = definition("agent-1", "SPARSE", true);
        sparse.setExamples(null);
        sparse.setSlots(null);
        IntentDefinitionFactory.reloadFromDb(List.of(sparse));
        when(chat.chat(any(ChatRequest.class))).thenReturn(ChatResponse.builder()
                .success(true)
                .content("{\"intentCode\":\"SPARSE\",\"intentName\":\"Sparse\",\"confidence\":0.8}")
                .build());
        assertTrue(service.recognize("agent-1", "conversation", "hello", null, null).success());
        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(chat).chat(captor.capture());
        String prompt = captor.getValue().getMessages().getFirst().content().getFirst().text();
        assertTrue(prompt.contains("Some intents require slot extraction"));
        assertTrue(prompt.contains("Examples:"));

        IntentDefinition noExamples = IntentDefinition.builder().code("NO_EXAMPLES")
                .name("No examples").description("none").examples(null)
                .requireSlotFilling(false).build();
        Method buildPrompt = IntentServiceImpl.class.getDeclaredMethod(
                "buildIntentPrompt", String.class, List.class);
        buildPrompt.setAccessible(true);
        String sparsePrompt = (String) buildPrompt.invoke(service, "q", List.of(noExamples));
        assertTrue(sparsePrompt.contains("Examples: N/A"));
        assertTrue(((String) buildPrompt.invoke(service, "q", (Object) null))
                .contains("Return result as JSON only"));
    }

    private static IntentDefBO definition(String agentId, String code, boolean slots) {
        IntentDefBO bo = new IntentDefBO();
        bo.setAgentId(agentId);
        bo.setCode(code);
        bo.setName(code + " name");
        bo.setDescription("description");
        bo.setCategory("conversation");
        bo.setPriority(1);
        bo.setConfidenceThreshold(0.7);
        bo.setExamples(List.of("example"));
        bo.setRequireSlotFilling(slots);
        bo.setSlots(slots ? Map.of("city", "city name") : Map.of());
        bo.setParameterMapping(Map.of());
        bo.setSlotDefaults(Map.of());
        bo.setEnabled(true);
        bo.setStatus(1);
        return bo;
    }
}
