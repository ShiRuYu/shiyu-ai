package com.shiyu.ai.kernel.context;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Explicit persistence tenant scope used by technical adapters such as the
 * MyBatis tenant factory. HTTP authentication code is responsible for
 * binding it for the request lifetime; domain and application code must pass
 * {@link TenantId} explicitly instead of reading this scope.
 */
public final class TenantScope {

    private static final ThreadLocal<TenantId> CURRENT = new ThreadLocal<>();

    private TenantScope() {
    }

    public static void set(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        CURRENT.set(tenantId);
    }

    public static Optional<TenantId> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static TenantId require() {
        TenantId tenantId = CURRENT.get();
        if (tenantId == null) {
            throw new IllegalStateException("tenant scope is required for tenant-owned persistence");
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static <T> T withTenant(TenantId tenantId, Supplier<T> action) {
        if (action == null) {
            throw new IllegalArgumentException("action is required");
        }
        TenantId previous = CURRENT.get();
        set(tenantId);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                clear();
            } else {
                set(previous);
            }
        }
    }
}
