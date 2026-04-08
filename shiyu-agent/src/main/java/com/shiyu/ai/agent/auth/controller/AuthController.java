package com.shiyu.ai.agent.auth.controller;

import com.shiyu.ai.agent.domain.request.LoginRequest;
import com.shiyu.ai.agent.domain.vo.LoginResponseVO;
import com.shiyu.ai.agent.auth.service.AuthService;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Result<LoginResponseVO>> login(@RequestBody LoginRequest request) {
        log.info("收到登录请求：username={}", request.getUsername());
        
        try {
            // 验证参数
            if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Result.fail("Username and password are required"));
            }
            
            // 调用登录服务
            LoginResponseVO response = authService.login(request.getUsername(), request.getPassword());
            
            if (response == null) {
                return ResponseEntity.status(403).body(Result.fail("Username or password is incorrect."));
            }
            
            return ResponseEntity.ok(Result.success(response));
            
        } catch (Exception e) {
            log.error("登录失败：username={}", request.getUsername(), e);
            return ResponseEntity.badRequest().body(Result.fail("登录失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取用户权限码
     * GET /auth/codes
     * @param token 访问令牌（从 Header 中获取）
     * @return 权限码列表
     */
    @GetMapping("/codes")
    public ResponseEntity<Result<List<String>>> getAuthCodes(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("收到获取权限码请求");
        
        try {
            String username = extractUsernameFromToken(token);
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.status(401).body(Result.fail("未认证或 token 无效"));
            }
            
            List<String> codes = authService.getAuthCodes(username);
            return ResponseEntity.ok(Result.success(codes));
            
        } catch (Exception e) {
            log.error("获取权限码失败", e);
            return ResponseEntity.status(401).body(Result.fail("获取权限码失败：" + e.getMessage()));
        }
    }
    
    /**
     * 刷新访问令牌
     * POST /auth/refresh
     * @param refreshToken 刷新令牌（从 Cookie 中获取）
     * @return 新的访问令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<Result<String>> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        log.info("收到刷新令牌请求");
        
        try {
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.status(403).body(Result.fail("Refresh token is required"));
            }
            
            String newAccessToken = authService.refreshToken(refreshToken);
            if (newAccessToken == null) {
                return ResponseEntity.status(403).body(Result.fail("Invalid refresh token"));
            }
            
            return ResponseEntity.ok(Result.success(newAccessToken));
            
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return ResponseEntity.status(403).body(Result.fail("刷新令牌失败：" + e.getMessage()));
        }
    }
    
    /**
     * 从 token 中提取用户名（简化实现）
     */
    private String extractUsernameFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        // 移除 Bearer 前缀
        String accessToken = token;
        if (token.startsWith("Bearer ")) {
            accessToken = token.substring(7);
        }
        
        // 简化解析：access-token:username:nickname
        if (accessToken.startsWith("access-token:")) {
            String[] parts = accessToken.split(":");
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        
        return null;
    }
    
    /**
     * 用户登出
     * POST /auth/logout
     * @param refreshToken 刷新令牌（从 Cookie 中获取）
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<String>> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        log.info("收到登出请求");
        
        try {
            if (refreshToken != null && !refreshToken.trim().isEmpty()) {
                authService.logout(refreshToken);
            }
            
            return ResponseEntity.ok(Result.success(""));
            
        } catch (Exception e) {
            log.error("登出失败", e);
            return ResponseEntity.badRequest().body(Result.fail("登出失败：" + e.getMessage()));
        }
    }
}
