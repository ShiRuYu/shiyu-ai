package com.shiyu.ai.common.core.event;

/** Replaceable transport boundary for durable cross-module events. */
@FunctionalInterface
public interface InfrastructureEventPublisher extends DomainEventPublisher {
}
