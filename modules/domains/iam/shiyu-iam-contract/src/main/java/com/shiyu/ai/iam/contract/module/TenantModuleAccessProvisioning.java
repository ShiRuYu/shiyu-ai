package com.shiyu.ai.iam.contract.module;

import com.shiyu.ai.kernel.context.TenantId;

/**
 * 定义 租户 Module Access Provisioning 相关的协作契约和调用边界。
 */
public interface TenantModuleAccessProvisioning {

    /**
     * 为指定租户补齐已装配模块的默认启用记录。
     *
     * @param tenantId 目标租户标识；必须与当前租户作用域一致
     */
    void initializeTenantDefaults(TenantId tenantId);
}
