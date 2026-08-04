package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;

public interface TenantRoleRepository {
    TenantBO selectTenantById(Long tenantId);
    RoleBO selectRoleById(Long roleId);
    RoleBO selectTenantSuperRole(Long tenantId);
    String selectTenantNameById(Long tenantId);
}
