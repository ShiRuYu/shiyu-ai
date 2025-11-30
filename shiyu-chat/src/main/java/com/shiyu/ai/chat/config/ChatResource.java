package com.shiyu.ai.chat.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class ChatResource {

    /**
     * 加载 System prompt tmpl.
     */
    @Value("classpath:/prompts/system-message.st")
    private org.springframework.core.io.Resource systemResource;

    @Value("classpath:/docs/wikipedia-curling.md")
    private org.springframework.core.io.Resource docsToStuffResource;

    @Value("classpath:/prompts/qa-prompt.st")
    private org.springframework.core.io.Resource qaPromptResource;
}
