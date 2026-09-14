package com.shiyu.ai.agent.implementation.event.model;

import java.time.Instant;
import java.util.UUID;

/** 领域事件基类 */
public abstract class DomainEvent {

    /**
     * eventId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String eventId;
    /**
     * occurredAt 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Instant occurredAt;
    /**
     * eventType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String eventType;

    /**
     * {@code DomainEvent} 创建并初始化当前类型实例。
     *
     * @param eventType 参数值，用于执行当前操作。
     */
    protected DomainEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString().replace("-", "");
        this.occurredAt = Instant.now();
        this.eventType = eventType;
    }

    /**
     * {@code getEventId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * {@code getOccurredAt} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Instant getOccurredAt() {
        return occurredAt;
    }

    /**
     * {@code getEventType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getEventType() {
        return eventType;
    }
}
