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
 * 实现 Auth 应用服务用例。
 */
@Service
public class AuthServiceImpl implements AuthService {
    /**
     * authentication 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthAuthenticationUseCase authentication;
    /**
     * permissions 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthPermissionUseCase permissions;
    /**
     * identity 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthIdentityUseCase identity;
    /**
     * sessions 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthSessionUseCase sessions;
    /**
     * recovery 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthRecoveryUseCase recovery;
    /**
     * contextSupport 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthTenantContextSupport contextSupport;

    /**
     * 处理认证serviceimpl。
     *
     * @param authRepository authRepository 参数。
     * @param userRepository userRepository 参数。
     * @param userScopeRoleRepository userScopeRoleRepository 参数。
     * @param tenantRoleRepository tenantRoleRepository 参数。
     * @param tenantRepository tenantRepository 参数。
     * @param menuService menuService 参数。
     * @param captchaService captchaService 参数。
     *
     * @return 处理结果。
     */
    public AuthServiceImpl(
            AuthRepository authRepository,
            UserRepository userRepository,
            UserScopeRoleRepository userScopeRoleRepository,
            TenantRoleRepository tenantRoleRepository,
            TenantRepository tenantRepository,
            MenuService menuService,
            CaptchaService captchaService) {
        this.contextSupport = new AuthTenantContextSupport(tenantRepository, tenantRoleRepository);
        this.authentication =
                new AuthAuthenticationUseCase(
                        userRepository,
                        userScopeRoleRepository,
                        tenantRoleRepository,
                        captchaService,
                        contextSupport);
        this.permissions = new AuthPermissionUseCase(authRepository);
        this.identity =
                new AuthIdentityUseCase(
                        userRepository,
                        userScopeRoleRepository,
                        tenantRoleRepository,
                        tenantRepository,
                        menuService,
                        contextSupport);
        this.sessions = new AuthSessionUseCase();
        this.recovery = new AuthRecoveryUseCase(userRepository, captchaService);
    }

    /**
     * {@code login} 执行当前类型定义的业务操作。
     *
     * @param username 参数值，用于执行当前操作。
     * @param password 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public LoginResponseVO login(String username, String password) {
        return login(username, password, null, null);
    }

    /**
     * {@code login} 执行当前类型定义的业务操作。
     *
     * @param username 参数值，用于执行当前操作。
     * @param password 参数值，用于执行当前操作。
     * @param roleId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public LoginResponseVO login(String username, String password, Long roleId) {
        return login(username, password, roleId, null);
    }

    /**
     * {@code login} 执行当前类型定义的业务操作。
     *
     * @param username 参数值，用于执行当前操作。
     * @param password 参数值，用于执行当前操作。
     * @param roleId 参数值，用于执行当前操作。
     * @param loginIp 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public LoginResponseVO login(String username, String password, Long roleId, String loginIp) {
        return authentication.login(username, password, roleId, loginIp);
    }

    /**
     * {@code getAuthCodes} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param username 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<String> getAuthCodes(ActorContext actor, String username) {
        return permissions.getAuthCodes(actor, username);
    }

    /**
     * {@code getAuthCodesByUserId} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<String> getAuthCodesByUserId(ActorContext actor, UserId userId) {
        return permissions.getAuthCodesByUserId(actor, userId);
    }

    /**
     * {@code refreshToken} 执行当前类型定义的业务操作。
     *
     * @param oldToken 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String refreshToken(String oldToken) {
        return sessions.refreshToken(oldToken);
    }

    /**
     * {@code logout} 执行当前类型定义的业务操作。
     *
     * @param token 参数值，用于执行当前操作。
     */
    @Override
    public void logout(String token) {
        sessions.logout(token);
    }

    /**
     * {@code switchCurrentRole} 执行当前类型定义的业务操作。
     *
     * @param userId 参数值，用于执行当前操作。
     * @param roleId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean switchCurrentRole(Long userId, Long roleId) {
        return identity.switchCurrentRole(userId, roleId);
    }

    /**
     * {@code switchCurrentTenant} 执行当前类型定义的业务操作。
     *
     * @param userId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean switchCurrentTenant(Long userId, TenantId tenantId) {
        return identity.switchCurrentTenant(userId, tenantId);
    }

    /**
     * {@code getUserTenants} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<TenantInfoVO> getUserTenants(ActorContext actor, Long userId) {
        return identity.getUserTenants(actor, userId);
    }

    /**
     * {@code register} 写入或更新当前模块中的业务数据。
     *
     * @param username 参数值，用于执行当前操作。
     * @param password 参数值，用于执行当前操作。
     * @param email 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public LoginResponseVO register(String username, String password, String email) {
        return authentication.register(username, password, email);
    }

    /**
     * {@code codeLogin} 执行当前类型定义的业务操作。
     *
     * @param phone 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     * @param captchaKey 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public LoginResponseVO codeLogin(String phone, String code, String captchaKey) {
        return authentication.codeLogin(phone, code, captchaKey);
    }

    /**
     * {@code forgetPassword} 执行当前类型定义的业务操作。
     *
     * @param email 参数值，用于执行当前操作。
     * @param newPassword 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     * @param captchaKey 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean forgetPassword(
            String email, String newPassword, String code, String captchaKey) {
        return recovery.forgetPassword(email, newPassword, code, captchaKey);
    }

    private Long resolveCurrentTenantId(String extInfo, List<UserScopeRoleBO> assignments) {
        return contextSupport.resolveCurrentTenantId(extInfo, assignments);
    }

    private boolean hasTenantAssignment(List<UserScopeRoleBO> assignments, Long tenantId) {
        return contextSupport.hasTenantAssignment(assignments, tenantId);
    }

    private Long numberValue(Object value) {
        return contextSupport.numberValue(value);
    }

    private boolean isDelegatedTenantContext(
            Map<String, Object> extInfo, List<UserScopeRoleBO> assignments, Long targetTenantId) {
        return contextSupport.isDelegatedTenantContext(extInfo, assignments, targetTenantId);
    }

    private RoleBO findTenantSuperRole(Long tenantId) {
        return contextSupport.findTenantSuperRole(tenantId);
    }

    private boolean isTenantSuperRole(RoleBO role) {
        return contextSupport.isTenantSuperRole(role);
    }

    private Map<String, Object> buildExtInfo(
            String oldExtInfo,
            RoleBO currentRole,
            Long currentTenantId,
            LocalDateTime now,
            String loginIp) {
        return contextSupport.buildExtInfo(oldExtInfo, currentRole, currentTenantId, now, loginIp);
    }

    private List<TenantContextVO> buildSubTenantList(
            List<UserScopeRoleBO> assignments, Long currentTenantId) {
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

    private RoleBO resolveCurrentRoleForTenant(
            Long roleId, List<RoleBO> roles, List<UserScopeRoleBO> assignments, Long tenantId) {
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
