package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.auth.repository.AuthRepository;
import com.shiyu.ai.dal.auth.repository.UserRepository;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.CaptchaService;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.TenantDO;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.dal.auth.repository.TenantRepository;
import com.shiyu.ai.dal.auth.repository.TenantRoleRepository;

import com.shiyu.ai.dal.auth.repository.UserScopeRoleRepository;

import com.shiyu.ai.dal.auth.bo.RoleBO;
import com.shiyu.ai.dal.auth.bo.UserBO;
import com.shiyu.ai.dal.auth.bo.TenantBO;
import com.shiyu.ai.auth.vo.LoginResponseVO;
import com.shiyu.ai.auth.vo.TenantContextVO;
import com.shiyu.ai.auth.vo.TenantInfoVO;
import com.shiyu.ai.auth.utils.SaTokenHelper;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.domain.LoginUser;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.common.core.utils.PasswordUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final UserScopeRoleRepository userWorkspaceRoleRepository;
    private final TenantRoleRepository tenantRoleRepository;
    private final TenantRepository tenantRepository;
    private final MenuService menuService;
    private final CaptchaService captchaService;
    
    
    private final HttpServletRequest request;

      @Value("${sa-token.timeout:7200}")
    private long tokenTimeout;

    public AuthServiceImpl(AuthRepository authRepository, UserRepository userRepository,
                           UserScopeRoleRepository userWorkspaceRoleRepository,
                           TenantRoleRepository tenantRoleRepository,
                           TenantRepository tenantRepository,
                           MenuService menuService,
                           CaptchaService captchaService,
                           HttpServletRequest request) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.userWorkspaceRoleRepository = userWorkspaceRoleRepository;
        this.tenantRoleRepository = tenantRoleRepository;
        this.tenantRepository = tenantRepository;
        this.menuService = menuService;
        this.captchaService = captchaService;
        
        
        this.request = request;
    }

    @Override
    public LoginResponseVO login(String username, String password) {
        return login(username, password, null);
    }

    @Override
    public LoginResponseVO login(String username, String password, Long roleId) {
        log.info("用户登录开始, username={}", username);

        try {
            UserBO user = userRepository.selectActiveUserByUsername(username);
            if (user == null) {
                log.warn("用户不存在 - {}", username);
                return null;
            }

            if (user.getStatus() == null || user.getStatus() != 1) {
                log.warn("用户已被禁用 - {}", username);
                return null;
            }

            if (!PasswordUtils.matches(password, user.getPassword())) {
                log.warn("密码错误 - {}", username);
                return null;
            }

            return completeLogin(user, roleId);
        } catch (Exception e) {
            log.error("用户登录异常, username={}", username, e);
            return null;
        }
    }

    /**
     * 凭证已经校验通过后，统一完成租户/角色上下文构建和 Token 签发。
     */
    private LoginResponseVO completeLogin(UserBO user, Long roleId) {
        try {
            if (user == null || user.getId() == null) {
                return null;
            }

            List<RoleBO> roles = userRepository.selectRolesByUserId(user.getId());
            // 查询用户工作空间角色列表
            List<UserScopeRoleDO> uwrList = userWorkspaceRoleRepository.selectByUserId(user.getId());

            // 从 ext_info 解析当前租户
            Map<String, Object> savedExtInfo = parseExtInfo(user.getExtInfo());
            Long currentTenantId = resolveCurrentTenantId(user.getExtInfo(), uwrList);
            Long homeTenantId = numberValue(savedExtInfo.get("homeTenantId"));
            if (homeTenantId == null) {
                homeTenantId = currentTenantId;
            }
            Set<Long> currentRoleIds = uwrList.stream()
                    .filter(item -> currentTenantId != null
                            && currentTenantId.equals(item.getTenantId())
                            && isActiveAssignment(item))
                    .map(UserScopeRoleDO::getRoleId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            roles = roles.stream()
                    .filter(role -> currentRoleIds.contains(role.getId()))
                    .toList();
            RoleBO currentRole = resolveCurrentRoleForTenant(roleId, roles, uwrList, currentTenantId);
            if (currentRole == null && "PARENT_SUPER_ADMIN".equals(savedExtInfo.get("switchMode"))) {
                RoleDO delegatedRole = findTenantSuperRole(currentTenantId);
                if (delegatedRole != null) {
                    currentRole = new RoleBO();
                    currentRole.setId(delegatedRole.getId());
                    currentRole.setCode(delegatedRole.getCode());
                    currentRole.setName(delegatedRole.getName());
                    roles = new ArrayList<>(roles);
                    roles.add(currentRole);
                }
            }

            // 获取租户名称
            String tenantName = null;
            if (currentTenantId != null) {
                TenantDO tenant = tenantRoleRepository.selectTenantById(currentTenantId);
                if (tenant != null) {
                    tenantName = tenant.getName();
                }
            }

            // 更新 ext_info
            String loginIp = getClientIp();
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> extInfoMap = buildExtInfo(user.getExtInfo(), currentRole, currentTenantId, now, loginIp);
            extInfoMap.put("homeTenantId", homeTenantId);
            if (!extInfoMap.containsKey("switchMode")) {
                extInfoMap.put("switchMode", "NORMAL");
            }
            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));

            // 设置 LoginUser 上下文（用于自动更新）
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(user.getId());
            loginUser.setUsername(user.getUsername());
            LoginContextHolder.setContext(loginUser);
            try {
                userRepository.update(user);
            } finally {
                LoginContextHolder.clearContext();
            }

            // 生成 Token
            SaTokenHelper helper = SaTokenHelper.getInstance();
            String accessToken = helper.loginWithKickout(user.getId());
            SaTokenHelper.clearLoginUserSession();
            long timeout = helper.getTokenTimeout();

            // 构建当前租户上下文列表
            List<TenantContextVO> subTenants = buildSubTenantList(uwrList, currentTenantId);

            // 构建用户所属租户列表
            List<TenantInfoVO> tenantList = buildTenantList(uwrList);
            String switchMode = (String) extInfoMap.getOrDefault("switchMode", "NORMAL");
            final Long resolvedHomeTenantId = homeTenantId;
            boolean homeTenantSuperAdmin = resolvedHomeTenantId != null
                    && uwrList != null
                    && uwrList.stream()
                    .filter(item -> resolvedHomeTenantId.equals(item.getTenantId())
                            && isActiveAssignment(item))
                    .map(UserScopeRoleDO::getRoleId)
                    .map(tenantRoleRepository::selectRoleById)
                    .anyMatch(this::isTenantSuperRole);
            if (homeTenantSuperAdmin) {
                tenantList = currentTenantId != null
                        && !resolvedHomeTenantId.equals(currentTenantId)
                        ? buildScopedTenantList(currentTenantId, homeTenantId)
                        : buildScopedTenantList(homeTenantId, homeTenantId);
            }

            LoginResponseVO response = new LoginResponseVO();
            response.setId(user.getId());
            response.setRealName(user.getNickName() != null ? user.getNickName() : user.getUsername());
            response.setUsername(user.getUsername());
            response.setHomePath("/");

            if (roles != null && !roles.isEmpty()) {
                response.setRoles(roles.stream()
                        .map(RoleBO::getCode)
                        .collect(Collectors.toList()));
            } else {
                response.setRoles(new ArrayList<>());
            }

            response.setAccessToken(accessToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(timeout);
            response.setCurrentTenantId(currentTenantId);
            response.setHomeTenantId(homeTenantId);
            response.setSwitchMode(switchMode);
            response.setTenantName(tenantName);
            response.setTenants(tenantList);
            response.setSubTenants(subTenants);

            log.info("用户登录成功, username={}, userId={}, tenantId={}, roles={}",
                    user.getUsername(), user.getId(), currentTenantId, response.getRoles());
            return response;
        } catch (Exception e) {
            log.error("完成登录上下文构建异常, userId={}", user.getId(), e);
            return null;
        }
    }

    private Long resolveCurrentTenantId(String extInfo, List<UserScopeRoleDO> uwrList) {
        Long assignedTenantId = null;
        if (extInfo != null && !extInfo.isEmpty()) {
            try {
                Map<String, Object> map = JSONUtils.parseObject(extInfo, Map.class);
                if (map != null && map.get("currentTenantId") instanceof Number) {
                    Long currentTenantId = ((Number) map.get("currentTenantId")).longValue();
                    if (hasTenantAssignment(uwrList, currentTenantId)
                            || isDelegatedTenantContext(map, uwrList, currentTenantId)) {
                        return currentTenantId;
                    }
                    log.warn("忽略无授权的 extInfo.currentTenantId: {}", currentTenantId);
                }
            } catch (Exception ignored) {
            }
        }
        if (uwrList != null && !uwrList.isEmpty()) {
            assignedTenantId = uwrList.stream()
                    .filter(this::isActiveAssignment)
                    .map(UserScopeRoleDO::getTenantId)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return assignedTenantId;
    }

    private boolean hasTenantAssignment(List<UserScopeRoleDO> uwrList, Long tenantId) {
        return tenantId != null && uwrList != null && uwrList.stream()
                .anyMatch(r -> tenantId.equals(r.getTenantId()) && isActiveAssignment(r));
    }

    private Long numberValue(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private boolean isDelegatedTenantContext(Map<String, Object> extInfo,
                                              List<UserScopeRoleDO> assignments,
                                              Long targetTenantId) {
        if (!"PARENT_SUPER_ADMIN".equals(extInfo.get("switchMode"))) {
            return false;
        }
        Long homeTenantId = numberValue(extInfo.get("homeTenantId"));
        return homeTenantId != null
                && targetTenantId != null
                && tenantRepository.selectDescendantIds(homeTenantId).contains(targetTenantId)
                && assignments.stream()
                .filter(item -> homeTenantId.equals(item.getTenantId()) && isActiveAssignment(item))
                .map(UserScopeRoleDO::getRoleId)
                .map(tenantRoleRepository::selectRoleById)
                .anyMatch(this::isTenantSuperRole);
    }

    private RoleDO findTenantSuperRole(Long tenantId) {
        return tenantRoleRepository.selectTenantSuperRole(tenantId);
    }

    private boolean isTenantSuperRole(RoleDO role) {
        return role != null
                && ("tenant_super".equals(role.getCode()) || "super".equals(role.getCode()))
                && role.getStatus() != null && role.getStatus() == 1
                && (role.getDelFlag() == null || role.getDelFlag() == 0);
    }

    private Map<String, Object> buildExtInfo(String oldExtInfo, RoleBO currentRole,
                                              Long currentTenantId,
                                              LocalDateTime now, String loginIp) {
        Map<String, Object> extInfoMap;
        if (oldExtInfo != null && !oldExtInfo.isEmpty()) {
            Map<String, Object> parsed = JSONUtils.parseObject(oldExtInfo, Map.class);
            extInfoMap = parsed != null ? parsed : new LinkedHashMap<>();
        } else {
            extInfoMap = new LinkedHashMap<>();
        }

        extInfoMap.put("lastLoginTime", now.toString());
        extInfoMap.put("lastLoginIp", loginIp);

        if (currentRole != null) {
            Map<String, Object> roleMap = new LinkedHashMap<>();
            roleMap.put("roleId", currentRole.getId());
            roleMap.put("roleName", currentRole.getName());
            roleMap.put("roleKey", currentRole.getCode());
            extInfoMap.put("currentRole", roleMap);
        }

        if (currentTenantId != null) {
            extInfoMap.put("currentTenantId", currentTenantId);
        }

        return extInfoMap;
    }

    private List<TenantContextVO> buildSubTenantList(List<UserScopeRoleDO> uwrList, Long currentTenantId) {
        if (uwrList == null || uwrList.isEmpty()) return new ArrayList<>();

        List<UserScopeRoleDO> filtered = uwrList;
        if (currentTenantId != null) {
            filtered = uwrList.stream()
                    .filter(r -> currentTenantId.equals(r.getTenantId()) && isActiveAssignment(r))
                    .collect(Collectors.toList());
        }

        List<TenantContextVO> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (UserScopeRoleDO uwr : filtered) {
            if (seen.add(uwr.getTenantId())) {
                TenantDO tenantWs = tenantRoleRepository.selectTenantById(uwr.getTenantId());
                RoleDO role = tenantRoleRepository.selectRoleById(uwr.getRoleId());
                if (isActiveTenant(tenantWs) && role != null
                        && role.getStatus() != null && role.getStatus() == 1
                        && (role.getDelFlag() == null || role.getDelFlag() == 0)) {
                    result.add(TenantContextVO.builder()
                            .tenantId(uwr.getTenantId())
                            .tenantName(tenantWs.getName())
                            .roleCode(role.getCode())
                            .build());
                }
            }
        }
        return result;
    }

    private List<TenantInfoVO> buildTenantList(List<UserScopeRoleDO> uwrList) {
        if (uwrList == null || uwrList.isEmpty()) return new ArrayList<>();

        Set<Long> tenantIds = uwrList.stream()
                .filter(this::isActiveAssignment)
                .map(UserScopeRoleDO::getTenantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<TenantInfoVO> result = new ArrayList<>();
        for (Long tid : tenantIds) {
            TenantDO tenant = tenantRoleRepository.selectTenantById(tid);
            if (isActiveTenant(tenant)) {
                TenantInfoVO vo = new TenantInfoVO();
                vo.setId(tenant.getId());
                vo.setCode(tenant.getCode());
                vo.setName(tenant.getName());
                vo.setPathName(tenant.getName());
                result.add(vo);
            }
        }
        return result;
    }

    private List<TenantInfoVO> buildScopedTenantList(Long scopeRootTenantId,
                                                      Long returnTenantId) {
        Map<Long, TenantInfoVO> result = new LinkedHashMap<>();
        Set<Long> visibleTenantIds = new LinkedHashSet<>(
                tenantRepository.selectDescendantIds(scopeRootTenantId));
        if (returnTenantId != null) {
            visibleTenantIds.add(returnTenantId);
        }
        for (TenantBO tenant : tenantRepository.selectAll()) {
            if (tenant.getId() == null
                    || !visibleTenantIds.contains(tenant.getId())
                    || tenant.getStatus() == null || tenant.getStatus() != 1
                    || (tenant.getDelFlag() != null && tenant.getDelFlag() != 0)) {
                continue;
            }
            TenantInfoVO vo = new TenantInfoVO();
            vo.setId(tenant.getId());
            vo.setCode(tenant.getCode());
            vo.setName(tenant.getName());
            vo.setPathName(buildTenantPath(tenant.getId()));
            result.put(tenant.getId(), vo);
        }
        return new ArrayList<>(result.values());
    }

    private String buildTenantPath(Long tenantId) {
        List<TenantBO> all = tenantRepository.selectAll();
        Map<Long, TenantBO> byId = all.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(TenantBO::getId, item -> item, (first, ignored) -> first));
        LinkedList<String> names = new LinkedList<>();
        TenantBO current = byId.get(tenantId);
        Set<Long> visited = new HashSet<>();
        while (current != null && visited.add(current.getId())) {
            names.addFirst(current.getName());
            current = current.getParentId() == null ? null : byId.get(current.getParentId());
        }
        return String.join(" / ", names);
    }

    private boolean isTenantSuperRole(RoleBO role) {
        return role != null
                && ("tenant_super".equals(role.getCode()) || "super".equals(role.getCode()));
    }

    private RoleBO resolveCurrentRoleForTenant(Long roleId, List<RoleBO> roles,
                                                List<UserScopeRoleDO> uwrList,
                                                Long tenantId) {
        if (roles == null || roles.isEmpty() || tenantId == null || uwrList == null) {
            return null;
        }
        Set<Long> allowedRoleIds = uwrList.stream()
                .filter(r -> tenantId.equals(r.getTenantId()) && isActiveAssignment(r))
                .map(UserScopeRoleDO::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return roles.stream()
                .filter(r -> allowedRoleIds.contains(r.getId()))
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .filter(r -> roleId == null || roleId.equals(r.getId()))
                .findFirst()
                .orElseGet(() -> roles.stream()
                        .filter(r -> allowedRoleIds.contains(r.getId()))
                        .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                        .findFirst()
                        .orElse(null));
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Override
    public List<String> getAuthCodes(String username) {
        log.info("获取权限编码, username={}", username);
        try {
            List<String> codes = authRepository.selectCodesByUsername(username);
            if (codes == null || codes.isEmpty()) {
                return new ArrayList<>();
            }
            return codes;
        } catch (Exception e) {
            log.error("获取权限编码异常, username={}", username, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getAuthCodesByUserId(Long userId) {
        log.info("获取权限编码, userId={}", userId);
        try {
            Long currentTenantId = LoginContextHolder.getCurrentTenantId();
            String currentRoleCode = LoginContextHolder.getCurrentRoleCode();
            List<String> codes;
            if (LoginContextHolder.isParentSuperAdminSwitch()
                    && currentTenantId != null && currentRoleCode != null) {
                codes = authRepository.selectCodesByRoleCodeAndTenant(
                        currentRoleCode, currentTenantId);
            } else if (currentTenantId != null && currentRoleCode != null) {
                codes = authRepository.selectCodesByUserIdAndRoleCode(
                        userId, currentTenantId, currentRoleCode);
            } else {
                codes = authRepository.selectCodesByUserId(userId, currentTenantId);
            }
            if (codes == null || codes.isEmpty()) {
                return new ArrayList<>();
            }
            return codes;
        } catch (Exception e) {
            log.error("获取权限编码异常, userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public String refreshToken(String oldToken) {
        log.info("刷新Token");
        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            Long userId = helper.getUserIdByToken(oldToken);
            if (userId == null) {
                log.warn("无效的access token");
                return null;
            }
            String newAccessToken = helper.refreshToken(userId);
            log.info("刷新Token成功, userId={}", userId);
            return newAccessToken;
        } catch (Exception e) {
            log.error("刷新Token异常", e);
            return null;
        }
    }

    @Override
    public boolean switchCurrentRole(Long userId, Long roleId) {
        log.info("切换角色, userId: {}, roleId: {}", userId, roleId);
        try {
            if (userId == null || roleId == null) {
                return false;
            }
            UserBO user = userRepository.selectById(userId);
            if (user == null) {
                log.warn("用户不存在, userId: {}", userId);
                return false;
            }
            List<UserScopeRoleDO> uwrList = userWorkspaceRoleRepository.selectByUserId(userId);
            Long currentTenantId = resolveCurrentTenantId(user.getExtInfo(), uwrList);
            if (currentTenantId == null || uwrList == null || uwrList.stream()
                    .noneMatch(r -> currentTenantId.equals(r.getTenantId())
                            && roleId.equals(r.getRoleId())
                            && isActiveAssignment(r))) {
                log.warn("角色不属于当前租户作用域, userId: {}, roleId: {}, currentTenantId: {}",
                        userId, roleId, currentTenantId);
                return false;
            }
            List<RoleBO> roles = userRepository.selectRolesByUserId(userId);
            RoleBO target = roles == null ? null : roles.stream()
                    .filter(r -> roleId.equals(r.getId()))
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                log.warn("角色不存在, userId: {}", userId);
                return false;
            }

            Map<String, Object> roleMap = new LinkedHashMap<>();
            roleMap.put("roleId", target.getId());
            roleMap.put("roleName", target.getName());
            roleMap.put("roleKey", target.getCode());

            Map<String, Object> extInfoMap = parseExtInfo(user.getExtInfo());
            extInfoMap.put("currentRole", roleMap);
            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            userRepository.update(user);

            log.info("切换角色成功, userId: {}, newRole: {}", userId, target.getName());
            SaTokenHelper.clearLoginUserSession();
            menuService.evictRouteMenuCache(userId);
            return true;
        } catch (Exception e) {
            log.error("切换角色异常, userId: {}, roleId: {}", userId, roleId, e);
            return false;
        }
    }

    @Override
    public boolean switchCurrentTenant(Long userId, Long tenantId) {
        log.info("切换租户, userId: {}, tenantId: {}", userId, tenantId);
        try {
            if (tenantId == null) {
                log.warn("切换租户失败，tenantId 为空, userId: {}", userId);
                return false;
            }
            UserBO user = userRepository.selectById(userId);
            if (user == null) return false;

            List<UserScopeRoleDO> uwrList = userWorkspaceRoleRepository.selectByUserId(userId);
            Map<String, Object> extInfoMap = parseExtInfo(user.getExtInfo());
            Long homeTenantId = numberValue(extInfoMap.get("homeTenantId"));
            if (homeTenantId == null) {
                homeTenantId = uwrList == null ? null : uwrList.stream()
                        .filter(this::isActiveAssignment)
                        .map(UserScopeRoleDO::getTenantId)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
            }
            if (homeTenantId == null) {
                return false;
            }
            final Long homeTenant = homeTenantId;
            Long currentTenantId = numberValue(extInfoMap.get("currentTenantId"));
            if (currentTenantId == null) {
                currentTenantId = homeTenantId;
            }
            TenantDO targetTenant = tenantRoleRepository.selectTenantById(tenantId);
            if (!isActiveTenant(targetTenant)) {
                return false;
            }

            List<RoleDO> assignedRoles = uwrList == null ? List.of() : uwrList.stream()
                    .filter(r -> tenantId.equals(r.getTenantId()) && isActiveAssignment(r))
                    .map(UserScopeRoleDO::getRoleId)
                    .filter(Objects::nonNull)
                    .map(tenantRoleRepository::selectRoleById)
                    .filter(role -> role != null
                            && tenantId.equals(role.getTenantId())
                            && role.getStatus() != null && role.getStatus() == 1
                            && (role.getDelFlag() == null || role.getDelFlag() == 0))
                    .sorted(Comparator.comparing(RoleDO::getId))
                    .toList();
            Long preferredRoleId = extInfoMap.get("currentRole") instanceof Map<?, ?> roleMap
                    ? numberValue(roleMap.get("roleId")) : null;
            RoleDO assignedRole = preferredRoleId == null ? null : assignedRoles.stream()
                    .filter(role -> preferredRoleId.equals(role.getId()))
                    .findFirst()
                    .orElse(null);
            if (assignedRole == null && !assignedRoles.isEmpty()) {
                assignedRole = assignedRoles.get(0);
            }
            boolean homeTenantSuper = uwrList != null && uwrList.stream()
                    .filter(r -> homeTenant.equals(r.getTenantId()) && isActiveAssignment(r))
                    .map(UserScopeRoleDO::getRoleId)
                    .map(tenantRoleRepository::selectRoleById)
                    .anyMatch(this::isTenantSuperRole);
            boolean returningHome = homeTenantId.equals(tenantId);
            boolean switchedAwayFromHome = !homeTenantId.equals(currentTenantId);
            Long allowedRootTenantId = homeTenantSuper && switchedAwayFromHome
                    ? currentTenantId : homeTenantId;
            boolean targetInAllowedSubtree = tenantRepository
                    .selectDescendantIds(allowedRootTenantId).contains(tenantId);
            if (homeTenantSuper && switchedAwayFromHome
                    && !returningHome && !targetInAllowedSubtree) {
                return false;
            }
            if (assignedRole == null
                    && !(homeTenantSuper && (returningHome || targetInAllowedSubtree))) {
                log.warn("切换租户失败，缺少租户归属或父租户超级管理员权限, userId={}, tenantId={}",
                        userId, tenantId);
                return false;
            }

            boolean switchingChild = !returningHome;
            // 直接分配的子租户角色优先；仅在没有有效直接角色时才使用父级超管委托。
            boolean parentSuperAdminSwitch = switchingChild
                    && assignedRole == null
                    && homeTenantSuper
                    && targetInAllowedSubtree;
            extInfoMap.put("currentTenantId", tenantId);
            extInfoMap.put("homeTenantId", homeTenantId);

            RoleDO role = parentSuperAdminSwitch
                    ? findTenantSuperRole(tenantId)
                    : assignedRole;
            if (role != null) {
                Map<String, Object> roleMap = new LinkedHashMap<>();
                roleMap.put("roleId", role.getId());
                roleMap.put("roleName", role.getName());
                roleMap.put("roleKey", role.getCode());
                extInfoMap.put("currentRole", roleMap);
            }
            extInfoMap.put("switchMode", parentSuperAdminSwitch
                    ? "PARENT_SUPER_ADMIN" : "NORMAL");
            if (parentSuperAdminSwitch) {
                extInfoMap.put("switchFromTenantId", homeTenantId);
            } else {
                extInfoMap.remove("switchFromTenantId");
            }

            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            userRepository.update(user);

            log.info("切换租户成功, userId: {}, tenantId: {}", userId, tenantId);
            SaTokenHelper.clearLoginUserSession();
            menuService.evictRouteMenuCache(userId);
            return true;
        } catch (Exception e) {
            log.error("切换租户异常, userId: {}, tenantId: {}", userId, tenantId, e);
            return false;
        }
    }

    @Override
    public List<TenantInfoVO> getUserTenants(Long userId) {
        List<UserScopeRoleDO> uwrList = userWorkspaceRoleRepository.selectByUserId(userId);
        List<TenantInfoVO> tenants = buildTenantList(uwrList);
        UserBO user = userRepository.selectById(userId);
        if (user != null) {
            Map<String, Object> extInfo = parseExtInfo(user.getExtInfo());
            Long homeTenantId = numberValue(extInfo.get("homeTenantId"));
            Long currentTenantId = numberValue(extInfo.get("currentTenantId"));
            if (currentTenantId == null) {
                currentTenantId = com.shiyu.ai.common.core.domain.LoginContextHolder.getCurrentTenantId();
            }
            if (homeTenantId == null) {
                homeTenantId = com.shiyu.ai.common.core.domain.LoginContextHolder.getHomeTenantId();
            }
            final Long resolvedHomeTenantId = homeTenantId;

            boolean homeTenantSuperAdmin = resolvedHomeTenantId != null
                    && uwrList != null
                    && uwrList.stream()
                    .filter(item -> resolvedHomeTenantId.equals(item.getTenantId()) && isActiveAssignment(item))
                    .map(UserScopeRoleDO::getRoleId)
                    .map(tenantRoleRepository::selectRoleById)
                    .anyMatch(this::isTenantSuperRole);

            // 租户切换后仍然返回完整租户树，不能只依赖当前子租户的角色
            // 或当前用户在子租户中的 user_scope_role 记录。
            if (homeTenantSuperAdmin
                    && resolvedHomeTenantId != null
                    && currentTenantId != null
                    && !resolvedHomeTenantId.equals(currentTenantId)) {
                return buildScopedTenantList(currentTenantId, resolvedHomeTenantId);
            }
            if (homeTenantSuperAdmin) {
                return buildScopedTenantList(resolvedHomeTenantId, resolvedHomeTenantId);
            }
        }
        return tenants;
    }

    @Override
    public void logout(String token) {
        log.info("注销退出");
        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            Long userId = helper.getUserIdByToken(token);
            if (userId != null) {
                helper.logout(userId);
                log.info("退出登录成功, userId={}", userId);
            } else {
                log.warn("无效的token, 注销失败");
            }
        } catch (Exception e) {
            log.error("注销异常", e);
        }
    }


    @Override
    public LoginResponseVO register(String username, String password, String email) {
        log.info("用户注册: username={}, email={}", username, email);
        UserBO existing = userRepository.selectByUsername(username);
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        UserBO user = new UserBO();
        user.setUsername(username);
        user.setPassword(PasswordUtils.encode(password));
        user.setEmail(email);
        user.setStatus(1);
        // 设置默认租户上下文，确保多租户插件自动填充 tenantId
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(0L);
        loginUser.setCurrentTenantId(1L);
        LoginContextHolder.setContext(loginUser);
        try {
            userRepository.insert(user);
        } finally {
            LoginContextHolder.clearContext();
        }
        // 为新用户分配默认租户和工作空间角色
        assignDefaultTenantWorkspaceRole(user.getId());
        log.info("用户注册成功: userId={}", user.getId());
        return login(username, password);
    }

    /**
     * 验证码登录。验证码验证成功后不再把验证码当作用户密码。
     */
    @Override
    public LoginResponseVO codeLogin(String phone, String code, String captchaKey) {
        log.info("验证码登录: phone={}", phone);
        if (!captchaService.validateCaptcha(captchaKey, code)) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        UserBO user = userRepository.selectByUsername(phone);
        if (user == null) {
            user = new UserBO();
            user.setUsername(phone);
            user.setPassword(PasswordUtils.encode(UUID.randomUUID().toString()));
            user.setStatus(1);
            // 设置默认租户上下文，确保多租户插件自动填充 tenantId
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(0L);
            loginUser.setCurrentTenantId(1L);
            LoginContextHolder.setContext(loginUser);
            try {
                userRepository.insert(user);
            } finally {
                LoginContextHolder.clearContext();
            }
            // 为新用户分配默认租户和工作空间
            assignDefaultTenantWorkspaceRole(user.getId());
        }
        return completeLogin(user, null);
    }

    /**
     * 忘记密码。验证码验证成功后才允许修改密码，并吊销旧会话。
     */
    @Override
    public boolean forgetPassword(String email, String newPassword, String code, String captchaKey) {
        log.info("忘记密码: email={}", email);
        if (!captchaService.validateCaptcha(captchaKey, code)) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }
        UserBO user = userRepository.selectByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("该邮箱未注册: " + email);
        }
        user.setPassword(PasswordUtils.encode(newPassword));
        boolean updated = userRepository.update(user);
        if (updated) {
            SaTokenHelper.getInstance().logout(user.getId());
            SaTokenHelper.clearLoginUserSession();
        }
        log.info("密码重置成功: userId={}", user.getId());
        return updated;
    }

    /**
     * 为新用户分配默认租户（tenantId=1）和默认作用域租户（currentTenantId=1）
     * 并查询系统第一个可用角色进行绑定，确保用户可正常登录
     */
    private void assignDefaultTenantWorkspaceRole(Long userId) {
        try {
            // 1. 设置用户所属租户
            UserBO user = userRepository.selectById(userId);
            if (user == null) return;

            // 2. 查询默认角色（第一个启用的角色）
            List<RoleBO> roles = userRepository.selectRolesByUserId(1L);
            RoleBO defaultRole;
            if (roles != null && !roles.isEmpty()) {
                defaultRole = roles.get(0);
            } else {
                log.warn("未找到可用角色，跳过用户默认角色分配");
                return;
            }

            // 3. 分配默认工作空间和角色
            UserScopeRoleDO uwr = new UserScopeRoleDO();
            uwr.setUserId(userId);
            uwr.setTenantId(1L);
            uwr.setRoleId(defaultRole.getId());
            uwr.setTenantId(1L);
            userWorkspaceRoleRepository.insert(uwr);

            // 4. 设置用户 extInfo 中的当前上下文
            Map<String, Object> roleMap = new LinkedHashMap<>();
            roleMap.put("roleId", defaultRole.getId());
            roleMap.put("roleName", defaultRole.getName());
            roleMap.put("roleKey", defaultRole.getCode());

            Map<String, Object> extInfoMap = new LinkedHashMap<>();
            extInfoMap.put("currentTenantId", 1L);
            extInfoMap.put("currentRole", roleMap);

            // 5. 更新用户 extInfo 和 tenantId
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(userId);
            LoginContextHolder.setContext(loginUser);
            try {
                // 更新 extInfo
                user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
                userRepository.update(user);
            } finally {
                LoginContextHolder.clearContext();
            }

            log.info("新用户默认租户/作用域分配成功: userId={}, tenantId=1, currentTenantId=1, roleId={}",
                    userId, defaultRole.getId());
        } catch (Exception e) {
            log.warn("新用户默认租户/空间分配异常，不影响用户登录: userId={}, error={}", userId, e.getMessage());
        }
    }

    private Map<String, Object> parseExtInfo(String extInfo) {
        if (extInfo != null && !extInfo.isEmpty()) {
            Map<String, Object> map = JSONUtils.parseObject(extInfo, Map.class);
            if (map != null) return map;
        }
        return new LinkedHashMap<>();
    }

    private boolean isActiveAssignment(UserScopeRoleDO item) {
        return item != null
                && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0)
                && item.getTenantId() != null
                && item.getRoleId() != null;
    }

    private boolean isActiveTenant(TenantDO item) {
        return item != null
                && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0);
    }
}
