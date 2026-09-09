package com.shiyu.ai.iam.implementation.application.authentication;

import com.mybatisflex.core.tenant.TenantManager;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.utils.PasswordUtils;
import com.shiyu.ai.iam.implementation.application.identity.AuthTenantContextSupport;
import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.iam.implementation.port.repository.TenantRoleRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.iam.implementation.service.CaptchaService;
import com.shiyu.ai.iam.implementation.utils.SaTokenHelper;
import com.shiyu.ai.iam.implementation.vo.LoginResponseVO;
import com.shiyu.ai.iam.implementation.vo.TenantContextVO;
import com.shiyu.ai.iam.implementation.vo.TenantInfoVO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/** Password, captcha and registration authentication use cases. */
@Slf4j
public final class AuthAuthenticationUseCase {
    private final UserRepository userRepository;
    private final UserScopeRoleRepository userScopeRoleRepository;
    private final TenantRoleRepository tenantRoleRepository;
    private final CaptchaService captchaService;
    private final AuthTenantContextSupport contextSupport;

    public AuthAuthenticationUseCase(UserRepository userRepository,
                                     UserScopeRoleRepository userScopeRoleRepository,
                                     TenantRoleRepository tenantRoleRepository,
                                     CaptchaService captchaService,
                                     AuthTenantContextSupport contextSupport) {
        this.userRepository = userRepository;
        this.userScopeRoleRepository = userScopeRoleRepository;
        this.tenantRoleRepository = tenantRoleRepository;
        this.captchaService = captchaService;
        this.contextSupport = contextSupport;
    }

    public LoginResponseVO login(String username, String password, Long roleId, String loginIp) {
        log.info("用户登录开始, usernamePresent={}", username != null);
        try {
            UserBO user = userRepository.selectActiveUserByUsername(username);
            if (user == null) {
                log.warn("用户登录失败, reason=USER_NOT_FOUND");
                return null;
            }
            if (user.getStatus() == null || user.getStatus() != 1) {
                log.warn("用户登录失败, reason=USER_DISABLED");
                return null;
            }
            if (!PasswordUtils.matches(password, user.getPassword())) {
                log.warn("用户登录失败, reason=INVALID_PASSWORD");
                return null;
            }
            return completeLogin(user, roleId, loginIp);
        } catch (Exception e) {
            log.error("用户登录异常, usernamePresent={}, errorType={}, errorMessageLength={}",
                    username != null, e.getClass().getSimpleName(), valueLength(e.getMessage()));
            return null;
        }
    }

    /** Completes tenant/role context construction and token issuance after credentials pass. */
    public LoginResponseVO completeLogin(UserBO user, Long roleId, String loginIp) {
        return TenantManager.withoutTenantCondition(() -> completeLoginWithoutTenantFilter(user, roleId, loginIp));
    }

    private LoginResponseVO completeLoginWithoutTenantFilter(UserBO user, Long roleId, String loginIp) {
        try {
            if (user == null || user.getId() == null) {
                return null;
            }
            List<RoleBO> roles = userRepository.selectRolesByUserId(user.getId());
            List<UserScopeRoleBO> assignments = userScopeRoleRepository.selectByUserId(user.getId());
            Map<String, Object> savedExtInfo = contextSupport.parseExtInfo(user.getExtInfo());
            Long currentTenantId = contextSupport.resolveCurrentTenantId(user.getExtInfo(), assignments);
            if (currentTenantId == null || currentTenantId <= 0) {
                log.warn("登录失败，用户没有有效的租户上下文, userIdPresent={}", user.getId() != null);
                return null;
            }
            Long homeTenantId = contextSupport.numberValue(savedExtInfo.get("homeTenantId"));
            if (homeTenantId == null) {
                homeTenantId = currentTenantId;
            }
            Set<Long> currentRoleIds = assignments.stream()
                    .filter(item -> currentTenantId.equals(item.getTenantId())
                            && contextSupport.isActiveAssignment(item))
                    .map(UserScopeRoleBO::getRoleId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            roles = roles.stream()
                    .filter(role -> currentRoleIds.contains(role.getId()))
                    .toList();
            RoleBO currentRole = contextSupport.resolveCurrentRoleForTenant(
                    roleId, roles, assignments, currentTenantId);
            if (currentRole == null && "PARENT_SUPER_ADMIN".equals(savedExtInfo.get("switchMode"))) {
                RoleBO delegatedRole = contextSupport.findTenantSuperRole(currentTenantId);
                if (delegatedRole != null) {
                    currentRole = delegatedRole;
                    roles = new ArrayList<>(roles);
                    roles.add(currentRole);
                }
            }
            TenantBO tenant = tenantRoleRepository.selectTenantById(new com.shiyu.ai.kernel.context.TenantId(currentTenantId));
            String tenantName = tenant == null ? null : tenant.getName();
            LocalDateTime now = LocalDateTime.now();
            String resolvedLoginIp = loginIp == null || loginIp.isBlank() ? "unknown" : loginIp;
            Map<String, Object> extInfoMap = contextSupport.buildExtInfo(
                    user.getExtInfo(), currentRole, currentTenantId, now, resolvedLoginIp);
            extInfoMap.put("homeTenantId", homeTenantId);
            extInfoMap.putIfAbsent("switchMode", "NORMAL");
            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            String switchMode = (String) extInfoMap.getOrDefault("switchMode", "NORMAL");
            if (!userRepository.update(user)) {
                log.warn("登录失败，最后登录信息持久化被拒绝, userIdPresent={}", user.getId() != null);
                return null;
            }
            SaTokenHelper helper = SaTokenHelper.getInstance();
            String accessToken = helper.loginWithKickout(user.getId());
            SaTokenHelper.clearUserContextSession();
            long timeout = helper.getTokenTimeout();
            List<TenantContextVO> subTenants = contextSupport.buildSubTenantList(assignments, currentTenantId);
            List<TenantInfoVO> tenantList = contextSupport.buildTenantList(assignments);
            final Long resolvedHomeTenantId = homeTenantId;
            boolean homeTenantSuperAdmin = resolvedHomeTenantId != null
                    && assignments != null
                    && assignments.stream()
                    .filter(item -> resolvedHomeTenantId.equals(item.getTenantId())
                            && contextSupport.isActiveAssignment(item))
                    .map(UserScopeRoleBO::getRoleId)
                    .map(tenantRoleRepository::selectRoleById)
                    .anyMatch(contextSupport::isTenantSuperRole);
            if (homeTenantSuperAdmin) {
                tenantList = currentTenantId != null && !resolvedHomeTenantId.equals(currentTenantId)
                        ? contextSupport.buildScopedTenantList(currentTenantId, homeTenantId)
                        : contextSupport.buildScopedTenantList(homeTenantId, homeTenantId);
            }
            LoginResponseVO response = new LoginResponseVO();
            response.setId(user.getId());
            response.setRealName(user.getNickName() != null ? user.getNickName() : user.getUsername());
            response.setUsername(user.getUsername());
            response.setHomePath("/");
            response.setRoles(roles == null ? new ArrayList<>() : roles.stream()
                    .map(RoleBO::getCode).collect(Collectors.toList()));
            response.setAccessToken(accessToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(timeout);
            response.setCurrentTenantId(currentTenantId);
            response.setHomeTenantId(homeTenantId);
            response.setSwitchMode(switchMode);
            response.setTenantName(tenantName);
            response.setTenants(tenantList);
            response.setSubTenants(subTenants);
            log.info("用户登录成功, userIdPresent={}, tenantSelected={}, roleCount={}",
                    user.getId() != null, currentTenantId != null,
                    response.getRoles() == null ? 0 : response.getRoles().size());
            return response;
        } catch (Exception e) {
            log.error("完成登录上下文构建异常, userIdPresent={}, errorType={}, errorMessageLength={}",
                    user != null && user.getId() != null,
                    e.getClass().getSimpleName(), valueLength(e.getMessage()));
            return null;
        }
    }

    public LoginResponseVO register(String username, String password, String email) {
        log.info("用户注册: usernamePresent={}, emailPresent={}", username != null, email != null);
        UserBO existing = userRepository.selectByUsername(username);
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        UserBO user = new UserBO();
        user.setUsername(username);
        user.setPassword(PasswordUtils.encode(password));
        user.setEmail(email);
        user.setStatus(1);
        userRepository.insert(user);
        assignDefaultTenantScopeRole(user.getId());
        log.info("用户注册成功: userIdPresent={}", user.getId() != null);
        return login(username, password, null, null);
    }

    public LoginResponseVO codeLogin(String phone, String code, String captchaKey) {
        log.info("验证码登录: phonePresent={}", phone != null);
        if (!captchaService.validateCaptcha(captchaKey, code)) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }
        UserBO user = userRepository.selectByUsername(phone);
        if (user == null) {
            user = new UserBO();
            user.setUsername(phone);
            user.setPassword(PasswordUtils.encode(UUID.randomUUID().toString()));
            user.setStatus(1);
            userRepository.insert(user);
            assignDefaultTenantScopeRole(user.getId());
        }
        return completeLogin(user, null, null);
    }

    public void assignDefaultTenantScopeRole(Long userId) {
        try {
            UserBO user = userRepository.selectById(userId);
            if (user == null) {
                throw new IllegalStateException("new user was not found after insert: " + userId);
            }
            RoleBO defaultRole = tenantRoleRepository.selectEnabledRoleByCode(
                    new com.shiyu.ai.kernel.context.TenantId(1L), "user");
            if (defaultRole == null) {
                throw new IllegalStateException("default user role is not configured for tenant 1");
            }
            UserScopeRoleBO assignment = new UserScopeRoleBO();
            assignment.setUserId(userId);
            assignment.setTenantId(1L);
            assignment.setRoleId(defaultRole.getId());
            userScopeRoleRepository.insert(assignment);
            Map<String, Object> roleMap = new LinkedHashMap<>();
            roleMap.put("roleId", defaultRole.getId());
            roleMap.put("roleName", defaultRole.getName());
            roleMap.put("roleKey", defaultRole.getCode());
            Map<String, Object> extInfoMap = new LinkedHashMap<>();
            extInfoMap.put("currentTenantId", 1L);
            extInfoMap.put("currentRole", roleMap);
            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            if (!userRepository.update(user)) {
                throw new IllegalStateException("failed to persist default tenant context for user " + userId);
            }
            log.info("新用户默认租户/作用域分配成功: userIdPresent={}, tenantAssigned={}, roleAssigned={}",
                    userId != null, true, defaultRole.getId() != null);
        } catch (RuntimeException e) {
            log.error("新用户默认租户/作用域分配失败: userIdPresent={}, errorType={}, errorMessageLength={}",
                    userId != null, e.getClass().getSimpleName(), valueLength(e.getMessage()));
            throw e;
        } catch (Exception e) {
            log.error("新用户默认租户/作用域分配失败: userIdPresent={}, errorType={}, errorMessageLength={}",
                    userId != null, e.getClass().getSimpleName(), valueLength(e.getMessage()));
            throw new IllegalStateException("failed to provision default tenant scope for user " + userId, e);
        }
    }

    private static int valueLength(String value) {
        return value == null ? 0 : value.length();
    }
}
