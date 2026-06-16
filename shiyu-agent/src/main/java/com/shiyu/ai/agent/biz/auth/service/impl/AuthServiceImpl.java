package com.shiyu.ai.agent.biz.auth.service.impl;

import com.shiyu.ai.agent.biz.auth.repository.AuthRepository;
import com.shiyu.ai.agent.biz.auth.repository.UserRepository;
import com.shiyu.ai.agent.biz.auth.service.AuthService;
import com.shiyu.ai.agent.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.agent.dal.dataobject.auth.TenantDO;
import com.shiyu.ai.agent.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.agent.dal.dataobject.auth.WorkspaceDO;
import com.shiyu.ai.agent.dal.mapper.auth.RoleMapper;
import com.shiyu.ai.agent.dal.mapper.auth.TenantMapper;
import com.shiyu.ai.agent.dal.mapper.auth.UserWorkspaceRoleMapper;
import com.shiyu.ai.agent.dal.mapper.auth.WorkspaceMapper;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.bo.UserBO;
import com.shiyu.ai.agent.domain.vo.LoginResponseVO;
import com.shiyu.ai.agent.domain.vo.WorkspaceContextVO;
import com.shiyu.ai.agent.utils.SaTokenHelper;
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
    private final UserWorkspaceRoleMapper userWorkspaceRoleMapper;
    private final WorkspaceMapper workspaceMapper;
    private final RoleMapper roleMapper;
    private final TenantMapper tenantMapper;
    private final HttpServletRequest request;

    @Value("${sa-token.timeout:7200}")
    private long tokenTimeout;

    public AuthServiceImpl(AuthRepository authRepository, UserRepository userRepository,
                           UserWorkspaceRoleMapper userWorkspaceRoleMapper,
                           WorkspaceMapper workspaceMapper,
                           RoleMapper roleMapper,
                           TenantMapper tenantMapper,
                           HttpServletRequest request) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.userWorkspaceRoleMapper = userWorkspaceRoleMapper;
        this.workspaceMapper = workspaceMapper;
        this.roleMapper = roleMapper;
        this.tenantMapper = tenantMapper;
        this.request = request;
    }

    @Override
    public LoginResponseVO login(String username, String password) {
        return login(username, password, null);
    }

    @Override
    public LoginResponseVO login(String username, String password, Long roleId) {
        log.info("收到登录请求：username={}", username);

        try {
            UserBO user = userRepository.selectUserWithRolesByUsername(username);
            if (user == null) {
                log.warn("登录失败：用户不存在 - {}", username);
                return null;
            }

            if (!"1".equals(user.getStatus())) {
                log.warn("登录失败：用户已禁用 - {}", username);
                return null;
            }

            if (!PasswordUtils.matches(password, user.getPassword())) {
                log.warn("登录失败：密码错误 - {}", username);
                return null;
            }

            List<RoleBO> roles = userRepository.selectRolesByUserId(user.getId());
            RoleBO currentRole = resolveCurrentRole(roleId, roles);

            // 加载租户和工作空间上下文
            List<UserWorkspaceRoleDO> uwrList = userWorkspaceRoleMapper.selectByUserId(user.getId());

            // 确定租户：优先 ext_info 中的偏好，否则取第一个
            Long currentTenantId = resolveCurrentTenantId(user.getExtInfo(), uwrList);
            Long currentWorkspaceId = resolveCurrentWorkspaceId(user.getExtInfo(), uwrList, currentTenantId);

            // 查询租户名称
            String tenantName = null;
            if (currentTenantId != null) {
                TenantDO tenant = tenantMapper.selectOneById(currentTenantId);
                if (tenant != null) {
                    tenantName = tenant.getName();
                }
            }

            // 构建 ext_info
            String loginIp = getClientIp();
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> extInfoMap = buildExtInfo(user.getExtInfo(), currentRole, currentTenantId, currentWorkspaceId, now, loginIp);
            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            userRepository.update(user);

            // 生成 Token
            SaTokenHelper helper = SaTokenHelper.getInstance();
            String accessToken = helper.loginWithKickout(user.getId());
            long timeout = helper.getTokenTimeout();

            // 构建工作空间列表
            List<WorkspaceContextVO> workspaces = buildWorkspaceContextList(uwrList, currentTenantId);

            // 构建租户列表
            List<Map<String, Object>> tenantList = buildTenantList(uwrList);

            LoginResponseVO response = new LoginResponseVO();
            response.setId(user.getId());
            response.setRealName(user.getNickName() != null ? user.getNickName() : user.getUsername());
            response.setUsername(user.getUsername());
            response.setHomePath("/workspace");

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
            response.setTenantId(currentTenantId);
            response.setTenantName(tenantName);
            response.setTenants(tenantList);
            response.setWorkspaces(workspaces);

            log.info("登录成功：username={}, userId={}, tenantId={}, workspaceId={}, roles={}",
                    username, user.getId(), currentTenantId, currentWorkspaceId, response.getRoles());
            return response;

        } catch (Exception e) {
            log.error("登录失败：{}", username, e);
            return null;
        }
    }

    private Long resolveCurrentTenantId(String extInfo, List<UserWorkspaceRoleDO> uwrList) {
        if (extInfo != null && !extInfo.isEmpty()) {
            try {
                Map<String, Object> map = JSONUtils.parseObject(extInfo, Map.class);
                if (map != null && map.get("currentTenantId") instanceof Number) {
                    return ((Number) map.get("currentTenantId")).longValue();
                }
            } catch (Exception ignored) {
            }
        }
        if (uwrList != null && !uwrList.isEmpty()) {
            return uwrList.get(0).getTenantId();
        }
        return null;
    }

    private Long resolveCurrentWorkspaceId(String extInfo, List<UserWorkspaceRoleDO> uwrList, Long currentTenantId) {
        if (extInfo != null && !extInfo.isEmpty()) {
            try {
                Map<String, Object> map = JSONUtils.parseObject(extInfo, Map.class);
                if (map != null && map.get("currentWorkspaceId") instanceof Number) {
                    return ((Number) map.get("currentWorkspaceId")).longValue();
                }
            } catch (Exception ignored) {
            }
        }
        if (uwrList != null && !uwrList.isEmpty() && currentTenantId != null) {
            return uwrList.stream()
                    .filter(r -> currentTenantId.equals(r.getTenantId()))
                    .map(UserWorkspaceRoleDO::getWorkspaceId)
                    .findFirst().orElse(null);
        }
        return null;
    }

    private Map<String, Object> buildExtInfo(String oldExtInfo, RoleBO currentRole,
                                              Long currentTenantId, Long currentWorkspaceId,
                                              LocalDateTime now, String loginIp) {
        Map<String, Object> extInfoMap;
        if (oldExtInfo != null && !oldExtInfo.isEmpty()) {
            extInfoMap = JSONUtils.parseObject(oldExtInfo, Map.class);
            if (extInfoMap == null) extInfoMap = new LinkedHashMap<>();
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
        if (currentWorkspaceId != null) {
            extInfoMap.put("currentWorkspaceId", currentWorkspaceId);
        }

        return extInfoMap;
    }

    private List<WorkspaceContextVO> buildWorkspaceContextList(List<UserWorkspaceRoleDO> uwrList, Long currentTenantId) {
        if (uwrList == null || uwrList.isEmpty()) return new ArrayList<>();

        List<UserWorkspaceRoleDO> filtered = uwrList;
        if (currentTenantId != null) {
            filtered = uwrList.stream()
                    .filter(r -> currentTenantId.equals(r.getTenantId()))
                    .collect(Collectors.toList());
        }

        List<WorkspaceContextVO> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (UserWorkspaceRoleDO uwr : filtered) {
            if (seen.add(uwr.getWorkspaceId())) {
                WorkspaceDO ws = workspaceMapper.selectOneById(uwr.getWorkspaceId());
                RoleDO role = roleMapper.selectOneById(uwr.getRoleId());
                result.add(WorkspaceContextVO.builder()
                        .workspaceId(uwr.getWorkspaceId())
                        .workspaceName(ws != null ? ws.getName() : "Unknown")
                        .roleCode(role != null ? role.getCode() : null)
                        .build());
            }
        }
        return result;
    }

    private List<Map<String, Object>> buildTenantList(List<UserWorkspaceRoleDO> uwrList) {
        if (uwrList == null || uwrList.isEmpty()) return new ArrayList<>();

        Set<Long> tenantIds = uwrList.stream()
                .map(UserWorkspaceRoleDO::getTenantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long tid : tenantIds) {
            TenantDO tenant = tenantMapper.selectOneById(tid);
            if (tenant != null) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", tenant.getId());
                m.put("code", tenant.getCode());
                m.put("name", tenant.getName());
                result.add(m);
            }
        }
        return result;
    }

    private RoleBO resolveCurrentRole(Long roleId, List<RoleBO> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return null;
        }
        if (roleId != null) {
            return userRoles.stream()
                    .filter(r -> r.getId().equals(roleId))
                    .findFirst()
                    .orElse(userRoles.get(0));
        }
        return userRoles.get(0);
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
        log.info("获取用户权限码：username={}", username);
        try {
            List<String> codes = authRepository.selectCodesByUsername(username);
            if (codes == null || codes.isEmpty()) {
                return new ArrayList<>();
            }
            return codes;
        } catch (Exception e) {
            log.error("获取权限码失败：username={}", username, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getAuthCodesByUserId(Long userId) {
        log.info("获取用户权限码：userId={}", userId);
        try {
            List<String> codes = authRepository.selectCodesByUserId(userId);
            if (codes == null || codes.isEmpty()) {
                return new ArrayList<>();
            }
            return codes;
        } catch (Exception e) {
            log.error("获取权限码失败：userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public String refreshToken(String oldToken) {
        log.info("刷新访问令牌");
        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            Long userId = helper.getUserIdByToken(oldToken);
            if (userId == null) {
                log.warn("无效的 access token");
                return null;
            }
            String newAccessToken = helper.refreshToken(userId);
            log.info("刷新令牌成功：userId={}", userId);
            return newAccessToken;
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return null;
        }
    }

    @Override
    public boolean switchCurrentRole(Long userId, Long roleId) {
        log.info("切换当前角色，userId: {}, roleId: {}", userId, roleId);
        try {
            UserBO user = userRepository.selectById(userId);
            if (user == null) {
                log.warn("用户不存在，userId: {}", userId);
                return false;
            }
            List<RoleBO> roles = userRepository.selectRolesByUserId(userId);
            RoleBO target = resolveCurrentRole(roleId, roles);
            if (target == null) {
                log.warn("用户无可用角色，userId: {}", userId);
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

            log.info("切换角色成功，userId: {}, newRole: {}", userId, target.getName());
            return true;
        } catch (Exception e) {
            log.error("切换角色失败，userId: {}, roleId: {}", userId, roleId, e);
            return false;
        }
    }

    @Override
    public boolean switchCurrentTenant(Long userId, Long tenantId) {
        log.info("切换当前租户，userId: {}, tenantId: {}", userId, tenantId);
        try {
            UserBO user = userRepository.selectById(userId);
            if (user == null) return false;

            Map<String, Object> extInfoMap = parseExtInfo(user.getExtInfo());
            extInfoMap.put("currentTenantId", tenantId);
            extInfoMap.remove("currentWorkspaceId"); // 切租户时重置空间
            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            userRepository.update(user);

            log.info("切换租户成功，userId: {}, tenantId: {}", userId, tenantId);
            return true;
        } catch (Exception e) {
            log.error("切换租户失败，userId: {}, tenantId: {}", userId, tenantId, e);
            return false;
        }
    }

    @Override
    public boolean switchCurrentWorkspace(Long userId, Long workspaceId) {
        log.info("切换当前工作空间，userId: {}, workspaceId: {}", userId, workspaceId);
        try {
            UserBO user = userRepository.selectById(userId);
            if (user == null) return false;

            Map<String, Object> extInfoMap = parseExtInfo(user.getExtInfo());
            extInfoMap.put("currentWorkspaceId", workspaceId);
            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            userRepository.update(user);

            log.info("切换工作空间成功，userId: {}, workspaceId: {}", userId, workspaceId);
            return true;
        } catch (Exception e) {
            log.error("切换工作空间失败，userId: {}, workspaceId: {}", userId, workspaceId, e);
            return false;
        }
    }

    @Override
    public List<WorkspaceContextVO> getUserWorkspaces(Long userId) {
        List<UserWorkspaceRoleDO> uwrList = userWorkspaceRoleMapper.selectByUserId(userId);
        if (uwrList == null || uwrList.isEmpty()) return new ArrayList<>();

        UserBO user = userRepository.selectById(userId);
        Long currentTenantId = null;
        if (user != null && user.getExtInfo() != null) {
            Map<String, Object> extMap = parseExtInfo(user.getExtInfo());
            Object tid = extMap.get("currentTenantId");
            if (tid instanceof Number) {
                currentTenantId = ((Number) tid).longValue();
            }
        }
        if (currentTenantId == null && !uwrList.isEmpty()) {
            currentTenantId = uwrList.get(0).getTenantId();
        }

        return buildWorkspaceContextList(uwrList, currentTenantId);
    }

    @Override
    public void logout(String token) {
        log.info("收到登出请求");
        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            Long userId = helper.getUserIdByToken(token);
            if (userId != null) {
                helper.logout(userId);
                log.info("登出成功：userId={}", userId);
            } else {
                log.warn("无效的 token，无法登出");
            }
        } catch (Exception e) {
            log.error("登出失败", e);
        }
    }

    private Map<String, Object> parseExtInfo(String extInfo) {
        if (extInfo != null && !extInfo.isEmpty()) {
            Map<String, Object> map = JSONUtils.parseObject(extInfo, Map.class);
            if (map != null) return map;
        }
        return new LinkedHashMap<>();
    }
}
