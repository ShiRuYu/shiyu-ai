package com.shiyu.ai.common.core.tx.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * {@code DomainEvent} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Getter
public abstract class DomainEvent {

    private final String eventId = UUID.randomUUID().toString();
    private final Instant occurredAt = Instant.now();
}
