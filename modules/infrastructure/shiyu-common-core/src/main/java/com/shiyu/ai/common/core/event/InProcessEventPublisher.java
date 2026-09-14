package com.shiyu.ai.common.core.event;

import com.shiyu.ai.kernel.event.DomainEventEnvelope;

import org.springframework.context.ApplicationEventPublisher;

/**
 * 在当前进程内发布基础设施事件。
 */
public final class InProcessEventPublisher implements InfrastructureEventPublisher {

    /**
     * 发布器，表示当前对象中的对应属性。
     */
    private final ApplicationEventPublisher publisher;

    /**
     * {@code InProcessEventPublisher} 创建并初始化当前类型实例。
     *
     * @param publisher 参数值，用于执行当前操作。
     */
    public InProcessEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * {@code publish} 执行当前模块定义的业务流程。
     *
     * @param event 参数值，用于执行当前操作。
     */
    @Override
    public void publish(DomainEventEnvelope<?> event) {
        publisher.publishEvent(event);
    }
}
