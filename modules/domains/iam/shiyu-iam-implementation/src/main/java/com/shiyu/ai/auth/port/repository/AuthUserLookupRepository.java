package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;
import java.util.Set;

public interface AuthUserLookupRepository {
    UserBO selectUserById(Long userId);
    boolean updateUserExtInfo(Long userId, String extInfo);
    List<UserScopeRoleBO> selectUserScopeRoles(Long userId);
    RoleBO selectRoleById(Long roleId);
    RoleBO selectTenantSuperRole(TenantId tenantId);
    List<RoleBO> selectRolesByIds(Set<Long> roleIds);
    TenantBO selectTenantById(TenantId tenantId);
}
