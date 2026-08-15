package com.shiyu.ai.web.interceptor;

import com.shiyu.ai.auth.api.response.AuthTenantResponse;
import com.shiyu.ai.auth.api.response.AuthUserResponse;
import com.shiyu.ai.auth.api.response.AuthRoleResponse;
import com.shiyu.ai.auth.api.response.AuthScopeRoleResponse;
import com.shiyu.ai.auth.utils.SaTokenHelper;
import com.shiyu.ai.auth.service.AuthContextService;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.core.domain.UserContext;
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
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private final AuthContextService authContextService;

    public UserContextInterceptor(AuthContextService authContextService) {
        this.authContextService = authContextService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        if (request.getDispatcherType() != DispatcherType.REQUEST) return true;

        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            if (!helper.isFrameworkLogin()) {
                log.warn("用户未登录，拦截请求: uri={}", request.getRequestURI());
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().print(JSONUtils.toJsonString(Result.fail(BizResultCode.UNAUTHORIZED, "未登录或登录已失效")));
                return false;
            }

            Long userId = SaTokenHelper.getCurrentUserId();
            UserContext userContext = SaTokenHelper.getUserContextFromSession();

            if (userContext != null && userId.equals(userContext.getUserId())) {
                // Session 上下文仍需校验用户、租户和角色的运行期状态。
                if (isCachedContextValid(userContext)) {
                    userContext.setToken(SaTokenHelper.getCurrentToken());
                    userContext.setIpaddr(getClientIp(request));
                    String ua = request.getHeader("User-Agent");
                    if (ua != null) {
                        userContext.setBrowser(parseBrowser(ua));
                        userContext.setOs(parseOS(ua));
                    }
                    UserContextHolder.setContext(userContext);
                    return true;
                } else {
                    // 切换租户或角色后 ext_info 已持久化，旧 Session 不能继续作为授权依据。
                    // 清除无效缓存，随后由 loadScopeContext 从持久层重新加载。
                    log.debug("检测到用户 Session 上下文失效，重新加载: userId={}", userId);
                    SaTokenHelper.clearUserContextSession();
                }
            }

            // 缓存不存在或已失效时，重新加载用户上下文。
            userContext = new UserContext();
            userContext.setUserId(userId);
            userContext.setToken(SaTokenHelper.getCurrentToken());
            userContext.setUserType(UserTypeEnum.SYS_USER);
            userContext.setLoginTime(System.currentTimeMillis());
            userContext.setExpireTime(helper.getTokenTimeout());
            userContext.setIpaddr(getClientIp(request));

            String ua = request.getHeader("User-Agent");
            if (ua != null) {
                userContext.setBrowser(parseBrowser(ua));
                userContext.setOs(parseOS(ua));
            }

            loadScopeContext(userId, userContext);

            UserContextHolder.setContext(userContext);
            try {
                SaTokenHelper.saveUserContextToSession(userContext);
            } catch (Exception ignored) {}

            log.debug("用户上下文加载完成: userId={}, homeTenantId={}, currentTenantId={}, currentRole={}, switchMode={}",
                    userId, userContext.getHomeTenantId(), userContext.getCurrentTenantId(),
                    userContext.getCurrentRoleCode(), userContext.getSwitchMode());

            return true;
        } catch (Exception e) {
            log.error("加载用户上下文失败，拦截请求: uri={}", request.getRequestURI(), e);
            UserContextHolder.clearContext();
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSONUtils.toJsonString(Result.fail(BizResultCode.UNAUTHORIZED, "用户上下文加载失败")));
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void loadScopeContext(Long userId, UserContext userContext) {
        try {
            AuthUserResponse user = authContextService.user(userId);
            if (user == null) {
                throw new IllegalStateException("登录用户不存在");
            }

            userContext.setUsername(user.getUsername());
            userContext.setNickName(user.getNickName());
            userContext.setAvatar(user.getAvatar());

            Long currentTenantId = null;
            if (user.getExtInfo() != null && !user.getExtInfo().isEmpty()) {
                try {
                    Map<String, Object> extInfo = JSONUtils.parseObject(
                            user.getExtInfo(), new TypeReference<Map<String, Object>>() {});
                    if (extInfo != null) {
                        // 归属租户与当前租户。
                        Object homeTid = extInfo.get("homeTenantId");
                        if (homeTid instanceof Number) {
                            userContext.setHomeTenantId(((Number) homeTid).longValue());
                        }
                        Object tid = extInfo.get("currentTenantId");
                        if (tid instanceof Number) {
                            currentTenantId = ((Number) tid).longValue();
                        }
                        Object switchMode = extInfo.get("switchMode");
                        if (switchMode instanceof String) {
                            userContext.setSwitchMode((String) switchMode);
                        }
                        Object fromTenantId = extInfo.get("switchFromTenantId");
                        if (fromTenantId instanceof Number) {
                            userContext.setSwitchFromTenantId(((Number) fromTenantId).longValue());
                        }
                        // 当前角色。
                        Object roleObj = extInfo.get("currentRole");
                        if (roleObj instanceof Map<?, ?> roleMap) {
                            Object roleKey = roleMap.get("roleKey");
                            Object roleId = roleMap.get("roleId");
                            if (roleId instanceof Number
                                    && roleKey instanceof String
                                    && isCurrentRoleValid(userId, currentTenantId,
                                    ((Number) roleId).longValue(), (String) roleKey)) {
                                userContext.setCurrentRoleCode((String) roleKey);
                                userContext.setCurrentRoleId(((Number) roleId).longValue());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("extInfo 解析失败: {}", e.getMessage());
                }
            }

            // 无持久化选择时，回退到第一个有效的租户作用域。
            if (currentTenantId == null) {
                List<AuthScopeRoleResponse> assignments =
                        authContextService.workspaceRoles(userId);
                if (assignments != null) {
                    currentTenantId = assignments.stream()
                            .filter(this::isActive)
                            .map(AuthScopeRoleResponse::getTenantId)
                            .filter(java.util.Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                }
            }

            userContext.setCurrentTenantId(currentTenantId);
            if (userContext.getHomeTenantId() == null) {
                userContext.setHomeTenantId(currentTenantId);
            }

            AuthTenantResponse currentTenant = currentTenantId == null
                    ? null : authContextService.tenant(currentTenantId);
            if (currentTenantId == null || !isActive(currentTenant)) {
                throw new IllegalStateException("当前租户不存在或已停用");
            }

            List<AuthScopeRoleResponse> assignments =
                    authContextService.workspaceRoles(userId);
            final Long contextTenantId = currentTenantId;
            boolean assigned = assignments != null && assignments.stream().anyMatch(item ->
                    contextTenantId.equals(item.getTenantId())
                            && isActive(item)
                            && item.getRoleId() != null
                            && isActive(authContextService.role(item.getRoleId())));
            boolean delegated = "PARENT_SUPER_ADMIN".equals(userContext.getSwitchMode())
                    && isParentSuperAdmin(userId, userContext.getHomeTenantId(), assignments)
                    && authContextService.descendantTenantIds(userContext.getHomeTenantId())
                            .contains(currentTenantId);
            if (!assigned && !delegated) {
                throw new IllegalStateException("鐢ㄦ埛涓嶅啀灞炰簬褰撳墠绉熸埛");
            }
            if (delegated && userContext.getCurrentRoleCode() == null) {
                AuthRoleResponse role = authContextService.tenantSuperRole(currentTenantId);
                if (isActive(role)) {
                    userContext.setCurrentRoleId(role.getId());
                    userContext.setCurrentRoleCode(role.getCode());
                }
            }

        } catch (Exception e) {
            log.error("加载租户上下文失败: userId={}", userId, e);
            throw new IllegalStateException("加载用户租户作用域失败", e);
        }
    }

    /**
     * 当前角色必须同时满足用户-租户-角色关联有效、角色本身有效。
     * 即使 extInfo 中声明 super，也不能绕过用户的实际授权关系。
     */
    private boolean isCurrentRoleValid(Long userId, Long currentTenantId,
                                       Long roleId, String roleCode) {
        if (userId == null || currentTenantId == null || roleId == null || roleCode == null) {
            return false;
        }
        List<AuthScopeRoleResponse> assignments =
                authContextService.workspaceRoles(userId);
        boolean assigned = assignments != null && assignments.stream().anyMatch(item ->
                item.getRoleId() != null
                        && roleId.equals(item.getRoleId())
                        && currentTenantId.equals(item.getTenantId())
                        && (item.getStatus() == null || item.getStatus() == 1)
                        && (item.getDelFlag() == null || item.getDelFlag() == 0));
        if (!assigned) {
            return false;
        }
        AuthRoleResponse role = authContextService.role(roleId);
        return role != null
                && roleCode.equals(role.getCode())
                && (role.getStatus() == null || role.getStatus() == 1)
                && (role.getDelFlag() == null || role.getDelFlag() == 0);
    }

    private boolean isCachedContextValid(UserContext userContext) {
        if (userContext.getCurrentTenantId() == null) {
            return false;
        }
        // 租户或角色发生变化时，以 user.ext_info 中的持久化选择为准。
        // 父级超级管理员切换属于临时上下文，不复用普通用户 Session。
        if (userContext.isParentSuperAdminSwitch()) {
            return false;
        }
        AuthUserResponse user = authContextService.user(userContext.getUserId());
        if (!isActive(user)) {
            return false;
        }
        AuthTenantResponse tenant = authContextService.tenant(userContext.getCurrentTenantId());
        if (!isActive(tenant)) {
            return false;
        }
        List<AuthScopeRoleResponse> assignments =
                authContextService.workspaceRoles(userContext.getUserId());
        boolean assigned = assignments != null && assignments.stream().anyMatch(item ->
                userContext.getCurrentTenantId().equals(item.getTenantId())
                        && isActive(item)
                        && item.getRoleId() != null
                        && isActive(authContextService.role(item.getRoleId())));
        boolean delegated = "PARENT_SUPER_ADMIN".equals(userContext.getSwitchMode())
                && isParentSuperAdmin(userContext.getUserId(), userContext.getHomeTenantId(), assignments)
                && authContextService.descendantTenantIds(userContext.getHomeTenantId())
                    .contains(userContext.getCurrentTenantId())
                && authContextService.tenantSuperRole(userContext.getCurrentTenantId()) != null;
        if (!assigned && !delegated) {
            return false;
        }
        if (userContext.getCurrentRoleCode() != null) {
            boolean roleValid = delegated && isTenantSuperCode(userContext.getCurrentRoleCode())
                    || assignments.stream().anyMatch(item -> {
                if (!userContext.getCurrentTenantId().equals(item.getTenantId())
                        || !isActive(item) || item.getRoleId() == null) {
                    return false;
                }
                AuthRoleResponse role = authContextService.role(item.getRoleId());
                return isActive(role) && userContext.getCurrentRoleCode().equals(role.getCode());
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
                                       List<AuthScopeRoleResponse> assignments) {
        if (homeTenantId == null || assignments == null) {
            return false;
        }
        return assignments.stream()
                .filter(item -> homeTenantId.equals(item.getTenantId()) && isActive(item))
                .map(AuthScopeRoleResponse::getRoleId)
                .map(authContextService::role)
                .anyMatch(role -> role != null && isTenantSuperCode(role.getCode())
                        && isActive(role));
    }

    private boolean isActive(AuthUserResponse item) {
        return item != null && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0);
    }

    private boolean isActive(AuthTenantResponse item) {
        return item != null && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0);
    }

    private boolean isActive(AuthRoleResponse item) {
        return item != null && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0);
    }

    private boolean isActive(AuthScopeRoleResponse item) {
        return item != null && item.getStatus() != null && item.getStatus() == 1
                && (item.getDelFlag() == null || item.getDelFlag() == 0);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clearContext();
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
