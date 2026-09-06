package com.shiyu.ai.memory.implementation.persistence.config;

import com.shiyu.ai.memory.implementation.persistence.repository.JdbcMagmaMemoryRepository;
import com.shiyu.ai.memory.implementation.persistence.repository.JVectorMemorySemanticIndex;
import com.shiyu.ai.memory.magma.MagmaMemoryService;
import com.shiyu.ai.memory.magma.MemoryAccessPolicy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class MagmaMemoryConfiguration {
    @Bean
    public MagmaMemoryService magmaMemoryService(JdbcMagmaMemoryRepository repository, JVectorMemorySemanticIndex index,
                                                 ObjectProvider<MemoryAccessPolicy> accessPolicies) {
        return new MagmaMemoryService(repository, index, accessPolicies.orderedStream().findFirst().orElse(null));
    }
}

