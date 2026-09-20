package com.shiyu.ai.common.event.publisher;

import com.shiyu.ai.common.event.api.InfrastructureEventPublisher;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;

import org.springframework.context.ApplicationEventPublisher;

/**
 * 发布 In Process 事件 相关的领域事件或基础设施消息。
 */
public final class InProcessEventPublisher implements InfrastructureEventPublisher {

    /**
     * 发布器，表示当前对象中的对应属性。
     */
    private final ApplicationEventPublisher publisher;

    /**
     * 执行 In Process 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param publisher 用于完成本次业务处理的 publisher 参数。
     */
    public InProcessEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 发布或发送 In Process 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    @Override
    public void publish(DomainEventEnvelope<?> event) {
        publisher.publishEvent(event);
    }
}
