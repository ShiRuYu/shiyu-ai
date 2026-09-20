package com.shiyu.ai.iam.implementation.web;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.iam.implementation.service.CaptchaService;
import com.shiyu.ai.iam.implementation.vo.CaptchaVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

/**
 * 处理 Captcha 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@Slf4j
@Tag(name = "Captcha", description = "Captcha")
@RestController
@RequestMapping("/api/iam/auth/captcha")
public class CaptchaController {

    /**
     * captchaService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final CaptchaService captchaService;

    /**
     * 执行 Captcha 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param captchaService 用于完成本次业务处理的 captchaService 参数。
     */
    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * 获取验证码
     *
     * @return SVG 格式的验证码图片
     */
    @Operation(summary = "Get Captcha")
    @GetMapping("")
    public Result<CaptchaVO> getCaptcha() {
        log.info("收到验证码请求");

        try {
            // 生成验证码
            CaptchaVO captchaVO = captchaService.generateCaptcha();

            // 返回结果
            return Result.success(captchaVO);

        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return Result.fail("生成验证码失败");
        }
    }

    /**
     * 验证验证码
     *
     * @param request 验证请求（包含 key 和 code）
     * @return 验证结果
     */
    @Operation(summary = "Validate Captcha")
    @PostMapping("/validate")
    public Result<ValidateCaptchaResponse> validateCaptcha(
            @Valid @RequestBody ValidateCaptchaRequest request) {
        log.info("收到验证码验证请求");

        try {
            // 验证验证码
            boolean valid = captchaService.validateCaptcha(request.getKey(), request.getCode());

            ValidateCaptchaResponse response;
            if (valid) {
                response = new ValidateCaptchaResponse(true, "验证码正确");
                return Result.success(response);
            } else {
                response = new ValidateCaptchaResponse(false, "验证码错误");
                return Result.success(response);
            }

        } catch (Exception e) {
            log.error("验证验证码失败", e);
            return Result.fail("验证验证码失败");
        }
    }

    /**
     * 封装 Validate Captcha 操作所需的请求条件和输入数据。
     */
    @lombok.Getter
    @lombok.Setter
    @lombok.AllArgsConstructor
    public static class ValidateCaptchaRequest {
        /** 验证码 key */
        private String key;

        /** 用户输入的验证码 */
        private String code;
    }

    /**
     * 封装 Validate Captcha 操作向调用方返回的传输数据。
     */
    @lombok.Getter
    @lombok.Setter
    @lombok.AllArgsConstructor
    public static class ValidateCaptchaResponse {
        /** 是否成功 */
        private Boolean success;

        /** 消息 */
        private String message;
    }
}
