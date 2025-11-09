package com.shiyu.ai.chat.core.model;


public class LocalLLMAdapter implements ModelAdapter {
    @Override
    public LMResponse generate(LMRequest request) {
        return new LMResponse("Local LLM response for: " + request.getPrompt());
    }
}
