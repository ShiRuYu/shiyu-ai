package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.cache.AgentLoader;
import com.shiyu.ai.agent.event.EventPublisher;
import com.shiyu.ai.dal.repository.agent.AgentExecutionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AgentRuntimeConfiguration {

    @Bean
    public AgentRuntime agentRuntime(AgentCacheManager cacheManager,
                                     AgentLoader agentLoader,
                                     AgentExecutionRepository executionRepository,
                                     JdbcTemplate jdbcTemplate,
                                     EventPublisher eventPublisher) {
        return new AgentRuntimeImpl(
            cacheManager,
            agentLoader,
            executionRepository,
            jdbcTemplate,
            eventPublisher
        );
    }
}
