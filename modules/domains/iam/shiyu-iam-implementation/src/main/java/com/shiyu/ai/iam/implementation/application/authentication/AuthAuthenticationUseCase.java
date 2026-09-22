package com.shiyu.ai.iam.implementation.application.authentication;

import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.common.foundation.utils.PasswordUtils;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 定义 认证 Authentication 相关用例的输入、授权和业务结果。
 */
@Slf4j
public final class AuthAuthenticationUseCase {
    /**
     * 用户仓储，表示当前对象中的对应属性。
     */
    private final UserRepository userRepository;
    /**
     * userScopeRoleRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final UserScopeRoleRepository userScopeRoleRepository;
    /**
     * 租户角色仓储，表示当前对象中的对应属性。
     */
    private final TenantRoleRepository tenantRoleRepository;
    /**
     * captchaService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final CaptchaService captchaService;
    /**
     * contextSupport 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthTenantContextSupport contextSupport;

    /**
     * 执行 认证 Authentication 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param userRepository 用于完成本次业务处理的 userRepository 参数。
     * @param userScopeRoleRepository 用于完成本次业务处理的 userScopeRoleRepository 参数。
     * @param tenantRoleRepository 用于完成本次业务处理的 tenantRoleRepository 参数。
     * @param captchaService 用于完成本次业务处理的 captchaService 参数。
     * @param contextSupport 用于完成本次业务处理的 contextSupport 参数。
     */
    public AuthAuthenticationUseCase(
            UserRepository userRepository,
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

    /**
     * 执行 认证 Authentication 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @param password 用于完成本次业务处理的 password 参数。
     * @param roleId 用于定位role的标识。
     * @param loginIp 用于完成本次业务处理的 loginIp 参数。
     * @return 返回 认证 Authentication 相关操作生成的结果数据。
     */
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
            log.error(
                    "用户登录异常, usernamePresent={}, errorType={}, errorMessageLength={}",
                    username != null,
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            return null;
        }
    }

    /**
     * 处理completelogin。
     *
     * @param user user 参数。
     * @param roleId roleId 参数。
     * @param loginIp loginIp 参数。
     *
     * @return 处理结果。
     */
    public LoginResponseVO completeLogin(UserBO user, Long roleId, String loginIp) {
        try {
            if (user == null || user.getId() == null) {
                return null;
            }
            List<RoleBO> roles = userRepository.selectRolesByUserId(user.getId());
            List<UserScopeRoleBO> assignments =
                    userScopeRoleRepository.selectByUserId(user.getId());
            Map<String, Object> savedExtInfo = contextSupport.parseExtInfo(user.getExtInfo());
            Long currentTenantId =
                    contextSupport.resolveCurrentTenantId(user.getExtInfo(), assignments);
            if (currentTenantId == null || currentTenantId <= 0) {
                log.warn("登录失败，用户没有有效的租户上下文, userIdPresent={}", user.getId() != null);
                return null;
            }
            Long homeTenantId = contextSupport.numberValue(savedExtInfo.get("homeTenantId"));
            if (homeTenantId == null) {
                homeTenantId = currentTenantId;
            }
            Set<Long> currentRoleIds =
                    assignments.stream()
                            .filter(
                                    item ->
                                            currentTenantId.equals(item.getTenantId())
                                                    && contextSupport.isActiveAssignment(item))
                            .map(UserScopeRoleBO::getRoleId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
            roles = roles.stream().filter(role -> currentRoleIds.contains(role.getId())).toList();
            RoleBO currentRole =
                    contextSupport.resolveCurrentRoleForTenant(
                            roleId, roles, assignments, currentTenantId);
            if (currentRole == null
                    && "PARENT_SUPER_ADMIN".equals(savedExtInfo.get("switchMode"))) {
                RoleBO delegatedRole = contextSupport.findTenantSuperRole(currentTenantId);
                if (delegatedRole != null) {
                    currentRole = delegatedRole;
                    roles = new ArrayList<>(roles);
                    roles.add(currentRole);
                }
            }
            TenantBO tenant =
                    tenantRoleRepository.selectTenantById(
                            new com.shiyu.ai.kernel.context.TenantId(currentTenantId));
            String tenantName = tenant == null ? null : tenant.getName();
            LocalDateTime now = LocalDateTime.now();
            String resolvedLoginIp = loginIp == null || loginIp.isBlank() ? "unknown" : loginIp;
            Map<String, Object> extInfoMap =
                    contextSupport.buildExtInfo(
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
            List<TenantContextVO> subTenants =
                    contextSupport.buildSubTenantList(assignments, currentTenantId);
            List<TenantInfoVO> tenantList = contextSupport.buildTenantList(assignments);
            final Long resolvedHomeTenantId = homeTenantId;
            boolean homeTenantSuperAdmin =
                    resolvedHomeTenantId != null
                            && assignments != null
                            && assignments.stream()
                                    .filter(
                                            item ->
                                                    resolvedHomeTenantId.equals(item.getTenantId())
                                                            && contextSupport.isActiveAssignment(
                                                                    item))
                                    .map(UserScopeRoleBO::getRoleId)
                                    .map(tenantRoleRepository::selectRoleById)
                                    .anyMatch(contextSupport::isTenantSuperRole);
            if (homeTenantSuperAdmin) {
                tenantList =
                        currentTenantId != null && !resolvedHomeTenantId.equals(currentTenantId)
                                ? contextSupport.buildScopedTenantList(
                                        currentTenantId, homeTenantId)
                                : contextSupport.buildScopedTenantList(homeTenantId, homeTenantId);
            }
            LoginResponseVO response = new LoginResponseVO();
            response.setId(user.getId());
            response.setRealName(
                    user.getNickName() != null ? user.getNickName() : user.getUsername());
            response.setUsername(user.getUsername());
            response.setHomePath("/");
            response.setRoles(
                    roles == null
                            ? new ArrayList<>()
                            : roles.stream().map(RoleBO::getCode).collect(Collectors.toList()));
            response.setAccessToken(accessToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(timeout);
            response.setCurrentTenantId(currentTenantId);
            response.setHomeTenantId(homeTenantId);
            response.setSwitchMode(switchMode);
            response.setTenantName(tenantName);
            response.setTenants(tenantList);
            response.setSubTenants(subTenants);
            log.info(
                    "用户登录成功, userIdPresent={}, tenantSelected={}, roleCount={}",
                    user.getId() != null,
                    currentTenantId != null,
                    response.getRoles() == null ? 0 : response.getRoles().size());
            return response;
        } catch (Exception e) {
            log.error(
                    "完成登录上下文构建异常, userIdPresent={}, errorType={}, errorMessageLength={}",
                    user != null && user.getId() != null,
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            return null;
        }
    }

    /**
     * 创建或保存 认证 Authentication 相关业务数据，并返回处理结果。
     *
     * @param username 用于完成本次业务处理的 username 参数。
     * @param password 用于完成本次业务处理的 password 参数。
     * @param email 用于完成本次业务处理的 email 参数。
     * @return 返回 认证 Authentication 相关操作生成的结果数据。
     */
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

    /**
     * 执行 认证 Authentication 相关业务数据，并返回处理结果。
     *
     * @param phone 用于完成本次业务处理的 phone 参数。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param captchaKey 用于完成本次业务处理的 captchaKey 参数。
     * @return 返回 认证 Authentication 相关操作生成的结果数据。
     */
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

    /**
     * 在默认租户内为新注册用户分配普通用户角色，并持久化其初始登录上下文。
     *
     * @param userId 已完成注册写入的新用户标识。
     */
    public void assignDefaultTenantScopeRole(Long userId) {
        com.shiyu.ai.kernel.context.TenantScope.withTenant(
                new com.shiyu.ai.kernel.context.TenantId(1L), () -> {
                    assignDefaultTenantScopeRoleInScope(userId);
                    return null;
                });
    }

    private void assignDefaultTenantScopeRoleInScope(Long userId) {
        try {
            UserBO user = userRepository.selectById(userId);
            if (user == null) {
                throw new IllegalStateException("new user was not found after insert: " + userId);
            }
            RoleBO defaultRole =
                    tenantRoleRepository.selectEnabledRoleByCode(
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
                throw new IllegalStateException(
                        "failed to persist default tenant context for user " + userId);
            }
            log.info(
                    "新用户默认租户/作用域分配成功: userIdPresent={}, tenantAssigned={}, roleAssigned={}",
                    userId != null,
                    true,
                    defaultRole.getId() != null);
        } catch (RuntimeException e) {
            log.error(
                    "新用户默认租户/作用域分配失败: userIdPresent={}, errorType={}, errorMessageLength={}",
                    userId != null,
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            throw e;
        } catch (Exception e) {
            log.error(
                    "新用户默认租户/作用域分配失败: userIdPresent={}, errorType={}, errorMessageLength={}",
                    userId != null,
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            throw new IllegalStateException(
                    "failed to provision default tenant scope for user " + userId, e);
        }
    }

    private static int valueLength(String value) {
        return value == null ? 0 : value.length();
    }
}
