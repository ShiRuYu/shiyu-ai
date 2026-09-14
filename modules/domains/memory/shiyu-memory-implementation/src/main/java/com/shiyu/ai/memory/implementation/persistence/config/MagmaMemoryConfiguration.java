package com.shiyu.ai.memory.implementation.persistence.config;
import com.shiyu.ai.memory.implementation.domain.magma.port.MemoryAccessPolicy;
import com.shiyu.ai.memory.implementation.domain.magma.service.MagmaMemoryService;

import com.shiyu.ai.memory.contract.model.*;
import com.shiyu.ai.memory.implementation.persistence.repository.JVectorMemorySemanticIndex;
import com.shiyu.ai.memory.implementation.persistence.repository.JdbcMagmaMemoryRepository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * {@code MagmaMemoryConfiguration} 提供平台模块的配置项，并集中声明其默认值和运行约束。
 */
@Configuration
@EnableScheduling
public class MagmaMemoryConfiguration {
    /**
     * {@code magmaMemoryService} 执行当前类型定义的业务操作。
     *
     * @param repository 参数值，用于执行当前操作。
     * @param index 参数值，用于执行当前操作。
     * @param accessPolicies 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public MagmaMemoryService magmaMemoryService(
            JdbcMagmaMemoryRepository repository,
            JVectorMemorySemanticIndex index,
            ObjectProvider<MemoryAccessPolicy> accessPolicies) {
        return new MagmaMemoryService(
                repository, index, accessPolicies.orderedStream().findFirst().orElse(null));
    }
}
