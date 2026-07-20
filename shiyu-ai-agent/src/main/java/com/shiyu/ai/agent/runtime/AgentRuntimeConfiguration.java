package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.cache.AgentLoader;
import com.shiyu.ai.agent.event.EventPublisher;
import com.shiyu.ai.dal.agent.repository.AgentCheckpointRepository;
import com.shiyu.ai.dal.agent.repository.AgentExecutionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentRuntimeConfiguration {

    @Bean
    public AgentRuntime agentRuntime(AgentCacheManager cacheManager,
                                     AgentLoader agentLoader,
                                     AgentExecutionRepository executionRepository,
                                     AgentCheckpointRepository checkpointRepository,
                                     EventPublisher eventPublisher) {
        return new AgentRuntimeImpl(
            cacheManager,
            agentLoader,
            executionRepository,
            checkpointRepository,
            eventPublisher
        );
    }
}
