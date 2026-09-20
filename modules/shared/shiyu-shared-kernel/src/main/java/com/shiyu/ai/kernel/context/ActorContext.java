package com.shiyu.ai.kernel.context;

import com.shiyu.ai.kernel.error.DomainAccessDeniedException;

import java.io.Serializable;
import java.util.Objects;

/**
 * 封装 Actor 相关的不可变数据及其字段约束。
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
