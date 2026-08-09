package com.shiyu.ai.web.model;

import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("dev")
class ChatDemoControllerTest {

    @Test
    void streamEndpointUsesSseAndPreservesContentChunks() throws Exception {
        ChatEngine chatEngine = mock(ChatEngine.class);
        ModelManager modelManager = mock(ModelManager.class);
        when(chatEngine.stream(any())).thenReturn(Flux.just(
                ChatResponse.builder().success(true).content("hello").build()));

        ChatDemoController controller = new ChatDemoController(chatEngine, modelManager);
        ChatDemoController.DemoChatRequest request = new ChatDemoController.DemoChatRequest();
        request.setPrompt("question");

        ChatResponse response = controller.streamChat(request).blockFirst();
        assertEquals("hello", response.getContent());

        Method method = ChatDemoController.class.getMethod(
                "streamChat", ChatDemoController.DemoChatRequest.class);
        PostMapping mapping = method.getAnnotation(PostMapping.class);
        assertEquals(MediaType.TEXT_EVENT_STREAM_VALUE, mapping.produces()[0]);
    }
}
