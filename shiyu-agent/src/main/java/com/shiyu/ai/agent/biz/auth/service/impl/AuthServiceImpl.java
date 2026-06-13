package com.shiyu.ai.agent.biz.auth.service.impl;

import com.shiyu.ai.agent.biz.auth.repository.AuthRepository;
import com.shiyu.ai.agent.biz.auth.repository.UserRepository;
import com.shiyu.ai.agent.biz.auth.service.AuthService;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.bo.UserBO;
import com.shiyu.ai.agent.domain.vo.LoginResponseVO;
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

/**
 * 认证服务实现类（基于 SaToken）
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;
    
    @Value("${sa-token.timeout:7200}")
    private long tokenTimeout; // Token 过期时间（秒），默认 2 小时
    
    public AuthServiceImpl(AuthRepository authRepository, UserRepository userRepository,
                           HttpServletRequest request) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
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
            // 1. 从数据库查询用户信息（包含角色）
            UserBO user = userRepository.selectUserWithRolesByUsername(username);
            if (user == null) {
                log.warn("登录失败：用户不存在 - {}", username);
                return null;
            }

            // 2. 验证用户状态
            if (!"1".equals(user.getStatus())) {
                log.warn("登录失败：用户已禁用 - {}", username);
                return null;
            }

            // 3. 验证密码（使用 BCrypt）
            if (!PasswordUtils.matches(password, user.getPassword())) {
                log.warn("登录失败：密码错误 - {}", username);
                return null;
            }

            // 4. 从数据库查询用户的角色列表
            List<RoleBO> roles = userRepository.selectRolesByUserId(user.getId());

            // 5. 确定当前角色
            RoleBO currentRole = resolveCurrentRole(roleId, roles);

            // 6. 构建扩展信息并持久化
            String loginIp = getClientIp();
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> extInfoMap = new LinkedHashMap<>();
            extInfoMap.put("lastLoginTime", now.toString());
            extInfoMap.put("lastLoginIp", loginIp);
            if (currentRole != null) {
                Map<String, Object> roleMap = new LinkedHashMap<>();
                roleMap.put("roleId", currentRole.getId());
                roleMap.put("roleName", currentRole.getName());
                roleMap.put("roleKey", currentRole.getCode());
                extInfoMap.put("currentRole", roleMap);
            }
            user.setExtInfo(JSONUtils.toJsonString(extInfoMap));
            userRepository.update(user);

            // 7. 单设备登录：先踢掉旧会话，再生成 Token
            SaTokenHelper helper = SaTokenHelper.getInstance();
            String accessToken = helper.loginWithKickout(user.getId());

            // 8. 获取 Token 过期时间
            long timeout = helper.getTokenTimeout();

            // 9. 构建响应数据
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

            log.info("登录成功：username={}, userId={}, roles={}",
                    username, user.getId(), response.getRoles());
            return response;

        } catch (Exception e) {
            log.error("登录失败：{}", username, e);
            return null;
        }
    }

    /**
     * 解析当前角色：如果传了 roleId 则使用对应角色，否则使用第一个角色
     */
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

    /**
     * 获取客户端 IP
     */
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
            // 从数据库查询权限码
            List<String> codes = authRepository.selectCodesByUsername(username);
            
            if (codes == null || codes.isEmpty()) {
                log.warn("用户 {} 没有配置权限码", username);
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
            // 从数据库查询权限码（通过用户 ID）
            List<String> codes = authRepository.selectCodesByUserId(userId);
            
            if (codes == null || codes.isEmpty()) {
                log.warn("用户 ID {} 没有配置权限码", userId);
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
            // 1. 从旧 Token 中提取 userId
            SaTokenHelper helper = SaTokenHelper.getInstance();
            Long userId = helper.getUserIdByToken(oldToken);
            if (userId == null) {
                log.warn("无效的 access token");
                return null;
            }
            
            // 2. 刷新 Token（先登出再登录）
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
            RoleBO target = null;
            if (roleId != null) {
                target = roles.stream()
                        .filter(r -> r.getId().equals(roleId))
                        .findFirst().orElse(null);
            }
            if (target == null && !roles.isEmpty()) {
                target = roles.get(0);
            }
            if (target == null) {
                log.warn("用户无可用角色，userId: {}", userId);
                return false;
            }

            Map<String, Object> roleMap = new LinkedHashMap<>();
            roleMap.put("roleId", target.getId());
            roleMap.put("roleName", target.getName());
            roleMap.put("roleKey", target.getCode());

            Map<String, Object> extInfoMap;
            if (user.getExtInfo() != null && !user.getExtInfo().isEmpty()) {
                extInfoMap = JSONUtils.parseObject(user.getExtInfo(), Map.class);
                if (extInfoMap == null) {
                    extInfoMap = new LinkedHashMap<>();
                }
            } else {
                extInfoMap = new LinkedHashMap<>();
            }
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
    public void logout(String token) {
        log.info("收到登出请求");
        
        try {
            // 从 Token 中提取 userId
            SaTokenHelper helper = SaTokenHelper.getInstance();
            Long userId = helper.getUserIdByToken(token);
            if (userId != null) {
                // 执行登出
                helper.logout(userId);
                log.info("登出成功：userId={}", userId);
            } else {
                log.warn("无效的 token，无法登出");
            }
        } catch (Exception e) {
            log.error("登出失败", e);
        }
    }
}
