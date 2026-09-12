package com.shiyu.ai.iam.implementation.web;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.enums.BizResultCode;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.iam.implementation.handler.LoginRateLimiter;
import com.shiyu.ai.iam.implementation.request.*;
import com.shiyu.ai.iam.implementation.service.AuthService;
import com.shiyu.ai.iam.implementation.service.UserService;
import com.shiyu.ai.iam.implementation.vo.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.contract.KnowledgeTenantProvisioning;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 认证 Controller 提供用户登录、登出等认证功能 */
@Slf4j
@Tag(name = "Auth", description = "Auth")
@RestController
@RequestMapping("/api/iam/auth")
public class AuthController {

    /**
     * authService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AuthService authService;
    /**
     * 用户服务，表示当前对象中的对应属性。
     */
    private final UserService userService;
    /**
     * loginRateLimiter 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final LoginRateLimiter loginRateLimiter;
    /**
     * knowledgeSpaceService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeTenantProvisioning knowledgeSpaceService;

    /**
     * {@code AuthController} 创建并初始化当前类型实例。
     *
     * @param authService 参数值，用于执行当前操作。
     * @param userService 参数值，用于执行当前操作。
     * @param loginRateLimiter 参数值，用于执行当前操作。
     * @param knowledgeSpaceService 参数值，用于执行当前操作。
     */
    public AuthController(
            AuthService authService,
            UserService userService,
            LoginRateLimiter loginRateLimiter,
            KnowledgeTenantProvisioning knowledgeSpaceService) {
        this.authService = authService;
        this.userService = userService;
        this.loginRateLimiter = loginRateLimiter;
        this.knowledgeSpaceService = knowledgeSpaceService;
    }

    /** 用户登录 POST /api/iam/auth/login */
    @Operation(summary = "Login")
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求：usernamePresent={}", request.getUsername() != null);

        String clientIp = loginRateLimiter.getClientIp();
        if (!loginRateLimiter.isAllowed(clientIp)) {
            log.warn("登录频率超限，usernamePresent={}", request.getUsername() != null);
            return Result.fail("登录尝试过于频繁，请稍后再试");
        }

        try {
            if (request.getUsername() == null
                    || request.getUsername().trim().isEmpty()
                    || request.getPassword() == null
                    || request.getPassword().trim().isEmpty()) {
                return Result.fail("Username and password are required");
            }

            LoginResponseVO response =
                    authService.login(
                            request.getUsername(),
                            request.getPassword(),
                            request.getRoleId(),
                            clientIp);

            if (response == null) {
                return Result.fail("Username or password is incorrect.");
            }

            loginRateLimiter.reset(clientIp);
            if (response.getCurrentTenantId() != null) {
                initializeTenantDefaultsWithAuditContext(response);
            }
            return Result.success(response);

        } catch (Exception e) {
            log.error("登录失败：usernamePresent={}", request.getUsername() != null, e);
            return Result.fail("登录失败");
        }
    }

    /**
     * 初始化租户defaultswithaudit上下文。
     *
     * @param response 响应对象。
     */
    private void initializeTenantDefaultsWithAuditContext(LoginResponseVO response) {
        UserContext loginContext = new UserContext();
        loginContext.setUserId(response.getId());
        loginContext.setUsername(response.getUsername());
        loginContext.setHomeTenantId(response.getHomeTenantId());
        loginContext.setCurrentTenantId(response.getCurrentTenantId());
        loginContext.setSwitchMode(response.getSwitchMode());
        TenantId tenantId = new TenantId(response.getCurrentTenantId());
        ActorContextHttpAdapter.runWithContext(
                loginContext,
                tenantId,
                () -> knowledgeSpaceService.initializeTenantDefaults(tenantId));
    }

    /** 用户注册 POST /api/iam/auth/register */
    @Operation(summary = "Register")
    @PostMapping("/register")
    public Result<LoginResponseVO> register(@Valid @RequestBody LoginRequest request) {
        log.info("收到注册请求: usernamePresent={}", request.getUsername() != null);
        try {
            LoginResponseVO response =
                    authService.register(
                            request.getUsername(), request.getPassword(), request.getEmail());
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.fail("注册失败，请检查输入");
        }
    }

    /** 验证码登录 POST /api/iam/auth/code-login */
    @Operation(summary = "Code Login")
    @PostMapping("/code-login")
    public Result<LoginResponseVO> codeLogin(@Valid @RequestBody CodeLoginRequest request) {
        log.info("收到验证码登录请求");
        try {
            LoginResponseVO response =
                    authService.codeLogin(
                            request.getPhone(), request.getCode(), request.getCaptchaKey());
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            return Result.fail("验证码登录失败，请检查输入");
        } catch (Exception e) {
            log.error("验证码登录失败", e);
            return Result.fail(BizResultCode.ERROR);
        }
    }

    /** 忘记密码 POST /api/iam/auth/forget-password */
    @Operation(summary = "Forget Password")
    @PostMapping("/forget-password")
    public Result<Boolean> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        log.info("收到忘记密码请求: emailPresent={}", request.getEmail() != null);
        try {
            boolean success =
                    authService.forgetPassword(
                            request.getEmail(), request.getNewPassword(),
                            request.getCode(), request.getCaptchaKey());
            return Result.success(success);
        } catch (IllegalArgumentException e) {
            return Result.fail("找回密码失败，请检查输入");
        }
    }

    /** 获取用户权限码 GET /api/iam/auth/codes */
    @Operation(summary = "Get Auth Codes")
    @GetMapping("/codes")
    public Result<List<String>> getAuthCodes() {
        log.info("收到获取权限码请求");
        try {
            UserId userId = new UserId(ActorContextHttpAdapter.userId());
            log.debug("当前登录用户上下文已解析: userIdPresent={}", userId.value() > 0);
            List<String> codes =
                    authService.getAuthCodesByUserId(
                            ActorContextHttpAdapter.currentActor(), userId);
            return Result.success(codes);

        } catch (Exception e) {
            log.error("获取权限码失败", e);
            return Result.fail("获取权限码失败");
        }
    }

    /** 刷新访问令牌 POST /api/iam/auth/refresh */
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

    /** 切换当前角色 POST /api/iam/auth/current-role */
    @Operation(summary = "Switch Current Role")
    @PostMapping("/current-role")
    public Result<SwitchContextResponse> switchCurrentRole(
            @Valid @RequestBody SwitchRoleRequest request) {
        log.info("收到切换角色请求");
        long userId = ActorContextHttpAdapter.userId();
        boolean success = authService.switchCurrentRole(userId, request.getRoleId());
        if (!success) return Result.fail("切换角色失败");
        knowledgeSpaceService.initializeTenantDefaults(
                new TenantId(ActorContextHttpAdapter.tenantId()));
        return Result.success(buildSwitchContext(userId));
    }

    /** 切换当前租户 POST /api/iam/auth/switch-tenant */
    @Operation(summary = "Switch Tenant")
    @PostMapping("/switch-tenant")
    public Result<SwitchContextResponse> switchTenant(
            @Valid @RequestBody SwitchTenantRequest request) {
        log.info("收到切换租户请求");
        long userId = ActorContextHttpAdapter.userId();
        TenantId tenantId =
                request.getTenantId() == null ? null : new TenantId(request.getTenantId());
        boolean success = authService.switchCurrentTenant(userId, tenantId);
        if (success) {
            knowledgeSpaceService.initializeTenantDefaults(
                    new TenantId(ActorContextHttpAdapter.tenantId()));
        }
        if (!success) return Result.fail("切换租户失败");
        return Result.success(buildSwitchContext(userId));
    }

    /** 构建切换后的完整上下文响应（消除 N+1 请求） */
    private SwitchContextResponse buildSwitchContext(Long userId) {
        UserVO userVO = userService.detailView(ActorContextHttpAdapter.currentActor(), userId);
        if (userVO != null) {
            try {
                userVO.setTenants(
                        authService.getUserTenants(ActorContextHttpAdapter.currentActor(), userId));
                if (userVO.getExtInfo() != null) {
                    var extMap =
                            com.shiyu.ai.common.core.utils.JSONUtils.parseObject(
                                    userVO.getExtInfo(), java.util.Map.class);
                    if (extMap != null) {
                        Object tid = extMap.get("currentTenantId");
                        if (tid instanceof Number)
                            userVO.setCurrentTenantId(((Number) tid).longValue());
                        Object homeTid = extMap.get("homeTenantId");
                        if (homeTid instanceof Number)
                            userVO.setHomeTenantId(((Number) homeTid).longValue());
                        Object mode = extMap.get("switchMode");
                        if (mode instanceof String) userVO.setSwitchMode((String) mode);
                    }
                }
            } catch (Exception e) {
                log.warn(
                        "获取用户租户信息失败: errorType={}, errorMessageLength={}",
                        e.getClass().getSimpleName(),
                        valueLength(e.getMessage()));
            }
        }
        return SwitchContextResponse.builder()
                .userInfo(userVO)
                .tenants(authService.getUserTenants(ActorContextHttpAdapter.currentActor(), userId))
                .build();
    }

    private int valueLength(String value) {
        return value == null ? 0 : value.length();
    }

    /** 获取用户租户列表 GET /api/iam/auth/tenants */
    @Operation(summary = "Get User Tenants")
    @GetMapping("/tenants")
    public Result<List<TenantInfoVO>> getUserTenants() {
        log.info("获取用户租户列表");
        long userId = ActorContextHttpAdapter.userId();
        return Result.success(
                authService.getUserTenants(ActorContextHttpAdapter.currentActor(), userId));
    }

    /** 用户登出 POST /api/iam/auth/logout */
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

    private String extractTokenFromHeader(String tokenHeader) {
        if (tokenHeader == null || tokenHeader.trim().isEmpty()) return null;
        if (tokenHeader.startsWith("Bearer ")) return tokenHeader.substring(7);
        return tokenHeader;
    }
}
