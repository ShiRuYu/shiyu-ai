package com.shiyu.ai.knowledge.contract;

import com.shiyu.ai.kernel.context.TenantId;

/**
 * 知识租户配置接口，负责初始化知识模块的租户默认配置。
 */
public interface KnowledgeTenantProvisioning {

    /**
     * 初始化指定租户的默认配置。
     *
     * @param tenantId 租户标识，不得为空
     */
    void initializeTenantDefaults(TenantId tenantId);
}
