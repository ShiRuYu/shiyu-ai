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
 * 提供 认证 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @param password 用于完成本次业务处理的 password 参数。
     * @return 返回 认证 相关操作生成的结果数据。
     */
    @Override
    public LoginResponseVO login(String username, String password) {
        return login(username, password, null, null);
    }

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @param password 用于完成本次业务处理的 password 参数。
     * @param roleId 用于定位role的标识。
     * @return 返回 认证 相关操作生成的结果数据。
     */
    @Override
    public LoginResponseVO login(String username, String password, Long roleId) {
        return login(username, password, roleId, null);
    }

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @param password 用于完成本次业务处理的 password 参数。
     * @param roleId 用于定位role的标识。
     * @param loginIp 用于完成本次业务处理的 loginIp 参数。
     * @return 返回 认证 相关操作生成的结果数据。
     */
    @Override
    public LoginResponseVO login(String username, String password, Long roleId, String loginIp) {
        return authentication.login(username, password, roleId, loginIp);
    }

    /**
     * 查询 认证 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param username 用于完成本次业务处理的 username 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<String> getAuthCodes(ActorContext actor, String username) {
        return permissions.getAuthCodes(actor, username);
    }

    /**
     * 查询 认证 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param userId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<String> getAuthCodesByUserId(ActorContext actor, UserId userId) {
        return permissions.getAuthCodesByUserId(actor, userId);
    }

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param oldToken 用于完成本次业务处理的 oldToken 参数。
     * @return 返回 认证 相关操作生成的结果数据。
     */
    @Override
    public String refreshToken(String oldToken) {
        return sessions.refreshToken(oldToken);
    }

    /**
     * 执行 认证 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param token 用于完成本次业务处理的 token 参数。
     */
    @Override
    public void logout(String token) {
        sessions.logout(token);
    }

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @param roleId 用于定位role的标识。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean switchCurrentRole(Long userId, Long roleId) {
        return identity.switchCurrentRole(userId, roleId);
    }

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param userId 当前操作涉及的用户标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean switchCurrentTenant(Long userId, TenantId tenantId) {
        return identity.switchCurrentTenant(userId, tenantId);
    }

    /**
     * 查询 认证 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param userId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<TenantInfoVO> getUserTenants(ActorContext actor, Long userId) {
        return identity.getUserTenants(actor, userId);
    }

    /**
     * 创建或保存 认证 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @param password 用于完成本次业务处理的 password 参数。
     * @param email 用于完成本次业务处理的 email 参数。
     * @return 返回 认证 相关操作生成的结果数据。
     */
    @Override
    public LoginResponseVO register(String username, String password, String email) {
        return authentication.register(username, password, email);
    }

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param phone 用于完成本次业务处理的 phone 参数。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param captchaKey 用于完成本次业务处理的 captchaKey 参数。
     * @return 返回 认证 相关操作生成的结果数据。
     */
    @Override
    public LoginResponseVO codeLogin(String phone, String code, String captchaKey) {
        return authentication.codeLogin(phone, code, captchaKey);
    }

    /**
     * 执行 认证 相关业务数据，并返回处理结果。
     *
     * @param email 用于完成本次业务处理的 email 参数。
     * @param newPassword 用于完成本次业务处理的 newPassword 参数。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param captchaKey 用于完成本次业务处理的 captchaKey 参数。
     * @return 返回本次条件判断是否成立。
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
