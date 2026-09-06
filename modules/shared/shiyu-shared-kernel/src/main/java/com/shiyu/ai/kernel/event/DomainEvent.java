package com.shiyu.ai.kernel.event;

/** Public event payload exposed by a bounded-context contract. */
@FunctionalInterface
public interface DomainEvent {

    String eventType();
}
