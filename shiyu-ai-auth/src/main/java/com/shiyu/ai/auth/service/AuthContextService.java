package com.shiyu.ai.auth.service;

import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.TenantDO;
import com.shiyu.ai.dal.auth.dataobject.UserDO;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.repository.AuthUserLookupRepository;
import com.shiyu.ai.dal.auth.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Auth-owned lookup boundary used by the Web request-context adapter. */
@Service
@RequiredArgsConstructor
public class AuthContextService {
    private final AuthUserLookupRepository userLookup;
    private final TenantRepository tenantRepository;

    public UserDO user(Long id) { return userLookup.selectUserById(id); }
    public TenantDO tenant(Long id) { return userLookup.selectTenantById(id); }
    public RoleDO role(Long id) { return userLookup.selectRoleById(id); }
    public RoleDO tenantSuperRole(Long tenantId) { return userLookup.selectTenantSuperRole(tenantId); }
    public List<UserScopeRoleDO> workspaceRoles(Long userId) {
        return userLookup.selectUserWorkspaceRoles(userId);
    }
    public List<Long> descendantTenantIds(Long tenantId) {
        return tenantRepository.selectDescendantIds(tenantId);
    }
}
