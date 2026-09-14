package com.shiyu.ai.agent.implementation.event.listener;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionCompletedEvent;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionFailedEvent;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionStartedEvent;
import com.shiyu.ai.agent.implementation.event.model.ModelCallEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Agent 事件监听器 记录执行事件到日志 */
@Slf4j
@Component
public class AgentEventListener {

    /**
     * {@code onExecutionStarted} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
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
     * {@code onExecutionCompleted} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
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
     * {@code onExecutionFailed} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
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
     * {@code onModelCall} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
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
