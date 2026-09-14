package com.shiyu.ai.common.web.auth;

import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;

/**
 * 将 HTTP 认证上下文转换为领域 ActorContext。
 */
public final class ActorContextHttpAdapter {

    private ActorContextHttpAdapter() {}

    /**
     * {@code currentActor} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static ActorContext currentActor() {
        Long tenantId = UserContextHolder.getCurrentTenantId();
        Long userId = UserContextHolder.getUserId();
        if (tenantId == null || tenantId <= 0 || userId == null || userId <= 0) {
            throw new ServiceException("当前租户或用户上下文不存在");
        }
        Long currentRoleId = UserContextHolder.getCurrentRoleId();
        RoleId roleId = currentRoleId == null ? null : new RoleId(currentRoleId);
        return new ActorContext(
                new TenantId(tenantId),
                new UserId(userId),
                roleId,
                UserContextHolder.getCurrentRoleCode(),
                toTenantId(UserContextHolder.getHomeTenantId(), tenantId),
                UserContextHolder.getSwitchMode(),
                UserContextHolder.isSuperAdmin());
    }

    private static TenantId toTenantId(Long homeTenantId, long currentTenantId) {
        long resolved = homeTenantId == null || homeTenantId <= 0 ? currentTenantId : homeTenantId;
        return new TenantId(resolved);
    }

    /**
     * 处理currentactorornull。
     *
     * @return 处理结果。
     */
    public static ActorContext currentActorOrNull() {
        try {
            return currentActor();
        } catch (ServiceException ex) {
            return null;
        }
    }

    /**
     * 处理租户标识。
     *
     * @return 受影响的记录数或生成的序号。
     */
    public static long tenantId() {
        return currentActor().tenantId().value();
    }

    /**
     * 处理用户标识。
     *
     * @return 受影响的记录数或生成的序号。
     */
    public static long userId() {
        return currentActor().userId().value();
    }

    /**
     * 处理平台管理员。
     *
     * @return 判断结果。
     */
    public static boolean platformAdmin() {
        return currentActor().platformAdmin();
    }

    /**
     * 处理home租户标识。
     *
     * @return 受影响的记录数或生成的序号。
     */
    public static Long homeTenantId() {
        return UserContextHolder.getHomeTenantId();
    }

    /**
     * 处理切换模式。
     *
     * @return 处理结果。
     */
    public static String switchMode() {
        return UserContextHolder.getSwitchMode();
    }

    /**
     * 在指定上下文中执行任务。
     *
     * @param context context 参数。
     * @param tenantId 租户标识。
     * @param action action 参数。
     */
    public static void runWithContext(
            com.shiyu.ai.common.core.domain.UserContext context,
            TenantId tenantId,
            Runnable action) {
        if (context == null || tenantId == null || action == null) {
            throw new IllegalArgumentException("context, tenantId and action are required");
        }
        com.shiyu.ai.common.core.domain.UserContext previousContext =
                UserContextHolder.getContext();
        TenantId previousTenant = TenantScope.current().orElse(null);
        try {
            UserContextHolder.setContext(context);
            TenantScope.set(tenantId);
            action.run();
        } finally {
            if (previousContext == null) UserContextHolder.clearContext();
            else UserContextHolder.setContext(previousContext);
            if (previousTenant == null) TenantScope.clear();
            else TenantScope.set(previousTenant);
        }
    }
}
