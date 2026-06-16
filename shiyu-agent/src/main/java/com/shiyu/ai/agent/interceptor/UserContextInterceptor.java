package com.shiyu.ai.agent.interceptor;

import com.shiyu.ai.agent.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.agent.dal.dataobject.auth.UserDO;
import com.shiyu.ai.agent.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.agent.dal.mapper.auth.RoleMapper;
import com.shiyu.ai.agent.dal.mapper.auth.UserMapper;
import com.shiyu.ai.agent.dal.mapper.auth.UserWorkspaceRoleMapper;
import com.shiyu.ai.agent.utils.SaTokenHelper;
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

    private final UserMapper userMapper;
    private final UserWorkspaceRoleMapper userWorkspaceRoleMapper;
    private final RoleMapper roleMapper;

    public UserContextInterceptor(UserMapper userMapper, UserWorkspaceRoleMapper userWorkspaceRoleMapper,
            RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.userWorkspaceRoleMapper = userWorkspaceRoleMapper;
        this.roleMapper = roleMapper;
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
                log.debug("用户未登录，跳过用户上下文设置");
                return true;
            }

            Long userId = SaTokenHelper.getCurrentUserId();

            LoginUser loginUser = new LoginUser();
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

            // 加载租户和空间上下文
            loadTenantWorkspaceContext(userId, loginUser);

            LoginContextHolder.setContext(loginUser);

            log.debug("用户上下文设置成功: userId={}, tenantId={}, workspaceId={}, uri={}",
                    userId, loginUser.getTenantId(), loginUser.getCurrentWorkspaceId(), request.getRequestURI());

            return true;

        } catch (Exception e) {
            log.warn("设置用户上下文失败: uri={}, error={}", request.getRequestURI(), e.getMessage());
            return true;
        }
    }

    private void loadTenantWorkspaceContext(Long userId, LoginUser loginUser) {
        try {
            UserDO user = userMapper.selectOneById(userId);
            if (user == null) return;

            loginUser.setUsername(user.getUsername());
            loginUser.setNickName(user.getNickName());
            loginUser.setAvatar(user.getAvatar());

            // 解析 ext_info 获取上次保存的租户/空间偏好
            Long currentTenantId = null;
            Long currentWorkspaceId = null;
            if (user.getExtInfo() != null && !user.getExtInfo().isEmpty()) {
                try {
                    Map<String, Object> extInfo = JSONUtils.parseObject(user.getExtInfo(), Map.class);
                    if (extInfo != null) {
                        Object tenantObj = extInfo.get("currentTenantId");
                        if (tenantObj instanceof Number) {
                            currentTenantId = ((Number) tenantObj).longValue();
                        }
                        Object wsObj = extInfo.get("currentWorkspaceId");
                        if (wsObj instanceof Number) {
                            currentWorkspaceId = ((Number) wsObj).longValue();
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            // 查 user_workspace_role
            List<UserWorkspaceRoleDO> uwrList = userWorkspaceRoleMapper.selectByUserId(userId);

            if (uwrList == null || uwrList.isEmpty()) {
                log.warn("用户 {} 未分配任何空间", userId);
                loginUser.setWorkspaceIds(new ArrayList<>());
                loginUser.setWorkspaceRoleMap(new HashMap<>());
                return;
            }

            // 按当前租户过滤（如果有偏好）
            Long effectiveTenantId = currentTenantId;
            if (effectiveTenantId == null && user.getTenantId() != null) {
                effectiveTenantId = user.getTenantId();
            }
            // 如果两者都无，取第一条记录的 tenant_id
            if (effectiveTenantId == null && !uwrList.isEmpty()) {
                effectiveTenantId = uwrList.get(0).getTenantId();
            }

            final Long filterTenantId = effectiveTenantId;
            List<UserWorkspaceRoleDO> filtered = uwrList;
            if (filterTenantId != null) {
                filtered = uwrList.stream()
                        .filter(r -> filterTenantId.equals(r.getTenantId()))
                        .collect(Collectors.toList());
                if (filtered.isEmpty()) {
                    filtered = uwrList; // 回退到全部
                }
            }

            // 构建 workspaceIds 和 workspaceRoleMap
            List<Long> workspaceIds = filtered.stream()
                    .map(UserWorkspaceRoleDO::getWorkspaceId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量加载角色 code
            Set<Long> roleIds = filtered.stream()
                    .map(UserWorkspaceRoleDO::getRoleId)
                    .collect(Collectors.toSet());
            Map<Long, String> roleCodeMap = new HashMap<>();
            for (Long roleId : roleIds) {
                RoleDO role = roleMapper.selectOneById(roleId);
                if (role != null) {
                    roleCodeMap.put(roleId, role.getCode());
                }
            }

            Map<Long, String> workspaceRoleMap = new HashMap<>();
            for (UserWorkspaceRoleDO uwr : filtered) {
                String code = roleCodeMap.get(uwr.getRoleId());
                if (code != null) {
                    workspaceRoleMap.putIfAbsent(uwr.getWorkspaceId(), code);
                }
            }

            loginUser.setTenantId(filterTenantId);
            loginUser.setCurrentWorkspaceId(currentWorkspaceId);
            loginUser.setWorkspaceIds(workspaceIds);
            loginUser.setWorkspaceRoleMap(workspaceRoleMap);

        } catch (Exception e) {
            log.warn("加载租户/空间上下文失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LoginContextHolder.clearContext();
        log.debug("用户上下文已清理: uri={}", request.getRequestURI());
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
