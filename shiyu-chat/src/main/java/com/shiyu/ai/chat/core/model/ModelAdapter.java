package com.shiyu.ai.chat.core.model;

public interface ModelAdapter {
    LMResponse generate(LMRequest request);
}
