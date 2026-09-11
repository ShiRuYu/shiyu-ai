package com.shiyu.ai.knowledge.implementation.domain.model;

import java.util.function.Consumer;

public interface ChatProvider {
    String profile();

    boolean available();

    void stream(String prompt, Consumer<String> consumer);
}
