package com.shiyu.ai.auth.interceptor;

import com.shiyu.ai.dal.auth.repository.AuthUserLookupRepository;
import com.shiyu.ai.dal.auth.repository.TenantRepository;
import com.shiyu.ai.dal.auth.dataobject.TenantDO;
import com.shiyu.ai.dal.auth.dataobject.UserDO;
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
                loginUser.setToken(SaTokenHelper.getCurrentToken());
                loginUser.setIpaddr(getClientIp(request));
                String ua = request.getHeader("User-Agent");
                if (ua != null) {
                    loginUser.setBrowser(parseBrowser(ua));
                    loginUser.setOs(parseOS(ua));
                }
                LoginContextHolder.setContext(loginUser);
                return true;
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

            log.debug("用户上下文加载: userId={}, scopeTenantId={}, visibleTenantIds={}, scopedTenantId={}",
                    userId, loginUser.getScopeTenantId(), loginUser.getVisibleTenantIds(), loginUser.getScopedTenantId());

            return true;
        } catch (Exception e) {
            log.warn("设置用户上下文失败: uri={}, error={}", request.getRequestURI(), e.getMessage());
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    private void loadScopeContext(Long userId, LoginUser loginUser) {
        try {
            UserDO user = authUserLookupRepository.selectUserById(userId);
            if (user == null) return;

            loginUser.setUsername(user.getUsername());
            loginUser.setNickName(user.getNickName());
            loginUser.setAvatar(user.getAvatar());

            Long currentScopeTenantId = null;
            if (user.getExtInfo() != null && !user.getExtInfo().isEmpty()) {
                try {
                    Map<String, Object> extInfo = JSONUtils.parseObject(user.getExtInfo(), Map.class);
                    if (extInfo != null) {
                        // 作用域租户
                        Object tid = extInfo.get("scopeTenantId");
                        if (tid instanceof Number) {
                            currentScopeTenantId = ((Number) tid).longValue();
                        }
                        // 历史遗留兼容
                        if (currentScopeTenantId == null && extInfo.get("currentTenantId") instanceof Number t) {
                            currentScopeTenantId = t.longValue();
                        }
                        // 子租户筛选器
                        Object sid = extInfo.get("scopedTenantId");
                        if (sid instanceof Number) {
                            loginUser.setScopedTenantId(((Number) sid).longValue());
                        }
                        // 当前角色
                        Object roleObj = extInfo.get("currentRole");
                        if (roleObj instanceof Map roleMap) {
                            Object roleKey = ((Map<String, Object>) roleMap).get("roleKey");
                            if (roleKey instanceof String) {
                                loginUser.setCurrentRoleCode((String) roleKey);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("extInfo 解析异常: {}", e.getMessage());
                }
            }

            // 兜底 scopeTenantId
            if (currentScopeTenantId == null) {
                if (user.getTenantId() != null) {
                    currentScopeTenantId = user.getTenantId();
                }
            }

            loginUser.setScopeTenantId(currentScopeTenantId);

            // 计算可见范围（scope 自身 + 所有后代）
            if (currentScopeTenantId != null) {
                List<Long> descendantIds = tenantRepository.selectDescendantIds(currentScopeTenantId);
                loginUser.setVisibleTenantIds(descendantIds);
            }

        } catch (Exception e) {
            log.warn("加载租户作用域异常: userId={}, error={}", userId, e.getMessage());
        }
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
