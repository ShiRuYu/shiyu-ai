package com.shiyu.ai.auth.port.repository;

import java.util.List;
import java.util.stream.Collectors;

public interface AuthRepository {
    List<String> selectRoleCodesByUserId(Long userId, Long tenantId);
    List<String> selectCodesByUserIdAndRoleCode(Long userId, Long tenantId, String roleCode);
    List<String> selectCodesByRoleCodeAndTenant(String roleCode, Long tenantId);
    List<String> selectCodesByUsername(String username);
    List<String> selectCodesByUserId(Long userId, Long currentTenantId);
    List<String> selectCodesByRoleId(Long roleId);
}
