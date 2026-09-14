package com.shiyu.ai.agent.implementation.config;

import com.shiyu.ai.agent.implementation.runtime.model.AgentExecutionContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code AgentMemoryConfiguration} 提供智能体模块的配置项，并集中声明其默认值和运行约束。
 */
@Configuration
public class AgentMemoryConfiguration {
    /**
     * {@code agentExecutionContext} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public AgentExecutionContext agentExecutionContext() {
        return new AgentExecutionContext();
    }
}
