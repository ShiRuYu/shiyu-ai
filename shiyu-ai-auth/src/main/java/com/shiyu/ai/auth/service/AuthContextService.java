package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.api.response.AuthRoleResponse;
import com.shiyu.ai.auth.api.response.AuthTenantResponse;
import com.shiyu.ai.auth.api.response.AuthUserResponse;
import com.shiyu.ai.auth.api.response.AuthScopeRoleResponse;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.auth.port.repository.AuthUserLookupRepository;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Auth-owned lookup boundary used by the Web request-context adapter. */
@Service
@RequiredArgsConstructor
public class AuthContextService {
    private final AuthUserLookupRepository userLookup;
    private final TenantRepository tenantRepository;

    public AuthUserResponse user(Long id) { return MapstructUtils.convert(userLookup.selectUserById(id), AuthUserResponse.class); }
    public AuthTenantResponse tenant(Long id) { return MapstructUtils.convert(userLookup.selectTenantById(id), AuthTenantResponse.class); }
    public AuthRoleResponse role(Long id) { return MapstructUtils.convert(userLookup.selectRoleById(id), AuthRoleResponse.class); }
    public AuthRoleResponse tenantSuperRole(Long tenantId) { return MapstructUtils.convert(userLookup.selectTenantSuperRole(tenantId), AuthRoleResponse.class); }
    public List<AuthScopeRoleResponse> workspaceRoles(Long userId) {
        return MapstructUtils.convert(userLookup.selectUserWorkspaceRoles(userId), AuthScopeRoleResponse.class);
    }
    public List<Long> descendantTenantIds(Long tenantId) {
        return tenantRepository.selectDescendantIds(tenantId);
    }
}
