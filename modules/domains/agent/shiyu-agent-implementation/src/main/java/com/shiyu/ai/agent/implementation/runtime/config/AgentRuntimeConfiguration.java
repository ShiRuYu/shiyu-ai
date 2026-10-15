package com.shiyu.ai.agent.implementation.runtime.config;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.service.AgentRuntimeImpl;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.implementation.cache.AgentCacheManager;
import com.shiyu.ai.agent.implementation.cache.AgentLoader;
import com.shiyu.ai.agent.implementation.event.publisher.EventPublisher;
import com.shiyu.ai.agent.implementation.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 智能体 Runtime 基础设施或应用能力的配置项及装配规则。
 */
@Configuration
public class AgentRuntimeConfiguration {

    /**
     * 装配带有持久化执行状态、检查点和工具审批能力的 Agent 运行时。
     *
     * @param cacheManager 按租户缓存 Agent 定义
     * @param agentLoader 从数据库加载已发布 Agent 版本
     * @param executionRepository 持久化执行状态
     * @param checkpointRepository 持久化图检查点
     * @param eventPublisher 发布执行生命周期事件
     * @param runtime 记录 AI 运行事件与用量
     * @param approvals 管理高风险工具审批
     * @return 已连接上述依赖的 Agent 运行时
     */
    @Bean
    public AgentRuntime agentRuntime(
            AgentCacheManager cacheManager,
            AgentLoader agentLoader,
            AgentExecutionRepository executionRepository,
            AgentCheckpointRepository checkpointRepository,
            EventPublisher eventPublisher,
            AiRuntimeService runtime,
            com.shiyu.ai.agent.implementation.runtime.service.ToolApprovalService approvals) {
        return new AgentRuntimeImpl(
                cacheManager,
                agentLoader,
                executionRepository,
                checkpointRepository,
                eventPublisher,
                runtime,
                approvals);
    }
}
