package com.shiyu.ai.runtime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(AiRunRepository.class)
    public AiRunRepository inMemoryAiRunRepository() { return new InMemoryAiRunRepository(); }

    @Bean
    @ConditionalOnMissingBean(AiAppRepository.class)
    public AiAppRepository inMemoryAiAppRepository() { return new InMemoryAiAppRepository(); }

    @Bean
    @ConditionalOnMissingBean(ToolApprovalRepository.class)
    public ToolApprovalRepository inMemoryToolApprovalRepository() { return new InMemoryToolApprovalRepository(); }
}
