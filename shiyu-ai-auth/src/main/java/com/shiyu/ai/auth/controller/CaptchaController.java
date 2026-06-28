package com.shiyu.ai.auth.controller;

import com.shiyu.ai.model.vo.CaptchaVO;
import com.shiyu.ai.auth.service.CaptchaService;
import com.shiyu.ai.common.core.api.Result;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 楠岃瘉鐮?Controller
 * 鎻愪緵楠岃瘉鐮佺敓鎴愬拰楠岃瘉鍔熻兘
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
     * 鑾峰彇楠岃瘉鐮?
     * @return SVG 鏍煎紡鐨勯獙璇佺爜鍥剧墖
     */
    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        log.info("鏀跺埌楠岃瘉鐮佽姹?);
        
        try {
            // 鐢熸垚楠岃瘉鐮?
            CaptchaVO captchaVO = captchaService.generateCaptcha();
            
            // 杩斿洖缁撴灉
            return Result.success(captchaVO);
            
        } catch (Exception e) {
            log.error("鐢熸垚楠岃瘉鐮佸け璐?, e);
            return Result.fail("鐢熸垚楠岃瘉鐮佸け璐?);
        }
    }
    
    /**
     * 楠岃瘉楠岃瘉鐮?
     * @param request 楠岃瘉璇锋眰锛堝寘鍚?key 鍜?code锛?
     * @return 楠岃瘉缁撴灉
     */
    @PostMapping("/captcha/validate")
    public Result<ValidateCaptchaResponse> validateCaptcha(@Valid @RequestBody ValidateCaptchaRequest request) {
        log.info("鏀跺埌楠岃瘉鐮侀獙璇佽姹傦細key={}", request.getKey());
        
        try {
            // 楠岃瘉楠岃瘉鐮?
            boolean valid = captchaService.validateCaptcha(request.getKey(), request.getCode());
            
            ValidateCaptchaResponse response;
            if (valid) {
                response = new ValidateCaptchaResponse(true, "楠岃瘉鐮佹纭?);
                return Result.success(response);
            } else {
                response = new ValidateCaptchaResponse(false, "楠岃瘉鐮侀敊璇?);
                return Result.success(response);
            }
            
        } catch (Exception e) {
            log.error("楠岃瘉楠岃瘉鐮佸け璐?, e);
            return Result.fail("楠岃瘉楠岃瘉鐮佸け璐?);
        }
    }
    
    /**
     * 楠岃瘉璇锋眰鍙傛暟
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ValidateCaptchaRequest {
        /**
         * 楠岃瘉鐮?key
         */
        private String key;
        
        /**
         * 鐢ㄦ埛杈撳叆鐨勯獙璇佺爜
         */
        private String code;
    }
    
    /**
     * 楠岃瘉鍝嶅簲
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ValidateCaptchaResponse {
        /**
         * 鏄惁鎴愬姛
         */
        private Boolean success;
        
        /**
         * 娑堟伅
         */
        private String message;
    }
}
