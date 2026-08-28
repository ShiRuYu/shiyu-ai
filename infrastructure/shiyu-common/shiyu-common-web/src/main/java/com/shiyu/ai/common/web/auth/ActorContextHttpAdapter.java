package com.shiyu.ai.common.web.auth;

import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.kernel.context.TenantScope;

/**
 * Converts the authenticated HTTP thread context into the explicit application context.
 *
 * <p>This is the only bridge from the legacy request context to domain commands. It belongs
 * to the technical web support library so domain-owned HTTP adapters can use it without
 * depending on the application shell.</p>
 */
public final class ActorContextHttpAdapter {

    private ActorContextHttpAdapter() {
    }

    public static ActorContext currentActor() {
        Long tenantId = UserContextHolder.getCurrentTenantId();
        Long userId = UserContextHolder.getUserId();
        if (tenantId == null || tenantId <= 0 || userId == null || userId <= 0) {
            throw new ServiceException("当前租户或用户上下文不存在");
        }
        Long currentRoleId = UserContextHolder.getCurrentRoleId();
        RoleId roleId = currentRoleId == null ? null : new RoleId(currentRoleId);
        return new ActorContext(new TenantId(tenantId), new UserId(userId), roleId,
                UserContextHolder.getCurrentRoleCode(),
                toTenantId(UserContextHolder.getHomeTenantId(), tenantId),
                UserContextHolder.getSwitchMode(), UserContextHolder.isSuperAdmin());
    }

    private static TenantId toTenantId(Long homeTenantId, long currentTenantId) {
        long resolved = homeTenantId == null || homeTenantId <= 0 ? currentTenantId : homeTenantId;
        return new TenantId(resolved);
    }

    /** Returns the actor for optional cross-cutting telemetry, or null for anonymous requests. */
    public static ActorContext currentActorOrNull() {
        try {
            return currentActor();
        } catch (ServiceException ex) {
            return null;
        }
    }

    /** Returns the current actor's tenant as a primitive only at the HTTP edge. */
    public static long tenantId() {
        return currentActor().tenantId().value();
    }

    /** Returns the current actor's user id at the HTTP edge. */
    public static long userId() {
        return currentActor().userId().value();
    }

    /** Returns whether the authenticated actor has platform-admin privileges. */
    public static boolean platformAdmin() {
        return currentActor().platformAdmin();
    }

    /** Optional presentation metadata maintained by the HTTP authentication context. */
    public static Long homeTenantId() {
        return UserContextHolder.getHomeTenantId();
    }

    /** Optional tenant-switching mode maintained by the HTTP authentication context. */
    public static String switchMode() {
        return UserContextHolder.getSwitchMode();
    }

    /**
     * Runs an anonymous-login bootstrap operation with the authenticated actor
     * temporarily installed at the HTTP boundary. Domain/application code still
     * receives explicit parameters and never touches either thread context.
     */
    public static void runWithContext(com.shiyu.ai.common.core.domain.UserContext context,
                                      TenantId tenantId, Runnable action) {
        if (context == null || tenantId == null || action == null) {
            throw new IllegalArgumentException("context, tenantId and action are required");
        }
        com.shiyu.ai.common.core.domain.UserContext previousContext = UserContextHolder.getContext();
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
