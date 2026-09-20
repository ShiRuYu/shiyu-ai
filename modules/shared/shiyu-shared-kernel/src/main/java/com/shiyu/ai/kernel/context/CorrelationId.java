package com.shiyu.ai.kernel.context;

import java.util.Objects;
import java.util.UUID;

/**
 * 封装 Correlation Id 相关的不可变数据及其字段约束。
 */
public record CorrelationId(String value) {

    public CorrelationId {
        Objects.requireNonNull(value, "correlationId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }
    }

    /**
     * 执行 Correlation Id 相关业务数据，并返回处理结果。
     *
     * @return 返回 Correlation Id 相关操作生成的结果数据。
     */
    public static CorrelationId random() {
        return new CorrelationId(UUID.randomUUID().toString());
    }
}
