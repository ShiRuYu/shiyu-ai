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
 * 定义 智能体 Runtime 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
public class AgentRuntimeConfiguration {

    /**
     * 执行 智能体 Runtime 相关业务数据，并返回处理结果。
     *
     * @param cacheManager 用于完成本次业务处理的 cacheManager 参数。
     * @param agentLoader 用于完成本次业务处理的 agentLoader 参数。
     * @param executionRepository 用于完成本次业务处理的 executionRepository 参数。
     * @param checkpointRepository 用于完成本次业务处理的 checkpointRepository 参数。
     * @param eventPublisher 用于完成本次业务处理的 eventPublisher 参数。
     * @param runtime 用于完成本次业务处理的 runtime 参数。
     * @return 返回 智能体 Runtime 相关操作生成的结果数据。
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
