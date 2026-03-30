package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.controller.response.LoginResponse;
import com.shiyu.ai.agent.domain.UserDO;
import com.shiyu.ai.agent.service.AuthService;
import com.shiyu.ai.agent.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 认证服务实现类（基于模拟数据）
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    
    private final CaptchaService captchaService;
    
    // 模拟用户数据库（实际项目中应该从数据库查询）
    private static final Map<String, UserDO> USER_DATABASE = new HashMap<>();
    
    // 模拟 token 存储（实际项目中应该使用 Redis 等）
    private static final Map<String, String> TOKEN_STORE = new HashMap<>();
    
    static {
        // 初始化模拟用户数据
        UserDO admin = new UserDO();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setNickName("超级管理员");
        admin.setEnable(true);
        USER_DATABASE.put("admin", admin);
        
        UserDO user = new UserDO();
        user.setId(2L);
        user.setUsername("user");
        user.setNickName("普通用户");
        user.setEnable(true);
        USER_DATABASE.put("user", user);
    }
    
    public AuthServiceImpl(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }
    
    @Override
    public LoginResponse login(String username, String password, String captcha, String captchaKey) {
        log.info("收到登录请求：username={}, captcha={}", username, captcha);
        
        LoginResponse response = new LoginResponse();
        
        try {
            // 1. 验证验证码
            if (!validateCaptcha(captchaKey, captcha)) {
                response.setCode(1);
                response.setMessage("验证码错误");
                response.setData(null);
                response.setOriginUrl("/auth/login");
                return response;
            }
            
            // 2. 查询用户信息
            UserDO user = USER_DATABASE.get(username);
            if (user == null) {
                log.warn("登录失败：用户不存在 - {}", username);
                response.setCode(1);
                response.setMessage("用户名或密码错误");
                response.setData(null);
                response.setOriginUrl("/auth/login");
                return response;
            }
            
            // 3. 验证用户状态
            if (!user.getEnable()) {
                log.warn("登录失败：用户已禁用 - {}", username);
                response.setCode(1);
                response.setMessage("账号已被禁用，请联系管理员");
                response.setData(null);
                response.setOriginUrl("/auth/login");
                return response;
            }
            
            // 4. 验证密码（模拟密码验证，实际项目中应该加密比对）
            // 这里简化处理：密码为 "123456" 或通过配置
            if (!"123456".equals(password)) {
                log.warn("登录失败：密码错误 - {}", username);
                response.setCode(1);
                response.setMessage("用户名或密码错误");
                response.setData(null);
                response.setOriginUrl("/auth/login");
                return response;
            }
            
            // 5. 生成访问令牌
            String accessToken = generateAccessToken(user);
            
            // 6. 构建响应数据
            LoginResponse.LoginData data = new LoginResponse.LoginData();
            data.setAccessToken(accessToken);
            
            response.setCode(0);
            response.setMessage("OK");
            response.setData(data);
            response.setOriginUrl("/auth/login");
            
            log.info("登录成功：username={}, accessToken={}", username, accessToken);
            
        } catch (Exception e) {
            log.error("登录失败：{}", username, e);
            response.setCode(1);
            response.setMessage("登录失败：" + e.getMessage());
            response.setData(null);
            response.setOriginUrl("/auth/login");
        }
        
        return response;
    }
    
    @Override
    public LoginResponse switchCurrentRole(String username, String roleCode) {
        log.info("收到切换角色请求：username={}, roleCode={}", username, roleCode);
        
        LoginResponse response = new LoginResponse();
        
        try {
            // 1. 查询用户信息
            UserDO user = USER_DATABASE.get(username);
            if (user == null) {
                log.warn("切换角色失败：用户不存在 - {}", username);
                response.setCode(1);
                response.setMessage("用户不存在");
                response.setData(null);
                response.setOriginUrl("/auth/current-role/switch/" + roleCode);
                return response;
            }
            
            // 2. 验证用户状态
            if (!user.getEnable()) {
                log.warn("切换角色失败：用户已禁用 - {}", username);
                response.setCode(1);
                response.setMessage("账号已被禁用，请联系管理员");
                response.setData(null);
                response.setOriginUrl("/auth/current-role/switch/" + roleCode);
                return response;
            }
            
            // 3. 查找目标角色
            RoleDO targetRole = findRoleByCode(user, roleCode);
            if (targetRole == null) {
                log.warn("切换角色失败：角色不存在 - {}, username={}", roleCode, username);
                response.setCode(1);
                response.setMessage("角色不存在或无权限使用此角色");
                response.setData(null);
                response.setOriginUrl("/auth/current-role/switch/" + roleCode);
                return response;
            }
            
            // 4. 验证角色状态
            if (!targetRole.getEnable()) {
                log.warn("切换角色失败：角色已禁用 - {}", roleCode);
                response.setCode(1);
                response.setMessage("角色已被禁用，请联系管理员");
                response.setData(null);
                response.setOriginUrl("/auth/current-role/switch/" + roleCode);
                return response;
            }
            
            // 5. 更新用户的当前角色
            user.setCurrentRole(targetRole);
            
            // 6. 生成新的访问令牌（包含新角色信息）
            String accessToken = generateAccessToken(user);
            
            // 7. 构建响应数据
            LoginResponse.LoginData data = new LoginResponse.LoginData();
            data.setAccessToken(accessToken);
            
            response.setCode(0);
            response.setMessage("OK");
            response.setData(data);
            response.setOriginUrl("/auth/current-role/switch/" + roleCode);
            
            log.info("切换角色成功：username={}, roleCode={}, accessToken={}", username, roleCode, accessToken);
            
        } catch (Exception e) {
            log.error("切换角色失败：username={}, roleCode={}", username, roleCode, e);
            response.setCode(1);
            response.setMessage("切换角色失败：" + e.getMessage());
            response.setData(null);
            response.setOriginUrl("/auth/current-role/switch/" + roleCode);
        }
        
        return response;
    }
    
    /**
     * 根据角色编码查找角色
     */
    private RoleDO findRoleByCode(UserDO user, String roleCode) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return null;
        }
        
        return user.getRoles().stream()
                .filter(role -> role.getCode().equals(roleCode))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public void logout(String accessToken) {
        log.info("收到登出请求：accessToken={}", accessToken);
        
        // 从 token 存储中移除
        TOKEN_STORE.remove(accessToken);
        
        log.info("登出成功：accessToken={}", accessToken);
    }
    
    /**
     * 验证验证码
     */
    private boolean validateCaptcha(String captchaKey, String captcha) {
        if (captchaKey == null || captchaKey.trim().isEmpty()) {
            // 如果没有提供 captchaKey，则不验证验证码（开发环境方便测试）
            log.debug("未提供验证码 key，跳过验证");
            return true;
        }
        
        boolean valid = captchaService.validateCaptcha(captchaKey, captcha);
        if (!valid) {
            log.warn("验证码验证失败");
        }
        return valid;
    }
    
    /**
     * 生成访问令牌
     */
    private String generateAccessToken(UserDO user) {
        // 生成简单的 token（实际项目中应该使用 JWT 等安全令牌）
        String accessToken = "access-token:" + user.getUsername() + ":" + user.getNickName();
        
        // 存储 token（设置过期时间等）
        TOKEN_STORE.put(accessToken, user.getUsername());
        
        return accessToken;
    }
}
