package com.shiyu.ai.iam.implementation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 验证码登录请求 */
@Data
@Schema(description = "验证码登录请求")
public class CodeLoginRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * phone 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String phone;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    @NotBlank(message = "验证码不能为空")
    @Schema(description = "短信验证码")
    private String code;

    /**
     * captchaKey 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "验证码Key不能为空")
    @Schema(description = "验证码Key（从 /captcha 接口获取）")
    private String captchaKey;
}
