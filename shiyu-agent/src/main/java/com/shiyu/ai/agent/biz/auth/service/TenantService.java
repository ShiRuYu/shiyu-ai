package com.shiyu.ai.agent.biz.auth.service;

import com.shiyu.ai.agent.domain.bo.TenantBO;

import java.util.List;

public interface TenantService {

    List<TenantBO> getAllTenants();

    TenantBO getTenantById(Long id);

    boolean createTenant(TenantBO tenantBO);

    boolean updateTenant(Long id, TenantBO tenantBO);

    boolean deleteTenant(Long id);
}
