package com.shiyu.ai.kernel.context;

import java.util.Objects;
import java.util.UUID;

/**
 * 表示跨请求和异步任务传播的关联标识。
 * @param value 值，表示该记录组件承载的数据。
 */
public record CorrelationId(String value) {

    public CorrelationId {
        Objects.requireNonNull(value, "correlationId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }
    }

    /**
     * {@code random} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static CorrelationId random() {
        return new CorrelationId(UUID.randomUUID().toString());
    }
}
