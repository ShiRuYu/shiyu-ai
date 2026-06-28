package com.shiyu.ai.core.langchain4j.config;

import com.shiyu.ai.core.langchain4j.Lc4jModelManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * LangChain4j 配置管理类（占位）
 * 实际的初始化逻辑已迁移到 Lc4jModelManager（ApplicationRunner），
 * 该类保留用于后续扩展（如健康检查、指标监控等）。
 */
@Slf4j
@Component
public class Lc4jConfiguration {

    private final Lc4jModelManager modelManager;

    public Lc4jConfiguration(Lc4jModelManager modelManager) {
        this.modelManager = modelManager;
        log.info("LangChain4j 配置管理类已加载");
    }
}
