package com.shiyu.ai.chat.core.model;

public class OpenAIAdapter implements ModelAdapter {
    @Override
    public LMResponse generate(LMRequest request) {
        return new LMResponse("OpenAI response for: " + request.getPrompt());
    }
}
