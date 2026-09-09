package com.shiyu.ai.model.contract.api;

import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;

import reactor.core.publisher.Flux;

/** Model bounded-context entry point consumed by other domains. */
public interface ChatEngine {
    ChatResponse chat(ChatRequest request);
    Flux<ChatResponse> stream(ChatRequest request);
}
