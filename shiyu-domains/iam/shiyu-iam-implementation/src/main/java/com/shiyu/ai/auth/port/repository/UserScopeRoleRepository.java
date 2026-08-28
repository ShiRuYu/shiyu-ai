package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface UserScopeRoleRepository {
    List<UserScopeRoleBO> selectByUserId(Long userId);
    default List<UserScopeRoleBO> selectByUserIds(List<Long> userIds) {
        return userIds == null ? List.of() : userIds.stream().flatMap(id -> selectByUserId(id).stream()).toList();
    }
    void insert(UserScopeRoleBO userScopeRole);
    void deleteByUserIdAndTenantId(Long userId, TenantId tenantId);
    void deleteByUserIdRoleIdAndTenantId(Long userId, Long roleId, TenantId tenantId);
}
