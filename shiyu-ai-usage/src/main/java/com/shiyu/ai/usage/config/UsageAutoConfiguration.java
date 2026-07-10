package com.shiyu.ai.usage.config;

import com.shiyu.ai.usage.collector.UsageCollector;
import com.shiyu.ai.usage.service.TokenUsageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
public class UsageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TokenUsageRepository tokenUsageRepository(JdbcTemplate jdbcTemplate) {
        return new TokenUsageRepository(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public UsageCollector usageCollector(TokenUsageRepository repository) {
        log.info("创建 UsageCollector");
        return new UsageCollector(repository);
    }
}
