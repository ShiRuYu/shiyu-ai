package com.shiyu.ai.agent.biz.auth.controller;

import com.shiyu.ai.agent.domain.request.LoginRequest;
import com.shiyu.ai.agent.domain.vo.LoginResponseVO;
import com.shiyu.ai.agent.biz.auth.service.AuthService;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 认证 Controller
 * 提供用户登录、登出等认证功能
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * 用户登录
     * POST /auth/login
     * @param request 登录请求（包含用户名、密码）
     * @return 登录响应（包含用户信息和访问令牌）
     */
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@RequestBody LoginRequest request) {
        log.info("收到登录请求：username={}", request.getUsername());
        
        try {
            // 验证参数
            if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return Result.fail("Username and password are required");
            }
            
            // 调用登录服务
            LoginResponseVO response = authService.login(request.getUsername(), request.getPassword());
            
            if (response == null) {
                return Result.fail("Username or password is incorrect.");
            }
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("登录失败：username={}", request.getUsername(), e);
            return Result.fail("登录失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户权限码
     * GET /auth/codes
     * @return 权限码列表
     */
    @GetMapping("/codes")
    public Result<List<String>> getAuthCodes() {
        log.info("收到获取权限码请求");
        
        try {
            // 从 LoginHelper 获取当前登录用户 ID
            Long userId = LoginHelper.getUserId();
            log.debug("当前登录用户 ID: {}", userId);
            
            // 通过用户 ID 查询权限码
            List<String> codes = authService.getAuthCodesByUserId(userId);
            return Result.success(codes);
            
        } catch (Exception e) {
            log.error("获取权限码失败", e);
            return Result.fail("获取权限码失败：" + e.getMessage());
        }
    }
    
    /**
     * 刷新访问令牌
     * POST /auth/refresh
     * @param request 包含旧 token 的请求体
     * @return 新的访问令牌
     */
    @PostMapping("/refresh")
    public Result<String> refreshToken(@RequestBody Map<String, String> request) {
        log.info("收到刷新令牌请求");
        
        try {
            String oldToken = request.get("accessToken");
            if (oldToken == null || oldToken.trim().isEmpty()) {
                return Result.fail("Access token is required");
            }
            
            String newAccessToken = authService.refreshToken(oldToken);
            if (newAccessToken == null) {
                return Result.fail("Invalid access token");
            }
            
            return Result.success(newAccessToken);
            
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return Result.fail("刷新令牌失败：" + e.getMessage());
        }
    }
    

    /**
     * 用户登出
     * POST /auth/logout
     * @param tokenHeader Authorization Header（Bearer Token）
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<String> logout(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader) {
        log.info("收到登出请求");
        
        try {
            String token = extractTokenFromHeader(tokenHeader);
            if (token != null && !token.trim().isEmpty()) {
                authService.logout(token);
            }
            
            return Result.success("");
            
        } catch (Exception e) {
            log.error("登出失败", e);
            return Result.fail("登出失败：" + e.getMessage());
        }
    }
    
    /**
     * 从 Authorization Header 中提取 Token
     */
    private String extractTokenFromHeader(String tokenHeader) {
        if (tokenHeader == null || tokenHeader.trim().isEmpty()) {
            return null;
        }
        
        // 移除 Bearer 前缀
        if (tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7);
        }
        
        return tokenHeader;
    }
}
