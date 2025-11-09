package com.shiyu.ai.chat.core.config;

import com.shiyu.ai.chat.core.model.LocalLLMAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {
    @Bean
    public LocalLLMAdapter chatProperties() {
        return new LocalLLMAdapter();
    }
}
