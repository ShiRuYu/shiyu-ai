package com.shiyu.ai.auth.controller;

import com.shiyu.ai.auth.request.*;
import com.shiyu.ai.auth.vo.*;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.UserService;
import com.shiyu.ai.auth.handler.LoginRateLimiter;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.auth.bo.UserBO;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

/**
 * 认证 Controller
 * 提供用户登录、登出等认证功能
 */
@Slf4j
@Tag(name = "Auth", description = "Auth")
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    private final LoginRateLimiter loginRateLimiter;
    
    public AuthController(AuthService authService, UserService userService, LoginRateLimiter loginRateLimiter) {
        this.authService = authService;
        this.userService = userService;
        this.loginRateLimiter = loginRateLimiter;
    }
    
    /**
     * 用户登录
     * POST /auth/login
     */
    @Operation(summary = "Login")
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求：username={}", request.getUsername());
        
        String clientIp = loginRateLimiter.getClientIp();
        if (!loginRateLimiter.isAllowed(clientIp)) {
            log.warn("登录频率超限，IP: {}, username: {}", clientIp, request.getUsername());
            return Result.fail("登录尝试过于频繁，请稍后再试");
        }
        
        try {
            if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return Result.fail("Username and password are required");
            }
            
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
     * 用户注册
     * POST /auth/register
     */
    @Operation(summary = "Register")
    @PostMapping("/register")
    public Result<LoginResponseVO> register(@Valid @RequestBody LoginRequest request) {
        log.info("收到注册请求: username={}", request.getUsername());
        try {
            LoginResponseVO response = authService.register(
                request.getUsername(), request.getPassword(), request.getEmail());
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 验证码登录
     * POST /auth/code-login
     */
    @Operation(summary = "Code Login")
    @PostMapping("/code-login")
    public Result<LoginResponseVO> codeLogin(@Valid @RequestBody CodeLoginRequest request) {
        log.info("收到验证码登录请求");
        try {
            LoginResponseVO response = authService.codeLogin(
                    request.getPhone(), request.getCode(), request.getCaptchaKey());
            return Result.success(response);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 忘记密码
     * POST /auth/forget-password
     */
    @Operation(summary = "Forget Password")
    @PostMapping("/forget-password")
    public Result<Boolean> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        log.info("收到忘记密码请求: email={}", request.getEmail());
        try {
            boolean success = authService.forgetPassword(
                    request.getEmail(), request.getNewPassword(),
                    request.getCode(), request.getCaptchaKey());
            return Result.success(success);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取用户权限码
     * GET /auth/codes
     */
    @Operation(summary = "Get Auth Codes")
    @GetMapping("/codes")
    public Result<List<String>> getAuthCodes() {
        log.info("收到获取权限码请求");
        try {
            Long userId = LoginContextHolder.getUserId();
            log.debug("当前登录用户 ID: {}", userId);
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
     */
    @Operation(summary = "Refresh Token")
    @PostMapping("/refresh")
    public Result<String> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("收到刷新令牌请求");
        try {
            String newAccessToken = authService.refreshToken(request.getAccessToken());
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
     * POST /auth/current-role
     */
    @Operation(summary = "Switch Current Role")
    @PostMapping("/current-role")
    public Result<SwitchContextResponse> switchCurrentRole(@Valid @RequestBody SwitchRoleRequest request) {
        log.info("收到切换角色请求");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("用户未登录");
        boolean success = authService.switchCurrentRole(userId, request.getRoleId());
        if (!success) return Result.fail("切换角色失败");
        return Result.success(buildSwitchContext(userId));
    }

    /**
     * 切换当前租户
     * POST /auth/switch-tenant
     */
    @Operation(summary = "Switch Tenant")
    @PostMapping("/switch-tenant")
    public Result<SwitchContextResponse> switchTenant(@Valid @RequestBody SwitchTenantRequest request) {
        log.info("收到切换租户请求");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("用户未登录");
        boolean success = authService.switchCurrentTenant(userId, request.getTenantId());
        if (!success) return Result.fail("切换租户失败");
        return Result.success(buildSwitchContext(userId));
    }

    /**
     * 构建切换后的完整上下文响应（消除 N+1 请求）
     */
    private SwitchContextResponse buildSwitchContext(Long userId) {
        UserBO userBO = userService.getUserDetail(userId);
        UserVO userVO = userBO != null ? MapstructUtils.convert(userBO, UserVO.class) : null;
        if (userVO != null) {
            try {
                userVO.setTenants(authService.getUserTenants(userId));
                if (userVO.getExtInfo() != null) {
                    var extMap = com.shiyu.ai.common.core.utils.JSONUtils.parseObject(
                            userVO.getExtInfo(), java.util.Map.class);
                    if (extMap != null) {
                        Object tid = extMap.get("currentTenantId");
                        if (tid instanceof Number) userVO.setCurrentTenantId(((Number) tid).longValue());
                        Object fid = extMap.get("filterTenantId");
                        if (fid instanceof Number) userVO.setFilterTenantId(((Number) fid).longValue());
                    }
                }
            } catch (Exception e) {
                log.warn("获取用户租户信息失败: {}", e.getMessage());
            }
        }
        return SwitchContextResponse.builder()
                .userInfo(userVO)
                .tenants(authService.getUserTenants(userId))
                .build();
    }

    /**
     * 获取用户租户列表
     * GET /auth/tenants
     */
    @Operation(summary = "Get User Tenants")
    @GetMapping("/tenants")
    public Result<List<TenantInfoVO>> getUserTenants() {
        log.info("获取用户租户列表");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("用户未登录");
        return Result.success(authService.getUserTenants(userId));
    }

    /**
     * 用户登出
     * POST /auth/logout
     */
    @Operation(summary = "Logout")
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
     * 设置子租户筛选器
     * POST /auth/scope-sub-tenant
     * 仅在根租户上下文中有效，设置后只查看该子租户的数据
     */
    @Operation(summary = "Filter Sub Tenant")
    @PostMapping("/scope-sub-tenant")
    public Result<Void> filterSubTenant(@Valid @RequestBody FilterSubTenantRequest request) {
        log.info("收到子租户筛选请求, subTenantId={}", request.getSubTenantId());
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("用户未登录");
        boolean success = authService.setScopedTenant(userId, request.getSubTenantId());
        return success ? Result.success() : Result.fail("设置子租户筛选器失败");
    }

    /**
     * 清除子租户筛选器
     * POST /auth/clear-scope
     * 回到根租户全量视角
     */
    @Operation(summary = "Clear Sub Tenant Filter")
    @PostMapping("/clear-scope")
    public Result<Void> clearFilter() {
        log.info("收到清除筛选器请求");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("用户未登录");
        boolean success = authService.clearScopedTenant(userId);
        return success ? Result.success() : Result.fail("清除筛选器失败");
    }

    private String extractTokenFromHeader(String tokenHeader) {
        if (tokenHeader == null || tokenHeader.trim().isEmpty()) return null;
        if (tokenHeader.startsWith("Bearer ")) return tokenHeader.substring(7);
        return tokenHeader;
    }
}
