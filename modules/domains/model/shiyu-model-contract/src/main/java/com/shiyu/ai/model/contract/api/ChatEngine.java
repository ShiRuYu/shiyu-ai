package com.shiyu.ai.model.contract.api;

import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;

import reactor.core.publisher.Flux;

/** Model bounded-context entry point consumed by other domains. */
public interface ChatEngine {
    /** Executes one provider-neutral chat request and returns the completed result. */
    ChatResponse chat(ChatRequest request);

    /** Executes a chat request as a stream of text, reasoning, tool and terminal events. */
    Flux<ChatResponse> stream(ChatRequest request);
}
