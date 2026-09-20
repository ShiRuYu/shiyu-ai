package com.shiyu.ai.kernel.context;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 实现 租户 Scope 相关的业务处理、协作逻辑或基础设施能力。
 */
public final class TenantScope {

    private static final ThreadLocal<TenantId> CURRENT = new ThreadLocal<>();

    private TenantScope() {}

    /**
     * 更新或设置 租户 Scope 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     */
    public static void set(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        CURRENT.set(tenantId);
    }

    /**
     * 获取当前 租户 Scope 相关业务数据，并返回处理结果。
     *
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    public static Optional<TenantId> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * 获取并校验 租户 Scope 相关业务数据，并返回处理结果。
     *
     * @return 返回 租户 Scope 相关操作生成的结果数据。
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
     * 校验命令声明的租户与当前执行作用域一致。
     *
     * @param tenantId 命令声明的目标租户。
     * @return 当前作用域中的租户。
     */
    public static TenantId requireMatches(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        TenantId current = require();
        if (!current.equals(tenantId)) {
            throw new IllegalArgumentException(
                    "tenantId does not match the current tenant scope");
        }
        return current;
    }

    /**
     * 当调用线程已经绑定租户时校验命令租户；未绑定时不创建新的作用域，
     * 由具体持久化入口按用途决定是否拒绝。
     *
     * @param tenantId 命令声明的目标租户。
     */
    public static void requireMatchesIfBound(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        current().ifPresent(current -> {
            if (!current.equals(tenantId)) {
                throw new IllegalArgumentException(
                        "tenantId does not match the current tenant scope");
            }
        });
    }

    /**
     * 删除或移除 租户 Scope 相关业务操作，并维护必要的状态和协作关系。
     */
    public static void clear() {
        CURRENT.remove();
    }

    /**
     * 执行 租户 Scope 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param action 用于完成本次业务处理的 action 参数。
     * @return 返回 租户 Scope 相关操作生成的结果数据。
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
