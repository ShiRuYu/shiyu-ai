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
 * {@code RuntimeAutoConfiguration} 提供智能体模块的配置项，并集中声明其默认值和运行约束。
 */
@Configuration
public class RuntimeAutoConfiguration {
    /**
     * {@code inMemoryAiRunRepository} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    @ConditionalOnMissingBean(AiRunRepository.class)
    public AiRunRepository inMemoryAiRunRepository() {
        return new InMemoryAiRunRepository();
    }

    /**
     * {@code inMemoryAiAppRepository} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    @ConditionalOnMissingBean(AiAppRepository.class)
    public AiAppRepository inMemoryAiAppRepository() {
        return new InMemoryAiAppRepository();
    }

    /**
     * {@code inMemoryToolApprovalRepository} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    @ConditionalOnMissingBean(ToolApprovalRepository.class)
    public ToolApprovalRepository inMemoryToolApprovalRepository() {
        return new InMemoryToolApprovalRepository();
    }

    /**
     * {@code toolExecutionPipeline} 将当前对象转换为目标表示形式。
     *
     * @param runtime 参数值，用于执行当前操作。
     * @param approvals 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    @ConditionalOnMissingBean(ToolExecutionPipeline.class)
    public ToolExecutionPipeline toolExecutionPipeline(
            AiRuntimeService runtime, ToolApprovalService approvals) {
        return new ToolExecutionPipeline(runtime, approvals);
    }
}
