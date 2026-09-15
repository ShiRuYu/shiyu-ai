package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.Collection;

/** 租户业务模块状态仓储，封装 AUTH_TENANT_MODULE 的持久化访问。 */
public interface TenantModuleAccessRepository {

    /**
     * 查询租户的模块启用状态。
     *
     * @param tenantId 当前租户标识。
     * @param moduleId 业务模块标识。
     * @return 状态为 1 时返回 true；不存在或已关闭时返回 false。
     */
    boolean isEnabled(TenantId tenantId, String moduleId);

    /** 补齐指定租户的模块默认启用记录，不覆盖已有禁用状态。 */
    void initializeTenantDefaults(TenantId tenantId, Collection<String> moduleIds);
}
