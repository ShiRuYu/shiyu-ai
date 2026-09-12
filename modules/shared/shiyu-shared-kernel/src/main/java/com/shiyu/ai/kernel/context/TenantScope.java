package com.shiyu.ai.kernel.context;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 提供租户范围校验和租户上下文切换的通用操作。
 */
public final class TenantScope {

    private static final ThreadLocal<TenantId> CURRENT = new ThreadLocal<>();

    private TenantScope() {}

    /**
     * {@code set} 写入或更新当前模块中的业务数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     */
    public static void set(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        CURRENT.set(tenantId);
    }

    /**
     * {@code current} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static Optional<TenantId> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * {@code require} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static TenantId require() {
        TenantId tenantId = CURRENT.get();
        if (tenantId == null) {
            throw new IllegalStateException(
                    "tenant scope is required for tenant-owned persistence");
        }
        return tenantId;
    }

    /**
     * {@code clear} 执行当前类型定义的业务操作。
     */
    public static void clear() {
        CURRENT.remove();
    }

    /**
     * {@code withTenant} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param action 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
