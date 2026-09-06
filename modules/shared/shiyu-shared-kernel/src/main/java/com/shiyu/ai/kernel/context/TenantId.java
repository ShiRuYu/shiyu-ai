package com.shiyu.ai.kernel.context;

import java.io.Serializable;

/** Strongly typed identifier for an owning tenant. */
public record TenantId(long value) implements Serializable {

    public TenantId {
        if (value <= 0) {
            throw new IllegalArgumentException("tenantId must be positive");
        }
    }
}
