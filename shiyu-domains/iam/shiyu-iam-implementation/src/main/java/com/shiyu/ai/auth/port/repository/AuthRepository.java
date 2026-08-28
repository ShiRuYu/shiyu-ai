package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import java.util.List;

public interface AuthRepository {
    List<String> selectRoleCodesByUserId(UserId userId, TenantId tenantId);
    List<String> selectCodesByUserIdAndRoleCode(UserId userId, TenantId tenantId, String roleCode);
    List<String> selectCodesByRoleCodeAndTenant(String roleCode, TenantId tenantId);
    List<String> selectCodesByUsername(String username, TenantId tenantId);
    List<String> selectCodesByUserId(UserId userId, TenantId currentTenantId);
    List<String> selectCodesByRoleId(Long roleId, TenantId tenantId);
}
