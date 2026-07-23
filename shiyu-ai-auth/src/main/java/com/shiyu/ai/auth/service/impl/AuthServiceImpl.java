package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.auth.repository.AuthRepository;
import com.shiyu.ai.dal.auth.repository.UserRepository;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.dal.auth.dataobject.RoleDO;
import com.shiyu.ai.dal.auth.dataobject.TenantDO;
import com.shiyu.ai.dal.auth.dataobject.UserWorkspaceRoleDO;
import com.shiyu.ai.dal.auth.dataobject.WorkspaceDO;
import com.shiyu.ai.dal.auth.repository.WorkspaceTenantRepository;

import com.shiyu.ai.dal.auth.repository.UserWorkspaceRoleRepository;

import com.shiyu.ai.dal.auth.bo.RoleBO;
import com.shiyu.ai.dal.auth.bo.UserBO;
import com.shiyu.ai.auth.vo.LoginResponseVO;
import com.shiyu.ai.auth.vo.WorkspaceContextVO;
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
    private final UserWorkspaceRoleRepository userWorkspaceRoleRepository;
    private final WorkspaceTenantRepository workspaceTenantRepository;
    
    
    private final HttpServletRequest request;

      @Value("${sa-token.timeout:7200}")
    private long tokenTimeout;

    public AuthServiceImpl(AuthRepository authRepository, UserRepository userRepository,
                           UserWorkspaceRoleRepository userWorkspaceRoleRepository, WorkspaceTenantRepository workspaceTenantRepository, HttpServletRequest request) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.userWorkspaceRoleRepository = userWorkspaceRoleRepository;
        this.workspaceTenantRepository = workspaceTenantRepository;
        
        
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

            List<RoleBO> roles = userRepository.selectRolesByUserId(user.getId());
            RoleBO currentRole = resolveCurrentRole(roleId, roles);

            // 查询用户工作空间角色列表
            List<UserWorkspaceRoleDO> uwrList = userWorkspaceRoleRepository.selectByUserId(user.getId());

            // 从 ext_info 解析当前租户和工作空间
            Long currentTenantId = resolveCurrentTenantId(user.getExtInfo(), uwrList);
            Long currentWorkspaceId = resolveCurrentWorkspaceId(user.getExtInfo(), uwrList, currentTenantId);

            // 获取租户名称
            String tenantName = null;
            if (currentTenantId != null) {
                TenantDO tenant = workspaceTenantRepository.selectTenantById(currentTenantId);
                if (tenant != null) {
                    tenantName = tenant.getName();
                }
            }

            // 更新 ext_info
            String loginIp = getClientIp();
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> extInfoMap = buildExtInfo(user.getExtInfo(), currentRole, currentTenantId, currentWorkspaceId, now, loginIp);
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

            // 构建工作空间上下文列表
            List<WorkspaceContextVO> workspaces = buildWorkspaceContextList(uwrList, currentTenantId);

            // 构建租户列表
            List<TenantInfoVO> tenantList = buildTenantList(uwrList);

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
            response.setCurrentWorkspaceId(currentWorkspaceId);
            response.setTenants(tenantList);
            response.setWorkspaces(workspaces);

            log.info("用户登录成功, username={}, userId={}, tenantId={}, workspaceId={}, roles={}",
                    username, user.getId(), currentTenantId, currentWorkspaceId, response.getRoles());
            return response;

        } catch (Exception e) {
            log.error("用户登录异常", username, e);
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
                WorkspaceDO ws = workspaceTenantRepository.selectWorkspaceById(uwr.getWorkspaceId());
                RoleDO role = workspaceTenantRepository.selectRoleById(uwr.getRoleId());
                result.add(WorkspaceContextVO.builder()
                        .workspaceId(uwr.getWorkspaceId())
                        .workspaceName(ws != null ? ws.getName() : "Unknown")
                        .roleCode(role != null ? role.getCode() : null)
                        .build());
            }
        }
        return result;
    }

    private List<TenantInfoVO> buildTenantList(List<UserWorkspaceRoleDO> uwrList) {
        if (uwrList == null || uwrList.isEmpty()) return new ArrayList<>();

        Set<Long> tenantIds = uwrList.stream()
                .map(UserWorkspaceRoleDO::getTenantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<TenantInfoVO> result = new ArrayList<>();
        for (Long tid : tenantIds) {
            TenantDO tenant = workspaceTenantRepository.selectTenantById(tid);
            if (tenant != null) {
                TenantInfoVO vo = new TenantInfoVO();
                vo.setId(tenant.getId());
                vo.setCode(tenant.getCode());
                vo.setName(tenant.getName());
                result.add(vo);
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
                    Long workspaceId = com.shiyu.ai.common.core.domain.LoginContextHolder.getCurrentWorkspaceId();
            List<String> codes = authRepository.selectCodesByUserId(userId, workspaceId);
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
            UserBO user = userRepository.selectById(userId);
            if (user == null) {
                log.warn("用户不存在, userId: {}", userId);
                return false;
            }
            List<RoleBO> roles = userRepository.selectRolesByUserId(userId);
            RoleBO target = resolveCurrentRole(roleId, roles);
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
            UserBO user = userRepository.selectById(userId);
            if (user == null) return false;

            Map<String, Object> extInfoMap = parseExtInfo(user.getExtInfo());
            extInfoMap.put("currentTenantId", tenantId);
            extInfoMap.remove("currentWorkspaceId");
            extInfoMap.remove("currentRole");

            // 查询用户在当前租户下的工作空间角色
            List<UserWorkspaceRoleDO> uwrList = userWorkspaceRoleRepository.selectByUserId(userId);
            if (uwrList != null && !uwrList.isEmpty()) {
                UserWorkspaceRoleDO first = uwrList.stream()
                        .filter(r -> tenantId.equals(r.getTenantId()))
                        .findFirst().orElse(null);
                if (first != null) {
                    extInfoMap.put("currentWorkspaceId", first.getWorkspaceId());
                    RoleDO role = workspaceTenantRepository.selectRoleById(first.getRoleId());
                    if (role != null) {
                        Map<String, Object> roleMap = new LinkedHashMap<>();
                        roleMap.put("roleId", role.getId());
                        roleMap.put("roleName", role.getName());
                        roleMap.put("roleKey", role.getCode());
                        extInfoMap.put("currentRole", roleMap);
                    }
                }
            }

            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            userRepository.update(user);

            log.info("切换租户成功, userId: {}, tenantId: {}", userId, tenantId);
            SaTokenHelper.clearLoginUserSession();
            return true;
        } catch (Exception e) {
            log.error("切换租户异常, userId: {}, tenantId: {}", userId, tenantId, e);
            return false;
        }
    }

    @Override
    public boolean switchCurrentWorkspace(Long userId, Long workspaceId) {
        log.info("切换工作空间, userId: {}, workspaceId: {}", userId, workspaceId);
        try {
            UserBO user = userRepository.selectById(userId);
            if (user == null) return false;

            Map<String, Object> extInfoMap = parseExtInfo(user.getExtInfo());
            extInfoMap.put("currentWorkspaceId", workspaceId);

            // 查询用户在当前工作空间下的角色
            List<UserWorkspaceRoleDO> uwrList = userWorkspaceRoleRepository.selectByUserId(userId);
            if (uwrList != null && !uwrList.isEmpty()) {
                UserWorkspaceRoleDO match = uwrList.stream()
                        .filter(r -> workspaceId.equals(r.getWorkspaceId()))
                        .findFirst().orElse(null);
                if (match != null) {
                    RoleDO role = workspaceTenantRepository.selectRoleById(match.getRoleId());
                    if (role != null) {
                        Map<String, Object> roleMap = new LinkedHashMap<>();
                        roleMap.put("roleId", role.getId());
                        roleMap.put("roleName", role.getName());
                        roleMap.put("roleKey", role.getCode());
                        extInfoMap.put("currentRole", roleMap);
                    }
                }
            }

            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            userRepository.update(user);

            log.info("切换工作空间成功, userId: {}, workspaceId: {}", userId, workspaceId);
            SaTokenHelper.clearLoginUserSession();
            return true;
        } catch (Exception e) {
            log.error("切换工作空间异常, userId: {}, workspaceId: {}", userId, workspaceId, e);
            return false;
        }
    }

    @Override
    public List<WorkspaceContextVO> getUserWorkspaces(Long userId) {
        List<UserWorkspaceRoleDO> uwrList = userWorkspaceRoleRepository.selectByUserId(userId);
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
    public List<TenantInfoVO> getUserTenants(Long userId) {
        List<UserWorkspaceRoleDO> uwrList = userWorkspaceRoleRepository.selectByUserId(userId);
        return buildTenantList(uwrList);
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
        userRepository.insert(user);
        log.info("用户注册成功: userId={}", user.getId());
        return login(username, password);
    }

    @Override
    public LoginResponseVO codeLogin(String phone, String code) {
        log.info("验证码登录: phone={}", phone);
        UserBO user = userRepository.selectByUsername(phone);
        if (user == null) {
            user = new UserBO();
            user.setUsername(phone);
            user.setPassword(PasswordUtils.encode(code));
            user.setStatus(1);
            userRepository.insert(user);
        }
        return login(user.getUsername(), code);
    }

    @Override
    public boolean forgetPassword(String email, String newPassword, String code) {
        log.info("忘记密码: email={}", email);
        UserBO user = userRepository.selectByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("该邮箱未注册: " + email);
        }
        user.setPassword(PasswordUtils.encode(newPassword));
        userRepository.update(user);
        log.info("密码重置成功: userId={}", user.getId());
        return true;
    }

    private Map<String, Object> parseExtInfo(String extInfo) {
        if (extInfo != null && !extInfo.isEmpty()) {
            Map<String, Object> map = JSONUtils.parseObject(extInfo, Map.class);
            if (map != null) return map;
        }
        return new LinkedHashMap<>();
    }
}

