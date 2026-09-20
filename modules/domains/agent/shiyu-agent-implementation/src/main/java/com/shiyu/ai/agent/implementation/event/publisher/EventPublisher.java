package com.shiyu.ai.agent.implementation.event.publisher;
import com.shiyu.ai.agent.implementation.event.model.DomainEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 发布 事件 相关的领域事件或基础设施消息。
 */
@Slf4j
@Component
public class EventPublisher {

    /**
     * springEventPublisher 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ApplicationEventPublisher springEventPublisher;

    /**
     * 执行 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param springEventPublisher 用于完成本次业务处理的 springEventPublisher 参数。
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
