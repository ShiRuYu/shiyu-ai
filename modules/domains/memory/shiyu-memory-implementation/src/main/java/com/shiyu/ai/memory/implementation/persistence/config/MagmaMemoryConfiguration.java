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
 * 定义 Magma 记忆 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
@EnableScheduling
public class MagmaMemoryConfiguration {
    /**
     * 执行 Magma 记忆 相关业务数据，并返回处理结果。
     *
     * @param repository 用于完成本次业务处理的 repository 参数。
     * @param index 用于完成本次业务处理的 index 参数。
     * @param accessPolicies 用于完成本次业务处理的 accessPolicies 参数。
     * @return 返回 Magma 记忆 相关操作生成的结果数据。
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
