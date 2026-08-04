package com.shiyu.ai.model.adapter.config;

import com.shiyu.ai.model.adapter.ModelManager;
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
