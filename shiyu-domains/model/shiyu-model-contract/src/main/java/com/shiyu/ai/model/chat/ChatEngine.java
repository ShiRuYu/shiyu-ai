package com.shiyu.ai.model.chat;

import reactor.core.publisher.Flux;

/** Model bounded-context entry point consumed by other domains. */
public interface ChatEngine {
    ChatResponse chat(ChatRequest request);
    Flux<ChatResponse> stream(ChatRequest request);
}
