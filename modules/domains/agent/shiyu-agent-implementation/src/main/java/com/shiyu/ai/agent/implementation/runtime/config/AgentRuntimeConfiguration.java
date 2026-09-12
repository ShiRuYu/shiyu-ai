package com.shiyu.ai.agent.implementation.runtime.config;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.service.AgentRuntimeImpl;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.implementation.cache.AgentCacheManager;
import com.shiyu.ai.agent.implementation.cache.AgentLoader;
import com.shiyu.ai.agent.implementation.event.publisher.EventPublisher;
import com.shiyu.ai.agent.implementation.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code AgentRuntimeConfiguration} 提供智能体模块的配置项，并集中声明其默认值和运行约束。
 */
@Configuration
public class AgentRuntimeConfiguration {

    /**
     * {@code agentRuntime} 执行当前类型定义的业务操作。
     *
     * @param cacheManager 参数值，用于执行当前操作。
     * @param agentLoader 参数值，用于执行当前操作。
     * @param executionRepository 参数值，用于执行当前操作。
     * @param checkpointRepository 参数值，用于执行当前操作。
     * @param eventPublisher 参数值，用于执行当前操作。
     * @param runtime 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public AgentRuntime agentRuntime(
            AgentCacheManager cacheManager,
            AgentLoader agentLoader,
            AgentExecutionRepository executionRepository,
            AgentCheckpointRepository checkpointRepository,
            EventPublisher eventPublisher,
            AiRuntimeService runtime) {
        return new AgentRuntimeImpl(
                cacheManager,
                agentLoader,
                executionRepository,
                checkpointRepository,
                eventPublisher,
                runtime);
    }
}
