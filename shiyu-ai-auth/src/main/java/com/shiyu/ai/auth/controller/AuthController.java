package com.shiyu.ai.auth.controller;

import com.shiyu.ai.model.request.LoginRequest;
import com.shiyu.ai.model.vo.LoginResponseVO;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.LoginRateLimiter;
import com.shiyu.ai.model.vo.WorkspaceContextVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 璁よ瘉 Controller
 * 鎻愪緵鐢ㄦ埛鐧诲綍銆佺櫥鍑虹瓑璁よ瘉鍔熻兘
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
     * 鐢ㄦ埛鐧诲綍
     * POST /auth/login
     * @param request 鐧诲綍璇锋眰锛堝寘鍚敤鎴峰悕銆佸瘑鐮侊級
     * @return 鐧诲綍鍝嶅簲锛堝寘鍚敤鎴蜂俊鎭拰璁块棶浠ょ墝锛?
     */
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequest request) {
        log.info("鏀跺埌鐧诲綍璇锋眰锛歶sername={}", request.getUsername());
        
        String clientIp = loginRateLimiter.getClientIp();
        if (!loginRateLimiter.isAllowed(clientIp)) {
            log.warn("鐧诲綍棰戠巼瓒呴檺锛孖P: {}, username: {}", clientIp, request.getUsername());
            return Result.fail("鐧诲綍灏濊瘯杩囦簬棰戠箒锛岃绋嶅悗鍐嶈瘯");
        }
        
        try {
            // 楠岃瘉鍙傛暟
            if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return Result.fail("Username and password are required");
            }
            
            // 璋冪敤鐧诲綍鏈嶅姟锛堝惈瑙掕壊閫夋嫨锛?
            LoginResponseVO response = authService.login(request.getUsername(), request.getPassword(), request.getRoleId());
            
            if (response == null) {
                return Result.fail("Username or password is incorrect.");
            }
            
            loginRateLimiter.reset(clientIp);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("鐧诲綍澶辫触锛歶sername={}", request.getUsername(), e);
            return Result.fail("鐧诲綍澶辫触");
        }
    }
    
    /**
     * 鑾峰彇鐢ㄦ埛鏉冮檺鐮?
     * GET /auth/codes
     * @return 鏉冮檺鐮佸垪琛?
     */
    @GetMapping("/codes")
    public Result<List<String>> getAuthCodes() {
        log.info("鏀跺埌鑾峰彇鏉冮檺鐮佽姹?);
        
        try {
            // 浠?LoginHelper 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛 ID
            Long userId = LoginContextHolder.getUserId();
            log.debug("褰撳墠鐧诲綍鐢ㄦ埛 ID: {}", userId);
            
            // 閫氳繃鐢ㄦ埛 ID 鏌ヨ鏉冮檺鐮?
            List<String> codes = authService.getAuthCodesByUserId(userId);
            return Result.success(codes);
            
        } catch (Exception e) {
            log.error("鑾峰彇鏉冮檺鐮佸け璐?, e);
            return Result.fail("鑾峰彇鏉冮檺鐮佸け璐?);
        }
    }
    
    /**
     * 鍒锋柊璁块棶浠ょ墝
     * POST /auth/refresh
     * @param request 鍖呭惈鏃?token 鐨勮姹備綋
     * @return 鏂扮殑璁块棶浠ょ墝
     */
    @PostMapping("/refresh")
    public Result<String> refreshToken(@RequestBody Map<String, String> request) {
        log.info("鏀跺埌鍒锋柊浠ょ墝璇锋眰");
        
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
            log.error("鍒锋柊浠ょ墝澶辫触", e);
            return Result.fail("鍒锋柊浠ょ墝澶辫触");
        }
    }
    

    /**
     * 鍒囨崲褰撳墠瑙掕壊
     * PATCH /auth/current-role
     */
    @PatchMapping("/current-role")
    public Result<Void> switchCurrentRole(@RequestBody Map<String, Long> body) {
        log.info("鏀跺埌鍒囨崲瑙掕壊璇锋眰");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) {
            return Result.fail("鐢ㄦ埛鏈櫥褰?);
        }
        Long roleId = body.get("roleId");
        boolean success = authService.switchCurrentRole(userId, roleId);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("鍒囨崲瑙掕壊澶辫触");
        }
    }

    /**
     * 鍒囨崲褰撳墠绉熸埛
     * POST /auth/switch-tenant
     */
    @PostMapping("/switch-tenant")
    public Result<List<WorkspaceContextVO>> switchTenant(@RequestBody Map<String, Long> body) {
        log.info("鏀跺埌鍒囨崲绉熸埛璇锋眰");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("鐢ㄦ埛鏈櫥褰?);
        Long tenantId = body.get("tenantId");
        if (tenantId == null) return Result.fail("tenantId 涓嶈兘涓虹┖");
        boolean success = authService.switchCurrentTenant(userId, tenantId);
        if (success) {
            List<WorkspaceContextVO> workspaces = authService.getUserWorkspaces(userId);
            return Result.success(workspaces);
        }
        return Result.fail("鍒囨崲绉熸埛澶辫触");
    }

    /**
     * 鍒囨崲褰撳墠宸ヤ綔绌洪棿
     * POST /auth/switch-workspace
     */
    @PostMapping("/switch-workspace")
    public Result<Void> switchWorkspace(@RequestBody Map<String, Object> body) {
        log.info("鏀跺埌鍒囨崲宸ヤ綔绌洪棿璇锋眰");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("鐢ㄦ埛鏈櫥褰?);
        Long workspaceId = body.get("workspaceId") != null
                ? ((Number) body.get("workspaceId")).longValue() : null;
        boolean success = authService.switchCurrentWorkspace(userId, workspaceId);
        if (success) return Result.success();
        return Result.fail("鍒囨崲宸ヤ綔绌洪棿澶辫触");
    }

    /**
     * 鑾峰彇鐢ㄦ埛宸ヤ綔绌洪棿鍒楄〃
     * GET /auth/workspaces
     */
    @GetMapping("/workspaces")
    public Result<List<WorkspaceContextVO>> getUserWorkspaces() {
        log.info("鑾峰彇鐢ㄦ埛宸ヤ綔绌洪棿鍒楄〃");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("鐢ㄦ埛鏈櫥褰?);
        List<WorkspaceContextVO> workspaces = authService.getUserWorkspaces(userId);
        return Result.success(workspaces);
    }

    /**
     * 鑾峰彇鐢ㄦ埛绉熸埛鍒楄〃
     * GET /auth/tenants
     */
    @GetMapping("/tenants")
    public Result<List<Map<String, Object>>> getUserTenants() {
        log.info("鑾峰彇鐢ㄦ埛绉熸埛鍒楄〃");
        Long userId = LoginContextHolder.getUserId();
        if (userId == null) return Result.fail("鐢ㄦ埛鏈櫥褰?);
        List<Map<String, Object>> tenants = authService.getUserTenants(userId);
        return Result.success(tenants);
    }

    /**
     * 鐢ㄦ埛鐧诲嚭
     * POST /auth/logout
     * @param tokenHeader Authorization Header锛圔earer Token锛?
     * @return 鐧诲嚭缁撴灉
     */
    @PostMapping("/logout")
    public Result<String> logout(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader) {
        log.info("鏀跺埌鐧诲嚭璇锋眰");
        
        try {
            String token = extractTokenFromHeader(tokenHeader);
            if (token != null && !token.trim().isEmpty()) {
                authService.logout(token);
            }
            
            return Result.success("");
            
        } catch (Exception e) {
            log.error("鐧诲嚭澶辫触", e);
            return Result.fail("鐧诲嚭澶辫触");
        }
    }
    
    /**
     * 浠?Authorization Header 涓彁鍙?Token
     */
    private String extractTokenFromHeader(String tokenHeader) {
        if (tokenHeader == null || tokenHeader.trim().isEmpty()) {
            return null;
        }
        
        // 绉婚櫎 Bearer 鍓嶇紑
        if (tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7);
        }
        
        return tokenHeader;
    }
}
