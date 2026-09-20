package com.shiyu.ai.iam.contract.module;

import com.shiyu.ai.kernel.context.TenantId;

/**
 * 定义 租户 Module Access 领域与外部能力交互的端口契约。
 */
public interface TenantModuleAccessPort {

    /**
     * 判断指定租户是否启用业务模块。
     *
     * @param tenantId 当前租户标识。
     * @param moduleId 规范化业务模块标识。
     * @return 记录为启用时返回 true。
     */
    boolean isEnabled(TenantId tenantId, String moduleId);
}
