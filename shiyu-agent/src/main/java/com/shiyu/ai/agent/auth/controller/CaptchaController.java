package com.shiyu.ai.agent.auth.controller;

import com.shiyu.ai.agent.domain.vo.CaptchaVO;
import com.shiyu.ai.agent.auth.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CaptchaVO> getCaptcha() {
        log.info("收到验证码请求");
        
        try {
            // 生成验证码
            CaptchaVO captchaVO = captchaService.generateCaptcha();
            
            // 返回结果
            return ResponseEntity.ok(captchaVO);
            
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 验证验证码
     * @param request 验证请求（包含 key 和 code）
     * @return 验证结果
     */
    @PostMapping("/captcha/validate")
    public ResponseEntity<ValidateCaptchaResponse> validateCaptcha(@RequestBody ValidateCaptchaRequest request) {
        log.info("收到验证码验证请求：key={}", request.getKey());
        
        try {
            // 验证验证码
            boolean valid = captchaService.validateCaptcha(request.getKey(), request.getCode());
            
            ValidateCaptchaResponse response;
            if (valid) {
                response = new ValidateCaptchaResponse(true, "验证码正确");
            } else {
                response = new ValidateCaptchaResponse(false, "验证码错误");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("验证验证码失败", e);
            return ResponseEntity.badRequest().body(new ValidateCaptchaResponse(false, "验证验证码失败：" + e.getMessage()));
        }
    }
    
    /**
     * 验证请求参数
     */
    @lombok.Data
    @lombok.AllArgsConstructor
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
    
    /**
     * 验证响应
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ValidateCaptchaResponse {
        /**
         * 是否成功
         */
        private Boolean success;
        
        /**
         * 消息
         */
        private String message;
    }
}
