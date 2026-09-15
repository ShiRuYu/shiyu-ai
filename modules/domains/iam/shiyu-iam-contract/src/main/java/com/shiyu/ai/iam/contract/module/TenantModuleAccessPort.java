package com.shiyu.ai.iam.contract.module;

import com.shiyu.ai.kernel.context.TenantId;

/**
 * IAM 提供的租户级业务模块访问契约。
 *
 * <p>调用方只能查询当前租户是否启用模块，不能通过此契约切换进程级模块开关或绕过领域权限。
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
