package com.shiyu.ai.agent.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 事件发布器
 * 包装 Spring ApplicationEventPublisher，统一事件发布入口
 */
@Slf4j
@Component
public class EventPublisher {

    private final ApplicationEventPublisher springEventPublisher;

    public EventPublisher(ApplicationEventPublisher springEventPublisher) {
        this.springEventPublisher = springEventPublisher;
    }

    /**
     * 发布领域事件
     */
    public void publish(DomainEvent event) {
        log.debug("发布事件: type={}, eventId={}", event.getEventType(), event.getEventId());
        springEventPublisher.publishEvent(event);
    }
}
