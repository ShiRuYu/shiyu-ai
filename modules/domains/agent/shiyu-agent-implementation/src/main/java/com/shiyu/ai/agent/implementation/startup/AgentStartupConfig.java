package com.shiyu.ai.agent.implementation.startup;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 定义 智能体 Startup 基础设施或应用能力的配置项及装配规则。
 */
@Slf4j
@Component
public final class AgentStartupConfig implements ApplicationRunner {
    /**
     * 执行 智能体 Startup 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param args 用于完成本次业务处理的 args 参数。
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("Agent 使用惰性加载，定义和版本由领域 seed 数据初始化");
    }
}
