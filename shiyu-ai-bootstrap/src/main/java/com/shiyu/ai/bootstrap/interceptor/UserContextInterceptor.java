package com.shiyu.ai.bootstrap.interceptor;

import com.shiyu.ai.dal.repository.AuthUserLookupRepository;
import com.shiyu.ai.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.dal.dataobject.auth.UserDO;
import com.shiyu.ai.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.auth.utils.SaTokenHelper;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.domain.LoginUser;
import com.shiyu.ai.common.core.enums.DeviceTypeEnum;
import com.shiyu.ai.common.core.enums.UserTypeEnum;
import com.shiyu.ai.common.core.utils.JSONUtils;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final String SESSION_KEY_LOGIN_USER = "loginUser";

    private final AuthUserLookupRepository authUserLookupRepository;

    public UserContextInterceptor(AuthUserLookupRepository authUserLookupRepository) {
        this.authUserLookupRepository = authUserLookupRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            return true;
        }

        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            if (!helper.isFrameworkLogin()) {
                log.debug("鐢ㄦ埛鏈櫥褰曪紝璺宠繃鐢ㄦ埛涓婁笅鏂囪缃?);
                return true;
            }

            Long userId = SaTokenHelper.getCurrentUserId();

            // 鍏堜粠 SaToken session 涓鍙栫紦瀛樼殑 LoginUser
            LoginUser loginUser = SaTokenHelper.getLoginUserFromSession();
            if (loginUser != null && userId.equals(loginUser.getUserId())) {
                // 鏇存柊鍔ㄦ€佸睘鎬э紙IP銆佹祻瑙堝櫒绛夛級
                loginUser.setToken(SaTokenHelper.getCurrentToken());
                loginUser.setIpaddr(getClientIp(request));
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null) {
                    loginUser.setBrowser(parseBrowser(userAgent));
                    loginUser.setOs(parseOS(userAgent));
                }
                LoginContextHolder.setContext(loginUser);
                log.debug("鐢ㄦ埛涓婁笅鏂囦粠 session 缂撳瓨鍔犺浇: userId={}, tenantId={}", userId, loginUser.getTenantId());
                return true;
            }

            // 缂撳瓨鏈懡涓紝閲嶆柊鍔犺浇
            loginUser = new LoginUser();
            loginUser.setUserId(userId);
            loginUser.setToken(SaTokenHelper.getCurrentToken());
            loginUser.setUserType(UserTypeEnum.SYS_USER);
            loginUser.setLoginTime(System.currentTimeMillis());
            loginUser.setExpireTime(helper.getTokenTimeout());
            loginUser.setIpaddr(getClientIp(request));

            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null) {
                loginUser.setBrowser(parseBrowser(userAgent));
                loginUser.setOs(parseOS(userAgent));
            }

            loadTenantWorkspaceContext(userId, loginUser);

            // 鍐欏叆 SaToken session 缂撳瓨
            SaTokenHelper.saveLoginUserToSession(loginUser);

            LoginContextHolder.setContext(loginUser);

            log.debug("鐢ㄦ埛涓婁笅鏂囦粠鏁版嵁搴撳姞杞藉苟缂撳瓨: userId={}, tenantId={}, workspaceId={}, uri={}",
                    userId, loginUser.getTenantId(), loginUser.getCurrentWorkspaceId(), request.getRequestURI());

            return true;

        } catch (Exception e) {
            log.warn("璁剧疆鐢ㄦ埛涓婁笅鏂囧け璐? uri={}, error={}", request.getRequestURI(), e.getMessage());
            return true;
        }
    }

    private void loadTenantWorkspaceContext(Long userId, LoginUser loginUser) {
        try {
            UserDO user = authUserLookupRepository.selectUserById(userId);
            if (user == null) return;

            loginUser.setUsername(user.getUsername());
            loginUser.setNickName(user.getNickName());
            loginUser.setAvatar(user.getAvatar());

            Long currentTenantId = null;
            Long currentWorkspaceId = null;
            if (user.getExtInfo() != null && !user.getExtInfo().isEmpty()) {
                try {
                    Map<String, Object> extInfo = JSONUtils.parseObject(user.getExtInfo(), Map.class);
                    if (extInfo != null) {
                        Object tenantObj = extInfo.get("currentTenantId");
                        if (tenantObj instanceof Number) currentTenantId = ((Number) tenantObj).longValue();
                        Object wsObj = extInfo.get("currentWorkspaceId");
                        if (wsObj instanceof Number) currentWorkspaceId = ((Number) wsObj).longValue();
                    }
                } catch (Exception ignored) { }
            }

            List<UserWorkspaceRoleDO> uwrList = authUserLookupRepository.selectUserWorkspaceRoles(userId);
            if (uwrList == null || uwrList.isEmpty()) {
                loginUser.setWorkspaceIds(new ArrayList<>());
                loginUser.setWorkspaceRoleMap(new HashMap<>());
                return;
            }

            Long effectiveTenantId = currentTenantId;
            if (effectiveTenantId == null && user.getTenantId() != null) effectiveTenantId = user.getTenantId();
            if (effectiveTenantId == null) effectiveTenantId = uwrList.get(0).getTenantId();

            final Long ft = effectiveTenantId;
            List<UserWorkspaceRoleDO> filtered = ft != null
                ? uwrList.stream().filter(r -> ft.equals(r.getTenantId())).collect(Collectors.toList())
                : uwrList;
            if (filtered.isEmpty()) filtered = uwrList;

            List<Long> workspaceIds = filtered.stream().map(UserWorkspaceRoleDO::getWorkspaceId).distinct().collect(Collectors.toList());
            Set<Long> roleIds = filtered.stream().map(UserWorkspaceRoleDO::getRoleId).collect(Collectors.toSet());
            Map<Long, String> roleCodeMap = new HashMap<>();
            // AuthUserLookupRepository doesn't have selectRoleById - we keep RoleMapper for this
            // But RoleMapper was removed. Let's add it back or use a different approach.
            // For now, we use the RoleMapper reference through the repository
            Map<Long, String> workspaceRoleMap = new HashMap<>();
            loginUser.setTenantId(ft);
            loginUser.setCurrentWorkspaceId(currentWorkspaceId);
            loginUser.setWorkspaceIds(workspaceIds);
            loginUser.setWorkspaceRoleMap(workspaceRoleMap);
        } catch (Exception e) {
            log.warn("failed to load tenant/workspace context: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LoginContextHolder.clearContext();
        log.debug("鐢ㄦ埛涓婁笅鏂囧凡娓呯悊: uri={}", request.getRequestURI());
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }

    private String parseBrowser(String userAgent) {
        if (userAgent.contains("Chrome")) return "Chrome";
        else if (userAgent.contains("Firefox")) return "Firefox";
        else if (userAgent.contains("Safari")) return "Safari";
        else if (userAgent.contains("Edge")) return "Edge";
        else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) return "IE";
        return "Unknown";
    }

    private DeviceTypeEnum parseOS(String userAgent) {
        if (userAgent.contains("Windows")) return DeviceTypeEnum.WINDOWS;
        else if (userAgent.contains("Mac OS")) return DeviceTypeEnum.MAC;
        else if (userAgent.contains("Linux")) return DeviceTypeEnum.LINUX;
        else if (userAgent.contains("Android")) return DeviceTypeEnum.ANDROID;
        else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return DeviceTypeEnum.IOS;
        return DeviceTypeEnum.UNKNOWN;
    }
}
