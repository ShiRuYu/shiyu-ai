package com.shiyu.ai.web.interceptor;

import com.shiyu.ai.dal.auth.dataobject.TenantDO;
import com.shiyu.ai.dal.auth.dataobject.UserDO;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.UserScopeRoleDO;
import com.shiyu.ai.auth.utils.SaTokenHelper;
import com.shiyu.ai.auth.service.AuthContextService;
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

    private final AuthContextService authContextService;

    public UserContextInterceptor(AuthContextService authContextService) {
        this.authContextService = authContextService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        if (request.getDispatcherType() == DispatcherType.ASYNC) return true;

        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            if (!helper.isFrameworkLogin()) {
                log.warn("鐢ㄦ埛鏈櫥褰曪紝鎷︽埅璇锋眰: uri={}", request.getRequestURI());
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().print(JSONUtils.toJsonString(Result.fail(BizResultCode.UNAUTHORIZED, "未登录或登录已失效")));
                return false;
            }

            Long userId = SaTokenHelper.getCurrentUserId();
            LoginUser loginUser = SaTokenHelper.getLoginUserFromSession();

            if (loginUser != null && userId.equals(loginUser.getUserId())) {
                // Session 鍙綔涓烘€ц兘缂撳瓨锛岀敤鎴枫€佽鑹层€佺鎴风姸鎬佸拰鑼冨洿蹇呴』瀹炴椂浠ユ暟鎹簱涓哄噯銆?
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
                    // 鍒囨崲绉熸埛/瑙掕壊鍚?ext_info 宸叉洿鏂帮紝鏃?session 鍙兘浠嶄繚鐣欐棫涓婁笅鏂囥€?
                    // 涓嶅簲鐩存帴鎷掔粷璇锋眰锛岃€屽簲娓呴櫎缂撳瓨骞朵粠鏁版嵁搴撻噸鏂板姞杞斤紱
                    // 濡傛灉鏁版嵁搴撲腑鐨勬潈闄愮‘瀹炲け鏁堬紝loadScopeContext 浼氭渶缁堟嫆缁濄€?
                    log.debug("鐧诲綍鐢ㄦ埛 session 涓婁笅鏂囧凡杩囨湡锛岄噸鏂板姞杞? userId={}", userId);
                    SaTokenHelper.clearLoginUserSession();
                }
            }

            // 缂撳瓨鏈懡涓紝閲嶆柊鍔犺浇
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

            log.debug("鐢ㄦ埛涓婁笅鏂囧姞杞? userId={}, homeTenantId={}, currentTenantId={}, currentRole={}, switchMode={}",
                    userId, loginUser.getHomeTenantId(), loginUser.getCurrentTenantId(),
                    loginUser.getCurrentRoleCode(), loginUser.getSwitchMode());

            return true;
        } catch (Exception e) {
            log.error("璁剧疆鐢ㄦ埛涓婁笅鏂囧け璐ワ紝鎷掔粷璇锋眰: uri={}", request.getRequestURI(), e);
            LoginContextHolder.clearContext();
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSONUtils.toJsonString(Result.fail(BizResultCode.UNAUTHORIZED, "用户上下文加载失败")));
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void loadScopeContext(Long userId, LoginUser loginUser) {
        try {
            UserDO user = authContextService.user(userId);
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
                        // 榛樿/褰撳墠绉熸埛
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
                        // 褰撳墠瑙掕壊
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
                    log.warn("extInfo 瑙ｆ瀽寮傚父: {}", e.getMessage());
                }
            }

            // 鍏滃簳 currentTenantId
            if (currentTenantId == null) {
                List<UserScopeRoleDO> assignments =
                        authContextService.workspaceRoles(userId);
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
                    ? null : authContextService.tenant(currentTenantId);
            if (currentTenantId == null || !isActive(currentTenant)) {
                throw new IllegalStateException("当前租户不存在或已停用");
            }

            List<UserScopeRoleDO> assignments =
                    authContextService.workspaceRoles(userId);
            final Long contextTenantId = currentTenantId;
            boolean assigned = assignments != null && assignments.stream().anyMatch(item ->
                    contextTenantId.equals(item.getTenantId())
                            && isActive(item)
                            && item.getRoleId() != null
                            && isActive(authContextService.role(item.getRoleId())));
            boolean delegated = "PARENT_SUPER_ADMIN".equals(loginUser.getSwitchMode())
                    && isParentSuperAdmin(userId, loginUser.getHomeTenantId(), assignments)
                    && authContextService.descendantTenantIds(loginUser.getHomeTenantId())
                            .contains(currentTenantId);
            if (!assigned && !delegated) {
                throw new IllegalStateException("鐢ㄦ埛涓嶅啀灞炰簬褰撳墠绉熸埛");
            }
            if (delegated && loginUser.getCurrentRoleCode() == null) {
                RoleDO role = authContextService.tenantSuperRole(currentTenantId);
                if (isActive(role)) {
                    loginUser.setCurrentRoleId(role.getId());
                    loginUser.setCurrentRoleCode(role.getCode());
                }
            }

        } catch (Exception e) {
            log.error("鍔犺浇绉熸埛浣滅敤鍩熷紓甯? userId={}", userId, e);
            throw new IllegalStateException("加载用户租户作用域失败", e);
        }
    }

    /**
     * currentRole 淇濆瓨鍦ㄧ敤鎴锋墿灞曚俊鎭腑锛屼絾鏄惁浠嶇劧鏈夋晥蹇呴』浠ュ綋鍓嶆巿鏉冨叧绯讳负鍑嗐€?
     * 鐗瑰埆鏄?super 瑙掕壊涓嶈兘浠呬俊浠?extInfo锛屽惁鍒欐挙鏉冨悗浠嶅彲鑳界粫杩囩鎴疯繃婊ゃ€?
     */
    private boolean isCurrentRoleValid(Long userId, Long currentTenantId,
                                       Long roleId, String roleCode) {
        if (userId == null || currentTenantId == null || roleId == null || roleCode == null) {
            return false;
        }
        List<UserScopeRoleDO> assignments =
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
        RoleDO role = authContextService.role(roleId);
        return role != null
                && roleCode.equals(role.getCode())
                && (role.getStatus() == null || role.getStatus() == 1)
                && (role.getDelFlag() == null || role.getDelFlag() == 0);
    }

    private boolean isCachedContextValid(LoginUser loginUser) {
        if (loginUser.getCurrentTenantId() == null) {
            return false;
        }
        // 濮旀墭鍒板瓙绉熸埛鏃讹紝homeTenant/currentTenant/currentRole 閮芥潵鑷?user.ext_info銆?
        // 涓嶅鐢ㄦ棫 session锛岄伩鍏嶅垏鎹㈠悗浠嶆寜鏃х鎴疯鑹叉牎楠屻€?
        if (loginUser.isParentSuperAdminSwitch()) {
            return false;
        }
        UserDO user = authContextService.user(loginUser.getUserId());
        if (!isActive(user)) {
            return false;
        }
        TenantDO tenant = authContextService.tenant(loginUser.getCurrentTenantId());
        if (!isActive(tenant)) {
            return false;
        }
        List<UserScopeRoleDO> assignments =
                authContextService.workspaceRoles(loginUser.getUserId());
        boolean assigned = assignments != null && assignments.stream().anyMatch(item ->
                loginUser.getCurrentTenantId().equals(item.getTenantId())
                        && isActive(item)
                        && item.getRoleId() != null
                        && isActive(authContextService.role(item.getRoleId())));
        boolean delegated = "PARENT_SUPER_ADMIN".equals(loginUser.getSwitchMode())
                && isParentSuperAdmin(loginUser.getUserId(), loginUser.getHomeTenantId(), assignments)
                && authContextService.descendantTenantIds(loginUser.getHomeTenantId())
                    .contains(loginUser.getCurrentTenantId())
                && authContextService.tenantSuperRole(loginUser.getCurrentTenantId()) != null;
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
                RoleDO role = authContextService.role(item.getRoleId());
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
                .map(authContextService::role)
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
