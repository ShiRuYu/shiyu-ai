package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.AuthCodeBO;
import com.shiyu.ai.auth.domain.model.RoleScopeAuthCodeBO;
import com.shiyu.ai.auth.domain.model.TenantAuthCodeBO;

import java.util.List;

/** Persistence port for authorization codes and tenant/role assignments. */
public interface AuthCodeRepository {
    List<AuthCodeBO> selectByTenantId(Long tenantId);
    List<AuthCodeBO> selectByRoleIdAndTenantId(Long roleId, Long tenantId);
    AuthCodeBO selectById(Long id);
    AuthCodeBO insert(AuthCodeBO code);
    void update(AuthCodeBO code);
    boolean existsByCode(String code, Long excludeId);
    boolean isAvailable(Long authCodeId, Long tenantId);
    List<AuthCodeBO> selectAvailableByIds(List<Long> ids, Long tenantId);
    boolean hasRoleAssignments(Long authCodeId);
    void insertTenantCode(TenantAuthCodeBO assignment);
    void deleteTenantCode(Long tenantId, Long authCodeId);
    long countActiveTenantLinks(Long authCodeId);
    void insertRoleAssignments(List<RoleScopeAuthCodeBO> assignments);
    void deleteRoleAssignments(Long roleId, Long tenantId, Long authCodeId);
}
