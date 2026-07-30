package com.shiyu.ai.web.interceptor;

import com.shiyu.ai.dal.auth.repository.AuthUserLookupRepository;
import com.shiyu.ai.dal.auth.repository.TenantRepository;
import com.shiyu.ai.dal.auth.dataobject.TenantDO;
import com.shiyu.ai.dal.auth.dataobject.UserDO;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.auth.utils.SaTokenHelper;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.domain.LoginUser;
import com.shiyu.ai.common.core.enums.DeviceTypeEnum;
import com.shiyu.ai.common.core.enums.UserTypeEnum;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.core.utils.JSONUtils;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private final AuthUserLookupRepository authUserLookupRepository;
    private final TenantRepository tenantRepository;

    public UserContextInterceptor(AuthUserLookupRepository authUserLookupRepository,
                                  TenantRepository tenantRepository) {
        this.authUserLookupRepository = authUserLookupRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        if (request.getDispatcherType() == DispatcherType.ASYNC) return true;

        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            if (!helper.isFrameworkLogin()) {
                log.warn("用户未登录，拦截请求: uri={}", request.getRequestURI());
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().print(JSONUtils.toJsonString(Result.fail(BizResultCode.UNAUTHORIZED, "未登录或登录已失效")));
                return false;
            }

            Long userId = SaTokenHelper.getCurrentUserId();
            LoginUser loginUser = SaTokenHelper.getLoginUserFromSession();

            if (loginUser != null && userId.equals(loginUser.getUserId())) {
                // Session 只作为性能缓存，用户、角色、租户状态和范围必须实时以数据库为准。
                if (isCachedContextValid(loginUser)) {
                    loginUser.setToken(SaTokenHelper.getCurrentToken());
                    loginUser.setIpaddr(getClientIp(request));
                    String ua = request.getHeader("User-Agent");
                    if (ua != null) {
                        loginUser.setBrowser(parseBrowser(ua));
                        loginUser.setOs(parseOS(ua));
                    }
                    LoginContextHolder.setContext(loginUser);
                    return true;
                } else {
                    // 切换租户/角色后 ext_info 已更新，旧 session 可能仍保留旧上下文。
                    // 不应直接拒绝请求，而应清除缓存并从数据库重新加载；
                    // 如果数据库中的权限确实失效，loadScopeContext 会最终拒绝。
                    log.debug("登录用户 session 上下文已过期，重新加载: userId={}", userId);
                    SaTokenHelper.clearLoginUserSession();
                }
            }

            // 缓存未命中，重新加载
            loginUser = new LoginUser();
            loginUser.setUserId(userId);
            loginUser.setToken(SaTokenHelper.getCurrentToken());
            loginUser.setUserType(UserTypeEnum.SYS_USER);
            loginUser.setLoginTime(System.currentTimeMillis());
            loginUser.setExpireTime(helper.getTokenTimeout());
            loginUser.setIpaddr(getClientIp(request));

            String ua = request.getHeader("User-Agent");
            if (ua != null) {
                loginUser.setBrowser(parseBrowser(ua));
                loginUser.setOs(parseOS(ua));
            }

            loadScopeContext(userId, loginUser);

            LoginContextHolder.setContext(loginUser);
            try {
                SaTokenHelper.saveLoginUserToSession(loginUser);
            } catch (Exception ignored) {}

            log.debug("用户上下文加载: userId={}, homeTenantId={}, currentTenantId={}, currentRole={}, switchMode={}",
                    userId, loginUser.getHomeTenantId(), loginUser.getCurrentTenantId(),
                    loginUser.getCurrentRoleCode(), loginUser.getSwitchMode());

            return true;
        } catch (Exception e) {
            log.error("设置用户上下文失败，拒绝请求: uri={}", request.getRequestURI(), e);
            LoginContextHolder.clearContext();
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSONUtils.toJsonString(Result.fail(BizResultCode.UNAUTHORIZED, "用户上下文加载失败")));
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void loadScopeContext(Long userId, LoginUser loginUser) {
        try {
            UserDO user = authUserLookupRepository.selectUserById(userId);
            if (user == null) {
                throw new IllegalStateException("登录用户不存在");
            }

            loginUser.setUsername(user.getUsername());
            loginUser.setNickName(user.getNickName());
            loginUser.setAvatar(user.getAvatar());

            Long currentTenantId = null;
            if (user.getExtInfo() != null && !user.getExtInfo().isEmpty()) {
                try {
                    Map<String, Object> extInfo = JSONUtils.parseObject(user.getExtInfo(), Map.class);
                    if (extInfo != null) {
                        // 默认/当前租户
                        Object homeTid = extInfo.get("homeTenantId");
                        if (homeTid instanceof Number) {
                            loginUser.setHomeTenantId(((Number) homeTid).longValue());
                        }
                        Object tid = extInfo.get("currentTenantId");
                        if (tid instanceof Number) {
                            currentTenantId = ((Number) tid).longValue();
                        }
                        Object switchMode = extInfo.get("switchMode");
                        if (switchMode instanceof String) {
                            loginUser.setSwitchMode((String) switchMode);
                        }
                        Object fromTenantId = extInfo.get("switchFromTenantId");
                        if (fromTenantId instanceof Number) {
                            loginUser.setSwitchFromTenantId(((Number) fromTenantId).longValue());
                        }
                        // 当前角色
                        Object roleObj = extInfo.get("currentRole");
                        if (roleObj instanceof Map roleMap) {
                            Object roleKey = ((Map<String, Object>) roleMap).get("roleKey");
                            Object roleId = ((Map<String, Object>) roleMap).get("roleId");
                            if (roleId instanceof Number
                                    && roleKey instanceof String
                                    && isCurrentRoleValid(userId, currentTenantId,
                                    ((Number) roleId).longValue(), (String) roleKey)) {
                                loginUser.setCurrentRoleCode((String) roleKey);
                                loginUser.setCurrentRoleId(((Number) roleId).longValue());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("extInfo 解析异常: {}", e.getMessage());
                }
            }

            // 兜底 currentTenantId
            if (currentTenantId == null) {
                List<UserScopeRoleDO> assignments =
                        authUserLookupRepository.selectUserWorkspaceRoles(userId);
                if (assignments != null) {
                    currentTenantId = assignments.stream()
                            .filter(this::isActive)
                            .map(UserScopeRoleDO::getTenantId)
                            .filter(java.util.Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                }
            }

            loginUser.setCurrentTenantId(currentTenantId);
            if (loginUser.getHomeTenantId() == null) {
                loginUser.setHomeTenantId(currentTenantId);
            }

            TenantDO currentTenant = currentTenantId == null
                    ? null : authUserLookupRepository.selectTenantById(currentTenantId);
            if (currentTenantId == null || !isActive(currentTenant)) {
                throw new IllegalStateException("当前租户不存在或已停用");
            }

            List<UserScopeRoleDO> assignments =
                    authUserLookupRepository.selectUserWorkspaceRoles(userId);
            final Long contextTenantId = currentTenantId;
            boolean assigned = assignments != null && assignments.stream().anyMatch(item ->
                    contextTenantId.equals(item.getTenantId())
                            && isActive(item)
                            && item.getRoleId() != null
                            && isActive(authUserLookupRepository.selectRoleById(item.getRoleId())));
            boolean delegated = "PARENT_SUPER_ADMIN".equals(loginUser.getSwitchMode())
                    && isParentSuperAdmin(userId, loginUser.getHomeTenantId(), assignments)
                    && tenantRepository.selectDescendantIds(loginUser.getHomeTenantId())
                            .contains(currentTenantId);
            if (!assigned && !delegated) {
                throw new IllegalStateException("用户不再属于当前租户");
            }
            if (delegated && loginUser.getCurrentRoleCode() == null) {
                RoleDO role = authUserLookupRepository.selectTenantSuperRole(currentTenantId);
                if (isActive(role)) {
                    loginUser.setCurrentRoleId(role.getId());
                    loginUser.setCurrentRoleCode(role.getCode());
                }
            }

        } catch (Exception e) {
            log.error("加载租户作用域异常: userId={}", userId, e);
            throw new IllegalStateException("加载用户租户作用域失败", e);
        }
    }

    /**
     * currentRole 保存在用户扩展信息中，但是否仍然有效必须以当前授权关系为准。
     * 特别是 super 角色不能仅信任 extInfo，否则撤权后仍可能绕过租户过滤。
     */
    private boolean isCurrentRoleValid(Long userId, Long currentTenantId,
                                       Long roleId, String roleCode) {
        if (userId == null || currentTenantId == null || roleId == null || roleCode == null) {
            return false;
        }
        List<UserScopeRoleDO> assignments =
                authUserLookupRepository.selectUserWorkspaceRoles(userId);
        boolean assigned = assignments != null && assignments.stream().anyMatch(item ->
                item.getRoleId() != null
                        && roleId.equals(item.getRoleId())
                        && currentTenantId.equals(item.getTenantId())
                        && (item.getStatus() == null || item.getStatus() == 1)
                        && (item.getDelFlag() == null || item.getDelFlag() == 0));
        if (!assigned) {
            return false;
        }
        RoleDO role = authUserLookupRepository.selectRoleById(roleId);
        return role != null
                && roleCode.equals(role.getCode())
                && (role.getStatus() == null || role.getStatus() == 1)
                && (role.getDelFlag() == null || role.getDelFlag() == 0);
    }

    private boolean isCachedContextValid(LoginUser loginUser) {
        if (loginUser.getCurrentTenantId() == null) {
            return false;
        }
        // 委托到子租户时，homeTenant/currentTenant/currentRole 都来自 user.ext_info。
        // 不复用旧 session，避免切换后仍按旧租户角色校验。
        if (loginUser.isParentSuperAdminSwitch()) {
            return false;
        }
        UserDO user = authUserLookupRepository.selectUserById(loginUser.getUserId());
        if (!isActive(user)) {
            return false;
        }
        TenantDO tenant = authUserLookupRepository.selectTenantById(loginUser.getCurrentTenantId());
        if (!isActive(tenant)) {
            return false;
        }
        List<UserScopeRoleDO> assignments =
                authUserLookupRepository.selectUserWorkspaceRoles(loginUser.getUserId());
        boolean assigned = assignments != null && assignments.stream().anyMatch(item ->
                loginUser.getCurrentTenantId().equals(item.getTenantId())
                        && isActive(item)
                        && item.getRoleId() != null
                        && isActive(authUserLookupRepository.selectRoleById(item.getRoleId())));
        boolean delegated = "PARENT_SUPER_ADMIN".equals(loginUser.getSwitchMode())
                && isParentSuperAdmin(loginUser.getUserId(), loginUser.getHomeTenantId(), assignments)
                && tenantRepository.selectDescendantIds(loginUser.getHomeTenantId())
                    .contains(loginUser.getCurrentTenantId())
                && authUserLookupRepository.selectTenantSuperRole(loginUser.getCurrentTenantId()) != null;
        if (!assigned && !delegated) {
            return false;
        }
        if (loginUser.getCurrentRoleCode() != null) {
            boolean roleValid = delegated && isTenantSuperCode(loginUser.getCurrentRoleCode())
                    || assignments.stream().anyMatch(item -> {
                if (!loginUser.getCurrentTenantId().equals(item.getTenantId())
                        || !isActive(item) || item.getRoleId() == null) {
                    return false;
                }
                RoleDO role = authUserLookupRepository.selectRoleById(item.getRoleId());
                return isActive(role) && loginUser.getCurrentRoleCode().equals(role.getCode());
            });
            if (!roleValid) {
                return false;
            }
        }
        return true;
    }

    private boolean isTenantSuperCode(String roleCode) {
        return "tenant_super".equals(roleCode) || "super".equals(roleCode);
    }

    private boolean isParentSuperAdmin(Long userId, Long homeTenantId,
                                       List<UserScopeRoleDO> assignments) {
        if (homeTenantId == null || assignments == null) {
            return false;
        }
        return assignments.stream()
                .filter(item -> homeTenantId.equals(item.getTenantId()) && isActive(item))
                .map(UserScopeRoleDO::getRoleId)
                .map(authUserLookupRepository::selectRoleById)
                .anyMatch(role -> role != null && isTenantSuperCode(role.getCode())
                        && isActive(role));
    }

    private boolean isActive(UserDO item) {
        return item != null && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0);
    }

    private boolean isActive(TenantDO item) {
        return item != null && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0);
    }

    private boolean isActive(RoleDO item) {
        return item != null && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0);
    }

    private boolean isActive(UserScopeRoleDO item) {
        return item != null && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginContextHolder.clearContext();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();
        if (ip != null && ip.indexOf(",") > 0) ip = ip.substring(0, ip.indexOf(","));
        return ip;
    }

    private String parseBrowser(String ua) {
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari")) return "Safari";
        if (ua.contains("Edge")) return "Edge";
        if (ua.contains("MSIE") || ua.contains("Trident")) return "IE";
        return "Unknown";
    }

    private DeviceTypeEnum parseOS(String ua) {
        if (ua.contains("Windows")) return DeviceTypeEnum.WINDOWS;
        if (ua.contains("Mac OS")) return DeviceTypeEnum.MAC;
        if (ua.contains("Linux")) return DeviceTypeEnum.LINUX;
        if (ua.contains("Android")) return DeviceTypeEnum.ANDROID;
        if (ua.contains("iPhone") || ua.contains("iPad")) return DeviceTypeEnum.IOS;
        return DeviceTypeEnum.UNKNOWN;
    }
}
