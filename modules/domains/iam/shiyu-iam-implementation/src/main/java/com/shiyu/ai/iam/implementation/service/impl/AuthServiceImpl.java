package com.shiyu.ai.iam.implementation.service.impl;

import com.shiyu.ai.iam.implementation.application.authentication.AuthAuthenticationUseCase;
import com.shiyu.ai.iam.implementation.application.authorization.AuthPermissionUseCase;
import com.shiyu.ai.iam.implementation.application.identity.AuthIdentityUseCase;
import com.shiyu.ai.iam.implementation.application.identity.AuthTenantContextSupport;
import com.shiyu.ai.iam.implementation.application.recovery.AuthRecoveryUseCase;
import com.shiyu.ai.iam.implementation.application.session.AuthSessionUseCase;
import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.iam.implementation.port.repository.AuthRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRoleRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.iam.implementation.service.AuthService;
import com.shiyu.ai.iam.implementation.service.CaptchaService;
import com.shiyu.ai.iam.implementation.service.MenuService;
import com.shiyu.ai.iam.implementation.vo.LoginResponseVO;
import com.shiyu.ai.iam.implementation.vo.TenantContextVO;
import com.shiyu.ai.iam.implementation.vo.TenantInfoVO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Stable IAM facade. Each public entry point delegates to one focused use-case
 * component while the facade preserves the existing service contract.
 */
@Service
public class AuthServiceImpl implements AuthService {
    private final AuthAuthenticationUseCase authentication;
    private final AuthPermissionUseCase permissions;
    private final AuthIdentityUseCase identity;
    private final AuthSessionUseCase sessions;
    private final AuthRecoveryUseCase recovery;
    private final AuthTenantContextSupport contextSupport;

    /**
     * Kept as the compatibility constructor used by existing Spring wiring and
     * unit tests. The focused use cases receive their repositories explicitly.
     */
    public AuthServiceImpl(AuthRepository authRepository, UserRepository userRepository,
                           UserScopeRoleRepository userScopeRoleRepository,
                           TenantRoleRepository tenantRoleRepository,
                           TenantRepository tenantRepository,
                           MenuService menuService,
                           CaptchaService captchaService) {
        this.contextSupport = new AuthTenantContextSupport(tenantRepository, tenantRoleRepository);
        this.authentication = new AuthAuthenticationUseCase(
                userRepository, userScopeRoleRepository, tenantRoleRepository,
                captchaService, contextSupport);
        this.permissions = new AuthPermissionUseCase(authRepository);
        this.identity = new AuthIdentityUseCase(
                userRepository, userScopeRoleRepository, tenantRoleRepository,
                tenantRepository, menuService, contextSupport);
        this.sessions = new AuthSessionUseCase();
        this.recovery = new AuthRecoveryUseCase(userRepository, captchaService);
    }

    @Override
    public LoginResponseVO login(String username, String password) {
        return login(username, password, null, null);
    }

    @Override
    public LoginResponseVO login(String username, String password, Long roleId) {
        return login(username, password, roleId, null);
    }

    @Override
    public LoginResponseVO login(String username, String password, Long roleId, String loginIp) {
        return authentication.login(username, password, roleId, loginIp);
    }

    @Override
    public List<String> getAuthCodes(ActorContext actor, String username) {
        return permissions.getAuthCodes(actor, username);
    }

    @Override
    public List<String> getAuthCodesByUserId(ActorContext actor, UserId userId) {
        return permissions.getAuthCodesByUserId(actor, userId);
    }

    @Override
    public String refreshToken(String oldToken) {
        return sessions.refreshToken(oldToken);
    }

    @Override
    public void logout(String token) {
        sessions.logout(token);
    }

    @Override
    public boolean switchCurrentRole(Long userId, Long roleId) {
        return identity.switchCurrentRole(userId, roleId);
    }

    @Override
    public boolean switchCurrentTenant(Long userId, TenantId tenantId) {
        return identity.switchCurrentTenant(userId, tenantId);
    }

    @Override
    public List<TenantInfoVO> getUserTenants(ActorContext actor, Long userId) {
        return identity.getUserTenants(actor, userId);
    }

    @Override
    public LoginResponseVO register(String username, String password, String email) {
        return authentication.register(username, password, email);
    }

    @Override
    public LoginResponseVO codeLogin(String phone, String code, String captchaKey) {
        return authentication.codeLogin(phone, code, captchaKey);
    }

    @Override
    public boolean forgetPassword(String email, String newPassword, String code, String captchaKey) {
        return recovery.forgetPassword(email, newPassword, code, captchaKey);
    }

    // Compatibility bridges for behavior tests that exercise the old private helper names.
    private Long resolveCurrentTenantId(String extInfo, List<UserScopeRoleBO> assignments) {
        return contextSupport.resolveCurrentTenantId(extInfo, assignments);
    }

    private boolean hasTenantAssignment(List<UserScopeRoleBO> assignments, Long tenantId) {
        return contextSupport.hasTenantAssignment(assignments, tenantId);
    }

    private Long numberValue(Object value) {
        return contextSupport.numberValue(value);
    }

    private boolean isDelegatedTenantContext(Map<String, Object> extInfo,
                                             List<UserScopeRoleBO> assignments,
                                             Long targetTenantId) {
        return contextSupport.isDelegatedTenantContext(extInfo, assignments, targetTenantId);
    }

    private RoleBO findTenantSuperRole(Long tenantId) {
        return contextSupport.findTenantSuperRole(tenantId);
    }

    private boolean isTenantSuperRole(RoleBO role) {
        return contextSupport.isTenantSuperRole(role);
    }

    private Map<String, Object> buildExtInfo(String oldExtInfo, RoleBO currentRole,
                                             Long currentTenantId, LocalDateTime now,
                                             String loginIp) {
        return contextSupport.buildExtInfo(oldExtInfo, currentRole, currentTenantId, now, loginIp);
    }

    private List<TenantContextVO> buildSubTenantList(List<UserScopeRoleBO> assignments,
                                                      Long currentTenantId) {
        return contextSupport.buildSubTenantList(assignments, currentTenantId);
    }

    private List<TenantInfoVO> buildTenantList(List<UserScopeRoleBO> assignments) {
        return contextSupport.buildTenantList(assignments);
    }

    private List<TenantInfoVO> buildScopedTenantList(Long scopeRootTenantId, Long returnTenantId) {
        return contextSupport.buildScopedTenantList(scopeRootTenantId, returnTenantId);
    }

    private String buildTenantPath(Long tenantId) {
        return contextSupport.buildTenantPath(tenantId);
    }

    private RoleBO resolveCurrentRoleForTenant(Long roleId, List<RoleBO> roles,
                                                List<UserScopeRoleBO> assignments, Long tenantId) {
        return contextSupport.resolveCurrentRoleForTenant(roleId, roles, assignments, tenantId);
    }

    private void assignDefaultTenantScopeRole(Long userId) {
        authentication.assignDefaultTenantScopeRole(userId);
    }

    private Map<String, Object> parseExtInfo(String extInfo) {
        return contextSupport.parseExtInfo(extInfo);
    }

    private boolean isActiveAssignment(UserScopeRoleBO assignment) {
        return contextSupport.isActiveAssignment(assignment);
    }

    private boolean isActiveTenant(TenantBO tenant) {
        return contextSupport.isActiveTenant(tenant);
    }
}
