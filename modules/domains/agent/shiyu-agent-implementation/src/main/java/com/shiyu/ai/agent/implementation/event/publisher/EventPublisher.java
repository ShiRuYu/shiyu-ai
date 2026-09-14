package com.shiyu.ai.agent.implementation.event.publisher;
import com.shiyu.ai.agent.implementation.event.model.DomainEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** 事件发布器 包装 Spring ApplicationEventPublisher，统一事件发布入口 */
@Slf4j
@Component
public class EventPublisher {

    /**
     * springEventPublisher 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ApplicationEventPublisher springEventPublisher;

    /**
     * {@code EventPublisher} 创建并初始化当前类型实例。
     *
     * @param springEventPublisher 参数值，用于执行当前操作。
     */
    public EventPublisher(ApplicationEventPublisher springEventPublisher) {
        this.springEventPublisher = springEventPublisher;
    }

    /** 发布领域事件 */
    public void publish(DomainEvent event) {
        log.debug("发布事件: type={}, eventId={}", event.getEventType(), event.getEventId());
        springEventPublisher.publishEvent(event);
    }
}
