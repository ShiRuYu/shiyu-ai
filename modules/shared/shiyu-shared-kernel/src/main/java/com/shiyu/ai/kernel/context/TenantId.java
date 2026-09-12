package com.shiyu.ai.kernel.context;

import java.io.Serializable;

/**
 * 表示请求和领域数据所属的租户唯一标识。
 * @param value 值，表示该记录组件承载的数据。
 */
public record TenantId(long value) implements Serializable {

    public TenantId {
        if (value <= 0) {
            throw new IllegalArgumentException("tenantId must be positive");
        }
    }
}
