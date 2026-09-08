package com.shiyu.ai.agent.event;

import com.shiyu.ai.agent.service.TimelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 时间线事件监听器
 * <p>
 * 异步消费节点执行事件，写入 {@code execution_timeline} 表。
 */
@Slf4j
@Component
public class TimelineEventListener {

    private final TimelineService timelineService;

    public TimelineEventListener(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @Async
    @EventListener
    public void onNodeStarted(NodeExecutionStartedEvent event) {
        timelineService.onNodeStarted(event);
    }

    @Async
    @EventListener
    public void onNodeCompleted(NodeExecutionCompletedEvent event) {
        timelineService.onNodeCompleted(event);
    }
}
