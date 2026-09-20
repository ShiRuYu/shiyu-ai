package com.shiyu.ai.common.mybatis.tenant;

import com.mybatisflex.core.tenant.TenantManager;

import java.util.function.Supplier;

/**
 * 实现 租户 Query Executor 相关的业务处理、协作逻辑或基础设施能力。
 */
public final class TenantQueryExecutor {

    private TenantQueryExecutor() {}

    /**
     * 执行一次显式授权后的跨租户读取。
     *
     * @param action 跨租户读取动作。
     * @param <T> 返回值类型。
     * @return 读取结果。
     */
    public static <T> T readAcrossTenants(Supplier<T> action) {
        if (action == null) throw new IllegalArgumentException("action is required");
        boolean previous = TenantManager.isIgnoreTenantCondition();
        try {
            TenantManager.ignoreTenantCondition();
            return action.get();
        } finally {
            if (previous) TenantManager.ignoreTenantCondition();
            else TenantManager.restoreTenantCondition();
        }
    }

    /**
     * 执行一次显式授权后的跨租户命令。
     *
     * @param action 跨租户命令。
     */
    public static void runAcrossTenants(Runnable action) {
        if (action == null) throw new IllegalArgumentException("action is required");
        readAcrossTenants(
                () -> {
                    action.run();
                    return null;
                });
    }
}
