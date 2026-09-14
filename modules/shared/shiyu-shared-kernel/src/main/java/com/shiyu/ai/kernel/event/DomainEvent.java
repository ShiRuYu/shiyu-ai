package com.shiyu.ai.kernel.event;

/**
 * DomainEvent 领域事件，描述共享内核领域相关业务状态变化。
 */
@FunctionalInterface
public interface DomainEvent {

    /**
     * 执行 {@code eventType} 定义的接口操作。
     *
     * @return 操作结果。
     */
    String eventType();
}
