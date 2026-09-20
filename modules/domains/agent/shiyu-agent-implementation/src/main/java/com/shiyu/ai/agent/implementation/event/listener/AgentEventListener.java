package com.shiyu.ai.agent.implementation.event.listener;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionCompletedEvent;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionFailedEvent;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionStartedEvent;
import com.shiyu.ai.agent.implementation.event.model.ModelCallEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 处理 智能体 事件 相关事件或请求，并推进后续业务流程。
 */
@Slf4j
@Component
public class AgentEventListener {

    /**
     * 处理 智能体 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    @EventListener
    @Async
    public void onExecutionStarted(AgentExecutionStartedEvent event) {
        log.info(
                "Agent 执行开始: agentIdPresent={}, executionIdPresent={}",
                event.getAgentId() != null,
                event.getExecutionId() != null);
    }

    /**
     * 处理 智能体 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    @EventListener
    @Async
    public void onExecutionCompleted(AgentExecutionCompletedEvent event) {
        log.info(
                "Agent 执行完成: agentIdPresent={}, executionIdPresent={}, duration={}ms",
                event.getAgentId() != null,
                event.getExecutionId() != null,
                event.getDurationMs());
    }

    /**
     * 处理 智能体 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    @EventListener
    @Async
    public void onExecutionFailed(AgentExecutionFailedEvent event) {
        log.warn(
                "Agent 执行失败: agentIdPresent={}, executionIdPresent={}, errorMessageLength={}",
                event.getAgentId() != null,
                event.getExecutionId() != null,
                event.getErrorMessage() == null ? 0 : event.getErrorMessage().length());
    }

    /**
     * 处理 智能体 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    @EventListener
    @Async
    public void onModelCall(ModelCallEvent event) {
        log.debug(
                "模型调用: platform={}, model={}, prompt={}, completion={}, latency={}ms",
                event.getPlatform(),
                event.getModel(),
                event.getPromptTokens(),
                event.getCompletionTokens(),
                event.getLatencyMs());
    }
}
