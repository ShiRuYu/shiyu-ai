package com.shiyu.ai.knowledge.contract;

import com.shiyu.ai.kernel.context.TenantId;

/**
 * 定义 知识 租户 Provisioning 相关的协作契约和调用边界。
 */
public interface KnowledgeTenantProvisioning {

    /**
     * 初始化指定租户的默认配置。
     *
     * @param tenantId 租户标识，不得为空
     */
    void initializeTenantDefaults(TenantId tenantId);
}
