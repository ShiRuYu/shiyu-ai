package com.shiyu.ai.iam.implementation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 忘记密码请求 */
@Data
@Schema(description = "忘记密码请求")
public class ForgetPasswordRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * email 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "注册邮箱")
    private String email;

    /**
     * newPassword 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码")
    private String newPassword;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    @NotBlank(message = "验证码不能为空")
    @Schema(description = "邮箱验证码")
    private String code;

    /**
     * captchaKey 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "验证码Key不能为空")
    @Schema(description = "验证码Key（从 /captcha 接口获取）")
    private String captchaKey;
}
