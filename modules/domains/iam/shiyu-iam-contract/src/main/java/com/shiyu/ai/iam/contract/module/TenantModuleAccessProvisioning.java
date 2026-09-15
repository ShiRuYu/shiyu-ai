package com.shiyu.ai.iam.contract.module;

import com.shiyu.ai.kernel.context.TenantId;

/** 为新租户初始化当前进程已装配的业务模块默认状态。 */
public interface TenantModuleAccessProvisioning {

    /**
     * 为指定租户补齐已装配模块的默认启用记录。
     *
     * @param tenantId 目标租户标识；必须与当前租户作用域一致
     */
    void initializeTenantDefaults(TenantId tenantId);
}
