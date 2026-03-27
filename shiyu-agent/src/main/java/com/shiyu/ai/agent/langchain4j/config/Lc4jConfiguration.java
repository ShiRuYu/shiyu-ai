package com.shiyu.ai.agent.langchain4j.config;

import com.shiyu.ai.agent.langchain4j.Lc4jModelManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * LangChain4j 配置管理类
 * 负责初始化和监控 LangChain4j 相关配置
 */
@Slf4j
@Component
public class Lc4jConfiguration {
    
    private final Lc4jModelManager modelManager;
    
    public Lc4jConfiguration(Lc4jModelManager modelManager) {
        this.modelManager = modelManager;
        log.info("LangChain4j 配置管理类已加载");
    }
    
    /**
     * 应用启动后检查可用的平台
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("=== LangChain4j 平台可用性检查 ===");
        modelManager.getAllAdapters().forEach((platformType, adapter) -> {
            boolean available = adapter.isAvailable();
            String defaultModel = adapter.getDefaultModelName();
            log.info("平台：{} | 状态：{} | 默认模型：{}", 
                    platformType, 
                    available ? "可用" : "不可用", 
                    defaultModel != null ? defaultModel : "未配置");
        });
        log.info("======================================");
    }
}
