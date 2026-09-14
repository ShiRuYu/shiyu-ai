package com.shiyu.ai.iam.implementation.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code LoginRequest} 表示平台模块的请求参数，承载调用方提交的输入数据。
 */
@Data
@Schema(description = "登录/注册请求")
public class LoginRequest implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * username 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    /**
     * password 属性，保存当前对象中的业务数据或协作依赖。
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码")
    private String password;

    /**
     * captcha 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "验证码")
    private String captcha;

    /**
     * captchaKey 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "验证码Key（从 /captcha 接口获取）")
    private String captchaKey;

    /**
     * 角色标识，表示当前对象中的对应属性。
     */
    @Schema(description = "登录后默认角色ID（可选）")
    private Long roleId;

    /**
     * email 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "邮箱（注册时必填）")
    private String email;

    /**
     * phone 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Schema(description = "手机号")
    private String phone;
}
