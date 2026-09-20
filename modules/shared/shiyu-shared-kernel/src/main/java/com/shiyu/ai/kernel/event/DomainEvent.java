package com.shiyu.ai.kernel.event;

/**
 * 表示 Domain 相关的领域事件或异常信息。
 */
@FunctionalInterface
public interface DomainEvent {

    /**
     * 执行 Domain 相关业务数据，并返回处理结果。
     *
     * @return 返回 Domain 相关操作生成的结果数据。
     */
    String eventType();
}
