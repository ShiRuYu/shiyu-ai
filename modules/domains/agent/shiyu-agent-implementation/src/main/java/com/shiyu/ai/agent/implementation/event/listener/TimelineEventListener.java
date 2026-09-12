package com.shiyu.ai.agent.implementation.event.listener;
import com.shiyu.ai.agent.implementation.event.model.NodeExecutionCompletedEvent;
import com.shiyu.ai.agent.implementation.event.model.NodeExecutionStartedEvent;

import com.shiyu.ai.agent.implementation.service.TimelineService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 时间线事件监听器
 *
 * <p>异步消费节点执行事件，写入 {@code execution_timeline} 表。
 */
@Slf4j
@Component
public class TimelineEventListener {

    /**
     * timelineService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final TimelineService timelineService;

    /**
     * {@code TimelineEventListener} 创建并初始化当前类型实例。
     *
     * @param timelineService 参数值，用于执行当前操作。
     */
    public TimelineEventListener(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    /**
     * {@code onNodeStarted} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
     */
    @Async
    @EventListener
    public void onNodeStarted(NodeExecutionStartedEvent event) {
        timelineService.onNodeStarted(event);
    }

    /**
     * {@code onNodeCompleted} 执行当前类型定义的业务操作。
     *
     * @param event 参数值，用于执行当前操作。
     */
    @Async
    @EventListener
    public void onNodeCompleted(NodeExecutionCompletedEvent event) {
        timelineService.onNodeCompleted(event);
    }
}
