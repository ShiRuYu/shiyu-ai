package com.shiyu.ai.kernel.context;

import com.shiyu.ai.kernel.error.DomainAccessDeniedException;

import java.io.Serializable;
import java.util.Objects;

/**
 * 承载 Actor 执行上下文。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param userId 用户标识，表示该记录组件承载的数据。
 * @param activeRoleId activeRoleId 属性，表示该记录组件承载的数据。
 * @param activeRoleCode activeRoleCode 属性，表示该记录组件承载的数据。
 * @param homeTenantId homeTenantId 属性，表示该记录组件承载的数据。
 * @param switchMode switchMode 属性，表示该记录组件承载的数据。
 * @param platformAdmin platformAdmin 属性，表示该记录组件承载的数据。
 */
public record ActorContext(
        TenantId tenantId,
        UserId userId,
        RoleId activeRoleId,
        String activeRoleCode,
        TenantId homeTenantId,
        String switchMode,
        boolean platformAdmin)
        implements Serializable {

    public ActorContext(TenantId tenantId, UserId userId, boolean platformAdmin) {
        this(tenantId, userId, null, null, tenantId, null, platformAdmin);
    }

    public ActorContext(
            TenantId tenantId, UserId userId, RoleId activeRoleId, boolean platformAdmin) {
        this(tenantId, userId, activeRoleId, null, tenantId, null, platformAdmin);
    }

    public ActorContext {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
    }

    /**
     * 处理parent超级管理员切换。
     *
     * @return 判断结果。
     */
    public boolean parentSuperAdminSwitch() {
        return "PARENT_SUPER_ADMIN".equals(switchMode);
    }

    public void requireTenant(TenantId resourceTenantId) {
        Objects.requireNonNull(resourceTenantId, "resourceTenantId must not be null");
        if (!tenantId.equals(resourceTenantId)) {
            throw new DomainAccessDeniedException(
                    "TENANT_MISMATCH",
                    "The requested resource does not belong to the actor tenant");
        }
    }
}
