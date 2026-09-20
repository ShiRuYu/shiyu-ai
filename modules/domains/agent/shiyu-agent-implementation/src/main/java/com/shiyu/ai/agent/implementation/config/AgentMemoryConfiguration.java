package com.shiyu.ai.agent.implementation.config;

import com.shiyu.ai.agent.implementation.runtime.model.AgentExecutionContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 智能体 记忆 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
public class AgentMemoryConfiguration {
    /**
     * 执行 智能体 记忆 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 记忆 相关操作生成的结果数据。
     */
    @Bean
    public AgentExecutionContext agentExecutionContext() {
        return new AgentExecutionContext();
    }
}
