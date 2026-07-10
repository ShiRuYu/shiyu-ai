package com.shiyu.ai.model.chat;

import reactor.core.publisher.Flux;

public interface ChatEngine {

    ChatResponse chat(ChatRequest request);

    Flux<ChatResponse> stream(ChatRequest request);

    ChatResponse chatWithMemory(String sessionId, ChatRequest request);

    Flux<ChatResponse> streamWithMemory(String sessionId, ChatRequest request);
}
