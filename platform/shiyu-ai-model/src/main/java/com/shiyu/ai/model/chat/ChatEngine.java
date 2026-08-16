package com.shiyu.ai.model.chat;

import reactor.core.publisher.Flux;

public interface ChatEngine {

    ChatResponse chat(ChatRequest request);

    Flux<ChatResponse> stream(ChatRequest request);

}
