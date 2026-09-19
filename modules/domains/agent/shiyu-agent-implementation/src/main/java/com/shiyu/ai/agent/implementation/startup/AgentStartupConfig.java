package com.shiyu.ai.agent.implementation.startup;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动后记录 Agent 惰性加载及定义初始化策略。
 */
@Slf4j
@Component
public final class AgentStartupConfig implements ApplicationRunner {
    /**
     * {@code run} 执行当前模块定义的业务流程。
     *
     * @param args 参数值，用于执行当前操作。
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("Agent 使用惰性加载，定义和版本由领域 seed 数据初始化");
    }
}
