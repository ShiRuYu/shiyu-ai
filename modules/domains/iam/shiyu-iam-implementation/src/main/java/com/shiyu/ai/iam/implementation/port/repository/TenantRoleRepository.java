package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.kernel.context.TenantId;

public interface TenantRoleRepository {
    TenantBO selectTenantById(TenantId tenantId);
    RoleBO selectRoleById(Long roleId);
    RoleBO selectEnabledRoleByCode(TenantId tenantId, String roleCode);
    RoleBO selectTenantSuperRole(TenantId tenantId);
    String selectTenantNameById(TenantId tenantId);
}

