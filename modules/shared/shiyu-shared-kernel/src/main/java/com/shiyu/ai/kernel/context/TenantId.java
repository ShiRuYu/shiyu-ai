package com.shiyu.ai.kernel.context;

import java.io.Serializable;

/**
 * 封装 租户 Id 相关的不可变数据及其字段约束。
 */
public record TenantId(long value) implements Serializable {

    public TenantId {
        if (value <= 0) {
            throw new IllegalArgumentException("tenantId must be positive");
        }
    }
}
