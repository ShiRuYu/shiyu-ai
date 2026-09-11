package com.shiyu.ai.common.core.event;

import com.shiyu.ai.kernel.event.DomainEventEnvelope;

import org.springframework.context.ApplicationEventPublisher;

/** Spring application-event implementation used by local deployments. */
public final class InProcessEventPublisher implements InfrastructureEventPublisher {

    private final ApplicationEventPublisher publisher;

    public InProcessEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(DomainEventEnvelope<?> event) {
        publisher.publishEvent(event);
    }
}
