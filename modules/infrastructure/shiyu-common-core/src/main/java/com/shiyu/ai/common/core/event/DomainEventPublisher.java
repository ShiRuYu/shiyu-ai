package com.shiyu.ai.common.core.event;

import com.shiyu.ai.kernel.event.DomainEventEnvelope;

/** Stable event publication port used by business modules. */
@FunctionalInterface
public interface DomainEventPublisher {

    void publish(DomainEventEnvelope<?> event);
}
