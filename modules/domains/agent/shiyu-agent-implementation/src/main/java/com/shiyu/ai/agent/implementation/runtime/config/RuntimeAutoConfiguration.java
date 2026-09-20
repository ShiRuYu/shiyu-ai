package com.shiyu.ai.agent.implementation.runtime.config;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiAppRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiRunRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryToolApprovalRepository;
import com.shiyu.ai.agent.implementation.runtime.port.AiAppRepository;
import com.shiyu.ai.agent.implementation.runtime.port.ToolApprovalRepository;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;
import com.shiyu.ai.agent.implementation.runtime.service.ToolApprovalService;
import com.shiyu.ai.agent.implementation.runtime.service.ToolExecutionPipeline;

import com.shiyu.ai.agent.contract.runtime.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 Runtime Auto 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
public class RuntimeAutoConfiguration {
    /**
     * 执行 Runtime Auto 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Bean
    @ConditionalOnMissingBean(AiRunRepository.class)
    public AiRunRepository inMemoryAiRunRepository() {
        return new InMemoryAiRunRepository();
    }

    /**
     * 执行 Runtime Auto 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Bean
    @ConditionalOnMissingBean(AiAppRepository.class)
    public AiAppRepository inMemoryAiAppRepository() {
        return new InMemoryAiAppRepository();
    }

    /**
     * 执行 Runtime Auto 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Bean
    @ConditionalOnMissingBean(ToolApprovalRepository.class)
    public ToolApprovalRepository inMemoryToolApprovalRepository() {
        return new InMemoryToolApprovalRepository();
    }

    /**
     * 执行 Runtime Auto 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Bean
    @ConditionalOnMissingBean(ToolExecutionPipeline.class)
    public ToolExecutionPipeline toolExecutionPipeline(
            AiRuntimeService runtime, ToolApprovalService approvals) {
        return new ToolExecutionPipeline(runtime, approvals);
    }
}
