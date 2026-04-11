package com.shiyu.ai.agent.biz.auth.service.impl;

import com.shiyu.ai.agent.biz.auth.repository.AuthRepository;
import com.shiyu.ai.agent.biz.auth.repository.UserRepository;
import com.shiyu.ai.agent.biz.auth.service.AuthService;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.bo.UserBO;
import com.shiyu.ai.agent.domain.vo.LoginResponseVO;
import com.shiyu.ai.agent.utils.SaTokenHelper;
import com.shiyu.ai.common.core.utils.PasswordUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    
    @Value("${sa-token.timeout:7200}")
    private long tokenTimeout; // Token 过期时间（秒），默认 2 小时
    
    public AuthServiceImpl(AuthRepository authRepository, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public LoginResponseVO login(String username, String password) {
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
            if (password == null || !PasswordUtils.matches(password, user.getPassword())) {
                log.warn("登录失败：密码错误 - {}", username);
                return null;
            }
            
            // 4. 从数据库查询用户的角色列表
            List<RoleBO> roles = userRepository.selectRolesByUserId(user.getId());
            
            // 5. 从数据库查询用户的权限码列表
            List<String> permissions = authRepository.selectCodesByUsername(username);
            
            // 6. 单设备登录：先踢掉旧会话，再生成 Token
            SaTokenHelper helper = SaTokenHelper.getInstance();
            String accessToken = helper.loginWithKickout(user.getId());
            
            // 7. 获取 Token 过期时间
            long timeout = helper.getTokenTimeout();
            
            // 9. 构建响应数据
            LoginResponseVO response = new LoginResponseVO();
            response.setId(user.getId());
            // 注意：不要返回密码
            response.setRealName(user.getNickName() != null ? user.getNickName() : user.getUsername());
            response.setUsername(user.getUsername());
            response.setHomePath("/workspace"); // 默认首页
            
            // 设置角色列表
            if (roles != null && !roles.isEmpty()) {
                response.setRoles(roles.stream()
                        .map(RoleBO::getCode)
                        .collect(Collectors.toList()));
            } else {
                response.setRoles(new ArrayList<>());
            }
            
            // 设置权限码列表
            response.setPermissions(permissions != null ? permissions : new ArrayList<>());
            
            // 设置 Token 信息
            response.setAccessToken(accessToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(timeout);
            
            log.info("登录成功：username={}, userId={}, roles={}, permissionsCount={}", 
                    username, user.getId(), response.getRoles(), 
                    response.getPermissions() != null ? response.getPermissions().size() : 0);
            return response;
            
        } catch (Exception e) {
            log.error("登录失败：{}", username, e);
            return null;
        }
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
