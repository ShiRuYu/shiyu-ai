package com.shiyu.ai.auth.service;

import com.shiyu.ai.dal.auth.bo.TenantBO;

import java.util.List;

/**
 * Tenant 接口
 */

public interface TenantService {

    /**
     * Get All Tenants
     * @return 处理结果
     */
    List<TenantBO> getAllTenants();

    /**
     * Get Tenant By Id
     * @return 处理结果
     */
    TenantBO getTenantById(Long id);

    /**
     * Create Tenant
     * @param TenantBO TenantBO
     * @return 处理结果
     */
    boolean createTenant(TenantBO tenantBO);

    /**
     * Update Tenant
     * @param TenantBO TenantBO
     * @return 处理结果
     */
    boolean updateTenant(Long id, TenantBO tenantBO);

    /**
     * Delete Tenant
     * @return 处理结果
     */
    boolean deleteTenant(Long id);
}
