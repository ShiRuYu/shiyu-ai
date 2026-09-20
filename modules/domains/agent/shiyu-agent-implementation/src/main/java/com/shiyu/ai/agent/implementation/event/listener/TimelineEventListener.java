package com.shiyu.ai.agent.implementation.event.listener;
import com.shiyu.ai.agent.implementation.event.model.NodeExecutionCompletedEvent;
import com.shiyu.ai.agent.implementation.event.model.NodeExecutionStartedEvent;

import com.shiyu.ai.agent.implementation.service.TimelineService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 处理 时间线 事件 相关事件或请求，并推进后续业务流程。
 */
@Slf4j
@Component
public class TimelineEventListener {

    /**
     * timelineService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final TimelineService timelineService;

    /**
     * 执行 时间线 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param timelineService 用于完成本次业务处理的 timelineService 参数。
     */
    public TimelineEventListener(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    /**
     * 处理 时间线 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    @Async
    @EventListener
    public void onNodeStarted(NodeExecutionStartedEvent event) {
        timelineService.onNodeStarted(event);
    }

    /**
     * 处理 时间线 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    @Async
    @EventListener
    public void onNodeCompleted(NodeExecutionCompletedEvent event) {
        timelineService.onNodeCompleted(event);
    }
}
