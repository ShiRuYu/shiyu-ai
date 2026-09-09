package com.shiyu.ai.agent.implementation.runtime;

import com.shiyu.ai.agent.contract.runtime.*;

import com.shiyu.ai.agent.implementation.cache.AgentCacheManager;
import com.shiyu.ai.agent.implementation.cache.AgentLoader;
import com.shiyu.ai.agent.implementation.event.EventPublisher;
import com.shiyu.ai.agent.implementation.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.agent.implementation.runtime.AiRuntimeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentRuntimeConfiguration {

    @Bean
    public AgentRuntime agentRuntime(AgentCacheManager cacheManager,
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
            runtime
        );
    }
}
