package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.dal.repository.AuthRepository;
import com.shiyu.ai.dal.repository.UserRepository;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.dal.dataobject.auth.RoleDO;
import com.shiyu.ai.dal.dataobject.auth.TenantDO;
import com.shiyu.ai.dal.dataobject.auth.UserWorkspaceRoleDO;
import com.shiyu.ai.dal.dataobject.auth.WorkspaceDO;
import com.shiyu.ai.dal.repository.WorkspaceTenantRepository;

import com.shiyu.ai.dal.repository.UserWorkspaceRoleRepository;

import com.shiyu.ai.model.bo.RoleBO;
import com.shiyu.ai.model.bo.UserBO;
import com.shiyu.ai.model.vo.LoginResponseVO;
import com.shiyu.ai.model.vo.WorkspaceContextVO;
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
        log.info("鐢ㄦ埛鐧诲綍寮€濮? username={}", username);

        try {
            UserBO user = userRepository.selectUserWithRolesByUsername(username);
            if (user == null) {
                log.warn("鐢ㄦ埛涓嶅瓨鍦?- {}", username);
                return null;
            }

            if (!"1".equals(user.getStatus())) {
                log.warn("鐢ㄦ埛宸茶绂佺敤 - {}", username);
                return null;
            }

            if (!PasswordUtils.matches(password, user.getPassword())) {
                log.warn("瀵嗙爜閿欒 - {}", username);
                return null;
            }

            List<RoleBO> roles = userRepository.selectRolesByUserId(user.getId());
            RoleBO currentRole = resolveCurrentRole(roleId, roles);

            // 鏌ヨ鐢ㄦ埛宸ヤ綔绌洪棿瑙掕壊鍒楄〃
            List<UserWorkspaceRoleDO> uwrList = userWorkspaceRoleRepository.selectByUserId(user.getId());

            // 浠?ext_info 瑙ｆ瀽褰撳墠绉熸埛鍜屽伐浣滅┖闂?
            Long currentTenantId = resolveCurrentTenantId(user.getExtInfo(), uwrList);
            Long currentWorkspaceId = resolveCurrentWorkspaceId(user.getExtInfo(), uwrList, currentTenantId);

            // 鑾峰彇绉熸埛鍚嶇О
            String tenantName = null;
            if (currentTenantId != null) {
                TenantDO tenant = workspaceTenantRepository.selectTenantById(currentTenantId);
                if (tenant != null) {
                    tenantName = tenant.getName();
                }
            }

            // 鏇存柊 ext_info
            String loginIp = getClientIp();
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> extInfoMap = buildExtInfo(user.getExtInfo(), currentRole, currentTenantId, currentWorkspaceId, now, loginIp);
            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));

            // 璁剧疆 LoginUser 涓婁笅鏂囷紙鐢ㄤ簬鑷姩鏇存柊锛?
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(user.getId());
            loginUser.setUsername(user.getUsername());
            LoginContextHolder.setContext(loginUser);
            try {
                userRepository.update(user);
            } finally {
                LoginContextHolder.clearContext();
            }

            // 鐢熸垚 Token
            SaTokenHelper helper = SaTokenHelper.getInstance();
            String accessToken = helper.loginWithKickout(user.getId());
            SaTokenHelper.clearLoginUserSession();
            long timeout = helper.getTokenTimeout();

            // 鏋勫缓宸ヤ綔绌洪棿涓婁笅鏂囧垪琛?
            List<WorkspaceContextVO> workspaces = buildWorkspaceContextList(uwrList, currentTenantId);

            // 鏋勫缓绉熸埛鍒楄〃
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

            log.info("鐢ㄦ埛鐧诲綍鎴愬姛, username={}, userId={}, tenantId={}, workspaceId={}, roles={}",
                    username, user.getId(), currentTenantId, currentWorkspaceId, response.getRoles());
            return response;

        } catch (Exception e) {
            log.error("鐢ㄦ埛鐧诲綍寮傚父", username, e);
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

    private List<Map<String, Object>> buildTenantList(List<UserWorkspaceRoleDO> uwrList) {
        if (uwrList == null || uwrList.isEmpty()) return new ArrayList<>();

        Set<Long> tenantIds = uwrList.stream()
                .map(UserWorkspaceRoleDO::getTenantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long tid : tenantIds) {
            TenantDO tenant = workspaceTenantRepository.selectTenantById(tid);
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
        log.info("鑾峰彇鏉冮檺缂栫爜, username={}", username);
        try {
            List<String> codes = authRepository.selectCodesByUsername(username);
            if (codes == null || codes.isEmpty()) {
                return new ArrayList<>();
            }
            return codes;
        } catch (Exception e) {
            log.error("鑾峰彇鏉冮檺缂栫爜寮傚父, username={}", username, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getAuthCodesByUserId(Long userId) {
        log.info("鑾峰彇鏉冮檺缂栫爜, userId={}", userId);
        try {
            List<String> codes = authRepository.selectCodesByUserId(userId);
            if (codes == null || codes.isEmpty()) {
                return new ArrayList<>();
            }
            return codes;
        } catch (Exception e) {
            log.error("鑾峰彇鏉冮檺缂栫爜寮傚父, userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public String refreshToken(String oldToken) {
        log.info("鍒锋柊Token");
        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            Long userId = helper.getUserIdByToken(oldToken);
            if (userId == null) {
                log.warn("鏃犳晥鐨刟ccess token");
                return null;
            }
            String newAccessToken = helper.refreshToken(userId);
            log.info("鍒锋柊Token鎴愬姛, userId={}", userId);
            return newAccessToken;
        } catch (Exception e) {
            log.error("鍒锋柊Token寮傚父", e);
            return null;
        }
    }

    @Override
    public boolean switchCurrentRole(Long userId, Long roleId) {
        log.info("鍒囨崲瑙掕壊, userId: {}, roleId: {}", userId, roleId);
        try {
            UserBO user = userRepository.selectById(userId);
            if (user == null) {
                log.warn("鐢ㄦ埛涓嶅瓨鍦? userId: {}", userId);
                return false;
            }
            List<RoleBO> roles = userRepository.selectRolesByUserId(userId);
            RoleBO target = resolveCurrentRole(roleId, roles);
            if (target == null) {
                log.warn("瑙掕壊涓嶅瓨鍦? userId: {}", userId);
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

            log.info("鍒囨崲瑙掕壊鎴愬姛, userId: {}, newRole: {}", userId, target.getName());
            SaTokenHelper.clearLoginUserSession();
            return true;
        } catch (Exception e) {
            log.error("鍒囨崲瑙掕壊寮傚父, userId: {}, roleId: {}", userId, roleId, e);
            return false;
        }
    }

    @Override
    public boolean switchCurrentTenant(Long userId, Long tenantId) {
        log.info("鍒囨崲绉熸埛, userId: {}, tenantId: {}", userId, tenantId);
        try {
            UserBO user = userRepository.selectById(userId);
            if (user == null) return false;

            Map<String, Object> extInfoMap = parseExtInfo(user.getExtInfo());
            extInfoMap.put("currentTenantId", tenantId);
            extInfoMap.remove("currentWorkspaceId");
            extInfoMap.remove("currentRole");

            // 鏌ヨ鐢ㄦ埛鍦ㄥ綋鍓嶇鎴蜂笅鐨勫伐浣滅┖闂磋鑹?
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

            log.info("鍒囨崲绉熸埛鎴愬姛, userId: {}, tenantId: {}", userId, tenantId);
            SaTokenHelper.clearLoginUserSession();
            return true;
        } catch (Exception e) {
            log.error("鍒囨崲绉熸埛寮傚父, userId: {}, tenantId: {}", userId, tenantId, e);
            return false;
        }
    }

    @Override
    public boolean switchCurrentWorkspace(Long userId, Long workspaceId) {
        log.info("鍒囨崲宸ヤ綔绌洪棿, userId: {}, workspaceId: {}", userId, workspaceId);
        try {
            UserBO user = userRepository.selectById(userId);
            if (user == null) return false;

            Map<String, Object> extInfoMap = parseExtInfo(user.getExtInfo());
            extInfoMap.put("currentWorkspaceId", workspaceId);

            // 鏌ヨ鐢ㄦ埛鍦ㄥ綋鍓嶅伐浣滅┖闂翠笅鐨勮鑹?
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

            log.info("鍒囨崲宸ヤ綔绌洪棿鎴愬姛, userId: {}, workspaceId: {}", userId, workspaceId);
            SaTokenHelper.clearLoginUserSession();
            return true;
        } catch (Exception e) {
            log.error("鍒囨崲宸ヤ綔绌洪棿寮傚父, userId: {}, workspaceId: {}", userId, workspaceId, e);
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
    public List<Map<String, Object>> getUserTenants(Long userId) {
        List<UserWorkspaceRoleDO> uwrList = userWorkspaceRoleRepository.selectByUserId(userId);
        return buildTenantList(uwrList);
    }

    @Override
    public void logout(String token) {
        log.info("娉ㄩ攢閫€鍑?);
        try {
            SaTokenHelper helper = SaTokenHelper.getInstance();
            Long userId = helper.getUserIdByToken(token);
            if (userId != null) {
                helper.logout(userId);
                log.info("閫€鍑虹櫥褰曟垚鍔? userId={}", userId);
            } else {
                log.warn("鏃犳晥鐨則oken, 娉ㄩ攢澶辫触");
            }
        } catch (Exception e) {
            log.error("娉ㄩ攢寮傚父", e);
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

