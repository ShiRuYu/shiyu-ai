package com.shiyu.ai.agent.implementation.event.model;

import java.time.Instant;
import java.util.UUID;

/**
 * 表示 Domain 相关的领域事件或异常信息。
 */
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
     * 执行 Domain 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param eventType 用于完成本次业务处理的 eventType 参数。
     */
    protected DomainEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString().replace("-", "");
        this.occurredAt = Instant.now();
        this.eventType = eventType;
    }

    /**
     * 查询 Domain 相关业务数据，并返回处理结果。
     *
     * @return 返回 Domain 相关操作生成的结果数据。
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * 查询 Domain 相关业务数据，并返回处理结果。
     *
     * @return 返回 Domain 相关操作生成的结果数据。
     */
    public Instant getOccurredAt() {
        return occurredAt;
    }

    /**
     * 查询 Domain 相关业务数据，并返回处理结果。
     *
     * @return 返回 Domain 相关操作生成的结果数据。
     */
    public String getEventType() {
        return eventType;
    }
}
