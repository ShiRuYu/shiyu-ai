package com.shiyu.ai.core.langchain4j.config;

import com.shiyu.ai.core.langchain4j.ModelManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatEngineConfiguration {

    private final ModelManager modelManager;

    public ChatEngineConfiguration(ModelManager modelManager) {
        this.modelManager = modelManager;
        log.info("ChatEngine 配置管理类已加载");
    }
}
