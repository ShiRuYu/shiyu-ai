package com.shiyu.ai.agent.auth.controller;

import com.shiyu.ai.agent.domain.request.LoginRequest;
import com.shiyu.ai.agent.domain.vo.LoginVO;
import com.shiyu.ai.agent.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
     * @param request 登录请求（包含用户名、密码、验证码）
     * @return 登录响应（包含访问令牌）
     */
    @PostMapping("/login")
    public ResponseEntity<LoginVO> login(@RequestBody LoginRequest request) {
        log.info("收到登录请求：username={}", request.getUsername());
        
        try {
            // 调用登录服务
            LoginVO response = authService.login(
                    request.getUsername(),
                    request.getPassword(),
                    request.getCaptcha(),
                    request.getCaptchaKey()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("登录失败：username={}", request.getUsername(), e);
            
            // 构建错误响应
            LoginVO errorResponse = new LoginVO();
            errorResponse.setCode(1);
            errorResponse.setMessage("登录失败：" + e.getMessage());
            errorResponse.setData(null);
            errorResponse.setOriginUrl("/auth/login");
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 切换当前角色
     * @param roleCode 角色编码
     * @param token 访问令牌（从 Header 中获取）
     * @return 登录响应（包含新的访问令牌）
     */
    @PostMapping("/current-role/switch/{roleCode}")
    public ResponseEntity<LoginVO> switchCurrentRole(
            @PathVariable String roleCode,
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("收到切换角色请求：roleCode={}", roleCode);
        
        try {
            // 从 token 中提取用户名（简化处理，实际项目中应该解析 JWT）
            String username = extractUsernameFromToken(token);
            if (username == null || username.trim().isEmpty()) {
                LoginVO errorResponse = new LoginVO();
                errorResponse.setCode(1);
                errorResponse.setMessage("未认证或 token 无效");
                errorResponse.setData(null);
                errorResponse.setOriginUrl("/auth/current-role/switch/" + roleCode);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 调用切换角色服务
            LoginVO response = authService.switchCurrentRole(username, roleCode);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("切换角色失败：roleCode={}", roleCode, e);
            
            LoginVO errorResponse = new LoginVO();
            errorResponse.setCode(1);
            errorResponse.setMessage("切换角色失败：" + e.getMessage());
            errorResponse.setData(null);
            errorResponse.setOriginUrl("/auth/current-role/switch/" + roleCode);
            
            return ResponseEntity.badRequest().body(errorResponse);
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
     * @param token 访问令牌（从 Header 中获取）
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("收到登出请求");
        
        try {
            // 移除 Bearer 前缀
            String accessToken = token;
            if (token != null && token.startsWith("Bearer ")) {
                accessToken = token.substring(7);
            }
            
            // 调用登出服务
            if (accessToken != null && !accessToken.trim().isEmpty()) {
                authService.logout(accessToken);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("message", "登出成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("登出失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 1);
            response.put("message", "登出失败：" + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
