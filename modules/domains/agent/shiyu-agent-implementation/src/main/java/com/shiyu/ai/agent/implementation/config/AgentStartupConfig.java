package com.shiyu.ai.agent.implementation.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * AgentStartupConfig 配置组件，负责注册和配置智能体领域相关基础设施。
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
