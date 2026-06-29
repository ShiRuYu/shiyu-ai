package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.request.LoginRequest;
import com.shiyu.ai.auth.vo.LoginResponseVO;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.LoginRateLimiter;
import com.shiyu.ai.auth.vo.WorkspaceContextVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    private final LoginRateLimiter loginRateLimiter;
    
    public AuthController(AuthService authService, LoginRateLimiter loginRateLimiter) {
        this.authService = authService;
        this.loginRateLimiter = loginRateLimiter;
    }
    
    /**
     * 用户登录
     * POST /auth/login
     * @param request 登录请求（包含用户名、密码）
     * @return 登录响应（包含用户信息和访问令牌）
     */
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求：username={}", request.getUsername());
        
        String clientIp = loginRateLimiter.getClientIp();
        if (!loginRateLimiter.isAllowed(clientIp)) {
            log.warn("登录频率超限，IP: {}, username: {}", clientIp, request.getUsername());
            return Result.fail("登录尝试过于频繁，请稍后再试");
        }
        
        try {
            // 验证参数
            if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return Result.fail("Username and password are required");
            }
            
            // 调用登录服务（含角色选择）
            LoginResponseVO response = authService.login(request.getUsername(), request.getPassword(), request.getRoleId());
            
            if (response == null) {
                return Result.fail("Username or password is incorrect.");
            }
            
            loginRateLimiter.reset(clientIp);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("登录失败：username={}", request.getUsername(), e);
            return Result.fail("登录失败");
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
            Long userId = LoginContextHolder.getUserId();
            log.debug("当前登录用户 ID: {}", userId);
            
            // 通过用户 ID 查询权限码
            List<String> codes = authService.getAuthCodesByUserId(userId);
            return Result.success(codes);
            
        } catch (Exception e) {
            log.error("获取权限码失败", e);
            return Result.fail("获取权限码失败");
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
            return Result.fail("刷新令牌失败");
        }
    }
    

    /**
     * 切换当前角色
     * PATCH /auth/current-role
     */
    @PatchMapping("/current-role")
    public Result<Void> switchCurrentRole(@RequestBody Map<String, Long> body) {
        log.info("收到切换角色请求");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) {
            return Result.fail("用户未登录");
        }
        Long roleId = body.get("roleId");
        boolean success = authService.switchCurrentRole(userId, roleId);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("切换角色失败");
        }
    }

    /**
     * 切换当前租户
     * POST /auth/switch-tenant
     */
    @PostMapping("/switch-tenant")
    public Result<List<WorkspaceContextVO>> switchTenant(@RequestBody Map<String, Long> body) {
        log.info("收到切换租户请求");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("用户未登录");
        Long tenantId = body.get("tenantId");
        if (tenantId == null) return Result.fail("tenantId 不能为空");
        boolean success = authService.switchCurrentTenant(userId, tenantId);
        if (success) {
            List<WorkspaceContextVO> workspaces = authService.getUserWorkspaces(userId);
            return Result.success(workspaces);
        }
        return Result.fail("切换租户失败");
    }

    /**
     * 切换当前工作空间
     * POST /auth/switch-workspace
     */
    @PostMapping("/switch-workspace")
    public Result<Void> switchWorkspace(@RequestBody Map<String, Object> body) {
        log.info("收到切换工作空间请求");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("用户未登录");
        Long workspaceId = body.get("workspaceId") != null
                ? ((Number) body.get("workspaceId")).longValue() : null;
        boolean success = authService.switchCurrentWorkspace(userId, workspaceId);
        if (success) return Result.success();
        return Result.fail("切换工作空间失败");
    }

    /**
     * 获取用户工作空间列表
     * GET /auth/workspaces
     */
    @GetMapping("/workspaces")
    public Result<List<WorkspaceContextVO>> getUserWorkspaces() {
        log.info("获取用户工作空间列表");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("用户未登录");
        List<WorkspaceContextVO> workspaces = authService.getUserWorkspaces(userId);
        return Result.success(workspaces);
    }

    /**
     * 获取用户租户列表
     * GET /auth/tenants
     */
    @GetMapping("/tenants")
    public Result<List<Map<String, Object>>> getUserTenants() {
        log.info("获取用户租户列表");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("用户未登录");
        List<Map<String, Object>> tenants = authService.getUserTenants(userId);
        return Result.success(tenants);
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
            return Result.fail("登出失败");
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
