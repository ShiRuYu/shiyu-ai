package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import java.util.List;
import java.util.Set;

public interface AuthUserLookupRepository {
    UserBO selectUserById(Long userId);
    boolean updateUserExtInfo(Long userId, String extInfo);
    List<UserScopeRoleBO> selectUserWorkspaceRoles(Long userId);
    RoleBO selectRoleById(Long roleId);
    RoleBO selectTenantSuperRole(Long tenantId);
    List<RoleBO> selectRolesByIds(Set<Long> roleIds);
    TenantBO selectTenantById(Long tenantId);
}
