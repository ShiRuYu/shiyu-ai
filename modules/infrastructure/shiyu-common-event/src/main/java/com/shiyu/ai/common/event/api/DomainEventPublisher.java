package com.shiyu.ai.common.event.api;

import com.shiyu.ai.kernel.event.DomainEventEnvelope;

/**
 * 发布 Domain 事件 相关的领域事件或基础设施消息。
 */
@FunctionalInterface
public interface DomainEventPublisher {

    /**
     * 发布domain事件publisher。
     *
     * @param event 领域事件。
     */
    void publish(DomainEventEnvelope<?> event);
}
