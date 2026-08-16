package com.shiyu.ai.agent.config;

import com.shiyu.ai.agent.runtime.AgentExecutionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentMemoryConfiguration {
    @Bean public AgentExecutionContext agentExecutionContext() { return new AgentExecutionContext(); }
}
