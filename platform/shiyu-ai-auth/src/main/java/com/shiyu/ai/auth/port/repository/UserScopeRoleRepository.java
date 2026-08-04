package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import java.util.List;

public interface UserScopeRoleRepository {
    List<UserScopeRoleBO> selectByUserId(Long userId);
    void insert(UserScopeRoleBO userWorkspaceRole);
    void deleteByUserIdAndTenantId(Long userId, Long tenantId);
    void deleteByUserIdRoleIdAndTenantId(Long userId, Long roleId, Long tenantId);
}
