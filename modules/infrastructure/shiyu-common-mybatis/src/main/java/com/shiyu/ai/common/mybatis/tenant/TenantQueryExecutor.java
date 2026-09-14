package com.shiyu.ai.common.mybatis.tenant;

import com.mybatisflex.core.tenant.TenantManager;

import java.util.function.Supplier;

/**
 * 受控执行跨租户查询，统一保存和恢复 MyBatis-Flex 的忽略标志。
 *
 * <p>调用方仍必须在进入此执行器前完成业务授权，并在查询中显式写出目标范围。
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
