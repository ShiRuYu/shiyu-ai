package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.AuthCodeBO;
import com.shiyu.ai.auth.domain.model.RoleScopeAuthCodeBO;
import com.shiyu.ai.auth.domain.model.TenantAuthCodeBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/** Persistence port for authorization codes and tenant/role assignments. */
public interface AuthCodeRepository {
    List<AuthCodeBO> selectByTenantId(TenantId tenantId);
    List<AuthCodeBO> selectByRoleIdAndTenantId(Long roleId, TenantId tenantId);
    AuthCodeBO selectById(Long id);
    AuthCodeBO insert(AuthCodeBO code);
    void update(AuthCodeBO code);
    boolean existsByCode(String code, Long excludeId);
    boolean isAvailable(Long authCodeId, TenantId tenantId);
    List<AuthCodeBO> selectAvailableByIds(List<Long> ids, TenantId tenantId);
    boolean hasRoleAssignments(Long authCodeId);
    void insertTenantCode(TenantAuthCodeBO assignment);
    void deleteTenantCode(TenantId tenantId, Long authCodeId);
    long countActiveTenantLinks(Long authCodeId);
    void insertRoleAssignments(List<RoleScopeAuthCodeBO> assignments);
    void deleteRoleAssignments(Long roleId, TenantId tenantId, Long authCodeId);
}
