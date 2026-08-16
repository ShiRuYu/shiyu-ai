package com.shiyu.ai.dal.memory.config;

import com.shiyu.ai.dal.memory.repository.JdbcMagmaMemoryRepository;
import com.shiyu.ai.dal.memory.repository.JVectorMemorySemanticIndex;
import com.shiyu.ai.memory.magma.MagmaMemoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class MagmaMemoryConfiguration {
    @Bean
    public MagmaMemoryService magmaMemoryService(JdbcMagmaMemoryRepository repository, JVectorMemorySemanticIndex index) {
        return new MagmaMemoryService(repository, index);
    }
}
