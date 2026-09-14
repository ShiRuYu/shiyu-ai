package com.shiyu.ai.common.core.event;

import com.shiyu.ai.kernel.event.DomainEventEnvelope;

/**
 * DomainEventPublisher 接口，定义基础设施模块的能力边界。
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
