package com.shiyu.ai.agent.controller;

import com.shiyu.ai.agent.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 验证码 Controller
 * 提供验证码生成和验证功能
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class CaptchaController {
    
    private final CaptchaService captchaService;
    
    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }
    
    /**
     * 获取验证码
     * @return SVG 格式的验证码图片
     */
    @GetMapping("/captcha")
    public ResponseEntity<Map<String, Object>> getCaptcha() {
        log.info("收到验证码请求");
        
        try {
            // 生成验证码
            Map<String, Object> captchaData = captchaService.generateCaptcha();
            
            // 返回结果
            return ResponseEntity.ok(captchaData);
            
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "生成验证码失败：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 验证验证码
     * @param request 验证请求（包含 key 和 code）
     * @return 验证结果
     */
    @PostMapping("/captcha/validate")
    public ResponseEntity<Map<String, Object>> validateCaptcha(@RequestBody ValidateCaptchaRequest request) {
        log.info("收到验证码验证请求：key={}", request.getKey());
        
        try {
            // 验证验证码
            boolean valid = captchaService.validateCaptcha(request.getKey(), request.getCode());
            
            Map<String, Object> response = new HashMap<>();
            if (valid) {
                response.put("success", true);
                response.put("message", "验证码正确");
            } else {
                response.put("success", false);
                response.put("message", "验证码错误");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("验证验证码失败", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "验证验证码失败：" + e.getMessage()
            ));
        }
    }
    
    /**
     * 验证请求参数
     */
    @lombok.Data
    public static class ValidateCaptchaRequest {
        /**
         * 验证码 key
         */
        private String key;
        
        /**
         * 用户输入的验证码
         */
        private String code;
    }
}
