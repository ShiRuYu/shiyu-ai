package com.shiyu.ai.common.foundation.tx.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * 表示 Domain 相关的领域事件或异常信息。
 */
@Getter
public abstract class DomainEvent {

    private final String eventId = UUID.randomUUID().toString();
    private final Instant occurredAt = Instant.now();
}
