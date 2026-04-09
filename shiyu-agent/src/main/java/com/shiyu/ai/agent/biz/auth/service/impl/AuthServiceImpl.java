package com.shiyu.ai.agent.biz.auth.service.impl;

import com.shiyu.ai.agent.biz.auth.repository.AuthRepository;
import com.shiyu.ai.agent.biz.auth.repository.UserRepository;
import com.shiyu.ai.agent.biz.auth.service.AuthService;
import com.shiyu.ai.agent.domain.bo.RoleBO;
import com.shiyu.ai.agent.domain.bo.UserBO;
import com.shiyu.ai.agent.domain.vo.LoginResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证服务实现类（基于模拟数据）
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    
    // 模拟 token 存储（实际项目中应该使用 Redis 等）
    private static final Map<String, String> TOKEN_STORE = new HashMap<>();
    
    // 模拟 refresh token 存储
    private static final Map<String, String> REFRESH_TOKEN_STORE = new HashMap<>();
    
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
            
            // 3. 验证密码
            if (password == null || !password.equals(user.getPassword())) {
                log.warn("登录失败：密码错误 - {}", username);
                return null;
            }
            
            // 4. 从数据库查询用户的角色列表
            List<RoleBO> roles = userRepository.selectRolesByUserId(user.getId());
            
            // 5. 生成访问令牌和刷新令牌
            String accessToken = generateAccessToken(user);
            String refreshToken = generateRefreshToken(user);
            
            // 6. 构建响应数据
            LoginResponseVO response = new LoginResponseVO();
            response.setId(user.getId());
            response.setPassword(user.getPassword());
            response.setRealName(user.getUsername());
            response.setUsername(user.getUsername());
            response.setHomePath("/workspace"); // 默认首页
            
            // 设置角色列表（从数据库查询的角色信息）
            if (roles != null && !roles.isEmpty()) {
                response.setRoles(roles.stream()
                        .map(RoleBO::getCode)
                        .collect(Collectors.toList()));
            } else {
                response.setRoles(new ArrayList<>());
            }
            
            response.setAccessToken(accessToken);
            
            log.info("登录成功：username={}, roles={}, accessToken={}", username, response.getRoles(), accessToken);
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
    public String refreshToken(String refreshToken) {
        log.info("刷新访问令牌");
        
        // 验证 refresh token
        String username = REFRESH_TOKEN_STORE.get(refreshToken);
        if (username == null) {
            log.warn("无效的 refresh token");
            return null;
        }
        
        // 从数据库查询用户
        UserBO user = userRepository.selectByUsername(username);
        if (user == null) {
            log.warn("用户不存在：{}", username);
            return null;
        }
        
        // 生成新的 access token
        String newAccessToken = generateAccessToken(user);
        log.info("刷新令牌成功：username={}", username);
        
        return newAccessToken;
    }
    
    @Override
    public void logout(String refreshToken) {
        log.info("收到登出请求");
        
        // 从 refresh token 存储中移除
        String username = REFRESH_TOKEN_STORE.remove(refreshToken);
        
        // 同时移除相关的 access token
        if (username != null) {
            TOKEN_STORE.entrySet().removeIf(entry -> entry.getValue().equals(username));
        }
        
        log.info("登出成功");
    }
    
    /**
     * 生成访问令牌
     */
    private String generateAccessToken(UserBO user) {
        // 生成简单的 token（实际项目中应该使用 JWT 等安全令牌）
        String accessToken = "access-token:" + user.getUsername() + ":" + System.currentTimeMillis();
        
        // 存储 token（设置过期时间等）
        TOKEN_STORE.put(accessToken, user.getUsername());
        
        return accessToken;
    }
    
    /**
     * 生成刷新令牌
     */
    private String generateRefreshToken(UserBO user) {
        // 生成简单的 refresh token
        String refreshToken = "refresh-token:" + user.getUsername() + ":" + System.currentTimeMillis();
        
        // 存储 refresh token
        REFRESH_TOKEN_STORE.put(refreshToken, user.getUsername());
        
        return refreshToken;
    }
}
